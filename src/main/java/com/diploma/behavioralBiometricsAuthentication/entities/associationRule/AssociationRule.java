package com.diploma.behavioralBiometricsAuthentication.entities.associationRule;

import com.diploma.behavioralBiometricsAuthentication.entities.enums.AssociationRuleParty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class AssociationRule {

    @Id
    @GeneratedValue
    private Long id;
    @OneToMany(cascade = CascadeType.ALL)
    private List<AssociationItem> associationItems;
    private int support;
    private double confidence;
    private long userId;


    @Override
    public String toString() {
        return AssociationRuleBuilder.builder()
                .conditionWord()
                .grabAssociationItems(associationItems, AssociationRuleParty.ANTECEDENT)
                .consequentWord()
                .grabAssociationItems(associationItems, AssociationRuleParty.CONSEQUENT)
                .toString();
    }
}
