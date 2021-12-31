package acs.behavioral_biometrics.user_keystroke_profile.utils;

import acs.behavioral_biometrics.user_keystroke_profile.service.FeatureSampleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class FeatureSampleComputeUtils {

    private final FeatureSampleService featureSampleService;

    public double [] getTypingSpeedRange() {
        double minimum = featureSampleService.findTypingSpeedValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum typing speed value..."));
        double maximum = featureSampleService.findTypingSpeedValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum typing speed value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getFrequencyRange() {
        double minimum = featureSampleService.findFrequencyValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = featureSampleService.findFrequencyValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getFlightTimeRange() {
        double minimum = featureSampleService.findFlightTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = featureSampleService.findFlightTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getDwellTimeRange() {
        double minimum = featureSampleService.findDwellTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = featureSampleService.findDwellTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getDigraphKUTimeRange() {
        double minimum = featureSampleService.findDigraphKUTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = featureSampleService.findDigraphKUTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getDigraphKDTimeRange() {
        double minimum = featureSampleService.findDigraphKDTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = featureSampleService.findDigraphKDTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getTrigraphKUTimeRange() {
        double minimum = featureSampleService.findTrigraphKUTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = featureSampleService.findTrigraphKUTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getTrigraphKDTimeRange() {
        double minimum = featureSampleService.findTrigraphKDTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = featureSampleService.findTrigraphKDTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
}
