package acs.behavioral_biometrics.association_rules.services;

import acs.behavioral_biometrics.association_rules.enums.AssociationRuleParty;
import acs.behavioral_biometrics.association_rules.factory.AssociationRulesEngineFactory;
import acs.behavioral_biometrics.association_rules.models.AssociationItem;
import acs.behavioral_biometrics.association_rules.models.AssociationRule;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Association Rules computational processor
 */
@Service
@Getter
@Setter
public class ARComputeProcessor {

    private int dbSize;
    private final AssociationRulesEngineFactory associationRulesEngine;

    public ARComputeProcessor(AssociationRulesEngineFactory associationRulesEngine) {
        this.associationRulesEngine = associationRulesEngine;
    }

    public Map<String, String> splitForVitalParts(String stringRule) {

        Pattern pattern = Pattern.compile(
                "(?<antecedent>([A-Za-z]+=[А-Я]+\\s)+)(==>\\s)(?<consequent>([A-Za-z]+=[А-Я]+\\s)+)(?<support>#SUP:\\s\\d+)(\\s)(?<confidence>#CONF:\\s\\d+\\.\\d+)"
        );
        Matcher matcher = pattern.matcher(stringRule);

        if(matcher.find())
            return new HashMap<>(){
                {  put(AssociationRuleParty.ANTECEDENT.name(), matcher.group("antecedent")); }
                {  put(AssociationRuleParty.CONSEQUENT.name(), matcher.group("consequent")); }
                {  put("support",    matcher.group("support")); }
                {  put("confidence", matcher.group("confidence")); }
            };
        else throw new RuntimeException("Impossible to process association rule :(");
    }

    public AssociationRule processPartitions(Map<String, String> parts, AssociationRule rule) {
        List<AssociationItem> antecedents = processParty(AssociationRuleParty.ANTECEDENT, parts, rule);
        List<AssociationItem> consequents = processParty(AssociationRuleParty.CONSEQUENT, parts, rule);

        int support = (int) getMeasure("support", parts);
        double confidence = (double) getMeasure("confidence", parts);

        return updateRule(rule, antecedents, consequents, support, confidence);
    }

    private AssociationRule updateRule(AssociationRule rule, List<AssociationItem> antecedents, List<AssociationItem> consequents, int support, double confidence) {
        rule.setAssociationItems(Stream.of(antecedents, consequents).flatMap(Collection::stream).collect(Collectors.toList()));
        rule.setSupport(support);
        rule.setConfidence(confidence);
        return rule;
    }

    private Number getMeasure(String measure, Map<String, String> parts) {
        return switch (measure) {
            case "support" -> Integer.parseInt(parts.get("support").split(":")[1].trim());
            case "confidence" -> Double.parseDouble(parts.get("confidence").split(":")[1].trim());
            default -> throw new RuntimeException("Bad measure specified!");
        };
    }

    private List<AssociationItem> processParty(AssociationRuleParty party, Map<String, String> parts, AssociationRule rule){
        return  Arrays.stream(parts.get(party.name()).split("\\s+"))
                .map(item -> associationRulesEngine.createAssociationItem(
                        party,
                        item.split("=")[0],
                        FuzzyMeasure.getByShortRepres(item.split("=")[1]),
                        rule
                ))
                .collect(Collectors.toList());
    }
}
