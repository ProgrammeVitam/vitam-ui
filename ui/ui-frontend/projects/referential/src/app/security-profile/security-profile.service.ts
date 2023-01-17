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
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { tap } from 'rxjs/operators';
import { SearchService, VitamUISnackBarService } from 'ui-frontend-common';

import { SecurityProfile } from 'projects/vitamui-library/src/lib/models/security-profile';
import { SecurityProfileApiService } from '../core/api/security-profile-api.service';

@Injectable({
  providedIn: 'root'
})
export class SecurityProfileService extends SearchService<SecurityProfile> {

  updated = new Subject<SecurityProfile>();

  constructor(
    private securityProfileApiService: SecurityProfileApiService,
    private snackBarService: VitamUISnackBarService,
    http: HttpClient) {
    super(http, securityProfileApiService, 'ALL');
  }

  get(id: string): Observable<SecurityProfile> {
    return this.securityProfileApiService.getOne(encodeURI(id));
  }

  getAll(): Observable<SecurityProfile[]> {
    const params = new HttpParams().set('embedded', 'ALL');
    return this.securityProfileApiService.getAllByParams(params);
  }

  existsProperties(properties: { name?: string, identifier?: string }): Observable<any> {
    const existSecurityProfile: any = {};
    if (properties.name) {
      existSecurityProfile.name = properties.name;
    }
    if (properties.identifier) {
      existSecurityProfile.identifier = properties.identifier;
    }

    const context = existSecurityProfile as SecurityProfile;
    return this.securityProfileApiService.check(context, this.headers);
  }

  create(profile: SecurityProfile) {
    return this.securityProfileApiService.create(profile, this.headers)
      .pipe(
        tap(
          (_: SecurityProfile) => {
            this.snackBarService.open({
              message: 'SNACKBAR.SECURITY_CREATED',
              icon: 'vitamui-icon-admin-key'
            });
          },
          (error) => {
            this.snackBarService.open({ message: error.error.message, translate: false });
          }
        )
      );
  }

  patch(data: { id: string, [key: string]: any }): Observable<SecurityProfile> {
    return this.securityProfileApiService.patch(data)
      .pipe(
        tap((response) => this.updated.next(response)),
        tap(
          (_) => {
            this.snackBarService.open({
              message: 'SNACKBAR.SECURITY_UPDATED',
              icon: 'vitamui-icon-admin-key'
            });
          },
          (error) => {
            this.snackBarService.open({ message: error.error.message, translate: false });
          }
        )
      );
  }

  delete(profile: SecurityProfile): Observable<any> {
    return this.securityProfileApiService.delete(profile.id).pipe(
      tap(() => {
          this.snackBarService.open({
            message: 'SNACKBAR.SECURITY_DELETED',
            icon: 'vitamui-icon-admin-key'
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        })
    );
  }

}
