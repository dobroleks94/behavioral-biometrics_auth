package acs.behavioral_biometrics.fuzzy_profile_mapper.model;

import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import lombok.Getter;
import lombok.Setter;


/**
 * Fuzzy Value - is union of fuzzy measure item (fuzzy term)
 *          and membership degree of explored crisp indicator (how closely the crisp value is to possible fuzzy term)
 * It is used to define the closest fuzzy range the explored crisp value belongs to
 */
@Getter
@Setter
public class FuzzyValue {

    private FuzzyMeasure measure;
    private Double membershipRate;

    public FuzzyValue(FuzzyMeasure measure, Double membershipRate) {
        this.measure = measure;
        this.membershipRate = membershipRate;
    }
}
