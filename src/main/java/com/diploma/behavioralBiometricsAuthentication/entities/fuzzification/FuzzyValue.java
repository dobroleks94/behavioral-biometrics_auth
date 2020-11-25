package com.diploma.behavioralBiometricsAuthentication.entities.fuzzification;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import lombok.Getter;
import lombok.Setter;


/**
 * Fuzzy Value - is union of fuzzy measure item (fuzzy term)
 *          and membership degree of explored crisp indicator (how closely the crisp value is to possible fuzzy term)
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
