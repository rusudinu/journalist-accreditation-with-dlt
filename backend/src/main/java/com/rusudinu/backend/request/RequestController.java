package com.rusudinu.backend.request;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import com.rusudinu.backend.user.User;
import com.rusudinu.backend.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/requests")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;
    private final UserService userService;

    @PostMapping
    public Request createRequest() {
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> attributes = ((JwtAuthenticationToken) authToken).getTokenAttributes();
        String userId = (String) attributes.get("sub");
        User user = userService.findOrCreateByKeycloakId(userId);
        return requestService.createRequest(user);
    }

    @GetMapping
    public List<Request> getHomepageRequests() {
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> attributes = ((JwtAuthenticationToken) authToken).getTokenAttributes();
        String userId = (String) attributes.get("sub");
        User user = userService.findOrCreateByKeycloakId(userId);
        LinkedTreeMap<String, Object> roles = (LinkedTreeMap<String, Object>) attributes.get("realm_access");
        List<String> rolesList = (List<String>) roles.get("roles");
        return requestService.fetchHomePageRequests(user.getId(), rolesList);
    }
}
