package acs.behavioral_biometrics.gui_fx.controllers;

import acs.behavioral_biometrics.access_control.AccessControlService;
import acs.behavioral_biometrics.app_utils.models.SystemLogger;
import acs.behavioral_biometrics.gui_fx.enums.AuthStep;
import acs.behavioral_biometrics.gui_fx.enums.AuthStepResult;
import acs.behavioral_biometrics.gui_fx.enums.GUIAction;
import acs.behavioral_biometrics.gui_fx.enums.Notification;
import acs.behavioral_biometrics.gui_fx.services.NotificationService;
import acs.behavioral_biometrics.gui_fx.services.StageCreationService;
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


    private static AccessControlService accessControlService;
    private static StageCreationService stageCreationService;
    private static NotificationService notificationService;
    private static SystemLogger logger;

    private String phrase;

    @Autowired
    private void initializeBeans(NotificationService notificationService,
                                 StageCreationService stageCreationService,
                                 AccessControlService accessControlService,
                                 SystemLogger logger) {

        LoginFXController.accessControlService = accessControlService;
        LoginFXController.notificationService = notificationService;
        LoginFXController.stageCreationService = stageCreationService;
        LoginFXController.logger = logger;

    }

    @FXML
    private void initialize() {
        setElementVisibility(AuthStep.IDENTIFICATION);
        try { phrase = accessControlService.extractArbitraryPhrase(inputPhrase.getText()); }
        catch (IOException e) { e.printStackTrace(); }
        inputPhrase.setText( phrase );
    }


    public void identify() {
        try {
            if ( accessControlService.doIdentificationStep( loginField.getText() ) )
                System.out.println("Keyboard Listener activated!");
            updateGUIStep(AuthStepResult.SUCCESS, step1, circleStep1, stepNum1, identification);
            setElementVisibility(AuthStep.PASSWORD_BIOMETRICS_AUTH);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            notificationService.createNotification(Notification.ERROR_LOGIN);
            updateGUIStep(AuthStepResult.FAIL, step1, circleStep1, stepNum1, identification);
        }
    }
    public void auth() throws IOException {
        String password = passwordField.getText();
        if(accessControlService.doPasswordAuthentication(password)){
            // the case, when password is correct
            updateGUIStep(AuthStepResult.SUCCESS, step2, circleStep2, stepNum2, authentication1, passwordAuth);
            try {
                if (accessControlService.doBiometricsAuthentication()) {
                    // the case, when biometrics authentication is activated & successfully performed
                    updateGUIStep(AuthStepResult.SUCCESS, step3, circleStep3, stepNum3, authentication2, biometrics1);
                    showLastAuthLayer();
                } else {
                    // the case, when biometrics authentication is not activated
                    notificationService.createNotification(Notification.SUCCESS);
                    accessControlService.approveAuthenticity();
                    StageCreationService.getCurrentStage().close();
                    stageCreationService.createStage("Особистий кабінет", new Stage(), GUIAction.INFO).show();
                    notificationService.createNotification(Notification.SUCCESS);
                }
            } catch (RuntimeException e){
                // the case, when biometrics is activated but authentication failed
                failAuth2();
                e.printStackTrace();
            }
        }
        else {
            // the case when password wrong
            updateGUIStep(AuthStepResult.FAIL, step2, circleStep2, stepNum2, authentication1, passwordAuth);
            notificationService.createNotification(Notification.ERROR_PWD);
            disableListener();
        }
    }
    public void login() throws IOException {
        String userInput = inputArea.getText().trim();
        if(inputPhrase.getText().trim().equals(userInput)){
            try {
                if ( !accessControlService.authenticate() ) {
                    failAuth3();
                    return;
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
                failAuth3();
                return;
            }
            this.inputArea.setText("");
            updateGUIStep(AuthStepResult.SUCCESS, step4, circleStep4, stepNum4, authentication3, biometrics2);
            accessControlService.approveAuthenticity();

            setElementVisibility(AuthStep.IDENTIFICATION);
            StageCreationService.getCurrentStage().close();
            stageCreationService.createStage("Особистий кабінет", new Stage(), GUIAction.INFO).show();
            notificationService.createNotification(Notification.SUCCESS);
        }
        else {
            updateGUIStep(AuthStepResult.FAIL, step4, circleStep4, stepNum4, authentication3, biometrics2);
            notificationService.createNotification(Notification.ERROR_PHRASE);
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
        accessControlService.clearKeystrokeData();
        logger.log(SystemLogger.KEY_FEATURE_CONTAINERS_CLEAN);
    }
    public void verifyFullText(KeyEvent keyEvent) throws IOException {
        if (keyEvent.getCode() == KeyCode.SPACE)
            accessControlService.processKeystrokeData();
        if(phrase.trim().length() == inputArea.getText().trim().length())
            login();
    }
    public void updatePhrase() throws IOException {
        this.phrase = accessControlService.extractArbitraryPhrase(inputPhrase.getText());
        inputPhrase.setText(this.phrase);
        accessControlService.clearKeystrokeData();
        clearInputArea();
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
        accessControlService.clearKeystrokeData();
        logger.log(SystemLogger.KEY_FEATURE_CONTAINERS_CLEAN);
    }
    //---------------------------------------------------------------------------------------------//

    // ----------------------------- Event delegations --------------------------------------------//
    public void delegateAuthorization(KeyEvent keyEvent) throws IOException {
        if(keyEvent.getCode().equals(KeyCode.ENTER))
            auth();
    }
    public void delegateIdentification(KeyEvent keyEvent) {
        if(keyEvent.getCode().equals(KeyCode.ENTER))
            identify();
    }
    //---------------------------------------------------------------------------------------------//

    //---------------------------- GUI resolution settings ----------------------------------------//

    public void updateGUIStep(AuthStepResult result, Rectangle block, Circle border, Label ... notes){
        Color fill = switch (result) {
            case SUCCESS -> Color.LIMEGREEN;
            case FAIL -> Color.RED;
            default -> Color.WHITE;
        };
        Color stroke = switch (result){
            case SUCCESS, FAIL -> Color.WHITE;
            default -> Color.LIMEGREEN;
        };
        block.setFill(fill);
        block.setStroke(stroke);

        border.setFill(fill);
        border.setStroke(stroke);

        Arrays.stream(notes).forEach(note -> note.setTextFill(stroke));
    }
    public void resetStepGUI(AuthStep step){
        switch (step) {
            case IDENTIFICATION -> updateGUIStep(AuthStepResult.UNDEFINED, step1, circleStep1, stepNum1, identification);
            case PASSWORD_BIOMETRICS_AUTH -> {
                updateGUIStep(AuthStepResult.UNDEFINED, step2, circleStep2, stepNum2, authentication1, passwordAuth);
                updateGUIStep(AuthStepResult.UNDEFINED, step3, circleStep3, stepNum3, authentication2, biometrics1);
            }
            case BIOMETRICS_PHRASE_AUTH ->
                updateGUIStep(AuthStepResult.UNDEFINED, step4, circleStep4, stepNum4, authentication3, biometrics2);
            case ALL -> {
                resetStepGUI(AuthStep.IDENTIFICATION);
                resetStepGUI(AuthStep.PASSWORD_BIOMETRICS_AUTH);
                resetStepGUI(AuthStep.BIOMETRICS_PHRASE_AUTH);
            }
        }
    }

    //---------------------------------------------------------------------------------------------//

    private void setElementVisibility(AuthStep step) {
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
            case IDENTIFICATION -> {
                title1.setVisible(true);
                title2.setVisible(true);
                continueButton.setVisible(true);
                loginPane.setVisible(true);
                resetStepGUI(AuthStep.ALL);
            }
            case PASSWORD_BIOMETRICS_AUTH -> {
                title1.setVisible(true);
                title2.setVisible(true);
                authButton.setVisible(true);
                passwordPane.setVisible(true);
                resetStepGUI(AuthStep.PASSWORD_BIOMETRICS_AUTH);
            }
            case BIOMETRICS_PHRASE_AUTH -> {
                phrasePane.setVisible(true);
                resetStepGUI(AuthStep.BIOMETRICS_PHRASE_AUTH);
            }
        }
    }
    private void showLastAuthLayer() {
       setElementVisibility(AuthStep.BIOMETRICS_PHRASE_AUTH);
       enableListener();
    }

    public void enableListener(){
        if (accessControlService.activateListener()) {
            System.out.println("Keyboard listener activated");
        }
    }
    public void disableListener(){
        if (accessControlService.deactivateListener()) {
            System.out.println("Keyboard listener disabled");
        }
    }

    private void failAuth2() {
        updateGUIStep(AuthStepResult.FAIL, step3, circleStep3, stepNum3, authentication2, biometrics1);
        disableListener();
        notificationService.createNotification(Notification.FAIL);
    }
    private void failAuth3() throws IOException {
        updatePhrase();
        clearInputArea();
        updateGUIStep(AuthStepResult.FAIL, step4, circleStep4, stepNum4, authentication3, biometrics2);
        notificationService.createNotification(Notification.FAIL);
    }

    public void resetIdentGUI() {
        resetStepGUI(AuthStep.IDENTIFICATION);
    }
    public void resetAuthGUI() {
        resetStepGUI(AuthStep.PASSWORD_BIOMETRICS_AUTH);
    }
    public void resetAuth2GUI(){
        resetStepGUI(AuthStep.BIOMETRICS_PHRASE_AUTH);
    }


}
