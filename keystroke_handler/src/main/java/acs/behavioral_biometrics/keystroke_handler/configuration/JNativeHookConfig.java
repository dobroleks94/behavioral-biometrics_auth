package acs.behavioral_biometrics.keystroke_handler.configuration;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class JNativeHookConfig {

    public JNativeHookConfig() throws NativeHookException {
        GlobalScreen.registerNativeHook();
    }

    @Bean
    public Logger logger(){
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        return logger;
    }

}
