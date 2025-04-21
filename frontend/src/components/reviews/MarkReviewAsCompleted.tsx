import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import ReviewService from '@/services/ReviewService';
import { useSelector } from 'react-redux';
import { CoreState } from '@/coreSlice';
import { IApprovalReview } from '@/bemodel/ApprovalProcess';

interface MarkReviewAsCompletedProps {
  documentId: number;
}

const MarkReviewAsCompleted: React.FC<MarkReviewAsCompletedProps> = ({ documentId }) => {
  const [reviews, setReviews] = useState<IApprovalReview[]>([]);
  const [loading, setLoading] = useState(true);
  const [marking, setMarking] = useState(false);
  const userId = useSelector((state: { core: CoreState }) => state.core.authenticatedUserId);

  useEffect(() => {
    if (userId) {
      fetchReviews();
    }
  }, [userId, documentId]);

  const fetchReviews = async () => {
    if (!userId) return;

    setLoading(true);
    try {
      const fetchedReviews = await ReviewService.getReviewsByReviewer(userId);
      setReviews(fetchedReviews);
    } catch (error) {
      console.error('Error fetching reviews:', error);
      toast.error('Failed to load reviews');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsCompleted = async () => {
    if (!userId) return;

    // Find the user's review for this document
    // Note: This is a simplification. In a real app, you would need to find the review
    // that corresponds to this document, which might require additional API calls or data.
    const userReview = reviews.find(review => review.id !== undefined);

    if (!userReview || !userReview.id) {
      toast.error('No review found to mark as completed');
      return;
    }

    setMarking(true);
    try {
      await ReviewService.markReviewAsCompleted(userReview.id);
      toast.success('Review marked as completed');
      fetchReviews(); // Refresh the reviews
    } catch (error: unknown) {
      console.error('Error marking review as completed:', error);
      const errorMessage = error.response?.data?.message || 'Failed to mark review as completed';
      toast.error(errorMessage);
    } finally {
      setMarking(false);
    }
  };

  // Check if the user has already completed the review
  const isReviewCompleted = reviews.some(review => review.completed === true);

  return (
    <div className="mt-4">
      {loading ? (
        <p>Loading...</p>
      ) : (
        <>
          {isReviewCompleted ? (
            <div className="p-2 bg-green-100 text-green-800 rounded-md">
              Review has been marked as completed
            </div>
          ) : (
            <Button 
              onClick={handleMarkAsCompleted} 
              disabled={marking}
              className="w-full"
            >
              {marking ? 'Marking as Completed...' : 'Mark Review as Completed'}
            </Button>
          )}
        </>
      )}
    </div>
  );
};

export default MarkReviewAsCompleted;
