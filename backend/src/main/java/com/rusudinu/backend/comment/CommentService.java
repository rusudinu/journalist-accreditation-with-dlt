package com.rusudinu.backend.comment;

import com.rusudinu.backend.document.Document;
import com.rusudinu.backend.document.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final DocumentRepository documentRepository;

    public CommentDTO addComment(CommentDTO commentDTO) {
        Document document = documentRepository.findById(commentDTO.getDocumentId())
                .orElseThrow(() -> new RuntimeException("Document not found with id: " + commentDTO.getDocumentId()));
        
        Comment comment = commentMapper.toComment(commentDTO);
        comment.setDocument(document);
        
        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDTO(savedComment);
    }

    public List<CommentDTO> getCommentsByDocumentId(Long documentId) {
        List<Comment> comments = commentRepository.findByDocumentId(documentId);
        return comments.stream()
                .map(commentMapper::toCommentDTO)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public CommentDTO updateComment(Long commentId, CommentDTO commentDTO) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        
        existingComment.setContent(commentDTO.getContent());
        existingComment.setAuthor(commentDTO.getAuthor());
        
        Comment updatedComment = commentRepository.save(existingComment);
        return commentMapper.toCommentDTO(updatedComment);
    }
}
