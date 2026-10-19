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
import { RuleService, validateObservable } from 'vitamui-library';

@Injectable({
  providedIn: 'root',
})
export class RuleCreateValidators {
  private readonly ruleService: RuleService;

  constructor(ruleService: RuleService = inject(RuleService)) {
    this.ruleService = ruleService;
  }

  /** Adds a `ruleIdExists` error when a rule with the same identifier already exists (except `ruleIdToIgnore`). */
  uniqueRuleId = (path: SchemaPath<string>, ruleIdToIgnore: () => string = () => undefined): void => {
    validateObservable(path, {
      stream: (value: string) => this.ruleIdExists(value, ruleIdToIgnore()),
      onSuccess: (exists: boolean) => (exists ? { kind: 'ruleIdExists' } : undefined),
    });
  };

  /** Checks if a rule with the given identifier exists (`false` without calling the backend if the value is `ruleIdToIgnore`). */
  ruleIdExists(ruleId: string, ruleIdToIgnore?: string): Observable<boolean> {
    return ruleId !== ruleIdToIgnore ? this.ruleService.existsProperties({ ruleId }) : of(false);
  }
}
