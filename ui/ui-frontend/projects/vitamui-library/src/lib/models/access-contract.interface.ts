/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
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
}
export interface AccessContractDisplay extends AccessContract {
  /** front uniquement - calculé une seule fois */
  accessRightType?: AccessRightType;
  originatingAgenciesLabels?: string[];
  ruleCategoryToFilterLabels?: string[];
  ruleCategoryToFilterForTheOtherOriginatingAgenciesLabels?: string[];
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
  if (
    accessContract.everyOriginatingAgency &&
    isEmpty(accessContract.ruleCategoryToFilter) &&
    isEmpty(accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies)
  ) {
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
