import {Id} from 'ui-frontend-common';
import {FileType} from './file-type.enum';

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
