package com.diploma.behavioralBiometricsAuthentication.factories;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FeatureSampleFactory {

    public FeatureSample createFeatureSample(Map<String, Double> featureData){
        return new FeatureSample(featureData);
    }

    public FuzzyFeatureSample createFuzzyFeatureSample(Map<String, FuzzyMeasure> featureData){
        return new FuzzyFeatureSample(featureData);
    }
}
