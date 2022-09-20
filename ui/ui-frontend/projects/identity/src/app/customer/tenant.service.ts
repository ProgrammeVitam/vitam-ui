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
import { Criterion, Operators, SearchQuery, Tenant } from 'ui-frontend-common';

import { HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { VitamUISnackBar, VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';
import { TenantApiService } from './tenant-api.service';

@Injectable()
export class TenantService {

  updated = new Subject<Tenant>();

  constructor(private tenantApi: TenantApiService, private snackBar: VitamUISnackBar) {}

  get(id: string): Observable<Tenant> {
    return this.tenantApi.getOne(id);
  }

  getAll() {
    return this.tenantApi.getAllByParams(new HttpParams());
  }

  getTenantsByCustomerIds(customerIds: string[]) {
    const criterionArray: any[] = [];
    criterionArray.push({ key: 'customerId', value: customerIds, operator: Operators.in });
    const query: SearchQuery = { criteria: criterionArray };
    const params = new HttpParams().set('criteria', JSON.stringify(query));

    return this.tenantApi.getAllByParams(params);
  }

  create(tenant: Tenant, ownerName: string): Observable<Tenant> {
    return this.tenantApi.create(tenant)
      .pipe(
        tap(
          (newTenant: Tenant) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: { type: 'tenantCreate', tenantName: newTenant.name, ownerName },
              duration: 10000
            });
          },
          () => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: { type: 'tenantCreateError' },
              duration: 10000
            });
          }
        )
      );
  }

  patch(partialTenant: { id: string, [key: string]: any }, ownerName: string): Observable<Tenant> {
    return this.tenantApi.patch(partialTenant)
      .pipe(
        tap((updatedTenant: Tenant) => this.updated.next(updatedTenant)),
        tap(
          (updatedTenant: Tenant) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: { type: 'tenantUpdate', tenantName: updatedTenant.name, ownerName },
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

  exists(name: string): Observable<any> {
    const criterionArray = [];
    const criterionCode: Criterion = { key: 'name', value: name, operator: Operators.equals };
    criterionArray.push(criterionCode);
    const query: SearchQuery = { criteria: criterionArray };
    const params = [{key : 'criteria', value: JSON.stringify(query)}];

    return this.tenantApi.checkExistsByParam(params);
  }

}
