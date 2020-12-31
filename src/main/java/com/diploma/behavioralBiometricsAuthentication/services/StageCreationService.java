package com.diploma.behavioralBiometricsAuthentication.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class StageCreationService {

    @Value("classpath:fxml/loginPage.fxml")
    private Resource loginPageResource;
    @Value("classpath:fxml/infoPage.fxml")
    private Resource infoPageResource;
    
    private static Stage currentStage;
    
    public Stage createStage(String title, Stage stage, String action) throws IOException {
        Resource fxmlTemplate = switch (action) {
            case "login" -> loginPageResource;
            case "info" -> infoPageResource;
            default -> throw new IllegalStateException("Unexpected value: " + action);
        };
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlTemplate.getURL());
        Parent root = fxmlLoader.load();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        setCurrentStage(stage);
        return stage;
    }

    public static Stage getCurrentStage() {
        return currentStage;
    }
    public static void setCurrentStage(Stage currentStage) {
        StageCreationService.currentStage = currentStage;
    }
}
