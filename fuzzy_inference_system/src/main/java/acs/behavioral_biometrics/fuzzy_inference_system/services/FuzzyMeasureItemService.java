package acs.behavioral_biometrics.fuzzy_inference_system.services;

import acs.behavioral_biometrics.fuzzy_inference_system.factory.FuzzyEntitiesFactory;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FeatureName;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyMeasureItem;
import acs.behavioral_biometrics.fuzzy_profile_mapper.repository.FuzzyMeasureItemRepository;
import acs.behavioral_biometrics.user_keystroke_profile.utils.FeatureSampleComputeUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class FuzzyMeasureItemService {

    private final FuzzyMeasureItemRepository fuzzyMeasureRepository;
    private final FeatureSampleComputeUtils featureComputation;
    private final FuzzyEntitiesFactory fuzzyEntitiesFactory;

    public FuzzyMeasureItemService(FuzzyMeasureItemRepository fuzzyMeasureRepository,
                                   FeatureSampleComputeUtils featureComputation,
                                   FuzzyEntitiesFactory fuzzyEntitiesFactory) {
        this.fuzzyMeasureRepository = fuzzyMeasureRepository;
        this.featureComputation = featureComputation;
        this.fuzzyEntitiesFactory = fuzzyEntitiesFactory;
    }


    public List<FuzzyMeasureItem> getFuzzyMeasuresByFeatureName(FeatureName featureName) { return fuzzyMeasureRepository.findAllByFeatureName(featureName); }
    public List<FuzzyMeasureItem> getAllFuzzyMeasureItems() { return fuzzyMeasureRepository.findAll(); }
    public List<FuzzyMeasureItem> computeFuzzyMeasureItems() {

        fuzzyMeasureRepository.deleteAll();

        List<FuzzyMeasureItem> fuzzyItems = new ArrayList<>() {
            {
                addAll( generate(FeatureName.DWELL_TIME, featureComputation.getDwellTimeRange()[0], featureComputation.getDwellTimeRange()[1]) );
                addAll( generate(FeatureName.FLIGHT_TIME, featureComputation.getFlightTimeRange()[0], featureComputation.getFlightTimeRange()[1]) );
                addAll( generate(FeatureName.DiGRAPH_KU_TIME, featureComputation.getDigraphKUTimeRange()[0], featureComputation.getDigraphKUTimeRange()[1]) );
                addAll( generate(FeatureName.DiGRAPH_KD_TIME, featureComputation.getDigraphKDTimeRange()[0], featureComputation.getDigraphKDTimeRange()[1]) );
                addAll( generate(FeatureName.TriGRAPH_KU_TIME, featureComputation.getTrigraphKUTimeRange()[0], featureComputation.getTrigraphKUTimeRange()[1]) );
                addAll( generate(FeatureName.TriGRAPH_KD_TIME, featureComputation.getTrigraphKDTimeRange()[0], featureComputation.getTrigraphKDTimeRange()[1]) );
                addAll( generate(FeatureName.SPEED, featureComputation.getTypingSpeedRange()[0], featureComputation.getTypingSpeedRange()[1]) );
                addAll( generate(FeatureName.FREQUENCY, featureComputation.getFrequencyRange()[0], featureComputation.getFrequencyRange()[1] ));
            }
        };

        fuzzyMeasureRepository.saveAll(fuzzyItems);

        return getAllFuzzyMeasureItems();
    }

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
