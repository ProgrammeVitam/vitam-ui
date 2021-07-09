import {Id} from 'ui-frontend-common';

export interface AccessionRegisters extends Id {
  tenant: number;
  version: number;
  originatingAgency: string;
}
