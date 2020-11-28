package com.diploma.behavioralBiometricsAuthentication.services;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.gui.PreferencesManager;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;
import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;
import ca.pfv.spmf.tools.resultConverter.ResultConverter;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FuzzyFeatureSample;
import com.diploma.behavioralBiometricsAuthentication.factories.AssociationRulesEngineFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

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

    public boolean getAssociationRules(List<FuzzyFeatureSample> fuzzyFeatureSamples){

        try {

            Map<Integer, String> itemsRepresentation = doDataPreProcess(fuzzyFeatureSamples);
            Itemsets frequentPatterns = getFrequentItemsets();
            generateAssociationRules(frequentPatterns);
            doDataPostProcess(itemsRepresentation);
            //TODO: generate AssociationRules Entities to return
            ioManagerService.deleteTemporaryFiles();
            //ioManagerService.deleteAllFiles();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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
    public Path doDataPostProcess(Map<Integer, String> dataPreProcess) throws IOException {

        Charset charset = PreferencesManager.getInstance().getPreferedCharset();
        ResultConverter converter = associationRulesEngine.createResultConverter();

        converter.convert(dataPreProcess, IOManagerService.getTempOutput(), IOManagerService.getTempOutputArff(), charset);

        return Paths.get(".", IOManagerService.getTempOutputArff());

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





    private class Utility{

        private int dbSize;

        private int getDBSize() {
            return dbSize;
        }
        private void setDBSize(int dbSize) {
            this.dbSize = dbSize;
        }
    }

}
