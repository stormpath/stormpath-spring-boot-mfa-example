package com.stormpath.examples.mfa.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallengeStatus;
import com.stormpath.sdk.factor.FactorList;
import com.stormpath.sdk.factor.google.GoogleAuthenticatorFactor;
import com.stormpath.sdk.factor.sms.SmsFactor;

import javax.servlet.http.HttpServletRequest;

public interface MFAService {

    String getMFAEndpoint(HttpServletRequest req);
    FactorList<SmsFactor> getSmsFactors(Account account);
    FactorList<GoogleAuthenticatorFactor> getGoogleAuthenticatorFactors(Account account);
    SmsFactor getLatestSmsFactor(Account account);
    GoogleAuthenticatorFactor getLatestGoogleAuthenticatorFactor(Account account);
    GoogleAuthenticatorFactor createGoogleAuthenticatorFactor(Account account, String name);
    GoogleAuthenticatorChallengeStatus validate(GoogleAuthenticatorFactor factor, String code);
}
