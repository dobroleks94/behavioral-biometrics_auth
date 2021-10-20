package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.KeyEventState;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.SampleType;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeyProfile;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeysSample;
import com.diploma.behavioralBiometricsAuthentication.factories.FeatureSampleFactory;
import com.diploma.behavioralBiometricsAuthentication.repositories.FeatureSampleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    public Long getSize() {
        return featureSampleRepository.count();
    }
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

    public Double[] getTypingSpeedRange() {
        List<FeatureSample> samples = featureSampleRepository.findAllByOrderByTypingSpeedAsc();
        return new Double[]{
                samples.get(0).getTypingSpeed(),
                samples.get(samples.size() - 1).getTypingSpeed()
        };
    }
    public Double[] getFrequencyRange() {
        Double minimum = findAscFrequencyValues(0).stream()
                .reduce(Double::min)
                .orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        Double maximum = findAscFrequencyValues((int) (getSize() - 1)).stream()
                .reduce(Double::max)
                .orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));

        return new Double[]{minimum, maximum};
    }

    public Double[] getFlightTimeRange() {
        Double minimum = findAscFlightTimeValues(0).stream()
                .reduce(Double::min)
                .orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        Double maximum = findAscFlightTimeValues((int) (getSize() - 1)).stream()
                .reduce(Double::max)
                .orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));

        return new Double[]{minimum, maximum};
    }
    public Double[] getDwellTimeRange() {
        Double minimum = findAscDwellTimeValues(0).stream()
                .reduce(Double::min)
                .orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        Double maximum = findAscDwellTimeValues((int) (getSize() - 1)).stream()
                .reduce(Double::max)
                .orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));

        return new Double[]{minimum, maximum};
    }
    public Double[] getDigraphTimeRange() {
        Double minimum = findAscDigraphTimeValues(0).stream()
                .reduce(Double::min)
                .orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        Double maximum = findAscDigraphTimeValues((int) (getSize() - 1)).stream()
                .reduce(Double::max)
                .orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));

        return new Double[]{minimum, maximum};
    }
    public Double[] getTrigraphTimeRange() {
        Double minimum = findAscTrigraphTimeValues(0).stream()
                .reduce(Double::min)
                .orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        Double maximum = findAscTrigraphTimeValues((int) (getSize() - 1)).stream()
                .reduce(Double::max)
                .orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));

        return new Double[]{minimum, maximum};
    }
    public List<Double> findAscFrequencyValues(int index) {
        return Stream.of(
                featureSampleRepository.findAllByOrderByMistakesFrequencyAsc().get(index).getMistakesFrequency(),
                featureSampleRepository.findAllByOrderByNumPadUsageFrequencyAsc().get(index).getNumPadUsageFrequency()
        ).collect(Collectors.toList());
    }
    public List<Double> findAscDigraphTimeValues(int index) {
        return Stream.of(
                featureSampleRepository.findAllByOrderByMeanDigraphKUTimeAsc().get(index).getMeanDigraphKUTime(),
                featureSampleRepository.findAllByOrderByMeanDigraphKDTimeAsc().get(index).getMeanDigraphKDTime())
                .collect(Collectors.toList());

    }
    public List<Double> findAscTrigraphTimeValues(int index) {
        return Stream.of(
                featureSampleRepository.findAllByOrderByMeanTrigraphKUTimeAsc().get(index).getMeanTrigraphKUTime(),
                featureSampleRepository.findAllByOrderByMeanTrigraphKDTimeAsc().get(index).getMeanTrigraphKDTime())
                .collect(Collectors.toList());

    }
    public List<Double> findAscDwellTimeValues(int index) {
        return Stream.of(
                featureSampleRepository.findAllByOrderByMeanDelBackspDwellAsc().get(index).getMeanDelBackspDwell(),
                featureSampleRepository.findAllByOrderByMeanDwellTimeAsc().get(index).getMeanDwellTime())
                .collect(Collectors.toList());

    }
    public List<Double> findAscFlightTimeValues(int index) {
        return Stream.of( featureSampleRepository.findAllByOrderByMeanFlightTimeAsc().get(index).getMeanFlightTime() )
                .collect(Collectors.toList());
    }

    public long getCount() { return featureSampleRepository.count(); }

    @Deprecated
    public List<Double> findAscTimeValues(int index) {
        return Stream.of(
                featureSampleRepository.findAllByOrderByMeanDelBackspDwellAsc().get(index).getMeanDelBackspDwell(),
                featureSampleRepository.findAllByOrderByMeanDwellTimeAsc().get(index).getMeanDwellTime(),
                featureSampleRepository.findAllByOrderByMeanFlightTimeAsc().get(index).getMeanFlightTime(),
                featureSampleRepository.findAllByOrderByMeanDigraphKUTimeAsc().get(index).getMeanDigraphKUTime(),
                featureSampleRepository.findAllByOrderByMeanDigraphKDTimeAsc().get(index).getMeanDigraphKDTime(),
                featureSampleRepository.findAllByOrderByMeanTrigraphKUTimeAsc().get(index).getMeanTrigraphKUTime(),
                featureSampleRepository.findAllByOrderByMeanTrigraphKDTimeAsc().get(index).getMeanTrigraphKDTime())
                .collect(Collectors.toList());
    }

    @Deprecated
    public Double[] getTimeCostRange() {
        Double minimum = findAscTimeValues(0).stream()
                .reduce(Double::min)
                .orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        Double maximum = findAscTimeValues((int) (getSize() - 1)).stream()
                .reduce(Double::max)
                .orElseThrow(() -> new RuntimeException("Cannot find maximum time value..."));

        return new Double[]{minimum, maximum};
    }


    private class Utility {

        private Map<String, Double> fillFeatures(List<KeysSample> samplesCollector, List<KeyProfile> keyProfilesCollector) {

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
        private void computeFlightTime(List<KeysSample> keysSamples, HashMap<String, Double> result) {
            List<KeysSample> digraphKeysSamples = keysSamples.stream().filter(smpl -> smpl.getType() == SampleType.DiGraph).collect(Collectors.toList());
            result.put("meanFlightTime",
                    ((double) sumAll(digraphKeysSamples, KeysSample::getFlightTime) / digraphKeysSamples.size()));
        }
        private void computeKeyRelation(List<KeysSample> samplesCollector, HashMap<String, Double> result, SampleType type, KeyEventState eventState) {
            List<KeysSample> samplesKUKD = samplesCollector.stream().filter(item -> item.getType() == type).collect(Collectors.toList());

            switch (eventState) {
                case KeyUp:
                    result.put(String.format("mean%sKUTime", type.name()),
                            ((double) sumAll(samplesKUKD, KeysSample::getKeyUpTime) / samplesKUKD.size()));
                    break;
                case KeyDown:
                    result.put(String.format("mean%sKDTime", type.name()),
                            ((double) sumAll(samplesKUKD, KeysSample::getKeyDownTime) / samplesKUKD.size()));
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
