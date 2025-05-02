package com.rusudinu.backend.user;

import com.rusudinu.backend.config.KeycloakClient;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {
	private final KeycloakClient keycloakClient;
	private final UserRepository userRepository;

	public User findOrCreateByKeycloakId(String keycloakId) {
		// Get user from Keycloak
		User keycloakUser = keycloakClient.getUserByKeycloakId(keycloakId);

		// If user doesn't exist in Keycloak, return null or throw exception
		if (keycloakUser == null || keycloakUser.getKeycloakId() == null) {
			throw new RuntimeException("User not found in Keycloak with id: " + keycloakId);
		}

		// If not found, save the user to the database

		return userRepository.findByKeycloakId(keycloakId)
				.orElseGet(() -> {
					// If not found, save the user to the database
					return userRepository.save(keycloakUser);
				});
	}

	public List<User> getAllUsers() {
		System.out.println(keycloakClient.getAllUsers().size());
		System.out.println(keycloakClient.getAllUsers());
		return keycloakClient.getAllUsers();
	}
}
