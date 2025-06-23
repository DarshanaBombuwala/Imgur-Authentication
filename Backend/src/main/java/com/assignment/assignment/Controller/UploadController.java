package com.assignment.assignment.Controller;

import com.assignment.assignment.Class.User;
import com.assignment.assignment.Repository.UserRepository;
import com.assignment.assignment.Service.UploadService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    @Autowired
    private final UploadService uploadService;

    @Autowired
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<String> uploadPhoto(@RequestParam("photo") MultipartFile file, HttpSession session) {
        String imgurUsername = (String) session.getAttribute("imgurUsername");

        // Ensure user is authenticated
        if (imgurUsername == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }

        Optional<User> existingUser = userRepository.findByImgurUsername(imgurUsername);
        existingUser.ifPresent(user -> session.setAttribute("email", user.getEmail()));

        String email = (String) session.getAttribute("email");
        String imgurId = (String) session.getAttribute("userId");

        // Enforce daily upload limit
        if (uploadService.hasReachedDailyLimit(imgurId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Daily upload limit reached (5 photos)");
        }

        try {
            uploadService.handleUpload(file, imgurUsername, Long.valueOf(imgurId), email);
            return ResponseEntity.ok("Photo uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/checkUserExists")
    public ResponseEntity<Boolean> checkUserExists(HttpSession session) {
        String userId = (String) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.ok(false); // User not logged in
        }

        try {
            boolean exists = userRepository.existsById(Long.valueOf(userId));
            return ResponseEntity.ok(exists);
        } catch (NumberFormatException e) {
            // Invalid user ID in session
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/saveUserIfNotExists")
    public ResponseEntity<Boolean> saveUserIfNotExists(@RequestBody Map<String, String> payload, HttpSession session) {
        String email = payload.get("email");
        session.setAttribute("email", email);

        String userId = (String) session.getAttribute("userId");

        // Check if user already exists by ID
        if (userId != null && userRepository.findById(Long.valueOf(userId)).isPresent()) {
            return ResponseEntity.ok(false);
        }

        String imgurUsername = (String) session.getAttribute("imgurUsername");
        String accessToken = (String) session.getAttribute("accessToken");

        // Ensure session has required values
        if (imgurUsername == null || accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        // Save new user
        User user = new User();
        user.setImgurUsername(imgurUsername);
        user.setEmail(email);
        user.setUserId(Long.valueOf(userId));
        User saved = userRepository.save(user);

        session.setAttribute("userId", String.valueOf(saved.getUserId()));
        return ResponseEntity.ok(true);
    }
}
