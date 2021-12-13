package com.diploma.behavioralBiometricsAuthentication.entities.fuzzification;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;


/**
 * This class describes fuzzy ranges with its mean crisp value
 */

@Getter
@NoArgsConstructor
@Entity
@Table(name = "fuzzy_measure_holder")
public class FuzzyMeasureItem {

    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private FeatureName featureName;
    @Enumerated(EnumType.STRING)
    private FuzzyMeasure fuzzyMeasure;
    private double crispDescriptor;

    public FuzzyMeasureItem(FeatureName featureName, FuzzyMeasure fuzzyMeasure, double crispDescriptor) {
        this.featureName = featureName;
        this.fuzzyMeasure = fuzzyMeasure;
        this.crispDescriptor = crispDescriptor;
    }
}
