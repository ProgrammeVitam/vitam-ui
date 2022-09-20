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
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Criterion, Operators, Profile, ProfileApiService, SearchQuery, SearchService } from 'ui-frontend-common';

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { VitamUISnackBar, VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';

@Injectable({
  providedIn: 'root'
})
export class HierarchyService extends SearchService<Profile>  {

  updated = new Subject<Profile>();

  constructor(private profileApi: ProfileApiService, http: HttpClient, private snackBar: VitamUISnackBar) {
    super(http, profileApi, 'ALL');
  }

  setTenantId(tenantIdentifier: number) {
    this.headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString()});
  }

  get(id: string): Observable<Profile> {
    return this.profileApi.getOneWithEmbedded(id, 'ALL', this.headers);
  }

  exists(tenantIdentifier: number, level: string, applicationName: string, name: string): Observable<boolean> {
    const criterionArray = [];
    const criterionName: Criterion = { key: 'name', value: name, operator: Operators.equalsIgnoreCase };
    const criterionTenantIdentifier: Criterion = { key: 'tenantIdentifier', value: tenantIdentifier, operator: Operators.equals };
    const criterionLevel: Criterion = { key: 'level', value: level, operator: Operators.equals };
    const criterionApplicationName: Criterion = { key: 'applicationName', value: applicationName, operator: Operators.equals};
    criterionArray.push(criterionName, criterionTenantIdentifier, criterionLevel, criterionApplicationName);
    const query: SearchQuery = { criteria: criterionArray };

    const params = [{key : 'criteria', value: JSON.stringify(query)}];

    return this.profileApi.checkExistsByParam(params, this.headers);
  }

  patch(data: { id: string, [key: string]: any }): Observable<Profile> {
    return this.profileApi.patch(data, this.headers)
      .pipe(
        tap((response) => this.updated.next(response)),
        tap(
          (response) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000,
              data: { type: 'profileUpdate', name: response.name }
            });
          },
          (error) => {
            this.snackBar.open(error.error.message, null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000
            });
          }
        )
      );
  }

  create(profile: Profile) {

    return this.profileApi.create(profile, this.headers).pipe(
      tap(
        (response: Profile) => {
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'profileAdminCreate', name: response.name },
            duration: 10000
          });
        },
        (error) => {
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000
          });
        }
      )
    );
  }
}
