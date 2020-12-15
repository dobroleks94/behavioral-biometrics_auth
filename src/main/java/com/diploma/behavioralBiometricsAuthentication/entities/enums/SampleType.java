package com.diploma.behavioralBiometricsAuthentication.entities.enums;

/**
 * Combinations of keys under exploration:
 *      - DiGraph -> is combination of two (contentLength = 2) keys;
 *      - TriGraph -> is combination of three (contentLength = 3) keys;
 */
public enum SampleType {

    DiGraph(2), TriGraph(3), UNIDEFINED(0);

    private final int contentLength;

    SampleType(int contentLength) {
        this.contentLength = contentLength;
    }

    public int getContentLength() {
        return contentLength;
    }
}
