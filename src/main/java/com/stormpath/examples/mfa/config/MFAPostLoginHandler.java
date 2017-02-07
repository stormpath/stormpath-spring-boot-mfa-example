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
package com.stormpath.examples.mfa.config;

import com.stormpath.examples.mfa.service.MFAService;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.mvc.WebHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class MFAPostLoginHandler {

    private MFAService mfaService;

    private static final Logger log = LoggerFactory.getLogger(MFAPostLoginHandler.class);

    @Autowired
    public MFAPostLoginHandler(MFAService mfaService) {
        this.mfaService = mfaService;
    }

    @Bean
    public WebHandler loginPostHandler() {
        return (request, response, account) -> {
            String mfaEndpoint = mfaService.getPostLoginMFAEndpoint(account);
            try {
                response.sendRedirect(mfaEndpoint);
            } catch (IOException e) {
                log.error("Error redirecting to {}: {}", mfaEndpoint, e.getMessage(), e);
            }
            return false;
        };
    }
}
