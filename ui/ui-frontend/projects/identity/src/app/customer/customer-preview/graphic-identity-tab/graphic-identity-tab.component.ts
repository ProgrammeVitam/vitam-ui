/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { Customer } from 'ui-frontend-common';
import { CustomerService } from './../../../core/customer.service';
import { GraphicIdentityUpdateComponent } from './graphic-identity-update/graphic-identity-update.component';

@Component({
  selector: 'app-graphic-identity-tab',
  templateUrl: './graphic-identity-tab.component.html',
  styleUrls: ['./graphic-identity-tab.component.scss']
})
export class GraphicIdentityTabComponent {

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

  logo: any;
  trustedUrlInline: SafeUrl;

  constructor(private customerService: CustomerService, private sanitizer: DomSanitizer, private dialog: MatDialog) { }

  resetTab(customer: Customer) {
    if (customer.hasCustomGraphicIdentity) {
      this.customerService.getCustomerLogo(customer.id).subscribe((data: any) => {
      const logo = data;
      this.trustedUrlInline = this.sanitizer.bypassSecurityTrustResourceUrl(window.URL.createObjectURL(logo.body));
      });
    }
  }

  openUpdateCustomerLogo() {
    const dialogRef = this.dialog.open(GraphicIdentityUpdateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: { customer: this.customer, logo: this.trustedUrlInline }
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) { this.refreshTab(); }
    });
  }

  refreshTab() {

  }

}
