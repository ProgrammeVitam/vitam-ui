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

import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ManagementContract } from 'projects/vitamui-library/src/public-api';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { SearchService } from 'ui-frontend-common';

import { ManagementContractsApiService } from '../core/api/management-contracts-api.service';
import { VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';

@Injectable({
  providedIn: 'root',
})
export class ManagementContractService extends SearchService<ManagementContract> {
  updated = new Subject<ManagementContract>();

  constructor(private managementContractApi: ManagementContractsApiService, private snackBar: MatSnackBar, http: HttpClient) {
    super(http, managementContractApi, 'ALL');
  }

  get(id: string): Observable<ManagementContract> {
    return this.managementContractApi.getOne(encodeURI(id));
  }

  getAll(): Observable<ManagementContract[]> {
    const params = new HttpParams().set('embedded', 'ALL');
    return this.managementContractApi.getAllByParams(params);
  }

  getAllForTenant(tenantId: string): Observable<ManagementContract[]> {
    const params = new HttpParams().set('embedded', 'ALL');
    const headers = new HttpHeaders().append('X-Tenant-Id', tenantId);
    return this.managementContractApi.getAllByParams(params, headers);
  }

  exists(name: string): Observable<boolean> {
    const managementContract = { name, identifier: name } as ManagementContract;
    return this.managementContractApi.check(managementContract, this.headers);
  }

  existsProperties(properties: { name?: string; identifier?: string }): Observable<any> {
    const existContract: any = {};
    if (properties.name) {
      existContract.name = properties.name;
    }
    if (properties.identifier) {
      existContract.identifier = properties.identifier;
    }

    const context = existContract as ManagementContract;
    return this.managementContractApi.check(context, this.headers);
  }

  patch(data: { id: string; [key: string]: any }): Observable<ManagementContract> {
    return this.managementContractApi.patch(data).pipe(
      tap((response) => this.updated.next(response)),
      tap(
        (response) => {
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
            data: { type: 'managementContractUpdate', name: response.name },
          });
        },
        (error) => {
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          });
        }
      )
    );
  }

  create(managementContract: ManagementContract) {
    return this.managementContractApi.create(managementContract).pipe(
      tap(
        (response: ManagementContract) => {
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'managementContractCreate', name: response.name },
            duration: 10000,
          });
        },
        (error) => {
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          });
        }
      )
    );
  }
}
