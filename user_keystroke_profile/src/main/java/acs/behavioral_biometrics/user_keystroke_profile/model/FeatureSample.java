package acs.behavioral_biometrics.user_keystroke_profile.model;

import acs.behavioral_biometrics.user_keystroke_profile.enums.Feature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Map;


/**
 * Sample of user profile features measurements
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
@NoArgsConstructor
public class FeatureSample {

    @Id
    @GeneratedValue
    private Long id;
    private double typingSpeed,                // [Key per Minute -- keys/min] -> (amount of pressed keys / input time cost) * 60 sec
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

    public FeatureSample(double typingSpeed,
                         double meanDwellTime, double meanDelBackspDwell, double meanFlightTime,
                         double meanDigraphKUTime, double meanDigraphKDTime,
                         double meanTrigraphKUTime, double meanTrigraphKDTime,
                         double mistakesFrequency, double numPadUsageFrequency) {

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

    public FeatureSample(Map<Feature, Double> featureData){
        this (  featureData.get(Feature.TYPING_SPEED),
                featureData.get(Feature.MEAN_DWELL_TIME), featureData.get(Feature.MEAN_DEL_BACKSP_DWELL), featureData.get(Feature.MEAN_FLIGHT_TIME),
                featureData.get(Feature.MEAN_DIGRAPH_KU_TIME), featureData.get(Feature.MEAN_DIGRAPH_KD_TIME),
                featureData.get(Feature.MEAN_TRIGRAPH_KU_TIME), featureData.get(Feature.MEAN_TRIGRAPH_KD_TIME),
                featureData.get(Feature.MISTAKES_FREQUENCY), featureData.get(Feature.NUMPAD_USAGE_FREQUENCY));
    }

    public static FeatureSample createFeatureSample(Map<Feature, Double> featureData){
        return new FeatureSample(featureData);
    }
}
