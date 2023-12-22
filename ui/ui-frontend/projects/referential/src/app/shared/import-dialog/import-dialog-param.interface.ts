import { FileTypes } from 'projects/vitamui-library/src/lib/models/file-types.enum';

export interface ImportDialogParam {
  title: string;
  subtitle: string;
  fileFormatDetailInfo?: string;
  allowedFiles: FileTypes[];
  referential: ReferentialTypes;
  successMessage: string;
  errorMessage?: string;
  iconMessage: string;
}

export enum ReferentialTypes {
  AGENCY = 'agency',
  ACCESS_CONTRACT = 'accesscontract',
  INGEST_CONTRACT = 'ingestcontract',
  FILE_FORMAT = 'fileformat',
  ONTOLOGY = 'ontology',
  RULE = 'rule'
}

export interface ImportError {
  line: number;
  column: string;
  error: string;
  data: string;
}
