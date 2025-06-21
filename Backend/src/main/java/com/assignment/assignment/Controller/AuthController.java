package com.assignment.assignment.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth20Service;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/auth")
public class AuthController {

    private final OAuth20Service imgurService;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuthController(OAuth20Service imgurService, ObjectMapper objectMapper) {
        this.imgurService = imgurService;
        this.objectMapper = objectMapper;
    }

    /**
     * Redirects user to Imgur authorization page if not already authenticated.
     */
    @GetMapping("/imgur")
    public void redirectToImgur(HttpSession session, HttpServletResponse response) throws IOException {
        // If user already logged in, redirect to dashboard
        if (session.getAttribute("imgurUsername") != null) {
            response.sendRedirect("http://localhost:3000/dashboard");
            return;
        }

        // Generate authorization URL and redirect user to Imgur
        String authorizationUrl = imgurService.getAuthorizationUrl();
        response.sendRedirect(authorizationUrl);
    }

    /**
     * Handles Imgur OAuth2 callback, retrieves user info, and stores in session.
     */
    @GetMapping("/callback")
    public void handleCallback(@RequestParam("code") String code,
                               HttpSession session,
                               HttpServletResponse httpServletResponse) throws IOException {

        // If user already logged in, redirect to dashboard
        if (session.getAttribute("imgurUsername") != null) {
            httpServletResponse.sendRedirect("http://localhost:3000/dashboard");
            return;
        }

        try {
            // Exchange code for access token
            OAuth2AccessToken accessToken = imgurService.getAccessToken(code);

            // Request user account info from Imgur
            OAuthRequest request = new OAuthRequest(Verb.GET, "https://api.imgur.com/3/account/me");
            imgurService.signRequest(accessToken, request);
            Response imgurResponse = imgurService.execute(request);

            // Parse response to get username and user ID
            JsonNode jsonNode     = objectMapper.readTree(imgurResponse.getBody());
            String imgurUsername  = jsonNode.path("data").path("url").asText();
            String userId         = jsonNode.path("data").path("id").asText();

            // Set authentication in Spring Security context
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(imgurUsername, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authToken);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            // Store user details in session
            session.setAttribute("imgurUsername", imgurUsername);
            session.setAttribute("accessToken", accessToken.getAccessToken());
            session.setAttribute("userId", userId);

            // Redirect to frontend dashboard
            httpServletResponse.sendRedirect("http://localhost:3000/dashboard");

        } catch (IOException | InterruptedException | ExecutionException e) {
            httpServletResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failed");
        }
    }

    /**
     * Logs out the user by invalidating the session.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build(); // just return 200 OK without redirect
    }

    /**
     * Endpoint to check if user is authenticated via session.
     */
    @GetMapping("/check")
    public ResponseEntity<String> checkAuthentication(HttpSession session) {
        if (session != null && session.getAttribute("imgurUsername") != null) {
            return ResponseEntity.ok("Authenticated");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
    }
    public void revokeImgurToken(String accessToken) throws Exception {
        String url = "https://api.imgur.com/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Form data for revoking the token
        String body = "token=" + accessToken;

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to revoke Imgur token. Status: " + response.getStatusCode());
        }
    }
}
