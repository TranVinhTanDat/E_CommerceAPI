package com.example.shoppecommerce.Controller;

import com.example.shoppecommerce.Entity.Comment;
import com.example.shoppecommerce.Entity.CommentRequest;
import com.example.shoppecommerce.Entity.Product;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Service.CommentService;
import com.example.shoppecommerce.Service.ProductService;
import com.example.shoppecommerce.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Comment>> getCommentsByProductId(@PathVariable Long productId) {
        List<Comment> comments = commentService.getCommentsByProductId(productId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CommentRequest commentRequest
    ) {
        User user = userService.findByUsername(userDetails.getUsername());
        Product product = productService.getProductById(commentRequest.getProductId());
        commentService.addComment(user, product, commentRequest.getCommentText(), commentRequest.getRating());
        return ResponseEntity.ok("Comment added successfully");
    }

    @PutMapping("/update/{commentId}")
    public ResponseEntity<String> updateComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody CommentRequest commentRequest
    ) {
        Optional<Comment> commentOptional = commentService.getCommentById(commentId);
        if (!commentOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Comment comment = commentOptional.get();
        User user = userService.findByUsername(userDetails.getUsername());

        // Check if the user is the owner of the comment
        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You are not allowed to update this comment");
        }

        commentService.updateComment(comment, commentRequest.getCommentText(), commentRequest.getRating());
        return ResponseEntity.ok("Comment updated successfully");
    }

    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<String> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long commentId
    ) {
        Optional<Comment> commentOptional = commentService.getCommentById(commentId);
        if (!commentOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Comment comment = commentOptional.get();
        User user = userService.findByUsername(userDetails.getUsername());

        // Check if the user is the owner of the comment
        if (!comment.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You are not allowed to delete this comment");
        }

        commentService.deleteComment(commentId);
        return ResponseEntity.ok("Comment deleted successfully");
    }
}
