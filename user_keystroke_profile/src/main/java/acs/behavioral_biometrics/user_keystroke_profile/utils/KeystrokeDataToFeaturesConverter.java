package acs.behavioral_biometrics.user_keystroke_profile.utils;

import acs.behavioral_biometrics.keystroke_handler.enums.SampleType;
import acs.behavioral_biometrics.keystroke_handler.models.KeyProfile;
import acs.behavioral_biometrics.keystroke_handler.models.KeysSample;
import acs.behavioral_biometrics.user_keystroke_profile.enums.Feature;
import acs.behavioral_biometrics.user_keystroke_profile.enums.KeyEventState;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class KeystrokeDataToFeaturesConverter {

    public Map<Feature, Double> fillFeatures(List<KeysSample> samplesCollector, List<KeyProfile> keyProfilesCollector) {

        HashMap<Feature, Double> result = new HashMap<>();

        computeTypeSpeed(keyProfilesCollector, result);
        computeDwellTime(keyProfilesCollector, result); // compute dwell time for input keys and SEPARATELY for edit buttons (Del/Backsp)
        computeFlightTime(samplesCollector, result);
        computeKeyRelation(samplesCollector, result, SampleType.DiGraph, KeyEventState.KeyUp);
        computeKeyRelation(samplesCollector, result, SampleType.DiGraph, KeyEventState.KeyDown);
        computeKeyRelation(samplesCollector, result, SampleType.TriGraph, KeyEventState.KeyUp);
        computeKeyRelation(samplesCollector, result, SampleType.TriGraph, KeyEventState.KeyDown);
        calculateUsageOf("BackspDel", keyProfilesCollector, result);
        calculateUsageOf("NumPad", keyProfilesCollector, result);

        return result;

    }

    public  <T> long sumAll(List<T> list, Function<T, Long> function) {
        return list.stream()
                .map(function)
                .reduce(Long::sum)
                .orElse(0L);
    }

    public void computeTypeSpeed(List<KeyProfile> keyProfilesCollector, HashMap<Feature, Double> result) {
        int size = keyProfilesCollector.size();  // the amount of all keys used in password, for example
        int minute = 60; // seconds :)
        int first = 0, last = keyProfilesCollector.size() - 1; // indexes of first key and last key
        double inpTime = (double) // time, spent for typing password (in seconds)
                (keyProfilesCollector.get(last).getReleaseTimestamp() - keyProfilesCollector.get(first).getPressTimestamp()) / 1000;

        result.put(Feature.TYPING_SPEED, (size / inpTime) * minute); // [keys per minute]
    }
    public void computeDwellTime(List<KeyProfile> keyProfilesCollector, HashMap<Feature, Double> result) {
        double dwellMean = (double) sumAll(keyProfilesCollector, KeyProfile::getHoldTime) / (long) keyProfilesCollector.size();

        List<KeyProfile> delKeys = keyProfilesCollector.stream().filter(item -> item.isBackspaceKey() || item.isDeleteKey()).collect(Collectors.toList());
        double delHoldMean = delKeys.size() > 0 ? (double) sumAll(delKeys, KeyProfile::getHoldTime) / delKeys.size() : 0;

        result.put(Feature.MEAN_DWELL_TIME, dwellMean);
        result.put(Feature.MEAN_DEL_BACKSP_DWELL, delHoldMean);
    }
    public void computeFlightTime(List<KeysSample> keysSamples, HashMap<Feature, Double> result) {
        List<KeysSample> digraphKeysSamples = keysSamples.stream().filter(smpl -> smpl.getType() == SampleType.DiGraph).collect(Collectors.toList());
        result.put(Feature.MEAN_FLIGHT_TIME,
                ((double) sumAll(digraphKeysSamples, KeysSample::getFlightTime) / digraphKeysSamples.size()));
    }
    public void computeKeyRelation(List<KeysSample> samplesCollector, HashMap<Feature, Double> result, SampleType type, KeyEventState eventState) {
        List<KeysSample> samplesKUKD = samplesCollector.stream().filter(item -> item.getType() == type).collect(Collectors.toList());
        switch (eventState) {
            case KeyUp -> result.put(Feature.getByFeatureName(String.format("mean%sKUTime", type.name())),
                    ((double) sumAll(samplesKUKD, KeysSample::getKeyUpTime) / samplesKUKD.size()));
            case KeyDown -> result.put(Feature.getByFeatureName(String.format("mean%sKDTime", type.name())),
                    ((double) sumAll(samplesKUKD, KeysSample::getKeyDownTime) / samplesKUKD.size()));
        }
    }
    public void calculateUsageOf(String what, List<KeyProfile> keyProfilesCollector, HashMap<Feature, Double> result) {
        long keyPressCount;
        switch (what) {
            case "NumPad" -> {
                keyPressCount = keyProfilesCollector.stream().filter(KeyProfile::isNumpadKey).count();
                result.put(Feature.NUMPAD_USAGE_FREQUENCY, (double) ((keyPressCount * 100) / keyProfilesCollector.size()));
            }
            case "BackspDel" -> {
                keyPressCount = keyProfilesCollector.stream().filter(item -> item.isDeleteKey() || item.isBackspaceKey()).count();
                result.put(Feature.MISTAKES_FREQUENCY, (double) ((keyPressCount * 100) / keyProfilesCollector.size()));
            }
        }
    }
}
