/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Component, EventEmitter, forwardRef, Input, OnInit, Output } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { AccessContractService, AuthService, ContextPermission, Option, Tenant } from 'vitamui-library';
import { CustomerApiService } from '../../../core/api/customer-api.service';
import { TenantApiService } from '../../../core/api/tenant-api.service';
import { IngestContractService } from '../../../ingest-contract/ingest-contract.service';
import { combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';

export const CONTEXT_PERMISSION_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => ContextEditPermissionComponent),
  multi: true,
};

@Component({
  selector: 'app-context-edit-permission',
  templateUrl: './context-edit-permission.component.html',
  styleUrls: ['./context-edit-permission.component.scss'],
  providers: [CONTEXT_PERMISSION_VALUE_ACCESSOR],
})
export class ContextEditPermissionComponent implements ControlValueAccessor, OnInit {
  constructor(
    private customerApiService: CustomerApiService,
    private tenantApiService: TenantApiService,
    private authService: AuthService,
    private accessService: AccessContractService,
    private ingestService: IngestContractService,
  ) {}
  permissions: ContextPermission[];
  selectedOrganisations: string[];

  @Input() editMode = false;
  @Output() changeOrganisations: EventEmitter<string[]> = new EventEmitter<string[]>();

  private tenants: Tenant[] = [];
  disabled: boolean;

  customersOptions: Option[] = [];
  tenantOptionsByOrganisation: Map<string, Option[]> = new Map();
  accessContractsOptionsByOrganisation: Map<string, Option[]>;
  ingestContractsOptionsByOrganisation: Map<string, Option[]>;
  onChange = (_x: any) => {};
  onTouched = () => {};

  ngOnInit(): void {
    // Get the list of all the organisations
    // and find for each permission the tenant organisation
    this.tenantApiService.getAll().subscribe((tenants) => {
      this.tenants = tenants ? tenants : [];
      this.tenantOptionsByOrganisation = this.tenants.reduce((acc, tenant) => {
        acc.set(tenant.customerId, [...(acc.get(tenant.customerId) || []), { key: '' + tenant.identifier, label: tenant.name }]);
        return acc;
      }, new Map<string, Option[]>());

      this.customerApiService.getAll().subscribe((customers) => {
        this.customersOptions = customers ? customers.map((customer) => ({ key: customer.id, label: customer.name })) : [];
        this.customersOptions.sort((c1, c2) => c1.label.localeCompare(c2.label));

        if (this.permissions && this.permissions.length > 0) {
          this.selectedOrganisations = new Array<string>();
          this.permissions.forEach((permission) => {
            let tenant: Tenant;
            if (permission.tenant != null) {
              tenant = this.tenants.find((t) => '' + t.identifier === permission.tenant);
            }

            if (tenant) {
              this.selectedOrganisations.push(tenant.customerId);
            } else {
              this.selectedOrganisations.push(null);
            }
          });
        }
      });
    });

    if (this.authService.user) {
      // Get the access contracts
      const accessTenantsInfo = this.authService.user.tenantsByApp.find((appTenantInfo) => appTenantInfo.name === 'ACCESS_APP');
      const accessTenants: Tenant[] = accessTenantsInfo ? accessTenantsInfo.tenants : [];
      combineLatest(
        accessTenants.map((tenant) =>
          this.accessService.getAllForTenant('' + tenant.identifier).pipe(map((accessContracts) => ({ tenant, accessContracts }))),
        ),
      ).subscribe((data) => {
        this.accessContractsOptionsByOrganisation = data.reduce((acc, { tenant, accessContracts }) => {
          acc.set(
            '' + tenant.identifier,
            accessContracts.map((x) => ({
              label: x.name,
              key: x.identifier,
            })),
          );
          return acc;
        }, new Map());
      });

      // Get the ingest contracts
      const ingestTenantsInfo = this.authService.user.tenantsByApp.find((appTenantInfo) => appTenantInfo.name === 'INGEST_APP');
      const ingestTenants: Tenant[] = ingestTenantsInfo ? ingestTenantsInfo.tenants : [];
      combineLatest(
        ingestTenants.map((tenant) =>
          this.ingestService.getAllForTenant('' + tenant.identifier).pipe(map((ingestContracts) => ({ tenant, ingestContracts }))),
        ),
      ).subscribe((data) => {
        this.ingestContractsOptionsByOrganisation = data.reduce((acc, { tenant, ingestContracts }) => {
          acc.set(
            '' + tenant.identifier,
            ingestContracts.map((x) => ({
              label: x.name,
              key: x.identifier,
            })),
          );
          return acc;
        }, new Map());
      });
    }
  }

  onDelete(index: number) {
    this.permissions.splice(index, 1);
    this.selectedOrganisations.splice(index, 1);
    this.changeOrganisations.emit(this.selectedOrganisations);
    if (this.onChange) {
      this.onChange(this.permissions);
    }
  }

  onAdd() {
    this.permissions.push({ tenant: '', accessContracts: [], ingestContracts: [] });
    if (!this.selectedOrganisations) {
      this.selectedOrganisations = new Array<string>();
    }
    this.selectedOrganisations.push(null);
    this.changeOrganisations.emit(this.selectedOrganisations);
    if (this.onChange) {
      this.onChange(this.permissions);
    }
  }

  onCustomerSelect(permission: ContextPermission) {
    permission.tenant = '';
    permission.accessContracts = [];
    permission.ingestContracts = [];
    if (this.onChange) {
      this.onChange(this.permissions);
    }
    this.changeOrganisations.emit(this.selectedOrganisations);
  }

  onTenantSelect() {
    if (this.onChange) {
      this.onChange(this.permissions);
    }
  }

  onContractSelect() {
    if (this.onChange) {
      this.onChange(this.permissions);
    }
  }

  writeValue(value: ContextPermission[]) {
    if (value) {
      this.permissions = value;
    } else {
      this.permissions = [];
    }
    if (this.onChange) {
      this.onChange(this.permissions);
    }
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean) {
    this.disabled = isDisabled;
  }
}
