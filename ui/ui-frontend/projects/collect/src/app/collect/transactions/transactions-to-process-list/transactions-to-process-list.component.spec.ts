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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { AgencyService, Direction, DiscussionService, StartupService, Transaction, TransactionStatus } from 'vitamui-library';
import { ArchiveCollectService } from '../../archive-search-collect/archive-collect.service';
import { TransactionValidationMode } from '../../models/transaction-validation-mode.enum';
import { TransactionsToProcessService } from '../transactions-to-process.service';
import { TransactionsService } from '../transactions.service';
import { TransactionsToProcessListComponent } from './transactions-to-process-list.component';

describe('TransactionsToProcessListComponent', () => {
  let component: TransactionsToProcessListComponent;
  let fixture: ComponentFixture<TransactionsToProcessListComponent>;

  const buildTransaction = (overrides: Partial<Transaction> = {}): Transaction => ({
    id: 'transactionId',
    projectId: 'projectId',
    status: TransactionStatus.OPEN,
    archivalAgreement: 'archivalAgreement',
    messageIdentifier: 'messageIdentifier',
    archivalAgencyIdentifier: 'archivalAgencyIdentifier',
    transferringAgencyIdentifier: 'transferringAgencyIdentifier',
    originatingAgencyIdentifier: 'originatingAgencyIdentifier',
    submissionAgencyIdentifier: 'submissionAgencyIdentifier',
    archiveProfile: 'archiveProfile',
    comment: 'comment',
    legalStatus: 'A legal status',
    ...overrides,
  });

  const transactionsToProcessServiceStub = {
    search: vi.fn().mockName('TransactionsToProcessService.search').mockReturnValue(of([])),
    loadMore: vi.fn().mockName('TransactionsToProcessService.loadMore').mockReturnValue(of([])),
    getTotalElements: 0,
  };

  const transactionsServiceStub = {
    hasBatchInError: vi.fn().mockName('TransactionsService.hasBatchInError').mockReturnValue(false),
    validateAndNotify: vi.fn().mockName('TransactionsService.validateAndNotify').mockReturnValue(of({})),
    sendTransactionAndNotify: vi.fn().mockName('TransactionsService.sendTransactionAndNotify').mockReturnValue(of({})),
    editTransactionAndNotify: vi.fn().mockName('TransactionsService.editTransactionAndNotify').mockReturnValue(of({})),
    abortTransactionAndNotify: vi.fn().mockName('TransactionsService.abortTransactionAndNotify').mockReturnValue(of({})),
  };

  const archiveCollectServiceStub = {
    hasCollectRole: vi.fn().mockName('ArchiveCollectService.hasCollectRole').mockReturnValue(of(false)),
  };

  const agencyServiceStub = {
    getAll: vi.fn().mockName('AgencyService.getAll').mockReturnValue(of([])),
  };

  const discussionServiceStub = {
    readStatusChanged: of(undefined),
    findEntityIdsWithUnreadDiscussions: vi.fn().mockName('DiscussionService.findEntityIdsWithUnreadDiscussions').mockReturnValue(of([])),
  };

  const startupServiceStub = {
    getTenantIdentifier: () => '1',
  };

  const matDialogStub = {
    open: vi.fn().mockName('MatDialog.open'),
  };

  const routerStub = {
    navigate: vi.fn().mockName('Router.navigate'),
  };

  beforeEach(async () => {
    transactionsToProcessServiceStub.search.mockClear();
    transactionsToProcessServiceStub.loadMore.mockClear();

    await TestBed.configureTestingModule({
      schemas: [NO_ERRORS_SCHEMA],
      imports: [TransactionsToProcessListComponent],
      providers: [
        { provide: TransactionsToProcessService, useValue: transactionsToProcessServiceStub },
        { provide: TransactionsService, useValue: transactionsServiceStub },
        { provide: ArchiveCollectService, useValue: archiveCollectServiceStub },
        { provide: AgencyService, useValue: agencyServiceStub },
        { provide: DiscussionService, useValue: discussionServiceStub },
        { provide: StartupService, useValue: startupServiceStub },
        { provide: MatDialog, useValue: matDialogStub },
        { provide: Router, useValue: routerStub },
      ],
    })
      // The template pulls in many vitamui-library components with their own heavy dependency
      // trees; it is irrelevant to the business logic under test, so it is stubbed out.
      .overrideComponent(TransactionsToProcessListComponent, { set: { template: '' } })
      .compileComponents();

    fixture = TestBed.createComponent(TransactionsToProcessListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('transactionHasActions', () => {
    it.each([TransactionStatus.OPEN, TransactionStatus.VALIDATED, TransactionStatus.ACK_KO, TransactionStatus.KO])(
      'should return true when status is %s',
      (status) => {
        expect(component.transactionHasActions(buildTransaction({ status }))).toBe(true);
      },
    );

    it.each([
      TransactionStatus.READY,
      TransactionStatus.SENDING,
      TransactionStatus.SENT,
      TransactionStatus.ACK_OK,
      TransactionStatus.ACK_WARNING,
      TransactionStatus.ABORTED,
    ])('should return false when status is %s', (status) => {
      expect(component.transactionHasActions(buildTransaction({ status }))).toBe(false);
    });
  });

  describe('getStatusColorClass / transactionStatusVisualColor', () => {
    it('should return "success" for ACK_OK', () => {
      expect(component.getStatusColorClass(buildTransaction({ status: TransactionStatus.ACK_OK }))).toBe('success');
    });

    it('should return "warning" for ACK_WARNING', () => {
      expect(component.getStatusColorClass(buildTransaction({ status: TransactionStatus.ACK_WARNING }))).toBe('warning');
    });

    it.each([TransactionStatus.ACK_KO, TransactionStatus.KO])('should return "danger" for %s', (status) => {
      expect(component.getStatusColorClass(buildTransaction({ status }))).toBe('danger');
    });

    it('should return an empty string for statuses with no associated visual color', () => {
      expect(component.getStatusColorClass(buildTransaction({ status: TransactionStatus.OPEN }))).toBe('');
    });
  });

  describe('searchTransactions criteria', () => {
    const lastSearchCriteria = () => {
      const pageRequest = transactionsToProcessServiceStub.search.mock.calls.at(-1)[0];
      return JSON.parse(pageRequest.criteria);
    };

    it('should send the current query and default to every status when none is selected', () => {
      component.searchByQuery('my query');

      const criteria = lastSearchCriteria();
      expect(criteria.query).toBe('my query');
      expect(criteria.statuses).toEqual(component.statusFilterOptions);
      expect(criteria.automaticIngestValues).toEqual([]);
    });

    it('should send null as query when no query has been entered', () => {
      component.searchByQuery('');

      expect(lastSearchCriteria().query).toBeNull();
    });

    it('should always send every status, sorting being used to group them instead of filtering', () => {
      component.changeColumn(component.status);
      component.sortTable();

      expect(lastSearchCriteria().statuses).toEqual(component.statusFilterOptions);
    });

    it('should send the selected automatic/manual flux filter', () => {
      component.onAutomaticIngestFilterChange([true]);

      expect(lastSearchCriteria().automaticIngestValues).toEqual([true]);
    });
  });

  describe('canSendTransaction', () => {
    it('should return true for manual flux when the user has the send role', () => {
      component.hasSendTransactionRole.set(true);
      expect(component.canSendTransaction(buildTransaction({ automaticIngest: false }))).toBe(true);
    });

    it('should return false for automatic flux even when the user has the send role', () => {
      component.hasSendTransactionRole.set(true);
      expect(component.canSendTransaction(buildTransaction({ automaticIngest: true }))).toBe(false);
    });

    it('should return false when the user does not have the send role', () => {
      component.hasSendTransactionRole.set(false);
      expect(component.canSendTransaction(buildTransaction({ automaticIngest: false }))).toBe(false);
    });
  });

  describe('loadUnreadDiscussions (via ngOnInit)', () => {
    it('should batch-lookup unread discussions for the ids of the loaded transactions', () => {
      discussionServiceStub.findEntityIdsWithUnreadDiscussions.mockClear();
      transactionsToProcessServiceStub.search.mockReturnValueOnce(of([buildTransaction({ id: 'transactionId' })]));

      component.ngOnInit();

      expect(discussionServiceStub.findEntityIdsWithUnreadDiscussions).toHaveBeenCalledWith('TRANSACTION', ['transactionId']);
    });
  });

  describe('onScroll', () => {
    it('should delegate to loadMore', () => {
      const loadMoreSpy = vi.spyOn(component, 'loadMore');
      component.onScroll();
      expect(loadMoreSpy).toHaveBeenCalled();
    });
  });

  describe('getAgencyName / getAgencyFullLabel', () => {
    it('should return the agency name when it is known', () => {
      component.agencyNames.set(new Map([['agencyId', 'Agency Name']]));
      expect(component.getAgencyName('agencyId')).toBe('Agency Name');
    });

    it('should combine the identifier and the agency name', () => {
      component.agencyNames.set(new Map([['agencyId', 'Agency Name']]));
      const label = component.getAgencyFullLabel(buildTransaction({ originatingAgencyIdentifier: 'agencyId' }));
      expect(label).toBe('agencyId - Agency Name');
    });

    it('should fall back to the identifier alone when the agency name is unknown', () => {
      component.agencyNames.set(new Map());
      const label = component.getAgencyFullLabel(buildTransaction({ originatingAgencyIdentifier: 'agencyId' }));
      expect(label).toBe('agencyId');
    });
  });

  describe('getTransactionIcon', () => {
    it('should return the automatic flux icon for an automatic ingest', () => {
      expect(component.getTransactionIcon(buildTransaction({ automaticIngest: true }))).toBe('vitamui-icon-ic40-flux-industriel');
    });

    it('should return the manual flux icon otherwise', () => {
      expect(component.getTransactionIcon(buildTransaction({ automaticIngest: false }))).toBe('vitamui-icon-ic40-flux-manuel');
    });
  });

  describe('sorting and navigation', () => {
    it('should update the sort direction and column, then re-search', () => {
      transactionsToProcessServiceStub.search.mockClear();

      component.changeDirection(Direction.ASCENDANT);
      component.changeColumn('messageIdentifier');
      component.sortTable();

      expect(component.direction).toBe(Direction.ASCENDANT);
      expect(component.column).toBe('messageIdentifier');
      expect(transactionsToProcessServiceStub.search).toHaveBeenCalled();
    });

    it('should navigate to the archive units of the transaction', () => {
      routerStub.navigate.mockClear();
      const transaction = buildTransaction({ projectId: 'projectId', id: 'transactionId' });

      component.searchArchiveUnitsByTransaction(transaction);

      expect(routerStub.navigate).toHaveBeenCalledWith(['collect/tenant/1/units', 'projectId', 'transactionId']);
    });
  });

  describe('hasBatchInError', () => {
    it('should delegate to TransactionsService', () => {
      transactionsServiceStub.hasBatchInError.mockReturnValueOnce(true);
      const transaction = buildTransaction();

      expect(component.hasBatchInError(transaction)).toBe(true);
      expect(transactionsServiceStub.hasBatchInError).toHaveBeenCalledWith(transaction);
    });
  });

  describe('unread discussions helpers', () => {
    it('should report a transaction as having unread discussions', () => {
      component.transactionIdsWithUnreadDiscussions.set(new Set(['transactionId']));
      const transaction = buildTransaction({ id: 'transactionId' });

      expect(component.hasUnreadDiscussions(transaction)).toBe(true);
      expect(component.getUnreadDiscussions(transaction)).toEqual([{ unread: true }]);
    });

    it('should report a transaction as having no unread discussions', () => {
      component.transactionIdsWithUnreadDiscussions.set(new Set());
      const transaction = buildTransaction({ id: 'transactionId' });

      expect(component.hasUnreadDiscussions(transaction)).toBe(false);
      expect(component.getUnreadDiscussions(transaction)).toEqual([]);
    });
  });

  describe('validateTransaction', () => {
    beforeEach(() => {
      transactionsServiceStub.validateAndNotify.mockClear();
      transactionsToProcessServiceStub.search.mockClear();
      matDialogStub.open.mockClear();
    });

    it('should validate directly, without confirmation, when there is no batch error', () => {
      transactionsServiceStub.hasBatchInError.mockReturnValueOnce(false);
      const transaction = buildTransaction({ automaticIngest: false });

      component.validateTransaction(transaction);

      expect(matDialogStub.open).not.toHaveBeenCalled();
      expect(transactionsServiceStub.validateAndNotify).toHaveBeenCalledWith(transaction, TransactionValidationMode.VALIDATE, {
        isAutomaticIngest: false,
      });
      // refreshAfterAction re-searches once the action completes.
      expect(transactionsToProcessServiceStub.search).toHaveBeenCalled();
    });

    it('should ask for confirmation and force-validate when there is a batch error and the user confirms', () => {
      transactionsServiceStub.hasBatchInError.mockReturnValueOnce(true);
      matDialogStub.open.mockReturnValueOnce({ afterClosed: () => of(true) });
      const transaction = buildTransaction({ automaticIngest: true });

      component.validateTransaction(transaction);

      expect(matDialogStub.open).toHaveBeenCalled();
      expect(transactionsServiceStub.validateAndNotify).toHaveBeenCalledWith(transaction, TransactionValidationMode.VALIDATE_IGNORE, {
        isAutomaticIngest: true,
      });
    });

    it('should not validate when there is a batch error and the user cancels the confirmation', () => {
      transactionsServiceStub.hasBatchInError.mockReturnValueOnce(true);
      matDialogStub.open.mockReturnValueOnce({ afterClosed: () => of(false) });

      component.validateTransaction(buildTransaction());

      expect(transactionsServiceStub.validateAndNotify).not.toHaveBeenCalled();
      expect(transactionsToProcessServiceStub.search).not.toHaveBeenCalled();
    });
  });

  describe('sendTransaction / editTransaction / abortTransaction', () => {
    beforeEach(() => {
      transactionsToProcessServiceStub.search.mockClear();
    });

    it('should send the transaction and re-search on success', () => {
      const transaction = buildTransaction({ id: 'transactionId' });
      component.sendTransaction(transaction);

      expect(transactionsServiceStub.sendTransactionAndNotify).toHaveBeenCalledWith('transactionId');
      expect(transactionsToProcessServiceStub.search).toHaveBeenCalled();
    });

    it('should re-search even when sending the transaction fails', () => {
      transactionsServiceStub.sendTransactionAndNotify.mockReturnValueOnce(throwError(() => new Error('failure')));
      component.sendTransaction(buildTransaction({ id: 'transactionId' }));

      expect(transactionsToProcessServiceStub.search).toHaveBeenCalled();
    });

    it('should edit the transaction and re-search on success', () => {
      const transaction = buildTransaction({ id: 'transactionId' });
      component.editTransaction(transaction);

      expect(transactionsServiceStub.editTransactionAndNotify).toHaveBeenCalledWith('transactionId');
      expect(transactionsToProcessServiceStub.search).toHaveBeenCalled();
    });

    it('should abort the transaction and re-search on success', () => {
      const transaction = buildTransaction({ id: 'transactionId' });
      component.abortTransaction(transaction);

      expect(transactionsServiceStub.abortTransactionAndNotify).toHaveBeenCalledWith('transactionId');
      expect(transactionsToProcessServiceStub.search).toHaveBeenCalled();
    });
  });
});
