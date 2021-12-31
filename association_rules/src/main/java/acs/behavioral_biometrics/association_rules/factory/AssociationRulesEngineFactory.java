package acs.behavioral_biometrics.association_rules.factory;

import acs.behavioral_biometrics.association_rules.enums.AssociationRuleParty;
import acs.behavioral_biometrics.association_rules.models.AssociationItem;
import acs.behavioral_biometrics.association_rules.models.AssociationRule;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.tools.dataset_converter.TransactionDatabaseConverter;
import ca.pfv.spmf.tools.resultConverter.ResultConverter;
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
