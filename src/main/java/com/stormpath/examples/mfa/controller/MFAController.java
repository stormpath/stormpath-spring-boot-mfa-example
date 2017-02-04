package com.stormpath.examples.mfa.controller;

import com.stormpath.examples.mfa.service.MFAService;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallengeStatus;
import com.stormpath.sdk.factor.Factor;
import com.stormpath.sdk.factor.google.GoogleAuthenticatorFactor;
import com.stormpath.sdk.lang.Assert;
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

    @RequestMapping(value = "/sms-setup", method = RequestMethod.GET)
    public String sms() {
        // TODO
        return "mfa/sms";
    }

    private String finishGoog(GoogleAuthenticatorFactor factor, Model model) {
        model.addAttribute("name", factor.getAccountName());
        return "mfa/goog";
    }

    @RequestMapping(value = "/goog-setup", method = RequestMethod.POST)
    public String googSetup(HttpServletRequest req, @RequestParam String name, Model model) {
        Account account = accountResolver.getAccount(req);

        GoogleAuthenticatorFactor factor = mfaService.createGoogleAuthenticatorFactor(account, name);
        model.addAttribute("qrcode", factor.getBase64QrImage());

        return finishGoog(factor, model);
    }

    @RequestMapping(value = "/goog-confirm", method = RequestMethod.GET)
    public String googCode(HttpServletRequest req, Model model) {
        Account account = accountResolver.getAccount(req);
        GoogleAuthenticatorFactor factor = mfaService.getGoogleAuthenticatorFactor(account);
        Assert.notNull(factor);

        return finishGoog(factor, model);
    }

    @RequestMapping(value = "/goog-confirm", method = RequestMethod.POST)
    public String googConfirm(HttpServletRequest req, @RequestParam String code, Model model) {
        Account account = accountResolver.getAccount(req);
        GoogleAuthenticatorFactor factor = mfaService.getGoogleAuthenticatorFactor(account);
        Assert.notNull(factor);

        if (!mfaService.validate(factor, code)) {
            model.addAttribute("error", "Incorrect Google Authenticator code.");
            return googCode(req, model);
        }

        mfaService.addMFAInfoToModel(account, model);
        model.addAttribute("status", "Successful Google Authenticator Code Confirmation");

        return "home";
    }

    @RequestMapping(value = "/goog-delete", method = RequestMethod.POST)
    public String googDelete(HttpServletRequest req, Model model) {
        Account account = accountResolver.getAccount(req);
        GoogleAuthenticatorFactor factor = mfaService.deleteGoogleAuthenticatorFactor(account);

        mfaService.addMFAInfoToModel(account, model);
        model.addAttribute("status", "Successfully Deleted Google Authenticator Factor: " + factor.getAccountName());

        return "home";
    }
}
