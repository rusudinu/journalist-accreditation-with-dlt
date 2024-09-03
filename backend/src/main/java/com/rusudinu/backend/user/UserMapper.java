package com.rusudinu.backend.user;

import com.rusudinu.backend.document.Document;
import com.rusudinu.backend.document.DocumentDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserDTO userDTO);

    UserDTO toUserDTO(User user);
}
