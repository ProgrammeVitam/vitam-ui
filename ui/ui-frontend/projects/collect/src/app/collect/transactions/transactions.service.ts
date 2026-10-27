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
import { BehaviorSubject, Observable, switchMap } from 'rxjs';
import { catchError, last, map, tap } from 'rxjs/operators';
import {
  BatchStatus,
  DEFAULT_PAGE_SIZE,
  Direction,
  PageRequest,
  PaginatedResponse,
  Project,
  SearchService,
  SnackBarService,
  Transaction,
  TransactionStatus,
} from 'vitamui-library';
import { ProjectsApiService } from '../core/api/project-api.service';
import { TransactionApiService } from '../core/api/transaction-api.service';
import { pollUntil } from './polling';
import { TransactionValidationMode } from '../models/transaction-validation-mode.enum';

const DEFAULT_VALIDATION_CONTEXT = { isAutomaticIngest: false };

@Injectable()
export class TransactionsService extends SearchService<Transaction> {
  private readonly transactionApiService: TransactionApiService;
  private readonly projectApiService = inject(ProjectsApiService);
  private readonly snackBarService = inject(SnackBarService);

  transactions$: BehaviorSubject<Transaction[]> = new BehaviorSubject<Transaction[]>([]);
  project$: BehaviorSubject<Project> = new BehaviorSubject<Project>(null);

  constructor() {
    const transactionApiService = inject(TransactionApiService);

    super(transactionApiService, 'ALL');

    this.transactionApiService = transactionApiService;
  }

  public getTransactionsByProjectId(
    projectId: string,
    pageRequest: PageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'id', Direction.ASCENDANT),
  ): Observable<PaginatedResponse<Transaction>> {
    this.pageRequest = pageRequest;
    return this.projectApiService.getTransactionsByProjectId(this.pageRequest, projectId);
  }

  public getProject$(): Observable<Project> {
    return this.project$;
  }

  public override search(_: PageRequest = null): Observable<Transaction[]> {
    return this.transactions$;
  }

  public getTransactionById(transactionById: string) {
    return this.transactionApiService.getTransactionById(transactionById);
  }

  public validate(
    transaction: Transaction,
    validationMode: TransactionValidationMode,
    context = { isAutomaticIngest: false },
  ): Observable<Transaction> {
    return this.validateTransaction(transaction.id, validationMode).pipe(
      switchMap(() => {
        const status$ = this.getTransactionById(transaction.id).pipe(map((next) => next.status));
        const isValidated = (status: TransactionStatus) => {
          return status === TransactionStatus.VALIDATED;
        };
        const isNotSending = (status: TransactionStatus) => {
          return status !== TransactionStatus.SENDING;
        };
        const isFinalState = context.isAutomaticIngest ? isNotSending : isValidated;
        const pollingOptions = { period: 3000, maxRetries: 30, until: isFinalState };

        return pollUntil(status$, pollingOptions);
      }),
      map((status) => ({ ...transaction, status: status })),
    );
  }

  validateTransaction(id: string, validationMode: TransactionValidationMode) {
    return this.transactionApiService.validateTransaction(id, validationMode);
  }

  sendTransaction(id: string) {
    return this.transactionApiService.sendTransaction(id);
  }

  editTransaction(id: string) {
    return this.transactionApiService.editTransaction(id);
  }

  abortTransaction(id: string) {
    return this.transactionApiService.abortTransaction(id);
  }

  public hasBatchInError(transaction: Transaction): boolean {
    return transaction.batches?.some((b) => b.BatchStatus === BatchStatus.KO) ?? false;
  }

  /**
   * Validates the transaction (polling until a final state is reached) and notifies the user of the outcome.
   * Callers should not infer or set the transaction status locally: reload the list to reflect the real server state.
   *
   * `validate()` polls and can emit several interim statuses before completing (e.g. for automatic ingest, it stops
   * as soon as the status is no longer SENDING, which isn't necessarily VALIDATED): only the last emission reflects
   * the actual outcome, and is what the success/error notification must be based on.
   */
  public validateAndNotify(
    transaction: Transaction,
    validationMode: TransactionValidationMode,
    context = DEFAULT_VALIDATION_CONTEXT,
  ): Observable<Transaction> {
    return this.validate(transaction, validationMode, context).pipe(
      last(),
      tap({
        next: (result) => {
          const message = this.isFailedStatus(result.status)
            ? 'COLLECT.VALIDATE_TRANSACTION_ERROR'
            : 'COLLECT.VALIDATE_TRANSACTION_VALIDATED';
          this.snackBarService.open({ message, duration: 10_000 });
        },
        error: () => this.snackBarService.open({ message: 'COLLECT.VALIDATE_TRANSACTION_ERROR', duration: 10_000 }),
      }),
    );
  }

  private isFailedStatus(status: TransactionStatus): boolean {
    return status === TransactionStatus.KO || status === TransactionStatus.ACK_KO;
  }

  public sendTransactionAndNotify(id: string): Observable<Transaction> {
    return this.sendTransaction(id).pipe(
      tap({
        next: () => this.snackBarService.open({ message: 'COLLECT.INGEST_TRANSACTION_LAUNCHED', duration: 10_000 }),
        error: () => this.snackBarService.open({ message: 'COLLECT.INGEST_TRANSACTION_ERROR', duration: 10_000 }),
      }),
    );
  }

  public editTransactionAndNotify(id: string): Observable<Transaction> {
    return this.editTransaction(id).pipe(
      tap({
        next: () => this.snackBarService.open({ message: 'COLLECT.TRANSACTION_REOPENED', duration: 10_000 }),
        error: () => this.snackBarService.open({ message: 'COLLECT.TRANSACTION_REOPEN_ERROR', duration: 10_000 }),
      }),
    );
  }

  public abortTransactionAndNotify(id: string): Observable<Transaction> {
    return this.abortTransaction(id).pipe(
      tap({
        next: () => this.snackBarService.open({ message: 'COLLECT.TRANSACTION_ABORTED', duration: 10_000 }),
        error: () => this.snackBarService.open({ message: 'COLLECT.TRANSACTION_ABORT_ERROR', duration: 10_000 }),
      }),
    );
  }

  downloadSipTransaction(id: string) {
    return this.transactionApiService
      .prepareSignedDownloadSipTransaction(id)
      .pipe(
        catchError((error) => {
          this.snackBarService.open({
            message: 'COLLECT.PROJECT_TRANSACTION_PREVIEW.TRANSACTION_SIP_DOWNLOAD_ERROR',
            duration: 10_000,
          });
          throw error;
        }),
      )
      .subscribe((signedUrl) => {
        this.snackBarService.startDownload(signedUrl);
      });
  }

  loadDataForTransactions(transactions: Transaction[], project: Project) {
    this.transactions$.next(transactions);
    this.project$.next(project);
  }

  public refreshTransactionsByProjectId(projectId: string, pageRequest?: PageRequest): Observable<Transaction[]> {
    return this.getTransactionsByProjectId(projectId, pageRequest).pipe(
      map((response) => response.values),
      tap((transactions) => this.transactions$.next(transactions)),
    );
  }
}
