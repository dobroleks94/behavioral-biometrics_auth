package com.diploma.behavioralBiometricsAuthentication.configurations;

import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class InitialConfig{

    private UserService userService;
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

   /* @Bean
    public void createInitialUser(){
        User dobroshtan = userService.createUser("oleksii_dobroshtan", "dobroshtan_Password94");
        dobroshtan.setId(1L);
        dobroshtan.setProtect(false);
        System.out.println("User created");
        System.out.println("User saved with id: " + userService.saveUser(dobroshtan).getId());
    }*/

    /*@Bean
    public void resetProtection(){
        User dobroshtan = userService.findByLogin("oleksii_dobroshtan");
        dobroshtan.setProtect(false);
        userService.saveUser(dobroshtan);
    }*/


}
