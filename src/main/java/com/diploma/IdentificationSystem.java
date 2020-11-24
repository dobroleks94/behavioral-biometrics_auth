package com.diploma;

import com.diploma.behavioralBiometricsAuthentication.services.FeatureSampleService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdentificationSystem {

    private static FeatureSampleService service;
    public static void main(String[] args) {
        SpringApplication.run(IdentificationSystem.class, args);
    }


}
