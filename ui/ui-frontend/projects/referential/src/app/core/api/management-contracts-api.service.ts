/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 /*
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
 *
 *
 */

import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BaseHttpClient, BASE_URL, ManagementContract, PageRequest, PaginatedResponse } from 'vitamui-library';

const HTTP_STATUS_OK = 200;

@Injectable({
  providedIn: 'root',
})
export class ManagementContractsApiService extends BaseHttpClient<ManagementContract> {
  constructor(http: HttpClient, @Inject(BASE_URL) baseUrl: string) {
    super(http, baseUrl + '/management-contract');
  }

  getAllByParams(params: HttpParams, headers?: HttpHeaders) {
    return super.getAllByParams(params, headers);
  }

  getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<ManagementContract>> {
    return super.getAllPaginated(pageRequest, embedded, headers);
  }

  getOne(id: string, headers?: HttpHeaders): Observable<ManagementContract> {
    return super.getOne(id, headers);
  }

  checkExistsByParam(params: Array<{ key: string; value: string }>, headers?: HttpHeaders): Observable<boolean> {
    return super.checkExistsByParam(params, headers);
  }

  create(managementContract: ManagementContract, headers?: HttpHeaders): Observable<ManagementContract> {
    return super.getHttp().post<ManagementContract>(super.getApiUrl(), managementContract, { headers });
  }

  check(managementContract: ManagementContract, headers?: HttpHeaders): Observable<boolean> {
    return super
      .getHttp()
      .post<void>(super.getApiUrl() + '/check', managementContract, { observe: 'response', headers })
      .pipe(map((response: HttpResponse<void>) => response.status === HTTP_STATUS_OK));
  }

  patch(partialmanagementContract: { id: string; [key: string]: any }, headers?: HttpHeaders) {
    return super.patch(partialmanagementContract, headers);
  }
}
