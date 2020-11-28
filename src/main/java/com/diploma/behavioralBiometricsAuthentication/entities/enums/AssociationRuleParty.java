package com.diploma.behavioralBiometricsAuthentication.entities.enums;

public enum AssociationRuleParty {
    ANTECEDENT("Antecedent"), CONSEQUENT("Consequent");

    private String name;
    AssociationRuleParty(String name) {
        this.name = name;
    }
}
