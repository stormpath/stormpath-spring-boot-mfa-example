package com.stormpath.examples.mfa.controller;

import com.stormpath.examples.mfa.service.MFAService;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    @Autowired
    MFAService mfaService;

    @Autowired
    AccountResolver accountResolver;

    @RequestMapping("/")
    public String home(HttpServletRequest req) {
        if (accountResolver.getAccount(req) != null && !mfaService.isMFASetup()) {
            return "redirect:/mfa/setup";
        }
        return "home";
    }
}
