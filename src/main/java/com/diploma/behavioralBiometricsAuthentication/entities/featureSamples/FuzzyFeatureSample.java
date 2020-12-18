package com.diploma.behavioralBiometricsAuthentication.entities.featureSamples;

import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import lombok.Getter;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Sample for user profile features' fuzzy values
 *
 *  ->  typingSpeed - definitely speed of typing (count of all pressed keys divided by time costs for input
 *                                                                          and multiplied by 60 sec [keys per minute] )
 *  ->  mean[...]Time - mean time:
 *               [dwell] - mean holding time of each key per transaction;
 *               [DelBackspDwell] - mean holding time of 'Delete' or 'Backspace' keys;
 *               [Flight] - time between releasing one key and pressing the next one;
 *               [DigraphKUTime] - time between releasing (KU - key up) one key and releasing the next one;
 *               [DigraphKDTime] - time between pressing (DK - key down) one key and pressing the next one;
 *               [TrigraphKUTime] - time between releasing first key and releasing last key in three-keys union
 *               [TrigraphKDTime] - time between pressing first key and pressing last key in three-keys union;
 *  ->   mistakesFrequency - relation of pressed 'Delete'/'Backspace' keys to general amount of pressed keys;
 *  ->   numPadUsageFrequency - relation of pressed NumPad keys to general amount of pressed keys;
 *
 */

@Getter
@Entity
public class FuzzyFeatureSample {

    @Id
    @GeneratedValue
    private Long id;
    private FuzzyMeasure typingSpeed,
                         meanDwellTime,
                         meanDelBackspDwell,
                         meanFlightTime,
                         meanDigraphKUTime,
                         meanDigraphKDTime,
                         meanTrigraphKUTime,
                         meanTrigraphKDTime,
                         mistakesFrequency,
                         numPadUsageFrequency;
    private long userId;

    public FuzzyFeatureSample() {}
    public FuzzyFeatureSample(FuzzyMeasure typingSpeed,
                              FuzzyMeasure meanDwellTime, FuzzyMeasure meanDelBackspDwell, FuzzyMeasure meanFlightTime,
                              FuzzyMeasure meanDigraphKUTime, FuzzyMeasure meanDigraphKDTime,
                              FuzzyMeasure meanTrigraphKUTime, FuzzyMeasure meanTrigraphKDTime,
                              FuzzyMeasure mistakesFrequency, FuzzyMeasure numPadUsageFrequency) {
        this.typingSpeed = typingSpeed;
        this.meanDwellTime = meanDwellTime;
        this.meanDelBackspDwell = meanDelBackspDwell;
        this.meanFlightTime = meanFlightTime;
        this.meanDigraphKUTime = meanDigraphKUTime;
        this.meanDigraphKDTime = meanDigraphKDTime;
        this.meanTrigraphKUTime = meanTrigraphKUTime;
        this.meanTrigraphKDTime = meanTrigraphKDTime;
        this.mistakesFrequency = mistakesFrequency;
        this.numPadUsageFrequency = numPadUsageFrequency;
    }

    public FuzzyFeatureSample (Map<String, FuzzyMeasure> featureData){
        this (  featureData.get("typingSpeed"),
                featureData.get("meanDwellTime"), featureData.get("meanDelBackspDwell"), featureData.get("meanFlightTime"),
                featureData.get("meanDiGraphKUTime"), featureData.get("meanDiGraphKDTime"),
                featureData.get("meanTriGraphKUTime"), featureData.get("meanTriGraphKDTime"),
                featureData.get("mistakesFrequency"), featureData.get("numPadUsageFrequency"));
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s, %s, %s, %s, %s, %s, %s, %s",
                                    typingSpeed.getShortRepres(),
                                    meanDwellTime.getShortRepres(),
                                    meanDelBackspDwell.getShortRepres(),
                                    meanFlightTime.getShortRepres(),
                                    meanDigraphKUTime.getShortRepres(),
                                    meanDigraphKDTime.getShortRepres(),
                                    meanTrigraphKUTime.getShortRepres(),
                                    meanTrigraphKDTime.getShortRepres(),
                                    mistakesFrequency.getShortRepres(),
                                    numPadUsageFrequency.getShortRepres());
    }

    public static List<String> getMapKeys(){
        return Arrays.asList(
                        "typingSpeed",
                        "meanDwellTime", "meanDelBackspDwell", "meanFlightTime",
                        "meanDiGraphKUTime", "meanDiGraphKDTime",
                        "meanTriGraphKUTime", "meanTriGraphKDTime",
                        "mistakesFrequency", "numPadUsageFrequency"
                );
    }
}
