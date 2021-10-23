package com.diploma.behavioralBiometricsAuthentication.repositories;

import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureSampleRepository extends JpaRepository<FeatureSample, Long> {
}
