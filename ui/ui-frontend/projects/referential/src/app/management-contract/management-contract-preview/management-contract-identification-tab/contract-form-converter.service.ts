/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import { Injectable } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { ManagementContract, PersistentIdentifierPolicy, PersistentIdentifierUsage, VersionRetentionPolicy } from 'ui-frontend-common';

@Injectable({
  providedIn: 'root',
})
export class ContractFormConverterService {
  constructor() {}

  // Convertir le formulaire en objet ManagementContract
  convertToManagementContract(form: FormGroup): ManagementContract {
    const contractFormValue = form.value;

    const persistentIdentifierPolicyList: PersistentIdentifierPolicy[] = contractFormValue.persistentIdentifiers
      .filter((policyFormValue: any) => policyFormValue.policyTypeOption)
      .map((policyFormValue: any) => {
        const persistentIdentifierUsages: PersistentIdentifierUsage[] = policyFormValue.objectUsagePolicies.map((usageFormValue: any) => {
          return {
            intermediaryVersion: usageFormValue.intermediaryVersion,
            initialVersion: usageFormValue.initialVersion,
            usageName: usageFormValue.objectUsage,
          };
        });

        return {
          persistentIdentifierPolicyType: policyFormValue.policyTypeOption,
          persistentIdentifierUnit: policyFormValue.shouldConcernArchiveUnits,
          persistentIdentifierAuthority: policyFormValue.authority,
          persistentIdentifierUsages: persistentIdentifierUsages,
        };
      });

    const managementContract: ManagementContract = {
      // Ajoutez les propriétés nécessaires ici
      id: null,
      tenant: contractFormValue.tenant,
      version: contractFormValue.version,
      name: contractFormValue.name,
      identifier: contractFormValue.identifier,
      description: contractFormValue.description,
      status: contractFormValue.status,
      creationDate: contractFormValue.creationDate,
      lastUpdate: contractFormValue.lastUpdate,
      activationDate: contractFormValue.activationDate,
      deactivationDate: contractFormValue.deactivationDate,
      storage: contractFormValue.storage,
      versionRetentionPolicy: this.extractVersionRetentionPolicy(contractFormValue),
      persistentIdentifierPolicyList: persistentIdentifierPolicyList,
    };

    return managementContract;
  }

  private extractVersionRetentionPolicy(contractFormValue: any): VersionRetentionPolicy | undefined {
    if (contractFormValue.versionRetentionPolicy) {
      return {
        initialVersion: contractFormValue.versionRetentionPolicy.initialVersion,
        intermediaryVersionEnum: contractFormValue.versionRetentionPolicy.intermediaryVersionEnum,
        usages: new Set(contractFormValue.versionRetentionPolicy.usages),
      };
    }

    return undefined;
  }
}
