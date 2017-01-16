package com.stormpath.examples.mfa.controller;

import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Autowired
    AccountResolver accountResolver;

    @RequestMapping("/")
    public String home() {
        return "home";
    }
}
