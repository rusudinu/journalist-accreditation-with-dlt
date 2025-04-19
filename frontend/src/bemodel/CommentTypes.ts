export interface IComment {
  id?: number;
  createdDate?: string;
  content: string;
  author?: string;
  documentId: number;
}

export interface ICommentFormData {
  content: string;
  author?: string;
  documentId: number;
}
