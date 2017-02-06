package com.stormpath.examples.mfa.controller;

import com.stormpath.examples.mfa.service.MFAService;
import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.servlet.account.AccountResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Controller
public class HomeController extends BaseController {

    @Autowired
    public HomeController(AccountResolver accountResolver, MFAService mfaService) {
        super(accountResolver, mfaService);
    }

    @RequestMapping("/")
    public String home(HttpServletRequest req, Model model) {
        Account account = getAccount(req);

        addMFAInfoToModel(req, model);

        Optional<String> mfaUnverified = mfaService.getMFAEndpoint(account);
        return mfaUnverified.orElse("home");
    }
}
