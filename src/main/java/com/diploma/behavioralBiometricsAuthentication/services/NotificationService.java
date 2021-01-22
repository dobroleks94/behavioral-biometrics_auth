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
                title = notification.contains("success-write")  ? "Saved" : "Successful authorization";
                message = notification.equals("success-write") ? "Record has been successfully added!" :
                          notification.equals("success-writeFIS")  ? "Biometrics security successfully activated" : "Authorization successfully done!";
            }
            case "fail", "fail-writeFIS" -> {
                image = new Image("/fxml/stylesheets/icons/fail.png");
                title = "Refused";
                message = notification.equals("fail") ? "Authorization failed :(" : "Oops... There is unexpected error :(";
            }
            case "error-password", "error-phrase", "error-login" -> {
                image = new Image("/fxml/stylesheets/icons/wrong.png");
                title = "Error";
                message = notification.equals("error-password") ? "Wrong password!"
                        : notification.equals("error-login") ? "Wrong username!"
                        : "Check the text is valid!";
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
