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
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest, HttpResponse } from '@angular/common/http';
import { Inject, Injectable, Injector } from '@angular/core';
import { MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import moment from 'moment';
import { Observable, throwError } from 'rxjs';

import { catchError, tap, timeoutWith } from 'rxjs/operators';
import { AuthService } from './auth.service';

import { VitamUISnackBarService } from './components/vitamui-snack-bar/vitamui-snack-bar.service';
import { ErrorDialogComponent } from './error-dialog/error-dialog.component';
import { ENVIRONMENT } from './injection-tokens';
import { Logger } from './logger/logger';

import { VitamUITimeoutError } from './models/http-interceptor/vitamui-timeout-error';
import { StartupService } from './startup.service';
import { SKIP_ERROR_NOTIFICATION } from './utils';

const URLS_INCREASED_TIMEOUT = ['file', 'download', 'export', 'documents', 'ingest'];
// @ts-ignore
const HTTP_STATUS_CODE_BAD_REQUEST = 400;
const HTTP_STATUS_CODE_UNAUTHORIZED = 401;
const HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR = 500;
const DEFAULT_API_TIMEOUT = 50000;
// @ts-ignore
const CLIENT_ERROR_START = 400;
// @ts-ignore
const SERVER_ERROR_START = 500;

// @ts-ignore
const NOTIFICATION_DELAY_MS = 20000;
const DEFAULT_DOWNLOAD_UPLOAD_API_TIMEOUT = 1000 * 60 * 60 * 5;
const DEFAULT_SERVER_ERROR_MESSAGE = 'EXCEPTIONS.HTTP_INTERCEPTOR.SERVER_ERROR_START';
const ERROR_NOTIFICATION_MESSAGE_BY_HTTP_STATUS: Map<number, string> = new Map([
  [403, 'EXCEPTIONS.HTTP_INTERCEPTOR.HTTP_STATUS_CODE_FORBIDDEN'],
  [400, 'EXCEPTIONS.HTTP_INTERCEPTOR.HTTP_STATUS_CODE_BAD_REQUEST'],
  [404, 'EXCEPTIONS.HTTP_INTERCEPTOR.HTTP_STATUS_CODE_NOT_FOUND'],
  [503, 'EXCEPTIONS.HTTP_INTERCEPTOR.HTTP_STATUS_CODE_SERVICE_UNAVAILABLE'],
  [504, 'EXCEPTIONS.HTTP_INTERCEPTOR.HTTP_STATUS_CODE_GATEWAY_TIMEOUT'],
  [417, 'EXCEPTIONS.HTTP_INTERCEPTOR.HTTP_STATUS_CODE_EXPECTATION_FAILED'],
  [412, 'EXCEPTIONS.HTTP_INTERCEPTOR.HTTP_STATUS_PRECONDITION_FAILED_EXCEPTION'],
  [408, 'EXCEPTIONS.HTTP_INTERCEPTOR.HTTP_STATUS_CODE_REQUEST_TIMEOUT'],
]);

@Injectable()
export class VitamUIHttpInterceptor implements HttpInterceptor {
  private errorDialog: MatDialogRef<ErrorDialogComponent>;
  private apiTimeout: number;
  private snackBarService: VitamUISnackBarService;

  constructor(
    private logger: Logger,
    private matDialog: MatDialog,
    private startupService: StartupService,
    private authService: AuthService,
    private injector: Injector,
    @Inject(ENVIRONMENT) private environment: any,
  ) {
    this.apiTimeout = environment?.apiTimeout ? environment.apiTimeout : DEFAULT_API_TIMEOUT;
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    this.initSnackBarService();

    let tenantIdentifier = request.headers.get('X-Tenant-Id');
    if ((!tenantIdentifier || tenantIdentifier === '') && this.authService.user) {
      // TODO: change to tenant-selection.service
      tenantIdentifier = this.startupService.getTenantIdentifier();
    }
    let requestId = request.headers.get('X-Request-Id');
    if (!requestId) {
      requestId = '' + Math.floor(Math.random() * 10 ** 15);
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
      },
    });

    let errorToByPass: number = null;
    if (request.headers.has('X-By-Passed-Error')) {
      errorToByPass = +request.headers.get('X-By-Passed-Error');
    }

    return next.handle(reqWithCredentials).pipe(
      timeoutWith(
        URLS_INCREASED_TIMEOUT.some((url) => request.url.includes(url)) ? DEFAULT_DOWNLOAD_UPLOAD_API_TIMEOUT : this.apiTimeout,
        throwError(new VitamUITimeoutError()),
      ),
      tap((ev: HttpEvent<any>) => {
        if (ev instanceof HttpResponse) {
          this.logger.log(this, 'processing response', ev);
        }
      }),
      catchError((response) => {
        if (response instanceof HttpErrorResponse && response.status !== errorToByPass) {
          this.logger.log(this, 'Processing http error', response);
          if (response.status === HTTP_STATUS_CODE_UNAUTHORIZED) {
            if (!this.environment.production && request && request.url.endsWith('/security')) {
              // MDI : hack for dev purposes, with the first connection, we redirect the user to login
              this.authService.redirectToLoginPage();
            } else {
              // connection was lost, we need to logout the user
              this.authService.logout();
            }
          } else {
            this.errorNotification(response, request);
          }
        }
        this.logger.log(this, 'Request error', response);
        return throwError(response);
      }),
    );
  }

  private initSnackBarService(): void {
    if (!this.snackBarService) {
      try {
        this.snackBarService = this.injector.get(VitamUISnackBarService);
      } catch (error) {}
    }
  }

  private errorNotification(response: HttpErrorResponse, request: HttpRequest<any>): void {
    if (!request.headers.has(SKIP_ERROR_NOTIFICATION)) {
      response.status === HTTP_STATUS_CODE_INTERNAL_SERVER_ERROR ? this.displayErrorDialog() : this.displaySnackBar(response);
    }
  }

  private displaySnackBar(response: HttpErrorResponse): void {
    const errorMsg = ERROR_NOTIFICATION_MESSAGE_BY_HTTP_STATUS.has(response.status)
      ? ERROR_NOTIFICATION_MESSAGE_BY_HTTP_STATUS.get(response.status)
      : DEFAULT_SERVER_ERROR_MESSAGE;
    this.snackBarService.open({ message: errorMsg });
  }

  private displayErrorDialog(): void {
    if (!this.errorDialog) {
      this.errorDialog = this.matDialog.open(ErrorDialogComponent, {
        panelClass: 'vitamui-modal',
      });
      this.errorDialog.afterClosed().subscribe(() => (this.errorDialog = null));
    }
  }
}
