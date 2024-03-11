import { Id } from '../id.interface';

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
