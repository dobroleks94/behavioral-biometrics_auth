package acs.behavioral_biometrics.fuzzy_inference_system.utils;

import acs.behavioral_biometrics.app_utils.service.UserService;
import acs.behavioral_biometrics.association_rules.enums.AssociationRuleParty;
import acs.behavioral_biometrics.association_rules.models.AssociationItem;
import acs.behavioral_biometrics.association_rules.models.AssociationRule;
import acs.behavioral_biometrics.fuzzy_inference_system.factory.FuzzyEntitiesFactory;
import acs.behavioral_biometrics.fuzzy_inference_system.services.FuzzyMeasureItemService;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FeatureName;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyMeasureItem;
import acs.behavioral_biometrics.fuzzy_profile_mapper.services.FuzzyFeatureSampleMapper;
import acs.behavioral_biometrics.user_keystroke_profile.enums.Feature;
import lombok.AllArgsConstructor;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTriangular;
import net.sourceforge.jFuzzyLogic.membership.Value;
import net.sourceforge.jFuzzyLogic.rule.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class FISComputeUtils {

    private final FuzzyEntitiesFactory factory;
    private final UserService userService;
    private final FuzzyFeatureSampleMapper fuzzyMapper;
    private final FuzzyMeasureItemService fuzzyMeasureItemService;

    public List<LinguisticTerm> defineOutputLinguisticTerms(Map<String, MembershipFunction> membershipFunctionMap) {
        return membershipFunctionMap.entrySet()
                .stream()
                .map(item -> factory.createTerm(item.getKey(), item.getValue()))
                .collect(Collectors.toList());
    }

    public void fillItemsWithFuzzyTerms(List<Variable> input) {
        input.forEach(
                variable -> updateVariable(
                        variable,
                        createInputTerms(variable.getName())
                )
        );
    }
    public void updateFunctionBlock(FunctionBlock fb, List<Variable> variables){
        variables.forEach(variable -> fb.setVariable(variable.getName(), variable));
    }
    public void updateVariable(Variable variable, List<LinguisticTerm> terms){
        terms.forEach(variable::add);
    }
    public void updateRule(Rule rule, RuleExpression antecedent, RuleTerm consequent){
        rule.setAntecedents(antecedent);
        rule.addConsequent(consequent.getVariable(), consequent.getTermName(), false);
    }

    public Map<String, MembershipFunction> getMembershipFunctions(int defuzzifyTermStep, Class<? extends MembershipFunction> membershipFunction) {
        Map<String, MembershipFunction> result = new HashMap<>();
        double currentPoint = 0.0;

        if (MembershipFunctionTriangular.class.equals(membershipFunction)) {
            for(var user : userService.findAll()){
                result.put(user.getLogin(), factory.createTriangularMF(
                        new Value[]{
                                new Value(currentPoint),
                                new Value(currentPoint + (defuzzifyTermStep / 2.0)),
                                new Value(currentPoint + defuzzifyTermStep)
                        }
                ));
                currentPoint += defuzzifyTermStep;
            }
        }
        return result;
    }

    public List<Rule> collectRules(List<AssociationRule> associationRules,
                                   List<Variable> variablesIn,
                                   Variable variableOut,
                                   RuleBlock ruleBlock){
        return associationRules.stream()
                .map(aRule -> {
                    Rule rule = factory.createRule(aRule.getId().toString(), ruleBlock);
                    List<RuleTerm> terms = collectRuleTerms(variablesIn, aRule, AssociationRuleParty.ANTECEDENT);
                    terms.addAll( collectRuleTerms(variablesIn, aRule, AssociationRuleParty.CONSEQUENT) );
                    RuleExpression expression = factory.createRuleExpressionAND(terms);
                    updateRule(rule, expression, factory.createRuleTerm(variableOut, userService.findById(aRule.getUserId()).getLogin()));
                    return rule;
                })
                .collect(Collectors.toList());

    }

    private Value[] valuesFor(String feature, FuzzyMeasure measure){
        FeatureName featureName = fuzzyMapper.chooseFeatureNameFrom(Feature.getByFeatureName(feature));
        List<FuzzyMeasureItem> fuzzyMeasures = fuzzyMeasureItemService.getFuzzyMeasuresByFeatureName(featureName);
        double min = Integer.MIN_VALUE;
        double mid = Integer.MIN_VALUE;
        double max = Integer.MIN_VALUE;
        switch (measure) {
            case VERY_LOW -> {
                min = fuzzyMeasures.get(0).getCrispDescriptor() - Math.abs(fuzzyMeasures.get(6).getCrispDescriptor());
                mid = fuzzyMeasures.get(0).getCrispDescriptor();
                max = fuzzyMeasures.get(1).getCrispDescriptor();
            }
            case LOW -> {
                min = fuzzyMeasures.get(0).getCrispDescriptor();
                mid = fuzzyMeasures.get(1).getCrispDescriptor();
                max = fuzzyMeasures.get(2).getCrispDescriptor();
            }
            case LESS_MEDIUM -> {
                min = fuzzyMeasures.get(1).getCrispDescriptor();
                mid = fuzzyMeasures.get(2).getCrispDescriptor();
                max = fuzzyMeasures.get(3).getCrispDescriptor();
            }
            case MEDIUM -> {
                min = fuzzyMeasures.get(2).getCrispDescriptor();
                mid = fuzzyMeasures.get(3).getCrispDescriptor();
                max = fuzzyMeasures.get(4).getCrispDescriptor();
            }
            case MORE_MEDIUM -> {
                min = fuzzyMeasures.get(3).getCrispDescriptor();
                mid = fuzzyMeasures.get(4).getCrispDescriptor();
                max = fuzzyMeasures.get(5).getCrispDescriptor();
            }
            case HIGH -> {
                min = fuzzyMeasures.get(4).getCrispDescriptor();
                mid = fuzzyMeasures.get(5).getCrispDescriptor();
                max = fuzzyMeasures.get(6).getCrispDescriptor();
            }
            case VERY_HIGH -> {
                min = fuzzyMeasures.get(5).getCrispDescriptor();
                mid = fuzzyMeasures.get(6).getCrispDescriptor();
                max = Math.abs(fuzzyMeasures.get(6).getCrispDescriptor()) * 2;
            }
        }
        return new Value[]{
                new Value(min),
                new Value(mid),
                new Value(max)
        };
    }
    private List<LinguisticTerm> createInputTerms(String feature){
        FeatureName featureName = fuzzyMapper.chooseFeatureNameFrom(Feature.getByFeatureName(feature));
        List<FuzzyMeasureItem> fuzzyMeasures = fuzzyMeasureItemService.getFuzzyMeasuresByFeatureName(featureName);
        return fuzzyMeasures.stream()
                .map(item -> factory.createTerm(
                                item.getFuzzyMeasure().getEngRepres(),
                                factory.createTriangularMF( valuesFor(feature, item.getFuzzyMeasure() ))
                        )
                )
                .collect(Collectors.toList());
    }

    private Variable getVariableByNameFrom(List<Variable> variables, String name){
        return variables.stream()
                .filter(var -> name.equals(var.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid variable name: " + name));
    }

    private List<RuleTerm> collectRuleTerms(List<Variable> variables,
                                            AssociationRule associationRule,
                                            AssociationRuleParty ruleParty){
        List<AssociationItem> items = switch (ruleParty) {
            case ANTECEDENT -> associationRule.getAntecedent();
            case CONSEQUENT -> associationRule.getConsequent();
        };

        return items.stream()
                .map(item -> factory.createRuleTerm(
                        getVariableByNameFrom(variables, item.getFeatureName()),
                        item.getMeasure().getEngRepres()))
                .collect(Collectors.toList());
    }
}
