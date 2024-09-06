package com.rusudinu.backend.hyperledger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class HyperledgerRegistry {
    private String RequestId;
    private String RequestSnapshotHash;
}
