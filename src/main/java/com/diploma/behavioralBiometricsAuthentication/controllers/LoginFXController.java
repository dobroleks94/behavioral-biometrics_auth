package com.diploma.behavioralBiometricsAuthentication.controllers;
import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.entities.logger.SystemLogger;
import com.diploma.behavioralBiometricsAuthentication.listeners.KeyboardListener;
import com.diploma.behavioralBiometricsAuthentication.services.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.jnativehook.GlobalScreen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.Arrays;


@Controller
public class LoginFXController {

    @FXML
    public Button authButton, continueButton;
    @FXML
    public Pane passwordPane, loginPane, phrasePane;
    @FXML
    private TextField loginField, passwordExpose;
    @FXML
    private PasswordField passwordField;
    @FXML
    public Rectangle step1, step2, step3, step4,
                     descriptionContainer, inputContainer;
    @FXML
    public Circle circleStep1, circleStep2, circleStep3, circleStep4;
    @FXML
    public Label title1, title2, stepNum1, stepNum2, stepNum3, stepNum4,
            identification, authentication1, authentication2, authentication3,
            passwordAuth, biometrics1, biometrics2,
            descriptionLabel;
    @FXML
    public TextArea inputPhrase, inputArea;

    private static ArbitraryPhraseExtractor phraseExtractor;
    private static KeyboardListener listener;
    private static KeyProfileSamplesService kpsService;
    private static StageCreationService stageCreationService;
    private static NotificationService notificationService;
    private static SystemLogger logger;

    private static AuthenticationService authService;

    private boolean activeListener;
    private String phrase;

    @Autowired
    private void initializeBeans(KeyboardListener listener,
                                 NotificationService notificationService,
                                 KeyProfileSamplesService kpsService,
                                 ArbitraryPhraseExtractor phraseExtractor,
                                 StageCreationService stageCreationService,
                                 AuthenticationService authService,
                                 SystemLogger logger) {

        LoginFXController.listener = listener;
        LoginFXController.notificationService = notificationService;
        LoginFXController.kpsService = kpsService;
        LoginFXController.phraseExtractor = phraseExtractor;
        LoginFXController.stageCreationService = stageCreationService;
        LoginFXController.logger = logger;

        LoginFXController.authService = authService;
    }

    @FXML
    private void initialize() throws IOException {
        setElementVisibility("identify");
        try { phrase = phraseExtractor.getRandomPhrase(); }
        catch (IOException e) { e.printStackTrace(); }
        inputPhrase.setText( phrase );
    }


    public void identify() throws IOException {
        try {
            authService.identifyUser( loginField.getText() );

            enableListener();
            updateGUIStep("success", step1, circleStep1, stepNum1, identification);
            setElementVisibility("auth1");
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            notificationService.createNotification("error-login");
            updateGUIStep("fail", step1, circleStep1, stepNum1, identification);
        }
    }
    public void auth() throws IOException {
        String password = passwordField.getText();
        if(authService.passwordAuth(password)){
            updateGUIStep("success", step2, circleStep2, stepNum2, authentication1, passwordAuth);
            if(authService.checkOnBiometricsProtection()) {
               try {
                   if (authService.checkUserIdentity(authService.biometricsAuth())) {
                       failAuth2();
                       return;
                   }
               }
               catch (RuntimeException e){
                   e.printStackTrace();
                   failAuth2();
                   return;
               }
            }
            else {
                notificationService.createNotification("success");
                AuthenticationService.authenticateUser();
                disableListener();
                StageCreationService.getCurrentStage().close();
                stageCreationService.createStage("Особистий кабінет", new Stage(), "info").show();
                notificationService.createNotification("success");
                return;
            }
            updateGUIStep("success", step3, circleStep3, stepNum3, authentication2, biometrics1);
            disableListener();
            showLastAuthLayer();
        }
        else {
            updateGUIStep("fail", step2, circleStep2, stepNum2, authentication1, passwordAuth);
            notificationService.createNotification("error-password");
            disableListener();
        }
    }
    public void login() throws IOException {
        String userInput = inputArea.getText().trim();
        if(inputPhrase.getText().trim().equals(userInput)){
            try {
                if (authService.checkUserIdentity(authService.biometricsAuth())) {
                    failAuth3();
                    return;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                failAuth3();
                return;
            }
            this.inputArea.setText("");
            updateGUIStep("success", step4, circleStep4, stepNum4, authentication3, biometrics2);
            AuthenticationService.authenticateUser();
            disableListener();

            setElementVisibility("identify");
            StageCreationService.getCurrentStage().close();
            stageCreationService.createStage("Особистий кабінет", new Stage(), "info").show();
            notificationService.createNotification("success");
        }
        else {
            updateGUIStep("fail", step4, circleStep4, stepNum4, authentication3, biometrics2);
            notificationService.createNotification("error-phrase");
        }
    }

    // ----------------------------- Input Area in Auth3 ------------------------------------------//
    public void actionInputArea(MouseEvent mouseEvent) {
        resetAuth2GUI();
        clearInputArea();
        enableListener();
    }
    public void clearInputArea(){
        this.inputArea.setText("");
        kpsService.clearAllContainers();
        logger.log(SystemLogger.KEY_FEATURE_CONTAINERS_CLEAN);
    }
    public void verifyFullText(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.SPACE)
            kpsService.buildSamples();
        if(phrase.trim().length() == inputArea.getText().trim().length())
            login();
    }
    public void updatePhrase() throws IOException {
        phrase = phraseExtractor.getRandomPhrase();
        inputPhrase.setText(phrase);
        kpsService.clearAllContainers();
        logger.log(SystemLogger.KEY_FEATURE_CONTAINERS_CLEAN);
    }
    //---------------------------------------------------------------------------------------------//

    // ----------------------------- Password process ---------------------------------------------//
    public void actionPassword() {
        resetAuthGUI();
        clearPassword();
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
    public void clearPassword(){
        this.passwordField.setText("");
        kpsService.clearAllContainers();
        logger.log(SystemLogger.KEY_FEATURE_CONTAINERS_CLEAN);
    }
    //---------------------------------------------------------------------------------------------//

    // ----------------------------- Event delegations --------------------------------------------//
    public void delegateAuthorization(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getCode().equals(KeyCode.ENTER))
            auth();
    }
    public void delegateIdentification(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getCode().equals(KeyCode.ENTER))
            identify();
    }
    //---------------------------------------------------------------------------------------------//

    //---------------------------- GUI resolution settings ----------------------------------------//

    public void updateGUIStep(String result, Rectangle block, Circle border, Label ... notes){
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
    public void resetStepGUI(String step){
        switch (step) {
            case "identification" -> updateGUIStep("default", step1, circleStep1, stepNum1, identification);
            case "authentication" -> {
                updateGUIStep("default", step2, circleStep2, stepNum2, authentication1, passwordAuth);
                updateGUIStep("default", step3, circleStep3, stepNum3, authentication2, biometrics1);
            }
            case "authentication2" -> {
                updateGUIStep("default", step4, circleStep4, stepNum4, authentication3, biometrics2);
            }
            case "all" -> {
                resetStepGUI("identification");
                resetStepGUI("authentication");
                resetStepGUI("authentication2");
            }
        }
    }
    public void resetIdentGUI() {
        resetStepGUI("identification");
    }
    public void resetAuthGUI() {
        resetStepGUI("authentication");
    }
    public void resetAuth2GUI(){
        resetStepGUI("authentication2");
    }
    //---------------------------------------------------------------------------------------------//

    private void setElementVisibility(String step) throws IOException {
        continueButton.setVisible(false);
        loginPane.setVisible(false);
        authButton.setVisible(false);
        passwordPane.setVisible(false);
        phrasePane.setVisible(false);
        title1.setVisible(false);
        title2.setVisible(false);
        loginField.setText("");
        passwordField.setText("");
        inputArea.setText("");
        switch (step){
            case "identify" -> {
                title1.setVisible(true);
                title2.setVisible(true);
                continueButton.setVisible(true);
                loginPane.setVisible(true);
                resetStepGUI("all");
            }
            case "auth1" -> {
                title1.setVisible(true);
                title2.setVisible(true);
                authButton.setVisible(true);
                passwordPane.setVisible(true);
                resetStepGUI("authentication");
            }
            case "auth2" -> {
                phrasePane.setVisible(true);
                resetStepGUI("authentication2");
            }
        }
    }
    private void showLastAuthLayer() throws IOException {
       setElementVisibility("auth2");
       enableListener();
    }

    public void enableListener(){
        if(!this.activeListener) {
            GlobalScreen.addNativeKeyListener(listener);
            this.activeListener = true;
            System.out.println("Keyboard Listener activated");
        }
    }
    public void disableListener(){
        GlobalScreen.removeNativeKeyListener(listener);
        this.activeListener = false;
        System.out.println("Listener disabled");
    }

    private void failAuth2() {
        updateGUIStep("fail", step3, circleStep3, stepNum3, authentication2, biometrics1);
        disableListener();
        notificationService.createNotification("fail");
    }
    private void failAuth3() throws IOException {
        updatePhrase();
        clearInputArea();
        updateGUIStep("fail", step4, circleStep4, stepNum4, authentication3, biometrics2);
        notificationService.createNotification("fail");
    }




}
