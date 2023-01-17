/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { Injectable } from '@angular/core';
import { AbstractControl, AsyncValidatorFn, ValidationErrors, ValidatorFn } from '@angular/forms';
import { of, timer } from 'rxjs';
import { map, switchMap, take } from 'rxjs/operators';
import { ArchiveSharedDataService } from './archive-shared-data.service';
import { ManagementRulesSharedDataService } from './management-rules-shared-data.service';

@Injectable()
export class  RuleValidator {
  private debounceTime = 400;
  ruleCategorySelected: string;

  constructor(private managementRulesSharedDataService: ManagementRulesSharedDataService, private archiveSharedDataService: ArchiveSharedDataService) { }

  uniqueRuleId(ruleIdToIgnore?: string): AsyncValidatorFn {
    return this.uniqueFields('ruleId', 'ruleIdExists', ruleIdToIgnore);
  }

  ruleIdPattern(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const regexp = /[À-ÖØ-öø-ÿ ]/;
      return regexp.test(control.value) ? { ruleIdPattern: true } : null;
    };
  }

  private getRuleType(ruleSelected: string) {
    switch (ruleSelected) {
      case 'APPRAISAL_RULE':
        return 'AppraisalRule';
      case 'STORAGE_RULE':
        return 'StorageRule';
      case 'ACCESS_RULE':
        return 'AccessRule';
      case 'REUSE_RULE':
        return 'ReuseRule';
      case 'CLASSIFICATION_RULE':
        return 'ClassificationRule';
      case 'HOLD_RULE':
        return 'HoldRule';
      case 'DISSEMINATION_RULE':
        return 'DisseminationRule';
      default:
        return null;
    }
  }

  private uniqueFields(field: string, existTag: string, valueToIgnore?: string) {
    this.archiveSharedDataService.getRuleCategory().subscribe((data) => {
      this.ruleCategorySelected = data;
    });
    return (control: AbstractControl) => {
      const properties: any = {};
      if (control.value) {
        properties[field] = control.value;
        properties.ruleType = this.getRuleType(this.ruleCategorySelected);
        const existField: any = {};
        existField[existTag] = true;
        return timer(this.debounceTime).pipe(
          switchMap(() => (control.value !== valueToIgnore ?
            this.managementRulesSharedDataService.existsProperties(properties) : of(false))),
          take(1),
          map((exists: boolean) => (exists ? null : existField))
        );
      } else {
        return of(false);
      }
    };
  }
}
