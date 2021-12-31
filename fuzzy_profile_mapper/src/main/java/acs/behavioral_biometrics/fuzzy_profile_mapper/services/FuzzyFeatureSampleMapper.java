package acs.behavioral_biometrics.fuzzy_profile_mapper.services;

import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FeatureName;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.Membership;
import acs.behavioral_biometrics.fuzzy_profile_mapper.factory.FuzzyEntityFactory;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyFeatureSample;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyMeasureItem;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyMeasureToCrispMembershipRate;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyValue;
import acs.behavioral_biometrics.user_keystroke_profile.enums.Feature;
import acs.behavioral_biometrics.user_keystroke_profile.model.FeatureSample;
import acs.behavioral_biometrics.user_keystroke_profile.service.FeatureSampleService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FuzzyFeatureSampleMapper {

    @Getter @Setter
    private List<FuzzyMeasureItem> measures;

    private FeatureSampleService featureSampleService;
    private FuzzyEntityFactory fuzzyFactory;

    public List<FuzzyFeatureSample> getFuzzyRepresentation(List<FeatureSample> featureSamples) {
        return featureSamples.stream()
                .map(this::convertToFuzzyFeature)
                .collect(Collectors.toList());
    }
    public FuzzyFeatureSample convertToFuzzyFeature(FeatureSample featureSample) {
        FuzzyFeatureSample fuzzyFeatureSample = FuzzyFeatureSample.createFuzzyFeatureSample(
                fillFeatures(featureSample, measures)
        );
        fuzzyFeatureSample.setUserId(featureSample.getUserId());
        return fuzzyFeatureSample;
    }
    public FeatureName chooseFeatureNameFrom(Feature feature) {
        return switch (feature) {
            case TYPING_SPEED -> FeatureName.SPEED;
            case MEAN_DWELL_TIME, MEAN_DEL_BACKSP_DWELL -> FeatureName.DWELL_TIME;
            case MEAN_FLIGHT_TIME -> FeatureName.FLIGHT_TIME;
            case MEAN_DIGRAPH_KU_TIME -> FeatureName.DiGRAPH_KU_TIME;
            case MEAN_TRIGRAPH_KU_TIME -> FeatureName.TriGRAPH_KU_TIME;
            case MEAN_DIGRAPH_KD_TIME -> FeatureName.DiGRAPH_KD_TIME;
            case MEAN_TRIGRAPH_KD_TIME -> FeatureName.TriGRAPH_KD_TIME;
            case MISTAKES_FREQUENCY, NUMPAD_USAGE_FREQUENCY -> FeatureName.FREQUENCY;
        };
    }

    private Map<Feature, FuzzyMeasure> fillFeatures(FeatureSample featureSample, List<FuzzyMeasureItem> fuzzyMeasures) {

        HashMap<Feature, FuzzyMeasure> result = new HashMap<>();

        computeFuzzy(Feature.TYPING_SPEED, featureSample, result);
        computeFuzzy(Feature.MEAN_DWELL_TIME, featureSample, result);
        computeFuzzy(Feature.MEAN_DEL_BACKSP_DWELL, featureSample, result);
        computeFuzzy(Feature.MEAN_FLIGHT_TIME, featureSample, result);
        computeFuzzy(Feature.MEAN_DIGRAPH_KU_TIME, featureSample, result);
        computeFuzzy(Feature.MEAN_DIGRAPH_KD_TIME, featureSample, result);
        computeFuzzy(Feature.MEAN_TRIGRAPH_KU_TIME, featureSample, result);
        computeFuzzy(Feature.MEAN_TRIGRAPH_KD_TIME, featureSample, result);
        computeFuzzy(Feature.MISTAKES_FREQUENCY, featureSample, result);
        computeFuzzy(Feature.NUMPAD_USAGE_FREQUENCY, featureSample, result);

        return result;
    }

    private void computeFuzzy(Feature key, FeatureSample featureSample, HashMap<Feature, FuzzyMeasure> result) {
        result.put(key, convertToFuzzyValue(featureSample, key).getMeasure());
    }

    private FuzzyValue convertToFuzzyValue(FeatureSample featureSample, Feature feature) {

        double crispValue = featureSampleService.chooseCrispValueFrom(featureSample, feature);
        FeatureName featureName = chooseFeatureNameFrom(feature);

        if( isHigherThanThreshold(crispValue, featureName) ){
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
}
