import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {tap} from 'rxjs/operators';
import {SearchService, VitamUISnackBar} from 'ui-frontend-common';

import {SecurityProfile} from 'projects/vitamui-library/src/lib/models/security-profile';
import {SecurityProfileApiService} from '../core/api/security-profile-api.service';
import {VitamUISnackBarComponent} from '../shared/vitamui-snack-bar';

@Injectable({
  providedIn: 'root'
})
export class SecurityProfileService extends SearchService<SecurityProfile> {

  updated = new Subject<SecurityProfile>();

  constructor(
    private securityProfileApiService: SecurityProfileApiService,
    private snackBar: VitamUISnackBar,
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
          (response: SecurityProfile) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              data: {type: 'securityProfileCreate', name: response.identifier},
              duration: 10000
            });
          },
          (error: any) => {
            this.snackBar.open(error.error.message, null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000
            });
          }
        )
      );
  }

  patch(data: { id: string, [key: string]: any }): Observable<SecurityProfile> {
    return this.securityProfileApiService.patch(data)
      .pipe(
        tap((response) => this.updated.next(response)),
        tap(
          (response) => {
            this.snackBar.openFromComponent(VitamUISnackBarComponent, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000,
              data: {type: 'securityProfileUpdate', name: response.identifier}
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

  delete(profile: SecurityProfile): Observable<any> {
    return this.securityProfileApiService.delete(profile.id).pipe(
      tap(() => {
          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
            data: {type: 'securityProfileDelete', name: profile.identifier}
          });
        },
        (error) => {
          this.snackBar.open(error.error.message, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000
          });
        })
    );
  }

}
