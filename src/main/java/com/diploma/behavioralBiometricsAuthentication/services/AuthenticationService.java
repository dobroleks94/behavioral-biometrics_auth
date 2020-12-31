package com.diploma.behavioralBiometricsAuthentication.services;

import com.diploma.behavioralBiometricsAuthentication.entities.User;
import com.diploma.behavioralBiometricsAuthentication.entities.featureSamples.FeatureSample;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserService userService;
    private final KeyProfileSamplesService kpsService;
    private final FeatureSampleService featureSampleService;
    private final FuzzyInferenceService fisService;

    private static User authenticatedUser;
    private static User tempUser;

    public static User getAuthenticatedUser() {
        return authenticatedUser;
    }
    public static void authenticateUser() {
        AuthenticationService.authenticatedUser = tempUser;
    }

    public AuthenticationService(UserService userService,
                                 KeyProfileSamplesService kpsService,
                                 FeatureSampleService featureSampleService,
                                 FuzzyInferenceService fisService) {
        this.userService = userService;
        this.kpsService = kpsService;
        this.featureSampleService = featureSampleService;
        this.fisService = fisService;
    }

    public User identifyUser(User user){
        tempUser = user;
        return tempUser;
    }
    public boolean passwordAuth(String password) {
        return userService.comparePassword(tempUser, password);
    }
    public User biometricsAuth() throws RuntimeException {
        kpsService.buildSamples();
        FeatureSample featureSample = featureSampleService.buildFeatureSample();
        featureSample.setUserId(tempUser.getId());

        return userService.findByLogin( fisService.authentication(featureSample) );
    }

    public void logout(){
        AuthenticationService.authenticatedUser = null;
    }

    public boolean checkOnBiometricsProtection() {
        return tempUser.isProtect();
    }
    public boolean checkUserIdentity(User user) {
        return !tempUser.getLogin().equals(user.getLogin());
    }


}
