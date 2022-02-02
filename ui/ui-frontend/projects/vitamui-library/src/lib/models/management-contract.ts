import {Id} from 'ui-frontend-common';

export interface ManagementContract extends Id {
  tenant: number;
  version: number;
  name: string;
  identifier: string;
  description: string;
  status: string;
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  storage: StorageStrategy;
}

export interface StorageStrategy {
  UnitStrategy: string,
  ObjectGroupStrategy: string,
  ObjectStrategy: string
}
