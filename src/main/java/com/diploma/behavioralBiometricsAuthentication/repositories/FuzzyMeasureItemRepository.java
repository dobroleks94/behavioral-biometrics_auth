package com.diploma.behavioralBiometricsAuthentication.repositories;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuzzyMeasureItemRepository extends JpaRepository<FuzzyMeasureItem, Long> {
    List<FuzzyMeasureItem> findAllByFeatureName(FeatureName featureName);
}
