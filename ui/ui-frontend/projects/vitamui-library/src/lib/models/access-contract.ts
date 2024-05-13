import { IAccessContract } from '../../app/modules';

export interface AccessContract extends IAccessContract {
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  dataObjectVersion: string[];
  writingPermission: boolean;
  writingRestrictedDesc: boolean;
  accessLog: Status;
  ruleFilter: boolean;
  ruleCategoryToFilter: string[];
  originatingAgencies: string[];
  rootUnits: string[];
  excludedRootUnits: string[];
}

export enum Status {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}
