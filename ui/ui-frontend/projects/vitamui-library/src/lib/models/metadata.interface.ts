export interface MetadataPermission {
  name: string;
  displayed: boolean;
  editable: boolean;
  indexedByUser: boolean;
}

export interface Metadata {
  title: string;
  description: string;
  documentType: string;
  originatingAgencyArchiveUnitIdentifier: string;
  systemId: string;
  status: string;
  dataObjectVersion: string;
  tags: Array<{ Key: string[], Value: string[] }>;
  vtags: Array<{ Key: string[], Value: string[] }>;
  unitups?: string[];
  allunitups?: string[];
  mimeType?: string;
  objectId: string;
  unitId: string;
  version: string;
  descriptionLevel: string;
  unitType: string;

  // Date
  createdDate: Date;
  startDate: Date;
  endDate: Date;
  receivedDate: Date;

  // Technical
  originatingAgencyIdentifier: string;
  submissionAgencyIdentifier: string;
  fileInfo: string;
  size: string;
  messageDigest: string;
  filename: string;

  // Lifecycle
  finalAction: string;
  rule: string;
  ruleStartDate: Date;

}
