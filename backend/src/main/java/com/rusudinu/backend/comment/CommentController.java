package com.rusudinu.backend.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDTO> addComment(@RequestBody CommentDTO commentDTO) {
        return ResponseEntity.ok(commentService.addComment(commentDTO));
    }

    @GetMapping("/document/{documentId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByDocumentId(@PathVariable Long documentId) {
        return ResponseEntity.ok(commentService.getCommentsByDocumentId(documentId));
    }

    @GetMapping("/{commentId}/verify")
    public ResponseEntity<CommentDTO> verifyComment(@PathVariable Long commentId) {
        return ResponseEntity.ok(commentService.verifyComment(commentId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDTO> updateComment(@PathVariable Long commentId, @RequestBody CommentDTO commentDTO) {
        return ResponseEntity.ok(commentService.updateComment(commentId, commentDTO));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
