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
import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { map, switchMap, tap } from 'rxjs/operators';

import { AppRootComponent, Customer, GlobalEventService, MenuOption, SubrogationModalService } from 'ui-frontend-common';
import { CustomerService } from '../../core/customer.service';
import { CustomerSelectService } from '../customer-select.service';

@Component({
  selector: 'app-subrogate-user',
  templateUrl: './subrogate-user.component.html',
  styleUrls: ['./subrogate-user.component.scss']
})
export class SubrogateUserComponent extends AppRootComponent implements OnInit {

  customer: Customer;
  customers: MenuOption[];
  search: string;

  constructor(
    public dialog: MatDialog,
    public globalEventService: GlobalEventService,
    private router: Router,
    private route: ActivatedRoute,
    private customerService: CustomerService,
    private subrogationModalService: SubrogationModalService,
    private customerSelectService: CustomerSelectService,
  ) {
    super(route);
  }

  ngOnInit() {
    const customerIdChange = this.route.paramMap.pipe(
      tap((paramMap) =>  {
        // emit tenant change event
        this.globalEventService.customerEvent.next(paramMap.get('customerId'));
      }),
      map((paramMap) => paramMap.get('customerId'))
    );
    const customerChange = customerIdChange.pipe(switchMap((customerId) => this.customerService.get(customerId)));

    customerChange.subscribe((customer) => this.customer = customer);
    this.customerSelectService.getAll(true).subscribe((option: MenuOption[]) => this.customers = option);
  }

  openUserSubrogationDialog() {
    this.subrogationModalService.open(this.customer.emailDomains);
  }

  changeCustomer(customerId: string) {
    this.router.navigate(['..', customerId], { relativeTo: this.route });
  }

  onSearchSubmit(search: string) {
    this.search = search;
  }

}
