import { AccessionRegisterDetail } from './accession-register-detail';
import { Id } from '../id.interface';

export interface AccessionRegisterSummary extends Id {
  tenant: number;
  version: number;
  originatingAgency: string;
  creationDate: string;
  binaryObjectSize: number;
  archiveUnit: number;
  objectGroup: number;
  binaryObject: number;
  accessionRegisterDetails?: AccessionRegisterDetail[];
}
