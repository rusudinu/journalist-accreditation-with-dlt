package com.rusudinu.backend.user;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
	private final UserService userService;
	private final UserMapper userMapper;

	@GetMapping
	public List<UserDTO> getAllUsers() {
		return userService.getAllUsers().stream().map(userMapper::toUserDTO).collect(Collectors.toList());
	}

	@GetMapping("/me")
	public User getMe() {
		Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
		Map<String, Object> attributes = ((JwtAuthenticationToken) authToken).getTokenAttributes();
		String userId = (String) attributes.get("sub");
		return userService.findOrCreateByKeycloakId(userId);
	}

	@GetMapping("/{keycloakId}")
	public User getUserByKeycloakId(@PathVariable String keycloakId) {
		return userService.findOrCreateByKeycloakId(keycloakId);
	}
}
