package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User findById(Long id) { return userRepository.findById(id)
                                                                    .orElseThrow(() -> new RuntimeException("User with id " + id + " has been not found :(")); }
    public User saveUser(User user) { return userRepository.save(user); }
    public User createUser(String login, String password){ return new User(login, passwordEncoder.encode(password)); }
    public List<User> findAll() { return userRepository.findAll(); }

    @Bean
    private void createInitial(){
        User dobroshtan = createUser("dobroshtan94", "dobroshtan_Password94");
        dobroshtan.setId(1L);
        System.out.println("User created");
        System.out.println("User saved with id: " + saveUser(dobroshtan).getId());
    }

}
