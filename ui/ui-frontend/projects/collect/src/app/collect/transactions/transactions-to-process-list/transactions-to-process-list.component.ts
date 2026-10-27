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
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuItem } from '@angular/material/menu';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { filter, of, switchMap, take } from 'rxjs';
import {
  AgencyService,
  ConfirmDialogComponent,
  ConfirmDialogData,
  DEFAULT_PAGE_SIZE,
  Direction,
  DiscussionDto,
  DiscussionIconComponent,
  DiscussionService,
  InfiniteScrollDirective,
  InfiniteScrollTable,
  OrderByButtonComponent,
  PageRequest,
  PipesModule,
  StartupService,
  TableFilterComponent,
  TableFilterDirective,
  TableFilterOptionComponent,
  TooltipDirective,
  Transaction,
  TransactionStatus,
  transactionStatusVisualColor,
  VitamuiMenuButtonComponent,
} from 'vitamui-library';
import { TransactionsToProcessService } from '../transactions-to-process.service';
import { TransactionsService } from '../transactions.service';
import { ArchiveCollectService } from '../../archive-search-collect/archive-collect.service';
import { TransactionValidationMode } from '../../models/transaction-validation-mode.enum';

@Component({
  selector: 'app-transactions-to-process-list',
  templateUrl: './transactions-to-process-list.component.html',
  styleUrls: ['./transactions-to-process-list.component.scss'],
  imports: [
    OrderByButtonComponent,
    VitamuiMenuButtonComponent,
    MatMenuItem,
    MatProgressSpinner,
    TooltipDirective,
    PipesModule,
    TranslatePipe,
    CommonModule,
    InfiniteScrollDirective,
    TableFilterDirective,
    TableFilterComponent,
    TableFilterOptionComponent,
    DiscussionIconComponent,
  ],
})
export class TransactionsToProcessListComponent extends InfiniteScrollTable<Transaction> implements OnInit {
  transactionsToProcessService: TransactionsToProcessService;
  private readonly transactionsService = inject(TransactionsService);
  private readonly archiveCollectService = inject(ArchiveCollectService);
  private readonly agencyService = inject(AgencyService);
  private readonly discussionService = inject(DiscussionService);
  private readonly startupService = inject(StartupService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  private currentQuery: string;

  tenantIdentifier: string;
  agencyNames = signal<Map<string, string>>(new Map());
  transactionIdsWithUnreadDiscussions = signal<Set<string>>(new Set());
  totalElements = signal(0);

  hasAbortTransactionRole = signal(false);
  hasEditTransactionRole = signal(false);
  hasSendTransactionRole = signal(false);
  hasCloseTransactionRole = signal(false);

  transactionStatusVisualColor = transactionStatusVisualColor;
  TransactionStatus = TransactionStatus;

  // sort columns :
  messageIdentifier: keyof Transaction = 'messageIdentifier';
  originatingAgencyIdentifier: keyof Transaction = 'originatingAgencyIdentifier';
  lastUpdate: keyof Transaction = 'lastUpdate';
  status: keyof Transaction = 'status';
  column = this.lastUpdate;
  direction = Direction.DESCENDANT;

  // filters :
  automaticIngestFilter: boolean[] = [];
  readonly statusFilterOptions = Object.values(TransactionStatus);

  constructor() {
    const transactionsToProcessService = inject(TransactionsToProcessService);

    super(transactionsToProcessService);

    this.transactionsToProcessService = transactionsToProcessService;
  }

  ngOnInit(): void {
    this.tenantIdentifier = this.startupService.getTenantIdentifier();
    this.checkTransactionsPermissions();
    this.agencyService.getAll().subscribe((agencies) => {
      this.agencyNames.set(new Map(agencies.map((agency) => [agency.identifier, agency.name])));
    });
    this.updatedData.subscribe(() => {
      this.totalElements.set(this.transactionsToProcessService.getTotalElements ?? 0);
      this.loadUnreadDiscussions(this.dataSource() ?? []);
    });
    this.discussionService.readStatusChanged.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.loadUnreadDiscussions(this.dataSource() ?? []);
    });
    this.searchTransactions();
  }

  private loadUnreadDiscussions(transactions: Transaction[]) {
    this.discussionService
      .findEntityIdsWithUnreadDiscussions(
        'TRANSACTION',
        transactions.map((transaction) => transaction.id),
      )
      .pipe(take(1))
      .subscribe((transactionIds) => this.transactionIdsWithUnreadDiscussions.set(new Set(transactionIds)));
  }

  override onScroll() {
    this.loadMore();
  }

  getAgencyName(identifier: string): string {
    return this.agencyNames().get(identifier);
  }

  getAgencyFullLabel(transaction: Transaction): string {
    const agencyName = this.getAgencyName(transaction.originatingAgencyIdentifier);
    return agencyName ? `${transaction.originatingAgencyIdentifier} - ${agencyName}` : transaction.originatingAgencyIdentifier;
  }

  getTransactionIcon(transaction: Transaction): string {
    return transaction.automaticIngest ? 'vitamui-icon-ic40-flux-industriel' : 'vitamui-icon-ic40-flux-manuel';
  }

  getStatusColorClass(transaction: Transaction): string {
    switch (transactionStatusVisualColor(transaction.status)) {
      case 'green':
        return 'success';
      case 'orange':
        return 'warning';
      case 'red':
        return 'danger';
      default:
        return '';
    }
  }

  changeDirection(direction: Direction) {
    this.direction = direction;
  }

  changeColumn(column: string) {
    this.column = column as keyof Transaction;
  }

  sortTable() {
    this.searchTransactions();
  }

  searchByQuery(query: string) {
    this.currentQuery = query;
    this.searchTransactions();
  }

  onAutomaticIngestFilterChange(values: boolean[]) {
    this.automaticIngestFilter = values ?? [];
    this.searchTransactions();
  }

  searchTransactions() {
    this.transactionIdsWithUnreadDiscussions.set(new Set());
    const criteria = JSON.stringify({
      query: this.currentQuery || null,
      statuses: this.statusFilterOptions,
      automaticIngestValues: this.automaticIngestFilter,
    });
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.column, this.direction, criteria);
    super.search(pageRequest);
  }

  searchArchiveUnitsByTransaction(transaction: Transaction) {
    this.router.navigate(['collect/tenant/' + this.tenantIdentifier + '/units', transaction.projectId, transaction.id]);
  }

  canSendTransaction(transaction: Transaction): boolean {
    return this.hasSendTransactionRole() && !transaction.automaticIngest;
  }

  transactionHasActions(transaction: Transaction): boolean {
    return [TransactionStatus.OPEN, TransactionStatus.VALIDATED, TransactionStatus.ACK_KO, TransactionStatus.KO].includes(
      transaction.status,
    );
  }

  hasBatchInError(transaction: Transaction): boolean {
    return this.transactionsService.hasBatchInError(transaction);
  }

  getUnreadDiscussions(transaction: Transaction): Pick<DiscussionDto, 'unread'>[] {
    return this.hasUnreadDiscussions(transaction) ? [{ unread: true }] : [];
  }

  hasUnreadDiscussions(transaction: Transaction): boolean {
    return this.transactionIdsWithUnreadDiscussions().has(transaction.id);
  }

  validateTransaction(transaction: Transaction) {
    const hasBatchError = this.hasBatchInError(transaction);
    const validationMode = hasBatchError ? TransactionValidationMode.VALIDATE_IGNORE : TransactionValidationMode.VALIDATE;
    const confirmation$ = hasBatchError
      ? this.dialog
          .open<ConfirmDialogComponent, ConfirmDialogData>(ConfirmDialogComponent, {
            disableClose: false,
            data: {
              title: 'COLLECT.OTHER_ACTIONS.DIALOG_MESSAGE.CONFIRM_FORCE_TRANSACTION_VALIDATION_MESSAGE',
              subTitle: 'COLLECT.OTHER_ACTIONS.DIALOG_MESSAGE.CONFIRM_TRANSACTION_VALIDATION',
              confirmLabel: 'COLLECT.OTHER_ACTIONS.DIALOG_MESSAGE.CONFIRM_TRANSACTION_VALIDATION',
              cancelLabel: 'COLLECT.OTHER_ACTIONS.DIALOG_MESSAGE.CANCEL',
            },
          })
          .afterClosed()
          .pipe(filter((confirmed) => !!confirmed))
      : of(true);

    confirmation$
      .pipe(
        switchMap(() =>
          this.transactionsService.validateAndNotify(transaction, validationMode, { isAutomaticIngest: !!transaction.automaticIngest }),
        ),
      )
      .subscribe(this.refreshAfterAction());
  }

  sendTransaction(transaction: Transaction) {
    this.transactionsService.sendTransactionAndNotify(transaction.id).subscribe(this.refreshAfterAction());
  }

  editTransaction(transaction: Transaction) {
    this.transactionsService.editTransactionAndNotify(transaction.id).subscribe(this.refreshAfterAction());
  }

  abortTransaction(transaction: Transaction) {
    this.transactionsService.abortTransactionAndNotify(transaction.id).subscribe(this.refreshAfterAction());
  }

  // Server state is the source of truth for the resulting status, whether the action succeeded or failed.
  private refreshAfterAction(): { next: () => void; error: () => void } {
    return {
      next: () => this.searchTransactions(),
      error: () => this.searchTransactions(),
    };
  }

  private checkTransactionsPermissions() {
    this.archiveCollectService.hasCollectRole('ROLE_ABORT_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasAbortTransactionRole.set(result);
    });
    this.archiveCollectService.hasCollectRole('ROLE_SEND_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasSendTransactionRole.set(result);
    });
    this.archiveCollectService.hasCollectRole('ROLE_REOPEN_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasEditTransactionRole.set(result);
    });
    this.archiveCollectService.hasCollectRole('ROLE_CLOSE_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasCloseTransactionRole.set(result);
    });
  }
}
