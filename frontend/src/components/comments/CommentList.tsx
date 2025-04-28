import React from 'react';
import { IComment } from '@/bemodel/CommentTypes';
import { Card } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';

interface CommentListProps {
  comments: IComment[];
}

const CommentList: React.FC<CommentListProps> = ({ comments }) => {
  if (!comments || comments.length === 0) {
    return <p className="text-gray-500 italic">No comments yet.</p>;
  }

  // Sort comments by date, newest first
  const sortedComments = [...comments].sort((a, b) => {
    const dateA = a.createdDate ? new Date(a.createdDate).getTime() : 0;
    const dateB = b.createdDate ? new Date(b.createdDate).getTime() : 0;
    return dateB - dateA;
  });

  // Check if the last comment is valid
  const lastComment = sortedComments.length > 0 ? sortedComments[0] : null;
  const lastCommentValid = lastComment?.isValid === true;

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold">Comments</h3>
      {lastCommentValid && (
        <Badge variant="secondary" className="mb-2">
          Comments signatures checked and are valid
        </Badge>
      )}
      {sortedComments.map((comment, index) => (
        <Card key={comment.id} className="p-4">
          <div className="flex justify-between items-start">
            <div className="font-medium">{comment.author || 'Anonymous'}</div>
            <div className="flex items-center gap-2">
              {index === 0 && (
                <Badge 
                  variant={
                    comment.isValid === undefined 
                      ? "outline" 
                      : comment.isValid 
                        ? "secondary" 
                        : "destructive"
                  }
                >
                  {comment.isValid === undefined 
                    ? "Comment signature not verified" 
                    : comment.isValid 
                      ? "Comment signature checked and is valid" 
                      : "Comment signature checked and is invalid"}
                </Badge>
              )}
              <div className="text-sm text-gray-500">
                {new Date(comment.createdDate || '').toLocaleString()}
              </div>
            </div>
          </div>
          <Separator className="my-2" />
          <div className="whitespace-pre-wrap">{comment.content}</div>
        </Card>
      ))}
    </div>
  );
};

export default CommentList;
