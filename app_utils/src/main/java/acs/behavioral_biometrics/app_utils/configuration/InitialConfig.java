package acs.behavioral_biometrics.app_utils.configuration;

import acs.behavioral_biometrics.app_utils.models.User;
import acs.behavioral_biometrics.app_utils.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class InitialConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public User createInitialUser(UserService userService){
        User dobroshtan = userService.createUser("oleksii_dobroshtan", "dobroshtan_Password94");
        dobroshtan.setId(1L);
        dobroshtan.setProtect(false);
        System.out.println("User created");
        System.out.println("User saved with id: " + userService.saveUser(dobroshtan).getId());
        return dobroshtan;
    }

}
