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
import { applyEach, PathKind, required, schema, SchemaPath, SchemaPathRules, validate } from '@angular/forms/signals';
import { requiredValue } from 'vitamui-library';
import { ObjectUsagePolicyForm, PersistentIdentifierPolicyForm } from '../components/management-contract-to-form-model-converter.service';

type FieldPath<TValue, TPathKind extends PathKind> = SchemaPath<TValue, SchemaPathRules.Supported, TPathKind>;

export enum ManagementContractValidationErrors {
  INVALID_AUTHORITY = 'InvalidAuthority',
  INVALID_OBJECT_USAGE_POLICY = 'invalidObjectUsagePolicy',
  INVALID_PERSISTENT_IDENTIFIER_POLICY_TARGET = 'invalidPersistentIdentifierPolicyTarget',
}

const AUTHORITY_REGEX = /^([0-9]{5}|[0-9]{9})$/;

export class ManagementContractValidators {
  static authority<TPathKind extends PathKind = PathKind.Root>(path: FieldPath<string, TPathKind>): void {
    validate(path, ({ value }) =>
      AUTHORITY_REGEX.test(value()) ? undefined : { kind: ManagementContractValidationErrors.INVALID_AUTHORITY },
    );
  }

  /** Validates the whole object usage policy: the initial version cannot be removed when there are no intermediary versions. */
  static objectUsagePolicy<TPathKind extends PathKind = PathKind.Root>(path: FieldPath<ObjectUsagePolicyForm, TPathKind>): void {
    validate(path, ({ value }) =>
      value().initialVersion === false && value().intermediaryVersion === 'NONE'
        ? { kind: ManagementContractValidationErrors.INVALID_OBJECT_USAGE_POLICY }
        : undefined,
    );
  }

  /** Validates the whole persistent identifier policy: it must concern archive units or objects. */
  static persistentIdentifierPolicyTarget<TPathKind extends PathKind = PathKind.Root>(
    path: FieldPath<PersistentIdentifierPolicyForm, TPathKind>,
  ): void {
    validate(path, ({ value }) =>
      value().shouldConcernArchiveUnits || value().shouldConcernObjects
        ? undefined
        : { kind: ManagementContractValidationErrors.INVALID_PERSISTENT_IDENTIFIER_POLICY_TARGET },
    );
  }
}

export const objectUsagePolicySchema = schema<ObjectUsagePolicyForm>((path) => {
  required(path.objectUsage);
  requiredValue(path.initialVersion);
  required(path.intermediaryVersion);
  ManagementContractValidators.objectUsagePolicy(path);
});

export const persistentIdentifierPolicySchema = schema<PersistentIdentifierPolicyForm>((path) => {
  required(path.authority);
  ManagementContractValidators.authority(path.authority);
  ManagementContractValidators.persistentIdentifierPolicyTarget(path);
  applyEach(path.objectUsagePolicies, objectUsagePolicySchema);
});
