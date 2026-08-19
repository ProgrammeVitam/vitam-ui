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
import { Component, Input, OnDestroy, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { Customer, StartupService } from 'vitamui-library';
import { HomepageMessageUpdateComponent } from './homepage-message-update/homepage-message-update.component';
import { KeyValuePipe } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-homepage-message-tab',
  templateUrl: './homepage-message-tab.component.html',
  styleUrls: ['./homepage-message-tab.component.scss'],
  imports: [KeyValuePipe, TranslatePipe],
})
export class HomepageMessageTabComponent implements OnDestroy {
  private dialog = inject(MatDialog);
  private startupService = inject(StartupService);

  @Input()
  set customer(customer: Customer) {
    this._customer = customer;
    this.resetTab(this.customer);
  }
  get customer(): Customer {
    return this._customer;
  }
  private _customer: Customer;

  @Input()
  set readOnly(readOnly: boolean) {
    this._readonly = readOnly;
  }
  get readonly(): boolean {
    return this._readonly;
  }

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

  ngOnDestroy(): void {
    this.destroy.next();
  }

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
      disableClose: true,
      data: { customer: this.customer },
    });
    dialogRef.afterClosed().subscribe();
  }
}
