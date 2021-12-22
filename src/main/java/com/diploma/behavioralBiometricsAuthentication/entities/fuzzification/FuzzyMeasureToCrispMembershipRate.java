package com.diploma.behavioralBiometricsAuthentication.entities.fuzzification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FuzzyMeasureToCrispMembershipRate {
    private FuzzyMeasureItem fuzzyMeasureItem;
    private double crispMembershipRate;
}
