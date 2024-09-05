package com.rusudinu.backend.request;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    Request toRequest(RequestDTO requestDTO);

    RequestDTO toRequestDTO(Request request);
}
