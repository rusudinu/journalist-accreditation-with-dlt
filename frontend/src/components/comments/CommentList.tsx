import React from 'react';
import { IComment } from '@/bemodel/CommentTypes';
import { Card } from '@/components/ui/card';
import { Separator } from '@/components/ui/separator';
import { Badge } from '@/components/ui/badge';
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from '@/components/ui/tooltip';

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

  return (
    <div className="space-y-4">
      <h3 className="text-lg font-semibold">Comments</h3>
      {sortedComments.map((comment) => (
        <Card key={comment.id} className="p-4">
          <div className="flex justify-between items-start">
            <div className="font-medium">{comment.author || 'Anonymous'}</div>
            <div className="flex items-center gap-2">
              {comment.isValid !== undefined && (
                <TooltipProvider>
                  <Tooltip>
                    <TooltipTrigger>
                      <Badge variant={comment.isValid ? "success" : "destructive"}>
                        {comment.isValid 
                          ? "Comment signature checked and is valid" 
                          : "Comment signature checked and is invalid"}
                      </Badge>
                    </TooltipTrigger>
                    <TooltipContent>
                      <p>
                        {comment.isValid 
                          ? "This comment's signature has been verified against the blockchain and is valid." 
                          : "This comment's signature does not match what's stored on the blockchain."}
                      </p>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
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
