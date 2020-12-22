package com.diploma.behavioralBiometricsAuthentication.controllers;
import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.listeners.KeyboardListener;
import com.diploma.behavioralBiometricsAuthentication.services.FeatureSampleService;
import com.diploma.behavioralBiometricsAuthentication.services.FuzzyInferenceService;
import com.diploma.behavioralBiometricsAuthentication.services.KeyProfileSamplesService;
import com.diploma.behavioralBiometricsAuthentication.services.UserService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.jnativehook.GlobalScreen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;


@Controller
public class LoginFXController {

    @FXML
    public Button loginButton, continueButton;
    @FXML
    public Pane passwordPane, loginPane;
    @FXML
    private TextField loginField, passwordExpose;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ImageView avatar;

    private static KeyboardListener listener;
    private static FeatureSampleService featureSampleService;
    private static UserService userService;
    private static FuzzyInferenceService fuzzyInferenceService;
    private static KeyProfileSamplesService kpsService;

    private String username;
    private String password;
    private boolean activeListener;

    @Autowired
    private void initializeBeans(KeyboardListener listener,
                                 FeatureSampleService featureSampleService,
                                 UserService userService,
                                 FuzzyInferenceService fuzzyInferenceService,
                                 KeyProfileSamplesService kpsService) {
        LoginFXController.listener = listener;
        LoginFXController.featureSampleService = featureSampleService;
        LoginFXController.userService = userService;
        LoginFXController.fuzzyInferenceService = fuzzyInferenceService;
        LoginFXController.kpsService = kpsService;
    }

    @FXML
    private void initialize(){
        double centerX = avatar.getX() + (avatar.getFitWidth() / 2);
        double centerY = avatar.getY() + (avatar.getFitHeight() / 2);
        Circle clip = new Circle(centerX, centerY, avatar.getFitHeight());
        avatar.setClip(clip);

        setElementVisibility(false);


    }
    private void setElementVisibility(boolean reverse){
        continueButton.setVisible(!reverse);
        loginPane.setVisible(!reverse);

        loginButton.setVisible(reverse);
        passwordPane.setVisible(reverse);
    }


    public void startListener() {
        try {
            this.username = userService.findByLogin( loginField.getText() ).getLogin();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            createNotification("error-login");
            return;
        }
        if (!this.activeListener) {
            GlobalScreen.addNativeKeyListener(listener);
            this.activeListener = true;
            System.out.println("Listener activated");
        }

        setElementVisibility(true);
    }

    public void login(ActionEvent actionEvent) {

        User currentUser = userService.findByLogin(this.username);
        this.password = passwordField.getText();

        if(userService.comparePassword(currentUser, this.password)){
            FeatureSample featureSample = null;
            try {
                featureSample = handleFeatureSample(currentUser);
            } catch (RuntimeException e){
                e.printStackTrace();
                createNotification("fail");
            }
            assert featureSample != null;
            String verdict = fuzzyInferenceService.authentication(featureSample);
            if (currentUser.getLogin().equals(verdict))
                createNotification("success");
            else {
                createNotification("fail");
            }
        }
        else
            createNotification("error-password");


        GlobalScreen.removeNativeKeyListener(listener);
        this.activeListener = false;
        System.out.println("Listener disabled");
    }

    private void createNotification(String notification) {
        Image image = null;
        String title = "";
        String message = "";
        switch (notification) {
            case "success" -> {
                image = new Image("/fxml/stylesheets/icons/success.png");
                title = "Авторизовано";
                message = "Авторизація пройшла успішно! Користувач: " + this.username;
            }
            case "fail" -> {
                image = new Image("/fxml/stylesheets/icons/fail.png");
                title = "Відмова";
                message = "Відхилено авторизацію :(";
            }
            case "error-login" -> {
                image = new Image("/fxml/stylesheets/icons/wrong.png");
                title = "Помилка";
                message = "Введено неправильний логін!";
            }
            case "error-password" -> {
                image = new Image("/fxml/stylesheets/icons/wrong.png");
                title = "Помилка";
                message = "Введено неправильний пароль!";
            }
        }
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(100);
        imageView.setFitHeight(100);
        Notifications notificationBuilder = Notifications.create()
                .title(title)
                .text(message)
                .graphic(imageView)
                .position(Pos.CENTER)
                .hideAfter(Duration.seconds(5))
                .darkStyle();

        notificationBuilder.show();
    }

    private FeatureSample handleFeatureSample(User currentUser) {
        kpsService.buildSamples();
        FeatureSample sample = featureSampleService.buildFeatureSample();
        sample.setUserId(currentUser.getId());

        return sample;
    }

    public void showPassword(MouseEvent mouseEvent) {
        passwordField.setVisible(false);
        passwordExpose.setText(passwordField.getText());
        passwordExpose.setVisible(true);
    }

    public void hidePassword(MouseEvent mouseEvent) {
        passwordField.setVisible(true);
        passwordExpose.setVisible(false);
    }
}
