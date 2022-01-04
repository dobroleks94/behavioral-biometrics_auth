package acs.behavioral_biometrics.association_rules.models;

import acs.behavioral_biometrics.association_rules.enums.AssociationRuleParty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class AssociationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    public List<AssociationItem> getAntecedent() {
        return this.associationItems
                .stream()
                .filter(item -> item.getParty().equals(AssociationRuleParty.ANTECEDENT))
                .collect(Collectors.toList());
    }

    public List<AssociationItem> getConsequent() {
        return this.associationItems
                .stream()
                .filter(item -> item.getParty().equals(AssociationRuleParty.CONSEQUENT))
                .collect(Collectors.toList());
    }


}
