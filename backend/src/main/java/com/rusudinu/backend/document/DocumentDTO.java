package com.rusudinu.backend.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class DocumentDTO {
    private Long id;
    private ZonedDateTime createdDate;
    private boolean isDeleted;
    private String storedDocumentName;
    private String status;
    private Long userId;
}
