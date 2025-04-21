export interface IComment {
  id?: number;
  createdDate?: string;
  content: string;
  author?: string;
  documentId: number;
  commentHash?: string;
  isValid?: boolean;
}

export interface ICommentFormData {
  content: string;
  author?: string;
  documentId: number;
}
