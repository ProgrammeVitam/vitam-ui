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
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BehaviorSubject, filter, finalize, of, switchMap } from 'rxjs';
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
  Direction,
  InfiniteScrollTable,
  SnackBarService,
  StartupService,
  Transaction,
  TransactionStatus,
} from 'vitamui-library';
import { TransactionsService } from '../transactions.service';
import { ArchiveCollectService } from '../../archive-search-collect/archive-collect.service';
import { ProjectsService } from '../../projects/projects.service';
import { SipImportTrackingService } from '../../shared/sip-import-tracking.service';
import { MatDialog } from '@angular/material/dialog';
import { TransactionValidationMode } from '../../models/transaction-validation-mode.enum';
import { BatchStatus } from 'projects/vitamui-library/src/app/modules/models/collect/batch-status';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css'],
  standalone: false,
})
export class TransactionListComponent extends InfiniteScrollTable<Transaction> implements OnInit {
  direction = Direction.DESCENDANT;
  orderBy = 'archivalAgreement';
  orderChange = new BehaviorSubject<string>(this.orderBy);
  tenantIdentifier: string;
  hasAbortTransactionRole = false;
  hasEditTransactionRole = false;
  hasSendTransactionRole = false;
  hasCloseTransactionRole = false;
  hasDownloadTransactionRole = false;
  isAutomaticIngest = false;

  constructor(
    private transactionService: TransactionsService,
    private archiveCollectService: ArchiveCollectService,
    private projectService: ProjectsService,
    private route: ActivatedRoute,
    private router: Router,
    private startupService: StartupService,
    private snackBarService: SnackBarService,
    private dialog: MatDialog,
    private sipImportTrackingService: SipImportTrackingService,
  ) {
    super(transactionService);
  }

  ngOnInit(): void {
    this.tenantIdentifier = this.startupService.getTenantIdentifier();
    this.checkTransactionsPermissions();
    super.search(null);
    this.route.params.subscribe((params) => {
      const projectId = params['projectId'];
      this.projectService.getProjectById(projectId).subscribe((project) => {
        this.isAutomaticIngest = project?.automaticIngest;
      });
    });
  }

  onScroll() {
    this.loadMore();
  }

  emitOrderChange(event: string) {
    this.orderChange.next(event);
  }

  searchArchiveUnitsByTransaction(transaction: Transaction) {
    this.router.navigate(['collect/tenant/' + this.tenantIdentifier + '/units', transaction.projectId, transaction.id]);
  }

  sendTransaction(transaction: Transaction) {
    this.transactionService.sendTransaction(transaction.id).subscribe(
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
        switchMap(() => {
          return this.transactionService.validate(transaction, validationMode, { isAutomaticIngest: this.isAutomaticIngest }).pipe(
            finalize(() => {
              this.snackBarService.open({
                message: 'COLLECT.VALIDATE_TRANSACTION_VALIDATED',
                duration: 10_000,
              });
            }),
          );
        }),
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

  canSend(transaction: Transaction): boolean {
    const allowedStatus = [TransactionStatus.VALIDATED];
    const isAllowedStatus = allowedStatus.includes(transaction.status);

    return this.hasSendTransactionRole && !this.isAutomaticIngest && isAllowedStatus;
  }

  abortTransaction(transaction: Transaction) {
    this.transactionService.abortTransaction(transaction.id).subscribe(
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

  editTransaction(transaction: Transaction) {
    this.transactionService.editTransaction(transaction.id).subscribe(
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

  downloadSipTransaction(transaction: Transaction) {
    return this.transactionService.downloadSipTransaction(transaction.id);
  }

  transactionIsOpen(transaction: Transaction): boolean {
    return TransactionStatus.OPEN === transaction.status;
  }

  sipImportInProgress(transaction: Transaction): boolean {
    return this.sipImportTrackingService.isSipImportInProgress(transaction.id);
  }

  transactionIsValidatable(transaction: Transaction): boolean {
    return this.transactionIsOpen(transaction) && !this.sipImportInProgress(transaction);
  }

  transactionIsEditable(transaction: Transaction): boolean {
    if (this.isAutomaticIngest && [TransactionStatus.READY, TransactionStatus.VALIDATED].includes(transaction.status)) {
      return false;
    }

    return [TransactionStatus.READY, TransactionStatus.VALIDATED, TransactionStatus.ACK_KO, TransactionStatus.KO].includes(
      transaction.status,
    );
  }

  transactionIsAbortable(transaction: Transaction): boolean {
    if (this.isAutomaticIngest && [TransactionStatus.READY, TransactionStatus.VALIDATED].includes(transaction.status)) {
      return false;
    }
    return [
      TransactionStatus.OPEN,
      TransactionStatus.READY,
      TransactionStatus.VALIDATED,
      TransactionStatus.ACK_KO,
      TransactionStatus.KO,
    ].includes(transaction.status);
  }

  transactionIsDownloadable(transaction: Transaction): boolean {
    return [TransactionStatus.VALIDATED, TransactionStatus.SENDING, TransactionStatus.SENT, TransactionStatus.ACK_KO].includes(
      transaction.status,
    );
  }

  private hasBatchInError(transaction: Transaction): boolean {
    return transaction.batches?.some((b) => b.BatchStatus === BatchStatus.KO) ?? false;
  }

  shouldShowBatchError(transaction: Transaction): boolean {
    return this.hasBatchInError(transaction) && transaction.status === TransactionStatus.OPEN;
  }

  private checkTransactionsPermissions() {
    this.archiveCollectService.hasCollectRole('ROLE_ABORT_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasAbortTransactionRole = result;
    });
    this.archiveCollectService.hasCollectRole('ROLE_SEND_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasSendTransactionRole = result;
    });
    this.archiveCollectService.hasCollectRole('ROLE_REOPEN_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasEditTransactionRole = result;
    });
    this.archiveCollectService.hasCollectRole('ROLE_CLOSE_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasCloseTransactionRole = result;
    });
    this.archiveCollectService.hasCollectRole('ROLE_DOWNLOAD_SIP_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasDownloadTransactionRole = result;
    });
  }
}
