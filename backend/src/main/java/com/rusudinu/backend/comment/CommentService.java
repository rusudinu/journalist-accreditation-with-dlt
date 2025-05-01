package com.rusudinu.backend.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusudinu.backend.distributedStorage.DistributedStorageService;
import com.rusudinu.backend.document.Document;
import com.rusudinu.backend.document.DocumentRepository;
import com.rusudinu.backend.hash.HashService;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final DocumentRepository documentRepository;
    private final HashService hashService;
    private final DistributedStorageService distributedStorageService;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public CommentDTO addComment(CommentDTO commentDTO) {
        log.info("adding new comment");
        Document document = documentRepository.findById(commentDTO.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + commentDTO.getDocumentId()));

        Comment comment = commentMapper.toComment(commentDTO);
        comment.setDocument(document);

        // Get the request ID
        Long requestId = document.getRequest().getId();

        // Generate hash for the comment (user + comment)
        String userCommentData = comment.getAuthor() + comment.getContent();

        // Get the previous hash from the blockchain
        String previousHash = distributedStorageService.getCommentHashByRequestId(requestId);

        // If there's a previous hash, include it in the new hash calculation
        String hashData;
        if (previousHash != null && !previousHash.isEmpty()) {
            hashData = userCommentData + previousHash;
            log.info("[COMMENT] ====== Creating chained hash with previous hash: {} ======", previousHash);
        } else {
            hashData = userCommentData;
            log.info("[COMMENT] ====== Creating initial hash without previous hash ======");
        }

        // Generate the hash
        log.info("[COMMENT] Generating hash for data: {}", hashData);
        byte[] commentHash = hashService.hashString(hashData);
        String encodedHash = Base64.getEncoder().encodeToString(commentHash);
        comment.setCommentHash(encodedHash);

        log.info("[COMMENT] ====== Generated comment hash: {} ======", encodedHash);

        Comment savedComment = commentRepository.save(comment);
        log.info("[COMMENT] Saved comment to database with ID: {}", savedComment.getId());

        // Store the hash on the blockchain
        log.info("[COMMENT] Calling distributedStorageService.persistCommentHash with requestId: {} and hash: {}", requestId, encodedHash);
        distributedStorageService.persistCommentHash(requestId, encodedHash);
        log.info("[COMMENT] ====== Successfully persisted comment hash for request ID: {} ======", requestId);

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
        log.info("[COMMENT] ====== START: Verifying comment with ID: {} ======", comment.getId());
        CommentDTO commentDTO = commentMapper.toCommentDTO(comment);

        // Verify the comment hash against the blockchain
        if (comment.getCommentHash() != null) {
            Long requestId = comment.getDocument().getRequest().getId();
            log.info("[COMMENT] Getting blockchain hash for request ID: {}", requestId);
            String blockchainHash = distributedStorageService.getCommentHashByRequestId(requestId);

            log.info("[COMMENT] Verifying comment. Blockchain Hash: {}", blockchainHash);
            log.info("[COMMENT] Comment Hash from DB: {}", comment.getCommentHash());

            // Check if blockchainHash is not null or empty before comparing
            boolean isValid = blockchainHash != null && !blockchainHash.isEmpty();
            log.info("[COMMENT] Is blockchain hash present? {}", isValid);

            // If the blockchain has a hash, check if it's part of the chain
            if (isValid) {
                // The latest hash in the blockchain should contain this comment's hash
                // or be equal to it if it's the latest comment
                boolean isLatestComment = blockchainHash.equals(comment.getCommentHash());
                boolean isPartOfChain = false;

                if (!isLatestComment) {
                    isPartOfChain = validateCommentInChain(comment, blockchainHash);
                    log.info("[COMMENT] Comment is not the latest. Is it part of the chain? {}", isPartOfChain);
                } else {
                    log.info("[COMMENT] Comment is the latest in the chain");

                    // Check if this is the only comment for the document
                    List<Comment> allComments = commentRepository.findByDocumentId(comment.getDocument().getId());
                    if (allComments.size() == 1) {
                        log.info("[COMMENT] This is the only comment for document ID: {}, re-hashing to verify integrity", comment.getDocument().getId());

                        // Re-hash the comment
                        String userCommentData = comment.getAuthor() + comment.getContent();
                        try {
                            byte[] commentHash = hashService.hashString(userCommentData);
                            String encodedHash = Base64.getEncoder().encodeToString(commentHash);

                            // Compare the re-hashed value with the stored hash
                            boolean hashesMatch = encodedHash.equals(comment.getCommentHash());
                            log.info("[COMMENT] Re-hashed comment. Original hash: {}, New hash: {}, Match: {}", 
                                    comment.getCommentHash(), encodedHash, hashesMatch);

                            // Update validity based on hash comparison
                            isLatestComment = isLatestComment && hashesMatch;

                            if (!hashesMatch) {
                                log.warn("[COMMENT] Hash mismatch detected for the only comment in document ID: {}", comment.getDocument().getId());
                            }
                        } catch (Exception e) {
                            log.error("[COMMENT] Error re-hashing comment", e);
                            isLatestComment = false;
                        }
                    }
                }

                isValid = isLatestComment || isPartOfChain;
            }

            log.info("[COMMENT] ====== RESULT: Comment is valid? {} ======", isValid);
            commentDTO.setIsValid(isValid);
        } else {
            log.info("[COMMENT] ====== RESULT: Comment has no hash, marking as invalid ======");
            commentDTO.setIsValid(false);
        }

        return commentDTO;
    }

    /**
     * Validates if a comment is part of the comment chain by reconstructing the hash chain
     * and checking if it leads to the current blockchain hash.
     */
    private boolean validateCommentInChain(Comment comment, String currentBlockchainHash) {
        log.info("[COMMENT] ====== START: Validating if comment is part of the chain ======");
        log.info("[COMMENT] Comment ID: {}", comment.getId());
        log.info("[COMMENT] Comment hash: {}", comment.getCommentHash());
        log.info("[COMMENT] Current blockchain hash: {}", currentBlockchainHash);

        // Retrieve all comments for the document
        List<Comment> allComments = commentRepository.findByDocumentId(comment.getDocument().getId());

        // Sort comments by creation date to ensure chronological order
        allComments.sort(Comparator.comparing(Comment::getCreatedDate));

        log.info("[COMMENT] Found {} comments for document ID: {}", allComments.size(), comment.getDocument().getId());

        // Reconstruct the hash chain
        String reconstructedHash = null;
        boolean foundTargetComment = false;
        boolean isPartOfChain = false;

        for (Comment c : allComments) {
            // Generate hash for the comment (user + comment)
            String userCommentData = c.getAuthor() + c.getContent();

            // If there's a previous hash, include it in the new hash calculation
            String hashData;
            if (reconstructedHash != null) {
                hashData = userCommentData + reconstructedHash;
                log.info("[COMMENT] Creating chained hash with previous hash: {}", reconstructedHash);
            } else {
                hashData = userCommentData;
                log.info("[COMMENT] Creating initial hash without previous hash");
            }

            // Generate the hash
            try {
                log.info("[COMMENT] Generating hash for data: {}", hashData);
                byte[] commentHash = hashService.hashString(hashData);
                String encodedHash = Base64.getEncoder().encodeToString(commentHash);
                reconstructedHash = encodedHash;

                log.info("[COMMENT] Generated comment hash: {}", encodedHash);

                // Check if this is our target comment
                if (c.getId().equals(comment.getId())) {
                    foundTargetComment = true;
                    // Verify that the stored hash matches the reconstructed hash
                    isPartOfChain = encodedHash.equals(c.getCommentHash());
                    log.info("[COMMENT] Found target comment. Stored hash: {}, Reconstructed hash: {}, Match: {}", 
                            c.getCommentHash(), encodedHash, isPartOfChain);

                    // If the hash doesn't match, no need to continue
                    if (!isPartOfChain) {
                        break;
                    }
                }

                // If we've already found and validated our target comment,
                // check if the final reconstructed hash matches the blockchain hash
                if (foundTargetComment && encodedHash.equals(currentBlockchainHash)) {
                    isPartOfChain = true;
                    log.info("[COMMENT] Final reconstructed hash matches blockchain hash");
                    break;
                }
            } catch (Exception e) {
                log.error("[COMMENT] Error generating hash", e);
                isPartOfChain = false;
                break;
            }
        }

        log.info("[COMMENT] ====== RESULT: Comment is part of the chain? {} ======", isPartOfChain);
        return isPartOfChain;
    }

    public void deleteComment(Long commentId) {
        // Note: In a real implementation, you might want to handle deletion differently
        // since deleting a comment would break the hash chain
        commentRepository.deleteById(commentId);
    }

    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        log.info("[COMMENT] ====== START: Updating comment with ID: {} ======", commentId);
        // Note: In a real implementation, you might want to handle updates differently
        // since updating a comment would break the hash chain
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        log.info("[COMMENT] Found existing comment with ID: {}", existingComment.getId());
        existingComment.setContent(commentDTO.getContent());
        existingComment.setAuthor(commentDTO.getAuthor());
        log.info("[COMMENT] Updated comment content and author");

        // Get the request ID
        Long requestId = existingComment.getDocument().getRequest().getId();
        log.info("[COMMENT] Request ID for this comment: {}", requestId);

        // Generate hash for the updated comment (user + comment)
        String userCommentData = existingComment.getAuthor() + existingComment.getContent();
        log.info("[COMMENT] User + comment data: {}", userCommentData);

        // Get the previous hash from the blockchain
        log.info("[COMMENT] Getting previous hash from blockchain for request ID: {}", requestId);
        String previousHash = distributedStorageService.getCommentHashByRequestId(requestId);
        log.info("[COMMENT] Previous hash from blockchain: {}", previousHash);

        // Generate the new hash including the previous hash
        String hashData = userCommentData + previousHash;
        log.info("[COMMENT] ====== Creating updated hash with previous hash: {} ======", previousHash);
        log.info("[COMMENT] Hash data: {}", hashData);

        try {
            log.info("[COMMENT] Generating hash for data");
            byte[] commentHash = hashService.hashString(hashData);
            String encodedHash = Base64.getEncoder().encodeToString(commentHash);
            existingComment.setCommentHash(encodedHash);
            log.info("[COMMENT] ====== Generated updated comment hash: {} ======", encodedHash);

            // Store the hash on the blockchain
            log.info("[COMMENT] Calling distributedStorageService.persistCommentHash with requestId: {} and hash: {}", requestId, encodedHash);
            distributedStorageService.persistCommentHash(requestId, encodedHash);
            log.info("[COMMENT] ====== Successfully persisted updated comment hash for request ID: {} ======", requestId);
        } catch (Exception e) {
            log.error("[COMMENT] ====== ERROR: Failed to hash comment ======", e);
            throw new RuntimeException("Failed to hash comment", e);
        }

        log.info("[COMMENT] Saving updated comment to database");
        Comment updatedComment = commentRepository.save(existingComment);
        log.info("[COMMENT] Comment saved with ID: {}", updatedComment.getId());

        CommentDTO updatedCommentDTO = commentMapper.toCommentDTO(updatedComment);
        updatedCommentDTO.setIsValid(true); // Updated comment is always valid
        log.info("[COMMENT] ====== DONE: Successfully updated comment with ID: {} ======", commentId);
        return updatedCommentDTO;
    }

    public CommentDTO verifyComment(Long commentId) {
        log.info("[COMMENT] ====== START: Verifying specific comment with ID: {} ======", commentId);

        try {
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

            log.info("[COMMENT] Found comment with ID: {}. Calling verifyAndMapComment", comment.getId());
            CommentDTO result = verifyAndMapComment(comment);

            log.info("[COMMENT] ====== DONE: Verification completed for comment ID: {}. Is valid: {} ======", 
                    commentId, result.getIsValid());

            return result;
        } catch (Exception e) {
            log.error("[COMMENT] ====== ERROR: Failed to verify comment with ID: {} ======", commentId, e);
            throw e;
        }
    }
}
