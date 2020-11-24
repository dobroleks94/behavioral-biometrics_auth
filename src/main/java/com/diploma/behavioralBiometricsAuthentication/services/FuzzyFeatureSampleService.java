package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.KeyEventState;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.SampleType;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.repositories.FuzzyFeatureSampleRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FuzzyFeatureSampleService {

    private FuzzyFeatureSampleRepository fuzzyFeatureSampleRepository;
    private Utils utils;
    private List<FuzzyMeasureItem> fuzzyMeasures;

    public FuzzyFeatureSampleService(FuzzyFeatureSampleRepository fuzzyFeatureSampleRepository) {
        this.fuzzyFeatureSampleRepository = fuzzyFeatureSampleRepository;
    }

    @PostConstruct
    private void initializeVariables() {
        this.utils = new Utils();
    }

    public List<FuzzyFeatureSample> findAll() {
        return fuzzyFeatureSampleRepository.findAll();
    }

    public void saveAll(List<FeatureSample> featureSamples) {
        fuzzyFeatureSampleRepository.saveAll(getFuzzyRepresentation(featureSamples));
    }

    public void setFuzzyMeasures(List<FuzzyMeasureItem> measureItems) {
        this.fuzzyMeasures = measureItems;
    }


    public List<FuzzyFeatureSample> getFuzzyRepresentation(List<FeatureSample> featureSamples) {
        return featureSamples.stream().map(this::convertToFuzzyFeature).collect(Collectors.toList());
    }

    public FuzzyFeatureSample convertToFuzzyFeature(FeatureSample featureSample) {
        return new FuzzyFeatureSample(utils.fillFeatures(featureSample, fuzzyMeasures));
    }


    private class Utils {
        private Map<String, FuzzyMeasure> fillFeatures(FeatureSample featureSample, List<FuzzyMeasureItem> fuzzyMeasures) {
            HashMap<String, FuzzyMeasure> result = new HashMap<>();
            //TODO: create fuzzy representation
            /*computeTypeSpeed(featureSample, fuzzyMeasures, result);
            computeDwellTime(featureSample, fuzzyMeasures, result); // compute dwell time for input keys and SEPARATELY for edit buttons (Del/Backsp)
            computeFlightTime(featureSample, fuzzyMeasures, result);
            computeKeyRelation(featureSample, fuzzyMeasures, result, SampleType.DiGraph, KeyEventState.KeyUp);
            computeKeyRelation(featureSample, fuzzyMeasures, result, SampleType.DiGraph, KeyEventState.KeyDown);
            computeKeyRelation(featureSample, fuzzyMeasures, result, SampleType.TriGraph, KeyEventState.KeyUp);
            computeKeyRelation(featureSample, fuzzyMeasures, result, SampleType.TriGraph, KeyEventState.KeyDown);
            calculateUsageOf("BackspDel", featureSample, result);
            calculateUsageOf("NumPad", featureSample, result);*/

            return result;
        }

        private void computeTypeSpeed(FeatureSample featureSample, List<FuzzyMeasureItem> measures, HashMap<String, FuzzyMeasure> result) {

        }
    }

}
