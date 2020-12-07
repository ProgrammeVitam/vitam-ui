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
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { switchMap, takeUntil } from 'rxjs/operators';
import {
  AppRootComponent,
  Customer,
  CustomerSelectionService,
  GlobalEventService,
  MenuOption,
  SubrogationModalService
} from 'ui-frontend-common';
import { CustomerSelectService } from '../customer-select.service';

@Component({
  selector: 'app-subrogate-user',
  templateUrl: './subrogate-user.component.html',
  styleUrls: ['./subrogate-user.component.scss']
})
export class SubrogateUserComponent extends AppRootComponent implements OnInit, OnDestroy {

  public customer: Customer;
  public customers: MenuOption[];
  public search: string;

  private destroyer$ = new Subject();

  constructor(
    public dialog: MatDialog,
    public globalEventService: GlobalEventService,
    private router: Router,
    private route: ActivatedRoute,
    private subrogationModalService: SubrogationModalService,
    private customerSelectService: CustomerSelectService,
    private customerSelectionService: CustomerSelectionService
  ) {
    super(route);
  }

  ngOnInit() {
    this.customerSelectService.getAll(true).pipe(switchMap((options) => {
      this.customers = options;
      this.customerSelectionService.setCustomers(options);
      return this.route.paramMap;
     })).subscribe((paramMap) => {
       const routeCustomerId = paramMap.get('customerId');
       const currentCustomerId = this.customerSelectionService.getSelectedCustomerId();

       if (!currentCustomerId || currentCustomerId !== routeCustomerId) {
         this.customerSelectionService.setCustomerId(routeCustomerId);
         this.updateCustomer(routeCustomerId);
       } else {
        this.updateCustomer(currentCustomerId);
       }

       this.globalEventService.customerEvent.pipe(takeUntil(this.destroyer$)).subscribe((customerId: string) => {
         if (!this.customer || this.customer.identifier !== customerId) {
           this.changeCustomer(customerId);
         }
       });
     });
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public openUserSubrogationDialog(): void {
    this.subrogationModalService.open(this.customer.emailDomains);
  }

  public onSearchSubmit(search: string): void {
    this.search = search;
  }

  private changeCustomer(customerId: string): void {
    this.router.navigate(['..', customerId], { relativeTo: this.route });
  }

  private updateCustomer(customerId: string): void {
    const customers = this.customerSelectService.getCustomers();
    if (customers) {
      this.customer = customers.find(value => value.id === customerId);
    }
  }

}
