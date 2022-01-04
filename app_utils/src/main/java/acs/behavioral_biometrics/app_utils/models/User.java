package acs.behavioral_biometrics.app_utils.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue
    private Long id;
    private String login;
    private String password;
    @Type(type = "yes_no")
    private boolean protectionEnabled; // switch behavioral biometrics protection (true - switch on, false - switch off)


    public User() {};
    public User(String login, String password){
        this.login = login;
        this.password = password;
    }
}
