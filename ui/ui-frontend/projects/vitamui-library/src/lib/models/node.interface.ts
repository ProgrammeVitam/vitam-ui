import { FileType, Id } from 'ui-frontend-common';

export interface Node extends Id {
  label: string;
  type: FileType;
  children: Node[];
  ingestContractIdentifier: string;
  vitamId: string;
  parents: Node[];
  checked: boolean;
  disabledChild?: boolean;
  disabled?: boolean;
  // OriginatingAgencyArchiveUnitIdentifier: string[];
}
