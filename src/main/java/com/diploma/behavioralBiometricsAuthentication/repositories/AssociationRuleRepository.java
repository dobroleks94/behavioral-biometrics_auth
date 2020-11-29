package com.diploma.behavioralBiometricsAuthentication.repositories;

import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssociationRuleRepository extends JpaRepository<AssociationRule, Long> {
}
