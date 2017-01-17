package com.stormpath.examples.mfa.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.ChallengeList;
import com.stormpath.sdk.challenge.Challenges;
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
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class MFAServiceImpl implements MFAService {

    private Client client;
    private AccountResolver accountResolver;

    @Autowired
    public MFAServiceImpl(Client client, AccountResolver accountResolver) {
        this.client = client;
        this.accountResolver = accountResolver;
    }

    @Override
    public String getMFAEndpoint(HttpServletRequest req) {
        Account account = accountResolver.getAccount(req);
        if (account == null) { return null; }

        GoogleAuthenticatorFactor googFactor = getLatestGoogleAuthenticatorFactor(account);
        SmsFactor smsFactor = getLatestSmsFactor(account);
        if (googFactor == null && smsFactor == null) {
            return "/mfa/setup";
        } else if (googFactor != null) { // favor google authenticator over sms
            return "/mfa/goog";
        } else { // smsFactor != null
            return "/mfa/sms";
        }
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

        // not actually creating a challenge?
        factor = account.createFactor(
            Factors.GOOGLE_AUTHENTICATOR.newCreateRequestFor(factor).createChallenge().build()
        );

        return factor;
    }

    @Override
    public GoogleAuthenticatorChallengeStatus validate(GoogleAuthenticatorFactor factor, String code) {
        GoogleAuthenticatorChallenge challenge = client.instantiate(GoogleAuthenticatorChallenge.class);
        challenge.setCode(code);
        challenge = (GoogleAuthenticatorChallenge) factor.createChallenge(
            Challenges.GOOGLE_AUTHENTICATOR.newCreateRequestFor(challenge).build()
        );

        if (challenge == null) { return GoogleAuthenticatorChallengeStatus.FAILED; }

        return challenge.getStatus();
    }
}
