package acs.behavioral_biometrics.keystroke_handler.utilities;

import acs.behavioral_biometrics.keystroke_handler.enums.SampleType;
import acs.behavioral_biometrics.keystroke_handler.factory.KeyInfoHolderFactory;
import acs.behavioral_biometrics.keystroke_handler.models.KeyProfile;
import acs.behavioral_biometrics.keystroke_handler.models.KeysSample;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@AllArgsConstructor
public class KeySamplesUtils {

    private final KeyInfoHolderFactory keyInfoHolderFactory;

    public boolean gather(SampleType sampleType, List<KeyProfile> temporaryCollector, List<KeysSample> samplesCollector) {

        int threshold = sampleType.getContentLength() - 1;

        try {
            for (int i = 0; i < temporaryCollector.size() - threshold; i++) {
                KeyProfile[] profiles = defineProfileSet(sampleType, temporaryCollector, i, threshold);
                samplesCollector.add(createSample(profiles));
            }
            return true;
        } catch (RuntimeException e) {
            e.printStackTrace();
            samplesCollector.clear();
            return false;
        }
    }

    private KeyProfile[] defineProfileSet(SampleType sampleType, List<KeyProfile> temporaryCollector, int index, int threshold) {
        KeyProfile[] res =
                sampleType == SampleType.DiGraph ?
                        new KeyProfile[]{temporaryCollector.get(index), temporaryCollector.get(index + threshold)}
                        :
                        sampleType == SampleType.TriGraph ?
                                new KeyProfile[]{temporaryCollector.get(index), temporaryCollector.get(index + threshold - 1), temporaryCollector.get(index + threshold)}
                                : new KeyProfile[]{};
        if (res.length == 0)
            throw new RuntimeException("Undefined sample type: " + sampleType.name());

        return res;
    }

    private KeysSample createSample(KeyProfile[] keyProfile) {
        String splitter = "__";

        String name = Arrays.stream(keyProfile)
                .map(profile -> profile.getKeyVal().concat(splitter))
                .reduce("", String::concat);
        name = name.substring(0, name.length() - splitter.length());


        SampleType type = keyProfile.length == 2 ? SampleType.DiGraph :
                keyProfile.length == 3 ? SampleType.TriGraph : SampleType.UNDEFINED;
        if (type == SampleType.UNDEFINED)
            throw new RuntimeException("Undefined sample type: " + type.name());


        double meanDwell = (double) Arrays.stream(keyProfile)
                .map(KeyProfile::getHoldTime)
                .reduce(Long::sum)
                .orElse(0L) / keyProfile.length;
        long flightTime = keyProfile[keyProfile.length - 1].getPressTimestamp() - keyProfile[0].getReleaseTimestamp();
        long keyUpTime = keyProfile[keyProfile.length - 1].getReleaseTimestamp() - keyProfile[0].getReleaseTimestamp();
        long keyDownTime = keyProfile[keyProfile.length - 1].getPressTimestamp() - keyProfile[0].getPressTimestamp();


        return keyInfoHolderFactory.createSample(name, type, meanDwell, flightTime, keyDownTime, keyUpTime);
    }
}
