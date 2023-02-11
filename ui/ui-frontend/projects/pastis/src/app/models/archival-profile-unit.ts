import {ProfileCommon} from './profile-common-models';

export interface ArchivalProfileUnit extends ProfileCommon {
  controlSchema: string;
  fields: string[];
}

export class ArchivalProfileUnitModel implements ArchivalProfileUnit {
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
  controlSchema: string;
  fields: string[];
}
