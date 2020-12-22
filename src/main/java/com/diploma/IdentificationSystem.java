package com.diploma;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdentificationSystem{

    public static void main(String[] args) {
        //SpringApplication.run(IdentificationSystem.class, args);
        Application.launch(IdentificationSystemFx.class, args);
    }
}
