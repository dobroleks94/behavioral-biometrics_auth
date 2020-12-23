package com.diploma.behavioralBiometricsAuthentication.repositories;

import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureSampleRepository extends JpaRepository<FeatureSample, Long> {

    List<FeatureSample> findAllByOrderByTypingSpeedAsc();
    List<FeatureSample> findAllByOrderByMeanDelBackspDwellAsc();
    List<FeatureSample> findAllByOrderByMeanDwellTimeAsc();
    List<FeatureSample> findAllByOrderByMeanFlightTimeAsc();
    List<FeatureSample> findAllByOrderByMeanDigraphKUTimeAsc();
    List<FeatureSample> findAllByOrderByMeanDigraphKDTimeAsc();
    List<FeatureSample> findAllByOrderByMeanTrigraphKUTimeAsc();
    List<FeatureSample> findAllByOrderByMeanTrigraphKDTimeAsc();
    List<FeatureSample> findAllByOrderByMistakesFrequencyAsc();
    List<FeatureSample> findAllByOrderByNumPadUsageFrequencyAsc();
}
