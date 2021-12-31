package acs.behavioral_biometrics.access_control;

import acs.behavioral_biometrics.app_utils.models.User;
import acs.behavioral_biometrics.app_utils.service.UserService;
import acs.behavioral_biometrics.fuzzy_inference_system.services.FuzzyInferenceService;
import acs.behavioral_biometrics.keystroke_handler.services.KeyProfileSamplesService;
import lombok.Getter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import acs.behavioral_biometrics.user_keystroke_profile.model.FeatureSample;
import acs.behavioral_biometrics.user_keystroke_profile.service.FeatureSampleService;

@Service
public class AuthenticationService {

    private final UserService userService;
    private final KeyProfileSamplesService kpsService;
    private final FeatureSampleService featureSampleService;
    private final FuzzyInferenceService fisService;

    @Getter
    private static User authenticatedUser;
    private static User tempUser;

    public AuthenticationService(UserService userService,
                                 KeyProfileSamplesService kpsService,
                                 FeatureSampleService featureSampleService,
                                 FuzzyInferenceService fisService) {
        this.userService = userService;
        this.kpsService = kpsService;
        this.featureSampleService = featureSampleService;
        this.fisService = fisService;
    }

    public void identifyUser(String username){
        tempUser = userService.findByLogin( username );
    }
    public static void authenticateUser() {
        AuthenticationService.authenticatedUser = tempUser;
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
        return tempUser.getLogin().equals(user.getLogin());
    }


}
