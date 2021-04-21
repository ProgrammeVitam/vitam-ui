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
import { uniqueId } from 'lodash-es';
import * as moment_ from 'moment';
import { Observable, throwError } from 'rxjs';
import { catchError, tap, timeout } from 'rxjs/operators';

import {
  HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse
} from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AuthService } from './auth.service';
import { ErrorDialogComponent } from './error-dialog/error-dialog.component';
import { ENVIRONMENT, WINDOW_LOCATION } from './injection-tokens';
import { Logger } from './logger/logger';
import { StartupService } from './startup.service';

const moment = moment_;

const HTTP_STATUS_CODE_BAD_REQUEST = 400;
const HTTP_STATUS_CODE_UNAUTHORIZED = 401;
const HTTP_STATUS_CODE_FORBIDDEN = 403;
const HTTP_STATUS_CODE_NOT_FOUND = 404;
const HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
const HTTP_STATUS_CODE_SERVICE_UNAVAILABLE = 503;
const HTTP_STATUS_CODE_GATEWAY_TIMEOUT = 504;
const DEFAULT_API_TIMEOUT = 50000;
const CLIENT_ERROR_START = 400;
const SERVER_ERROR_START = 500;

const NOTIFICATION_DELAY_MS = 20000;

@Injectable()
export class VitamUIHttpInterceptor implements HttpInterceptor {

  private errorDialog: MatDialogRef<ErrorDialogComponent>;

  private apiTimeout: number;

  constructor(
    private logger: Logger,
    private matDialog: MatDialog,
    private snackBar: MatSnackBar,
    private startupService: StartupService,
    private authService: AuthService,
    @Inject(ENVIRONMENT) private environment: any,
    @Inject(WINDOW_LOCATION) private location: any
  ) {
    if (environment && environment.apiTimeout != null) {
      this.apiTimeout = environment.apiTimeout;
    } else {
      this.apiTimeout = DEFAULT_API_TIMEOUT;
    }
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let tenantIdentifier = request.headers.get('X-Tenant-Id');
    if ((!tenantIdentifier || tenantIdentifier === undefined || tenantIdentifier === '') && this.authService.user) {
      tenantIdentifier = this.startupService.getTenantIdentifier();
    }
    let requestId = request.headers.get('X-Request-Id');
    if (!requestId) {
      requestId = uniqueId('' + Math.floor(Math.random() * Math.floor(moment().unix())));
    }
    let applicationId = request.headers.get('X-Application-Id');
    if (!applicationId) {
      applicationId = this.startupService.CURRENT_APP_ID;
    }
    const headerTimeout = request.headers.get('X-Api-Timeout');
    if (headerTimeout && !isNaN(Number(headerTimeout))) {
      this.apiTimeout = Number(headerTimeout);
    }

    const reqWithCredentials = request.clone({
      withCredentials: true,
      headers: request.headers.delete('X-By-Passed-Error'),
      setHeaders: {
        'X-Request-Id': requestId,
        'X-Request-Timestamp': moment().unix().toString(),
        'X-Requested-With': 'XMLHttpRequest',
        'X-Tenant-Id': tenantIdentifier ? tenantIdentifier.toString() : '-1',
        'X-Application-Id': applicationId,
      }
    });

    let errorToByPass: number = null;
    if (request.headers.has('X-By-Passed-Error')) {
      errorToByPass = +request.headers.get('X-By-Passed-Error');
    }

    return next.handle(reqWithCredentials)
      .pipe(
        timeout(this.apiTimeout),
        tap((ev: HttpEvent<any>) => {
          if (ev instanceof HttpResponse) {
            this.logger.log(this, 'processing response', ev);
          }
        }),
        catchError((response) => {
          if (response instanceof HttpErrorResponse && response.status !== errorToByPass) {
            this.logger.log(this, 'Processing http error', response);
            if (response.status === HTTP_STATUS_CODE_UNAUTHORIZED) {
              // const loginRedirectUrl = response.headers.get('x-login-redirect');
              // this.router.navigate(['login-redirect'], { queryParams: { url: loginRedirectUrl } });
              if (!this.environment.production && request && request.url.endsWith('/security')) {
                // MDI : hack for dev purposes, with the first connection, we redirect the user to login
                this.authService.user = null;
                this.location.href = this.startupService.getLoginUrl();
              } else {
                // connection was lost, we need to logout the user
                this.authService.logout();
              }
            } else if (response.status === HTTP_STATUS_CODE_FORBIDDEN) {
              this.snackBar.open('Vous n\'avez pas les droits nécessaires pour effectuer cette opération.', null, {
                panelClass: 'vitamui-snack-bar',
                duration: NOTIFICATION_DELAY_MS,
              });
            } else if (response.status === HTTP_STATUS_CODE_BAD_REQUEST) {
              this.snackBar.open('Erreur : requête invalide', null, {
                panelClass: 'vitamui-snack-bar',
                duration: NOTIFICATION_DELAY_MS,
              });
            } else if (response.status === HTTP_STATUS_CODE_NOT_FOUND) {
              this.snackBar.open('Erreur : La ressource n\'existe pas', null, {
                panelClass: 'vitamui-snack-bar',
                duration: NOTIFICATION_DELAY_MS,
              });
            } else if (response.status === HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR) {
              if (!this.errorDialog) {
                this.errorDialog = this.matDialog.open(ErrorDialogComponent, {
                  panelClass: 'vitamui-modal',
                });
                this.errorDialog.afterClosed().subscribe(() => this.errorDialog = null);
              }
            } else if (response.status === HTTP_STATUS_CODE_SERVICE_UNAVAILABLE) {
              this.snackBar.open('Une erreur technique est survenue : service indisponible', null, {
                panelClass: 'vitamui-snack-bar',
                duration: NOTIFICATION_DELAY_MS,
              });
            } else if (response.status === HTTP_STATUS_CODE_GATEWAY_TIMEOUT) {
              this.snackBar.open('Une erreur technique est survenue : le serveur ne répond pas.', null, {
                panelClass: 'vitamui-snack-bar',
                duration: NOTIFICATION_DELAY_MS,
              });
            } else if (response.status >= CLIENT_ERROR_START) {
              this.snackBar.open('Une erreur technique est survenue : requête invalide', null, {
                panelClass: 'vitamui-snack-bar',
                duration: NOTIFICATION_DELAY_MS,
              });
            } else if (response.status >= SERVER_ERROR_START) {
              this.snackBar.open('Une erreur technique est survenue : erreur interne', null, {
                panelClass: 'vitamui-snack-bar',
                duration: NOTIFICATION_DELAY_MS,
              });
            }
          } else if (response && response.name === 'TimeoutError') {
            this.logger.log(this, 'Timeout error', response);
            this.snackBar.open('Une erreur technique est survenue : le serveur ne répond pas.', null, {
              panelClass: 'vitamui-snack-bar',
              duration: NOTIFICATION_DELAY_MS,
            });
          } else {
            this.logger.log(this, 'Request error', response);
          }

          return throwError(response);
        })
      );
  }
}
