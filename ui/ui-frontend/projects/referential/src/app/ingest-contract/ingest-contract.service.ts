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
import {HttpClient, HttpHeaders, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {IngestContract} from 'projects/vitamui-library/src/public-api';
import {Observable, Subject} from 'rxjs';
import {tap} from 'rxjs/operators';
import {SearchService} from 'ui-frontend-common';

import {IngestContractApiService} from '../core/api/ingest-contract-api.service';
import {VitamUISnackBarComponent} from '../shared/vitamui-snack-bar';

@Injectable({
  providedIn: 'root'
})
export class IngestContractService extends SearchService<IngestContract> {

  updated = new Subject<IngestContract>();

  constructor(private ingestContractApi: IngestContractApiService,
              private snackBar: MatSnackBar,
              http: HttpClient) {
    super(http, ingestContractApi, 'ALL');
  }

  get(id: string): Observable<IngestContract> {
    return this.ingestContractApi.getOne(encodeURI(id));
  }

  getAll(): Observable<IngestContract[]> {
    const params = new HttpParams().set('embedded', 'ALL');
    return this.ingestContractApi.getAllByParams(params);
  }

  getAllForTenant(tenantId: string): Observable<IngestContract[]> {
    // TODO: Check add of tenantId
    const params = new HttpParams().set('embedded', 'ALL');
    const headers = new HttpHeaders().append('X-Tenant-Id', tenantId);
    return this.ingestContractApi.getAllByParams(params, headers);
  }

  exists(name: string): Observable<boolean> {
    const ingestContract = {name, identifier: name} as IngestContract;
    return this.ingestContractApi.check(ingestContract, this.headers);
  }

  existsProperties(properties: { name?: string, identifier?: string }): Observable<any> {
    const existContract: any = {};
    if (properties.name) {
      existContract.name = properties.name;
    }
    if (properties.identifier) {
      existContract.identifier = properties.identifier;
    }

    const context = existContract as IngestContract;
    return this.ingestContractApi.check(context, this.headers);
  }

  patch(data: { id: string, [key: string]: any }): Observable<IngestContract> {
    return this.ingestContractApi.patch(data)
      .pipe(
        tap((response) => this.updated.next(response)),
        tap(
          (response) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000,
              data: {type: 'ingestContractUpdate', name: response.name}
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

  create(ingestContract: IngestContract) {
    return this.ingestContractApi.create(ingestContract)
      .pipe(
        tap(
          (response: IngestContract) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: {type: 'ingestContractCreate', name: response.name},
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
}
