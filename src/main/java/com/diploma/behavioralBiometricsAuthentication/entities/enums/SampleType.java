package com.diploma.behavioralBiometricsAuthentication.entities.enums;

public enum SampleType {

    DiGraph(2), TriGraph(3), UNIDEFINED(0);

    int contentLength;

    SampleType(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getContentLength() {
        return contentLength;
    }
}
