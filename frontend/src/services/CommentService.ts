import axios from 'axios';
import { IComment, ICommentFormData } from '@/bemodel/CommentTypes';

const API_URL = import.meta.env.VITE_BACKEND_URL;

export const CommentService = {
  // Get comments for a document
  getCommentsByDocumentId: async (documentId: number): Promise<IComment[]> => {
    try {
      const response = await axios.get(`${API_URL}/api/v1/comments/document/${documentId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching comments:', error);
      throw error;
    }
  },

  // Add a comment to a document
  addComment: async (documentId: number, commentData: ICommentFormData): Promise<IComment> => {
    try {
      // Include documentId in the request body
      const commentWithDocumentId = { ...commentData, documentId };
      const response = await axios.post(`${API_URL}/api/v1/comments`, commentWithDocumentId);
      return response.data;
    } catch (error) {
      console.error('Error adding comment:', error);
      throw error;
    }
  },

  // Verify a comment
  verifyComment: async (commentId: number): Promise<IComment> => {
    try {
      const response = await axios.get(`${API_URL}/api/v1/comments/${commentId}/verify`);
      return response.data.isValid;
    } catch (error) {
      console.error('Error verifying comment:', error);
      throw error;
    }
  },

  checkIfRequestIsValid: async (requestId: string): Promise<boolean> => {
    try {
      const response = await axios.get(`${API_URL}/api/v1/requests/verify/${requestId}`, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    } catch (error) {
      console.error('Error verifying request:', error);
      throw error;
    }
  }
};

export default CommentService;
