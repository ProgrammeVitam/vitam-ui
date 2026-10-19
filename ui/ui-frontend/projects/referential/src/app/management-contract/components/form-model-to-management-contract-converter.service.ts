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
import { Injectable } from '@angular/core';
import {
  ManagementContract,
  PersistentIdentifierPolicy,
  PersistentIdentifierPolicyTypeEnum,
  PersistentIdentifierUsage,
  StorageStrategy,
  VersionRetentionPolicy,
} from 'vitamui-library';
import { Converter } from './converter';
import { PersistentIdentifierPoliciesForm } from './management-contract-to-form-model-converter.service';

/** Value of a management contract form (all the properties are optional, as each form only edits some of them) */
export interface ManagementContractFormValue extends PersistentIdentifierPoliciesForm {
  tenant?: number;
  version?: number;
  name?: string;
  identifier?: string;
  description?: string;
  status?: boolean;
  creationDate?: string;
  lastUpdate?: string;
  activationDate?: string;
  deactivationDate?: string;
  storage?: StorageStrategy;
  versionRetentionPolicy?: any;
}

@Injectable({
  providedIn: 'root',
})
export class FormModelToManagementContractConverterService implements Converter<ManagementContractFormValue, ManagementContract> {
  convert(source: ManagementContractFormValue): ManagementContract {
    const persistentIdentifierPolicyList: PersistentIdentifierPolicy[] = source.persistentIdentifierPolicies
      .filter((policyFormValue) => policyFormValue.policyTypeOption)
      .map((policyFormValue) => {
        const persistentIdentifierUsages: PersistentIdentifierUsage[] = policyFormValue.objectUsagePolicies.map((usageFormValue) => {
          return {
            intermediaryVersion: usageFormValue.intermediaryVersion,
            initialVersion: usageFormValue.initialVersion as unknown as string,
            usageName: usageFormValue.objectUsage,
          };
        });

        return {
          persistentIdentifierPolicyType: policyFormValue.policyTypeOption as PersistentIdentifierPolicyTypeEnum,
          persistentIdentifierUnit: policyFormValue.shouldConcernArchiveUnits,
          persistentIdentifierAuthority: policyFormValue.authority,
          persistentIdentifierUsages: persistentIdentifierUsages,
        };
      });

    const managementContract: ManagementContract = {
      id: null,
      tenant: source.tenant,
      version: source.version,
      name: source.name,
      identifier: source.identifier,
      description: source.description,
      status: source.status ? 'ACTIVE' : 'INACTIVE',
      creationDate: source.creationDate,
      lastUpdate: source.lastUpdate,
      activationDate: source.activationDate,
      deactivationDate: source.deactivationDate,
      storage: source.storage,
      versionRetentionPolicy: this.extractVersionRetentionPolicy(source),
      persistentIdentifierPolicyList: persistentIdentifierPolicyList,
    };

    return managementContract;
  }

  private extractVersionRetentionPolicy(object: any): VersionRetentionPolicy | undefined {
    if (object.versionRetentionPolicy) {
      return {
        initialVersion: object.versionRetentionPolicy.initialVersion,
        intermediaryVersionEnum: object.versionRetentionPolicy.intermediaryVersionEnum,
        usages: new Set(object.versionRetentionPolicy.usages),
      };
    }

    return undefined;
  }
}
