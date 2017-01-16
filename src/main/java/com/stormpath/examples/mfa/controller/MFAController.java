package com.stormpath.examples.mfa.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller("/mfa")
public class MFAController {

    @RequestMapping("setup")
    public String onRegister() {
        return "mfa/setup";
    }
}
