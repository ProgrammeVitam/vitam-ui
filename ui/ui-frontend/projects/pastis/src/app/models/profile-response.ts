import {FileNode} from './file-node';
import { ProfileDescription } from './profile-description.model';


export interface ProfileResponse {
  id: string;
  name: string;
  profile: FileNode;
  notice?: ProfileDescription;
  type: string;
}
