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
/* tslint:disable:no-use-before-declare */

import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { ContextPermission } from 'projects/vitamui-library/src/public-api';
import { AuthService, Option } from 'ui-frontend-common';
import { Tenant } from 'ui-frontend-common/app/modules/models/customer';
import { AccessContractService } from '../../../access-contract/access-contract.service';
import { IngestContractService } from '../../../ingest-contract/ingest-contract.service';

export const CONTEXT_PERMISSION_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => ContextEditPermissionComponent),
  multi: true
};

@Component({
  selector: 'app-context-edit-permission',
  templateUrl: './context-edit-permission.component.html',
  styleUrls: ['./context-edit-permission.component.scss'],
  providers: [CONTEXT_PERMISSION_VALUE_ACCESSOR]
})
export class ContextEditPermissionComponent implements ControlValueAccessor, OnInit {

  constructor(
    private accessService: AccessContractService,
    private ingestService: IngestContractService,
    private authService: AuthService) {
  }
  permissions: ContextPermission[];

  @Input() editMode = false;

  disabled: boolean;

  tenants: Option[] = [];
  accessContracts: Map<string, Option[]> = new Map();
  ingestContracts: Map<string, Option[]> = new Map();
  // tslint:disable-next-line:variable-name
  onChange = (_x: any) => {
  }
  onTouched = () => {
  }

  ngOnInit(): void {
    if (this.authService.user) {
      const accessTenantsInfo = this.authService.user.tenantsByApp.find(
        appTenantInfo => appTenantInfo.name === 'ACCESS_APP');
      const accessTenants: Tenant[] = accessTenantsInfo ? accessTenantsInfo.tenants : [];
      accessTenants.forEach((tenant) => {
        if (!this.tenants.find(appTenant => appTenant.key === '' + tenant.identifier)) {
          this.tenants.push({ key: '' + tenant.identifier, label: tenant.name });
        }
        this.accessService.getAllForTenant('' + tenant.identifier).subscribe(
          accessContracts => {
            this.accessContracts.set('' + tenant.identifier, accessContracts.map(x => ({
              label: x.name,
              key: x.identifier
            })));
          });
      });

      const ingestTenantsInfo = this.authService.user.tenantsByApp.find(
        appTenantInfo => appTenantInfo.name === 'INGEST_APP');
      const ingestTenants: Tenant[] = ingestTenantsInfo ? ingestTenantsInfo.tenants : [];
      ingestTenants.forEach((tenant) => {
        if (!this.tenants.find(appTenant => appTenant.key === '' + tenant.identifier)) {
          this.tenants.push({ key: '' + tenant.identifier, label: tenant.name });
        }
        this.ingestService.getAllForTenant('' + tenant.identifier).subscribe(
          ingestContracts => {
            this.ingestContracts.set('' + tenant.identifier, ingestContracts.map(x => ({
              label: x.name,
              key: x.identifier
            })));
          });
      });

      this.tenants.sort((t1, t2) => t1.label.localeCompare(t2.label));
    }
  }

  onDelete(index: number) {
    this.permissions.splice(index, 1);
    if (this.onChange) {
      this.onChange(this.permissions);
    }
  }

  onAdd() {
    this.permissions.push({ tenant: '', accessContracts: [], ingestContracts: [] });
    if (this.onChange) {
      this.onChange(this.permissions);
    }
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
      this.permissions = [{ tenant: null, accessContracts: [], ingestContracts: [] }];
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
