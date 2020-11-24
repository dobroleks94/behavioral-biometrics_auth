package com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.SampleType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Sample {

    private String name;
    private SampleType type;

    private double meanDwell;
    private long flightTime,
                 keyUpTime,
                 keyDownTime;


    public Sample(String name, SampleType type, double meanDwell, long flightTime, long keyUpTime, long keyDownTime) {
        this.name = name;
        this.type = type;
        this.meanDwell = meanDwell;
        this.flightTime = flightTime;
        this.keyUpTime = keyUpTime;
        this.keyDownTime = keyDownTime;
    }
}
