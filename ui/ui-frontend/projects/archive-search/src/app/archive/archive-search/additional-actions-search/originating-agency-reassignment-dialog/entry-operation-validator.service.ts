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
import { PathKind, SchemaPath, SchemaPathRules, validate, ValidationError } from '@angular/forms/signals';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { validateObservable } from 'vitamui-library';
import { ArchiveService } from '../../../archive.service';

type EntryOperationIdsError = ValidationError & Record<string, unknown>;

@Injectable({
  providedIn: 'root',
})
export class EntryOperationValidatorService {
  private readonly archiveService = inject(ArchiveService);

  debounceTime = 400;

  /**
   * Validates entry operation IDs.
   * - Checks for empty input or whitespace-only input
   * - Validates format of each operation ID (36 lowercase alphanumeric characters)
   * - Validates each operation ID exists in the logbook via backend API
   * - Returns validation errors for invalid or non-existent IDs
   */
  validateEntryOperationIds<TPathKind extends PathKind = PathKind.Root>(
    path: SchemaPath<string, SchemaPathRules.Supported, TPathKind>,
  ): void {
    // Check if value is only whitespace
    validate(path, ({ value }) => (value() && value().trim().length === 0 ? { kind: 'whitespaceOnly' } : undefined));

    validateObservable(path, {
      debounceTime: this.debounceTime,
      stream: (value) => this.entryOperationIdsErrors(value),
      onSuccess: (error) => error ?? undefined,
    });
  }

  private entryOperationIdsErrors(value: string): Observable<EntryOperationIdsError | null> {
    // Don't validate empty values (use required for that)
    if (!value) {
      return of(null);
    }

    // Split, trim, and filter out empty values
    const idsArray = value
      .split(',')
      .map((id: string) => id.trim())
      .filter((id: string) => id.length > 0);

    if (idsArray.length === 0) {
      return of({ kind: 'emptyAfterTrim' });
    }

    // Check for duplicates
    const uniqueIds = new Set(idsArray);
    if (uniqueIds.size !== idsArray.length) {
      const duplicateIds = idsArray.filter((id: string, index: number) => idsArray.indexOf(id) !== index);
      return of({ kind: 'duplicateIds', duplicateIds: [...new Set(duplicateIds)].join(', ') });
    }

    // Validate format: 36 lowercase alphanumeric characters
    const operationIdPattern = /^[a-z0-9]{36}$/;
    const invalidFormatIds = idsArray.filter((id: string) => !operationIdPattern.test(id));

    if (invalidFormatIds.length > 0) {
      return of({ kind: 'invalidFormat', invalidIds: invalidFormatIds.join(', ') });
    }

    // check existence
    return this.archiveService.checkOperationIdsExistence(idsArray).pipe(
      map((existenceMap: { [key: string]: boolean }) => {
        const invalidIds = idsArray.filter((id: string) => !existenceMap[id]);

        if (invalidIds.length > 0) {
          return { kind: 'invalidOperationIds', invalidIds: invalidIds.join(', ') };
        }

        return null;
      }),
      catchError(() => {
        // In case of error, consider all IDs as potentially invalid
        return of({ kind: 'invalidOperationIds', invalidIds: idsArray.join(', ') });
      }),
    );
  }
}
