import { Id } from '../id.interface';
import { AccessionRegisterDetail } from './accession-register-detail';

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
