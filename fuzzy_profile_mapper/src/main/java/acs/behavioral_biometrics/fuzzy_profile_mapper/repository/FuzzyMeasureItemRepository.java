package acs.behavioral_biometrics.fuzzy_profile_mapper.repository;

import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FeatureName;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyMeasureItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FuzzyMeasureItemRepository extends JpaRepository<FuzzyMeasureItem, Long> {
    List<FuzzyMeasureItem> findAllByFeatureName(FeatureName featureName);
}
