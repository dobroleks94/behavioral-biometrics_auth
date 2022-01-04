package acs.behavioral_biometrics.app_utils.configuration;

import acs.behavioral_biometrics.app_utils.models.User;
import acs.behavioral_biometrics.app_utils.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@PropertySource(value = "classpath:application.yml", factory = YamlPropertySourceFactory.class)
public class InitialConfig {

    @Value("${initial-user.login}")
    private String login;
    @Value("${initial-user.password}")
    private String password;

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public User createInitialUser(UserService userService){
        User dobroshtan = userService.createUser(login, password);
        try { dobroshtan = userService.findByLogin(dobroshtan.getLogin()); }
        catch (RuntimeException e){
            System.out.println(e.getMessage());
            System.out.println("Creating new user");
            dobroshtan.setId(1L);
            dobroshtan.setProtectionEnabled(false);
            System.out.println("User created");
        }
        System.out.printf("User with id=%s has been updated%n", userService.saveUser(dobroshtan).getId());
        return dobroshtan;
    }

}
