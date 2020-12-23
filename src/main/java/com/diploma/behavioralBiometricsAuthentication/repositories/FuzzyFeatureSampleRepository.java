package com.diploma.behavioralBiometricsAuthentication.repositories;

import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuzzyFeatureSampleRepository extends JpaRepository<FuzzyFeatureSample, Long> {
    void deleteAllByUserId(Long userId);
}
