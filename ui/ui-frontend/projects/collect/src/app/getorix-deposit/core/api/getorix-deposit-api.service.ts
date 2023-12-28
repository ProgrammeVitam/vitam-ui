/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { BASE_URL, BaseHttpClient } from 'ui-frontend-common';
import { GetorixDeposit } from '../model/getorix-deposit.interface';
import { GetorixUnitFullPath } from '../model/getorix-unit-full-path.interface';

@Injectable({
  providedIn: 'root',
})
export class GetorixDepositApiService extends BaseHttpClient<any> {
  baseUrl: string;

  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/getorix-deposit');
    this.baseUrl = baseUrl;
  }

  getBaseUrl() {
    return this.baseUrl;
  }

  createGetorixDeposit(getorixDeposit: GetorixDeposit): Observable<GetorixDeposit> {
    return this.http.post<any>(`${this.apiUrl}`, getorixDeposit);
  }

  getGetorixDepositById(getorixDepositId: string): Observable<GetorixDeposit> {
    return this.http.get<GetorixDeposit>(`${this.apiUrl}/` + getorixDepositId);
  }

  updateGetorixDepositInformations(getorixDeposit: GetorixDeposit): Observable<GetorixDeposit> {
    return this.http.put<GetorixDeposit>(`${this.apiUrl}/${getorixDeposit.id}`, getorixDeposit);
  }

  getUnitFullPath(unitId: string, headers?: HttpHeaders): Observable<GetorixUnitFullPath[]> {
    return this.http.get<GetorixUnitFullPath[]>(this.apiUrl + '/' + unitId + '/full-path', { headers });
  }
}
