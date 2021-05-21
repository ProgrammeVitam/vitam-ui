export interface OperationsResults {
  hits: any;
  results: OperationDetails[];
  facetResults?: any[];
  context?: any;
}

export interface OperationResponse {
  $hits: any;
  $results: OperationDetails[];
  $facetResults?: any[];
  $context?: any;
}

export interface OperationDetails {
  stepStatus: string;
  previousStep: string;
  nextStep: string;
  operationId: string;
  processType: string;
  stepByStep: boolean;
  globalState: string;
  processDate: Date;
}

export interface OperationCategory {
  key: string;
  value: string;
}
