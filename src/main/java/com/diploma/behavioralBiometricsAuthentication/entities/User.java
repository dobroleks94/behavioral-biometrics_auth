package com.diploma.behavioralBiometricsAuthentication.entities;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue
    private Long id;
    private String login;
    private String password;
    private boolean protect; // switch behavioral biometrics protection (true - switch on, false - switch off)


    public User() {};
    public User(String login, String password){
        this.login = login;
        this.password = password;
    }
}
