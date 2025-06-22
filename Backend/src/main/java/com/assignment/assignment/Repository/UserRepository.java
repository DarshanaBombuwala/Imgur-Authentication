package com.assignment.assignment.Repository;
import com.assignment.assignment.Class.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByImgurUsername(String imgurUsername);
}