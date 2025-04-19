package com.rusudinu.backend.document;

import com.rusudinu.backend.comment.CommentMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {CommentMapper.class})
public interface DocumentMapper {
    Document toDocument(DocumentDTO documentDTO);

    DocumentDTO toDocumentDTO(Document document);
}
