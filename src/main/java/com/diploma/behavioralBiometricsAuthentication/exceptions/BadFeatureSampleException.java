package com.diploma.behavioralBiometricsAuthentication.exceptions;

public class BadFeatureSampleException extends RuntimeException {

    public BadFeatureSampleException() {
        super("It is likely to have been input nothing :(");
    }
}
