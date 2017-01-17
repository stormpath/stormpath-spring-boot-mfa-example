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
        return new WebHandler() {
            @Override
            public boolean handle(HttpServletRequest request, HttpServletResponse response, Account account) {
                String mfaEndpoint = mfaService.getMFAEndpoint(request);
                try {
                    response.sendRedirect(mfaEndpoint);
                } catch (IOException e) {
                    log.error("Error redirecting to {}: {}", mfaEndpoint, e.getMessage(), e);
                }
                return false;
            }
        };
    }
}
