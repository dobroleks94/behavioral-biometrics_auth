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

    public List<FuzzyMeasureItem> getFuzzyMeasuresByFeatureName(FeatureName featureName) { return fuzzyMeasureRepository.findAllByFeatureName(featureName); }
    public List<FuzzyMeasureItem> getAllFuzzyMeasureItems() { return fuzzyMeasureRepository.findAll(); }
    public List<FuzzyMeasureItem> computeFuzzyMeasureItems() {

        fuzzyMeasureRepository.deleteAll();

        List<FuzzyMeasureItem> fuzzyItems = new ArrayList<>();

        fuzzyItems.addAll( utils.generate(FeatureName.DWELL_TIME, featureSampleService.getDwellTimeRange()[0], featureSampleService.getDwellTimeRange()[1]) );
        fuzzyItems.addAll( utils.generate(FeatureName.FLIGHT_TIME, featureSampleService.getFlightTimeRange()[0], featureSampleService.getFlightTimeRange()[1]) );
        fuzzyItems.addAll( utils.generate(FeatureName.DiGRAPH_KU_TIME, featureSampleService.getDigraphKUTimeRange()[0], featureSampleService.getDigraphKUTimeRange()[1]) );
        fuzzyItems.addAll( utils.generate(FeatureName.DiGRAPH_KD_TIME, featureSampleService.getDigraphKDTimeRange()[0], featureSampleService.getDigraphKDTimeRange()[1]) );
        fuzzyItems.addAll( utils.generate(FeatureName.TriGRAPH_KU_TIME, featureSampleService.getTrigraphKUTimeRange()[0], featureSampleService.getTrigraphKUTimeRange()[1]) );
        fuzzyItems.addAll( utils.generate(FeatureName.TriGRAPH_KD_TIME, featureSampleService.getTrigraphKDTimeRange()[0], featureSampleService.getTrigraphKDTimeRange()[1]) );
        fuzzyItems.addAll( utils.generate(FeatureName.SPEED, featureSampleService.getTypingSpeedRange()[0], featureSampleService.getTypingSpeedRange()[1]) );
        fuzzyItems.addAll( utils.generate(FeatureName.FREQUENCY, featureSampleService.getFrequencyRange()[0], featureSampleService.getFrequencyRange()[1] ));

        fuzzyMeasureRepository.saveAll(fuzzyItems);

        return getAllFuzzyMeasureItems();
    }


    private class Utility{

        private List<FuzzyMeasureItem> generate(FeatureName name, double min, double max){

            double step = (max - min) / (FuzzyMeasure.values().length - 1); // defining a value of fuzzy indicators' range size

            FuzzyMeasureItem veryLow = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.VERY_LOW, min);
            FuzzyMeasureItem low = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.LOW, veryLow.getCrispDescriptor() + step);
            FuzzyMeasureItem lessMedium = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.LESS_MEDIUM, low.getCrispDescriptor() + step);
            FuzzyMeasureItem medium = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.MEDIUM, lessMedium.getCrispDescriptor() + step);
            FuzzyMeasureItem moreMedium = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.MORE_MEDIUM, medium.getCrispDescriptor() + step);
            FuzzyMeasureItem high = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.HIGH, moreMedium.getCrispDescriptor() + step);
            FuzzyMeasureItem veryHigh = fuzzyEntitiesFactory.createFuzzyMeasureItem(name, FuzzyMeasure.VERY_HIGH, max);

            return Arrays.asList(veryLow, low, lessMedium, medium, moreMedium, high, veryHigh);
        }
    }
}
