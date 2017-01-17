package com.stormpath.examples.mfa.controller;

import com.stormpath.examples.mfa.service.MFAService;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class HomeController {

    private AccountResolver accountResolver;
    private MFAService mfaService;

    @Autowired
    public HomeController(AccountResolver accountResolver, MFAService mfaService) {
        this.accountResolver = accountResolver;
        this.mfaService = mfaService;
    }

    @RequestMapping("/")
    public String home(HttpServletRequest req) {
//        if (mfaService.shouldRedirectToMFAFlow(req)) {
//            return "redirect:" + mfaService.getMFAEndpoint(req);
//        }
        return "home";
    }
}
