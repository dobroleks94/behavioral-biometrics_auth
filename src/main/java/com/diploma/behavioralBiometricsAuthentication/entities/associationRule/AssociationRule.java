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


}
