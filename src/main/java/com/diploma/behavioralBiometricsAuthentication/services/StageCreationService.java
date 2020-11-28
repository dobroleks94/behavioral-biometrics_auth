package com.diploma.behavioralBiometricsAuthentication.services;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;

@Service
public class StageCreationService {

    public Stage createStage(String fxmlLocation, int sceneWidth, int sceneHeight) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(this.getClass().getClassLoader().getResource(fxmlLocation)));

        Stage stage = new Stage();
        stage.setScene(new Scene(root, sceneWidth, sceneHeight));
        stage.setResizable(false);

        return stage;
    }

    public Stage createStage(Stage stage, String fxmlLocation, int sceneWidth, int sceneHeight) throws IOException {

        Parent root = FXMLLoader.load(Objects.requireNonNull(this.getClass().getClassLoader().getResource(fxmlLocation)));

        stage.setScene(new Scene(root, sceneWidth, sceneHeight));
        stage.setResizable(false);

        return stage;
    }
}
