package com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.SampleType;
import lombok.Getter;
import lombok.Setter;

/**
 * Combination of several keys (here is combination of 2 and 3 keys)
 * Contains information about relation between release/press timestamps of several keys, time between pressing two keys, and mean hold time of comprised keys
 */
@Getter
@Setter
public class KeysSample {

    private String name;
    private SampleType type;

    private double meanDwell;
    private long flightTime,
                 keyUpTime,
                 keyDownTime;


    public KeysSample(String name, SampleType type, double meanDwell, long flightTime, long keyUpTime, long keyDownTime) {
        this.name = name;
        this.type = type;
        this.meanDwell = meanDwell;
        this.flightTime = flightTime;
        this.keyUpTime = keyUpTime;
        this.keyDownTime = keyDownTime;
    }
}
