package com.rusudinu.backend.comment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "documentId", source = "document.id")
    CommentDTO toCommentDTO(Comment comment);

    @Mapping(target = "document.id", source = "documentId")
    Comment toComment(CommentDTO commentDTO);
}
