package com.restaurant.luch.controllers;

import com.restaurant.luch.cache.AdminDataCache;
import com.restaurant.luch.config.AppConfig;
import com.restaurant.luch.models.*;
import com.restaurant.luch.services.RestaurantService;
import com.restaurant.luch.services.SupabaseStorageService;
import com.restaurant.luch.utils.SupabaseApiClient;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private VBox dishesList, addDishForm, ordersList, bookingsList, usersList;
    @FXML private TableView<Dish> dishesTable;
    @FXML private TableView<Order> ordersTable;
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableView<User> usersTable;
    @FXML private TextField searchDishField;
    @FXML private ComboBox<String> orderStatusFilter;
    @FXML private ComboBox<String> bookingStatusFilter;
    @FXML private TextField userSearchField;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Label loadingStatusLabel;

    @FXML private TextField dishNameField;
    @FXML private ComboBox<String> dishCategoryCombo;
    @FXML private TextField dishPriceField;
    @FXML private CheckBox dishAvailableCheckBox;
    @FXML private TextArea dishDescriptionArea;
    @FXML private TextArea dishIngredientsArea;
    @FXML private Label selectedImageLabel;

    private ObservableList<Dish> dishesData = FXCollections.observableArrayList();
    private ObservableList<Order> ordersData = FXCollections.observableArrayList();
    private ObservableList<Booking> bookingsData = FXCollections.observableArrayList();
    private ObservableList<User> usersData = FXCollections.observableArrayList();

    private Map<Integer, String> userNames = new HashMap<>();

    private MainController mainController;
    private RestaurantService restaurantService;
    private File selectedImageFile;
    private Dish editingDish = null;

    private ExecutorService executor = Executors.newFixedThreadPool(4);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("AdminController: initialize started");
        try {
            if (!RestaurantService.isCurrentUserAdmin()) {
                System.err.println("AdminController: user is not admin");
                Platform.runLater(() -> {
                    showAlert("Ошибка", "У вас нет прав доступа к админ-панели");
                    if (mainController != null) mainController.showMainContent();
                });
                return;
            }

            restaurantService = RestaurantService.getInstance();
            setupUI();
            loadAdminData();
            System.out.println("AdminController: initialize finished");
        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert("Ошибка", "Ошибка инициализации: " + e.getMessage()));
        }
    }

    public void setMainController(MainController controller) {
        this.mainController = controller;
    }

    private void setupUI() {
        setupDishesTable();
        setupOrdersTable();
        setupBookingsTable();
        setupUsersTable();

        orderStatusFilter.setItems(FXCollections.observableArrayList(
                "Все", "PENDING", "CONFIRMED", "PREPARING", "READY", "COMPLETED", "CANCELLED"
        ));
        orderStatusFilter.setValue("Все");
        orderStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterOrders());

        bookingStatusFilter.setItems(FXCollections.observableArrayList(
                "Все", "PENDING", "CONFIRMED", "SEATED", "COMPLETED", "CANCELLED"
        ));
        bookingStatusFilter.setValue("Все");
        bookingStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> filterBookings());

        userSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterUsers());

        showAllDishes();
    }

    @FXML private void goBack() {
        if (mainController != null) mainController.showMainContent();
    }

    @FXML private void showAllDishes() {
        setVisiblePane(dishesList);
    }

    @FXML private void showAddDishForm() {
        setVisiblePane(addDishForm);
        clearAddDishForm();
        editingDish = null;
    }

    @FXML private void showEditDishForm() {
        Dish selected = dishesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editDish(selected);
        } else {
            showAlert("Ошибка", "Выберите блюдо из списка");
        }
    }

    @FXML private void showDeleteDishForm() {
        Dish selected = dishesTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            deleteDish(selected);
        } else {
            showAlert("Ошибка", "Выберите блюдо для удаления");
        }
    }

    @FXML private void showAllOrders() {
        setVisiblePane(ordersList);
    }

    @FXML private void confirmOrder() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateOrderStatus(selected.getId(), "CONFIRMED");
        } else {
            showAlert("Ошибка", "Выберите заказ");
        }
    }

    @FXML private void markDelivered() {
        Order selected = ordersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateOrderStatus(selected.getId(), "READY");
        } else {
            showAlert("Ошибка", "Выберите заказ");
        }
    }

    @FXML private void showAllBookings() {
        setVisiblePane(bookingsList);
    }

    @FXML private void confirmBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateBookingStatus(selected.getId(), AppConfig.BOOKING_STATUS_CONFIRMED);
        } else {
            showAlert("Ошибка", "Выберите бронирование из списка");
        }
    }

    @FXML private void cancelBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            updateBookingStatus(selected.getId(), AppConfig.BOOKING_STATUS_CANCELLED);
        } else {
            showAlert("Ошибка", "Выберите бронирование из списка");
        }
    }

    @FXML private void showAllUsers() {
        setVisiblePane(usersList);
    }

    @FXML private void searchDishes() { filterDishes(); }
    @FXML private void refreshOrders() { loadOrders(); }
    @FXML private void refreshBookings() { loadBookings(); }
    @FXML private void searchUsers() { filterUsers(); }

    private void editDish(Dish dish) {
        editingDish = dish;
        setVisiblePane(addDishForm);

        dishNameField.setText(dish.getName());
        dishCategoryCombo.setValue(dish.getCategory());
        dishPriceField.setText(String.valueOf(dish.getPrice()));
        dishAvailableCheckBox.setSelected(dish.isAvailable());
        dishDescriptionArea.setText(dish.getDescription());
        if (dish.getIngredients() != null) {
            dishIngredientsArea.setText(String.join(", ", dish.getIngredients()));
        } else {
            dishIngredientsArea.clear();
        }

        if (dish.getImageUrl() != null && !dish.getImageUrl().isEmpty()) {
            selectedImageLabel.setText("(текущее фото будет сохранено, если не выбрано новое)");
        } else {
            selectedImageLabel.setText("(нет фото)");
        }
        selectedImageFile = null;
    }

    @FXML private void saveDish() {
        try {
            String name = dishNameField.getText().trim();
            String category = dishCategoryCombo.getValue();
            double price = Double.parseDouble(dishPriceField.getText().trim());
            boolean available = dishAvailableCheckBox.isSelected();
            String description = dishDescriptionArea.getText().trim();
            String ingredientsText = dishIngredientsArea.getText().trim();

            if (name.isEmpty() || category == null) {
                showAlert("Ошибка", "Заполните обязательные поля");
                return;
            }

            boolean isNew = (editingDish == null);
            Dish dish;
            if (isNew) {
                dish = new Dish();
            } else {
                dish = editingDish;
            }

            dish.setName(name);
            dish.setCategory(category);
            dish.setPrice(price);
            dish.setAvailable(available);
            dish.setDescription(description);
            if (!ingredientsText.isEmpty()) {
                dish.setIngredients(Arrays.asList(ingredientsText.split("\\s*,\\s*")));
            } else {
                dish.setIngredients(null);
            }

            if (isNew) {
                Dish createdDish = restaurantService.addDish(dish);
                if (selectedImageFile != null) {
                    uploadDishImage(createdDish, selectedImageFile);
                } else {
                    Platform.runLater(() -> {
                        showAlert("Успех", "Блюдо добавлено");
                        refreshDishesAfterChange();
                    });
                }
            } else {
                restaurantService.updateDish(dish);
                if (selectedImageFile != null) {
                    uploadDishImage(dish, selectedImageFile);
                } else {
                    Platform.runLater(() -> {
                        showAlert("Успех", "Блюдо обновлено");
                        refreshDishesAfterChange();
                    });
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Некорректная цена");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", e.getMessage());
        }
    }

    @FXML private void cancelDishForm() { showAllDishes(); }
    @FXML private void selectDishImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображение");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.jpg", "*.png", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedImageFile = file;
            selectedImageLabel.setText(file.getName());
        }
    }

    private void uploadDishImage(Dish dish, File file) {
        new Thread(() -> {
            try {
                String fileName = "dish_" + dish.getId() + "_" + System.currentTimeMillis();
                String imageUrl = SupabaseStorageService.uploadImage(file, fileName);
                dish.setImageUrl(imageUrl);
                restaurantService.updateDish(dish);
                Platform.runLater(() -> {
                    showAlert("Успех", "Блюдо " + (editingDish == null ? "добавлено" : "обновлено") + " с фото");
                    refreshDishesAfterChange();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить фото: " + e.getMessage()));
            }
        }).start();
    }

    private void refreshDishesAfterChange() {
        new Thread(() -> {
            try {
                List<Dish> freshDishes = restaurantService.getAllDishes();
                AdminDataCache.setDishes(freshDishes);
                Platform.runLater(() -> {
                    dishesData.setAll(freshDishes);
                    showAllDishes();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось обновить список блюд: " + e.getMessage()));
            }
        }).start();
    }

    private void updateOrderStatus(int orderId, String newStatus) {
        new Thread(() -> {
            try {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", newStatus);
                SupabaseApiClient.adminPatch(AppConfig.TABLE_ORDERS, "id", orderId, updates);
                loadOrders();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка обновления статуса заказа: " + e.getMessage()));
            }
        }).start();
    }

    private void deleteOrder(Order order) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление заказа");
        confirm.setContentText("Удалить заказ №" + order.getId() + "? Это действие необратимо.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    restaurantService.deleteOrder(order.getId());
                    loadOrders();
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Ошибка", "Не удалось удалить заказ: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void updateBookingStatus(int bookingId, String newStatus) {
        new Thread(() -> {
            try {
                Map<String, Object> updates = new HashMap<>();
                updates.put("status", newStatus);
                SupabaseApiClient.adminPatch(AppConfig.TABLE_BOOKINGS, "id", bookingId, updates);
                loadBookings();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка обновления статуса бронирования: " + e.getMessage()));
            }
        }).start();
    }

    private void deleteBooking(Booking booking) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление бронирования");
        confirm.setContentText("Удалить бронирование №" + booking.getId() + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    restaurantService.deleteBooking(booking.getId());
                    loadBookings();
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Ошибка", "Не удалось удалить бронирование: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void deleteDish(Dish dish) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удаление блюда");
        confirm.setContentText("Удалить блюдо " + dish.getName() + "?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                try {
                    restaurantService.deleteDish(dish.getId());
                    refreshDishesAfterChange();
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Ошибка", e.getMessage()));
                }
            }).start();
        }
    }

    private void toggleUserBlock(User user) {
        boolean newBlocked = !user.isBlocked();
        user.setBlocked(newBlocked);
        new Thread(() -> {
            try {
                restaurantService.updateUser(user);
                loadUsers();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", e.getMessage()));
            }
        }).start();
    }

    private void deleteUser(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Удалить пользователя?");
        confirm.setContentText("Удалить пользователя " + user.getEmail() + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    restaurantService.deleteUser(user.getId());
                    loadUsers();
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> showAlert("Ошибка", e.getMessage()));
                }
            }).start();
        }
    }

    private void loadAdminData() {
        List<Dish> cachedDishes = AdminDataCache.getDishes();
        List<Order> cachedOrders = AdminDataCache.getOrders();
        List<Booking> cachedBookings = AdminDataCache.getBookings();
        List<User> cachedUsers = AdminDataCache.getUsers();

        if (cachedDishes != null && cachedOrders != null && cachedBookings != null && cachedUsers != null) {
            Platform.runLater(() -> {
                usersData.setAll(cachedUsers);
                userNames.clear();
                for (User u : cachedUsers) {
                    userNames.put(u.getId(), u.getName());
                }
                dishesData.setAll(cachedDishes);
                ordersData.setAll(cachedOrders);
                bookingsData.setAll(cachedBookings);

                filterOrders();
                filterBookings();
                filterUsers();

                if (loadingIndicator != null) loadingIndicator.setVisible(false);
                if (loadingStatusLabel != null) loadingStatusLabel.setText("Готово (из кэша)");
            });
            return;
        }

        Platform.runLater(() -> {
            if (loadingIndicator != null) loadingIndicator.setVisible(true);
            if (loadingStatusLabel != null) loadingStatusLabel.setText("Загрузка данных...");
        });

        executor.submit(() -> {
            try {
                updateLoadingStatus("Загрузка пользователей...");
                List<User> users = restaurantService.getAllUsers();
                AdminDataCache.setUsers(users);
                Platform.runLater(() -> {
                    usersData.setAll(users);
                    userNames.clear();
                    for (User u : users) userNames.put(u.getId(), u.getName());
                    filterUsers();
                });
            } catch (Exception e) {
                handleLoadError(e, "пользователей");
            }
        });

        executor.submit(() -> {
            try {
                updateLoadingStatus("Загрузка блюд...");
                List<Dish> dishes = restaurantService.getAllDishes();
                AdminDataCache.setDishes(dishes);
                Platform.runLater(() -> {
                    dishesData.setAll(dishes);
                    dishesTable.setItems(dishesData);
                });
            } catch (Exception e) {
                handleLoadError(e, "блюд");
            }
        });

        executor.submit(() -> {
            try {
                updateLoadingStatus("Загрузка заказов...");
                List<Order> orders = restaurantService.getAllOrdersAdmin();
                AdminDataCache.setOrders(orders);
                Platform.runLater(() -> {
                    ordersData.setAll(orders);
                    filterOrders();
                });
            } catch (Exception e) {
                handleLoadError(e, "заказов");
            }
        });

        executor.submit(() -> {
            try {
                updateLoadingStatus("Загрузка бронирований...");
                List<Booking> bookings = restaurantService.getAllBookingsAdmin();
                AdminDataCache.setBookings(bookings);
                Platform.runLater(() -> {
                    bookingsData.setAll(bookings);
                    filterBookings();
                });
            } catch (Exception e) {
                handleLoadError(e, "бронирований");
            }
        });

        executor.submit(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    if (loadingIndicator != null) loadingIndicator.setVisible(false);
                    if (loadingStatusLabel != null) loadingStatusLabel.setText("Готово");
                });
            } catch (InterruptedException ignored) {}
        });
    }

    private void handleLoadError(Exception e, String entity) {
        e.printStackTrace();
        Platform.runLater(() -> {
            showAlert("Ошибка загрузки " + entity, e.getMessage());
            if (loadingIndicator != null) loadingIndicator.setVisible(false);
            if (loadingStatusLabel != null) loadingStatusLabel.setText("Ошибка загрузки " + entity);
        });
    }

    private void updateLoadingStatus(String message) {
        Platform.runLater(() -> {
            if (loadingStatusLabel != null) loadingStatusLabel.setText(message);
        });
    }

    private void loadOrders() {
        new Thread(() -> {
            try {
                List<Order> orders = restaurantService.getAllOrdersAdmin();
                AdminDataCache.setOrders(orders);
                Platform.runLater(() -> {
                    ordersData.setAll(orders);
                    filterOrders();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить заказы: " + e.getMessage()));
            }
        }).start();
    }

    private void loadBookings() {
        new Thread(() -> {
            try {
                List<Booking> bookings = restaurantService.getAllBookingsAdmin();
                AdminDataCache.setBookings(bookings);
                Platform.runLater(() -> {
                    bookingsData.setAll(bookings);
                    filterBookings();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить бронирования: " + e.getMessage()));
            }
        }).start();
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                List<User> users = restaurantService.getAllUsers();
                AdminDataCache.setUsers(users);
                Platform.runLater(() -> {
                    usersData.setAll(users);
                    userNames.clear();
                    for (User u : users) userNames.put(u.getId(), u.getName());
                    filterUsers();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить пользователей: " + e.getMessage()));
            }
        }).start();
    }

    private void filterDishes() {
        String query = searchDishField.getText().toLowerCase();
        if (query.isEmpty()) {
            dishesTable.setItems(dishesData);
        } else {
            ObservableList<Dish> filtered = FXCollections.observableArrayList();
            for (Dish d : dishesData) {
                if (d.getName().toLowerCase().contains(query) ||
                        d.getCategory().toLowerCase().contains(query)) {
                    filtered.add(d);
                }
            }
            dishesTable.setItems(filtered);
        }
    }

    private void filterOrders() {
        String selected = orderStatusFilter.getValue();
        if (selected == null || "Все".equals(selected)) {
            ordersTable.setItems(ordersData);
        } else {
            ObservableList<Order> filtered = FXCollections.observableArrayList();
            for (Order o : ordersData) {
                if (selected.equalsIgnoreCase(o.getStatus())) {
                    filtered.add(o);
                }
            }
            ordersTable.setItems(filtered);
        }
    }

    private void filterBookings() {
        String selected = bookingStatusFilter.getValue();
        if (selected == null || "Все".equals(selected)) {
            bookingsTable.setItems(bookingsData);
        } else {
            ObservableList<Booking> filtered = FXCollections.observableArrayList();
            for (Booking b : bookingsData) {
                if (selected.equalsIgnoreCase(b.getStatus())) {
                    filtered.add(b);
                }
            }
            bookingsTable.setItems(filtered);
        }
    }

    private void filterUsers() {
        String query = userSearchField.getText().toLowerCase();
        if (query.isEmpty()) {
            usersTable.setItems(usersData);
        } else {
            ObservableList<User> filtered = FXCollections.observableArrayList();
            for (User u : usersData) {
                if (u.getEmail().toLowerCase().contains(query) ||
                        (u.getName() != null && u.getName().toLowerCase().contains(query))) {
                    filtered.add(u);
                }
            }
            usersTable.setItems(filtered);
        }
    }

    private void setupDishesTable() {
        TableColumn<Dish, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Dish, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Dish, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Dish, Double> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Dish, String> descCol = new TableColumn<>("Описание");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Dish, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setCellFactory(col -> new TableCell<Dish, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox pane = new HBox(5, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #808847; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #6C27DA; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                editBtn.setOnAction(e -> {
                    Dish dish = getTableView().getItems().get(getIndex());
                    editDish(dish);
                });
                deleteBtn.setOnAction(e -> {
                    Dish dish = getTableView().getItems().get(getIndex());
                    deleteDish(dish);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        dishesTable.getColumns().setAll(idCol, nameCol, categoryCol, priceCol, descCol, actionsCol);
        dishesTable.setItems(dishesData);
    }

    private void setupOrdersTable() {
        TableColumn<Order, Integer> idCol = new TableColumn<>("ID заказа");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Order, String> userCol = new TableColumn<>("Клиент");
        userCol.setCellValueFactory(cellData -> {
            Integer userId = cellData.getValue().getUserId();
            String name = userId != null ? userNames.getOrDefault(userId, "ID " + userId) : "Гость";
            return new SimpleStringProperty(name);
        });

        TableColumn<Order, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOrderDate() != null ?
                        cellData.getValue().getOrderDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : ""));

        TableColumn<Order, Double> totalCol = new TableColumn<>("Сумма");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));

        TableColumn<Order, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatusDisplay()));

        TableColumn<Order, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setCellFactory(col -> new TableCell<Order, Void>() {
            private final ComboBox<String> statusCombo = new ComboBox<>();
            private final Button saveBtn = new Button("✓");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox pane = new HBox(5, statusCombo, saveBtn, deleteBtn);

            {
                statusCombo.getItems().setAll(AppConfig.getAllOrderStatuses());
                saveBtn.setStyle("-fx-background-color: #0C6038; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #6C27DA; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                saveBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    String newStatus = statusCombo.getValue();
                    if (newStatus != null && !newStatus.equals(order.getStatus())) {
                        updateOrderStatus(order.getId(), newStatus);
                    }
                });
                deleteBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    deleteOrder(order);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    statusCombo.setValue(order.getStatus());
                    setGraphic(pane);
                }
            }
        });

        ordersTable.getColumns().setAll(idCol, userCol, dateCol, totalCol, statusCol, actionsCol);
        ordersTable.setItems(ordersData);
    }

    private void setupBookingsTable() {
        TableColumn<Booking, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Booking, String> guestCol = new TableColumn<>("Гость");
        guestCol.setCellValueFactory(new PropertyValueFactory<>("guestName"));

        TableColumn<Booking, String> phoneCol = new TableColumn<>("Телефон");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Booking, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBookingDate().toString()));

        TableColumn<Booking, String> timeCol = new TableColumn<>("Время");
        timeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getBookingTime().toString()));

        TableColumn<Booking, Integer> guestsCol = new TableColumn<>("Гостей");
        guestsCol.setCellValueFactory(new PropertyValueFactory<>("guestsCount"));

        TableColumn<Booking, String> tableCol = new TableColumn<>("Столик");
        tableCol.setCellValueFactory(new PropertyValueFactory<>("tableType"));

        TableColumn<Booking, String> statusCol = new TableColumn<>("Статус");
        statusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatusDisplay()));

        TableColumn<Booking, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setCellFactory(col -> new TableCell<Booking, Void>() {
            private final Button confirmBtn = new Button("✓");
            private final Button cancelBtn = new Button("✗");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox pane = new HBox(5, confirmBtn, cancelBtn, deleteBtn);

            {
                confirmBtn.setStyle("-fx-background-color: #0C6038; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                cancelBtn.setStyle("-fx-background-color: #FFD2A1; -fx-text-fill: #2D4C39; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #6C27DA; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                confirmBtn.setOnAction(e -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    updateBookingStatus(booking.getId(), AppConfig.BOOKING_STATUS_CONFIRMED);
                });
                cancelBtn.setOnAction(e -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    updateBookingStatus(booking.getId(), AppConfig.BOOKING_STATUS_CANCELLED);
                });
                deleteBtn.setOnAction(e -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    deleteBooking(booking);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        bookingsTable.getColumns().setAll(idCol, guestCol, phoneCol, dateCol, timeCol, guestsCol, tableCol, statusCol, actionsCol);
        bookingsTable.setItems(bookingsData);
    }

    private void setupUsersTable() {
        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<User, String> nameCol = new TableColumn<>("Имя");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, String> phoneCol = new TableColumn<>("Телефон");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<User, Boolean> adminCol = new TableColumn<>("Админ");
        adminCol.setCellValueFactory(new PropertyValueFactory<>("admin"));

        TableColumn<User, Boolean> blockedCol = new TableColumn<>("Заблокирован");
        blockedCol.setCellValueFactory(new PropertyValueFactory<>("blocked"));

        TableColumn<User, Void> actionsCol = new TableColumn<>("Действия");
        actionsCol.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button blockBtn = new Button("🚫");
            private final Button deleteBtn = new Button("🗑️");
            private final HBox pane = new HBox(5, blockBtn, deleteBtn);

            {
                blockBtn.setStyle("-fx-background-color: #808847; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #6C27DA; -fx-text-fill: white; -fx-background-radius: 3; -fx-cursor: hand;");
                blockBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    toggleUserBlock(user);
                });
                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        usersTable.getColumns().setAll(idCol, nameCol, emailCol, phoneCol, adminCol, blockedCol, actionsCol);
        usersTable.setItems(usersData);
    }

    private void setVisiblePane(Node pane) {
        for (Node child : contentArea.getChildren()) {
            child.setVisible(false);
            child.setManaged(false);
        }
        pane.setVisible(true);
        pane.setManaged(true);
    }

    private void clearAddDishForm() {
        dishNameField.clear();
        dishCategoryCombo.setValue(null);
        dishPriceField.clear();
        dishAvailableCheckBox.setSelected(true);
        dishDescriptionArea.clear();
        dishIngredientsArea.clear();
        selectedImageLabel.setText("(Нет файла)");
        selectedImageFile = null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}