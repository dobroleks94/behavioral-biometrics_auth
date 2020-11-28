package com.diploma.behavioralBiometricsAuthentication.factories;

import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;
import ca.pfv.spmf.tools.resultConverter.ResultConverter;
import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationItem;
import com.diploma.behavioralBiometricsAuthentication.entities.associationRule.AssociationRule;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.AssociationRuleParty;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
import org.springframework.stereotype.Component;

@Component
public class AssociationRulesEngineFactory {

    public TransactionDatabaseConverter createTransactionalDBConverter() { return new TransactionDatabaseConverter(); }

    public ResultConverter createResultConverter() { return new ResultConverter(); }

    public AlgoFPGrowth createFrequentPatternMiner() { return new AlgoFPGrowth(); }

    public AlgoAgrawalFaster94 createAssociationRulesGenerator() { return new AlgoAgrawalFaster94(); }

    public AssociationRule createRule() { return new AssociationRule(); }

    public AssociationItem createAssociationItem(AssociationRuleParty party,
                                                 String featureName,
                                                 FuzzyMeasure measure,
                                                 AssociationRule rule) { return new AssociationItem(party, featureName, measure, rule); }
}
