package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.repositories.FuzzyFeatureSampleRepository;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
@Setter
public class FuzzyFeatureSampleService {

    private FuzzyFeatureSampleRepository fuzzyFeatureSampleRepository;
    private Utils utils;

    @PostConstruct
    private void initializeVariables(){
        this.utils = new Utils();
    }

    public List<FuzzyFeatureSample> findAll() { return  fuzzyFeatureSampleRepository.findAll(); }
    public void saveAll(List<FeatureSample> featureSamples) {  fuzzyFeatureSampleRepository.saveAll(utils.getFuzzyRepresentation(featureSamples)); }
    public void setFuzzyMeasures(List<FuzzyMeasureItem> measureItems){ utils.fuzzyMeasures = measureItems; }


    private class Utils{

        private List<FuzzyMeasureItem> fuzzyMeasures;

        private List<FuzzyFeatureSample> getFuzzyRepresentation(List<FeatureSample> featureSamples) {
            return new ArrayList<>();
        }
    }
}
