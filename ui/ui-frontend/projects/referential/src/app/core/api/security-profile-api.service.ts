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
import {HttpClient, HttpHeaders, HttpParams, HttpResponse} from '@angular/common/http';
import {Inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {BASE_URL, BaseHttpClient, PageRequest, PaginatedResponse} from 'ui-frontend-common';
import {SecurityProfile} from '../../../../../vitamui-library/src/lib/models/security-profile';

const HTTP_STATUS_OK = 200;

@Injectable({
  providedIn: 'root'
})
export class SecurityProfileApiService extends BaseHttpClient<SecurityProfile> {

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/security-profile');
  }

  getAllByParams(params: HttpParams, headers?: HttpHeaders) {
    return super.getAllByParams(params, headers);
  }

  getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<SecurityProfile>> {
    return super.getAllPaginated(pageRequest, embedded, headers);
  }

  getOne(id: string, headers?: HttpHeaders): Observable<SecurityProfile> {
    return super.getOne(id, headers);
  }

  patch(partialSecurityProfile: { id: string, [key: string]: any }, headers?: HttpHeaders) {
    return super.patch(partialSecurityProfile, headers);
  }

  create(securityProfile: SecurityProfile, headers?: HttpHeaders): Observable<SecurityProfile> {
    return super.getHttp().post<any>(super.getApiUrl(), securityProfile, {headers});
  }

  check(securityProfile: SecurityProfile, headers?: HttpHeaders): Observable<boolean> {
    return super.getHttp().post<any>(super.getApiUrl() + '/check', securityProfile, {observe: 'response', headers})
      .pipe(map((response: HttpResponse<void>) => response.status === HTTP_STATUS_OK));
  }

  delete(id: string, headers?: HttpHeaders) {
    return super.getHttp().delete(super.getApiUrl() + '/' + id, {headers});
  }

}
