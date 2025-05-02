package com.rusudinu.backend.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
	User toUser(UserDTO userDTO);

	UserDTO toUserDTO(User user);
}
