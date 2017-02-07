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
package com.stormpath.examples.mfa.controller;

import com.stormpath.examples.mfa.service.MFAService;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.challenge.google.GoogleAuthenticatorChallengeStatus;
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
public class MFAController extends BaseController {

    @Autowired
    public MFAController(AccountResolver accountResolver, MFAService mfaService) {
        super(accountResolver, mfaService);
    }

    @RequestMapping("/setup")
    public String setup(HttpServletRequest req, Model model) {
        addMFAInfoToModel(req, model);
        return "mfa/setup";
    }

    private String finishGoog(GoogleAuthenticatorFactor factor, Model model) {
        model.addAttribute("name", factor.getAccountName());
        return "mfa/goog";
    }

    @RequestMapping(value = "/goog-setup", method = RequestMethod.POST)
    public String googSetup(HttpServletRequest req, @RequestParam String name, Model model) {
        GoogleAuthenticatorFactor factor = mfaService.createGoogleAuthenticatorFactor(getAccount(req), name);
        model.addAttribute("qrcode", factor.getBase64QrImage());

        return finishGoog(factor, model);
    }

    @RequestMapping(value = "/goog-confirm", method = RequestMethod.GET)
    public String googCode(HttpServletRequest req, Model model) {
        GoogleAuthenticatorFactor factor = mfaService.getGoogleAuthenticatorFactor(getAccount(req));

        return finishGoog(factor, model);
    }

    @RequestMapping(value = "/goog-confirm", method = RequestMethod.POST)
    public String googConfirm(HttpServletRequest req, @RequestParam String code, Model model) {
        if (mfaService.validate(getAccount(req), code) != GoogleAuthenticatorChallengeStatus.SUCCESS) {
            model.addAttribute("error", "Incorrect Google Authenticator code.");
            return googCode(req, model);
        }

        addMFAInfoToModel(req, model);
        model.addAttribute("status", "Successful Google Authenticator Code Confirmation");

        return "home";
    }

    @RequestMapping(value = "/goog-delete", method = RequestMethod.POST)
    public String googDelete(HttpServletRequest req, Model model) {
        GoogleAuthenticatorFactor factor = mfaService.deleteGoogleAuthenticatorFactor(getAccount(req));

        addMFAInfoToModel(req, model);
        model.addAttribute("status", "Successfully Deleted Google Authenticator Factor: " + factor.getAccountName());

        return "home";
    }
}
