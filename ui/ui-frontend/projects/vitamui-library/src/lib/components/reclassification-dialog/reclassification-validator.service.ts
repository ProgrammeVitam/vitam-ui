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
import { inject, Injectable } from '@angular/core';
import { PathKind, SchemaPath, SchemaPathRules } from '@angular/forms/signals';
import { BehaviorSubject, from, Observable, of } from 'rxjs';
import { CriteriaDataType, CriteriaOperator } from '../../../app/modules/models/criteria/criteria.enums';
import { SearchCriteriaDto, SearchCriteriaTypeEnum } from '../../../app/modules/models/criteria/search-criteria.interface';
import { ReclassificationService } from '../../../app/modules/services/reclassification.service';
import { validateObservable } from '../../signal-forms/signal-form-validators';

type FieldPath<TPathKind extends PathKind> = SchemaPath<string | null, SchemaPathRules.Supported, TPathKind>;

@Injectable({
  providedIn: 'root',
})
export class ReclassificationValidatorService {
  private readonly reclassificationService = inject(ReclassificationService);

  private readonly auTitleSubject = new BehaviorSubject<string>('');
  debounceTime = 400;

  emitArchiveUnitTitle(auTitle: string) {
    this.auTitleSubject.next(auTitle);
  }

  getArchiveUnitTitle(): Observable<string> {
    return this.auTitleSubject.asObservable();
  }

  /** Signal forms rule: the field value must not be one of the current parents (`alreadyExistParents` error). */
  alreadyExistParents<TPathKind extends PathKind>(path: FieldPath<TPathKind>, archiveUnitAllunitup: string[], codeToIgnore?: string): void {
    validateObservable(path, {
      stream: (value) => of(value !== codeToIgnore && this.isAlreadyExistingParentValue(value, archiveUnitAllunitup)),
      onSuccess: (exists) => (exists ? { kind: 'alreadyExistParents' } : undefined),
      debounceTime: this.debounceTime,
    });
  }

  isAlreadyExistingParentValue(parentId: string, archiveUnitAllunitup: string[]): boolean {
    return true ? archiveUnitAllunitup.filter((p) => p === parentId).length > 0 : false;
  }

  /** Signal forms rule: the field value must be the GUID of an existing archive unit (`targetGuid` error). Emits the archive unit title. */
  existArchiveUnit<TPathKind extends PathKind>(path: FieldPath<TPathKind>, criteriaDto: () => SearchCriteriaDto): void {
    validateObservable(path, {
      stream: (value) => this.unitDoesNotExist(value, criteriaDto()),
      onSuccess: (doesNotExist) => (doesNotExist ? { kind: 'targetGuid' } : undefined),
      debounceTime: this.debounceTime,
    });
  }

  private unitDoesNotExist(value: string | null, criteriaDto: SearchCriteriaDto): Observable<boolean> {
    if (value === null) return of(false);

    const searchCriteria: SearchCriteriaDto = {
      pageNumber: criteriaDto.pageNumber,
      size: criteriaDto.size,
      criteriaList: [
        {
          criteria: 'GUID',
          operator: CriteriaOperator.EQ,
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
          dataType: CriteriaDataType.STRING,
          values: [{ value, id: value }],
        },
      ],
    };
    return from(
      this.reclassificationService
        .searchArchiveUnitsByCriteria(searchCriteria)
        .toPromise()
        .then((data) => {
          if (data.totalResults === 1) {
            this.emitArchiveUnitTitle(ReclassificationService.fetchAuTitle(data.results[0]));
            return false;
          } else {
            this.emitArchiveUnitTitle(null);
            return true;
          }
        }),
    );
  }
}
