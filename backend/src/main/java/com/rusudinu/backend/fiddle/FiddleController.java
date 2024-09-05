package com.rusudinu.backend.fiddle;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import com.rusudinu.backend.eth.EthService;
import com.rusudinu.backend.eth.model.DocumentRegistry;
import com.rusudinu.backend.request.Request;
import com.rusudinu.backend.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.DefaultGasProvider;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/fiddle")
@RequiredArgsConstructor
public class FiddleController {
    private final EthService ethService;

    @GetMapping("{id}")
    public void getHomepageRequests(@PathVariable Long id) {
        ethService.saveDocumentSignature(id, "documentSignature");
        ethService.getDocumentsForRequest(id);
    }
}
