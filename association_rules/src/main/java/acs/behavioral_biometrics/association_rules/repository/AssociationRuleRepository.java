package acs.behavioral_biometrics.association_rules.repository;

import acs.behavioral_biometrics.association_rules.models.AssociationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssociationRuleRepository extends JpaRepository<AssociationRule, Long> {
    void deleteAllByUserId(long userId);
}
