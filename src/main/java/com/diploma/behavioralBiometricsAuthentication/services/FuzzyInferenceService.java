package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationItem;
import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationRule;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.AssociationRuleParty;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.factories.FuzzyEntitiesFactory;
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
    private final UserService userService;
    private final IOManagerService ioManagerService;

    private Utility utility;

    public FuzzyInferenceService(FuzzyEntitiesFactory factory,
                                 FuzzyMeasureItemService fuzzyMeasureItemService,
                                 FuzzyFeatureSampleService fuzzyFeatureSampleService,
                                 IOManagerService ioManagerService,
                                 UserService userService) {
        this.factory = factory;
        this.fuzzyMeasureItemService = fuzzyMeasureItemService;
        this.fuzzyFeatureSampleService = fuzzyFeatureSampleService;
        this.ioManagerService = ioManagerService;
        this.userService = userService;
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

        Map<String, MembershipFunction> membershipFunctionMap = utility.getMembershipFunctions
                (defuzzifyTermStep, MembershipFunctionTriangular.class);  // Membership function ranges for output decision

        List<LinguisticTerm> outputTerms = membershipFunctionMap.entrySet()
                .stream()
                .map(item -> factory.createTerm(item.getKey(), item.getValue()))
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
    public String authentication(FeatureSample inputSample) throws RuntimeException{
        FIS fis = ioManagerService.loadFIS();

        fis.setVariable("typingSpeed", inputSample.getTypingSpeed());
        fis.setVariable("numPadUsageFrequency", inputSample.getNumPadUsageFrequency());
        fis.setVariable("mistakesFrequency", inputSample.getMistakesFrequency());
        fis.setVariable("meanTriGraphKUTime", inputSample.getMeanTrigraphKUTime());
        fis.setVariable("meanTriGraphKDTime", inputSample.getMeanTrigraphKDTime());
        fis.setVariable("meanFlightTime", inputSample.getMeanFlightTime());
        fis.setVariable("meanDwellTime", inputSample.getMeanDwellTime());
        fis.setVariable("meanDiGraphKUTime", inputSample.getMeanDigraphKUTime());
        fis.setVariable("meanDiGraphKDTime", inputSample.getMeanDigraphKDTime());
        fis.setVariable("meanDelBackspDwell", inputSample.getMeanDelBackspDwell());

        fis.evaluate();

        Variable userVerdict = fis.getVariable("user");

        return userVerdict.getLinguisticTerms().values().stream()
                                            .filter(lingTerm -> userVerdict.getValue() > lingTerm.getMembershipFunction().getParameter(0) &&
                                                                userVerdict.getValue() < lingTerm.getMembershipFunction().getParameter(2))
                                            .map(LinguisticTerm::getTermName)
                                            .findFirst().orElseThrow(() -> new RuntimeException("Intruder detected!"));
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
            double min = Integer.MIN_VALUE;
            double mid = Integer.MIN_VALUE;
            double max = Integer.MIN_VALUE;
            switch (measure) {
                case VERY_LOW -> {
                    min = fuzzyMeasures.get(0).getCrispDescriptor() - fuzzyMeasures.get(6).getCrispDescriptor();
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
                    max = fuzzyMeasures.get(6).getCrispDescriptor() * 2;
                }
            }
            return new Value[]{
                    new Value(min),
                    new Value(mid),
                    new Value(max)
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
        private Map<String, MembershipFunction> getMembershipFunctions(int defuzzifyTermStep, Class<? extends MembershipFunction> membershipFunction) {
            Map<String, MembershipFunction> result = new HashMap<>();
            double currentPoint = 0.0;
            List<User> users = userService.findAll();
            if (MembershipFunctionTriangular.class.equals(membershipFunction)) {
                for(var user : users){
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
                        Rule rule = factory.createRule(aRule.getId().toString(), ruleBlock);
                        List<RuleTerm> terms = collectRuleTerms(variablesIn, aRule, AssociationRuleParty.ANTECEDENT);
                        terms.addAll( collectRuleTerms(variablesIn, aRule, AssociationRuleParty.CONSEQUENT) );
                        RuleExpression expression = factory.createRuleExpressionAND(terms);
                        factory.updateRule(rule, expression, factory.createRuleTerm(variableOut, userService.findById(aRule.getUserId()).getLogin()));
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
