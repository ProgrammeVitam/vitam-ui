import {Id} from 'ui-frontend-common';

export interface Agency extends Id {
  tenant: number;
  version: number;
  name: string;
  identifier: string;
  description: string;
}
