package acs.behavioral_biometrics.fuzzy_inference_system.services;

import acs.behavioral_biometrics.app_utils.configuration.YamlPropertySourceFactory;
import acs.behavioral_biometrics.association_rules.models.AssociationRule;
import acs.behavioral_biometrics.fuzzy_inference_system.factory.FuzzyEntitiesFactory;
import acs.behavioral_biometrics.fuzzy_inference_system.utils.FISComputeUtils;
import acs.behavioral_biometrics.user_keystroke_profile.enums.Feature;
import acs.behavioral_biometrics.user_keystroke_profile.model.FeatureSample;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunction;
import net.sourceforge.jFuzzyLogic.membership.MembershipFunctionTriangular;
import net.sourceforge.jFuzzyLogic.rule.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@PropertySource(value = "classpath:fis-config.yml", factory = YamlPropertySourceFactory.class)
public class FuzzyInferenceService {


    @Value("${fcl.function-block}")
    private String functionBlockName;
    @Value("${fcl.rule-block}")
    private String ruleBlockName;
    @Value("${fcl.variables.output}")
    private String outputVarName;

    private final FuzzyEntitiesFactory factory;
    private final FuzzyIOManagerService fuzzyIoManagerService;
    private final FISComputeUtils fisUtils;

    public FuzzyInferenceService(FuzzyEntitiesFactory factory, FuzzyIOManagerService fuzzyIoManagerService, FISComputeUtils fisUtils) {
        this.factory = factory;
        this.fuzzyIoManagerService = fuzzyIoManagerService;
        this.fisUtils = fisUtils;
    }

    public FIS createNewFIS(int defuzzifyTermStep, List<AssociationRule> associationRules){
        FIS fis = factory.createPlainFIS();
        FunctionBlock functionBlock = factory.createFunctionBlock(functionBlockName, fis);

        List<Variable> input = factory.createInputVariables();
        Variable output = factory.createVariable(outputVarName);

        fisUtils.updateFunctionBlock(functionBlock, new ArrayList<>(input) {{ add(output); }});  // filling the variables block

        Map<String, MembershipFunction> membershipFunctionMap =
                fisUtils.getMembershipFunctions(defuzzifyTermStep, MembershipFunctionTriangular.class);  // Membership function ranges for output decision

        fisUtils.fillItemsWithFuzzyTerms(input); // creating TERMS for each FUZZIFY item
        List<LinguisticTerm> outputTerms = fisUtils.defineOutputLinguisticTerms(membershipFunctionMap);  // output TERMS for decision (Genuine or Intruder user)

        fisUtils.updateVariable(output, outputTerms);  // adding terms to respective variables
        output.setDefuzzifier(factory.createDefuzzifierCOG(output)); // Defuzzifier definition and assigning to output variable
        RuleBlock ruleBlock = factory.createRuleBlock(ruleBlockName, functionBlock); // creating rule block which contains all rules
        List<Rule> rules = fisUtils.collectRules(associationRules, input, output, ruleBlock); // generating rules from Association Rules

        functionBlock.setRuleBlocks(new HashMap<>() {{ put(ruleBlock.getName(), ruleBlock); }});  //including Rule Block to Function Block

        rules.forEach(ruleBlock::add); // including rules to Rule Block

        return fis;
    }


    public String authentication(FeatureSample inputSample) throws RuntimeException{
        FIS fis = fuzzyIoManagerService.loadFIS();

        fis.setVariable(Feature.TYPING_SPEED.getFeatureName(), inputSample.getTypingSpeed());
        fis.setVariable(Feature.NUMPAD_USAGE_FREQUENCY.getFeatureName(), inputSample.getNumPadUsageFrequency());
        fis.setVariable(Feature.MISTAKES_FREQUENCY.getFeatureName(), inputSample.getMistakesFrequency());
        fis.setVariable(Feature.MEAN_TRIGRAPH_KU_TIME.getFeatureName(), inputSample.getMeanTrigraphKUTime());
        fis.setVariable(Feature.MEAN_TRIGRAPH_KD_TIME.getFeatureName(), inputSample.getMeanTrigraphKDTime());
        fis.setVariable(Feature.MEAN_FLIGHT_TIME.getFeatureName(), inputSample.getMeanFlightTime());
        fis.setVariable(Feature.MEAN_DWELL_TIME.getFeatureName(), inputSample.getMeanDwellTime());
        fis.setVariable(Feature.MEAN_DIGRAPH_KU_TIME.getFeatureName(), inputSample.getMeanDigraphKUTime());
        fis.setVariable(Feature.MEAN_DIGRAPH_KD_TIME.getFeatureName(), inputSample.getMeanDigraphKDTime());
        fis.setVariable(Feature.MEAN_DEL_BACKSP_DWELL.getFeatureName(), inputSample.getMeanDelBackspDwell());

        fis.evaluate();

        return inference( fis.getVariable(outputVarName) );
    }

    private String inference(Variable userVerdict) {
        return userVerdict.getLinguisticTerms().values().stream()
                .filter(lingTerm -> userVerdict.getValue() > lingTerm.getMembershipFunction().getParameter(0) &&
                        userVerdict.getValue() < lingTerm.getMembershipFunction().getParameter(2))
                .map(LinguisticTerm::getTermName)
                .findFirst().orElseThrow(() -> new RuntimeException("Intruder detected!"));
    }
}
