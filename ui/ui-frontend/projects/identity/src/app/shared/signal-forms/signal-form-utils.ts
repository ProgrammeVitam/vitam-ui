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
import { untracked } from '@angular/core';
import { FieldTree, ValidationError } from '@angular/forms/signals';

function isPlainObject(value: unknown): boolean {
  return value !== null && typeof value === 'object' && !Array.isArray(value) && Object.getPrototypeOf(value) === Object.prototype;
}

/**
 * Returns the value of the field without its disabled descendant fields,
 * like the `value` of a reactive `FormGroup` (the signal forms model always contains the disabled fields).
 */
export function enabledValue<T>(field: FieldTree<T>): Partial<T> {
  return untracked(() => {
    const value = field().value();
    if (!isPlainObject(value)) {
      return value;
    }
    const record = value as Record<string, unknown>;
    const children = field as unknown as Record<string, FieldTree<unknown>>;
    return Object.keys(record).reduce<Record<string, unknown>>((result, key) => {
      const child = children[key];
      if (!child || !child().disabled()) {
        result[key] = child ? enabledValue(child) : record[key];
      }
      return result;
    }, {}) as Partial<T>;
  });
}

/** Whether the field has an error of the given kind (signal forms equivalent of `control.hasError(kind)`). */
export function hasError(field: () => { errors: () => readonly ValidationError[] }, kind: string): boolean {
  return field()
    .errors()
    .some((error) => error.kind === kind);
}
