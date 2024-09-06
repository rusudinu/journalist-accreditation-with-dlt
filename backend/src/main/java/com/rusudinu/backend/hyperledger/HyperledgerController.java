package com.rusudinu.backend.hyperledger;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/hyperledger")
@RequiredArgsConstructor
public class HyperledgerController {
    private final HyperledgerRegistryService hyperledgerRegistryService;

    @GetMapping("/registry/{requestId}")
    public HyperledgerRegistry getHyperledgerRegistryByRegistryId(@PathVariable("requestId") Long requestId) {
        return hyperledgerRegistryService.getHyperledgerRegistryByRegistryId(requestId);
    }

    @PostMapping("/registry")
    public void saveOrUpdateHyperledgerRegistry(@RequestBody HyperledgerRegistry hyperledgerRegistry) {
        hyperledgerRegistryService.saveOrUpdateHyperledgerRegistry(hyperledgerRegistry);
    }
}
