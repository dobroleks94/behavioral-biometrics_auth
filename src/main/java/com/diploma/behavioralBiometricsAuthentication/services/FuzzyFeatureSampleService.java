package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.Membership;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureToCrispMembershipRate;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyValue;
import com.diploma.behavioralBiometricsAuthentication.factories.FeatureSampleFactory;
import com.diploma.behavioralBiometricsAuthentication.factories.FuzzyEntitiesFactory;
import com.diploma.behavioralBiometricsAuthentication.repositories.FuzzyFeatureSampleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Predicate;
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
            result.put(key, getFuzzySet(featureSample, key).getMeasure());
        }
        private FuzzyValue getFuzzySet(FeatureSample featureSample, String feature) {

            double crispValue = chooseCrispValueFrom(featureSample, feature);
            FeatureName featureName = chooseFeatureNameFrom(feature);

            if(isHigherThanThreshold(crispValue, featureName)){
                return fuzzyFactory.createFuzzyValue(FuzzyMeasure.VERY_HIGH, crispValue);
            }

            var possibleMembershipMap = getPossibleMembership(featureName, crispValue);

            return measures.stream()
                    .filter(measure -> measure.getFeatureName() == featureName)
                    .map(measure -> getFuzzyValue(
                            possibleMembershipMap.get(Membership.LEFT).getFuzzyMeasureItem(),
                            possibleMembershipMap.get(Membership.RIGHT).getFuzzyMeasureItem(),
                            possibleMembershipMap.get(Membership.LEFT).getCrispMembershipRate(),
                            possibleMembershipMap.get(Membership.RIGHT).getCrispMembershipRate(),
                            measure))
                    .max(Comparator.comparing(FuzzyValue::getMembershipRate))
                    .orElseThrow(RuntimeException::new);
        }

        private Map<Membership, FuzzyMeasureToCrispMembershipRate> getPossibleMembership(FeatureName featureName, double crispValue){
            Predicate<FuzzyMeasureItem> crispHigherOrEqual = item -> crispValue >= item.getCrispDescriptor();
            Predicate<FuzzyMeasureItem> crispLower = item -> crispValue < item.getCrispDescriptor();

            FuzzyMeasureItem leftPossible = getFuzzyMeasureItem(featureName, crispHigherOrEqual, FuzzyMeasure.VERY_LOW);
            FuzzyMeasureItem rightPossible = getFuzzyMeasureItem(featureName, crispLower, FuzzyMeasure.VERY_HIGH);

            double leftMembershipRate = (rightPossible.getCrispDescriptor() - crispValue) / (rightPossible.getCrispDescriptor() - leftPossible.getCrispDescriptor());
            double rightMembershipRate = (crispValue - leftPossible.getCrispDescriptor()) / (rightPossible.getCrispDescriptor() - leftPossible.getCrispDescriptor());

            return new HashMap<>(){
                {
                    put(Membership.LEFT, FuzzyMeasureToCrispMembershipRate.builder().fuzzyMeasureItem(leftPossible).crispMembershipRate(leftMembershipRate < 0 ? 0 : leftMembershipRate).build());
                    put(Membership.RIGHT, FuzzyMeasureToCrispMembershipRate.builder().fuzzyMeasureItem(rightPossible).crispMembershipRate(rightMembershipRate > 1 ? 1 : rightMembershipRate).build());
                }
            };

        }

        private boolean isHigherThanThreshold(double crispValue, FeatureName featureName) {
            return measures.stream()
                    .filter(item -> item.getFeatureName() == featureName && item.getFuzzyMeasure() == FuzzyMeasure.VERY_HIGH)
                    .findFirst()
                    .orElseThrow(RuntimeException::new)
                    .getCrispDescriptor() <= crispValue;
        }

        private FuzzyValue getFuzzyValue(FuzzyMeasureItem leftPossible, FuzzyMeasureItem rightPossible, double left, double right, FuzzyMeasureItem measure) {
            if (measure.getFuzzyMeasure() == leftPossible.getFuzzyMeasure())
                return fuzzyFactory.createFuzzyValue(leftPossible.getFuzzyMeasure(), left);
            else if (measure.getFuzzyMeasure() == rightPossible.getFuzzyMeasure())
                return fuzzyFactory.createFuzzyValue(rightPossible.getFuzzyMeasure(), right);
            else
                return fuzzyFactory.createFuzzyValue(measure.getFuzzyMeasure(), 0.0);
        }

        private FuzzyMeasureItem getFuzzyMeasureItem(FeatureName featureName, Predicate<FuzzyMeasureItem> crispFilter, FuzzyMeasure fuzzyMeasure) {
            return measures.stream()
                    .filter(item -> item.getFeatureName() == featureName)
                    .filter(crispFilter)
                    .reduce((first, last) -> last).orElse(
                            measures.stream()
                                    .filter(item -> item.getFeatureName() == featureName && item.getFuzzyMeasure() == fuzzyMeasure)
                                    .findFirst().orElseThrow(RuntimeException::new)
                    );
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
