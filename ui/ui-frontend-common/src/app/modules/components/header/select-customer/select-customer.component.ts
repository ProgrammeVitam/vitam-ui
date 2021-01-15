import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { Customer } from '../../../models/customer/customer.interface';
import { MenuOption } from '../../navbar/customer-menu/menu-option.interface';

@Component({
  selector: 'vitamui-common-select-customer',
  templateUrl: './select-customer.component.html',
  styleUrls: ['./select-customer.component.scss']
})
export class SelectCustomerComponent implements OnInit, OnDestroy {

  @Input() customers: MenuOption[];

  @Input() selectedCustomer: MenuOption;

  @Output() customerSelected = new EventEmitter<MenuOption>();

  private destroyer$ = new Subject();

  constructor() { }

  ngOnInit() { }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public selectCustomer(customer: MenuOption): void {
    this.selectedCustomer = customer;
    this.customerSelected.emit(this.selectedCustomer);
  }

}
