import {Id} from 'ui-frontend-common';

export class ContextPermission {
  tenant: string;
  accessContracts: string[];
  ingestContracts: string[];

  constructor(tenant: string, accessContract: string[], ingestContract: string[]) {
    this.tenant = tenant;
    this.accessContracts = accessContract;
    this.ingestContracts = ingestContract;
  }
}

export interface Context extends Id {
  name: string;
  identifier: string;
  status: string;
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  enableControl: string;
  securityProfile: string;
  permissions: ContextPermission[];
}
