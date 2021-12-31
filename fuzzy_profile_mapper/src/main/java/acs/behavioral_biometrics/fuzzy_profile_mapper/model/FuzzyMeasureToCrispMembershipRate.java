package acs.behavioral_biometrics.fuzzy_profile_mapper.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FuzzyMeasureToCrispMembershipRate {
    private FuzzyMeasureItem fuzzyMeasureItem;
    private double crispMembershipRate;
}
