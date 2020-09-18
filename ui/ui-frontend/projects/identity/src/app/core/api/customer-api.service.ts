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
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BASE_URL, BaseHttpClient, Customer, Logger, PageRequest, PaginatedResponse } from 'ui-frontend-common';

@Injectable({
  providedIn: 'root'
})
export class CustomerApiService extends BaseHttpClient<Customer> {

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string, private logger: Logger) {
    super(http, baseUrl + '/customers');
  }

  getAllByParams(params: HttpParams, headers?: HttpHeaders) {
    return super.getAllByParams(params, headers);
  }

  getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<Customer>> {
    return super.getAllPaginated(pageRequest, embedded, headers);
  }

  getOne(id: string, headers?: HttpHeaders): Observable<Customer> {
    return super.getOne(id, headers);
  }

  checkExistsByParam(params: Array<{ key: string, value: string }>, headers?: HttpHeaders): Observable<boolean> {
    return super.checkExistsByParam(params, headers);
  }

  createCustomer(customer: Customer, logo?: File, headers?: HttpHeaders): Observable<Customer> {
    const formData: FormData = new FormData();
    formData.append('customerDto', JSON.stringify({
      id: customer.id,
      enabled: customer.enabled,
      code: customer.code,
      identifier: customer.identifier,
      name: customer.name,
      companyName: customer.companyName,
      passwordRevocationDelay: customer.passwordRevocationDelay,
      otp: customer.otp,
      idp: customer.idp,
      address: customer.address,
      addressType: customer.addressType,
      internalCode: customer.internalCode,
      language: customer.language,
      emailDomains: customer.emailDomains,
      defaultEmailDomain: customer.defaultEmailDomain,
      owners: customer.owners,
      readonly: customer.readonly,
      themeColors: customer.themeColors
    }));
    if (logo) {
      formData.append('logo', logo);
    }
    return super.getHttp().post<any>(super.getApiUrl(), formData, { headers });
  }

  patchCustomer(partialCustomer: { id: string, [key: string]: any }, logo?: File, headers?: HttpHeaders): Observable<Customer> {
    const formData: FormData = new FormData();
    formData.append('partialCustomerDto', JSON.stringify(partialCustomer));
    if (logo) {
      formData.append('logo', logo);
    }
    this.logger.log(this, 'Form data => ', formData);
    return super.getHttp().patch<any>(super.getApiUrl() + '/' + partialCustomer.id, formData, { headers });
  }

  getMyCustomer(): Observable<Customer> {
    return super.getHttp().get<any>(super.getApiUrl() + '/me');
  }

  getCustomerLogo(id: string): Observable<HttpResponse<Blob>> {
    return super.getHttp().get(super.getApiUrl() + '/' + id + '/logo', { observe: 'response', responseType: 'blob' });
  }

}
