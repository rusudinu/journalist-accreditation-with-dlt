package com.rusudinu.backend.fiddle;

import com.rusudinu.backend.eth.EthService;
import com.rusudinu.backend.hash.HashService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/fiddle")
@RequiredArgsConstructor
public class FiddleController {
    private final EthService ethService;
    private final HashService hashService;

    @GetMapping
    public boolean getHomepageRequests() {
        return hashService.verifyDocument(hashService.hashDocument("test.pdf"), "test.pdf");
//        ethService.saveDocumentSignature(id, "documentSignature");
//        ethService.getDocumentsForRequest(id);
    }
}
