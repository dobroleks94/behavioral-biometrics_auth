package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeyProfile;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.Sample;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.KeyEventState;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.SampleType;
import com.diploma.behavioralBiometricsAuthentication.exceptions.BadFeatureSampleException;
import com.diploma.behavioralBiometricsAuthentication.exceptions.UndefinedSampleTypeException;
import com.diploma.behavioralBiometricsAuthentication.factories.KeyInfoHolderFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class KeyProfileSamplesService {

    private final KeyInfoHolderFactory keyInfoHolderFactory;

    private Utility util;

    private List<Sample> samplesCollector;
    private List<KeyProfile> keyProfilesCollector, temporaryCollector;


    public KeyProfileSamplesService(KeyInfoHolderFactory keyInfoHolderFactory){
        this.keyInfoHolderFactory = keyInfoHolderFactory;
    }

    @PostConstruct
    private void initializeVariables(){

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


    public FeatureSample buildFeatureSample() {
        if (samplesCollector.size() > 0)
            return new FeatureSample(util.fillFeatures(samplesCollector, keyProfilesCollector));
        else
            throw new BadFeatureSampleException();
    }




    private class Utility{

        private boolean gather(SampleType sampleType) {

            int threshold = sampleType.getContentLength() - 1;

            try {
                for (int i = 0; i < temporaryCollector.size() - threshold; i++) {
                    KeyProfile[] profiles = defineProfileSet(sampleType, i, threshold);
                    samplesCollector.add( createSample(profiles) );
                }
                return true;
            } catch (UndefinedSampleTypeException e){
                e.printStackTrace();

                samplesCollector.clear();
                return false;
            }
        }

        private Sample createSample(KeyProfile[] keyProfile) throws UndefinedSampleTypeException {
            String splitter = "__";

            String name = Arrays.stream(keyProfile)
                    .map(profile -> profile.getKeyVal().concat(splitter))
                    .reduce("", String::concat);
            name = name.substring(0, name.length() - splitter.length());


            SampleType type = keyProfile.length == 2 ? SampleType.DiGraph  :
                    keyProfile.length == 3 ? SampleType.TriGraph : SampleType.UNIDEFINED;
            if (type == SampleType.UNIDEFINED)
                throw new UndefinedSampleTypeException(type.name());


            double meanDwell = (double) Arrays.stream(keyProfile)
                    .map(KeyProfile::getHoldTime)
                    .reduce(Long::sum)
                    .orElse(0L) / keyProfile.length;
            long flightTime = keyProfile[keyProfile.length - 1].getPressTimestamp() - keyProfile[0].getReleaseTimestamp();
            long keyUpTime = keyProfile[keyProfile.length - 1].getReleaseTimestamp() - keyProfile[0].getReleaseTimestamp();
            long keyDownTime = keyProfile[keyProfile.length - 1].getPressTimestamp() - keyProfile[0].getPressTimestamp();


            return keyInfoHolderFactory.createSample(name, type, meanDwell, flightTime, keyDownTime, keyUpTime);
        }

        private KeyProfile[] defineProfileSet(SampleType sampleType, int index, int threshold){
            KeyProfile[] res =
                    sampleType == SampleType.DiGraph  ?
                            new KeyProfile[] {temporaryCollector.get(index), temporaryCollector.get(index + threshold) }
                            :
                            sampleType == SampleType.TriGraph ?
                                    new KeyProfile[] {temporaryCollector.get(index), temporaryCollector.get(index + threshold - 1), temporaryCollector.get(index + threshold) }
                                    : new KeyProfile[]{};
            if (res.length == 0)
                throw new UndefinedSampleTypeException(sampleType.name());

            return res;
        }

        private Map<String, Double> fillFeatures(List<Sample> samplesCollector, List<KeyProfile> keyProfilesCollector) {
            HashMap<String, Double> result = new HashMap<>();

            computeTypeSpeed(keyProfilesCollector, result);
            computeDwellTime(keyProfilesCollector, result); // compute dwell time for input keys and SEPARATELY for edit buttons (Del/Backsp)
            computeFlightTime(samplesCollector, result);
            computeKeyRelation(samplesCollector, result, SampleType.DiGraph, KeyEventState.KeyUp);
            computeKeyRelation(samplesCollector, result, SampleType.DiGraph, KeyEventState.KeyDown);
            computeKeyRelation(samplesCollector, result, SampleType.TriGraph, KeyEventState.KeyUp);
            computeKeyRelation(samplesCollector, result, SampleType.TriGraph, KeyEventState.KeyDown);
            calculateUsageOf("BackspDel", keyProfilesCollector, result);
            calculateUsageOf("NumPad", keyProfilesCollector, result);

            samplesCollector.clear();
            keyProfilesCollector.clear();
            temporaryCollector.clear();
            return result;

        }

        private <T> long sumAll(List<T> list, Function<T, Long> function){
            return list.stream()
                    .map(function)
                    .reduce(Long::sum)
                    .orElse(0L);
        }

        private void computeTypeSpeed(List<KeyProfile> keyProfilesCollector, HashMap<String, Double> result) {
            int size = keyProfilesCollector.size();
            int minute = 60;
            int first = 0, last = keyProfilesCollector.size() - 1;
            double inpTime = (double)
                    (keyProfilesCollector.get(last).getReleaseTimestamp() - keyProfilesCollector.get(first).getPressTimestamp()) / 1000;

            result.put("typingSpeed", (size / inpTime) * minute);
        }

        private void computeDwellTime(List<KeyProfile> keyProfilesCollector, HashMap<String, Double> result) {
            double dwellMean = (double) sumAll(keyProfilesCollector, KeyProfile::getHoldTime) / (long) keyProfilesCollector.size();

            List<KeyProfile> delKeys = keyProfilesCollector.stream().filter(item -> item.isBackspaceKey() || item.isDeleteKey()).collect(Collectors.toList());
            double delHoldMean = delKeys.size() > 0 ? (double) sumAll(delKeys, KeyProfile::getHoldTime) / delKeys.size() : 0;

            result.put("meanDwellTime", dwellMean);
            result.put("meanDelBackspDwell", delHoldMean);
        }

        private void computeFlightTime(List<Sample> samples, HashMap<String, Double> result) {
            List<Sample> digraphSamples = samples.stream().filter(smpl -> smpl.getType() == SampleType.DiGraph).collect(Collectors.toList());
            result.put("meanFlightTime",
                    ( (double)sumAll(digraphSamples, Sample::getFlightTime) / digraphSamples.size() ));
        }

        private void computeKeyRelation(List<Sample> samplesCollector, HashMap<String, Double> result, SampleType type, KeyEventState eventState) {
            List<Sample> samplesKUKD = samplesCollector.stream().filter(item -> item.getType() == type).collect(Collectors.toList());

            switch (eventState){
                case KeyUp:
                    result.put(String.format("mean%sKUTime", type.name()),
                            ( (double)sumAll(samplesKUKD, Sample::getKeyUpTime) / samplesKUKD.size()));
                    break;
                case KeyDown:
                    result.put(String.format("mean%sKDTime", type.name()),
                            ( (double)sumAll(samplesKUKD, Sample::getKeyDownTime) / samplesKUKD.size()));
                    break;
            }
        }

        private void calculateUsageOf(String what, List<KeyProfile> keyProfilesCollector, HashMap<String, Double> result) {
            long keyPressCount;
            switch (what){
                case "NumPad":
                    keyPressCount = keyProfilesCollector.stream().filter(KeyProfile::isNumpadKey).count();
                    result.put("numPadUsageFrequency", (double)((keyPressCount * 100) / keyProfilesCollector.size()) );
                    break;
                case "BackspDel":
                    keyPressCount = keyProfilesCollector.stream().filter(item -> item.isDeleteKey() || item.isBackspaceKey()).count();
                    result.put("mistakesFrequency", (double)((keyPressCount * 100) / keyProfilesCollector.size()) );
                    break;
            }
        }
    }
}
