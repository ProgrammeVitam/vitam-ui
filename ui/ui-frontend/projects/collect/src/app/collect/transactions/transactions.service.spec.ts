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
import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { LoggerModule, SnackBarService, Transaction, TransactionStatus } from 'vitamui-library';

import { ProjectsApiService } from '../core/api/project-api.service';
import { TransactionApiService } from '../core/api/transaction-api.service';
import { TransactionValidationMode } from '../models/transaction-validation-mode.enum';
import { TransactionsService } from './transactions.service';

describe('TransactionsService', () => {
  let service: TransactionsService;
  let transactionApiService: {
    validateTransaction: ReturnType<typeof vi.fn>;
    getTransactionById: ReturnType<typeof vi.fn>;
    sendTransaction: ReturnType<typeof vi.fn>;
    editTransaction: ReturnType<typeof vi.fn>;
    abortTransaction: ReturnType<typeof vi.fn>;
  };
  let projectsApiService: { getTransactionsByProjectId: ReturnType<typeof vi.fn> };
  let snackBarService: { open: ReturnType<typeof vi.fn> };

  const transaction: Transaction = {
    id: 'transactionId',
    projectId: 'projectId',
    status: TransactionStatus.OPEN,
    archivalAgreement: 'archivalAgreement',
    messageIdentifier: 'messageIdentifier',
    archivalAgencyIdentifier: 'archivalAgencyIdentifier',
    transferringAgencyIdentifier: 'transferringAgencyIdentifier',
    originatingAgencyIdentifier: 'originatingAgencyIdentifier',
    submissionAgencyIdentifier: 'submissionAgencyIdentifier',
    archiveProfile: 'archivalProfile',
    comment: 'comment',
    legalStatus: 'A legal status',
  };

  beforeEach(() => {
    transactionApiService = {
      validateTransaction: vi.fn().mockReturnValue(of(transaction)),
      getTransactionById: vi.fn().mockReturnValue(of({ ...transaction, status: TransactionStatus.VALIDATED })),
      sendTransaction: vi.fn().mockReturnValue(of(transaction)),
      editTransaction: vi.fn().mockReturnValue(of(transaction)),
      abortTransaction: vi.fn().mockReturnValue(of(transaction)),
    };
    projectsApiService = {
      getTransactionsByProjectId: vi.fn(),
    };
    snackBarService = {
      open: vi.fn(),
    };

    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot()],
    })
      .overrideProvider(TransactionApiService, { useValue: transactionApiService })
      .overrideProvider(ProjectsApiService, { useValue: projectsApiService })
      .overrideProvider(SnackBarService, { useValue: snackBarService });

    service = TestBed.inject(TransactionsService);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('hasBatchInError', () => {
    it('should return true when a batch is in KO status', () => {
      const result = service.hasBatchInError({ ...transaction, batches: [{ BatchId: '1', BatchStatus: 'KO' }] });
      expect(result).toBe(true);
    });

    it('should return false when no batch is in KO status', () => {
      const result = service.hasBatchInError({ ...transaction, batches: [{ BatchId: '1', BatchStatus: 'PURGED' }] });
      expect(result).toBe(false);
    });

    it('should return false when there are no batches', () => {
      expect(service.hasBatchInError({ ...transaction, batches: undefined })).toBe(false);
    });
  });

  describe('validateAndNotify', () => {
    it('should notify success once the transaction reaches its final validated state', () => {
      vi.useFakeTimers();

      let result: Transaction;
      service.validateAndNotify(transaction, TransactionValidationMode.VALIDATE).subscribe((r) => (result = r));
      vi.advanceTimersByTime(0);

      expect(transactionApiService.validateTransaction).toHaveBeenCalledWith(transaction.id, TransactionValidationMode.VALIDATE);
      expect(result.status).toBe(TransactionStatus.VALIDATED);
      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.VALIDATE_TRANSACTION_VALIDATED' }));
    });

    it('should notify an error when the final status never becomes validated and polling times out', () => {
      vi.useFakeTimers();
      transactionApiService.getTransactionById.mockReturnValue(of({ ...transaction, status: TransactionStatus.KO }));

      service.validateAndNotify(transaction, TransactionValidationMode.VALIDATE).subscribe();
      // Poll period (3000ms) * maxRetries (30) : the timeout after which polling gives up.
      vi.advanceTimersByTime(3000 * 30);

      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.VALIDATE_TRANSACTION_ERROR' }));
    });

    it('should notify an error when validation fails', () => {
      vi.useFakeTimers();
      transactionApiService.validateTransaction.mockReturnValue(throwError(() => new Error('failure')));

      service.validateAndNotify(transaction, TransactionValidationMode.VALIDATE).subscribe({ error: () => {} });
      vi.advanceTimersByTime(0);

      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.VALIDATE_TRANSACTION_ERROR' }));
    });

    it('should stop polling as soon as the status is not SENDING for an automatic ingest', () => {
      vi.useFakeTimers();
      transactionApiService.getTransactionById.mockReturnValue(of({ ...transaction, status: TransactionStatus.ACK_OK }));

      service.validateAndNotify(transaction, TransactionValidationMode.VALIDATE, { isAutomaticIngest: true }).subscribe();
      vi.advanceTimersByTime(0);

      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.VALIDATE_TRANSACTION_VALIDATED' }));
    });
  });

  describe('sendTransactionAndNotify', () => {
    it('should notify success when the ingest is launched', () => {
      service.sendTransactionAndNotify(transaction.id).subscribe();
      expect(transactionApiService.sendTransaction).toHaveBeenCalledWith(transaction.id);
      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.INGEST_TRANSACTION_LAUNCHED' }));
    });

    it('should notify an error when the ingest fails', () => {
      transactionApiService.sendTransaction.mockReturnValue(throwError(() => new Error('failure')));
      service.sendTransactionAndNotify(transaction.id).subscribe({ error: () => {} });
      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.INGEST_TRANSACTION_ERROR' }));
    });
  });

  describe('editTransactionAndNotify', () => {
    it('should notify success when the transaction is reopened', () => {
      service.editTransactionAndNotify(transaction.id).subscribe();
      expect(transactionApiService.editTransaction).toHaveBeenCalledWith(transaction.id);
      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.TRANSACTION_REOPENED' }));
    });

    it('should notify an error when the reopen fails', () => {
      transactionApiService.editTransaction.mockReturnValue(throwError(() => new Error('failure')));
      service.editTransactionAndNotify(transaction.id).subscribe({ error: () => {} });
      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.TRANSACTION_REOPEN_ERROR' }));
    });
  });

  describe('abortTransactionAndNotify', () => {
    it('should notify success when the transaction is aborted', () => {
      service.abortTransactionAndNotify(transaction.id).subscribe();
      expect(transactionApiService.abortTransaction).toHaveBeenCalledWith(transaction.id);
      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.TRANSACTION_ABORTED' }));
    });

    it('should notify an error when the abort fails', () => {
      transactionApiService.abortTransaction.mockReturnValue(throwError(() => new Error('failure')));
      service.abortTransactionAndNotify(transaction.id).subscribe({ error: () => {} });
      expect(snackBarService.open).toHaveBeenCalledWith(expect.objectContaining({ message: 'COLLECT.TRANSACTION_ABORT_ERROR' }));
    });
  });

  describe('refreshTransactionsByProjectId', () => {
    it('should update transactions$ with the fetched values', () => {
      projectsApiService.getTransactionsByProjectId.mockReturnValue(
        of({ values: [transaction], pageNum: 0, pageSize: 10, hasMore: false, totalElements: 1 }),
      );

      let emitted: Transaction[];
      service.transactions$.subscribe((values) => (emitted = values));

      service.refreshTransactionsByProjectId('projectId').subscribe();

      expect(emitted).toEqual([transaction]);
    });
  });
});
