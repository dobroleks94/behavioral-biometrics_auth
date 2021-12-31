package acs.behavioral_biometrics.keystroke_handler.listener;

import acs.behavioral_biometrics.keystroke_handler.services.KeyProfileHandlerService;
import acs.behavioral_biometrics.keystroke_handler.services.KeyProfileSamplesService;
import lombok.AllArgsConstructor;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class KeyboardListener implements NativeKeyListener {

    private final KeyProfileHandlerService keyProfileHandlerService;
    private final KeyProfileSamplesService samplesService;

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        keyProfileHandlerService.processPressing(NativeKeyEvent.getKeyText(e.getKeyCode()), e.getKeyCode(), e.getWhen());
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        samplesService.addTemporary( keyProfileHandlerService.processReleasing(e.getKeyCode(), e.getWhen()) );
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) { }
}
