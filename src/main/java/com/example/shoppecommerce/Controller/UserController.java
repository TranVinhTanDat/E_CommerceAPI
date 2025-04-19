package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/add-user")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        logger.info("Adding new user: {}", user.getUsername());
        User createdUser = userService.addUser(user);
        logger.info("User added successfully: {}", createdUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/edit-user/{id}")
    public ResponseEntity<User> editUser(@PathVariable long id, @RequestBody User user) {
        logger.info("Received request to update user with ID: {}", id);
        try {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            logger.info("Authenticated user: {}", currentUsername);
            User currentUser = userService.findByUsername(currentUsername);

            if (currentUser == null) {
                logger.error("Current user not found for username: {}", currentUsername);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            logger.info("Current user ID: {}, Role: {}", currentUser.getId(), currentUser.getRole());
            if (!currentUser.getId().equals(id) && !currentUser.getRole().equals("ADMIN")) {
                logger.warn("User {} does not have permission to update user ID: {}", currentUsername, id);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            User updatedUser = userService.updateUser(id, user);
            logger.info("User updated successfully: {}", updatedUser.getId());
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            logger.error("Error updating user ID: {}. Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser() {
        logger.info("Fetching current user");
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Authenticated user: {}", currentUsername);

        User currentUser = userService.findByUsername(currentUsername);
        if (currentUser == null) {
            logger.error("Current user not found for username: {}", currentUsername);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        logger.info("Current user found: {}", currentUser.getUsername());
        return ResponseEntity.ok(currentUser);
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        logger.info("Deleting user with ID: {}", id);
        try {
            userService.deleteUser(id);
            logger.info("User deleted successfully: {}", id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Delete User Success");
        } catch (RuntimeException e) {
            logger.error("Error deleting user ID: {}. Error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Fetching all users");
        List<User> users = userService.getAllUsers();
        logger.info("Fetched {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/id")
    public ResponseEntity<User> findUserById(@RequestParam long id) {
        logger.info("Fetching user with ID: {}", id);
        Optional<User> user = userService.getUserId(id);
        if (user.isPresent()) {
            logger.info("User found: {}", user.get().getUsername());
            return ResponseEntity.ok(user.get());
        } else {
            logger.warn("User not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}