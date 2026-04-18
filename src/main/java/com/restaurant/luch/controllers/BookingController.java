package com.restaurant.luch.controllers;

import com.restaurant.luch.config.AppConfig;
import com.restaurant.luch.models.Booking;
import com.restaurant.luch.models.User;
import com.restaurant.luch.services.RestaurantService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class BookingController implements Initializable {

    @FXML private TextField guestNameField;
    @FXML private TextField guestLastNameField;
    @FXML private TextField guestPhoneField;
    @FXML private TextField guestEmailField;
    @FXML private DatePicker bookingDatePicker;
    @FXML private ComboBox<String> bookingTimeCombo;
    @FXML private Spinner<Integer> guestCountSpinner;
    @FXML private ComboBox<String> tableCombo;
    @FXML private TextArea bookingCommentsArea;
    @FXML private CheckBox wineCheckBox;
    @FXML private CheckBox decorCheckBox;
    @FXML private CheckBox cakeCheckBox;
    @FXML private CheckBox musicCheckBox;
    @FXML private Label totalPriceLabel;

    private RestaurantService service;
    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        service = RestaurantService.getInstance();

        SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2);
        guestCountSpinner.setValueFactory(valueFactory);

        bookingTimeCombo.getItems().addAll(
                "10:00", "10:30", "11:00", "11:30", "12:00", "12:30", "13:00",
                "18:00", "18:30", "19:00", "19:30", "20:00", "20:30", "21:00"
        );
        bookingTimeCombo.setValue("19:00");

        tableCombo.getItems().addAll("Столик у окна (2 места)", "Столик в центре (4 места)", "VIP-кабинка (6 мест)");
        tableCombo.setValue("Столик у окна (2 места)");

        bookingDatePicker.setValue(LocalDate.now());

        guestCountSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateAvailableTables());
        wineCheckBox.selectedProperty().addListener(e -> updateTotalPrice());
        decorCheckBox.selectedProperty().addListener(e -> updateTotalPrice());
        cakeCheckBox.selectedProperty().addListener(e -> updateTotalPrice());
        musicCheckBox.selectedProperty().addListener(e -> updateTotalPrice());

        User currentUser = RestaurantService.getCurrentUser();
        if (currentUser != null) {
            guestNameField.setText(currentUser.getName());
            guestPhoneField.setText(currentUser.getPhone());
            guestEmailField.setText(currentUser.getEmail());
            if (currentUser.getName() != null && currentUser.getName().contains(" ")) {
                String[] parts = currentUser.getName().split(" ", 2);
                guestNameField.setText(parts[0]);
                guestLastNameField.setText(parts[1]);
            }
        }

        updateTotalPrice();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void goBack() {
        if (mainController != null) mainController.showMainContent();
    }

    @FXML
    private void updateAvailableTables() {
        int guests = guestCountSpinner.getValue();
        if (guests <= 2) {
            tableCombo.getItems().setAll("Столик у окна (2 места)", "Столик в центре (4 места)");
        } else if (guests <= 4) {
            tableCombo.getItems().setAll("Столик в центре (4 места)", "VIP-кабинка (6 мест)");
        } else {
            tableCombo.getItems().setAll("VIP-кабинка (6 мест)");
        }
        tableCombo.setValue(tableCombo.getItems().get(0));
    }

    private void updateTotalPrice() {
        double total = 0;
        if (wineCheckBox.isSelected()) total += 500;
        if (decorCheckBox.isSelected()) total += 1000;
        if (musicCheckBox.isSelected()) total += 2000;
        totalPriceLabel.setText(String.format("%.0f ₽", total));
    }

    @FXML
    private void bookTable() {
        // Проверка авторизации
        if (RestaurantService.getCurrentUser() == null) {
            showAlert("Требуется авторизация", "Для бронирования столика необходимо войти в систему.");
            return;
        }

        if (guestNameField.getText().trim().isEmpty() ||
                guestPhoneField.getText().trim().isEmpty() ||
                bookingDatePicker.getValue() == null ||
                bookingTimeCombo.getValue() == null ||
                tableCombo.getValue() == null) {
            showAlert("Ошибка", "Пожалуйста, заполните все обязательные поля (имя, телефон, дата, время, столик)");
            return;
        }

        try {
            String fullName = guestNameField.getText().trim()
                    + (guestLastNameField.getText().trim().isEmpty() ? "" : " " + guestLastNameField.getText().trim());

            LocalDate date = bookingDatePicker.getValue();
            LocalTime time = LocalTime.parse(bookingTimeCombo.getValue(), DateTimeFormatter.ofPattern("HH:mm"));

            Booking booking = new Booking();
            booking.setGuestName(fullName);
            booking.setPhone(guestPhoneField.getText().trim());
            booking.setBookingDate(date);
            booking.setBookingTime(time);
            booking.setGuestsCount(guestCountSpinner.getValue());
            booking.setTableType(tableCombo.getValue());
            booking.setSpecialRequests(bookingCommentsArea.getText());
            booking.setStatus(AppConfig.BOOKING_STATUS_PENDING);
            // ⚠️ Удалена строка: booking.setCreatedDate(LocalDateTime.now());
            // Поле created_date отсутствует в таблице bookings, поэтому не отправляем его.

            User currentUser = RestaurantService.getCurrentUser();
            if (currentUser != null) {
                booking.setUserId(currentUser.getId());
            }

            service.addBooking(booking);

            showAlert("Успех", "Столик успешно забронирован!");
            clearForm();
            goBack();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось создать бронирование: " + e.getMessage());
        }
    }

    private void clearForm() {
        guestNameField.clear();
        guestLastNameField.clear();
        guestPhoneField.clear();
        guestEmailField.clear();
        bookingDatePicker.setValue(LocalDate.now());
        bookingTimeCombo.setValue("19:00");
        guestCountSpinner.getValueFactory().setValue(2);
        tableCombo.setValue(tableCombo.getItems().get(0));
        bookingCommentsArea.clear();
        wineCheckBox.setSelected(false);
        decorCheckBox.setSelected(false);
        cakeCheckBox.setSelected(false);
        musicCheckBox.setSelected(false);
        updateTotalPrice();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}