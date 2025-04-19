package com.rusudinu.backend.document;

import com.rusudinu.backend.comment.CommentDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private List<CommentDTO> comments = new ArrayList<>();
}
