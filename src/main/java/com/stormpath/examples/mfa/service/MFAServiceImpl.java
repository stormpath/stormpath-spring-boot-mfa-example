package com.stormpath.examples.mfa.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallenge;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallengeStatus;
import com.stormpath.sdk.challenge.sms.SmsChallenge;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.factor.Factor;
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

    @SuppressWarnings("unchecked")
    @Override
    public String getPostLoginMFAEndpoint(Account account) {
        Assert.notNull(account);
        GoogleAuthenticatorFactor googFactor = getGoogleAuthenticatorFactor(account);
        SmsFactor smsFactor = getSmsFactor(account);

        // favor google
        if (googFactor != null) {
            googFactor.createChallenge(client.instantiate(GoogleAuthenticatorChallenge.class));
        } else if (smsFactor != null) {
            smsFactor.createChallenge(client.instantiate(SmsChallenge.class));
        }

        return getMFAEndpoint(googFactor, smsFactor);
    }

    @Override
    public Optional<String> getMFAEndpoint(Account account) {
        if (account == null) { return Optional.empty(); }

        GoogleAuthenticatorFactor googFactor = getGoogleAuthenticatorFactor(account);
        SmsFactor smsFactor = getSmsFactor(account);

        if (
            (factorVerified(googFactor) || factorVerified(smsFactor)) &&
            latestChallengeIsSatisfied(googFactor)
        ) {
            return Optional.empty();
        }

        return Optional.of("redirect:" + getMFAEndpoint(googFactor, smsFactor));
    }

    private boolean factorVerified(Factor factor) {
        if (factor != null && factor.getFactorVerificationStatus() == FactorVerificationStatus.VERIFIED) {
            return true;
        }
        return false;
    }

    private boolean latestChallengeIsSatisfied(GoogleAuthenticatorFactor factor) {
        if (factor == null) { return false; }
        if (
            factor.getMostRecentChallenge() != null &&
            factor.getMostRecentChallenge().getStatus() == GoogleAuthenticatorChallengeStatus.SUCCESS
        ) {
            return true;
        }
        return false;
    }

    @Override
    public void addMFAInfoToModel(Account account, Model model) {
        if (account == null) { return; }

        GoogleAuthenticatorFactor googFactor = getGoogleAuthenticatorFactor(account);
        SmsFactor smsFactor = getSmsFactor(account);

        if (smsFactor != null) { model.addAttribute("smsFactor", smsFactor); }
        if (googFactor != null) { model.addAttribute("googFactor", googFactor); }
    }

    @SuppressWarnings("unchecked")
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

        factor = account.createFactor(
            Factors.GOOGLE_AUTHENTICATOR.newCreateRequestFor(factor).build()
        );

        return factor;
    }

    @Override
    public GoogleAuthenticatorFactor deleteGoogleAuthenticatorFactor(Account account) {
        GoogleAuthenticatorFactor factor = getGoogleAuthenticatorFactor(account);
        factor.delete();
        return factor;
    }

    @Override
    public boolean validate(Factor factor, String code) {
        Assert.notNull(factor);
        Assert.notNull(code);
        return factor.getMostRecentChallenge() != null && factor.getMostRecentChallenge().validate(code);
    }

    private String getMFAEndpoint(GoogleAuthenticatorFactor googFactor, SmsFactor smsFactor) {
        if (googFactor == null && smsFactor == null) {
            return "/mfa/setup";
        } else if (googFactor != null) { // favor google authenticator over sms
            return "/mfa/goog-confirm";
        } else { // smsFactor != null
            return "/mfa/sms-confirm";
        }
    }
}
