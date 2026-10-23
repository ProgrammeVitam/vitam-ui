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
import { ChangeDetectorRef, Component, inject, OnInit, input, model, output, signal } from '@angular/core';
import { form, FormField, FormValueControl } from '@angular/forms/signals';
import { AccessContractService, AuthService, ContextPermission, Option, SelectComponent, Tenant, TooltipDirective } from 'vitamui-library';
import { CustomerApiService } from '../../../core/api/customer-api.service';
import { TenantApiService } from '../../../core/api/tenant-api.service';
import { IngestContractService } from '../../../ingest-contract/ingest-contract.service';
import { combineLatest } from 'rxjs';
import { map } from 'rxjs/operators';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-context-edit-permission',
  templateUrl: './context-edit-permission.component.html',
  styleUrls: ['./context-edit-permission.component.scss'],
  imports: [TooltipDirective, SelectComponent, FormField, TranslatePipe],
})
export class ContextEditPermissionComponent implements FormValueControl<ContextPermission[]>, OnInit {
  private readonly customerApiService = inject(CustomerApiService);
  private readonly tenantApiService = inject(TenantApiService);
  private readonly authService = inject(AuthService);
  private readonly accessService = inject(AccessContractService);
  private readonly ingestService = inject(IngestContractService);
  private readonly cdr = inject(ChangeDetectorRef);

  // Bound to the signal forms field of the parent form with [formField]
  readonly value = model<ContextPermission[]>([]);
  readonly selectedOrganisations = signal<string[]>([]);
  protected readonly permissionsForm = form(this.value);
  protected readonly organisationsForm = form(this.selectedOrganisations);

  readonly editMode = input(false);
  readonly changeOrganisations = output<string[]>();

  private tenants: Tenant[] = [];

  customersOptions: Option[] = [];
  tenantOptionsByOrganisation: Map<string, Option[]> = new Map();
  accessContractsOptionsByOrganisation: Map<string, Option[]>;
  ingestContractsOptionsByOrganisation: Map<string, Option[]>;
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

        const permissions = this.value();
        if (permissions && permissions.length > 0) {
          this.selectedOrganisations.set(
            permissions.map((permission) => {
              let tenant: Tenant;
              if (permission.tenant != null) {
                tenant = this.tenants.find((t) => '' + t.identifier === permission.tenant);
              }
              return tenant ? tenant.customerId : null;
            }),
          );
        }
        this.cdr.markForCheck();
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
        this.cdr.markForCheck();
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
        this.cdr.markForCheck();
      });
    }
  }

  onDelete(index: number) {
    this.value.update((permissions) => permissions.filter((_permission, i) => i !== index));
    this.selectedOrganisations.update((organisations) => organisations.filter((_organisation, i) => i !== index));
    this.changeOrganisations.emit(this.selectedOrganisations());
  }

  onAdd() {
    this.value.update((permissions) => [...permissions, { tenant: '', accessContracts: [], ingestContracts: [] }]);
    this.selectedOrganisations.update((organisations) => [...organisations, null]);
    this.changeOrganisations.emit(this.selectedOrganisations());
  }

  onCustomerSelect(index: number) {
    this.permissionsForm[index]().value.set({ tenant: '', accessContracts: [], ingestContracts: [] });
    this.changeOrganisations.emit(this.selectedOrganisations());
  }
}
