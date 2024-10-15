/*
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

import { AbstractControl, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';

export enum DatePattern {
  YEAR = '([1-9]\\d{1,})',
  YEAR_MONTH = `${DatePattern.YEAR}-(0[1-9]|1[0-2])`,
  YEAR_MONTH_DAY = '...',
}

export class CustomValidators {
  static pattern(pattern: string | RegExp, message?: string): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;

      const error = Validators.pattern(pattern)(control);
      if (error) return { pattern: { ...error.pattern, message: message || error.pattern.requiredPattern } };

      return null;
    };
  }

  static date(pattern: DatePattern): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      if (!control.value) return null;
      if (pattern === DatePattern.YEAR_MONTH_DAY) return this.isValidYearMonthDay(control.value) ? null : { pattern: true };

      return Validators.pattern(pattern)(control);
    };
  }

  private static isValidYearMonthDay(dateString: string) {
    if (!/^\d*-\d*-\d*$/.test(dateString)) {
      return false; // Invalid format
    }

    // Step 1: Split the string into components
    const parts = dateString.split('-');

    // Step 2: Convert to integers
    const year = parseInt(parts[0], 10);
    const month = parseInt(parts[1], 10) - 1; // Months are zero-based (0 = January, 11 = December)
    const day = parseInt(parts[2], 10);

    // Step 3: Create a Date object
    const date = new Date(year, month, day);

    // Step 4: Check for validity
    return !(date.getFullYear() !== year || date.getMonth() !== month || date.getDate() !== day);
  }
}
