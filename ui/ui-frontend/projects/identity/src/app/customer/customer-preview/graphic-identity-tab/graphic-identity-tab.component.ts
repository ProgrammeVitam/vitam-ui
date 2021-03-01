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
import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Customer, Theme, ThemeColorType, ThemeService } from 'ui-frontend-common';
import { CustomerService } from '../../../core/customer.service';
import { GraphicIdentityUpdateComponent } from './graphic-identity-update/graphic-identity-update.component';
import { LogosSafeResourceUrl } from './logos-safe-resource-url.interface';


@Component({
  selector: 'app-graphic-identity-tab',
  templateUrl: './graphic-identity-tab.component.html',
  styleUrls: ['./graphic-identity-tab.component.scss']
})
export class GraphicIdentityTabComponent implements OnInit, OnDestroy {

  @Input()
  set customer(customer: Customer) {
    this._customer = customer;
    this.resetTab(this.customer);
  }
  get customer(): Customer { return this._customer; }
  private _customer: Customer;

  @Input()
  set readOnly(readOnly: boolean) {
    this._readonly = readOnly;
  }
  get readonly(): boolean { return this._readonly; }

  private _readonly: boolean;
  private destroy = new Subject();
  public isLoading = false;
  public customerLogos: LogosSafeResourceUrl = {};
  public defaultTheme: Theme;

  public theme: Theme;
  public COLOR_NAME: {[colorId: string]: string};
  public THEME_COLORS = ThemeColorType;

  constructor(private customerService: CustomerService, private dialog: MatDialog,
              private themeService: ThemeService) {
  }
  ngOnDestroy(): void {
    this.destroy.next();
  }

  ngOnInit() {
    this.COLOR_NAME = this.themeService.getBaseColors();
  }

  private majTheme(colors: {[colorId: string]: string}, portalMessage: string, portalTitle: string): Theme {
    return {
      colors,
      portalMessage,
      portalTitle,
    };
  }

  private resetTab(customer: Customer): void {
    if (customer.hasCustomGraphicIdentity) {
      const colors = this.themeService.getThemeColors(customer.themeColors);
      this.theme = this.majTheme(colors, customer.portalMessage, customer.portalTitle);
    } else {
      this.theme = this.majTheme(
        this.themeService.getThemeColors(this.themeService.defaultTheme.colors),
        this.themeService.defaultTheme.portalMessage,
        this.themeService.defaultTheme.portalTitle
      );
      this.theme.headerUrl = this.themeService.defaultTheme.headerUrl;
      this.theme.portalUrl = this.themeService.defaultTheme.portalUrl;
      this.theme.footerUrl = this.themeService.defaultTheme.footerUrl;
    }
    if (customer.hasCustomGraphicIdentity) {
      this.isLoading = true;
    }
    this.customerService.getLogos(customer.id)
    .pipe(takeUntil(this.destroy))
    .subscribe((logos: LogosSafeResourceUrl[]) => {
      this.customerLogos = {
        headerUrl: logos[0],
        footerUrl: logos[1],
        portalUrl: logos[2],
      };
      if (customer.hasCustomGraphicIdentity) {
        this.theme.headerUrl = this.customerLogos.headerUrl;
        this.theme.footerUrl = this.customerLogos.footerUrl;
        this.theme.portalUrl = this.customerLogos.portalUrl;
      }
      this.isLoading = false;
    });
    this.themeService.overloadLocalTheme(this.theme.colors, 'div.vitamui-sidepanel');
  }

  openUpdateCustomerLogo() {
    const dialogRef = this.dialog.open(GraphicIdentityUpdateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: { customer: this.customer, logos: this.customerLogos }
    });
    dialogRef.afterClosed().subscribe();
  }
}
