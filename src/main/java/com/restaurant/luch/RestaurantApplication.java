package com.restaurant.luch;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

public class RestaurantApplication extends Application {

    @Override
    public void start(Stage stage) {
        // Глобальный обработчик непойманных исключений
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Критическая ошибка");
                alert.setHeaderText("Приложение столкнулось с непредвиденной ошибкой");
                alert.setContentText(throwable.getMessage() + "\n\nПодробности в консоли.");
                alert.showAndWait();
                System.exit(1);
            });
        });

        try {
            System.out.println("🚀 Запуск приложения 'Луч'...");

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/auth.fxml"));
            if (fxmlLoader.getLocation() == null) {
                fxmlLoader = new FXMLLoader(getClass().getResource("/auth.fxml"));
                if (fxmlLoader.getLocation() == null) {
                    throw new RuntimeException("FXML файл не найден ни по одному из путей");
                }
            }
            Scene scene = new Scene(fxmlLoader.load(), 1400, 900);

            stage.setTitle("Ресторан 'Луч' - Авторизация");
            stage.setScene(scene);
            stage.setOnCloseRequest(event -> {
                System.out.println("👋 Приложение закрыто");
                System.exit(0);
            });

            stage.show();
            System.out.println("✅ Приложение успешно запущено!");

        } catch (Exception e) {
            System.err.println("❌ Ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}