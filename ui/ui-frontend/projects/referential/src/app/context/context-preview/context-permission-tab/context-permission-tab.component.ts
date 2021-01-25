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
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Context, ContextPermission } from 'projects/vitamui-library/src/public-api';
import { AccessContract } from 'projects/vitamui-library/src/public-api';
import { IngestContract } from 'projects/vitamui-library/src/public-api';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap, tap } from 'rxjs/operators';
import { AuthService, Customer, diff, Tenant } from 'ui-frontend-common';
import { extend, isEmpty } from 'underscore';
import { AccessContractService } from '../../../access-contract/access-contract.service';
import { CustomerApiService } from '../../../core/api/customer-api.service';
import { TenantApiService } from '../../../core/api/tenant-api.service';
import { IngestContractService } from '../../../ingest-contract/ingest-contract.service';
import { ContextEditComponent } from '../../context-edit/context-edit.component';
import { ContextService } from '../../context.service';

@Component({
  selector: 'app-context-permission-tab',
  templateUrl: './context-permission-tab.component.html',
  styleUrls: ['./context-permission-tab.component.scss']
})
export class ContextPermissionTabComponent implements OnInit {

  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  submited = false;
  unchanged = true;
  dataLoaded = false;
  isPermissionsOnMultipleOrganisations = false;

  // The initial context beforme modification
  // tslint:disable-next-line:variable-name
  private _context: Context;
  // The updated permissions
  private updatedPermissions: ContextPermission[] = new Array();

  tenants: Map<string, Tenant> = new Map();
  organisations: Map<string, Customer> = new Map();
  accessContracts: Map<string, AccessContract> = new Map();
  ingestContracts: Map<string, IngestContract> = new Map();

  @Input()
  readOnly: boolean;

  @Input()
  set context(context: Context) {
    if (!context.permissions) {
      context.permissions = [];
    }

    for (const permission of context.permissions) {
      if (!permission.accessContracts) {
        permission.accessContracts = [];
      }
      if (!permission.ingestContracts) {
        permission.ingestContracts = [];
      }
    }

    this._context = context;
    this.resetForm(this.context);
    this.updated.emit(false);
  }

  get context(): Context { return this._context; }

  previousValue = (): Context => {
    return this._context;
  }

  constructor(
    public dialog: MatDialog,
    private contextService: ContextService,
    private customerApiService: CustomerApiService,
    private tenantApiService: TenantApiService,
    private authService: AuthService,
    private accessService: AccessContractService,
    private ingestService: IngestContractService
  ) { }

  ngOnInit(): void {
    let accessContractObservable: Observable<any> = of();
    let ingestContractObservable: Observable<any> = of();

    // Build the forkJoins used to get the access and ingest contracts
    if (this.authService.user) {
      // Get the access contracts
      const accessTenantsInfo = this.authService.user.tenantsByApp.find(
        appTenantInfo => appTenantInfo.name === 'ACCESS_APP');
      if (accessTenantsInfo && accessTenantsInfo.tenants && accessTenantsInfo.tenants.length > 0) {
        accessContractObservable = forkJoin(accessTenantsInfo.tenants.map(tenant => {
          return this.accessService.getAllForTenant('' + tenant.identifier).pipe(
            tap((accessContracts) => {
              accessContracts.forEach(accessContract => {
                this.accessContracts.set(accessContract.identifier, accessContract);
              });
            })
          );
        }));
      }

      // Get the ingest contracts
      const ingestTenantsInfo = this.authService.user.tenantsByApp.find(
        appTenantInfo => appTenantInfo.name === 'INGEST_APP');
      if (ingestTenantsInfo && ingestTenantsInfo.tenants && ingestTenantsInfo.tenants.length > 0) {
        ingestContractObservable = forkJoin(ingestTenantsInfo.tenants.map(tenant => {
          return this.ingestService.getAllForTenant('' + tenant.identifier).pipe(
            tap((ingestContracts) => {
              ingestContracts.forEach(ingestContract => {
                this.ingestContracts.set(ingestContract.identifier, ingestContract);
              });
            })
          );
        }));
      }
    }

    // Get the list of all the organisations and tenants
    forkJoin([
      this.tenantApiService.getAll(),
      this.customerApiService.getAll(),
      accessContractObservable,
      ingestContractObservable
    ]).subscribe(([tenants, customers]) => {
      if (tenants && tenants.length > 0) {
        tenants.forEach(tenant => {
          this.tenants.set('' + tenant.identifier, tenant);
        });
      }
      if (customers && customers.length > 0) {
        customers.forEach(customer => {
          this.organisations.set(customer.id, customer);
        });
      }

      this.dataLoaded = true;
    });
  }

  sameArray(a: string[], b: string[]): boolean {
    if (!a && !b) {
      return true;
    }
    if (!a || !b) {
      return false;
    }

    if (a.filter(item => b.indexOf(item) < 0).length > 0) {
      return false;
    }

    if (b.filter(item => a.indexOf(item) < 0).length > 0) {
      return false;
    }

    return true;
  }

  samePermission(p1: ContextPermission[], p2: ContextPermission[]): boolean {
    if (!p1 && !p2) { return true; }
    if (!p1 || !p2) { return false; }
    if (p1.length !== p2.length) {
      return false;
    }

    for (let i = 0; i < p1.length; i++) {
      if (p1[i].tenant !== p2[i].tenant) {
        return false;
      }
      if (!this.sameArray(p1[i].accessContracts, p2[i].accessContracts)) {
        return false;
      }
      if (!this.sameArray(p1[i].ingestContracts, p2[i].ingestContracts)) {
        return false;
      }
    }

    return true;
  }

  hasChanged() {
    this.unchanged = this.samePermission(this.previousValue().permissions, this.updatedPermissions);
    this.updated.emit(!this.unchanged);
  }

  prepareSubmit(): Observable<Context> {
    return of(diff({permissions: this.updatedPermissions}, this.previousValue())).pipe(
      filter((formData) => !isEmpty(formData)),
      map((formData) => extend({ id: this.previousValue().id, identifier: this.previousValue().identifier }, formData)),
      switchMap((formData: { id: string, [key: string]: any }) => this.contextService.patch(formData).pipe(catchError(() => of(null)))));
  }

  onSubmit() {
    this.submited = true;
    this.prepareSubmit().subscribe(() => {
      this.contextService.get(this._context.identifier).subscribe(
        response => {
          this.submited = false;
          this.context = response;
          this.hasChanged();
        }
      );
    }, () => {
      this.submited = false;
    });
  }

  resetForm(context: Context) {
    this.updatedPermissions = new Array();
    for (const permission of context.permissions) {
      this.updatedPermissions.push(
        new ContextPermission(permission.tenant, permission.accessContracts, permission.ingestContracts)
      );
    }
  }

  openEditContextDialog() {
    const dialogRef = this.dialog.open(ContextEditComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: this.updatedPermissions
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result && result.permissions) {
        this.updatedPermissions = result.permissions;
        // Check if the permissions have been modified
        this.hasChanged();
      }
    });
  }

  getOrganisationName(tenantIdentifier: string): string {
    let organisation: Customer = null;
    if (this.tenants && this.organisations) {
      const tenant = this.tenants.get(tenantIdentifier);
      if (tenant) {
        organisation = this.organisations.get(tenant.customerId);
      }
    }
    return organisation ? organisation.name : '';
  }

  getAccessContractNames(accessContractIdentifiers: string[]): string {
    let label = '';
    if (accessContractIdentifiers && accessContractIdentifiers.length > 0) {
      accessContractIdentifiers.forEach((identifier, index) => {
        const contract = this.accessContracts.get(identifier);
        if (contract) {
          label += contract.name;
        }
        if (index < accessContractIdentifiers.length - 1) {
          label += ', ';
        }
      });
    }
    return label;
  }

  getIngestContractNames(ingestContractIdentifiers: string[]): string {
    let label = '';
    if (ingestContractIdentifiers && ingestContractIdentifiers.length > 0) {
      ingestContractIdentifiers.forEach((id, index) => {
        const contract = this.ingestContracts.get(id);
        if (contract) {
          label += contract.name;
        }
        if (index < ingestContractIdentifiers.length - 1) {
          label += ', ';
        }
      });
      return label;
    }
  }
}
