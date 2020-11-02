import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subject } from 'rxjs';
import { Tenant } from '../../..';

@Component({
  selector: 'vitamui-common-select-tenant',
  templateUrl: './select-tenant.component.html',
  styleUrls: ['./select-tenant.component.scss']
})
export class SelectTenantComponent implements OnInit, OnDestroy {

  /** Available tenant list to display */
  @Input() tenants: Tenant[];

  /** Current tenant in the select box */
  @Input() selectedTenant: Tenant;

  @Output() tenantSelected = new EventEmitter<Tenant>();

  private destroyer$ = new Subject();

  constructor() { }

  ngOnInit() { }

  ngOnDestroy() {
    this.destroyer$.next();
  }

  public selectTenant(tenant: Tenant): void {
    this.selectedTenant = tenant;
    this.tenantSelected.emit(this.selectedTenant);
  }

}
