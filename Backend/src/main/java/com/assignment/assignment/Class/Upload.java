package com.assignment.assignment.Class;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Upload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imgurUsername;

    private String filePath;

    @Column(nullable = true)
    private String email;

    private LocalDateTime uploadDate;

    private Long imgurUserId;

    public Upload(String imgurUsername, String string, LocalDateTime now, Long imgurId) {
        this.imgurUsername = imgurUsername;
        this.filePath = string;
        this.uploadDate = now;
        this.imgurUserId = imgurId;
    }
}
