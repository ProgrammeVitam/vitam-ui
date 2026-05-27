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
import { Injectable, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  ManagementContract,
  PersistentIdentifierPolicy,
  PersistentIdentifierPolicyTypeEnum,
  PersistentIdentifierUsage,
} from '../../../../../vitamui-library/src/app/modules/models/managementContract/management-contract.interface';
import { ManagementContractValidators } from '../validators/management-contract-validators';
import { Converter } from './converter';

@Injectable({
  providedIn: 'root',
})
export class ManagementContractToFormGroupConverterService implements Converter<ManagementContract, FormGroup> {
  private formBuilder = inject(FormBuilder);

  convert(source: ManagementContract): FormGroup {
    return this.formBuilder.group({
      persistentIdentifierPolicies: this.formBuilder.array(
        (source.persistentIdentifierPolicyList || [this.getDefaultPersistentIdentifierPolicy()]).map((policy) =>
          this.buildPolicyGroup(policy),
        ),
      ),
    });
  }

  getDefaultPersistentIdentifierPolicy(): PersistentIdentifierPolicy {
    return {
      persistentIdentifierPolicyType: '' as PersistentIdentifierPolicyTypeEnum, // TODO: should we include NONE in persistentIdType ?
      persistentIdentifierAuthority: '',
      persistentIdentifierUnit: false,
      persistentIdentifierUsages: [],
    };
  }

  getDefaultManagementContractForm(): FormGroup {
    const managementContract: ManagementContract = {
      id: null,
      tenant: -1,
      version: -1,
      name: null,
      identifier: null,
      description: null,
      status: null,
      creationDate: null,
      lastUpdate: null,
      activationDate: null,
      deactivationDate: null,
      storage: null,
      versionRetentionPolicy: null,
      persistentIdentifierPolicyList: [this.getDefaultPersistentIdentifierPolicy()],
    };
    return this.convert(managementContract);
  }

  private buildPolicyGroup(policy: PersistentIdentifierPolicy): FormGroup {
    return this.formBuilder.group(
      {
        policyTypeOption: [policy.persistentIdentifierPolicyType],
        authority: [policy.persistentIdentifierAuthority, [Validators.required, ManagementContractValidators.authorityValidator]],
        shouldConcernArchiveUnits: [policy.persistentIdentifierUnit],
        shouldConcernObjects: [Boolean(policy.persistentIdentifierUsages.length)],
        objectUsagePolicies: this.formBuilder.array(
          policy.persistentIdentifierUsages.map((objectUsagePolicy) => this.buildObjectUsageGroup(objectUsagePolicy)),
        ),
      },
      { validators: [ManagementContractValidators.persistentIdentifierPolicyValidator] },
    );
  }

  private buildObjectUsageGroup(objectUsagePolicy: PersistentIdentifierUsage): FormGroup {
    return this.formBuilder.group(
      {
        objectUsage: [objectUsagePolicy.usageName, Validators.required],
        initialVersion: [objectUsagePolicy.initialVersion, Validators.required],
        intermediaryVersion: [objectUsagePolicy.intermediaryVersion, Validators.required],
      },
      { validators: [ManagementContractValidators.objectUsagePolicyValidator] },
    );
  }
}
