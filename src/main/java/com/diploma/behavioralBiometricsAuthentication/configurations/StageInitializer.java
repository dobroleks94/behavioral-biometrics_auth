package com.diploma.behavioralBiometricsAuthentication.configurations;

import com.diploma.IdentificationSystemFx.StageReadyEvent;
import com.diploma.behavioralBiometricsAuthentication.services.StageCreationService;
import javafx.stage.Stage;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;


@Configuration
public class StageInitializer implements ApplicationListener<StageReadyEvent> {

    private final StageCreationService stageCreationService;

    public StageInitializer(StageCreationService stageCreationService) {
        this.stageCreationService = stageCreationService;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Stage stage = stageCreationService.createStage("Ласкаво просимо!", event.getStage(), "login");
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
