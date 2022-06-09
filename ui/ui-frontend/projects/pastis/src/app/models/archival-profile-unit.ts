import {Id} from 'ui-frontend-common';

export interface ArchivalProfileUnit extends Id {
  tenant: number;
  version: number;
  identifier: string;
  name: string;
  description: string;
  status: string;
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  controlSchema: string;
  fields: string[];
}

export class ArchivalProfileUnitModel implements ArchivalProfileUnit {
  id: string;
  tenant: number;
  version: number;
  identifier: string;
  name: string;
  description: string;
  status: string;
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  controlSchema: string;
  fields: string[];
}