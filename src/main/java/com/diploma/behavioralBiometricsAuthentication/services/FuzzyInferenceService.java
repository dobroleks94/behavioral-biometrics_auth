package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationItem;
import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationRule;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.AssociationRuleParty;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.VarOutput;
import com.diploma.behavioralBiometricsAuthentication.factories.FuzzyEntitiesFactory;
import lombok.AllArgsConstructor;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTriangular;
import net.sourceforge.jFuzzyLogic.membership.Value;
import net.sourceforge.jFuzzyLogic.rule.*;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FuzzyInferenceService {

    private final FuzzyEntitiesFactory factory;
    private final FuzzyMeasureItemService fuzzyMeasureItemService;
    private final FuzzyFeatureSampleService fuzzyFeatureSampleService;

    private Utility utility;

    public FuzzyInferenceService(FuzzyEntitiesFactory factory, FuzzyMeasureItemService fuzzyMeasureItemService, FuzzyFeatureSampleService fuzzyFeatureSampleService) {
        this.factory = factory;
        this.fuzzyMeasureItemService = fuzzyMeasureItemService;
        this.fuzzyFeatureSampleService = fuzzyFeatureSampleService;
    }

    @PostConstruct
    private void initializeVariables(){
        this.utility = new Utility();
    }

    public FIS createNewFIS(int defuzzifyTermStep, List<AssociationRule> associationRules){
        FIS fis = factory.createPlainFIS();
        FunctionBlock functionBlock = factory.createFunctionBlock("authenticator", fis);

        List<Variable> input = utility.createInputVariables();
        Variable output = factory.createVariable("user");

        factory.updateFunctionBlock(functionBlock, input);  // filling the VAR_INPUT block
        functionBlock.setVariable(output.getName(), output); // filling the VAR_OUTPUT block

        input.forEach(
                variable -> factory.updateVariable(
                        variable,
                        utility.createInputTerms(variable.getName())
                )
        ); // creating TERMS for each FUZZIFY item

        Map<VarOutput, MembershipFunction> membershipFunctionMap = utility.getMembershipFunctions
                (defuzzifyTermStep, MembershipFunctionTriangular.class);  // Membership function ranges for output decision

        List<LinguisticTerm> outputTerms = membershipFunctionMap.entrySet()
                .stream()
                .map(item -> factory.createTerm(item.getKey().name(), item.getValue()))
                .collect(Collectors.toList());  // output TERMS for decision (Genuine or Intruder user)
        factory.updateVariable(output, outputTerms);  // adding terms to respective variables
        output.setDefuzzifier(factory.createDefuzzifierCOG(output)); // Defuzzifier definition and assigning to output variable
        RuleBlock ruleBlock = factory.createRuleBlock("rulesFromAssociationRules", functionBlock); // creating rule block which contains all rules
        List<Rule> rules = utility.collectRules(associationRules, input, output, ruleBlock); // generating rules from Association Rules

        HashMap<String, RuleBlock> ruleBlocksMap = new HashMap<>();
        ruleBlocksMap.put(ruleBlock.getName(), ruleBlock);
        functionBlock.setRuleBlocks(ruleBlocksMap);  //including Rule Block to Function Block

        rules.forEach(ruleBlock::add); // including rules to Rule Block


        return fis;
    }



    private class Utility{

        private List<Variable> createInputVariables() {
            return FuzzyFeatureSample.getMapKeys()
                    .stream()
                    .map(factory::createVariable)
                    .collect(Collectors.toList());
        }
        private Value[] valuesFor(String feature, FuzzyMeasure measure){
            FeatureName featureName = fuzzyFeatureSampleService.getFeatureName(feature);
            List<FuzzyMeasureItem> fuzzyMeasures = fuzzyMeasureItemService.getFuzzyMeasuresByFeatureName(featureName);
            int indexMin = Integer.MIN_VALUE;
            int indexMid = Integer.MIN_VALUE;
            int indexMax = Integer.MIN_VALUE;
            switch (measure) {
                case VERY_LOW -> {
                    indexMin = 0;
                    indexMid = 0;
                    indexMax = 1;
                }
                case LOW -> {
                    indexMin = 0;
                    indexMid = 1;
                    indexMax = 2;
                }
                case LESS_MEDIUM -> {
                    indexMin = 1;
                    indexMid = 2;
                    indexMax = 3;
                }
                case MEDIUM -> {
                    indexMin = 2;
                    indexMid = 3;
                    indexMax = 4;
                }
                case MORE_MEDIUM -> {
                    indexMin = 3;
                    indexMid = 4;
                    indexMax = 5;
                }
                case HIGH -> {
                    indexMin = 4;
                    indexMid = 5;
                    indexMax = 6;
                }
                case VERY_HIGH -> {
                    indexMin = 5;
                    indexMid = 6;
                    indexMax = 6;
                }
            }
            return new Value[]{
                    new Value(fuzzyMeasures.get(indexMin).getCrispDescriptor()),
                    new Value(fuzzyMeasures.get(indexMid).getCrispDescriptor()),
                    new Value(fuzzyMeasures.get(indexMax).getCrispDescriptor())
            };
        }
        private List<LinguisticTerm> createInputTerms(String feature){
            FeatureName featureName = fuzzyFeatureSampleService.getFeatureName(feature);
            List<FuzzyMeasureItem> fuzzyMeasures = fuzzyMeasureItemService.getFuzzyMeasuresByFeatureName(featureName);
            return fuzzyMeasures.stream()
                    .map(item -> factory.createTerm(
                            item.getFuzzyMeasure().getEngRepres(),
                            factory.createTriangularMF( valuesFor(feature, item.getFuzzyMeasure() ))
                            )
                    )
                    .collect(Collectors.toList());
        }
        private Map<VarOutput, MembershipFunction> getMembershipFunctions(int defuzzifyTermStep, Class<? extends MembershipFunction> membershipFunction) {
            Map<VarOutput, MembershipFunction> result = new HashMap<>();
            double currentPoint = 0.0;
            if (MembershipFunctionTriangular.class.equals(membershipFunction)) {
                for(var user : VarOutput.values()){
                    result.put(user, factory.createTriangularMF(
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
        private Variable getVariableByNameFrom(List<Variable> variables, String name){
            return variables.stream()
                    .filter(var -> name.equals(var.getName()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Invalid variable name: " + name));
        }
        private List<Rule> collectRules(List<AssociationRule> associationRules,
                                        List<Variable> variablesIn,
                                        Variable variableOut,
                                        RuleBlock ruleBlock){

            return associationRules.stream()
                    .map(aRule -> {
                        Rule rule = factory.createRule("Rule #" + aRule.getId(), ruleBlock);
                        List<RuleTerm> terms = collectRuleTerms(variablesIn, aRule, AssociationRuleParty.ANTECEDENT);
                        terms.addAll( collectRuleTerms(variablesIn, aRule, AssociationRuleParty.CONSEQUENT) );
                        RuleExpression expression = factory.createRuleExpressionAND(terms);
                        factory.updateRule(rule, expression, factory.createRuleTerm(variableOut, VarOutput.GENUINE.name()));
                        return rule;
                    })
                    .collect(Collectors.toList());

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
}
