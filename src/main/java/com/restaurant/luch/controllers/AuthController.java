package com.restaurant.luch.controllers;

import com.restaurant.luch.models.User;
import com.restaurant.luch.services.RestaurantService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class AuthController implements Initializable {

    @FXML private VBox loginCard, registerCard;
    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;
    @FXML private TextField loginPasswordVisibleField;  // для показа пароля
    @FXML private Button loginPasswordEyeButton;
    @FXML private Label loginErrorLabel;

    @FXML private TextField registerNameField, registerEmailField, registerPhoneField;
    @FXML private PasswordField registerPasswordField;
    @FXML private TextField registerPasswordVisibleField;
    @FXML private Button registerPasswordEyeButton;
    @FXML private PasswordField registerConfirmPasswordField;
    @FXML private TextField registerConfirmPasswordVisibleField;
    @FXML private Button registerConfirmPasswordEyeButton;
    @FXML private Label registerErrorLabel;

    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showLoginCard();
        // синхронизация текста между скрытым и видимым полями пароля (по желанию)
        setupPasswordVisibilityToggle();
    }

    private void setupPasswordVisibilityToggle() {
        // Для логина
        loginPasswordVisibleField.textProperty().bindBidirectional(loginPasswordField.textProperty());
        // Для регистрации
        registerPasswordVisibleField.textProperty().bindBidirectional(registerPasswordField.textProperty());
        registerConfirmPasswordVisibleField.textProperty().bindBidirectional(registerConfirmPasswordField.textProperty());
    }

    @FXML
    private void toggleLoginPasswordVisibility() {
        toggleVisibility(loginPasswordField, loginPasswordVisibleField, loginPasswordEyeButton);
    }

    @FXML
    private void toggleRegisterPasswordVisibility() {
        toggleVisibility(registerPasswordField, registerPasswordVisibleField, registerPasswordEyeButton);
    }

    @FXML
    private void toggleRegisterConfirmPasswordVisibility() {
        toggleVisibility(registerConfirmPasswordField, registerConfirmPasswordVisibleField, registerConfirmPasswordEyeButton);
    }

    private void toggleVisibility(PasswordField hiddenField, TextField visibleField, Button eyeButton) {
        if (hiddenField.isVisible()) {
            hiddenField.setVisible(false);
            hiddenField.setManaged(false);
            visibleField.setVisible(true);
            visibleField.setManaged(true);
            eyeButton.setText("🙈");
        } else {
            hiddenField.setVisible(true);
            hiddenField.setManaged(true);
            visibleField.setVisible(false);
            visibleField.setManaged(false);
            eyeButton.setText("👁");
        }
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void switchToLogin() {
        showLoginCard();
    }

    @FXML
    private void switchToRegister() {
        showRegisterCard();
    }

    @FXML
    private void login() {
        String email = loginEmailField.getText().trim();
        String password = loginPasswordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            loginErrorLabel.setText("Заполните все поля!");
            loginErrorLabel.setVisible(true);
            return;
        }

        if (!isValidEmail(email)) {
            loginErrorLabel.setText("Некорректный формат email");
            loginErrorLabel.setVisible(true);
            return;
        }
        if (!isValidPassword(password)) {
            loginErrorLabel.setText("Пароль должен быть не более 20 символов");
            loginErrorLabel.setVisible(true);
            return;
        }

        new Thread(() -> {
            try {
                User user = RestaurantService.getInstance().getUserByEmail(email);
                Platform.runLater(() -> {
                    if (user == null) {
                        loginErrorLabel.setText("Пользователь не найден");
                        loginErrorLabel.setVisible(true);
                        return;
                    }

                    if (BCrypt.checkpw(password, user.getPassword())) {
                        RestaurantService.setCurrentUser(user);
                        navigateToMainScreen();
                    } else {
                        loginErrorLabel.setText("Неверный пароль");
                        loginErrorLabel.setVisible(true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loginErrorLabel.setText("Ошибка при входе: " + e.getMessage());
                    loginErrorLabel.setVisible(true);
                });
            }
        }).start();
    }

    @FXML
    private void register() {
        String name = registerNameField.getText().trim();
        String email = registerEmailField.getText().trim();
        String phone = registerPhoneField.getText().trim();
        String pass = registerPasswordField.getText();
        String confirm = registerConfirmPasswordField.getText();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            registerErrorLabel.setText("Заполните все поля!");
            registerErrorLabel.setVisible(true);
            return;
        }
        if (!pass.equals(confirm)) {
            registerErrorLabel.setText("Пароли не совпадают!");
            registerErrorLabel.setVisible(true);
            return;
        }

        if (!isValidName(name)) {
            registerErrorLabel.setText("Имя может содержать только буквы, пробелы и дефисы");
            registerErrorLabel.setVisible(true);
            return;
        }
        if (!isValidEmail(email)) {
            registerErrorLabel.setText("Некорректный формат email");
            registerErrorLabel.setVisible(true);
            return;
        }
        if (!isValidPhone(phone)) {
            registerErrorLabel.setText("Некорректный формат телефона (пример: +7 999 123-45-67)");
            registerErrorLabel.setVisible(true);
            return;
        }
        if (!isValidPassword(pass)) {
            registerErrorLabel.setText("Пароль должен быть не более 20 символов");
            registerErrorLabel.setVisible(true);
            return;
        }

        new Thread(() -> {
            try {
                if (RestaurantService.getInstance().getUserByEmail(email) != null) {
                    Platform.runLater(() -> {
                        registerErrorLabel.setText("Email уже зарегистрирован");
                        registerErrorLabel.setVisible(true);
                    });
                    return;
                }

                User newUser = new User();
                newUser.setName(name);
                newUser.setEmail(email);
                newUser.setPhone(phone);
                newUser.setPassword(BCrypt.hashpw(pass, BCrypt.gensalt()));
                newUser.setAdmin(false);
                newUser.setBlocked(false);
                newUser.setFailedLoginAttempts(0);

                User createdUser = RestaurantService.getInstance().addUser(newUser);

                Platform.runLater(() -> {
                    if (createdUser != null && createdUser.getId() != null) {
                        RestaurantService.setCurrentUser(createdUser);
                        navigateToMainScreen();
                    } else {
                        registerErrorLabel.setText("Ошибка при автоматическом входе: не удалось получить данные пользователя");
                        registerErrorLabel.setVisible(true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    registerErrorLabel.setText("Ошибка регистрации: " + e.getMessage());
                    registerErrorLabel.setVisible(true);
                });
            }
        }).start();
    }

    @FXML
    private void resetPassword() {
        showAlert("Сброс пароля", "Функция в разработке");
    }

    private void showLoginCard() {
        loginCard.setVisible(true);
        loginCard.setManaged(true);
        registerCard.setVisible(false);
        registerCard.setManaged(false);
        loginErrorLabel.setVisible(false);
        registerErrorLabel.setVisible(false);
    }

    private void showRegisterCard() {
        loginCard.setVisible(false);
        loginCard.setManaged(false);
        registerCard.setVisible(true);
        registerCard.setManaged(true);
        loginErrorLabel.setVisible(false);
        registerErrorLabel.setVisible(false);
    }

    private void navigateToMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/main.fxml"));
            Parent root = loader.load();
            MainController controller = loader.getController();

            Stage stage = (Stage) registerCard.getScene().getWindow();
            controller.setPrimaryStage(stage);
            stage.getScene().setRoot(root);
            stage.setTitle("Ресторан Луч - Главная");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось загрузить главный экран");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========== Валидация ==========
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

    private boolean isValidPassword(String password) {
        return password != null && password.length() <= 20;
    }
}