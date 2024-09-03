package com.rusudinu.backend.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rusudinu.backend.document.DocumentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UserDTO {
    private Long id;
    private String keycloakId;
    private ZonedDateTime createdDate;
    private boolean isDeleted;
    private List<DocumentDTO> documents;
}
