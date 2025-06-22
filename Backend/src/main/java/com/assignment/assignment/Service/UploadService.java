package com.assignment.assignment.Service;

import com.assignment.assignment.Class.Upload;
import com.assignment.assignment.Repository.UploadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UploadService {

    @Autowired
    private UploadRepository uploadRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Handles the process of validating, saving, and logging an uploaded image,
     * and sends a confirmation email to the user.
     */
    public void handleUpload(MultipartFile file, String imgurUsername, Long imgurId, String email) throws Exception {
        // Validate that the file is an image
        if (!file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Validate file size (limit: 5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB");
        }

        // Generate a unique filename and save the file locally
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get("uploads/" + fileName);
        Files.createDirectories(filePath.getParent()); // Ensure directory exists
        Files.write(filePath, file.getBytes());

        // Save upload info to the database
        Upload upload = new Upload(imgurUsername, filePath.toString(), LocalDateTime.now(), imgurId);
        uploadRepository.save(upload);

        // Send confirmation email
        if (email != null && !email.isEmpty()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Photo Upload Confirmation");
            message.setText("Your photo " + fileName + " has been successfully uploaded!");

            try {
                mailSender.send(message);
            } catch (Exception e) {
                // Log email failure (optional: integrate proper logging system)
                System.err.println("Failed to send email: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if a user has reached the daily upload limit (5 photos per day).
     */
    public boolean hasReachedDailyLimit(String imgurId) {
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1); // exclusive end

        int uploadCount = uploadRepository.countDailyUploads(imgurId, startOfDay, endOfDay);

        return uploadCount >= 5;
    }

}
