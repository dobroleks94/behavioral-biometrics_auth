package acs.behavioral_biometrics.association_rules.models;

import acs.behavioral_biometrics.association_rules.enums.AssociationRuleParty;
import acs.behavioral_biometrics.association_rules.enums.RuleConstructors;
import acs.behavioral_biometrics.fuzzy_profile_mapper.enums.FuzzyMeasure;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AssociationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Enumerated(EnumType.STRING)
    private AssociationRuleParty party;
    private String featureName;
    @Enumerated(EnumType.STRING)
    private FuzzyMeasure measure;
    @ManyToOne
    private AssociationRule parent;

    public AssociationItem(AssociationRuleParty party, String featureName, FuzzyMeasure measure, AssociationRule parent) {
        this.party = party;
        this.featureName = featureName;
        this.measure = measure;
        this.parent = parent;
    }


    @Override
    public String toString() {
        return String.format("%s %s %s", featureName, RuleConstructors.IS, measure.getEngRepres());
    }
}
