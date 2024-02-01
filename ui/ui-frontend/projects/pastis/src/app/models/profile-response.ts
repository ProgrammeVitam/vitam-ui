import { FileNode } from './file-node';
import { ProfileDescription } from './profile-description.model';
import { ProfileType } from './profile-type.enum';

export interface ProfileResponse {
  id: string;
  name: string;
  profile: FileNode;
  notice?: ProfileDescription;
  type: ProfileType;
}
