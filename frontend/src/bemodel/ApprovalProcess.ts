export interface IApprovalProcess {
  id?: number;
  createdDate?: string;
  name?: string;
  description?: string;
  steps?: IApprovalStep[];
}

export interface IApprovalStep {
  id?: number;
  createdDate?: string;
  name?: string;
  description?: string;
  stepOrder?: number;
  minReviewers?: number;
  requiresApproval?: boolean;
  status?: "PENDING" | "IN_PROGRESS" | "APPROVED" | "REJECTED" | "COMPLETED";
  reviews?: IApprovalReview[];
}

export interface IApprovalReview {
  id?: number;
  createdDate?: string;
  comment?: string;
  approved?: boolean;
  completed?: boolean;
  reviewerId?: number;
}

export interface IRequestWithApprovalStatus {
  id?: number;
  createdDate?: string;
  status?: "CREATED" | "VALIDATED" | "APPROVED" | "REJECTED";
  userId?: number;
  userName?: string;
  approvalProcessId?: number;
  approvalProcessName?: string;
  currentStepName?: string;
  currentStepNumber?: number;
  totalSteps?: number;
  progressDisplay?: string; // e.g. "2/3"
}
