package acs.behavioral_biometrics.user_keystroke_profile.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Generalized names of features to be used in FeatureSample and
 *                  while converting to its fuzzy representation
 */
@Getter
@AllArgsConstructor
public enum Feature {

    TYPING_SPEED("typingSpeed"),
    MEAN_DWELL_TIME("meanDwellTime"),
    MEAN_DEL_BACKSP_DWELL("meanDelBackspDwell"),
    MEAN_FLIGHT_TIME("meanFlightTime"),
    MEAN_DIGRAPH_KU_TIME("meanDiGraphKUTime"),
    MEAN_DIGRAPH_KD_TIME("meanDiGraphKDTime"),
    MEAN_TRIGRAPH_KU_TIME("meanTriGraphKUTime"),
    MEAN_TRIGRAPH_KD_TIME("meanTriGraphKDTime"),
    MISTAKES_FREQUENCY("mistakesFrequency"),
    NUMPAD_USAGE_FREQUENCY("numPadUsageFrequency");

    private final String featureName;
    public static Feature getByFeatureName(String featureName){
        return Arrays.stream(Feature.values())
                .filter(feature -> featureName.equals(feature.getFeatureName()))
                .findFirst()
                .orElseThrow(RuntimeException::new);
    }
}
