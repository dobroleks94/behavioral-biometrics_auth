package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeyProfile;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.Sample;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.SampleType;
import com.diploma.behavioralBiometricsAuthentication.factories.KeyInfoHolderFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class KeyProfileSamplesService {

    private final KeyInfoHolderFactory keyInfoHolderFactory;

    private Utility util;

    private List<Sample> samplesCollector;
    private List<KeyProfile> keyProfilesCollector, temporaryCollector;


    public KeyProfileSamplesService(KeyInfoHolderFactory keyInfoHolderFactory) {
        this.keyInfoHolderFactory = keyInfoHolderFactory;
    }

    @PostConstruct
    private void initializeVariables() {

        this.util = new Utility();

        this.samplesCollector = new ArrayList<>();
        this.keyProfilesCollector = new ArrayList<>();
        this.temporaryCollector = new ArrayList<>();
    }


    public void addTemporary(KeyProfile key) {
        temporaryCollector.add(key);
    }

    public void buildSamples() {
        if (util.gather(SampleType.DiGraph) && util.gather(SampleType.TriGraph)) {
            keyProfilesCollector.addAll(temporaryCollector);
            temporaryCollector.clear();
        }
    }

    public List<Sample> getSamplesCollector() {
        return samplesCollector;
    }
    public List<KeyProfile> getKeyProfilesCollector() {
        return keyProfilesCollector;
    }
    public List<KeyProfile> getTemporaryCollector() {
        return temporaryCollector;
    }
    public void clearAllContainers(){
        getTemporaryCollector().clear();
        getKeyProfilesCollector().clear();
        getSamplesCollector().clear();
    }



    private class Utility {

        private boolean gather(SampleType sampleType) {

            int threshold = sampleType.getContentLength() - 1;

            try {
                for (int i = 0; i < temporaryCollector.size() - threshold; i++) {
                    KeyProfile[] profiles = defineProfileSet(sampleType, i, threshold);
                    samplesCollector.add(createSample(profiles));
                }
                return true;
            } catch (RuntimeException e) {
                e.printStackTrace();
                samplesCollector.clear();
                return false;
            }
        }

        private KeyProfile[] defineProfileSet(SampleType sampleType, int index, int threshold) {
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

        private Sample createSample(KeyProfile[] keyProfile) {
            String splitter = "__";

            String name = Arrays.stream(keyProfile)
                    .map(profile -> profile.getKeyVal().concat(splitter))
                    .reduce("", String::concat);
            name = name.substring(0, name.length() - splitter.length());


            SampleType type = keyProfile.length == 2 ? SampleType.DiGraph :
                    keyProfile.length == 3 ? SampleType.TriGraph : SampleType.UNIDEFINED;
            if (type == SampleType.UNIDEFINED)
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
}
