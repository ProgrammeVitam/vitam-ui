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
import { PathKind, SchemaPath, SchemaPathRules } from '@angular/forms/signals';
import { Observable, of } from 'rxjs';
import { AccessContractService, validateObservable } from 'vitamui-library';

type FieldPath<TValue, TPathKind extends PathKind> = SchemaPath<TValue, SchemaPathRules.Supported, TPathKind>;

@Injectable({
  providedIn: 'root',
})
export class AccessContractCreateValidators {
  private readonly accessContractService: AccessContractService;

  constructor(accessContractService: AccessContractService = inject(AccessContractService)) {
    this.accessContractService = accessContractService;
  }

  /**
   * Checks that no other access contract has the same name.
   * @param nameToIgnore returns the name that is not checked
   */
  uniqueName<TPathKind extends PathKind = PathKind.Root>(path: FieldPath<string, TPathKind>, nameToIgnore?: () => string): void {
    this.uniqueField(path, 'name', 'nameExists', (value) => value !== nameToIgnore?.());
  }

  /**
   * Checks that no other access contract has the same name, ignoring the name of the edited access contract.
   * @param accessContract returns the edited access contract
   */
  uniqueNameWhileEdit<TPathKind extends PathKind = PathKind.Root>(
    path: FieldPath<string, TPathKind>,
    accessContract: () => { name?: string } | undefined,
  ): void {
    this.uniqueField(path, 'name', 'nameExists', (value) => accessContract() !== undefined && value !== accessContract().name);
  }

  uniqueIdentifier<TPathKind extends PathKind = PathKind.Root>(
    path: FieldPath<string, TPathKind>,
    identifierToIgnore?: () => string,
  ): void {
    this.uniqueField(path, 'identifier', 'identifierExists', (value) => value !== identifierToIgnore?.());
  }

  private uniqueField<TPathKind extends PathKind>(
    path: FieldPath<string, TPathKind>,
    field: string,
    errorKind: string,
    shouldCheck: (value: string) => boolean,
  ): void {
    validateObservable(path, {
      stream: (value: string): Observable<boolean> =>
        shouldCheck(value) ? this.accessContractService.existsProperties({ [field]: value?.trim() }) : of(false),
      onSuccess: (exists: boolean) => (exists ? { kind: errorKind } : undefined),
    });
  }
}
