import {Id} from 'ui-frontend-common';

export interface Profile extends Id {
  status: string;
  tenant: number;
  version: number;
  identifier: string;
  name: string;
  description: string;
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  format: string;
  path: string;
}

export class ProfileModel implements Profile {
  id: string;
  status: string;
  tenant: number;
  version: number;
  identifier: string;
  name: string;
  description: string;
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  format: string;
  path: string;
}
