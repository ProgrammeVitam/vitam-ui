import { Id } from '../../app/modules';
import { isEmpty } from 'underscore';

export interface AccessContract extends Id {
  creationDate: string;
  lastUpdate: string;
  activationDate: string;
  deactivationDate: string;
  dataObjectVersion: string[];
  writingPermission: boolean;
  writingRestrictedDesc: boolean;
  accessLog: Status;
  ruleFilter: boolean;
  rootUnits: string[];
  excludedRootUnits: string[];

  tenant: number;
  version: number;
  name: string;
  identifier: string;
  description: string;
  status: string;
  everyDataObjectVersion: boolean;

  /** tous les services producteurs sont impliqués - signifie que originatingAgencies est  vide. */
  everyOriginatingAgency: boolean;
  /** les services producteurs impliqués */
  originatingAgencies: string[];
  /** les règles de gestion à appliquer pour les services producteurs sélectionné */
  ruleCategoryToFilter: string[];
  /** les règles de gestion à appliquer pour tous les autres services producteurs */
  ruleCategoryToFilterForTheOtherOriginatingAgencies: string[];
  /** authoriser l'acces à tous les plan de classement */
  doNotFilterFilingSchemes: boolean;

  /** front uniquement - calculé une seule fois */
  accessRightType?: AccessRightType;
}

export enum Status {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
}

export enum AccessRightType {
  ACCESS_FULL = 'ACCESS_FULL',
  ACCESS_BY_PRODUCERS = 'ACCESS_BY_PRODUCERS',
  ACCESS_BY_EXPIRED_MANAGEMENT_RULES = 'ACCESS_BY_EXPIRED_MANAGEMENT_RULES',
  ACCESS_BY_PRODUCERS_OR_EXPIRED_MANAGEMENT_RULES = 'ACCESS_BY_PRODUCERS_OR_EXPIRED_MANAGEMENT_RULES',
  ACCESS_BY_PRODUCERS_AND_EXPIRED_MANAGEMENT_RULES = 'ACCESS_BY_PRODUCERS_AND_EXPIRED_MANAGEMENT_RULES',
}

export function accessRightTypeOf(accessContract: AccessContract): AccessRightType {
  if (accessContract.everyOriginatingAgency) {
    return AccessRightType.ACCESS_FULL;
  }
  if (
    !isEmpty(accessContract.originatingAgencies) &&
    isEmpty(accessContract.ruleCategoryToFilter) &&
    isEmpty(accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies)
  ) {
    return AccessRightType.ACCESS_BY_PRODUCERS;
  }
  if (
    isEmpty(accessContract.originatingAgencies) &&
    !isEmpty(accessContract.ruleCategoryToFilter) &&
    isEmpty(accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies)
  ) {
    return AccessRightType.ACCESS_BY_EXPIRED_MANAGEMENT_RULES;
  }
  if (
    !isEmpty(accessContract.originatingAgencies) &&
    isEmpty(accessContract.ruleCategoryToFilter) &&
    !isEmpty(accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies)
  ) {
    return AccessRightType.ACCESS_BY_PRODUCERS_OR_EXPIRED_MANAGEMENT_RULES;
  }
  if (
    !isEmpty(accessContract.originatingAgencies) &&
    !isEmpty(accessContract.ruleCategoryToFilter) &&
    isEmpty(accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies)
  ) {
    return AccessRightType.ACCESS_BY_PRODUCERS_AND_EXPIRED_MANAGEMENT_RULES;
  }
}
