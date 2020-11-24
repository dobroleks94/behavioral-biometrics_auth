package com.diploma.behavioralBiometricsAuthentication.repositories;

import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuzzyMeasureItemRepository extends JpaRepository<FuzzyMeasureItem, Long> {
}
