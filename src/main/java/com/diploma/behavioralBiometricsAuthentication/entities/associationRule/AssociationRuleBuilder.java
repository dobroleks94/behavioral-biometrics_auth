package com.diploma.behavioralBiometricsAuthentication.entities.associationRule;

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
    public AssociationRuleBuilder grabAssociationItems(List<AssociationItem> items){
        String pattern = RuleConstructors.construct(
                RuleConstructors.SPACE,
                RuleConstructors.AND,
                RuleConstructors.SPACE
        );
        collectItemsLogicSequence(items, pattern);
        return this;
    }

    private void collectItemsLogicSequence(List<AssociationItem> items, String pattern) {
        items.stream()
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
