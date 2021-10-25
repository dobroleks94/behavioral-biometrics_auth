package com.diploma.behavioralBiometricsAuthentication.listeners;

import com.diploma.behavioralBiometricsAuthentication.services.*;
import lombok.AllArgsConstructor;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.springframework.stereotype.Component;



@Component
@AllArgsConstructor
public class KeyboardListener implements NativeKeyListener {

    private final KeyProfileHandlerService keyProfileHandlerService;
    private final KeyProfileSamplesService kpsService;


    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_ALT_R)
            triggerTrick();
        keyProfileHandlerService.processPressing(NativeKeyEvent.getKeyText(e.getKeyCode()), e.getKeyCode(), e.getWhen());
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        kpsService.addTemporary( keyProfileHandlerService.processReleasing(e.getKeyCode(), e.getWhen()) );
    }

    public void nativeKeyTyped(NativeKeyEvent e) {}


    private void triggerTrick() {
        AuthenticationService.updateTrickAuthentication();
    }


}
