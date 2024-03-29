package acs.behavioral_biometrics.fuzzy_profile_mapper.repository;

import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyFeatureSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FuzzyFeatureSampleRepository extends JpaRepository<FuzzyFeatureSample, Long> {
    void deleteAllByUserId(Long userId);
}
