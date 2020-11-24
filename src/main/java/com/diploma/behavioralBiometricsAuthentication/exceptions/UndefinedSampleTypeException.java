package com.diploma.behavioralBiometricsAuthentication.exceptions;

public class UndefinedSampleTypeException extends RuntimeException {

    public UndefinedSampleTypeException(String message) {
        super("Undefined sample exception: " + message);
    }
}
