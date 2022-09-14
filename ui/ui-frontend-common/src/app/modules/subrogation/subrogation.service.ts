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
import { Inject, Injectable, LOCALE_ID } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBarRef } from '@angular/material/snack-bar';
import { interval, Observable, of, Subject } from 'rxjs';
import { catchError, filter, last, map, startWith, switchMap, take, takeUntil, tap } from 'rxjs/operators';

import * as moment_ from 'moment';
import { SubrogationApiService } from '../api/subrogation-api.service';
import { AuthService } from '../auth.service';
import { SUBROGRATION_REFRESH_RATE_MS } from '../injection-tokens';
import { Logger } from '../logger/logger';
import { Subrogation } from '../models';
import { SubrogationSnackBarComponent } from './subrogation-snack-bar/subrogation-snack-bar.component';

import { formatDate } from '@angular/common';
import { VitamUISnackBarService } from '../components/vitamui-snack-bar/vitamui-snack-bar.service';

const moment = moment_;

@Injectable({
  providedIn: 'root'
})
export class SubrogationService {

  subrogationCancel = new Subject();

  private subrogationSnackBarComponent: MatSnackBarRef<SubrogationSnackBarComponent>;

  private readonly TIMEOUT_SUBROGATION_MS = 300000;

  constructor(
    private logger: Logger,
    private subrogationApi: SubrogationApiService,
    private snackBarService: VitamUISnackBarService,
    private authService: AuthService,
    @Inject(SUBROGRATION_REFRESH_RATE_MS) private subrogationRefreshRateMs: number,
    @Inject(LOCALE_ID) private local: string) {}

  intervalCheck: number;

  createSubrogation(subrogation: Subrogation): Observable<Subrogation> {
    return this.subrogationApi.create(subrogation);
  }

  checkSubrogationStatus(subrogationCreated: Subrogation, dialogRef: MatDialogRef<any>) {
    const delay = 3000;

    return interval(delay).pipe(
      takeUntil(this.subrogationCancel),
      switchMap(() => this.getSubrogation(subrogationCreated.id, dialogRef)),
      filter((subrogationWatched) => subrogationWatched.status === 'ACCEPTED'),
      take(1)
    );
  }

  checkCurrentUserIsInSubrogation(): Observable<Subrogation> {
    return this.subrogationApi.getMySubrogationAsSuperuser().pipe(
      map((response) => response),
      catchError(() => of(undefined))
    );
  }

  getMySubrogation(): Observable<Subrogation> {
    return this.subrogationApi.getMySubrogationAsSuperuser();
  }

  getSubrogation(id: string, dialogRef: MatDialogRef<any>): Observable<Subrogation> {
    return this.subrogationApi.getOne(id).pipe(
      tap({
        error: (error) => {
          this.logger.error(this, error);
          dialogRef.close();
          this.snackBarService.open({
            message: 'SUBROGATION.HOME.RESULTS_TABLE.MODAL.DENIED_SUBROGATION',
            icon: 'vitamui-icon-link banner-icon'
          });
        }
      })
    );
  }

  cancelSubrogation(subrogation: Subrogation): Observable<void> {
    return this.subrogationApi.delete(subrogation.id).pipe(
      tap({
        next: () => {
          this.snackBarService.open({
            message: 'SUBROGATION.HOME.RESULTS_TABLE.MODAL.CANCEL_SUBROGATION',
            icon: 'vitamui-icon-link banner-icon'
          });
          this.subrogationCancel.next();
        },
        error: (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        }
      })
    );
  }

  accept(id: string): Observable<Subrogation> {
    return this.subrogationApi.accept(id);
  }

  decline(id: string): Observable<void> {
    return this.subrogationApi.decline(id);
  }

  getCurrent(): Observable<Subrogation> {
    return this.subrogationApi.getMySubrogationAsSurrogate();
  }

  checkSubrogation() {
    const callCount = this.TIMEOUT_SUBROGATION_MS / this.subrogationRefreshRateMs;
    const endTime = new Date();
    endTime.setMilliseconds(endTime.getMilliseconds() + this.TIMEOUT_SUBROGATION_MS);
    this.snackBarService.open( {
      message: 'SNACKBAR.ACTIVATED_SUBROGATION',
      translateParams: { duration: '5', hours: formatDate(endTime, 'H', this.local), minutes: formatDate(endTime, 'mm', this.local )},
      duration: 50000,
    });
    this.logger.log(this, callCount);
    const subrogationAccepted = new Subject();
    interval(this.subrogationRefreshRateMs).pipe(
      startWith(),
      take(callCount),
      takeUntil(subrogationAccepted),
      map(() => this.authService.user),
      filter((user) => !!user),
      switchMap(() => this.subrogationApi.getMySubrogationAsSurrogate()),
      filter((data) => data !== null),
      tap((data) => {
        if (data.status === 'CREATED' && !this.subrogationSnackBarComponent) {
          this.subrogationSnackBarComponent = this.snackBarService.openFromComponent(SubrogationSnackBarComponent, { subro: data }, 0);
          this.subrogationSnackBarComponent.afterDismissed().subscribe(() => this.subrogationSnackBarComponent = null);
        }
      }),
      map((data) => {
        if (data.status === 'ACCEPTED') {
          subrogationAccepted.next();

          return true;
        }

        return false;
      }),
      last()
    )
      .subscribe((accepted) => {
        if (!accepted) {
          this.snackBarService.open({
            message: 'SNACKBAR.FINISHED_SUBROGATION',
            icon: 'vitamui-icon-link banner-icon'
          });
        }
      });
  }
}
