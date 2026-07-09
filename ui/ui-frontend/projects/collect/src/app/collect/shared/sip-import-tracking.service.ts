/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { distinctUntilChanged, finalize, map } from 'rxjs/operators';
import { Logger } from 'vitamui-library';
import { TransactionApiService } from '../core/api/transaction-api.service';
import { OperationState } from '../models/operation-status.interface';
import { pollUntil } from '../transactions/polling';

/**
 * Tracks the SIP import workflows (Vitam operations) launched on transactions during this session.
 * As long as the import operation of a transaction is not COMPLETED, the transaction must not be
 * validated, otherwise Vitam raises a technical error.
 */
@Injectable({
  providedIn: 'root',
})
export class SipImportTrackingService {
  private transactionApiService = inject(TransactionApiService);
  private logger = inject(Logger);

  private static readonly POLLING_PERIOD_MS = 5_000;
  private static readonly MAX_POLLING_RETRIES = 720; // stop polling after one hour

  private pendingTransactionIds$ = new BehaviorSubject<Set<string>>(new Set());

  /** Starts polling the SIP import operation status until it is completed. */
  trackSipImport(transactionId: string, operationId: string): void {
    if (!transactionId || !operationId || this.isSipImportInProgress(transactionId)) {
      return;
    }
    this.setPending(transactionId, true);
    const status$ = this.transactionApiService.getOperationStatus(operationId);
    pollUntil(status$, {
      period: SipImportTrackingService.POLLING_PERIOD_MS,
      maxRetries: SipImportTrackingService.MAX_POLLING_RETRIES,
      until: (status) => status.globalState === OperationState.COMPLETED,
    })
      // On error or timeout, stop blocking the validation (same behavior as before this guard)
      .pipe(finalize(() => this.setPending(transactionId, false)))
      .subscribe({
        error: (error) => this.logger.warn(this, `Unable to track SIP import operation ${operationId}`, error),
      });
  }

  isSipImportInProgress(transactionId: string): boolean {
    return this.pendingTransactionIds$.value.has(transactionId);
  }

  sipImportInProgress$(transactionId: string): Observable<boolean> {
    return this.pendingTransactionIds$.pipe(
      map((pendingIds) => pendingIds.has(transactionId)),
      distinctUntilChanged(),
    );
  }

  private setPending(transactionId: string, pending: boolean): void {
    const pendingIds = new Set(this.pendingTransactionIds$.value);
    if (pending) {
      pendingIds.add(transactionId);
    } else {
      pendingIds.delete(transactionId);
    }
    this.pendingTransactionIds$.next(pendingIds);
  }
}
