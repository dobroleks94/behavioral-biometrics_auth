package com.diploma.behavioralBiometricsAuthentication.factories;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.FeatureName;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyMeasureItem;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.FuzzyValue;
import com.diploma.behavioralBiometricsAuthentication.entities.fuzzification.VarOutput;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.defuzzifier.Defuzzifier;
import net.sourceforge.jFuzzyLogic.defuzzifier.DefuzzifierCenterOfGravity;
import net.sourceforge.jFuzzyLogic.fcl.FclObject;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTriangular;
import net.sourceforge.jFuzzyLogic.membership.Value;
import net.sourceforge.jFuzzyLogic.rule.*;
import net.sourceforge.jFuzzyLogic.ruleAccumulationMethod.RuleAccumulationMethodMax;
import net.sourceforge.jFuzzyLogic.ruleActivationMethod.RuleActivationMethodMin;
import net.sourceforge.jFuzzyLogic.ruleConnectionMethod.RuleConnectionMethodAndMin;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class FuzzyEntitiesFactory {

    public FuzzyValue createFuzzyValue(FuzzyMeasure measure, Double membershipRate){
        return new FuzzyValue(measure, membershipRate);
    }

    public FuzzyMeasureItem createFuzzyMeasureItem(FeatureName featureName, FuzzyMeasure fuzzyMeasure, double crispDescriptor) {
        return new FuzzyMeasureItem(featureName, fuzzyMeasure, crispDescriptor);
    }

    public FIS createPlainFIS(){
        return new FIS();
    }
    public FunctionBlock createFunctionBlock(String name, FIS fis){
        FunctionBlock functionBlock = new FunctionBlock(fis);
        fis.addFunctionBlock(name, functionBlock);
        return functionBlock;
    }
    public void updateFunctionBlock(FunctionBlock fb, List<Variable> variables){
        for(var variable : variables)
            fb.setVariable(variable.getName(), variable);
    }


    public Variable createVariable(String name){
        return new Variable(name);
    }
    public MembershipFunctionTriangular createTriangularMF(Value[] triangularValues){
        return new MembershipFunctionTriangular(triangularValues[0], triangularValues[1], triangularValues[2]);
    }
    public LinguisticTerm createTerm(String name, MembershipFunction membershipFunction){
        return new LinguisticTerm(name, membershipFunction);
    }
    public void updateVariable(Variable variable, List<LinguisticTerm> terms){
        for(var term : terms)
            variable.add(term);
    }


    public RuleBlock createRuleBlock(String name, FunctionBlock functionBlock){
        RuleBlock ruleBlock = new RuleBlock(functionBlock);
        ruleBlock.setName(name);
        ruleBlock.setRuleAccumulationMethod(new RuleAccumulationMethodMax());
        ruleBlock.setRuleActivationMethod(new RuleActivationMethodMin());
        return ruleBlock;
    }
    public Rule createRule(String name, RuleBlock ruleBlock){
        return new Rule(name, ruleBlock);
    }
    public RuleTerm createRuleTerm(Variable variable, String term){
        return new RuleTerm(variable, term, false);
    }
    public RuleExpression createRuleExpressionAND(List<RuleTerm> terms){
        if (terms.size() == 2)
            return new RuleExpression(terms.get(0), terms.get(1), RuleConnectionMethodAndMin.get());

        FclObject term1 = terms.get(0);
        terms.remove(0);
        FclObject term2 = createRuleExpressionAND(terms);

        return new RuleExpression(term1, term2, RuleConnectionMethodAndMin.get());
    }
    public void updateRule(Rule rule, RuleExpression antecedent, RuleTerm consequent){
        rule.setAntecedents(antecedent);
        rule.addConsequent(consequent.getVariable(), consequent.getTermName(), false);
    }

    public Defuzzifier createDefuzzifierCOG(Variable variable) {
        return new DefuzzifierCenterOfGravity(variable);
    }
}
