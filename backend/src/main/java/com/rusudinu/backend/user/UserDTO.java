package com.rusudinu.backend.user;

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
}
