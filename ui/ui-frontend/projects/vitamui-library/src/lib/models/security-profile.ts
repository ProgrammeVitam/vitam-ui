import { Id } from '../../app/modules';

export interface SecurityProfile extends Id {
  name: string;
  identifier: string;
  fullAccess: boolean;
  permissions: string[];
}
