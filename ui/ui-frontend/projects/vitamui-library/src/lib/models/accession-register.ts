import {Id} from 'ui-frontend-common';

export interface AccessionRegister extends Id {
  tenant: number;
  version: number;
  originatingAgency: string;
  creationDate: string;
  binaryObjectSize: number;
  archiveUnit: number;
  objectGroup: number;
  binaryObject: number;
}
