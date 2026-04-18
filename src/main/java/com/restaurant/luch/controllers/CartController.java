package com.restaurant.luch.controllers;

import com.restaurant.luch.config.AppConfig;
import com.restaurant.luch.models.Dish;
import com.restaurant.luch.models.Order;
import com.restaurant.luch.models.OrderItem;
import com.restaurant.luch.services.CartService;
import com.restaurant.luch.services.RestaurantService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class CartController implements Initializable {

    @FXML private VBox cartItemsContainer;
    @FXML private Label emptyCartLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> deliveryCombo;
    @FXML private ComboBox<String> paymentCombo;
    @FXML private TextArea commentArea;
    @FXML private Button checkoutBtn;
    @FXML private Button backBtn;

    private CartService cartService;
    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cartService = CartService.getInstance();

        deliveryCombo.getItems().setAll("Самовывоз (0 ₽)", "Доставка (200 ₽)");
        deliveryCombo.setValue("Самовывоз (0 ₽)");
        deliveryCombo.setOnAction(e -> updateTotalDisplay());

        paymentCombo.getItems().setAll("💳 Карта", "💰 Наличные", "📱 QR-код (Яндекс.Касса)");
        paymentCombo.setValue("💳 Карта");

        checkoutBtn.setOnAction(e -> checkout());
        backBtn.setOnAction(e -> goBack());

        updateCartDisplay();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void goBack() {
        if (mainController != null) mainController.showMainContent();
    }

    @FXML
    private void updateTotal() {
        updateTotalDisplay();
    }

    private void updateCartDisplay() {
        cartItemsContainer.getChildren().clear();
        if (cartService.isEmpty()) {
            emptyCartLabel.setVisible(true);
            cartItemsContainer.setVisible(false);
        } else {
            emptyCartLabel.setVisible(false);
            cartItemsContainer.setVisible(true);
            for (var entry : cartService.getCartItems().entrySet()) {
                Dish dish = entry.getKey();
                int quantity = entry.getValue();
                HBox itemBox = createCartItemBox(dish, quantity);
                cartItemsContainer.getChildren().add(itemBox);
            }
        }
        updateTotalDisplay();
        checkoutBtn.setDisable(cartService.isEmpty());
    }

    private HBox createCartItemBox(Dish dish, int quantity) {
        HBox itemBox = new HBox(10);
        itemBox.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0; -fx-padding: 10px;");
        itemBox.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(3);
        infoBox.setPrefWidth(200);
        Label nameLabel = new Label(dish.getName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0C6038;");
        Label priceLabel = new Label(String.format("%.0f ₽ за шт.", dish.getPrice()));
        priceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2D4C39;");
        infoBox.getChildren().addAll(nameLabel, priceLabel);

        HBox quantityBox = new HBox(5);
        quantityBox.setAlignment(Pos.CENTER);
        quantityBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 4px; -fx-padding: 2px;");

        Button minusBtn = new Button("-");
        minusBtn.setPrefSize(30, 30);
        minusBtn.setStyle("-fx-background-color: #6C27DA; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        minusBtn.setOnAction(e -> {
            cartService.removeOne(dish);
            updateCartDisplay();
        });

        Label qtyLabel = new Label(String.valueOf(quantity));
        qtyLabel.setStyle("-fx-font-size: 14px; -fx-min-width: 30px; -fx-alignment: center; -fx-text-fill: #0C6038;");

        Button plusBtn = new Button("+");
        plusBtn.setPrefSize(30, 30);
        plusBtn.setStyle("-fx-background-color: #0C6038; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 5; -fx-background-radius: 5; -fx-cursor: hand;");
        plusBtn.setOnAction(e -> {
            int currentQty = cartService.getQuantity(dish);
            if (currentQty < 20) {
                cartService.addToCart(dish);
                updateCartDisplay();
            } else {
                showAlert("Внимание", "Нельзя добавить больше 20 штук одного блюда");
            }
        });

        quantityBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);

        double itemTotal = dish.getPrice() * quantity;
        Label itemTotalLabel = new Label(String.format("%.0f ₽", itemTotal));
        itemTotalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #6C27DA; -fx-min-width: 70px; -fx-alignment: center-right;");

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-font-size: 14px; -fx-background-color: transparent; -fx-text-fill: #6C27DA; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            cartService.removeCompletely(dish);
            updateCartDisplay();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        itemBox.getChildren().addAll(infoBox, quantityBox, spacer, itemTotalLabel, deleteBtn);
        return itemBox;
    }

    private void updateTotalDisplay() {
        double subtotal = cartService.getTotalPrice();
        double delivery = deliveryCombo.getValue().contains("200") ? 200.0 : 0.0;
        double tax = subtotal * 0.10;
        double total = subtotal + delivery + tax;

        subtotalLabel.setText(String.format("%.0f ₽", subtotal));
        taxLabel.setText(String.format("%.0f ₽", tax));
        totalLabel.setText(String.format("%.0f ₽", total));
    }

    @FXML
    private void clearCart() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Очистка корзины");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы уверены, что хотите очистить корзину?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            cartService.clear();
            updateCartDisplay();
        }
    }

    @FXML
    private void checkout() {
        if (RestaurantService.getCurrentUser() == null) {
            showAlert("Требуется авторизация", "Для оформления заказа необходимо войти в систему.");
            return;
        }

        if (cartService.isEmpty()) {
            showAlert("Ошибка", "Корзина пуста");
            return;
        }

        new Thread(() -> {
            try {
                Order order = new Order();
                order.setTotalAmount(cartService.getTotalPrice());
                order.setStatus(AppConfig.ORDER_STATUS_PENDING);
                String orderType = deliveryCombo.getValue().contains("Самовывоз")
                        ? AppConfig.ORDER_TYPE_TAKEOUT
                        : AppConfig.ORDER_TYPE_DELIVERY;
                order.setOrderType(orderType);
                order.setSpecialRequests(commentArea.getText());
                order.setOrderDate(LocalDateTime.now());

                RestaurantService service = RestaurantService.getInstance();
                Order savedOrder = service.addOrder(order);
                if (savedOrder == null || savedOrder.getId() == null) {
                    throw new Exception("Не удалось получить ID созданного заказа");
                }

                List<OrderItem> orderItems = new ArrayList<>();
                for (var entry : cartService.getCartItems().entrySet()) {
                    Dish dish = entry.getKey();
                    int quantity = entry.getValue();
                    OrderItem item = new OrderItem();
                    item.setOrderId(savedOrder.getId());
                    item.setDishId(dish.getId());
                    item.setQuantity(quantity);
                    item.setPriceAtOrder(dish.getPrice());
                    orderItems.add(item);
                }

                service.addOrderItems(orderItems);

                Platform.runLater(() -> {
                    cartService.clear();
                    updateCartDisplay();

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Заказ оформлен");
                    success.setHeaderText(null);
                    success.setContentText("✅ Ваш заказ принят!\n\nСумма: " + totalLabel.getText() +
                            "\nСпособ оплаты: " + paymentCombo.getValue() +
                            "\nТип: " + orderType);
                    success.showAndWait();

                    goBack();
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось оформить заказ: " + e.getMessage()));
            }
        }).start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}