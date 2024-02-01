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
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { Context, SearchService, VitamUISnackBarService } from 'ui-frontend-common';

import { ContextApiService } from '../core/api/context-api.service';

@Injectable({
  providedIn: 'root',
})
export class ContextService extends SearchService<Context> {
  updated = new Subject<Context>();

  constructor(
    private contextApiService: ContextApiService,
    private snackBarService: VitamUISnackBarService,
    http: HttpClient,
  ) {
    super(http, contextApiService, 'ALL');
  }

  get(id: string): Observable<Context> {
    return this.contextApiService.getOne(encodeURI(id));
  }

  existsProperties(properties: { name?: string; identifier?: string }): Observable<any> {
    const existContext: any = {};
    if (properties.name) {
      existContext.name = properties.name;
    }
    if (properties.identifier) {
      existContext.identifier = properties.identifier;
    }

    const context = existContext as Context;
    return this.contextApiService.check(context, this.headers);
  }

  create(context: Context) {
    return this.contextApiService.create(context, this.headers).pipe(
      tap(
        (response: Context) => {
          this.snackBarService.open({
            message: 'SNACKBAR.CONTEXT_CREATED',
            translateParams: {
              name: response.identifier,
            },
            icon: 'vitamui-icon-admin-key',
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        },
      ),
    );
  }

  patch(data: { id: string; [key: string]: any }): Observable<Context> {
    return this.contextApiService.patch(data).pipe(
      tap((response) => this.updated.next(response)),
      tap(
        (response) => {
          this.snackBarService.open({
            message: 'SNACKBAR.CONTEXT_UPDATED',
            translateParams: {
              name: response.identifier,
            },
            icon: 'vitamui-icon-admin-key',
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        },
      ),
    );
  }

  setTenantId(tenantIdentifier: number) {
    this.headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString() });
  }
}
