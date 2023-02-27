import { FileNode } from './file-node';
import { ProfileDescription } from './profile-description.model';

export type ProfileType = 'PA' | 'PUA';

export enum ProfileMode {
  PA = 'PA',
  PUA = 'PUA',
}

export interface ProfileResponse {
  id: string;
  name: string;
  profile: FileNode;
  notice?: ProfileDescription;
  type: ProfileType;
}
