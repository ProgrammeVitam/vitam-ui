import { Id } from '../../app/modules';

export interface Agency extends Id {
  tenant: number;
  version: number;
  name: string;
  identifier: string;
  description: string;
}
