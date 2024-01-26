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
import {animate, state, style, transition, trigger} from '@angular/animations';
import {Component, Input, OnDestroy, OnInit} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {Subscription} from 'rxjs';

import {Customer, DownloadUtils, IdentityProvider} from 'ui-frontend-common';
import {IdentityProviderCreateComponent} from './identity-provider-create/identity-provider-create.component';
import {IdentityProviderService} from './identity-provider.service';
import {ProviderApiService} from './provider-api.service';

@Component({
  selector: 'app-sso-tab',
  templateUrl: './sso-tab.component.html',
  styleUrls: ['./sso-tab.component.scss'],
  animations: [
    trigger('panelTransition', [
      state('previous', style({ transform: 'translate3d(-100%, 0, 0)' })),
      state('next', style({ transform: 'translate3d(100%, 0, 0)'  })),
      state('current', style({ transform: 'translate3d(0, 0, 0)' })),
      transition('* <=> current', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
    trigger('slideLeftTransition', [
      transition(':enter', [
        style({ transform: 'translate3d(-100%, 0, 0)' }),
        animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)'),
      ]),
      transition(':leave', [
        animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)', style({ transform: 'translate3d(-100%, 0, 0)' })),
      ]),
    ]),
    trigger('slideRightTransition', [
      state('*', style({ transform: 'translate3d(0, 0, 0)' })),
      transition(':enter', [
        style({ transform: 'translate3d(100%, 0, 0)' }),
        animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)'),
      ]),
      transition(':leave', [
        animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)', style({ transform: 'translate3d(100%, 0, 0)' })),
      ]),
    ]),
  ]
})
export class SsoTabComponent implements OnDestroy, OnInit {

  providers: IdentityProvider[];
  panel1Position = 'current';
  panel2Position = 'next';
  selectedIdentityProvider: IdentityProvider;
  domains: Array<{ value: string; disabled: boolean }> = [];

  @Input()
  set customer(customer: Customer) {
    this._customer = customer;
    this.selectedIdentityProvider = null;
    if (!this._customer) {
      return;
    }
    this.identityProviderService.getAll(this.customer.id).subscribe((providers: IdentityProvider[]) => {
      this.providers = providers;
    });
    this.refreshAvailableDomains();
  }
  get customer(): Customer { return this._customer; }
  private _customer: Customer;

  @Input() readOnly: boolean;

  private updatedProviderSub: Subscription;

  constructor(public dialog: MatDialog, private identityProviderService: IdentityProviderService,
              private providerApi: ProviderApiService) { }

  ngOnInit() {
    this.updatedProviderSub = this.identityProviderService.updated.subscribe((updatedProvider: IdentityProvider) => {
      const providerIndex = this.providers.findIndex((provider) => updatedProvider.id === provider.id);
      if (providerIndex > -1) {
        this.providers[providerIndex] = updatedProvider;
      }
      this.refreshAvailableDomains();
    });
  }

  ngOnDestroy() {
    this.updatedProviderSub.unsubscribe();
  }

  openCreateIDPDialog() {
    const dialogRef = this.dialog.open(IdentityProviderCreateComponent, {
      data: {
        customer: this.customer,
        domains: this.domains
      },
      disableClose: true,
      panelClass: 'vitamui-modal'
    });
    dialogRef.afterClosed().subscribe((result: IdentityProvider) => {
      if (result) {
        this.providers.push(result);
        this.refreshAvailableDomains();
      }
    });
  }

  selectIdentityProvider(identityProvider: IdentityProvider) {
    this.selectedIdentityProvider = identityProvider;
  }

  get domainsAvailable(): boolean {
    return this.domains.filter((domain) => !domain.disabled).length > 0;
  }

  downloadFile(isInternalProvider: boolean, url: string): void {
    if(!isInternalProvider){
      this.providerApi.getFileByUrl(url).subscribe((response: any) => DownloadUtils.loadFromBlob(response, response.body.type));
    }
  }

  private refreshAvailableDomains() {
    this.identityProviderService.getDomainByCustomerId(this.customer.id).subscribe((domains) => {
      this.domains = this.customer.emailDomains.map((domain) => ({ value: domain, disabled: domains.includes(domain) }));
    });
  }

}
