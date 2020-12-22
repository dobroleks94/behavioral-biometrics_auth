package com.diploma.behavioralBiometricsAuthentication.controllers;

import com.diploma.behavioralBiometricsAuthentication.listeners.KeyboardListener;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class LoginPageController {

    private KeyboardListener listener;
    @Autowired
    public void setListener(KeyboardListener listener) {
        this.listener = listener;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String loginPage(Model model){
        return "login";
    }
}
