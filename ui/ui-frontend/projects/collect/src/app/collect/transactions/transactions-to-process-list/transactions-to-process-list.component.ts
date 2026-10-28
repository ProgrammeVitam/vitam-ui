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
import { Component, OnInit, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuItem } from '@angular/material/menu';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CommonModule } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';
import { filter, finalize, of, switchMap, take } from 'rxjs';
import {
  AgencyService,
  BatchStatus,
  ConfirmDialogComponent,
  ConfirmDialogData,
  DEFAULT_PAGE_SIZE,
  Direction,
  DiscussionDto,
  DiscussionEntity,
  DiscussionIconComponent,
  DiscussionPanelComponent,
  DiscussionService,
  InfiniteScrollDirective,
  InfiniteScrollTable,
  OrderByButtonComponent,
  PageRequest,
  PipesModule,
  SnackBarService,
  StartupService,
  TableFilterComponent,
  TableFilterDirective,
  TableFilterOptionComponent,
  TooltipDirective,
  Transaction,
  TransactionStatus,
  TRANSACTION_STATUSES_TO_PROCESS,
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
    MatTooltipModule,
    TooltipDirective,
    PipesModule,
    TranslatePipe,
    CommonModule,
    InfiniteScrollDirective,
    TableFilterDirective,
    TableFilterComponent,
    TableFilterOptionComponent,
    DiscussionIconComponent,
    DiscussionPanelComponent,
  ],
})
export class TransactionsToProcessListComponent extends InfiniteScrollTable<Transaction> implements OnInit {
  transactionsToProcessService: TransactionsToProcessService;
  private readonly transactionsService = inject(TransactionsService);
  private readonly archiveCollectService = inject(ArchiveCollectService);
  private readonly agencyService = inject(AgencyService);
  private readonly discussionService = inject(DiscussionService);
  private readonly startupService = inject(StartupService);
  private readonly snackBarService = inject(SnackBarService);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  private currentQuery: string;

  tenantIdentifier: string;
  agencyNames = signal<Map<string, string>>(new Map());
  discussionsByTransactionId = signal<Map<string, DiscussionDto[]>>(new Map());
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
  statusFilter: TransactionStatus[] = [];
  readonly statusFilterOptions = TRANSACTION_STATUSES_TO_PROCESS;

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
      this.loadDiscussions(this.dataSource() ?? []);
    });
    this.searchTransactions();
  }

  private loadDiscussions(transactions: Transaction[]) {
    const newTransactions = transactions.filter((transaction) => !this.discussionsByTransactionId().has(transaction.id));
    newTransactions.forEach((transaction) => {
      this.discussionService
        .findDiscussions({ type: 'TRANSACTION', id: transaction.id })
        .pipe(take(1))
        .subscribe((discussions) => {
          this.discussionsByTransactionId.update((map) => new Map(map).set(transaction.id, discussions));
        });
    });
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
    const direction: number = this.direction === Direction.ASCENDANT ? -1 : 1;
    this.dataSource.update((transactions) =>
      [...(transactions ?? [])].sort((a, b) => {
        return a[this.column] === b[this.column] ? 0 : a[this.column] > b[this.column] ? direction : -direction;
      }),
    );
  }

  searchByQuery(query: string) {
    this.currentQuery = query;
    this.searchTransactions();
  }

  onAutomaticIngestFilterChange(values: boolean[]) {
    this.automaticIngestFilter = values ?? [];
    this.searchTransactions();
  }

  onStatusFilterChange(values: TransactionStatus[]) {
    this.statusFilter = values ?? [];
    this.searchTransactions();
  }

  searchTransactions() {
    const criteria = JSON.stringify({
      query: this.currentQuery || null,
      statuses: this.statusFilter.length > 0 ? this.statusFilter : TRANSACTION_STATUSES_TO_PROCESS,
      automaticIngestValues: this.automaticIngestFilter,
    });
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.column, this.direction, criteria);
    super.search(pageRequest);
  }

  searchArchiveUnitsByTransaction(transaction: Transaction) {
    this.router.navigate(['collect/tenant/' + this.tenantIdentifier + '/units', transaction.projectId, transaction.id]);
  }

  transactionHasActions(transaction: Transaction): boolean {
    return [TransactionStatus.OPEN, TransactionStatus.VALIDATED, TransactionStatus.ACK_KO, TransactionStatus.KO].includes(
      transaction.status,
    );
  }

  hasBatchInError(transaction: Transaction): boolean {
    return transaction.batches?.some((b) => b.BatchStatus === BatchStatus.KO) ?? false;
  }

  getUnreadDiscussions(transaction: Transaction): DiscussionDto[] {
    return (this.discussionsByTransactionId().get(transaction.id) ?? []).filter((discussion) => discussion.unread);
  }

  hasUnreadDiscussions(transaction: Transaction): boolean {
    return this.getUnreadDiscussions(transaction).length > 0;
  }

  getDiscussionEntities(transaction: Transaction): DiscussionEntity[] {
    return [
      { type: 'PROJECT', id: transaction.projectId },
      { type: 'TRANSACTION', id: transaction.id, main: true },
    ];
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
          this.transactionsService.validate(transaction, validationMode, { isAutomaticIngest: !!transaction.automaticIngest }).pipe(
            finalize(() => {
              this.snackBarService.open({
                message: 'COLLECT.VALIDATE_TRANSACTION_VALIDATED',
                duration: 10_000,
              });
            }),
          ),
        ),
      )
      .subscribe({
        next: (next) => {
          transaction.status = next.status;
        },
        error: () => {
          transaction.status = TransactionStatus.KO;
        },
      });
  }

  sendTransaction(transaction: Transaction) {
    this.transactionsService.sendTransaction(transaction.id).subscribe(
      () => {
        transaction.status = TransactionStatus.SENDING;
        this.snackBarService.open({
          message: 'COLLECT.INGEST_TRANSACTION_LAUNCHED',
          duration: 10_000,
        });
      },
      () => {
        transaction.status = TransactionStatus.KO;
      },
    );
  }

  editTransaction(transaction: Transaction) {
    this.transactionsService.editTransaction(transaction.id).subscribe(
      () => {
        transaction.status = TransactionStatus.OPEN;
        this.snackBarService.open({
          message: 'COLLECT.TRANSACTION_REOPENED',
          duration: 10_000,
        });
      },
      () => {
        transaction.status = TransactionStatus.KO;
      },
    );
  }

  abortTransaction(transaction: Transaction) {
    this.transactionsService.abortTransaction(transaction.id).subscribe(
      () => {
        transaction.status = TransactionStatus.ABORTED;
        this.snackBarService.open({
          message: 'COLLECT.TRANSACTION_ABORTED',
          duration: 10_000,
        });
      },
      () => {
        transaction.status = TransactionStatus.KO;
      },
    );
  }

  stopPropagation(event: Event) {
    event.stopPropagation();
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
