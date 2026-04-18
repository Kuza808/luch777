package com.restaurant.luch.controllers;

import com.restaurant.luch.cache.AdminDataCache;
import com.restaurant.luch.models.Dish;
import com.restaurant.luch.models.User;
import com.restaurant.luch.services.CartService;
import com.restaurant.luch.services.RestaurantService;
import com.restaurant.luch.utils.ImageCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class MainController implements Initializable {

    private static List<Dish> cachedDishes;
    private static boolean dishesLoaded = false;

    @FXML private GridPane dishesGrid;
    @FXML private Label emptyLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private Label cartItemCountLabel;
    @FXML private Button adminButton;
    @FXML private Button retryButton;

    private RestaurantService restaurantService;
    private CartService cartService;
    private Stage primaryStage;

    private List<Dish> allDishes;
    private String currentCategory = "Все категории";
    private String currentSearch = "";

    private final Map<Integer, Boolean> favoriteCache = new ConcurrentHashMap<>();
    private final AtomicInteger retryCount = new AtomicInteger(0);
    private static final int MAX_RETRIES = 3;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        restaurantService = RestaurantService.getInstance();
        cartService = CartService.getInstance();
        setupCategoryFilter();
        updateAdminButtonVisibility();
        updateCartCount();

        if (dishesLoaded && cachedDishes != null) {
            allDishes = cachedDishes;
            Platform.runLater(() -> {
                populateCategories(cachedDishes);
                filterDishes();
                emptyLabel.setText("");
                if (retryButton != null) retryButton.setVisible(false);
            });
            refreshDishesInBackground();
        } else {
            loadDishesAsync();
        }

        if (retryButton != null) {
            retryButton.setVisible(false);
            retryButton.setOnAction(e -> {
                retryButton.setVisible(false);
                emptyLabel.setText("Загрузка...");
                loadDishesAsync();
            });
        }

        if (!AdminDataCache.isLoaded()) {
            AdminDataCache.loadAllAsync(restaurantService);
        }
    }

    private void refreshDishesInBackground() {
        new Thread(() -> {
            try {
                List<Dish> freshDishes = restaurantService.getAllDishes();
                if (freshDishes != null) {
                    cachedDishes = freshDishes;
                    dishesLoaded = true;
                    Platform.runLater(() -> {
                        allDishes = cachedDishes;
                        populateCategories(cachedDishes);
                        filterDishes();
                    });
                }
            } catch (Exception e) {
                System.err.println("Ошибка фонового обновления блюд: " + e.getMessage());
            }
        }).start();
    }

    private void updateCartCount() {
        if (cartItemCountLabel != null) {
            int totalItems = cartService.getCartItems().values().stream().mapToInt(Integer::intValue).sum();
            cartItemCountLabel.setText(String.valueOf(totalItems));
        }
    }

    private void updateAdminButtonVisibility() {
        if (adminButton != null) {
            boolean isAdmin = RestaurantService.isCurrentUserAdmin();
            adminButton.setVisible(isAdmin);
            adminButton.setManaged(isAdmin);
        }
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void loadDishesAsync() {
        new Thread(() -> {
            try {
                System.out.println("MainController: загрузка блюд...");
                List<Dish> dishes = restaurantService.getAllDishes();
                System.out.println("Получено блюд из БД: " + dishes.size());
                retryCount.set(0);
                cachedDishes = dishes;
                dishesLoaded = true;
                Platform.runLater(() -> {
                    allDishes = dishes;
                    populateCategories(dishes);
                    filterDishes();
                    if (retryButton != null) retryButton.setVisible(false);
                    emptyLabel.setText(dishes.isEmpty() ? "Меню пусто. Добавьте блюда в Supabase." : "");
                });
            } catch (Exception e) {
                e.printStackTrace();
                int attempts = retryCount.incrementAndGet();
                if (attempts <= MAX_RETRIES) {
                    System.out.println("Повторная попытка " + attempts + " из " + MAX_RETRIES + " через 2 сек");
                    try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                    loadDishesAsync();
                } else {
                    Platform.runLater(() -> {
                        emptyLabel.setText("Ошибка загрузки: " + e.getMessage());
                        if (retryButton != null) {
                            retryButton.setVisible(true);
                            retryButton.setText("Повторить");
                        }
                    });
                }
            }
        }).start();
    }

    private void populateCategories(List<Dish> dishes) {
        Set<String> categories = dishes.stream()
                .map(Dish::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
        categoryCombo.getItems().clear();
        categoryCombo.getItems().add("Все категории");
        categoryCombo.getItems().addAll(categories);
        categoryCombo.setValue("Все категории");
    }

    private void displayDishes(List<Dish> dishes) {
        dishesGrid.getChildren().clear();
        int column = 0;
        int row = 0;
        for (Dish dish : dishes) {
            VBox card = createDishCard(dish);
            dishesGrid.add(card, column, row);
            column++;
            if (column >= 4) {
                column = 0;
                row++;
            }
        }
        emptyLabel.setVisible(dishes.isEmpty());
        dishesGrid.setVisible(!dishes.isEmpty());
        dishesGrid.setAlignment(Pos.CENTER);
        dishesGrid.requestLayout();
    }

    private VBox createDishCard(Dish dish) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #FFD2A1; -fx-border-color: #808847; -fx-border-radius: 8px; -fx-padding: 15px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5px);");
        card.setPrefWidth(220);
        card.setAlignment(Pos.TOP_CENTER);

        ImageView imageView = createDishImageView(dish);

        Label nameLabel = new Label(dish.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0C6038;");
        nameLabel.setWrapText(true);

        Label descriptionLabel = new Label(
                dish.getDescription() != null && !dish.getDescription().isEmpty() ? dish.getDescription() : "Нет описания"
        );
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2D4C39;");
        descriptionLabel.setWrapText(true);

        Label ingredientsLabel = new Label();
        if (dish.getIngredients() != null && !dish.getIngredients().isEmpty()) {
            ingredientsLabel.setText("Состав: " + String.join(", ", dish.getIngredients()));
        } else {
            ingredientsLabel.setText("Состав не указан");
        }
        ingredientsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6C27DA;");
        ingredientsLabel.setWrapText(true);

        Label priceLabel = new Label(String.format("%.0f ₽", dish.getPrice()));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #6C27DA;");

        Button favButton = new Button();
        favButton.setStyle("-fx-background-color: transparent; -fx-font-size: 20px; -fx-text-fill: #0C6038; -fx-cursor: hand;");
        boolean isFav = favoriteCache.getOrDefault(dish.getId(), false);
        favButton.setText(isFav ? "♥" : "♡");
        favButton.setDisable(RestaurantService.getCurrentUser() == null);
        favButton.setOnAction(e -> toggleFavorite(dish, favButton));

        Node quantityControl = createQuantityControl(dish, card);

        card.getChildren().addAll(imageView, nameLabel, descriptionLabel, ingredientsLabel, priceLabel, favButton, quantityControl);
        return card;
    }

    private ImageView createDishImageView(Dish dish) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);
        imageView.setImage(ImageCache.getImage(dish.getImageUrl(), 180, 120, true, true));
        return imageView;
    }

    private Node createQuantityControl(Dish dish, VBox parentCard) {
        int qty = cartService.getQuantity(dish);
        if (qty == 0) {
            Button addButton = new Button("В корзину");
            addButton.setStyle("-fx-background-color: #808847; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8px 15px; -fx-background-radius: 5; -fx-cursor: hand;");
            addButton.setOnAction(e -> {
                cartService.addToCart(dish);
                updateCartCount();
                replaceCardContent(dish, parentCard);
            });
            return addButton;
        } else {
            HBox controls = new HBox(5);
            controls.setAlignment(Pos.CENTER);

            Button minusBtn = new Button("-");
            minusBtn.setStyle("-fx-background-color: #6C27DA; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 5; -fx-cursor: hand;");
            minusBtn.setPrefSize(35, 35);
            minusBtn.setOnAction(e -> {
                cartService.removeOne(dish);
                updateCartCount();
                replaceCardContent(dish, parentCard);
            });

            Label qtyLabel = new Label(String.valueOf(qty));
            qtyLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-min-width: 40px; -fx-alignment: center; -fx-text-fill: #0C6038;");

            Button plusBtn = new Button("+");
            plusBtn.setStyle("-fx-background-color: #0C6038; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 5; -fx-cursor: hand;");
            plusBtn.setPrefSize(35, 35);
            plusBtn.setOnAction(e -> {
                if (qty < 20) {
                    cartService.addToCart(dish);
                    updateCartCount();
                    replaceCardContent(dish, parentCard);
                } else {
                    showAlert("Внимание", "Нельзя добавить больше 20 штук одного блюда");
                }
            });

            controls.getChildren().addAll(minusBtn, qtyLabel, plusBtn);
            return controls;
        }
    }

    private void replaceCardContent(Dish dish, VBox card) {
        int row = GridPane.getRowIndex(card) == null ? 0 : GridPane.getRowIndex(card);
        int col = GridPane.getColumnIndex(card) == null ? 0 : GridPane.getColumnIndex(card);
        dishesGrid.getChildren().remove(card);
        VBox newCard = createDishCard(dish);
        dishesGrid.add(newCard, col, row);
    }

    private void toggleFavorite(Dish dish, Button btn) {
        User currentUser = RestaurantService.getCurrentUser();
        if (currentUser == null) {
            showAlert("Внимание", "Чтобы добавить в избранное, войдите в систему");
            return;
        }

        btn.setDisable(true);

        new Thread(() -> {
            try {
                boolean isFav = restaurantService.isFavorite(currentUser.getId(), dish.getId());
                if (isFav) {
                    restaurantService.removeFromFavorites(currentUser.getId(), dish.getId());
                } else {
                    restaurantService.addToFavorites(currentUser.getId(), dish.getId());
                }
                favoriteCache.put(dish.getId(), !isFav);
                Platform.runLater(() -> {
                    btn.setText(!isFav ? "♥" : "♡");
                    btn.setDisable(false);
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Ошибка", "Не удалось обновить избранное: " + e.getMessage());
                    btn.setDisable(false);
                });
            }
        }).start();
    }

    private void setupCategoryFilter() {
        categoryCombo.setValue("Все категории");
        categoryCombo.setOnAction(e -> filterDishes());
        searchField.textProperty().addListener((obs, old, val) -> filterDishes());
    }

    private void filterDishes() {
        currentCategory = categoryCombo.getValue();
        currentSearch = searchField.getText().toLowerCase();

        if (allDishes == null) {
            emptyLabel.setText("Загрузка меню...");
            return;
        }

        List<Dish> filtered = allDishes.stream()
                .filter(d -> (currentCategory == null || currentCategory.equals("Все категории")
                        || d.getCategory().equalsIgnoreCase(currentCategory)))
                .filter(d -> d.getName().toLowerCase().contains(currentSearch) ||
                        (d.getDescription() != null && d.getDescription().toLowerCase().contains(currentSearch)))
                .collect(Collectors.toList());
        displayDishes(filtered);
    }

    @FXML private void searchDishes() { filterDishes(); }
    @FXML private void goToCabinet() { loadView("cabinet.fxml", "Личный кабинет"); }
    @FXML private void goToCart() { loadView("cart.fxml", "Корзина"); }
    @FXML private void goToBooking() { loadView("booking.fxml", "Бронирование столика"); }
    @FXML private void goToAdmin() {
        if (RestaurantService.isCurrentUserAdmin()) {
            loadView("admin.fxml", "Админ-панель");
        } else {
            showAlert("Доступ запрещён", "У вас нет прав администратора");
        }
    }
    @FXML private void logout() {
        RestaurantService.logout();
        loadView("auth.fxml", "Вход");
    }

    private void loadView(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/" + fxml));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof AuthController) ((AuthController) controller).setMainController(this);
            else if (controller instanceof CabinetController) ((CabinetController) controller).setMainController(this);
            else if (controller instanceof CartController) ((CartController) controller).setMainController(this);
            else if (controller instanceof BookingController) ((BookingController) controller).setMainController(this);
            else if (controller instanceof AdminController) ((AdminController) controller).setMainController(this);
            primaryStage.setTitle("Ресторан Луч - " + title);
            primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить экран: " + fxml);
        }
    }

    public void showMainContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            primaryStage.getScene().setRoot(root);
            primaryStage.setTitle("Ресторан Луч - Главная");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}