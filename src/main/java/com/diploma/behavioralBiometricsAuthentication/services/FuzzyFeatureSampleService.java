package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyValue;
import com.diploma.behavioralBiometricsAuthentication.factories.FeatureSampleFactory;
import com.diploma.behavioralBiometricsAuthentication.factories.FuzzyEntitiesFactory;
import com.diploma.behavioralBiometricsAuthentication.repositories.FuzzyFeatureSampleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FuzzyFeatureSampleService {

    private final FuzzyFeatureSampleRepository fuzzyFeatureSampleRepository;
    private final FuzzyEntitiesFactory fuzzyFactory;
    private final FeatureSampleFactory featureSampleFactory;
    private Utility utils;
    private List<FuzzyMeasureItem> measures;

    public FuzzyFeatureSampleService(FuzzyFeatureSampleRepository fuzzyFeatureSampleRepository,
                                     FuzzyEntitiesFactory fuzzyFactory,
                                     FeatureSampleFactory featureSampleFactory) {
        this.fuzzyFeatureSampleRepository = fuzzyFeatureSampleRepository;
        this.fuzzyFactory = fuzzyFactory;
        this.featureSampleFactory = featureSampleFactory;
    }

    @PostConstruct
    private void initializeVariables() {
        this.utils = new Utility();
    }

    public List<FuzzyFeatureSample> findAll() {
        return fuzzyFeatureSampleRepository.findAll();
    }
    public List<FuzzyFeatureSample> saveAll(List<FeatureSample> featureSamples) {
        return fuzzyFeatureSampleRepository.saveAll(getFuzzyRepresentation(featureSamples));
    }
    public void deleteAll() { fuzzyFeatureSampleRepository.deleteAll(); }

    public void setFuzzyMeasures(List<FuzzyMeasureItem> measureItems) {
        this.measures = measureItems;
    }
    public List<FuzzyFeatureSample> getFuzzyRepresentation(List<FeatureSample> featureSamples) {
        return featureSamples.stream()
                .map(this::convertToFuzzyFeature)
                .collect(Collectors.toList());
    }
    public FuzzyFeatureSample convertToFuzzyFeature(FeatureSample featureSample) {
        return featureSampleFactory.createFuzzyFeatureSample(
                utils.fillFeatures(featureSample, measures)
               );
    }


    private class Utility {
        private Map<String, FuzzyMeasure> fillFeatures(FeatureSample featureSample, List<FuzzyMeasureItem> fuzzyMeasures) {

            HashMap<String, FuzzyMeasure> result = new HashMap<>();

            computeFuzzy("typingSpeed", featureSample, result);
            computeFuzzy("meanDwellTime", featureSample, result);
            computeFuzzy("meanDelBackspDwell", featureSample, result);
            computeFuzzy("meanFlightTime", featureSample, result);
            computeFuzzy("meanDiGraphKUTime", featureSample, result);
            computeFuzzy("meanDiGraphKDTime", featureSample, result);
            computeFuzzy("meanTriGraphKUTime", featureSample, result);
            computeFuzzy("meanTriGraphKDTime", featureSample, result);
            computeFuzzy("mistakesFrequency", featureSample, result);
            computeFuzzy("numPadUsageFrequency", featureSample, result);

            return result;
        }

        private void computeFuzzy(String key, FeatureSample featureSample, HashMap<String, FuzzyMeasure> result) {
            result.put(key, getFuzzySet(featureSample, key).get(0).getMeasure());
        }
        private List<FuzzyValue> getFuzzySet(FeatureSample featureSample, String feature) {

            double crispValue = chooseCrispValueFrom(featureSample, feature);
            FeatureName featureName = chooseFeatureNameFrom(feature);
            boolean isHigherThanThreshold = measures.stream()
                    .filter(item -> item.getFeatureName() == featureName && item.getFuzzyMeasure() == FuzzyMeasure.VERY_HIGH)
                    .findFirst()
                    .get()
                    .getCrispDescriptor() <= crispValue;

            if(isHigherThanThreshold){
                FuzzyValue fuzzyValue = fuzzyFactory.createFuzzyValue(FuzzyMeasure.VERY_HIGH, crispValue);
                return measures.stream().map(measure -> {
                    if ( measure.getFuzzyMeasure() == fuzzyValue.getMeasure())
                        return fuzzyValue;
                    return fuzzyFactory.createFuzzyValue(measure.getFuzzyMeasure(), 0.0);
                }).sorted((a, b) -> (int) (b.getMembershipRate() * 100 - a.getMembershipRate() * 100))
                        .collect(Collectors.toList());
            }

            FuzzyMeasureItem leftPossible = measures.stream()
                    .filter(item -> item.getFeatureName() == featureName)
                    .filter(item -> crispValue >= item.getCrispDescriptor())
                    .reduce((first, last) -> last).orElse(
                            measures.stream()
                                    .filter(item -> item.getFeatureName() == featureName && item.getFuzzyMeasure() == FuzzyMeasure.VERY_LOW)
                                    .findFirst().get()
                    );
            FuzzyMeasureItem rightPossible = measures.stream()
                    .filter(item -> item.getFeatureName() == featureName)
                    .filter(item -> crispValue < item.getCrispDescriptor())
                    .findFirst().orElse(
                            measures.stream()
                                    .filter(item -> item.getFeatureName() == featureName && item.getFuzzyMeasure() == FuzzyMeasure.VERY_HIGH)
                                    .findFirst().get()
                    );

            double leftMembershipRate = (rightPossible.getCrispDescriptor() - crispValue) / (rightPossible.getCrispDescriptor() - leftPossible.getCrispDescriptor());
            double rightMembershipRate = (crispValue - leftPossible.getCrispDescriptor()) / (rightPossible.getCrispDescriptor() - leftPossible.getCrispDescriptor());

            final double left = leftMembershipRate < 0 ? 0 : leftMembershipRate;
            final double right = rightMembershipRate > 1 ? 1 : rightMembershipRate;

            List<FuzzyValue> values = new ArrayList<>();
            measures.stream().filter(measure -> measure.getFeatureName() == featureName).forEach(measure -> {
                if (measure.getFuzzyMeasure() == leftPossible.getFuzzyMeasure())
                    values.add(fuzzyFactory.createFuzzyValue(leftPossible.getFuzzyMeasure(), left));
                else if (measure.getFuzzyMeasure() == rightPossible.getFuzzyMeasure())
                    values.add(fuzzyFactory.createFuzzyValue(rightPossible.getFuzzyMeasure(), right));
                else
                    values.add(fuzzyFactory.createFuzzyValue(measure.getFuzzyMeasure(), 0.0));

            });
            values.sort((a, b) -> (int) (b.getMembershipRate() * 100 - a.getMembershipRate() * 100));
            return values;


        }

        private Double chooseCrispValueFrom(FeatureSample featureSample, String feature) {
            switch (feature) {
                case "typingSpeed":
                    return featureSample.getTypingSpeed();
                case "meanDwellTime":
                    return featureSample.getMeanDwellTime();
                case "meanDelBackspDwell":
                    return featureSample.getMeanDelBackspDwell();
                case "meanFlightTime":
                    return featureSample.getMeanFlightTime();
                case "meanDiGraphKUTime":
                    return featureSample.getMeanDigraphKUTime();
                case "meanDiGraphKDTime":
                    return featureSample.getMeanDigraphKDTime();
                case "meanTriGraphKUTime":
                    return featureSample.getMeanTrigraphKUTime();
                case "meanTriGraphKDTime":
                    return featureSample.getMeanTrigraphKDTime();
                case "mistakesFrequency":
                    return featureSample.getMistakesFrequency();
                case "numPadUsageFrequency":
                    return featureSample.getNumPadUsageFrequency();
                default:
                    throw new RuntimeException("Bad feature name: " + feature);
            }
        }
        private FeatureName chooseFeatureNameFrom(String feature) {
            switch (feature) {
                case "typingSpeed":
                    return FeatureName.SPEED;
                case "meanDwellTime", "meanDelBackspDwell", "meanFlightTime",
                        "meanDiGraphKUTime", "meanDiGraphKDTime",
                        "meanTriGraphKUTime", "meanTriGraphKDTime":
                    return FeatureName.TIME;
                case "mistakesFrequency", "numPadUsageFrequency":
                    return FeatureName.FREQUENCY;
                default:
                    throw new RuntimeException("Bad feature name: " + feature);
            }
        }
    }
}
