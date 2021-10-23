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
import javax.transaction.Transactional;
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

    public List<FuzzyFeatureSample> saveAll(List<FeatureSample> featureSamples) {
        return fuzzyFeatureSampleRepository.saveAll(getFuzzyRepresentation(featureSamples));
    }
    public List<FuzzyFeatureSample> findAll(){ return fuzzyFeatureSampleRepository.findAll(); }
    @Transactional
    public void deleteAllByUserId(Long userId) { fuzzyFeatureSampleRepository.deleteAllByUserId(userId);}

    public void setFuzzyMeasures(List<FuzzyMeasureItem> measureItems) {
        this.measures = measureItems;
    }
    public List<FuzzyFeatureSample> getFuzzyRepresentation(List<FeatureSample> featureSamples) {
        return featureSamples.stream()
                .map(this::convertToFuzzyFeature)
                .collect(Collectors.toList());
    }
    public FuzzyFeatureSample convertToFuzzyFeature(FeatureSample featureSample) {
        FuzzyFeatureSample fuzzyFeatureSample = featureSampleFactory.createFuzzyFeatureSample(
                utils.fillFeatures(featureSample, measures)
        );
        fuzzyFeatureSample.setUserId(featureSample.getUserId());
        return fuzzyFeatureSample;
    }
    public FeatureName getFeatureName(String feature){ return utils.chooseFeatureNameFrom(feature); }

    public long getCount() { return fuzzyFeatureSampleRepository.count(); }


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
            return switch (feature) {
                case "typingSpeed" -> featureSample.getTypingSpeed();
                case "meanDwellTime" -> featureSample.getMeanDwellTime();
                case "meanDelBackspDwell" -> featureSample.getMeanDelBackspDwell();
                case "meanFlightTime" -> featureSample.getMeanFlightTime();
                case "meanDiGraphKUTime" -> featureSample.getMeanDigraphKUTime();
                case "meanDiGraphKDTime" -> featureSample.getMeanDigraphKDTime();
                case "meanTriGraphKUTime" -> featureSample.getMeanTrigraphKUTime();
                case "meanTriGraphKDTime" -> featureSample.getMeanTrigraphKDTime();
                case "mistakesFrequency" -> featureSample.getMistakesFrequency();
                case "numPadUsageFrequency" -> featureSample.getNumPadUsageFrequency();
                default -> throw new RuntimeException("Bad feature name: " + feature);
            };
        }
        private FeatureName chooseFeatureNameFrom(String feature) {
            return switch (feature) {
                case "typingSpeed" -> FeatureName.SPEED;
                case "meanDwellTime", "meanDelBackspDwell" -> FeatureName.DWELL_TIME;
                case "meanFlightTime" -> FeatureName.FLIGHT_TIME;
                case "meanDiGraphKUTime" -> FeatureName.DiGRAPH_KU_TIME;
                case "meanTriGraphKUTime" -> FeatureName.TriGRAPH_KU_TIME;
                case "meanDiGraphKDTime" -> FeatureName.DiGRAPH_KD_TIME;
                case "meanTriGraphKDTime" -> FeatureName.TriGRAPH_KD_TIME;
                case "mistakesFrequency", "numPadUsageFrequency" -> FeatureName.FREQUENCY;
                default -> throw new RuntimeException("Bad feature name: " + feature);
            };
        }
    }
}
