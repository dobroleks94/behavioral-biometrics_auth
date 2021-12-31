package acs.behavioral_biometrics.fuzzy_profile_mapper.factory;

import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyValue;
import org.springframework.stereotype.Component;

@Component
public class FuzzyEntityFactory {

    public FuzzyValue createFuzzyValue(FuzzyMeasure measure, Double membershipRate){
        return new FuzzyValue(measure, membershipRate);
    }
}
