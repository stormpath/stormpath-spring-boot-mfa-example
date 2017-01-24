package com.stormpath.examples.mfa.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallenge;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallengeStatus;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.factor.FactorCriteria;
import com.stormpath.sdk.factor.FactorList;
import com.stormpath.sdk.factor.FactorStatus;
import com.stormpath.sdk.factor.FactorVerificationStatus;
import com.stormpath.sdk.factor.Factors;
import com.stormpath.sdk.factor.google.GoogleAuthenticatorFactor;
import com.stormpath.sdk.factor.sms.SmsFactor;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;

@Service
public class MFAServiceImpl implements MFAService {

    private Client client;

    @Autowired
    public MFAServiceImpl(Client client, AccountResolver accountResolver) {
        this.client = client;
    }

    @Override
    public String getPostLoginMFAEndpoint(Account account) {
        Assert.notNull(account);
        GoogleAuthenticatorFactor googFactor = getGoogleAuthenticatorFactor(account);
        SmsFactor smsFactor = getSmsFactor(account);

        return getMFAEndpoint(googFactor, smsFactor, false);
    }

    @Override
    public Optional<String> getMFAUnverifiedEndpoint(Account account) {
        if (account == null) { return Optional.empty(); }

        GoogleAuthenticatorFactor googFactor = getGoogleAuthenticatorFactor(account);
        SmsFactor smsFactor = getSmsFactor(account);

        if (
            (googFactor != null && googFactor.getFactorVerificationStatus() == FactorVerificationStatus.VERIFIED) ||
            (smsFactor != null && smsFactor.getFactorVerificationStatus() == FactorVerificationStatus.VERIFIED)
        ) {
            return Optional.empty();
        }

        return Optional.of(getMFAEndpoint(googFactor, smsFactor, true));
    }

    @Override
    public void addMFAInfoToModel(Account account, Model model) {
        if (account == null) { return; }

        GoogleAuthenticatorFactor googFactor = getGoogleAuthenticatorFactor(account);
        SmsFactor smsFactor = getSmsFactor(account);

        if (smsFactor != null) { model.addAttribute("smsFactor", smsFactor); }
        if (googFactor != null) { model.addAttribute("googFactor", googFactor); }
    }

    @Override
    public SmsFactor getSmsFactor(Account account) {
        FactorList<SmsFactor> factors = account.getFactors(
            Factors.SMS.where(Factors.SMS.status().eq(FactorStatus.ENABLED))
        );
        if (factors.getSize() > 0) {
            return factors.iterator().next();
        }
        return null;
    }

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

        factor = account.createFactor(
                Factors.GOOGLE_AUTHENTICATOR.newCreateRequestFor(factor).build()
        );

        return factor;
    }

    @Override
    public GoogleAuthenticatorChallengeStatus validate(GoogleAuthenticatorFactor factor, String code) {
        GoogleAuthenticatorChallenge challenge = factor.createChallenge(code);

        return challenge.getStatus();
    }

    private String getMFAEndpoint(GoogleAuthenticatorFactor googFactor, SmsFactor smsFactor, boolean shouldRedirect) {
        String ret = shouldRedirect ? "redirect:" : "";
        if (googFactor == null && smsFactor == null) {
            ret += "/mfa/setup";
        } else if (googFactor != null) { // favor google authenticator over sms
            ret += "/mfa/goog-confirm";
        } else { // smsFactor != null
            ret += "/mfa/sms-confirm";
        }
        return ret;
    }
}
