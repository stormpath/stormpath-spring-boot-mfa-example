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
