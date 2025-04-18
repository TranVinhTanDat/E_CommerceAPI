package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Service.JwtService;
import com.example.shoppecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;
    @PostMapping("/add-user")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        User createdUser = userService.addUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/edit-user/{id}")
    public ResponseEntity<?> editUser(
            @PathVariable Long id,
            @RequestBody User updatedUser,
            @RequestHeader("Authorization") String token) {
        try {
            // Lấy username từ token
            String username = jwtService.extractUsername(token.substring(7));
            User existingUser = userService.findByUsername(username);
            if (existingUser == null || !existingUser.getId().equals(id)) {
                return ResponseEntity.badRequest().body("User not found or unauthorized");
            }

            // Cập nhật thông tin người dùng
            existingUser.setUsername(updatedUser.getUsername());
            existingUser.setEmail(updatedUser.getEmail());
            existingUser.setAvatar(updatedUser.getAvatar());
            existingUser.setRole(updatedUser.getRole());
            userService.updateUser(existingUser);

            // Nếu username thay đổi, tạo token mới
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User updated successfully");
            if (!username.equals(updatedUser.getUsername())) {
                UserDetails userDetails = userService.loadUserByUsername(updatedUser.getUsername());
                String newToken = jwtService.generateToken(userDetails, existingUser.getId(), existingUser.getRole());
                response.put("token", newToken);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Delete User Success");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/id")
    public ResponseEntity<User> findUserById(@PathVariable long id) {
        Optional<User> user = userService.getUserId(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }
}