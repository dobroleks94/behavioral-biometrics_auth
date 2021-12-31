package acs.behavioral_biometrics.fuzzy_profile_mapper.model;

import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import acs.behavioral_biometrics.user_keystroke_profile.enums.Feature;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Sample for user profile features' fuzzy values
 *
 *  ->  typingSpeed - definitely speed of typing
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
    @Enumerated(EnumType.STRING)
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
    @Setter
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

    public FuzzyFeatureSample(Map<Feature, FuzzyMeasure> featureData){
        this (  featureData.get(Feature.TYPING_SPEED),
                featureData.get(Feature.MEAN_DWELL_TIME), featureData.get(Feature.MEAN_DEL_BACKSP_DWELL), featureData.get(Feature.MEAN_FLIGHT_TIME),
                featureData.get(Feature.MEAN_DIGRAPH_KU_TIME), featureData.get(Feature.MEAN_DIGRAPH_KD_TIME),
                featureData.get(Feature.MEAN_TRIGRAPH_KU_TIME), featureData.get(Feature.MEAN_TRIGRAPH_KD_TIME),
                featureData.get(Feature.MISTAKES_FREQUENCY), featureData.get(Feature.NUMPAD_USAGE_FREQUENCY));
    }

    public static FuzzyFeatureSample createFuzzyFeatureSample(Map<Feature, FuzzyMeasure> featureData){
        return new FuzzyFeatureSample(featureData);
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
                        Feature.TYPING_SPEED.getFeatureName(),
                        Feature.MEAN_DWELL_TIME.getFeatureName(), Feature.MEAN_DEL_BACKSP_DWELL.getFeatureName(), Feature.MEAN_FLIGHT_TIME.getFeatureName(),
                        Feature.MEAN_DIGRAPH_KU_TIME.getFeatureName(), Feature.MEAN_DIGRAPH_KD_TIME.getFeatureName(),
                        Feature.MEAN_TRIGRAPH_KU_TIME.getFeatureName(), Feature.MEAN_TRIGRAPH_KD_TIME.getFeatureName(),
                        Feature.MISTAKES_FREQUENCY.getFeatureName(), Feature.NUMPAD_USAGE_FREQUENCY.getFeatureName()
                );
    }
}
