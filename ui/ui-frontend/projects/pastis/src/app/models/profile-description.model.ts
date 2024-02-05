import { Id } from 'ui-frontend-common';

export interface ProfileDescription extends Id {
  identifier: string;
  name: string;
  description?: string;
  status?: string;
  creationDate: string;
  lastUpdate: string;
  type: string;
  activationDate?: string;
  deactivationDate?: string;
  controlSchema?: string;
  tenant?: number;
  version?: number;
  fields?: string[];
  path?: string;
  format?: string;
  isEditable?: boolean;
}

export class ProfileDescriptionModel implements ProfileDescription {
  id: string;
  identifier: string;
  name: string;
  description: string;
  status: string;
  creationDate: string;
  lastUpdate: string;
  type: string;
  activationDate: string;
  deactivationDate: string;
  controlSchema: string;
  tenant: number;
  version: number;
  fields: string[];
  path: string;
  format: string;
}
