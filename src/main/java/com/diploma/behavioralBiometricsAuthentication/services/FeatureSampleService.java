package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.KeyEventState;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.SampleType;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeyProfile;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.Sample;
import com.diploma.behavioralBiometricsAuthentication.factories.FeatureSampleFactory;
import com.diploma.behavioralBiometricsAuthentication.repositories.FeatureSampleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

@Service
public class FeatureSampleService {

    private final FeatureSampleRepository featureSampleRepository;
    private final KeyProfileSamplesService kpsService;
    private final FeatureSampleFactory featureSampleFactory;
    private Utility utils;

    public FeatureSampleService(FeatureSampleRepository featureSampleRepository,
                                KeyProfileSamplesService kpsService,
                                FeatureSampleFactory featureSampleFactory) {
        this.featureSampleRepository = featureSampleRepository;
        this.kpsService = kpsService;
        this.featureSampleFactory = featureSampleFactory;
    }

    @PostConstruct
    private void initializeValues(){
        this.utils = new Utility();
    }

    public FeatureSample save(FeatureSample sample) {
        return featureSampleRepository.save(sample);
    }
    public long getCount() { return featureSampleRepository.count(); }
    public List<FeatureSample> findAll() {
        return featureSampleRepository.findAll();
    }

    public FeatureSample buildFeatureSample() {
        if (kpsService.getSamplesCollector().size() > 0) {
            FeatureSample featureSample = featureSampleFactory.createFeatureSample(
                    utils.fillFeatures(kpsService.getSamplesCollector(), kpsService.getKeyProfilesCollector())
            );
            kpsService.clearAllContainers();
            return featureSample;
        }
        else
            throw new RuntimeException("It is likely to have been input nothing :(");
    }

    public double [] getTypingSpeedRange() {
        double minimum = findTypingSpeedValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum typing speed value..."));
        double maximum = findTypingSpeedValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum typing speed value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getFrequencyRange() {
        double minimum = findFrequencyValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = findFrequencyValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getFlightTimeRange() {
        double minimum = findFlightTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = findFlightTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getDwellTimeRange() {
        double minimum = findDwellTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = findDwellTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getDigraphKUTimeRange() {
        double minimum = findDigraphKUTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = findDigraphKUTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getDigraphKDTimeRange() {
        double minimum = findDigraphKDTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = findDigraphKDTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getTrigraphKUTimeRange() {
        double minimum = findTrigraphKUTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = findTrigraphKUTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }
    public double[] getTrigraphKDTimeRange() {
        double minimum = findTrigraphKDTimeValues().min().orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        double maximum = findTrigraphKDTimeValues().max().orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));
        return new double[]{minimum, maximum};
    }


    private DoubleStream findFrequencyValues() {
        List<FeatureSample> samples = featureSampleRepository.findAll();
        return DoubleStream.concat(
                samples.stream().mapToDouble(FeatureSample::getMistakesFrequency),
                samples.stream().mapToDouble(FeatureSample::getNumPadUsageFrequency)
        );
    }
    private DoubleStream findDwellTimeValues() {
        List<FeatureSample> samples = featureSampleRepository.findAll();
        return DoubleStream.concat(
                samples.stream().mapToDouble(FeatureSample::getMeanDelBackspDwell),
                samples.stream().mapToDouble(FeatureSample::getMeanDwellTime)
        );

    }
    private DoubleStream findFlightTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanFlightTime);
    }
    private DoubleStream findTypingSpeedValues(){
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getTypingSpeed);
    }
    private DoubleStream findDigraphKUTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanDigraphKUTime);
    }
    private DoubleStream findDigraphKDTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanDigraphKDTime);
    }
    private DoubleStream findTrigraphKUTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanTrigraphKUTime);
    }
    private DoubleStream findTrigraphKDTimeValues() {
        return featureSampleRepository.findAll().stream().mapToDouble(FeatureSample::getMeanTrigraphKDTime);
    }


    private static class Utility {

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

            return result;

        }

        private <T> long sumAll(List<T> list, Function<T, Long> function) {
            return list.stream()
                    .map(function)
                    .reduce(Long::sum)
                    .orElse(0L);
        }

        private void computeTypeSpeed(List<KeyProfile> keyProfilesCollector, HashMap<String, Double> result) {
            int size = keyProfilesCollector.size();  // the amount of all keys used in password, for example
            int minute = 60; // seconds :)
            int first = 0, last = keyProfilesCollector.size() - 1; // indexes of first key and last key
            double inpTime = (double) // time, spent for typing password (in seconds)
                    (keyProfilesCollector.get(last).getReleaseTimestamp() - keyProfilesCollector.get(first).getPressTimestamp()) / 1000;

            result.put("typingSpeed", (size / inpTime) * minute); // [keys per minute]
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
                    ((double) sumAll(digraphSamples, Sample::getFlightTime) / digraphSamples.size()));
        }
        private void computeKeyRelation(List<Sample> samplesCollector, HashMap<String, Double> result, SampleType type, KeyEventState eventState) {
            List<Sample> samplesKUKD = samplesCollector.stream().filter(item -> item.getType() == type).collect(Collectors.toList());

            switch (eventState) {
                case KeyUp:
                    result.put(String.format("mean%sKUTime", type.name()),
                            ((double) sumAll(samplesKUKD, Sample::getKeyUpTime) / samplesKUKD.size()));
                    break;
                case KeyDown:
                    result.put(String.format("mean%sKDTime", type.name()),
                            ((double) sumAll(samplesKUKD, Sample::getKeyDownTime) / samplesKUKD.size()));
                    break;
            }
        }
        private void calculateUsageOf(String what, List<KeyProfile> keyProfilesCollector, HashMap<String, Double> result) {
            long keyPressCount;
            switch (what) {
                case "NumPad":
                    keyPressCount = keyProfilesCollector.stream().filter(KeyProfile::isNumpadKey).count();
                    result.put("numPadUsageFrequency", (double) ((keyPressCount * 100) / keyProfilesCollector.size()));
                    break;
                case "BackspDel":
                    keyPressCount = keyProfilesCollector.stream().filter(item -> item.isDeleteKey() || item.isBackspaceKey()).count();
                    result.put("mistakesFrequency", (double) ((keyPressCount * 100) / keyProfilesCollector.size()));
                    break;
            }
        }

    }
}
