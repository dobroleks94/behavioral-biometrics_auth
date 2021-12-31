package acs.behavioral_biometrics.access_control;

import acs.behavioral_biometrics.app_utils.models.User;
import acs.behavioral_biometrics.keystroke_handler.listener.KeyboardListener;
import acs.behavioral_biometrics.keystroke_handler.services.KeyProfileSamplesService;
import org.jnativehook.GlobalScreen;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import acs.behavioral_biometrics.info_utils.ArbitraryPhraseExtractor;

import java.io.IOException;

@Service
public class AccessControlService {

    private boolean activeListener;

    private final KeyboardListener listener;
    private final AuthenticationService authService;
    private final ArbitraryPhraseExtractor phraseExtractor;
    private final KeyProfileSamplesService kpsService;

    public AccessControlService(KeyboardListener listener, AuthenticationService authService, ArbitraryPhraseExtractor phraseExtractor, KeyProfileSamplesService kpsService) {
        this.listener = listener;
        this.authService = authService;
        this.phraseExtractor = phraseExtractor;
        this.kpsService = kpsService;
    }

    public User getAuthenticatedUser(){
        return AuthenticationService.getAuthenticatedUser();
    }

    public void logout(){
        authService.logout();
    }

    public boolean doIdentificationStep(String username){
        authService.identifyUser( username );
        return activateListener();
    }
    public boolean doPasswordAuthentication(String password){
        return authService.passwordAuth(password);
    }
    public boolean doBiometricsAuthentication(){
        if (authService.checkOnBiometricsProtection()){
            return authenticate();
        }
        return false;
    }
    public boolean authenticate(){
        User possibleUser = authService.biometricsAuth();
        if (authService.checkUserIdentity(possibleUser)){
            deactivateListener();
            return true;
        }
        return false;
    }
    public void approveAuthenticity(){
        AuthenticationService.authenticateUser();
        deactivateListener();
    }

    public boolean activateListener(){
        if(!this.activeListener) {
            GlobalScreen.addNativeKeyListener(listener);
            this.activeListener = true;
            return true;
        }
        return false;
    }

    public boolean deactivateListener(){
        if(this.activeListener) {
            GlobalScreen.removeNativeKeyListener(listener);
            this.activeListener = false;
            return true;
        }
        return false;
    }

    public String extractArbitraryPhrase(String text) throws IOException {
        String phrase = phraseExtractor.getRandomPhrase();
        if (!phrase.equals(text))
            return phrase;
        return extractArbitraryPhrase(phrase);
    }

    public void clearKeystrokeData(){
        kpsService.clearAllContainers();
    }
    public void processKeystrokeData(){
        kpsService.buildSamples();
    }

}
