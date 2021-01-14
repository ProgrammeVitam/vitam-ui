import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { Customer, ThemeService } from 'ui-frontend-common';
import { HomepageMessageUpdateComponent } from './homepage-message-update/homepage-message-update.component';

@Component({
  selector: 'app-homepage-message-tab',
  templateUrl: './homepage-message-tab.component.html',
  styleUrls: ['./homepage-message-tab.component.scss']
})
export class HomepageMessageTabComponent implements OnInit, OnDestroy {

  @Input()
  set customer(customer: Customer) {
    this._customer = customer;
    this.resetTab(this.customer);
  }
  get customer(): Customer { return this._customer; }
  // tslint:disable-next-line:variable-name
  private _customer: Customer;

  @Input()
  set readOnly(readOnly: boolean) {
    this._readonly = readOnly;
  }
  get readonly(): boolean { return this._readonly; }

  // tslint:disable-next-line:variable-name
  private _readonly: boolean;
  private destroy = new Subject();

  public portalTitle: string;
  public portalMessage: string;

  constructor(private dialog: MatDialog,
              private themeService: ThemeService ) {
  }
  ngOnDestroy(): void {
    this.destroy.next();
  }

  ngOnInit() {
  }

  private resetTab(customer: Customer): void {
    this.portalMessage = customer && this.customer.portalMessage ? this.customer.portalMessage
    : this.themeService.defaultTheme.portalMessage;
    this.portalTitle =  customer && this.customer.portalTitle ? this.customer.portalTitle
    : this.themeService.defaultTheme.portalTitle;
  }

  openUpdateHomepageMessage() {
   const dialogRef = this.dialog.open(HomepageMessageUpdateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: { customer: this.customer }
    });
   dialogRef.afterClosed().subscribe();
  }
}
