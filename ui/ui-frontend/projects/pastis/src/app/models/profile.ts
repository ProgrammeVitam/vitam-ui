import {ProfileCommon} from './profile-common-models';

export interface Profile extends ProfileCommon {
  format: string;
  path: string;
}

export class ProfileModel implements Profile {
  id: string;
  identifier: string;
  name: string;
  description: string;
  status: string;
  tenant: number;
  version: number;
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  format: string;
  path: string;
}
