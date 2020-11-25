package com.diploma.behavioralBiometricsAuthentication.entities.fuzzification;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


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
    private FeatureName featureName;
    private FuzzyMeasure fuzzyMeasure;
    private double crispDescriptor;

    public FuzzyMeasureItem(FeatureName featureName, FuzzyMeasure fuzzyMeasure, double crispDescriptor) {
        this.featureName = featureName;
        this.fuzzyMeasure = fuzzyMeasure;
        this.crispDescriptor = crispDescriptor;
    }

    /*private double minThreshold;
    private double maxThreshold;

    public FuzzyMeasureItem(FeatureName featureName, FuzzyMeasure fuzzyMeasure, double minThreshold, double maxThreshold) {
        this.featureName = featureName;
        this.fuzzyMeasure = fuzzyMeasure;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }*/
}
