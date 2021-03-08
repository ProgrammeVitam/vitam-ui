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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { Customer, Owner, Tenant } from 'ui-frontend-common';
import { CustomerDataService } from '../../customer.data.service';
import { OwnerCreateComponent } from '../../owner-create/owner-create.component';
import { OwnerService } from '../../owner.service';
import { TenantCreateComponent } from '../../tenant-create/tenant-create.component';
import { TenantService } from '../../tenant.service';

@Component({
  selector: 'app-owner-list',
  templateUrl: './owner-list.component.html',
  styleUrls: ['./owner-list.component.scss']
})
export class OwnerListComponent implements OnDestroy, OnInit {

  @Input()
  set customer(customer: Customer) {
    this._customer = customer;
  }
  get customer(): Customer { return this._customer; }
  private _customer: Customer = null;

  @Output() ownerClick = new EventEmitter<Owner>();
  @Output() tenantClick = new EventEmitter<any>();

  private updatedOwnerSub: Subscription;
  private updatedTenantSub: Subscription;

  ownersWithoutTenant: Owner[];
  myTenants: Tenant[];

  constructor(
    private dialog: MatDialog,
    private ownerService: OwnerService,
    private tenantService: TenantService,
    private customerDataService: CustomerDataService,
  ) { }

  ngOnInit() {
    this.filteredData(this.customerDataService.tenants);

    this.customerDataService.tenantsUpdated$.subscribe((tenants) =>  {
      this.filteredData(tenants);
    });

    this.updatedOwnerSub = this.ownerService.updated.subscribe((updatedOwner: Owner) => {
      const ownerIndex = (this.customer.owners || []).findIndex((owner: Owner) => updatedOwner.id === owner.id);
      if (ownerIndex > -1) {
        this.customer.owners[ownerIndex] = updatedOwner;
      }
      this.filteredData(this.myTenants);
    });

    this.updatedTenantSub = this.tenantService.updated.subscribe((updatedTenant: Tenant) => {
      this.customerDataService.updateTenant(updatedTenant);
    });
  }

  ngOnDestroy() {
    this.updatedOwnerSub.unsubscribe();
    this.updatedTenantSub.unsubscribe();
  }

  openCreateOwnerDialog() {
    const dialogRef = this.dialog.open(OwnerCreateComponent, {
      data: {
        customer: this.customer
      },
      disableClose: true,
      panelClass: 'vitamui-modal'
    });
    dialogRef.afterClosed().pipe(filter((result) => !!result))
      .subscribe((result: { owner?: Owner, tenant?: Tenant }) => {
        if (result.owner) {
          this.customer.owners = this.customer.owners.concat([result.owner]);
          this.filteredData(this.myTenants);
        }
        if (result.tenant) {
          this.customerDataService.addTenants([result.tenant]);
        }
      });
  }

  filteredData(tenants: Tenant[]) {
    if (tenants) {

      this.myTenants = tenants.sort((a, b) => a.identifier - b.identifier)
      .filter((tenant: Tenant) => tenant.customerId === this.customer.id);

      const ownersIds: string[] = this.myTenants.map((tenant: Tenant) => tenant.ownerId);

      this.ownersWithoutTenant = this.customer.owners.filter((owner) => !ownersIds.includes(owner.id));
    }
  }

  clickTenant(tenant: Tenant, owner: Owner) {
    this.tenantService.get(tenant.id).subscribe((tenantResponse: Tenant) => {
      this.tenantClick.emit({ tenant: tenantResponse, owner });
    });
  }

  getOwner(ownerId: string): Owner {
    if (!ownerId) {
      return null;
    }
    return this.customer.owners.filter((owner) => owner.id === ownerId)[0];
  }

  openCreateTenantDialog(owner: Owner) {
    const dialogRef = this.dialog.open(TenantCreateComponent, {
      disableClose: true,
      data: { owner },
      panelClass: 'vitamui-modal'
    });
    dialogRef.afterClosed().pipe(filter((result) => !!result))
    .subscribe((newTenant: Tenant) => {
      this.customerDataService.addTenants([newTenant]);
    });
  }

}
