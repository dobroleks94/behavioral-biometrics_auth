package com.diploma.behavioralBiometricsAuthentication.controllers;
import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.listeners.KeyboardListener;
import com.diploma.behavioralBiometricsAuthentication.services.FeatureSampleService;
import com.diploma.behavioralBiometricsAuthentication.services.FuzzyInferenceService;
import com.diploma.behavioralBiometricsAuthentication.services.KeyProfileSamplesService;
import com.diploma.behavioralBiometricsAuthentication.services.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.jnativehook.GlobalScreen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.Arrays;


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
    public Rectangle step1, step2, step3, step4;
    @FXML
    public Circle circleStep1, circleStep2, circleStep3, circleStep4;
    @FXML
    public Label stepNum1, stepNum2, stepNum3, stepNum4,
            identification, authentication1, authentication2, authentication3,
            passwordAuth, biometrics1, biometrics2;


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
        setElementVisibility(false);
    }


    public void startListener() {
        try {
            this.username = userService.findByLogin( loginField.getText() ).getLogin();
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            createNotification("error-login");
            updateGUIStep("fail", step1, circleStep1, stepNum1, identification);
            return;
        }
        if (!this.activeListener) {
            GlobalScreen.addNativeKeyListener(listener);
            this.activeListener = true;
            System.out.println("Keyboard Listener activated");
        }
        updateGUIStep("success", step1, circleStep1, stepNum1, identification);
        setElementVisibility(true);
    }
    public void login() {

        User currentUser = userService.findByLogin(this.username);
        this.password = passwordField.getText();
        String verdict = "";
        if(userService.comparePassword(currentUser, this.password)){
            updateGUIStep("success", step2, circleStep2, stepNum2, authentication1, passwordAuth);
            FeatureSample featureSample;
            if(currentUser.isProtect()) {
                try {
                    featureSample = handleFeatureSample(currentUser);
                    verdict = fuzzyInferenceService.authentication(featureSample);

                    if (!currentUser.getLogin().equals(verdict)) {
                        updateGUIStep("fail", step3, circleStep3, stepNum3, authentication2, biometrics1);
                        createNotification("fail");
                        this.passwordField.setText("");
                        return;
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    createNotification("fail");
                    updateGUIStep("fail", step3, circleStep3, stepNum3, authentication2, biometrics1);
                    this.passwordField.setText("");
                    return;
                }
            }
            createNotification("success");
            updateGUIStep("success", step3, circleStep3, stepNum3, authentication2, biometrics1);
            this.passwordField.setText("");
        }
        else {
            updateGUIStep("fail", step2, circleStep2, stepNum2, authentication1, passwordAuth);
            createNotification("error-password");
        }

        GlobalScreen.removeNativeKeyListener(listener);
        this.activeListener = false;
        System.out.println("Listener disabled");
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
    public void delegateAuthorization(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER))
            login();
    }
    public void delegateIdentification(KeyEvent keyEvent){
        if(keyEvent.getCode().equals(KeyCode.ENTER))
            startListener();
    }

    public void resetIdentGUI() {
        resetStepGUI("identification");
    }
    public void resetAuthGUI() {
        resetStepGUI("authentication");
    }


    private void setElementVisibility(boolean reverse){
        continueButton.setVisible(!reverse);
        loginPane.setVisible(!reverse);

        loginButton.setVisible(reverse);
        passwordPane.setVisible(reverse);
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
        double duration = 2;
        Notifications notificationBuilder = Notifications.create()
                .title(title)
                .text(message)
                .graphic(imageView)
                .position(Pos.CENTER)
                .hideAfter(Duration.seconds(duration))
                .darkStyle();

        notificationBuilder.show();
    }
    private FeatureSample handleFeatureSample(User currentUser) {
        kpsService.buildSamples();
        FeatureSample sample = featureSampleService.buildFeatureSample();
        sample.setUserId(currentUser.getId());

        return sample;
    }
    private void updateGUIStep(String result, Rectangle block, Circle border, Label ... notes){
        Color fill = switch (result) {
            case "success" -> Color.LIMEGREEN;
            case "fail" -> Color.RED;
            default -> Color.WHITE;
        };
        Color stroke = switch (result){
            case "success", "fail" -> Color.WHITE;
            default -> Color.LIMEGREEN;
        };
        block.setFill(fill);
        block.setStroke(stroke);

        border.setFill(fill);
        border.setStroke(stroke);

        Arrays.stream(notes).forEach(note -> note.setTextFill(stroke));
    }
    private void resetStepGUI(String step){
        switch (step) {
            case "identification" -> updateGUIStep("default", step1, circleStep1, stepNum1, identification);
            case "authentication" -> {
                updateGUIStep("default", step2, circleStep2, stepNum2, authentication1, passwordAuth);
                updateGUIStep("default", step3, circleStep3, stepNum3, authentication2, biometrics1);
                updateGUIStep("default", step4, circleStep4, stepNum4, authentication3, biometrics2);
            }
            case "all" -> {
                resetStepGUI("identification");
                resetStepGUI("authentication");
            }
        }
    }
    private boolean tryParse(String str){
        try { Integer.parseInt(str); }
        catch (Exception e){ return false; }

        return true;
    }
}
