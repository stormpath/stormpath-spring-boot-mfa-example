/*
 * Copyright 2017 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.examples.mfa.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallenge;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallengeStatus;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.factor.Factor;
import com.stormpath.sdk.factor.FactorList;
import com.stormpath.sdk.factor.FactorStatus;
import com.stormpath.sdk.factor.FactorVerificationStatus;
import com.stormpath.sdk.factor.Factors;
import com.stormpath.sdk.factor.google.GoogleAuthenticatorFactor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MFAServiceImpl implements MFAService {

    private Client client;

    @Autowired
    public MFAServiceImpl(Client client) {
        this.client = client;
    }

    private String getMFAEndpoint(GoogleAuthenticatorFactor googFactor) {
        if (googFactor == null) { return "/mfa/setup"; }
        return "/mfa/goog-confirm";
    }

    private boolean factorVerified(Factor factor) {
        return factor != null && factor.getFactorVerificationStatus() == FactorVerificationStatus.VERIFIED;
    }

    private boolean latestChallengeIsSatisfied(GoogleAuthenticatorFactor factor) {
        return factor != null && factor.getMostRecentChallenge() != null &&
            factor.getMostRecentChallenge().getStatus() == GoogleAuthenticatorChallengeStatus.SUCCESS;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String getPostLoginMFAEndpoint(Account account) {
        GoogleAuthenticatorFactor googFactor = getGoogleAuthenticatorFactor(account);

        if (googFactor != null) {
            googFactor.createChallenge(client.instantiate(GoogleAuthenticatorChallenge.class));
        }

        return getMFAEndpoint(googFactor);
    }

    @Override
    public Optional<String> getMFAEndpoint(Account account) {
        if (account == null) { return Optional.empty(); }

        GoogleAuthenticatorFactor googFactor = getGoogleAuthenticatorFactor(account);

        if (factorVerified(googFactor) && latestChallengeIsSatisfied(googFactor)) {
            return Optional.empty();
        }

        return Optional.of("redirect:" + getMFAEndpoint(googFactor));
    }

    @SuppressWarnings("unchecked")
    @Override
    public GoogleAuthenticatorFactor getGoogleAuthenticatorFactor(Account account) {
        FactorList<GoogleAuthenticatorFactor> factors = account.getFactors(
            Factors.GOOGLE_AUTHENTICATOR.where(Factors.GOOGLE_AUTHENTICATOR.status().eq(FactorStatus.ENABLED))
        );
        if (factors.getSize() > 0) { return factors.iterator().next(); }
        return null;
    }

    @Override
    public GoogleAuthenticatorFactor createGoogleAuthenticatorFactor(Account account, String name) {
        GoogleAuthenticatorFactor factor = client.instantiate(GoogleAuthenticatorFactor.class);

        factor.setAccountName(name);
        factor.setIssuer(null);
        factor.setStatus(FactorStatus.ENABLED);
        factor = account.createFactor(factor);

        return factor;
    }

    @Override
    public GoogleAuthenticatorFactor deleteGoogleAuthenticatorFactor(Account account) {
        GoogleAuthenticatorFactor factor = getGoogleAuthenticatorFactor(account);
        factor.delete();
        return factor;
    }

    @Override
    public GoogleAuthenticatorChallengeStatus validate(Account account, String code) {
        return getGoogleAuthenticatorFactor(account).createChallenge(code).getStatus();
    }
}
