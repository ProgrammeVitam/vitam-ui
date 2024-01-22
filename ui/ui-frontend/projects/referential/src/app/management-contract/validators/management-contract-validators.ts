import { Injectable } from '@angular/core';
import { AbstractControl } from '@angular/forms';

export enum ManagementContractValidationErrors {
  INVALID_AUTHORITY = 'InvalidAuthority',
  INVALID_OBJECT_USAGE_POLICY = 'invalidObjectUsagePolicy',
  INVALID_PERSISTENT_IDENTIFIER_POLICY_TARGET = 'invalidPersistentIdentifierPolicyTarget',
}

@Injectable({
  providedIn: 'root',
})
export class ManagementContractValidators {
  static authorityValidator = (control: AbstractControl): { [key: string]: boolean } | null => {
    const regex = /^([0-9]{5}|[0-9]{9})$/;

    if (!regex.test(control.value)) {
      return { [ManagementContractValidationErrors.INVALID_AUTHORITY]: true };
    }
    return null;
  };

  static objectUsagePolicyValidator = (control: AbstractControl): { [key: string]: boolean } | null => {
    const initialVersion = control.get('initialVersion');
    const intermediaryVersion = control.get('intermediaryVersion');

    if (initialVersion.value === false && intermediaryVersion.value === 'NONE') {
      return { [ManagementContractValidationErrors.INVALID_OBJECT_USAGE_POLICY]: true };
    }

    return null;
  };

  static persistentIdentifierPolicyValidator = (control: AbstractControl): { [key: string]: boolean } | null => {
    const shouldConcernArchiveUnits = control.get('shouldConcernArchiveUnits').value;
    const shouldConcernObjects = control.get('shouldConcernObjects').value;
    const valid = shouldConcernArchiveUnits || shouldConcernObjects;

    if (!valid) {
      return { [ManagementContractValidationErrors.INVALID_PERSISTENT_IDENTIFIER_POLICY_TARGET]: true };
    }

    return null;
  };
}
