package com.rusudinu.backend.config;

import com.rusudinu.backend.user.User;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakClient {

	// Admin credentials - should be stored securely in a production environment
	private static final String ADMIN_USERNAME = "admin";
	private static final String ADMIN_PASSWORD = "admin";
	private final RestTemplate restTemplate;
	@Value("${spring.security.oauth2.client.provider.keycloak.issuer-uri}")
	private String keycloakIssuerUri;
	@Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
	private String clientId;

	/**
	 * Get all users from Keycloak
	 * @return List of User objects
	 */
	public List<User> getAllUsers() {
		String accessToken = getAdminToken();

		// Extract the realm name from the issuer URI
		String realm = extractRealmFromIssuerUri();

		// Set up headers with the access token
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		// Create the HTTP entity
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Call Keycloak API to get all users
		ResponseEntity<List> response = restTemplate.exchange(
				keycloakIssuerUri.replace("/realms/" + realm, "") + "/admin/realms/" + realm + "/users",
				HttpMethod.GET,
				entity,
				List.class
		);

		List<Map<String, Object>> users = response.getBody();

		// Convert Keycloak users to our User model
		List<User> result = new ArrayList<>();
		if (users != null) {
			for (Map<String, Object> keycloakUser : users) {
				User user = new User();
				user.setKeycloakId((String) keycloakUser.get("id"));

				// Set name if available
				if (keycloakUser.containsKey("firstName") && keycloakUser.containsKey("lastName")) {
					String firstName = (String) keycloakUser.get("firstName");
					String lastName = (String) keycloakUser.get("lastName");
					if (firstName != null && lastName != null) {
						user.setName(firstName + " " + lastName);
					}
					else if (firstName != null) {
						user.setName(firstName);
					}
					else if (lastName != null) {
						user.setName(lastName);
					}
				}

				// If name is still null, use username
				if (user.getName() == null && keycloakUser.containsKey("username")) {
					user.setName((String) keycloakUser.get("username"));
				}

				result.add(user);
			}
		}

		return result;
	}

	/**
	 * Get a user from Keycloak by ID
	 * @param keycloakId The Keycloak user ID
	 * @return User object
	 */
	public User getUserByKeycloakId(String keycloakId) {
		String accessToken = getAdminToken();

		// Extract the realm name from the issuer URI
		String realm = extractRealmFromIssuerUri();

		// Set up headers with the access token
		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		// Create the HTTP entity
		HttpEntity<String> entity = new HttpEntity<>(headers);

		// Call Keycloak API to get the user
		ResponseEntity<Map> response = restTemplate.exchange(
				keycloakIssuerUri.replace("/realms/" + realm, "") + "/admin/realms/" + realm + "/users/" + keycloakId,
				HttpMethod.GET,
				entity,
				Map.class
		);

		Map<String, Object> keycloakUser = response.getBody();

		// Convert Keycloak user to our User model
		User user = new User();
		if (keycloakUser != null) {
			user.setKeycloakId((String) keycloakUser.get("id"));

			// Set name if available
			if (keycloakUser.containsKey("firstName") && keycloakUser.containsKey("lastName")) {
				String firstName = (String) keycloakUser.get("firstName");
				String lastName = (String) keycloakUser.get("lastName");
				if (firstName != null && lastName != null) {
					user.setName(firstName + " " + lastName);
				}
				else if (firstName != null) {
					user.setName(firstName);
				}
				else if (lastName != null) {
					user.setName(lastName);
				}
			}

			// If name is still null, use username
			if (user.getName() == null && keycloakUser.containsKey("username")) {
				user.setName((String) keycloakUser.get("username"));
			}
		}

		return user;
	}

	/**
	 * Get an admin token to access Keycloak's admin API
	 * @return Access token
	 */
	private String getAdminToken() {
		// Extract the realm name from the issuer URI
		String realm = extractRealmFromIssuerUri();

		// Prepare the request body
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("client_id", "admin-cli");
		formData.add("username", ADMIN_USERNAME);
		formData.add("password", ADMIN_PASSWORD);
		formData.add("grant_type", "password");

		// Set up headers
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// Create the HTTP entity
		HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

		// Call Keycloak token endpoint
		ResponseEntity<Map> response = restTemplate.exchange(
				keycloakIssuerUri.replace("/realms/" + realm, "") + "/realms/master/protocol/openid-connect/token",
				HttpMethod.POST,
				entity,
				Map.class
		);

		Map<String, Object> tokenResponse = response.getBody();

		if (tokenResponse != null && tokenResponse.containsKey("access_token")) {
			return (String) tokenResponse.get("access_token");
		}
		else {
			throw new RuntimeException("Failed to get admin token from Keycloak");
		}
	}

	/**
	 * Extract the realm name from the issuer URI
	 * @return Realm name
	 */
	private String extractRealmFromIssuerUri() {
		// The issuer URI is in the format: http://localhost:9001/realms/journalist-accreditation
		String[] parts = keycloakIssuerUri.split("/realms/");
		if (parts.length > 1) {
			return parts[1];
		}
		else {
			throw new RuntimeException("Invalid Keycloak issuer URI format");
		}
	}
}
