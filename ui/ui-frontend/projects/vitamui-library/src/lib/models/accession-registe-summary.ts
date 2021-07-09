import {Id} from 'ui-frontend-common';
import {AccessionRegisterDetail} from './accession-registers-detail';

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
