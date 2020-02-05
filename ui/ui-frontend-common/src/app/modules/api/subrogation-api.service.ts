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
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { AuthService } from '../auth.service';
import { BaseHttpClient } from '../base-http-client';
import { BASE_URL } from '../injection-tokens';
import { Group, Subrogation } from '../models';

@Injectable({
  providedIn: 'root'
})
export class SubrogationApiService extends BaseHttpClient<Subrogation> {

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string,
              private authService: AuthService) {
    super(http, baseUrl + '/subrogations');
  }

  getAllByParams(params: HttpParams, headers?: HttpHeaders) {
    return super.getAllByParams(params, headers);
  }

  getOne(id: string, headers?: HttpHeaders): Observable<Subrogation> {
    return super.getOne(id, headers);
  }

  getMySubrogationAsSuperuser(): Observable<Subrogation> {
    const headers = new HttpHeaders({ 'X-Tenant-Id': this.authService.getAnyTenantIdentifier() });

    return this.http.get<Subrogation>(this.apiUrl + '/me/superuser', { headers });
  }

  getMySubrogationAsSurrogate(): Observable<Subrogation> {
    const headers = new HttpHeaders({ 'X-Tenant-Id': this.authService.getAnyTenantIdentifier() });

    return this.http.get<Subrogation>(this.apiUrl + '/me/surrogate', { headers });
  }

  accept(id: string): Observable<Subrogation> {
    return this.http.patch<Subrogation>(this.apiUrl + '/surrogate/accept/' + id, {});
  }

  decline(id: string): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/surrogate/decline/' + id);
  }

  create(subrogation: Subrogation, headers?: HttpHeaders): Observable<Subrogation> {
    return super.create(subrogation, headers);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(this.apiUrl + '/' + id);
  }

  getGroupById(id: string, headers?: HttpHeaders): Observable<Group> {
    const params = new HttpParams();

    return this.http.get<Group>(this.apiUrl + '/groups/' + id, { params, headers });
  }
}
