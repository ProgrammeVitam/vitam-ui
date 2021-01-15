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
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { CustomerSelectionService } from '../../../customer-selection.service';

import { CommonMenuComponent } from '../common-menu/common-menu.component';
import { MenuType } from '../menu-type.enum';
import { CustomerMenuService } from './customer-menu.service';
import { MenuOption } from './menu-option.interface';

@Component({
  selector: 'vitamui-common-customer-menu',
  templateUrl: './customer-menu.component.html',
  styleUrls: ['./customer-menu.component.scss']
})
export class CustomerMenuComponent implements OnInit, OnDestroy {

  @Input() customers: MenuOption[];

  @Output() customerSelect = new EventEmitter<string>();

  private activeCustomerId: string;
  private customerSelection: Subscription;

  constructor(
    private dialog: MatDialog,
    private customerMenuService: CustomerMenuService,
    private customerSelectionService: CustomerSelectionService,
    private route: ActivatedRoute) { }

  ngOnInit() {
    this.route.paramMap.subscribe((params: any) => {
      this.activeCustomerId = params.get('customerId');
    });
    this.customerSelection = this.customerMenuService.getSelectedCustomer().subscribe((customerId) => {
      if (customerId !== this.activeCustomerId) {
        this.activeCustomerId = customerId;
        this.customerSelectionService.setCustomerId(customerId);
        this.emitCustomerIdChange(customerId);
      }
    });
  }

  emitCustomerIdChange(customerId: string) {
    this.customerSelect.emit(customerId);
  }

  get activeCustomer(): MenuOption {
    return (this.customers || []).find((customer) => customer.value === this.activeCustomerId);
  }

  openCustomerMenu(): void {
    this.dialog.open(CommonMenuComponent, {
      panelClass: 'vitamui-modal',
      data: { menuType: MenuType.customer, items: this.customers }
    });
  }

  ngOnDestroy(): void {
    this.customerSelection.unsubscribe();
  }

}
