package com.diploma.behavioralBiometricsAuthentication.services;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    public void createNotification(String notification) {
        Image image = null;
        String title = "";
        String message = "";
        switch (notification) {
            case "success", "success-write", "success-writeFIS" -> {
                image = new Image("/fxml/stylesheets/icons/success.png");
                title = notification.contains("success-write")  ? "Збережено" : "Авторизовано";
                message = notification.equals("success-write") ? "Запис успішно додано!" :
                          notification.equals("success-writeFIS")  ? "Біометричний захист успішно активовано" : "Авторизація пройшла успішно!";
            }
            case "fail", "fail-writeFIS" -> {
                image = new Image("/fxml/stylesheets/icons/fail.png");
                title = "Відмова";
                message = notification.equals("fail") ? "Відхилено авторизацію :(" : "Упс... Виникла непередбачена помилка :(";
            }
            case "error-password", "error-phrase", "error-login" -> {
                image = new Image("/fxml/stylesheets/icons/wrong.png");
                title = "Помилка";
                message = notification.equals("error-password") ? "Введено неправильний пароль!"
                        : notification.equals("error-login") ? "Введено неправильний логін!"
                        : "Перевірте правильність введеного тексту!";
            }
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        double duration = 1.5;
        Notifications notificationBuilder = Notifications.create()
                .title(title)
                .text(message)
                .graphic(imageView)
                .position(Pos.CENTER)
                .hideAfter(Duration.seconds(duration))
                .darkStyle()
                .hideCloseButton();

        notificationBuilder.show();
    }
}
