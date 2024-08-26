import { ArchivalProfileUnit } from './archival-profile-unit';
import { Profile } from './profile';
import { ProfileVersion } from './profile-version.enum';
import { ProfileType } from './profile-type.enum';

export interface ProfileDescription extends Partial<Profile>, Partial<ArchivalProfileUnit> {
  type: ProfileType;
  sedaVersion: ProfileVersion;
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
  type: ProfileType;
  sedaVersion: ProfileVersion;
  controlSchema: string;
  fields: string[];
  format: string;
  path: string;
}
