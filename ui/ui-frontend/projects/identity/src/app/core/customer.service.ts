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
import { Criterion, Customer, Operators, SearchQuery } from 'ui-frontend-common';

import { Injectable } from '@angular/core';

import { HttpResponse } from '@angular/common/http';
import { VitamUISnackBar, VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';
import { CustomerApiService } from './api/customer-api.service';

export const DEFAULT_PAGE_SIZE = 20;

@Injectable({
  providedIn: 'root'
})
export class CustomerService {

  updated = new Subject<Customer>();

  constructor(private customerApi: CustomerApiService, private snackBar: VitamUISnackBar) {}

  get(id: string): Observable<Customer> {
    return this.customerApi.getOne(id);
  }

  getMyCustomer(): Observable<Customer> {
    return this.customerApi.getMyCustomer();
  }

  exists(properties: { code?: string, domain?: string }): Observable<any> {

    const criterionArray: Criterion[] = [];
    if (properties.code) {
      criterionArray.push({ key: 'code', value: properties.code, operator: Operators.equals });
    }
    if (properties.domain) {
      criterionArray.push({ key: 'emailDomains', value: properties.domain, operator: Operators.containsIgnoreCase });
    }
    const query: SearchQuery = { criteria: criterionArray };

    const params = [{key : 'criteria', value: JSON.stringify(query)}];

    return this.customerApi.checkExistsByParam(params);
  }

  create(customer: Customer, logo?: File): Observable<Customer> {
    return this.customerApi.createCustomer(customer, logo)
      .pipe(
        tap(
          (response: Customer) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: { type: 'customerCreate', code: response.code },
              duration: 10000
            });
          },
          () => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: { type: 'customerCreateError' },
              duration: 10000
            });
          }
        )
      );
  }

  patch(partialCustomer: { id: string, [key: string]: any }, logo?: File): Observable<Customer> {
    return this.customerApi.patchCustomer(partialCustomer, logo)
      .pipe(
        tap((updatedCustomer: Customer) => this.updated.next(updatedCustomer)),
        tap(
          (updatedCustomer: Customer) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: { type: 'customerUpdate', code: updatedCustomer.code },
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

  getCustomerLogo(id: string): Observable<HttpResponse<Blob>> {
    return this.customerApi.getCustomerLogo(id);
  }

}
