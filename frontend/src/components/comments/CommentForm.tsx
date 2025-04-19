import React, { useState } from 'react';
import { Button } from '@/components/ui/button';
import { ICommentFormData } from '@/bemodel/CommentTypes';
import { toast } from 'sonner';
import CommentService from '@/services/CommentService';
import { useSelector } from 'react-redux';
import { CoreState } from '@/coreSlice';

// Maximum character limit for comments
const MAX_COMMENT_LENGTH = 500;

interface CommentFormProps {
  documentId: number;
  onCommentAdded: () => void;
}

const CommentForm: React.FC<CommentFormProps> = ({ documentId, onCommentAdded }) => {
  const [content, setContent] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const userName = useSelector((state: { core: CoreState }) => state.core.authenticatedUserName);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!content.trim()) {
      toast.error('Please enter a comment');
      return;
    }

    if (content.length > MAX_COMMENT_LENGTH) {
      toast.error(`Comment is too long. Maximum ${MAX_COMMENT_LENGTH} characters allowed.`);
      return;
    }

    setIsSubmitting(true);

    try {
      const commentData: ICommentFormData = {
        content: content.trim(),
        author: userName || 'Anonymous',
        documentId
      };

      await CommentService.addComment(documentId, commentData);

      setContent('');
      toast.success('Comment added successfully');
      onCommentAdded();
    } catch (error) {
      console.error('Error adding comment:', error);
      toast.error('Failed to add comment');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <h3 className="text-lg font-semibold">Add a Comment</h3>
      <div>
        <textarea
          className="w-full p-2 border rounded-md min-h-[100px]"
          placeholder="Write your comment here..."
          value={content}
          onChange={(e) => setContent(e.target.value.slice(0, MAX_COMMENT_LENGTH))}
          disabled={isSubmitting}
          maxLength={MAX_COMMENT_LENGTH}
        />
        <div className="text-sm text-gray-500 mt-1 flex justify-end">
          {content.length}/{MAX_COMMENT_LENGTH} characters
        </div>
      </div>
      <Button type="submit" disabled={isSubmitting || !content.trim()}>
        {isSubmitting ? 'Submitting...' : 'Add Comment'}
      </Button>
    </form>
  );
};

export default CommentForm;
