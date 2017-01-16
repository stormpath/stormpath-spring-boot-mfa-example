package com.stormpath.examples.mfa.service;

import org.springframework.stereotype.Service;

@Service
public class MFAServiceImpl implements MFAService {

    @Override
    public boolean isMFASetup() {
        return true;
    }
}
