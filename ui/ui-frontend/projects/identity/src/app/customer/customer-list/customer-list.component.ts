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
import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';

import { Customer, DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, Owner, PageRequest, Tenant } from 'ui-frontend-common';
import { CustomerService } from '../../core/customer.service';
import { CustomerDataService } from '../customer.data.service';
import { OwnerCreateComponent } from '../owner-create/owner-create.component';
import { TenantService } from '../tenant.service';
import { CustomerListService } from './customer-list.service';

@Component({
  selector: 'app-customer-list',
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.scss'],
  animations: [
    trigger('expansion', [
      state('collapsed', style({height: '0px', visibility: 'hidden', opacity: '0'})),
      state('expanded', style({height: '*', visibility: 'visible',  opacity: '1'})),
      transition('expanded <=> collapsed', animate('150ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),

    trigger('arrow', [
      state('collapsed', style({transform: 'rotate(180deg)'})),
      state('expanded', style({transform: 'none'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),
  ]
})
export class CustomerListComponent extends InfiniteScrollTable<Customer> implements OnDestroy, OnInit {

  @Output() customerClick = new EventEmitter<Customer>();
  @Output() ownerClick = new EventEmitter<Owner>();
  @Output() tenantClick = new EventEmitter<any>();

  tenants: Tenant[] = [];
  overridePendingChange: true;
  loaded = false;

  private updatedCustomerSub: Subscription;

  constructor(
    public customerListService: CustomerListService,
    public customerService: CustomerService,
    public tenantService: TenantService,
    private customerDataService: CustomerDataService,
    private dialog: MatDialog
  ) {
    super(customerListService);
  }

  ngOnInit() {
    this.searchCustomersOrderedByCode();

    this.customerDataService.tenantsUpdated$.subscribe((tenants) => {
      this.tenants = tenants;
    });

    // When the list is reloaded, we retrieve the tenants .
    this.updatedData.subscribe(() => {
      // get the id of every customer
      // look for customer tenants that we don't already have on our list (tenants not previously fetched)
      const customerIds = this.dataSource.filter((customer: Customer) => {
        const existingTenant = this.tenants.find((tenant) => tenant.customerId === customer.id);
        if (!existingTenant) {
          return true;
        }

        return false;
      })
      .map((customer: Customer) => customer.id);

      if (customerIds && customerIds.length > 0) {
        // fetch remaining tenants
        this.tenantService.getTenantsByCustomerIds(customerIds).subscribe((results) => {
          this.customerDataService.addTenants(results);
          // we load everything before displaying data
          this.loaded = true;
          // we change the pending after tenants load
          this.pending = false;
        });
      } else {
        // we load everything before displaying data
        this.loaded = true;
        // we change the pending after tenants load
        this.pending = false;
      }
    });

    this.updatedCustomerSub = this.customerService.updated.subscribe((updatedCustomer: Customer) => {
      const customerIndex = this.dataSource.findIndex((customer) => updatedCustomer.id === customer.id);
      if (customerIndex > -1) {
        this.dataSource[customerIndex] = updatedCustomer;
      }
    });
  }

  ngOnDestroy() {
    this.updatedCustomerSub.unsubscribe();
    this.updatedData.unsubscribe();
  }

  searchCustomersOrderedByCode() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, 'code', Direction.ASCENDANT));
  }

  openCreateOwnerDialog(customer: Customer) {
    const dialogRef = this.dialog.open(OwnerCreateComponent, {
      disableClose: true,
      data: { customer },
      panelClass: 'vitamui-modal'
    });
    dialogRef.afterClosed().pipe(filter((result) => !!result))
    .subscribe((result: { owner?: Owner, tenant?: Tenant }) => {
      if (result.owner) {
        customer.owners.push(result.owner);
      }
    });
  }

}
