package com.stormpath.examples.mfa.controller;

import com.stormpath.examples.mfa.service.MFAService;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallengeStatus;
import com.stormpath.sdk.factor.google.GoogleAuthenticatorFactor;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/mfa")
public class MFAController {

    private AccountResolver accountResolver;
    private MFAService mfaService;

    @Autowired
    public MFAController(AccountResolver accountResolver, MFAService mfaService) {
        this.accountResolver = accountResolver;
        this.mfaService = mfaService;
    }

    @RequestMapping("/setup")
    public String setup(HttpServletRequest req, Model model) {
        // no need for null check, can't get here without being logged in
        Account account = accountResolver.getAccount(req);

        mfaService.addMFAInfoToModel(account, model);

        return "mfa/setup";
    }

    @RequestMapping(value = "/sms", method = RequestMethod.GET)
    public String sms() {
        // TODO
        return "mfa/sms";
    }

    @RequestMapping(value = "/goog")
    public String goog(HttpServletRequest req, @RequestParam(required = false) String name, Model model) {
        Account account = accountResolver.getAccount(req);

        GoogleAuthenticatorFactor factor = mfaService.getGoogleAuthenticatorFactor(account);
        if (factor == null) {
            factor = mfaService.createGoogleAuthenticatorFactor(account, name);
        }

        final GoogleAuthenticatorFactor googFactor = factor;
        mfaService.getMFAUnverifiedEndpoint(account).ifPresent(s -> model.addAttribute("qrcode", googFactor.getBase64QrImage()));

        model.addAttribute("name", factor.getAccountName());

        return "mfa/goog";
    }

    @RequestMapping(value = "/goog-confirm", method = RequestMethod.POST)
    public String googConfirm(HttpServletRequest req, @RequestParam String code, Model model) {
        Account account = accountResolver.getAccount(req);
        GoogleAuthenticatorFactor factor = mfaService.getGoogleAuthenticatorFactor(account);
        if (factor == null) {
            // TODO need error message here
            return "redirect:/mfa/setup";
        }

        if (mfaService.validate(factor, code) != GoogleAuthenticatorChallengeStatus.SUCCESS) {
            // TODO need error message here
            return "redirect:/mfa/goog?name=" + factor.getAccountName();
        }

        mfaService.addMFAInfoToModel(account, model);

        // TODO need factor verification success message
        return "home";
    }
}
