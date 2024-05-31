/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import { Component, OnInit } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';
import { Direction, InfiniteScrollTable, StartupService, Transaction, TransactionStatus } from 'vitamui-library';
import { TransactionsService } from '../transactions.service';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css'],
})
export class TransactionListComponent extends InfiniteScrollTable<Transaction> implements OnInit {
  direction = Direction.DESCENDANT;
  orderBy = 'archivalAgreement';
  orderChange = new BehaviorSubject<string>(this.orderBy);
  tenantIdentifier: string;

  constructor(
    private snackBar: MatSnackBar,
    private transactionService: TransactionsService,
    private translateService: TranslateService,
    private router: Router,
    private startupService: StartupService,
  ) {
    super(transactionService);
  }

  ngOnInit(): void {
    this.tenantIdentifier = this.startupService.getTenantIdentifier();
    super.search(null);
  }

  onScroll() {
    this.loadMore();
  }

  emitOrderChange(event: string) {
    this.orderChange.next(event);
  }

  searchArchiveUnitsByTransaction(transaction: Transaction) {
    this.router.navigate(['collect/tenant/' + this.tenantIdentifier + '/units', transaction.projectId, transaction.id], {
      queryParams: { projectName: transaction.messageIdentifier },
    });
  }

  sendTransaction(transaction: Transaction) {
    this.transactionService.sendTransaction(transaction.id).subscribe(
      () => {
        const message = this.translateService.instant('COLLECT.INGEST_TRANSACTION_LAUNCHED');
        transaction.status = TransactionStatus.SENDING;
        this.snackBar.open(message, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });
      },
      () => {
        transaction.status = TransactionStatus.KO;
      },
    );
  }

  validateTransaction(transaction: Transaction) {
    this.transactionService.validateTransaction(transaction.id).subscribe(
      () => {
        const message = this.translateService.instant('COLLECT.VALIDATE_TRANSACTION_VALIDATED');
        transaction.status = TransactionStatus.READY;
        this.snackBar.open(message, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });
      },
      () => {
        transaction.status = TransactionStatus.KO;
      },
    );
  }

  abortTransaction(transaction: Transaction) {
    this.transactionService.abortTransaction(transaction.id).subscribe(
      () => {
        transaction.status = TransactionStatus.ABORTED;
        const message = this.translateService.instant('COLLECT.TRANSACTION_ABORTED');
        this.snackBar.open(message, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
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
        const message = this.translateService.instant('COLLECT.TRANSACTION_REOPENED');
        this.snackBar.open(message, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });
      },
      () => {
        transaction.status = TransactionStatus.KO;
      },
    );
  }

  transactionIsOpen(transaction: Transaction): boolean {
    return TransactionStatus.OPEN === transaction.status;
  }

  transactionIsReady(transaction: Transaction): boolean {
    return TransactionStatus.READY === transaction.status;
  }

  transactionIsEditable(transaction: Transaction): boolean {
    return [TransactionStatus.READY, TransactionStatus.ACK_KO, TransactionStatus.KO].indexOf(transaction.status) !== -1;
  }

  transactionIsAbortable(transaction: Transaction): boolean {
    return (
      [TransactionStatus.OPEN, TransactionStatus.READY, TransactionStatus.ACK_KO, TransactionStatus.KO].indexOf(transaction.status) !== -1
    );
  }
}
