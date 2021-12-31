package acs.behavioral_biometrics.gui_fx.services;

import acs.behavioral_biometrics.app_utils.configuration.YamlPropertySourceFactory;
import acs.behavioral_biometrics.gui_fx.enums.Messages;
import acs.behavioral_biometrics.gui_fx.enums.Notification;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

@Service
@PropertySource(value = "classpath:gui-config.yml", factory = YamlPropertySourceFactory.class)
public class NotificationService {

    @Value("${notifications.title.success.save}")
    private String saveTitle;
    @Value("${notifications.title.success.auth}")
    private String authTitle;
    @Value("${notifications.title.fail}")
    private String failTitle;
    @Value("${notifications.title.error}")
    private String errorTitle;
    @Value("${notifications.icon-path.success}")
    private String successIcon;
    @Value("${notifications.icon-path.fail}")
    private String failIcon;
    @Value("${notifications.icon-path.error}")
    private String errorIcon;

    @Value("${notifications.title.success.prefix}")
    private String prefix;


    public void createNotification(Notification notification) {
        Image image = null;
        String title = "";
        String message = "";
        switch (notification) {
            case SUCCESS, SUCCESS_WRITE, SUCCESS_FIS -> {
                image = new Image(successIcon);
                title = notification.getDefinition().contains(prefix)  ? saveTitle : authTitle;
                message = notification.equals(Notification.SUCCESS_WRITE) ? Messages.RECORD_ADDED.getMessage() :
                          notification.equals(Notification.SUCCESS_FIS)  ? Messages.BIOMETRICS_ACTIVATED.getMessage() : Messages.SUCCESS_AUTH.getMessage();
            }
            case FAIL, FAIL_FIS -> {
                image = new Image(failIcon);
                title = failTitle;
                message = notification.equals(Notification.FAIL) ? Messages.FAIL_AUTH.getMessage() : Messages.FAIL.getMessage();
            }
            case ERROR_LOGIN, ERROR_PWD, ERROR_PHRASE -> {
                image = new Image(errorIcon);
                title = errorTitle;
                message = notification.equals(Notification.ERROR_PWD) ? Messages.ERROR_PWD.getMessage()
                        : notification.equals(Notification.ERROR_LOGIN) ? Messages.ERROR_LOGIN.getMessage()
                        : Messages.ERROR_PHRASE.getMessage();
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
