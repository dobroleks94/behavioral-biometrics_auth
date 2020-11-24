package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.KeyEventState;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.SampleType;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.KeyProfile;
import com.diploma.behavioralBiometricsAuthentication.entities.keysAnalysisEntities.Sample;
import com.diploma.behavioralBiometricsAuthentication.exceptions.BadFeatureSampleException;
import com.diploma.behavioralBiometricsAuthentication.repositories.FeatureSampleRepository;
import lombok.AllArgsConstructor;
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
    private Utils utils;

    public FeatureSampleService(FeatureSampleRepository featureSampleRepository, KeyProfileSamplesService kpsService) {
        this.featureSampleRepository = featureSampleRepository;
        this.kpsService = kpsService;
    }

    @PostConstruct
    private void initializeValues(){
        this.utils = new Utils();
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

    public Double[] getTimeCostsRange() {
        Double minimum = findAscTimedValues(0).stream()
                .reduce(Double::min)
                .orElseThrow(() -> new RuntimeException("Cannot find minimum time value..."));
        Double maximum = findAscTimedValues((int) (getSize() - 1)).stream()
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

    public List<Double> findAscTimedValues(int index) {
        return Stream.of(
                featureSampleRepository.findAllByOrderByMeanDelBackspDwellAsc().get(index).getMeanDelBackspDwell(),
                featureSampleRepository.findAllByOrderByMeanDwellTimeAsc().get(index).getMeanDwellTime(),
                featureSampleRepository.findAllByOrderByMeanDigraphKUTimeAsc().get(index).getMeanDigraphKUTime(),
                featureSampleRepository.findAllByOrderByMeanDigraphKDTimeAsc().get(index).getMeanDigraphKDTime(),
                featureSampleRepository.findAllByOrderByMeanTrigraphKUTimeAsc().get(index).getMeanTrigraphKUTime(),
                featureSampleRepository.findAllByOrderByMeanTrigraphKDTimeAsc().get(index).getMeanTrigraphKDTime())
                .collect(Collectors.toList());

    }

    public FeatureSample buildFeatureSample() {
        if (kpsService.getSamplesCollector().size() > 0)
            return new FeatureSample(utils.fillFeatures(kpsService.getSamplesCollector(), kpsService.getKeyProfilesCollector()));
        else
            throw new BadFeatureSampleException();
    }


    private class Utils {
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
            kpsService.getTemporaryCollector().clear();
            return result;

        }

        private <T> long sumAll(List<T> list, Function<T, Long> function) {
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
