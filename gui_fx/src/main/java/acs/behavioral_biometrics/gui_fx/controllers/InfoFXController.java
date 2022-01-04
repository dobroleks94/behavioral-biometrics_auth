package acs.behavioral_biometrics.gui_fx.controllers;

import acs.behavioral_biometrics.access_control.AccessControlService;
import acs.behavioral_biometrics.app_utils.models.SystemLogger;
import acs.behavioral_biometrics.association_rules.models.AssociationRule;
import acs.behavioral_biometrics.gui_fx.enums.GUIAction;
import acs.behavioral_biometrics.gui_fx.enums.Notification;
import acs.behavioral_biometrics.gui_fx.services.NotificationService;
import acs.behavioral_biometrics.gui_fx.services.StageCreationService;
import acs.behavioral_biometrics.info_utils.SystemUserDataCollector;
import acs.behavioral_biometrics.system_training.service.TrainingService;
import javafx.animation.FillTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;


@Controller
public class InfoFXController {

    private static SystemUserDataCollector dataCollector;
    private static AccessControlService accessControlService;
    private static TrainingService trainingService;
    private static StageCreationService stageCreationService;
    private static NotificationService notificationService;
    private static SystemLogger logger;

    @FXML
    private Pane mainPage, passwordPage, freeTextPane;
    @FXML
    private Label associationRuleCount, featureSampleCount, featureCount, termCount, username;
    @FXML
    private ToggleSwitch toggleSwitch;
    @FXML
    private TextField trainPwd;
    @FXML
    private TextArea inputPhrase, inputArea;


    @Autowired
    public void initializeBeans(StageCreationService stageCreationService,
                                AccessControlService accessControlService,
                                TrainingService trainingService,
                                SystemUserDataCollector dataCollector,
                                NotificationService notificationService,
                                SystemLogger logger) {

        InfoFXController.stageCreationService = stageCreationService;
        InfoFXController.accessControlService = accessControlService;
        InfoFXController.trainingService = trainingService;
        InfoFXController.logger = logger;
        InfoFXController.dataCollector = dataCollector;
        InfoFXController.notificationService = notificationService;
    }

    @FXML
    private void initialize(){
        toggleSwitch = new ToggleSwitch();
        toggleSwitch.setLayoutX(430);
        toggleSwitch.setLayoutY(385);
        toggleSwitch.setCursor(Cursor.HAND);
        mainPage.getChildren().addAll(toggleSwitch);
        username.setText(accessControlService.getAuthenticatedUser().getLogin());
        showMainPage();
    }

    public void showMainPage() {
        resetGUI();
        mainPage.setVisible(true);
        updateInfoCard();
        trainingService.deactivateListener();
    }
    public void showPasswordInput() {
        resetGUI();
        passwordPage.setVisible(true);
        trainingService.activateListener();
    }
    public void showFreeTextInput() throws IOException {
        resetGUI();
        freeTextPane.setVisible(true);
        updatePhrase();
        enableListener();
    }
    public void logout() throws IOException {
        accessControlService.logout();
        StageCreationService.getCurrentStage().close();
        stageCreationService.createStage("Ласкаво просимо!", new Stage(), GUIAction.LOGIN).show();
    }

    public void writeInputAreaSample(){
        if (inputPhrase.getText().trim().equals(inputArea.getText().trim())){
            updateKeystrokeSamplesSet();
        }
        else {
            notificationService.createNotification(Notification.ERROR_PHRASE);
        }
    }

    private void updateKeystrokeSamplesSet() {
        trainingService.writeSample(accessControlService.getAuthenticatedUser().getId());

        logger.log(SystemLogger.SAMPLE_SAVE_SUCCESS_RESULT);
        notificationService.createNotification(Notification.SUCCESS_WRITE);
        clearAllInputs();
        showMainPage();
    }

    public void writePasswordSample(){
        if(accessControlService.doPasswordAuthentication(trainPwd.getText().trim())) {
            updateKeystrokeSamplesSet();
        }
        else {
            notificationService.createNotification(Notification.ERROR_PWD);
        }
    }
    public void generateFIS() {
        List<AssociationRule> associationRules =
                trainingService.obtainNewKeystrokeAssociationRules(
                        accessControlService.getAuthenticatedUser().getId()
                );

        logger.log(SystemLogger.ASSOCIATION_RULES_SAVE_SUCCESS_RESULT);

        try { trainingService.generateInferringFIS(associationRules); }
        catch (IOException ioException) {
            ioException.printStackTrace();
            notificationService.createNotification(Notification.FAIL_FIS);
            return;
        }
        notificationService.createNotification(Notification.SUCCESS_FIS);
    }

    public void updatePhrase() throws IOException {
        String phrase = trainingService.extractArbitraryPhrase( inputPhrase.getText() );
        inputPhrase.setText(phrase);
        trainingService.clearKeystrokeData();
        clearAllInputs();
        logger.log(SystemLogger.KEY_FEATURE_CONTAINERS_CLEAN);
    }
    private void updateInfoCard(){
        toggleSwitch.switchedOnProperty().set(accessControlService.getAuthenticatedUser().isProtectionEnabled());
        associationRuleCount.setText(String.valueOf( dataCollector.getAssociationRulesCount() ));
        featureSampleCount.setText(String.valueOf( dataCollector.getFeatureSamplesCount() ));
        featureCount.setText(String.valueOf( dataCollector.getFeaturesCount() ));
        termCount.setText(String.valueOf( dataCollector.getTermsCount() ));

        toggleSwitch.animation.play();
    }

    public void clearAllInputs(){
        trainPwd.setText("");
        inputArea.setText("");
        trainingService.clearKeystrokeData();
        logger.log(SystemLogger.KEY_FEATURE_CONTAINERS_CLEAN);
    }
    public void resetGUI(){
        passwordPage.setVisible(false);
        freeTextPane.setVisible(false);
        mainPage.setVisible(false);
    }

    public void actionInputArea() {
        clearAllInputs();
        enableListener();
    }
    public void verifyFullText(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.SPACE)
            trainingService.processKeystrokeData();
        if (keyEvent.getCode() == KeyCode.ENTER)
            writeInputAreaSample();
    }
    public void checkEnterPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER)
            writePasswordSample();
    }

    public void enableListener(){
        if(trainingService.activateListener()) {
            System.out.println("Keyboard Listener activated");
        }
    }



    private class ToggleSwitch extends Parent {
        private final BooleanProperty switchedOn = new SimpleBooleanProperty(false);

        private final TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.25));
        private final FillTransition fillTransition = new FillTransition(Duration.seconds(0.25));
        private final ParallelTransition animation = new ParallelTransition(translateTransition, fillTransition);

        public BooleanProperty switchedOnProperty() {
            return switchedOn;
        }

        public ToggleSwitch() {

            Rectangle background = createToggleBackground();
            Circle trigger = createToggleTrigger();

            translateTransition.setNode(trigger);
            fillTransition.setShape(background);

            getChildren().addAll(background, trigger);
            switchedOn.addListener((obs, oldState, newState) -> {
                updateToggleTransitionPosition(background, trigger, newState);
                if (newState) { generateFIS(); }
                trainingService.updateUser(newState);
                updateInfoCard();
                animation.play();
            });
            setOnMouseClicked(event -> {
                switchedOn.set(!switchedOn.get());
            });
        }

        private void updateToggleTransitionPosition(Rectangle background, Circle trigger, Boolean newState) {
            translateTransition.setToX(newState ? background.getWidth() - trigger.getRadius() * 2 : 0);
            fillTransition.setFromValue(newState ? Color.WHITESMOKE : Color.LIMEGREEN);
            fillTransition.setToValue(newState ? Color.LIMEGREEN : Color.WHITESMOKE);
        }

        private Circle createToggleTrigger() {
            Circle trigger = new Circle(40);
            trigger.setCenterX(40);
            trigger.setCenterY(40);
            trigger.setFill(Color.WHITE);
            trigger.setStroke(Color.LIGHTGRAY);
            return trigger;
        }

        private Rectangle createToggleBackground() {
            Rectangle background = new Rectangle(150, 80);
            background.setArcHeight(80);
            background.setArcWidth(80);
            background.setFill(Color.WHITESMOKE);
            background.setStroke(Color.LIGHTGRAY);
            return background;
        }
    }
}
