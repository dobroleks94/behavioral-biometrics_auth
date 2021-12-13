package com.diploma.behavioralBiometricsAuthentication.entities.associationRule;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.AssociationRuleParty;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.RuleConstructors;

import java.util.List;

public class AssociationRuleBuilder {

    private static final StringBuilder stringBuilder = new StringBuilder();

    public static AssociationRuleBuilder builder() {
        return new AssociationRuleBuilder();
    }
    public AssociationRuleBuilder conditionWord() {
        stringBuilder.append(
                RuleConstructors.construct(
                        RuleConstructors.CONDITION,
                        RuleConstructors.SPACE
                )
        );
        return this;
    }
    public AssociationRuleBuilder grabAssociationItems(List<AssociationItem> items, AssociationRuleParty ruleParty){
        String pattern = RuleConstructors.construct(
                RuleConstructors.SPACE,
                RuleConstructors.AND,
                RuleConstructors.SPACE
        );
        collectItemsLogicSequence(items, pattern, ruleParty);
        return this;
    }

    private void collectItemsLogicSequence(List<AssociationItem> items, String pattern, AssociationRuleParty ruleParty) {
        items.stream()
                .filter(item -> item.getParty().equals(ruleParty))
                .map(AssociationItem::toString)
                .map(associationItem -> associationItem.concat(pattern))
                .forEach(stringBuilder::append);

        int redundantStart = stringBuilder.length() - pattern.length();
        int redundantEnd = stringBuilder.length();

        stringBuilder.delete( redundantStart, redundantEnd );
    }

    public AssociationRuleBuilder consequentWord(){
        stringBuilder.append(
                RuleConstructors.construct(
                        RuleConstructors.SPACE,
                        RuleConstructors.CONSEQUENCE,
                        RuleConstructors.SPACE
                )
        );
        return this;
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }
}
