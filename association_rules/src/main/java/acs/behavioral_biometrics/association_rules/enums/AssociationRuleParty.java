package acs.behavioral_biometrics.association_rules.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AssociationRuleParty {
    ANTECEDENT("Antecedent"), CONSEQUENT("Consequent");

    private final String name;
}
