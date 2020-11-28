package com.diploma.behavioralBiometricsAuthentication.services;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.gui.PreferencesManager;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;
import ca.pfv.spmf.tools.resultConverter.ResultConverter;
import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationItem;
import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationRule;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.AssociationRuleParty;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.factories.AssociationRulesEngineFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AssociationRulesService {

    private static final int MINIMUM_PATTERN_LENGTH = 5;
    private static final int MAXIMUM_PATTERN_LENGTH = Integer.MAX_VALUE;

    private static final double MINIMUM_SUPPORT = 0.5;
    private static final double MINIMUM_CONFIDENCE = 1;

    private final AssociationRulesEngineFactory associationRulesEngine;
    private final IOManagerService ioManagerService;
    private Utility utility;


    public AssociationRulesService(AssociationRulesEngineFactory associationRulesEngine, IOManagerService ioManagerService) {
        this.associationRulesEngine = associationRulesEngine;
        this.ioManagerService = ioManagerService;
    }

    @PostConstruct
    private void initializeVariables(){
        this.utility = new Utility();
    }

    public List<AssociationRule> getAssociationRules(List<FuzzyFeatureSample> fuzzyFeatureSamples){

        try {

            Map<Integer, String> itemsRepresentation = doDataPreProcess(fuzzyFeatureSamples);
            Itemsets frequentPatterns = getFrequentItemsets();
            generateAssociationRules(frequentPatterns);

            return doDataPostProcess(itemsRepresentation);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<Integer, String> doDataPreProcess(List<FuzzyFeatureSample> fuzzyFeatureSamples) throws IOException {

        try { ioManagerService.fillFile(fuzzyFeatureSamples); }
        catch (IOException e) { e.printStackTrace(); }

        TransactionDatabaseConverter converter = associationRulesEngine.createTransactionalDBConverter();

        return converter.convertARFFandReturnMap(
                IOManagerService.getTempInputArff(),
                IOManagerService.getTempInput(),
                Integer.MAX_VALUE
        );
    }
    public List<AssociationRule> doDataPostProcess(Map<Integer, String> dataPreProcess) throws IOException {

        Charset charset = PreferencesManager.getInstance().getPreferedCharset();
        ResultConverter converter = associationRulesEngine.createResultConverter();

        converter.convert(dataPreProcess, IOManagerService.getTempOutput(), IOManagerService.getTempOutputArff(), charset);

        List<AssociationRule> rulesEntities = parseOutput();
        ioManagerService.deleteAllFiles();

        return rulesEntities;

    }
    public Itemsets getFrequentItemsets() throws IOException {

        AlgoFPGrowth fpGrowth = associationRulesEngine.createFrequentPatternMiner();

        fpGrowth.setMinimumPatternLength(MINIMUM_PATTERN_LENGTH);
        fpGrowth.setMaximumPatternLength(MAXIMUM_PATTERN_LENGTH);
        Itemsets itemsets = fpGrowth.runAlgorithm(IOManagerService.getTempInput(), null, MINIMUM_SUPPORT);
        utility.setDBSize(fpGrowth.getDatabaseSize());
        return itemsets;
    }

    public void generateAssociationRules(Itemsets itemsets) throws IOException {

        AlgoAgrawalFaster94 algoAgrawal = associationRulesEngine.createAssociationRulesGenerator();

//        algoAgrawal.setMaxAntecedentLength(FuzzyFeatureSample.getMapKeys().size());
//        algoAgrawal.setMaxConsequentLength(FuzzyFeatureSample.getMapKeys().size());
        algoAgrawal.runAlgorithm(itemsets, IOManagerService.getTempOutput(), utility.getDBSize(), MINIMUM_CONFIDENCE);
    }

    public List<AssociationRule> parseOutput() throws IOException {
        return Files.lines(Paths.get(".", IOManagerService.getTempOutputArff()))
                .map(utility::splitForVitalParts)
                .map(item -> utility.processPartitions(item, associationRulesEngine.createRule()))
                .collect(Collectors.toList());
    }





    private class Utility{

        private int dbSize;

        private int getDBSize() {
            return dbSize;
        }
        private void setDBSize(int dbSize) {
            this.dbSize = dbSize;
        }

        private Map<String, String> splitForVitalParts(String stringRule) {

            Pattern pattern = Pattern.compile("(([A-Za-z]+=[А-Я]+\\s)+)(==>\\s)(([A-Za-z]+=[А-Я]+\\s)+)(#SUP:\\s\\d+)(\\s)(#CONF:\\s\\d+\\.\\d+)");
            Matcher matcher = pattern.matcher(stringRule);

            if(matcher.find())
                return new HashMap<>(){
                    {  put(AssociationRuleParty.ANTECEDENT.name(), matcher.group(1)); }
                    {  put(AssociationRuleParty.CONSEQUENT.name(), matcher.group(4)); }
                    {  put("support",    matcher.group(6)); }
                    {  put("confidence", matcher.group(8)); }
                };
            else throw new RuntimeException("Impossible to process association rule :(");
        }

        private AssociationRule processPartitions(Map<String, String> parts, AssociationRule rule) {
            List<AssociationItem> antecedents = processParty(AssociationRuleParty.ANTECEDENT, parts, rule);
            List<AssociationItem> consequents = processParty(AssociationRuleParty.CONSEQUENT, parts, rule);
            int support = (int) getMeasure("support", parts);
            double confidence = (double) getMeasure("confidence", parts);

            rule.setAntecedent(antecedents);
            rule.setConsequent(consequents);
            rule.setSupport(support);
            rule.setConfidence(confidence);

            return rule;
        }

        private Number getMeasure(String measure, Map<String, String> parts) {
            switch (measure){
                case "support":
                    return Integer.parseInt(parts.get("support").split(":")[1].trim());
                case "confidence":
                    return Double.parseDouble(parts.get("confidence").split(":")[1].trim());
                default:
                    throw new RuntimeException("Bad measure specified!");
            }
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

}
