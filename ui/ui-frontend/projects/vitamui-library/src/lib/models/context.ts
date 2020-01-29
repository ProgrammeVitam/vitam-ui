import { Id } from 'ui-frontend-common';

export class ContextPermission {
  tenant: string;
  accessContracts: string[];
  ingestContracts: string[];

  constructor() {}
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
