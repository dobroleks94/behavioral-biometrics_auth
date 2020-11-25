package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.factories.FuzzyEntitiesFactory;
import com.diploma.behavioralBiometricsAuthentication.repositories.FuzzyMeasureItemRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FuzzyMeasureItemService {

    private final FuzzyMeasureItemRepository fuzzyMeasureRepository;
    private final FeatureSampleService featureSampleService;
    private final FuzzyEntitiesFactory fuzzyEntitiesFactory;
    private Utility utils;

    public FuzzyMeasureItemService(FuzzyMeasureItemRepository fuzzyMeasureRepository,
                                   FeatureSampleService featureSampleService,
                                   FuzzyEntitiesFactory fuzzyEntitiesFactory) {
        this.fuzzyMeasureRepository = fuzzyMeasureRepository;
        this.featureSampleService = featureSampleService;
        this.fuzzyEntitiesFactory = fuzzyEntitiesFactory;
    }

    @PostConstruct
    private void initializeVariables(){
        this.utils = new Utility();
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




    private class Utility{

        private List<FuzzyMeasureItem> generate(FeatureName name, double min, double max){

            double step = (max - min) / 5; // defining a value of fuzzy indicators' range size

            FuzzyMeasureItem veryLow = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.VERY_LOW, 0);
            FuzzyMeasureItem low = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.LOW, min);
            FuzzyMeasureItem lessMedium = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.LESS_MEDIUM, low.getCrispDescriptor() + step);
            FuzzyMeasureItem medium = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.MEDIUM, lessMedium.getCrispDescriptor() + step);
            FuzzyMeasureItem moreMedium = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.MORE_MEDIUM, medium.getCrispDescriptor() + step);
            FuzzyMeasureItem high = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.HIGH, moreMedium.getCrispDescriptor() + step);
            FuzzyMeasureItem veryHigh = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.VERY_HIGH, max);

            return Arrays.asList(veryLow, low, lessMedium, medium, moreMedium, high, veryHigh);
        }
    }
}
