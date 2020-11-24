package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.repositories.FuzzyMeasureItemRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FuzzyMeasureItemService {

    private FuzzyMeasureItemRepository fuzzyMeasureRepository;
    private FeatureSampleService featureSampleService;

    public FuzzyMeasureItemService(FuzzyMeasureItemRepository fuzzyMeasureRepository, FeatureSampleService featureSampleService) {
        this.fuzzyMeasureRepository = fuzzyMeasureRepository;
        this.featureSampleService = featureSampleService;
    }

    private Utils utils;

    @PostConstruct
    private void initializeVariables(){
        this.utils = new Utils();
    }

    public List<FuzzyMeasureItem> getAllFuzzyMeasureItems() { return fuzzyMeasureRepository.findAll(); }
    public List<FuzzyMeasureItem> computeFuzzyMeasureItems() {

        fuzzyMeasureRepository.deleteAll();

        List<FuzzyMeasureItem> fuzzyItems = new ArrayList<>();

        double minTime = featureSampleService.getTimeCostsRange()[0];
        double maxTime = featureSampleService.getTimeCostsRange()[1];

        double minSpeed = featureSampleService.getTypingSpeedRange()[0];
        double maxSpeed = featureSampleService.getTypingSpeedRange()[1];

        double minFrequencyRate = featureSampleService.getFrequencyRange()[0];
        double maxFrequencyRate = featureSampleService.getFrequencyRange()[1];


        fuzzyItems.addAll( utils.generate(FeatureName.TIME, minTime, maxTime) );
        fuzzyItems.addAll( utils.generate(FeatureName.SPEED, minSpeed, maxSpeed) );
        fuzzyItems.addAll( utils.generate(FeatureName.FREQUENCY, minFrequencyRate, maxFrequencyRate ));

        fuzzyMeasureRepository.saveAll(fuzzyItems);

        return getAllFuzzyMeasureItems();
    }




    private class Utils{

        private List<FuzzyMeasureItem> generate(FeatureName name, double min, double max){

            double step = (max - min) / 5; // defining a value of fuzzy indicators' range size

            FuzzyMeasureItem veryLow = new FuzzyMeasureItem(name, FuzzyMeasure.VERY_LOW, 0, min);
            FuzzyMeasureItem low = new FuzzyMeasureItem(name, FuzzyMeasure.LOW, min + 0.01, veryLow.getMaxThreshold() + step);
            FuzzyMeasureItem lessMedium = new FuzzyMeasureItem(name, FuzzyMeasure.LESS_MEDIUM, low.getMaxThreshold() + 0.01, low.getMaxThreshold() + step);
            FuzzyMeasureItem medium = new FuzzyMeasureItem(name, FuzzyMeasure.MEDIUM, lessMedium.getMaxThreshold() + 0.01, lessMedium.getMaxThreshold() + step);
            FuzzyMeasureItem moreMedium = new FuzzyMeasureItem(name, FuzzyMeasure.MORE_MEDIUM, medium.getMaxThreshold() + 0.01, medium.getMaxThreshold() + step);
            FuzzyMeasureItem high = new FuzzyMeasureItem(name, FuzzyMeasure.HIGH, moreMedium.getMaxThreshold() + 0.01, max);
            FuzzyMeasureItem veryHigh = new FuzzyMeasureItem(name, FuzzyMeasure.VERY_HIGH, max + 0.01, Integer.MAX_VALUE);

            return Arrays.asList(veryLow, low, lessMedium, medium, moreMedium, high, veryHigh);
        }
    }
}
