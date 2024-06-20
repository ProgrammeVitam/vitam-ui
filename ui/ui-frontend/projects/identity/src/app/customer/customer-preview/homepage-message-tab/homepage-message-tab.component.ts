import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { Customer, StartupService } from 'vitamui-library';
import { HomepageMessageUpdateComponent } from './homepage-message-update/homepage-message-update.component';

@Component({
  selector: 'app-homepage-message-tab',
  templateUrl: './homepage-message-tab.component.html',
  styleUrls: ['./homepage-message-tab.component.scss'],
})
export class HomepageMessageTabComponent implements OnInit, OnDestroy {
  @Input()
  set customer(customer: Customer) {
    this._customer = customer;
    this.resetTab(this.customer);
  }
  get customer(): Customer {
    return this._customer;
  }
  // tslint:disable-next-line:variable-name
  private _customer: Customer;

  @Input()
  set readOnly(readOnly: boolean) {
    this._readonly = readOnly;
  }
  get readonly(): boolean {
    return this._readonly;
  }

  // tslint:disable-next-line:variable-name
  private _readonly: boolean;
  private destroy = new Subject<void>();

  public portalTitle: string;
  public portalMessage: string;

  public portalTitles: {
    [key: string]: string;
  };
  public portalMessages: {
    [key: string]: string;
  };

  public language: string;

  constructor(
    private dialog: MatDialog,
    private startupService: StartupService,
  ) {}

  ngOnDestroy(): void {
    this.destroy.next();
  }

  ngOnInit() {}

  private resetTab(customer: Customer): void {
    const title = this.startupService.getConfigStringValue('PORTAL_TITLE');
    const message = this.startupService.getConfigStringValue('PORTAL_MESSAGE');

    if (customer) {
      if (customer.language) {
        this.language = customer.language;
      }
      if (customer.portalMessages) {
        this.portalMessages = customer.portalMessages;
      }
      if (customer.portalTitles) {
        this.portalTitles = this.customer.portalTitles;
      }
    }

    this.portalTitle = this.portalTitles && this.portalTitles[this.language] ? this.portalTitles[this.language] : title;
    this.portalMessage = this.portalMessages && this.portalMessages[this.language] ? this.portalMessages[this.language] : message;
  }

  openUpdateHomepageMessage() {
    const dialogRef = this.dialog.open(HomepageMessageUpdateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: { customer: this.customer },
    });
    dialogRef.afterClosed().subscribe();
  }
}
