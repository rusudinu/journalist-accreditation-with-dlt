import React, { useEffect, useState } from 'react';
import CommentList from './CommentList';
import CommentForm from './CommentForm';
import { IComment } from '@/bemodel/CommentTypes';
import CommentService from '@/services/CommentService';
import { toast } from 'sonner';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { RefreshCw } from 'lucide-react';
import MarkReviewAsCompleted from '@/components/reviews/MarkReviewAsCompleted';

interface DocumentCommentsProps {
  documentId: number;
}

const DocumentComments: React.FC<DocumentCommentsProps> = ({ documentId }) => {
  const [comments, setComments] = useState<IComment[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchComments = async () => {
    setLoading(true);
    try {
      const fetchedComments = await CommentService.getCommentsByDocumentId(documentId);
      setComments(fetchedComments);
    } catch (error) {
      console.error('Error fetching comments:', error);
      toast.error('Failed to load comments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (documentId) {
      fetchComments();
    }
  }, [documentId]);

  const handleCommentAdded = () => {
    fetchComments();
  };

  const [refreshing, setRefreshing] = useState(false);

  const handleRefresh = async () => {
    setRefreshing(true);
    await fetchComments();
    setRefreshing(false);
    toast.success('Comments refreshed');
  };

  return (
    <div className="mt-6">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-bold">Document Comments</h2>
        <Button 
          variant="outline" 
          size="sm" 
          onClick={handleRefresh} 
          disabled={loading || refreshing}
        >
          <RefreshCw className={`h-4 w-4 mr-2 ${refreshing ? 'animate-spin' : ''}`} />
          Refresh
        </Button>
      </div>
      <Separator className="my-4" />

      {loading ? (
        <p>Loading comments...</p>
      ) : (
        <CommentList comments={comments} />
      )}

      <Separator className="my-4" />

      <CommentForm documentId={documentId} onCommentAdded={handleCommentAdded} />

      <Separator className="my-4" />

      <MarkReviewAsCompleted documentId={documentId} />
    </div>
  );
};

export default DocumentComments;
