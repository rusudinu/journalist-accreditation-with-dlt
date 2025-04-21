import axios from 'axios';
import { IApprovalReview } from '@/bemodel/ApprovalProcess';

const API_URL = import.meta.env.VITE_BACKEND_URL;

export const ReviewService = {
  // Get reviews for a reviewer
  getReviewsByReviewer: async (reviewerId: string): Promise<IApprovalReview[]> => {
    try {
      const response = await axios.get(`${API_URL}/api/approval-reviews/reviewer/${reviewerId}`);
      return response.data;
    } catch (error) {
      console.error('Error fetching reviews:', error);
      throw error;
    }
  },

  // Get requests for a reviewer
  getRequestsByReviewer: async (reviewerId: string): Promise<unknown[]> => {
    try {
      const response = await axios.get(`${API_URL}/api/approval-reviews/reviewer/${reviewerId}/requests`);
      return response.data;
    } catch (error) {
      console.error('Error fetching requests:', error);
      throw error;
    }
  },

  // Mark a review as completed
  markReviewAsCompleted: async (reviewId: number): Promise<IApprovalReview> => {
    try {
      const response = await axios.put(`${API_URL}/api/approval-reviews/${reviewId}`, {
        completed: true
      });
      return response.data;
    } catch (error) {
      console.error('Error marking review as completed:', error);
      throw error;
    }
  }
};

export default ReviewService;
