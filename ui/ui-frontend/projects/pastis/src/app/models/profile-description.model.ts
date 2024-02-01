import { ArchivalProfileUnit } from './archival-profile-unit';
import { Profile } from './profile';

export interface ProfileDescription extends Partial<Profile>, Partial<ArchivalProfileUnit> {
  type: string;
  isEditable?: boolean;
}

export class ProfileDescriptionModel implements ProfileDescription {
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
  type: string;
  controlSchema: string;
  fields: string[];
  format: string;
  path: string;
}
