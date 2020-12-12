package com.diploma.behavioralBiometricsAuthentication.entities.associationRule;

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
    private List<AssociationItem> antecedent;
    @OneToMany(cascade = CascadeType.ALL)
    private List<AssociationItem> consequent;
    private int support;
    private double confidence;

    private String getStringRule(List<AssociationItem> items){
        String itemRepres = items.stream()
                .map(AssociationItem::toString)
                .reduce("", (prevRes, item) -> prevRes + " AND " + item);
        String pattern = " AND ";
        return (itemRepres.startsWith(pattern))
                ? itemRepres.substring(pattern.length())
                : itemRepres;
    }

    @Override
    public String toString() {

        return new StringBuilder("IF ")
                .append(getStringRule(antecedent))
                .append( " THEN " )
                .append(getStringRule(consequent))
                .toString();
    }
}
