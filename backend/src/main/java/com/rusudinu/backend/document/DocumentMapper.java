package com.rusudinu.backend.document;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DocumentMapper {
    Document toDocument(DocumentDTO documentDTO);

    DocumentDTO toDocumentDTO(Document document);
}
