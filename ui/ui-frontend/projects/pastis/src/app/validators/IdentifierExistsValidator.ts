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
import { rxResource } from '@angular/core/rxjs-interop';
import { PathKind, SchemaPath, SchemaPathRules, validateAsync } from '@angular/forms/signals';
import { catchError, map, of, switchMap, take, timer } from 'rxjs';
import { ProfileService } from '../core/services/profile.service';
import { ArchivalProfileUnit } from '../models/archival-profile-unit';
import { Profile } from '../models/profile';

export const IDENTIFIER_EXIST_ERROR_KEY = 'identifierExist';

@Injectable({
  providedIn: 'root',
})
export class IdentifierExistsValidator {
  private readonly profileService = inject(ProfileService);

  /**
   * Adds an async validator checking that the identifier is not already used by a PA (or PUA, according to `modePua`) profile.
   * The validation is run again when the value or the mode changes, and is skipped for an empty identifier.
   */
  identifierExists<TPathKind extends PathKind = PathKind.Root>(
    path: SchemaPath<string, SchemaPathRules.Supported, TPathKind>,
    modePua: boolean | (() => boolean),
  ): void {
    validateAsync(path, {
      params: ({ value }) => (value() ? { identifier: value(), modePua: typeof modePua === 'function' ? modePua() : modePua } : undefined),
      factory: (params) =>
        rxResource({
          params,
          stream: ({ params: { identifier, modePua } }) => timer(300).pipe(switchMap(() => this.checkIdentifier(identifier, modePua))),
        }),
      onSuccess: (exists) => (exists ? { kind: IDENTIFIER_EXIST_ERROR_KEY } : undefined),
      onError: () => undefined,
    });
  }

  private checkIdentifier(identifier: string, modePua: boolean) {
    const validationObservable = modePua
      ? this.profileService.checkPuaProfile({ identifier } as ArchivalProfileUnit)
      : this.profileService.checkPaProfile({ identifier } as Profile);

    return validationObservable.pipe(
      take(1),
      map((response) => !!response),
      catchError(() => of(false)),
    );
  }
}
