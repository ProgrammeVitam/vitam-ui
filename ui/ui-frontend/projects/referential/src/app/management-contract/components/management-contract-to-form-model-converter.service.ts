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
} from 'vitamui-library';
import { Converter } from './converter';

/** Signal forms model of an object usage policy of a persistent identifier policy */
export interface ObjectUsagePolicyForm {
  objectUsage: string;
  initialVersion: boolean;
  intermediaryVersion: string;
}

/** Signal forms model of a persistent identifier policy */
export interface PersistentIdentifierPolicyForm {
  policyTypeOption: string;
  authority: string;
  shouldConcernArchiveUnits: boolean;
  shouldConcernObjects: boolean;
  objectUsagePolicies: ObjectUsagePolicyForm[];
}

export interface PersistentIdentifierPoliciesForm {
  persistentIdentifierPolicies: PersistentIdentifierPolicyForm[];
}

@Injectable({
  providedIn: 'root',
})
export class ManagementContractToFormModelConverterService implements Converter<ManagementContract, PersistentIdentifierPoliciesForm> {
  convert(source: ManagementContract): PersistentIdentifierPoliciesForm {
    return {
      persistentIdentifierPolicies: (source.persistentIdentifierPolicyList || [this.getDefaultPersistentIdentifierPolicy()]).map((policy) =>
        this.buildPolicy(policy),
      ),
    };
  }

  getDefaultPersistentIdentifierPolicy(): PersistentIdentifierPolicy {
    return {
      persistentIdentifierPolicyType: '' as PersistentIdentifierPolicyTypeEnum, // TODO: should we include NONE in persistentIdType ?
      persistentIdentifierAuthority: '',
      persistentIdentifierUnit: false,
      persistentIdentifierUsages: [],
    };
  }

  getDefaultPersistentIdentifierPolicies(): PersistentIdentifierPolicyForm[] {
    return [this.buildPolicy(this.getDefaultPersistentIdentifierPolicy())];
  }

  private buildPolicy(policy: PersistentIdentifierPolicy): PersistentIdentifierPolicyForm {
    return {
      policyTypeOption: policy.persistentIdentifierPolicyType ?? null,
      authority: policy.persistentIdentifierAuthority ?? null,
      shouldConcernArchiveUnits: policy.persistentIdentifierUnit ?? null,
      shouldConcernObjects: Boolean(policy.persistentIdentifierUsages.length),
      objectUsagePolicies: policy.persistentIdentifierUsages.map((objectUsagePolicy) => this.buildObjectUsagePolicy(objectUsagePolicy)),
    };
  }

  private buildObjectUsagePolicy(objectUsagePolicy: PersistentIdentifierUsage): ObjectUsagePolicyForm {
    return {
      objectUsage: objectUsagePolicy.usageName ?? null,
      initialVersion: (objectUsagePolicy.initialVersion as unknown as boolean) ?? null,
      intermediaryVersion: objectUsagePolicy.intermediaryVersion ?? null,
    };
  }
}
