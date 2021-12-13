package com.diploma.behavioralBiometricsAuthentication.entities.associationRule;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.AssociationRuleParty;
import com.diploma.behavioralBiometricsAuthentication.entities.enums.FuzzyMeasure;
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
    @GeneratedValue
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
        return String.format("%s IS %s", featureName, measure.getEngRepres());
    }
}
