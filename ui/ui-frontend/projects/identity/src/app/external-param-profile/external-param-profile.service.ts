import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import {
  Criterion,
  ExternalParamProfile,
  ExternalParamProfileApiService,
  Operators,
  SearchQuery,
  SearchService,
  VitamUISnackBar,
} from 'ui-frontend-common';
import { VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';

@Injectable({
  providedIn: 'root',
})
export class ExternalParamProfileService extends SearchService<ExternalParamProfile> {
  updated = new Subject<ExternalParamProfile>();

  constructor(private externalParamProfileApi: ExternalParamProfileApiService, private snackBar: VitamUISnackBar, http: HttpClient) {
    super(http, externalParamProfileApi);
  }

  getOne(id: string): Observable<ExternalParamProfile> {
    return this.externalParamProfileApi.getOne(id);
  }

  create(externalParamProfile: ExternalParamProfile) {
    return this.externalParamProfileApi.create(externalParamProfile).pipe(
      tap(
        (response: ExternalParamProfile) => {
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'externalParamProfileCreate', name: response.name },
            duration: 10000,
          });
        },
        (error) => {
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          });
        }
      )
    );
  }

  patch(data: { id: string; [key: string]: any }): Observable<ExternalParamProfile> {
    return this.externalParamProfileApi.patch(data).pipe(
      tap((response) => this.updated.next(response)),
      tap(
        (response) => {
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
            data: { type: 'externalParamProfileUpdate', name: response.name },
          });
        },
        (error) => {
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          });
        }
      )
    );
  }

  exists(tenantIdentifier: number, applicationName: string, name: string): Observable<boolean> {
    const criterionArray = [];
    const criterionTenantIdentifier: Criterion = { key: 'tenantIdentifier', value: tenantIdentifier, operator: Operators.equals };
    const criterionApplicationName: Criterion = { key: 'applicationName', value: applicationName, operator: Operators.equals };
    const criterionName: Criterion = { key: 'name', value: name, operator: Operators.equalsIgnoreCase };
    criterionArray.push(criterionName, criterionTenantIdentifier, criterionApplicationName);
    const query: SearchQuery = { criteria: criterionArray };

    const params = [{ key: 'criteria', value: JSON.stringify(query) }];

    return this.externalParamProfileApi.checkExistsByParam(params, this.headers);
  }
}
