package com.stormpath.examples.mfa.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallengeStatus;
import com.stormpath.sdk.factor.FactorList;
import com.stormpath.sdk.factor.google.GoogleAuthenticatorFactor;
import com.stormpath.sdk.factor.sms.SmsFactor;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

public interface MFAService {

    String getPostLoginMFAEndpoint(Account account);
    Optional<String> getMFAUnverifiedEndpoint(Account account);
    void addMFAInfoToModel(Account account, Model model);

    SmsFactor getSmsFactor(Account account);
    GoogleAuthenticatorFactor getGoogleAuthenticatorFactor(Account account);

    GoogleAuthenticatorFactor createGoogleAuthenticatorFactor(Account account, String name);
    GoogleAuthenticatorFactor deleteGoogleAuthenticatorFactor(Account account);
    GoogleAuthenticatorChallengeStatus validate(GoogleAuthenticatorFactor factor, String code);
}
