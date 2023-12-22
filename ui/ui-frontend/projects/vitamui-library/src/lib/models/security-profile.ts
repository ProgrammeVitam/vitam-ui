import {Id} from 'ui-frontend-common';

export interface SecurityProfile extends Id {
  name: string;
  identifier: string;
  fullAccess: boolean;
  permissions: string[];
}
