package acs.behavioral_biometrics.association_rules.services;

import acs.behavioral_biometrics.app_utils.configuration.YamlPropertySourceFactory;
import acs.behavioral_biometrics.app_utils.models.SystemLogger;
import acs.behavioral_biometrics.app_utils.models.User;
import acs.behavioral_biometrics.association_rules.factory.AssociationRulesEngineFactory;
import acs.behavioral_biometrics.association_rules.models.AssociationRule;
import acs.behavioral_biometrics.association_rules.repository.AssociationRuleRepository;
import acs.behavioral_biometrics.fuzzy_profile_mapper.model.FuzzyFeatureSample;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.gui.PreferencesManager;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;
import ca.pfv.spmf.tools.resultConverter.ResultConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@PropertySource(value = "classpath:association_rules-config.yml", factory = YamlPropertySourceFactory.class)
public class AssociationRulesService {

//    private static final int MINIMUM_PATTERN_LENGTH = 7;
//    private static final int MAXIMUM_PATTERN_LENGTH = Integer.MAX_VALUE;
//
//    private static final double MINIMUM_SUPPORT = 0.15;
//    private static final double MINIMUM_CONFIDENCE = 1;
    @Value("${association-rules.support}")
    private double minSupport;
    @Value("${association-rules.confidence}")
    private double minConfidence;
    @Value("${association-rules.min-pattern-length}")
    private int minPatternLength;


    private final AssociationRuleRepository associationRuleRepository;
    private final AssociationRulesEngineFactory associationRulesEngine;
    private final RulesIOManagerService rulesIoManagerService;
    private final SystemLogger logger;
    private final ARComputeProcessor arComputeProcessor;


    public AssociationRulesService(AssociationRuleRepository associationRuleRepository,
                                   AssociationRulesEngineFactory associationRulesEngine,
                                   RulesIOManagerService rulesIoManagerService,
                                   SystemLogger logger,
                                   ARComputeProcessor arComputeProcessor) {
        this.associationRuleRepository = associationRuleRepository;
        this.associationRulesEngine = associationRulesEngine;
        this.rulesIoManagerService = rulesIoManagerService;
        this.logger = logger;
        this.arComputeProcessor = arComputeProcessor;
    }

    public long getCount() { return associationRuleRepository.count(); }
    public List<AssociationRule> saveAll(List<AssociationRule> associationRules){ return associationRuleRepository.saveAll(associationRules); }
    public void deleteAll() { associationRuleRepository.deleteAll(); }
    public List<AssociationRule> assignOwner(User user, List<AssociationRule> rules) {
        rules.forEach(rule -> rule.setUserId(user.getId()));
        return rules;
    }
    @Transactional
    public void deleteUserRules(long userId){
        associationRuleRepository.deleteAllByUserId(userId);
    }
    public List<AssociationRule> getAssociationRules(List<FuzzyFeatureSample> fuzzyFeatureSamples){
        try {
            Map<Integer, String> itemsRepresentation = doDataPreProcess(fuzzyFeatureSamples);
            logger.log(SystemLogger.PRE_PROCESS_RESULT);
            Itemsets frequentPatterns = getFrequentItemsets();
            logger.log(SystemLogger.FREQUENT_PATTERNS_RESULT);
            generateAssociationRules(frequentPatterns);
            logger.log(SystemLogger.ASSOCIATION_RULES_DATA_RESULT);

            return doDataPostProcess(itemsRepresentation);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<Integer, String> doDataPreProcess(List<FuzzyFeatureSample> fuzzyFeatureSamples) throws IOException {
        try { rulesIoManagerService.fillFile(fuzzyFeatureSamples); }
        catch (IOException e) { e.printStackTrace(); }

        TransactionDatabaseConverter converter = associationRulesEngine.createTransactionalDBConverter();
        return converter.convertARFFandReturnMap(
                rulesIoManagerService.getTempInputArff(),
                rulesIoManagerService.getTempInput(),
                Integer.MAX_VALUE
        );
    }
    public List<AssociationRule> doDataPostProcess(Map<Integer, String> dataPreProcess) throws IOException {

        Charset charset = PreferencesManager.getInstance().getPreferedCharset();
        ResultConverter converter = associationRulesEngine.createResultConverter();

        converter.convert(dataPreProcess, rulesIoManagerService.getTempOutput(), rulesIoManagerService.getTempOutputArff(), charset);

        List<AssociationRule> rulesEntities = parseOutput();
        logger.log(SystemLogger.POST_PROCESS_RESULT);
        rulesIoManagerService.deleteAllFiles();
        logger.log(SystemLogger.DELETING_RESULT);

        return rulesEntities;

    }
    public Itemsets getFrequentItemsets() throws IOException {
        AlgoFPGrowth fpGrowth = associationRulesEngine.createFrequentPatternMiner();
        fpGrowth.setMinimumPatternLength(minPatternLength);
        fpGrowth.setMaximumPatternLength(Integer.MAX_VALUE);

        Itemsets itemsets = fpGrowth.runAlgorithm(rulesIoManagerService.getTempInput(), null, minSupport);
        arComputeProcessor.setDbSize(fpGrowth.getDatabaseSize());

        return itemsets;
    }

    public void generateAssociationRules(Itemsets itemsets) throws IOException {
        AlgoAgrawalFaster94 algoAgrawal = associationRulesEngine.createAssociationRulesGenerator();
        algoAgrawal.runAlgorithm(itemsets, rulesIoManagerService.getTempOutput(), arComputeProcessor.getDbSize(), minConfidence);
    }

    public List<AssociationRule> parseOutput() throws IOException {
        return Files.lines(Paths.get(".", rulesIoManagerService.getTempOutputArff()))
                .map(arComputeProcessor::splitForVitalParts)
                .map(item -> arComputeProcessor.processPartitions(item, associationRulesEngine.createRule()))
                .collect(Collectors.toList());
    }

}
