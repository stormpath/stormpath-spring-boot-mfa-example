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
        GoogleAuthenticatorFactor googFactor = getLatestGoogleAuthenticatorFactor(account);
        SmsFactor smsFactor = getLatestSmsFactor(account);

        return getMFAEndpoint(googFactor, smsFactor, false);
    }

    @Override
    public Optional<String> mfaUnverified(Account account) {
        if (account == null) { return Optional.empty(); }

        GoogleAuthenticatorFactor googFactor = getLatestGoogleAuthenticatorFactor(account);
        SmsFactor smsFactor = getLatestSmsFactor(account);

        if (
            (googFactor != null && googFactor.getFactorVerificationStatus() == FactorVerificationStatus.VERIFIED) ||
            (smsFactor != null && smsFactor.getFactorVerificationStatus() == FactorVerificationStatus.VERIFIED)
        ) {
            return Optional.empty();
        }

        return Optional.of(getMFAEndpoint(googFactor, smsFactor, true));
    }

    private String getMFAEndpoint(GoogleAuthenticatorFactor googFactor, SmsFactor smsFactor, boolean shouldRedirect) {
        String ret = shouldRedirect ? "redirect:" : "";
        if (googFactor == null && smsFactor == null) {
            ret += "/mfa/setup";
        } else if (googFactor != null) { // favor google authenticator over sms
            ret += "/mfa/goog";
        } else { // smsFactor != null
            ret += "/mfa/sms";
        }
        return ret;
    }

    @Override
    public FactorList<SmsFactor> getSmsFactors(Account account) {
        return getSmsFactors(account, false);
    }

    private FactorList<SmsFactor> getSmsFactors(Account account, boolean andVerified) {
        FactorCriteria criteria = Factors.SMS.where(Factors.SMS.status().eq(FactorStatus.ENABLED));
        if (andVerified) {
            criteria = criteria.and(Factors.SMS.verificationStatus().eq(FactorVerificationStatus.VERIFIED));
        }
        return account.getFactors(criteria);
    }

    @Override
    public FactorList<GoogleAuthenticatorFactor> getGoogleAuthenticatorFactors(Account account) {
        return getGoogleAuthenticatorFactors(account, false);
    }

    private FactorList<GoogleAuthenticatorFactor> getGoogleAuthenticatorFactors(Account account, boolean andVerified) {
        FactorCriteria criteria = Factors.GOOGLE_AUTHENTICATOR.where(Factors.GOOGLE_AUTHENTICATOR.status().eq(FactorStatus.ENABLED));
        if (andVerified) {
            criteria = criteria.and(Factors.GOOGLE_AUTHENTICATOR.verificationStatus().eq(FactorVerificationStatus.VERIFIED));
        }
        return account.getFactors(criteria);
    }

    @Override
    public SmsFactor getLatestSmsFactor(Account account) {
        FactorList<SmsFactor> smsFactors = getSmsFactors(account);
        if (smsFactors.getSize() > 0) {
            return smsFactors.iterator().next();
        }
        return null;
    }

    @Override
    public GoogleAuthenticatorFactor getLatestGoogleAuthenticatorFactor(Account account) {
        FactorList<GoogleAuthenticatorFactor> googleAuthenticatorFactors = getGoogleAuthenticatorFactors(account);
        if (googleAuthenticatorFactors.getSize() > 0) {
            return googleAuthenticatorFactors.iterator().next();
        }
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
}
