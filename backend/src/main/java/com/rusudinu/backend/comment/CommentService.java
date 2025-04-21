package com.rusudinu.backend.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusudinu.backend.distributedStorage.DistributedStorageService;
import com.rusudinu.backend.document.Document;
import com.rusudinu.backend.document.DocumentRepository;
import com.rusudinu.backend.hash.HashService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final DocumentRepository documentRepository;
    private final HashService hashService;
    private final DistributedStorageService distributedStorageService;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public CommentDTO addComment(CommentDTO commentDTO) {
        Document document = documentRepository.findById(commentDTO.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + commentDTO.getDocumentId()));

        Comment comment = commentMapper.toComment(commentDTO);
        comment.setDocument(document);

        // Generate hash for the comment
        String commentJson = objectMapper.writeValueAsString(comment);
        byte[] commentHash = hashService.hashString(commentJson);
        String encodedHash = Base64.getEncoder().encodeToString(commentHash);
        comment.setCommentHash(encodedHash);

        Comment savedComment = commentRepository.save(comment);

        // Store the hash on the blockchain
        // We'll use the document's request ID and append the comment ID to make it unique
        Long requestId = document.getRequest().getId();
        String commentKey = requestId + "-comment-" + savedComment.getId();
        distributedStorageService.persistRegistrySnapshot(Long.valueOf(commentKey), encodedHash);

        CommentDTO savedCommentDTO = commentMapper.toCommentDTO(savedComment);
        savedCommentDTO.setIsValid(true); // New comment is always valid
        return savedCommentDTO;
    }

    public List<CommentDTO> getCommentsByDocumentId(Long documentId) {
        List<Comment> comments = commentRepository.findByDocumentId(documentId);
        return comments.stream()
                .map(this::verifyAndMapComment)
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private CommentDTO verifyAndMapComment(Comment comment) {
        CommentDTO commentDTO = commentMapper.toCommentDTO(comment);

        // Verify the comment hash against the blockchain
        if (comment.getCommentHash() != null) {
            Long requestId = comment.getDocument().getRequest().getId();
            String commentKey = requestId + "-comment-" + comment.getId();
            String blockchainHash = distributedStorageService.getRegistrySnapshotHashByRequestId(Long.valueOf(commentKey));

            boolean isValid = comment.getCommentHash().equals(blockchainHash);
            commentDTO.setIsValid(isValid);
        } else {
            commentDTO.setIsValid(false);
        }

        return commentDTO;
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        existingComment.setContent(commentDTO.getContent());
        existingComment.setAuthor(commentDTO.getAuthor());

        // Generate new hash for the updated comment
        String commentJson;
        try {
            commentJson = objectMapper.writeValueAsString(existingComment);
            byte[] commentHash = hashService.hashString(commentJson);
            String encodedHash = Base64.getEncoder().encodeToString(commentHash);
            existingComment.setCommentHash(encodedHash);

            // Update the hash on the blockchain
            Long requestId = existingComment.getDocument().getRequest().getId();
            String commentKey = requestId + "-comment-" + existingComment.getId();
            distributedStorageService.persistRegistrySnapshot(Long.valueOf(commentKey), encodedHash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash comment", e);
        }

        Comment updatedComment = commentRepository.save(existingComment);
        CommentDTO updatedCommentDTO = commentMapper.toCommentDTO(updatedComment);
        updatedCommentDTO.setIsValid(true); // Updated comment is always valid
        return updatedCommentDTO;
    }

    public CommentDTO verifyComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        return verifyAndMapComment(comment);
    }
}
