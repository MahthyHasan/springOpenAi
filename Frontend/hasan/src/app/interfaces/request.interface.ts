export interface IRequestResponse {
  id: string;
  unStructuredData: string;
  structuredData?:string;
  status: 'Pending' | 'Processing' | 'Completed' | 'Failed';
  errorMessage?: string;
  createdAt: string;
  updatedAt: string;
}
