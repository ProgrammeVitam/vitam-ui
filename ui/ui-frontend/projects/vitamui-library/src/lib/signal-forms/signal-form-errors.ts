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
import { ValidationErrors } from '@angular/forms';
import { CompatValidationError } from '@angular/forms/signals/compat';
import { ValidationError } from '@angular/forms/signals';

/**
 * Converts signal forms validation errors to reactive forms `ValidationErrors`.
 * Built-in signal forms errors are mapped to their reactive forms key and params, so that the existing
 * `ERRORS.*` i18n messages (`maxlength`, `minlength`, `pattern`...) can be reused.
 */
export function toValidationErrors(errors: readonly ValidationError[] | null | undefined): ValidationErrors | null {
  if (!errors?.length) {
    return null;
  }
  return errors.reduce<ValidationErrors>((validationErrors, error) => {
    if (error instanceof CompatValidationError) {
      validationErrors[error.kind] = error.context;
      return validationErrors;
    }
    const {
      kind,
      fieldTree: _fieldTree,
      message: _message,
      formField: _formField,
      ...params
    } = error as ValidationError & Record<string, any>;
    switch (kind) {
      case 'maxLength':
        validationErrors['maxlength'] = { requiredLength: params['maxLength'] };
        break;
      case 'minLength':
        validationErrors['minlength'] = { requiredLength: params['minLength'] };
        break;
      case 'pattern':
        validationErrors['pattern'] = { requiredPattern: String(params['pattern']) };
        break;
      default: {
        const definedParams = Object.fromEntries(
          Object.entries(params).filter(([key, value]) => !key.startsWith('__') && value !== undefined),
        );
        validationErrors[kind] = Object.keys(definedParams).length ? definedParams : true;
      }
    }
    return validationErrors;
  }, {});
}

/**
 * Converts reactive forms `ValidationErrors` to signal forms validation errors (the inverse of `toValidationErrors` for custom errors):
 * `{ errorKey: { param: value } }` becomes `[{ kind: 'errorKey', param: value }]` and `{ errorKey: true }` becomes `[{ kind: 'errorKey' }]`.
 */
export function fromValidationErrors(errors: ValidationErrors | null | undefined): ValidationError.WithoutFieldTree[] {
  return Object.entries(errors ?? {}).map(([kind, params]) =>
    params instanceof Object && !Array.isArray(params) ? { ...params, kind } : { kind },
  );
}
