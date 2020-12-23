package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User findByLogin(String login) { return userRepository.findByLogin(login)
            .orElseThrow(() -> new RuntimeException("User with login " + login + " has been not found :("));}
    public User findById(Long id) { return userRepository.findById(id)
                                                                    .orElseThrow(() -> new RuntimeException("User with id " + id + " has been not found :(")); }
    public User saveUser(User user) { return userRepository.save(user); }
    public User createUser(String login, String password){ return new User(login, passwordEncoder.encode(password)); }
    public List<User> findAll() { return userRepository.findAll(); }

    public boolean comparePassword(User user, String password){
        return passwordEncoder.matches(password, user.getPassword());
    }

}
