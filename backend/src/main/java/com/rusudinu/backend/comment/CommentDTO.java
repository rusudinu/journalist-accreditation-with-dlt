package com.rusudinu.backend.comment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CommentDTO {
    private Long id;
    private ZonedDateTime createdDate;
    private String content;
    private String author;
    private Long documentId;
    private String commentHash;
    private Boolean isValid;
}
