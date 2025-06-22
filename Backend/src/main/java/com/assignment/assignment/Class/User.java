package com.assignment.assignment.Class;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "User_Details")
public class User {

    @Id
    private Long userId;

    private String imgurUsername;

    private String email;


    public User(String imgurUsername,Long userId, String email) {
        this.imgurUsername = imgurUsername;
        this.userId = userId;
        this.email = email;
    }
}
