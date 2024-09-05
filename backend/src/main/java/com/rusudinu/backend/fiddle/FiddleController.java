package com.rusudinu.backend.fiddle;

import com.rusudinu.backend.eth.EthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

@Slf4j
@RestController
@RequestMapping("/api/v1/fiddle")
@RequiredArgsConstructor
public class FiddleController {
    private final EthService ethService;

    @GetMapping("{id}")
    public void getHomepageRequests(@PathVariable Long id) {
//        ethService.saveDocumentSignature(id, "documentSignature");
//        ethService.getDocumentsForRequest(id);
        try {
            ethService.testKey();
        } catch (UnrecoverableKeyException e) {
            throw new RuntimeException(e);
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
