package com.diploma.behavioralBiometricsAuthentication.factories;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyValue;
import org.springframework.stereotype.Component;

@Component
public class FuzzyEntitiesFactory {

    public FuzzyValue createFuzzyValue(FuzzyMeasure measure, Double membershipRate){
        return new FuzzyValue(measure, membershipRate);
    }

    public FuzzyMeasureItem createFuzzyMeasureItem(FeatureName featureName, FuzzyMeasure fuzzyMeasure, double crispDescriptor) {
        return new FuzzyMeasureItem(featureName, fuzzyMeasure, crispDescriptor);
    }
}
