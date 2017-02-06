package com.stormpath.examples.mfa.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallengeStatus;
import com.stormpath.sdk.factor.google.GoogleAuthenticatorFactor;

import java.util.Optional;

public interface MFAService {

    String getPostLoginMFAEndpoint(Account account);
    Optional<String> getMFAEndpoint(Account account);

    GoogleAuthenticatorFactor createGoogleAuthenticatorFactor(Account account, String name);
    GoogleAuthenticatorFactor getGoogleAuthenticatorFactor(Account account);
    GoogleAuthenticatorFactor deleteGoogleAuthenticatorFactor(Account account);
    GoogleAuthenticatorChallengeStatus validate(Account account, String code);
}
