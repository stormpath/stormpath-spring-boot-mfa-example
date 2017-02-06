package com.stormpath.examples.mfa.controller;

import com.stormpath.examples.mfa.service.MFAService;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.factor.google.GoogleAuthenticatorFactor;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;

class BaseController {

    private AccountResolver accountResolver;
    MFAService mfaService;

    BaseController(AccountResolver accountResolver, MFAService mfaService) {
        this.accountResolver = accountResolver;
        this.mfaService = mfaService;
    }

    Account getAccount(HttpServletRequest req) {
        return accountResolver.getAccount(req);
    }

    void addMFAInfoToModel(HttpServletRequest req, Model model) {
        Account account = getAccount(req);
        if (account == null) { return; }
        GoogleAuthenticatorFactor googFactor = mfaService.getGoogleAuthenticatorFactor(account);
        if (googFactor != null) { model.addAttribute("googFactor", googFactor); }
    }
}
