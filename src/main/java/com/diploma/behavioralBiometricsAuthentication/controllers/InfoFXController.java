package com.diploma.behavioralBiometricsAuthentication.controllers;

import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationRule;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.logger.SystemLogger;
import com.diploma.behavioralBiometricsAuthentication.listeners.KeyboardListener;
import com.diploma.behavioralBiometricsAuthentication.services.*;
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
import net.sourceforge.jFuzzyLogic.FIS;
import org.jnativehook.GlobalScreen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.util.List;


@Controller
public class InfoFXController {

    private static AssociationRulesService arService;
    private static FuzzyFeatureSampleService ffsService;
    private static FeatureSampleService fsService;
    private static KeyProfileSamplesService kpsService;
    private static SystemLogger logger;
    private static NotificationService notificationService;
    private static ArbitraryPhraseExtractor phraseExtractor;
    private static KeyboardListener listener;
    private static UserService userService;
    private static AuthenticationService authService;
    private static FuzzyMeasureItemService fuzzyMeasureItemService;
    private static FuzzyInferenceService fisService;
    private static IOManagerService ioManagerService;
    private static StageCreationService stageCreationService;

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

    private String phrase;
    private boolean activeListener;

    @Autowired
    public void initializeBeans(AssociationRulesService arService,
                                FuzzyFeatureSampleService ffsService,
                                KeyProfileSamplesService kpsService,
                                FeatureSampleService fsService,
                                SystemLogger logger,
                                NotificationService notificationService,
                                ArbitraryPhraseExtractor phraseExtractor,
                                KeyboardListener listener,
                                UserService userService,
                                AuthenticationService authService,
                                FuzzyMeasureItemService fuzzyMeasureItemService,
                                FuzzyInferenceService fisService,
                                IOManagerService ioManagerService,
                                StageCreationService stageCreationService) {

        InfoFXController.arService = arService;
        InfoFXController.ffsService = ffsService;
        InfoFXController.kpsService = kpsService;
        InfoFXController.fsService = fsService;
        InfoFXController.logger = logger;
        InfoFXController.notificationService = notificationService;
        InfoFXController.phraseExtractor = phraseExtractor;
        InfoFXController.listener = listener;
        InfoFXController.userService = userService;
        InfoFXController.authService = authService;
        InfoFXController.fuzzyMeasureItemService = fuzzyMeasureItemService;
        InfoFXController.fisService = fisService;
        InfoFXController.ioManagerService = ioManagerService;
        InfoFXController.stageCreationService = stageCreationService;
    }

    @FXML
    private void initialize(){
        toggleSwitch = new ToggleSwitch();
        toggleSwitch.setLayoutX(430);
        toggleSwitch.setLayoutY(385);
        toggleSwitch.setCursor(Cursor.HAND);

        mainPage.getChildren().addAll(toggleSwitch);

        username.setText(AuthenticationService.getAuthenticatedUser().getLogin());

        showMainPage();
    }

    public void showMainPage() {
        resetGUI();
        mainPage.setVisible(true);
        updateInfoCard();
        disableListener();
    }
    public void showPasswordInput() {
        resetGUI();
        passwordPage.setVisible(true);
        enableListener();
    }
    public void showFreeTextInput() throws IOException {
        resetGUI();
        freeTextPane.setVisible(true);
        updatePhrase();
        enableListener();
    }
    public void logout() throws IOException {
        authService.logout();
        StageCreationService.getCurrentStage().close();
        stageCreationService.createStage("Ласкаво просимо!", new Stage(), "login").show();
    }


    public void writeSample() {
            kpsService.buildSamples();

            FeatureSample featureSample = fsService.buildFeatureSample();
            featureSample.setUserId(AuthenticationService.getAuthenticatedUser().getId());

            fsService.save(featureSample);
            disableListener();

            logger.log(SystemLogger.SAMPLE_SAVE_SUCCESS_RESULT);
            notificationService.createNotification("success-write");

            clearAllInputs();
            updateUser(false);

            showMainPage();
    }
    public void writeInputAreaSample(){
        if (inputPhrase.getText().trim().equals(inputArea.getText().trim()))
            writeSample();
        else
            notificationService.createNotification("error-phrase");
    }
    public void writePasswordSample(){
        if(authService.passwordAuth(trainPwd.getText().trim()))
            writeSample();
        else
            notificationService.createNotification("error-password");
    }
    public void generateFIS() {
        ffsService.setFuzzyMeasures( fuzzyMeasureItemService.computeFuzzyMeasureItems() );
        ffsService.deleteAllByUserId(AuthenticationService.getAuthenticatedUser().getId());
        arService.deleteAll();

        List<FuzzyFeatureSample> fuzzyFeatures = ffsService.saveAll( fsService.findAll() );
        List<AssociationRule> associationRules = arService.saveAll(
                arService.assignOwner(userService.findById(AuthenticationService.getAuthenticatedUser().getId()),
                        arService.getAssociationRules(fuzzyFeatures))
        );
        logger.log(SystemLogger.ASSOCIATION_RULES_SAVE_SUCCESS_RESULT);

        FIS fis = fisService.createNewFIS(10, associationRules);
        try {
            ioManagerService.writeFIS(fis);
            notificationService.createNotification("success-writeFIS");
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
            notificationService.createNotification("fail-writeFIS");
        }


    }

    public void updateUser(boolean isProtected){
        AuthenticationService.getAuthenticatedUser().setProtect(isProtected);
        userService.saveUser(AuthenticationService.getAuthenticatedUser());
    }
    public void updatePhrase() throws IOException {
        phrase = phraseExtractor.getRandomPhrase();
        inputPhrase.setText(phrase);
        kpsService.clearAllContainers();
        logger.log(SystemLogger.KEY_FEATURE_CONTAINERS_CLEAN);
    }
    private void updateInfoCard(){
        toggleSwitch.switchedOnProperty().set(AuthenticationService.getAuthenticatedUser().isProtect());
        associationRuleCount.setText(String.valueOf( arService.getCount() ));
        featureSampleCount.setText(String.valueOf( fsService.getCount() ));
        featureCount.setText(String.valueOf( FuzzyFeatureSample.getMapKeys().size() ));
        termCount.setText(String.valueOf( FuzzyMeasure.values().length ));

        toggleSwitch.animation.play();
    }

    public void clearAllInputs(){
        trainPwd.setText("");
        inputArea.setText("");
        kpsService.clearAllContainers();
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
            kpsService.buildSamples();
        if (keyEvent.getCode() == KeyCode.ENTER)
            writeInputAreaSample();
    }
    public void checkEnterPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER)
            writePasswordSample();
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



    private class ToggleSwitch extends Parent {
        private BooleanProperty switchedOn = new SimpleBooleanProperty(false);

        private TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.25));
        private FillTransition fillTransition = new FillTransition(Duration.seconds(0.25));
        private ParallelTransition animation = new ParallelTransition(translateTransition, fillTransition);

        public BooleanProperty switchedOnProperty() {
            return switchedOn;
        }

        public ToggleSwitch() {

            Rectangle background = new Rectangle(150, 80);
            background.setArcHeight(80);
            background.setArcWidth(80);
            background.setFill(Color.WHITESMOKE);
            background.setStroke(Color.LIGHTGRAY);

            Circle trigger = new Circle(40);
            trigger.setCenterX(40);
            trigger.setCenterY(40);
            trigger.setFill(Color.WHITE);
            trigger.setStroke(Color.LIGHTGRAY);

            translateTransition.setNode(trigger);
            fillTransition.setShape(background);

            getChildren().addAll(background, trigger);

            switchedOn.addListener((obs, oldState, newState) -> {
                boolean isOn = newState;
                translateTransition.setToX(isOn ? background.getWidth() - trigger.getRadius() * 2 : 0);
                fillTransition.setFromValue(isOn ? Color.WHITESMOKE : Color.LIMEGREEN);
                fillTransition.setToValue(isOn ? Color.LIMEGREEN : Color.WHITESMOKE);

                if (isOn)
                    generateFIS();

                updateUser(isOn);
                updateInfoCard();

                animation.play();
            });

            setOnMouseClicked(event -> {
                switchedOn.set(!switchedOn.get());
            });
        }
    }
}
