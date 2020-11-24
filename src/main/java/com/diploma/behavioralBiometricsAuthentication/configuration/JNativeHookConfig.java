package com.diploma.behavioralBiometricsAuthentication.configuration;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class JNativeHookConfig {

    public JNativeHookConfig(NativeKeyListener listener) throws NativeHookException {
        GlobalScreen.registerNativeHook();
        GlobalScreen.addNativeKeyListener(listener);
    }

    @Bean
    public Logger logger(){
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        return logger;
    }

}
