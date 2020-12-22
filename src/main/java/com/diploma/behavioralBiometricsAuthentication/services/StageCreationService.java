package com.diploma.behavioralBiometricsAuthentication.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class StageCreationService {
    public Stage createStage(Stage stage, Resource fxmlTemplate) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlTemplate.getURL());
        Parent root = fxmlLoader.load();
        stage.setScene(new Scene(root));

        return stage;
    }
}
