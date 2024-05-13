/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import { CriteriaSearchQuery, Criterion, Group, Operators, SearchService, VitamUISnackBarService } from 'vitamui-library';

import { GroupApiService } from '../core/api/group-api.service';

@Injectable({
  providedIn: 'root',
})
export class GroupService extends SearchService<Group> {
  updated = new Subject<Group>();

  constructor(
    private groupApi: GroupApiService,
    private snackBarService: VitamUISnackBarService,
  ) {
    super(groupApi);
  }

  create(group: Group) {
    return this.groupApi.create(group).pipe(
      tap(
        (response: Group) => {
          this.snackBarService.open({
            message: 'SHARED.SNACKBAR.GROUP_CREATE',
            translateParams: {
              param1: response.name,
            },
            icon: 'vitamui-icon-keys',
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        },
      ),
    );
  }

  get(id: string): Observable<Group> {
    return this.groupApi.getOneWithEmbedded(id, 'ALL');
  }

  exists(customerId: string, name: string): Observable<any> {
    const criterionArray: Criterion[] = [];
    if (customerId) {
      criterionArray.push({ key: 'customerId', value: customerId, operator: Operators.equals });
    }
    if (name) {
      criterionArray.push({ key: 'name', value: name, operator: Operators.equals });
    }
    const query: CriteriaSearchQuery = { criteria: criterionArray };

    const params = [{ key: 'criteria', value: JSON.stringify(query) }];

    return this.groupApi.checkExistsByParam(params);
  }

  unitExists(customerId: string, unit: string): Observable<any> {
    const criteria: Criterion[] = [];
    criteria.push({ key: 'units', value: unit, operator: Operators.equalsIgnoreCase });
    criteria.push({ key: 'customerId', value: customerId, operator: Operators.equals });
    const query: CriteriaSearchQuery = { criteria };
    const params = [{ key: 'criteria', value: JSON.stringify(query) }];

    return this.groupApi.checkExistsByParam(params);
  }

  patch(groupPartial: { id: string; [key: string]: any }): Observable<Group> {
    return this.groupApi.patch(groupPartial).pipe(
      tap((updatedGroup: Group) => this.updated.next(updatedGroup)),
      tap(
        (response: Group) => {
          this.snackBarService.open({
            message: 'SHARED.SNACKBAR.GROUP_UPDATE',
            translateParams: {
              param1: response.name,
            },
            icon: 'vitamui-icon-keys',
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        },
      ),
    );
  }

  getAll(enabled?: boolean): Observable<Group[]> {
    const criterionArray: Criterion[] = [];

    if (enabled) {
      criterionArray.push({ key: 'enabled', value: enabled, operator: Operators.equals });
    }
    const query: CriteriaSearchQuery = { criteria: criterionArray };

    let params = new HttpParams();

    if (criterionArray.length > 0) {
      params = params.set('criteria', JSON.stringify(query));
    }

    return this.groupApi.getAllByParams(params);
  }

  getNonEmptyLevels(query: CriteriaSearchQuery) {
    return this.groupApi.getLevels(query).pipe(map((levels) => levels.filter((l) => !!l)));
  }

  export(): Observable<HttpResponse<Blob>> {
    return this.groupApi.export();
  }
}
