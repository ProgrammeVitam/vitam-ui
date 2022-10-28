import {Component, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {Router} from '@angular/router';
import {TranslateService} from '@ngx-translate/core';
import {BehaviorSubject} from 'rxjs';
import {Direction, InfiniteScrollTable, Transaction, TransactionStatus} from 'ui-frontend-common';
import {TransactionsService} from '../transactions.service';

@Component({
  selector: 'app-transaction-list',
  templateUrl: './transaction-list.component.html',
  styleUrls: ['./transaction-list.component.css']
})
export class TransactionListComponent extends InfiniteScrollTable<Transaction> implements OnInit {
  direction = Direction.DESCENDANT;
  orderBy = 'archivalAgreement';
  orderChange = new BehaviorSubject<string>(this.orderBy);

  constructor(private snackBar: MatSnackBar, private transactionService: TransactionsService,
              private translateService: TranslateService, private router: Router) {
    super(transactionService);
  }

  ngOnInit(): void {
    super.search(null);
  }

  onScroll() {
    this.loadMore();
  }

  emitOrderChange(event: string) {
    this.orderChange.next(event);
  }

  searchArchiveUnitsByTransaction(transaction: Transaction) {
    this.router.navigate(['collect/archive-search-collect',
      transaction.projectId, transaction.id], {queryParams: {projectName: transaction.messageIdentifier}});
  }

  sendTransaction(transaction: Transaction) {
    this.transactionService.sendTransaction(transaction.id).subscribe(() => {
      const message = this.translateService.instant('COLLECT.INGEST_TRANSACTION_LAUNCHED');
      transaction.status = TransactionStatus.SENDING;
      this.snackBar.open(message, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
      });
    },()=>{
      transaction.status = TransactionStatus.KO;
    });
  }

  validateTransaction(transaction: Transaction) {
    this.transactionService.validateTransaction(transaction.id).subscribe(() => {
      const message = this.translateService.instant('COLLECT.VALIDATE_TRANSACTION_VALIDATED');
      transaction.status = TransactionStatus.READY;
      this.snackBar.open(message, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
      });
    },()=>{
      transaction.status = TransactionStatus.KO;
    });
  }


  abortTransaction(transaction: Transaction) {
    this.transactionService.abortTransaction(transaction.id).subscribe(() => {
      transaction.status = TransactionStatus.ABORTED;
      const message = this.translateService.instant('COLLECT.TRANSACTION_ABORTED');
      this.snackBar.open(message, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
      });
    },()=>{
      transaction.status = TransactionStatus.KO;
    });
  }

  editTransaction(transaction: Transaction) {
    this.transactionService.editTransaction(transaction.id).subscribe(() => {
      transaction.status = TransactionStatus.OPEN;
      const message = this.translateService.instant('COLLECT.TRANSACTION_REOPENED');
      this.snackBar.open(message, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
      });
    },()=>{
      transaction.status = TransactionStatus.KO;
    });
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
    return [TransactionStatus.OPEN, TransactionStatus.READY,
      TransactionStatus.ACK_KO, TransactionStatus.KO].indexOf(transaction.status) !== -1;
  }
}
