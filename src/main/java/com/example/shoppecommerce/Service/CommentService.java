package com.example.shoppecommerce.Service;

import com.example.shoppecommerce.Entity.Comment;
import com.example.shoppecommerce.Entity.Product;
import com.example.shoppecommerce.Entity.User;
import com.example.shoppecommerce.Repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public List<Comment> getCommentsByProductId(Long productId) {
        return commentRepository.findByProductId(productId);
    }

    public void addComment(User user, Product product, String commentText, int rating) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setProduct(product);
        comment.setCommentText(commentText);
        comment.setRating(rating);
        commentRepository.save(comment);
    }

    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    public void updateComment(Comment comment, String commentText, int rating) {
        comment.setCommentText(commentText);
        comment.setRating(rating);
        commentRepository.save(comment);
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
