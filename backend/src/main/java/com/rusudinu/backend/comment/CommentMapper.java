package com.rusudinu.backend.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "documentId", source = "document.id")
    CommentDTO toCommentDTO(Comment comment);

    @Mapping(target = "document.id", source = "documentId")
    @Mapping(target = "isValid", ignore = true) // This will be set by the service
    Comment toComment(CommentDTO commentDTO);
}
