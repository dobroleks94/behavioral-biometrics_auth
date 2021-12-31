package acs.behavioral_biometrics.user_keystroke_profile.repository;

import acs.behavioral_biometrics.user_keystroke_profile.model.FeatureSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureSampleRepository extends JpaRepository<FeatureSample, Long> {
}
