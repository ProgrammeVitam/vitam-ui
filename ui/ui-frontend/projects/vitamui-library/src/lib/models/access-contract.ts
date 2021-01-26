import {AccessContract as IAccessContract} from 'ui-frontend-common';

export interface AccessContract extends IAccessContract {
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  dataObjectVersion: string[];
  writingPermission: boolean;
  writingRestrictedDesc: boolean;
  accessLog: string;
  ruleFilter: boolean;
  ruleCategoryToFilter: string[];
  originatingAgencies: string[];
  rootUnits: string[];
  excludedRootUnits: string[];
}
