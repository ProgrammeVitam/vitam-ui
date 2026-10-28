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
import { SchemaPath } from '@angular/forms/signals';

import { Observable, of } from 'rxjs';
import { validateObservable } from 'vitamui-library';
import { FileFormatService } from '../file-format.service';

@Injectable({
  providedIn: 'root',
})
export class FileFormatCreateValidators {
  private readonly fileFormatService: FileFormatService;

  constructor(fileFormatService: FileFormatService = inject(FileFormatService)) {
    this.fileFormatService = fileFormatService;
  }

  /** Adds a `nameExists` error when a file format with the same name already exists (except `nameToIgnore`). */
  uniqueName = (path: SchemaPath<string>, nameToIgnore: () => string = () => undefined): void => {
    this.uniqueField(path, 'name', 'nameExists', nameToIgnore);
  };

  /** Adds a `puidExists` error when a file format with the same PUID already exists (except `puidToIgnore`). */
  uniquePuid = (path: SchemaPath<string>, puidToIgnore: () => string = () => undefined): void => {
    this.uniqueField(path, 'puid', 'puidExists', puidToIgnore);
  };

  /** Checks if a file format with the given property value exists (`false` without calling the backend if the value is `valueToIgnore`). */
  exists(field: 'name' | 'puid', value: string, valueToIgnore?: string): Observable<boolean> {
    return value !== valueToIgnore ? this.fileFormatService.existsProperties({ [field]: value }) : of(false);
  }

  private uniqueField(path: SchemaPath<string>, field: 'name' | 'puid', existTag: string, valueToIgnore: () => string) {
    validateObservable(path, {
      stream: (value: string) => this.exists(field, value, valueToIgnore()),
      onSuccess: (exists: boolean) => (exists ? { kind: existTag } : undefined),
    });
  }
}
