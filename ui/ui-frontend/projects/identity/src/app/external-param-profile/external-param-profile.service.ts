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
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { map, tap } from 'rxjs/operators';
import {
  AccessContract,
  AccessContractApiService,
  CriteriaSearchQuery,
  Criterion,
  ExternalParamProfile,
  ExternalParamProfileApiService,
  Operators,
  SearchService,
  VitamUISnackBarService,
} from 'vitamui-library';

@Injectable({
  providedIn: 'root',
})
export class ExternalParamProfileService extends SearchService<ExternalParamProfile> {
  updated = new Subject<ExternalParamProfile>();

  constructor(
    private externalParamProfileApi: ExternalParamProfileApiService,
    private accessContractApiService: AccessContractApiService,
    private snackBarService: VitamUISnackBarService,
  ) {
    super(externalParamProfileApi);
  }

  getOne(id: string): Observable<ExternalParamProfile> {
    return this.externalParamProfileApi.getOne(id);
  }

  getAllActiveAccessContracts(tenantIdentifier: string): Observable<AccessContract[]> {
    const params = new HttpParams();
    const headers = new HttpHeaders().append('X-Tenant-Id', tenantIdentifier);
    return this.accessContractApiService
      .getAllAccessContracts(params, headers)
      .pipe(map((accessContracts) => accessContracts.filter((accessContract) => accessContract.status === 'ACTIVE')));
  }

  create(externalParamProfile: ExternalParamProfile) {
    return this.externalParamProfileApi.create(externalParamProfile).pipe(
      tap((response: ExternalParamProfile) => {
        this.snackBarService.open({
          message: 'EXTERNAL_PARAM_PROFILE.NOTIF_EXTERNAL_PARAM_PROFILE_CREATED',
          icon: 'vitamui-icon-admin-key',
          translateParams: { name: response.name },
        });
      }),
    );
  }

  patch(data: { id: string; [key: string]: any }): Observable<ExternalParamProfile> {
    return this.externalParamProfileApi.patch(data).pipe(
      tap((response) => this.updated.next(response)),
      tap(
        (response) => {
          this.snackBarService.open({
            message: 'EXTERNAL_PARAM_PROFILE.NOTIF_EXTERNAL_PARAM_PROFILE_UPDATED',
            icon: 'vitamui-icon-admin-key',
            translateParams: { name: response.name },
          });
        },
        (error) => this.snackBarService.open({ message: error.error.message, translate: false }),
      ),
    );
  }

  exists(tenantIdentifier: number, applicationName: string, name: string): Observable<boolean> {
    const criterionArray = [];
    const criterionTenantIdentifier: Criterion = { key: 'tenantIdentifier', value: tenantIdentifier, operator: Operators.equals };
    const criterionApplicationName: Criterion = { key: 'applicationName', value: applicationName, operator: Operators.equals };
    const criterionName: Criterion = { key: 'name', value: name, operator: Operators.equalsIgnoreCase };
    criterionArray.push(criterionName, criterionTenantIdentifier, criterionApplicationName);
    const query: CriteriaSearchQuery = { criteria: criterionArray };

    const params = [{ key: 'criteria', value: JSON.stringify(query) }];

    return this.externalParamProfileApi.checkExistsByParam(params, this.headers);
  }
}
