package com.restaurant.luch.controllers;

import com.restaurant.luch.models.Booking;
import com.restaurant.luch.models.Dish;
import com.restaurant.luch.models.Order;
import com.restaurant.luch.models.User;
import com.restaurant.luch.services.CartService;
import com.restaurant.luch.services.RestaurantService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class CabinetController implements Initializable {

    @FXML private StackPane contentArea;

    @FXML private VBox profileSection;
    @FXML private Label userNameLabel, userEmailLabel, userPhoneLabel, userBirthdayLabel;
    @FXML private TextField editNameField, editEmailField, editPhoneField;
    @FXML private DatePicker editBirthdayPicker;

    @FXML private VBox ordersSection;
    @FXML private TableView<Order> myOrdersTable;
    @FXML private Label noOrdersLabel;

    @FXML private VBox bookingsSection;
    @FXML private TableView<Booking> myBookingsTable;
    @FXML private Label noBookingsLabel;

    @FXML private ScrollPane favoritesSection;
    @FXML private GridPane favoritesGrid;

    private MainController mainController;
    private User currentUser;
    private RestaurantService restaurantService;
    private CartService cartService;

    private ObservableList<Order> ordersData = FXCollections.observableArrayList();
    private ObservableList<Booking> bookingsData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        restaurantService = RestaurantService.getInstance();
        cartService = CartService.getInstance();
        currentUser = RestaurantService.getCurrentUser();

        if (currentUser == null) {
            Platform.runLater(() -> {
                showAlert("Ошибка", "Необходимо авторизоваться");
                goBack();
            });
            return;
        }

        setupTables();
        loadUserProfile();
        loadUserOrders();
        loadUserBookings();
        showProfile();
    }

    private void setupTables() {
        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("№ заказа");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Order, String> orderDateCol = new TableColumn<>("Дата");
        orderDateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOrderDate() != null ?
                        cellData.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : ""));

        TableColumn<Order, Double> orderTotalCol = new TableColumn<>("Сумма");
        orderTotalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        TableColumn<Order, String> orderStatusCol = new TableColumn<>("Статус");
        orderStatusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatusDisplay()));

        myOrdersTable.getColumns().setAll(orderIdCol, orderDateCol, orderTotalCol, orderStatusCol);
        myOrdersTable.setItems(ordersData);

        TableColumn<Booking, LocalDate> bookingDateCol = new TableColumn<>("Дата");
        bookingDateCol.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));

        TableColumn<Booking, String> bookingTimeCol = new TableColumn<>("Время");
        bookingTimeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBookingTime() != null ?
                        cellData.getValue().getBookingTime().toString() : ""));

        TableColumn<Booking, Integer> guestsCol = new TableColumn<>("Гостей");
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("guestsCount"));

        TableColumn<Booking, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatusDisplay()));

        TableColumn<Booking, String> tableCol = new TableColumn<>("Столик");
        tableCol.setCellValueFactory(new PropertyValueFactory<>("tableType"));

        myBookingsTable.getColumns().setAll(bookingDateCol, bookingTimeCol, guestsCol, tableCol, statusCol);
        myBookingsTable.setItems(bookingsData);
    }

    private void loadUserProfile() {
        if (currentUser != null) {
            userNameLabel.setText(currentUser.getName());
            userEmailLabel.setText(currentUser.getEmail());
            userPhoneLabel.setText(currentUser.getPhone());
            userBirthdayLabel.setText("—");

            editNameField.setText(currentUser.getName());
            editEmailField.setText(currentUser.getEmail());
            editPhoneField.setText(currentUser.getPhone());
        }
    }

    private void loadUserOrders() {
        new Thread(() -> {
            try {
                List<Order> orders = restaurantService.getOrdersByUserId(currentUser.getId());
                Platform.runLater(() -> {
                    ordersData.clear();
                    ordersData.addAll(orders);
                    noOrdersLabel.setVisible(orders.isEmpty());
                    myOrdersTable.setVisible(!orders.isEmpty());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить заказы"));
                e.printStackTrace();
            }
        }).start();
    }

    private void loadUserBookings() {
        new Thread(() -> {
            try {
                List<Booking> bookings = restaurantService.getBookingsByUserId(currentUser.getId());
                Platform.runLater(() -> {
                    bookingsData.clear();
                    bookingsData.addAll(bookings);
                    noBookingsLabel.setVisible(bookings.isEmpty());
                    myBookingsTable.setVisible(!bookings.isEmpty());
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить бронирования"));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML private void showProfile() { setVisiblePane(profileSection); }
    @FXML private void showOrders() { setVisiblePane(ordersSection); loadUserOrders(); }
    @FXML private void showBookings() { setVisiblePane(bookingsSection); loadUserBookings(); }
    @FXML private void showFavorites() { setVisiblePane(favoritesSection); loadFavorites(); }

    private void setVisiblePane(Node pane) {
        for (Node child : contentArea.getChildren()) {
            child.setVisible(false);
            child.setManaged(false);
        }
        pane.setVisible(true);
        pane.setManaged(true);
    }

    private void loadFavorites() {
        new Thread(() -> {
            try {
                List<Dish> favDishes = restaurantService.getFavoriteDishes(currentUser.getId());
                Platform.runLater(() -> displayFavorites(favDishes));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить избранное: " + e.getMessage()));
            }
        }).start();
    }

    private void displayFavorites(List<Dish> dishes) {
        favoritesGrid.getChildren().clear();
        int column = 0, row = 0;
        for (Dish dish : dishes) {
            VBox card = createFavoriteCard(dish);
            favoritesGrid.add(card, column, row);
            column++;
            if (column >= 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createFavoriteCard(Dish dish) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #FFD2A1; -fx-border-color: #808847; -fx-border-radius: 8; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        card.setPrefWidth(200);
        card.setAlignment(Pos.TOP_CENTER);
        populateFavoriteCard(card, dish);
        return card;
    }

    private void populateFavoriteCard(VBox card, Dish dish) {
        card.getChildren().clear();

        ImageView imageView = createDishImageView(dish);
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(dish.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0C6038;");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("%.0f ₽", dish.getPrice()));
        priceLabel.setStyle("-fx-text-fill: #6C27DA; -fx-font-weight: bold;");

        Node quantityControl = createQuantityControl(dish, card);

        Button removeFavBtn = new Button("Удалить");
        removeFavBtn.setStyle("-fx-background-color: #6C27DA; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
        removeFavBtn.setOnAction(e -> {
            removeFromFavorites(dish);
            loadFavorites();
        });

        card.getChildren().addAll(imageView, nameLabel, priceLabel, quantityControl, removeFavBtn);
    }

    private ImageView createDishImageView(Dish dish) {
        ImageView imageView = new ImageView();
        try {
            if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
                imageView.setImage(new Image(dish.getImageUrl(), 180, 120, true, true, true));
            } else {
                InputStream is = getClass().getResourceAsStream("/images/placeholder.png");
                if (is != null) {
                    imageView.setImage(new Image(is, 180, 120, true, true));
                } else {
                    imageView.setImage(new Image(new ByteArrayInputStream(new byte[0])));
                }
            }
        } catch (Exception e) {
            imageView.setImage(new Image(new ByteArrayInputStream(new byte[0])));
        }
        return imageView;
    }

    private Node createQuantityControl(Dish dish, VBox parentCard) {
        int qty = cartService.getQuantity(dish);
        if (qty == 0) {
            Button addButton = new Button("В корзину");
            addButton.setStyle("-fx-background-color: #808847; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 5 10; -fx-background-radius: 5; -fx-cursor: hand;");
            addButton.setOnAction(e -> {
                cartService.addToCart(dish);
                populateFavoriteCard(parentCard, dish);
            });
            return addButton;
        } else {
            HBox controls = new HBox(5);
            controls.setAlignment(Pos.CENTER);

            Button minusBtn = new Button("-");
            minusBtn.setStyle("-fx-background-color: #6C27DA; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
            minusBtn.setPrefSize(30, 30);
            minusBtn.setOnAction(e -> {
                cartService.removeOne(dish);
                populateFavoriteCard(parentCard, dish);
            });

            Label qtyLabel = new Label(String.valueOf(qty));
            qtyLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 30; -fx-alignment: center; -fx-text-fill: #0C6038;");

            Button plusBtn = new Button("+");
            plusBtn.setStyle("-fx-background-color: #0C6038; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
            plusBtn.setPrefSize(30, 30);
            plusBtn.setOnAction(e -> {
                int currentQty = cartService.getQuantity(dish);
                if (currentQty < 20) {
                    cartService.addToCart(dish);
                    populateFavoriteCard(parentCard, dish);
                } else {
                    showAlert("Внимание", "Нельзя добавить больше 20 штук одного блюда");
                }
            });

            controls.getChildren().addAll(minusBtn, qtyLabel, plusBtn);
            return controls;
        }
    }

    private void removeFromFavorites(Dish dish) {
        new Thread(() -> {
            try {
                restaurantService.removeFromFavorites(currentUser.getId(), dish.getId());
                Platform.runLater(() -> showAlert("Успех", "Блюдо удалено из избранного"));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось удалить: " + e.getMessage()));
            }
        }).start();
    }

    @FXML private void saveProfile() {
        String newName = editNameField.getText().trim();
        String newEmail = editEmailField.getText().trim();
        String newPhone = editPhoneField.getText().trim();

        if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
            showAlert("Ошибка", "Имя, email и телефон не могут быть пустыми");
            return;
        }

        if (!isValidName(newName)) {
            showAlert("Ошибка", "Имя может содержать только буквы, пробелы и дефисы");
            return;
        }
        if (!isValidEmail(newEmail)) {
            showAlert("Ошибка", "Некорректный формат email");
            return;
        }
        if (!isValidPhone(newPhone)) {
            showAlert("Ошибка", "Некорректный формат телефона (пример: +7 999 123-45-67)");
            return;
        }

        currentUser.setName(newName);
        currentUser.setEmail(newEmail);
        currentUser.setPhone(newPhone);

        new Thread(() -> {
            try {
                restaurantService.updateUser(currentUser);
                Platform.runLater(() -> {
                    userNameLabel.setText(newName);
                    userEmailLabel.setText(newEmail);
                    userPhoneLabel.setText(newPhone);
                    showAlert("Успех", "Профиль обновлён");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось сохранить: " + e.getMessage()));
            }
        }).start();
    }

    @FXML private void cancelEdit() { loadUserProfile(); }

    @FXML private void logout() {
        RestaurantService.logout();
        if (mainController != null) mainController.showMainContent();
    }

    @FXML private void goBack() {
        if (mainController != null) mainController.showMainContent();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidName(String name) {
        return name != null && !name.trim().isEmpty() && name.matches("[а-яА-ЯёЁa-zA-Z\\s-]+");
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        if (phone == null || phone.isEmpty()) return false;
        String digits = phone.replaceAll("[^\\d]", "");
        return digits.length() == 11 && (digits.startsWith("7") || digits.startsWith("8"));
    }
}