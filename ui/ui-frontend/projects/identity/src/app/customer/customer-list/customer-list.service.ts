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
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { Customer, DEFAULT_PAGE_SIZE, Direction, Owner, PageRequest, PaginatedResponse, SearchService } from 'ui-frontend-common';
import { CustomerApiService } from '../../core/api/customer-api.service';

@Injectable({
  providedIn: 'root'
})
export class CustomerListService extends SearchService<Customer> {

  constructor(http: HttpClient, private customerApi: CustomerApiService) {
    super(http, customerApi, 'OWNER,TENANT');
  }

  search(pageRequest: PageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'code', Direction.ASCENDANT)): Observable<Customer[]> {
    this.pageRequest = pageRequest;

    return this.customerApi.getAllPaginated(this.pageRequest, 'OWNER,TENANT')
      .pipe(
        map((paginated: PaginatedResponse<Customer>) => {
          this.data = paginated.values.map((customer) => this.transformCustomer(customer));
          this.pageRequest.page = paginated.pageNum;
          this.hasMore = paginated.hasMore;

          return this.data;
        })
      );
  }

  loadMore(): Observable<Customer[]> {
    if (!this.hasMore) { return of(this.data); }

    this.pageRequest.page += 1;

    return this.customerApi.getAllPaginated(this.pageRequest, 'OWNER,TENANT')
      .pipe(
        map((paginated: PaginatedResponse<Customer>) => {
          this.data = this.data.concat(paginated.values.map((customer) => this.transformCustomer(customer)));
          this.pageRequest.page = paginated.pageNum;
          this.hasMore = paginated.hasMore;

          return this.data;
        })
      );
  }

  private transformCustomer(customer: Customer): Customer {
    return {

      id: customer.id,
      identifier: customer.identifier,
      enabled: customer.enabled,
      code: customer.code,
      name: customer.name,
      companyName: customer.companyName,
      passwordRevocationDelay: customer.passwordRevocationDelay,
      otp: customer.otp,
      address: customer.address,
      internalCode: customer.internalCode,
      language: customer.language,
      emailDomains: customer.emailDomains,
      defaultEmailDomain: customer.defaultEmailDomain,
      owners: this.filteredOwners(customer),
      portalMessage: customer.portalMessage,
      portalTitle: customer.portalTitle,
      readonly: customer.readonly,
      hasCustomGraphicIdentity: customer.hasCustomGraphicIdentity,
      themeColors: customer.themeColors,
      gdprAlert : customer.gdprAlert,
      gdprAlertDelay : customer.gdprAlertDelay,

    };
  }

  private filteredOwners(customer: Customer): Owner[] {
    return customer.owners;
  }

}
