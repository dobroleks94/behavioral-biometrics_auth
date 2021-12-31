package acs.behavioral_biometrics.info_utils;

import acs.behavioral_biometrics.association_rules.services.AssociationRulesService;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyFeatureSample;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import acs.behavioral_biometrics.user_keystroke_profile.service.FeatureSampleService;

@Service
@AllArgsConstructor
public class SystemUserDataCollector {

    private final AssociationRulesService associationRulesService;
    private final FeatureSampleService featureSampleService;

    public long getAssociationRulesCount(){
        return associationRulesService.getCount();
    }
    public long getFeatureSamplesCount(){
        return featureSampleService.getCount();
    }
    public long getFeaturesCount(){
        return FuzzyFeatureSample.getMapKeys().size();
    }
    public long getTermsCount(){
        return FuzzyMeasure.values().length;
    }

}
