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
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EMPTY, of, Subject } from 'rxjs';
import { ENVIRONMENT, InjectorModule, LoggerModule, StartupService } from 'vitamui-library';
import { environment } from './../../environments/environment';

import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { ActivatedRoute } from '@angular/router';
import { CustomerCreateComponent } from './customer-create/customer-create.component';
import { CustomerComponent } from './customer.component';
import { CustomerListComponent } from './customer-list/customer-list.component';
import { CustomerPreviewComponent } from './customer-preview/customer-preview.component';
import { OwnerPreviewComponent } from './owner-preview/owner-preview.component';

import { VitamUICommonTestModule } from 'vitamui-library/testing';
import { CustomerService } from '../core/customer.service';
import { CustomerDataService } from './customer.data.service';
import { OwnerService } from './owner.service';
import { TenantService } from './tenant.service';

let component: CustomerComponent;
let fixture: ComponentFixture<CustomerComponent>;

class Page {
  get customerList() {
    return fixture.nativeElement.querySelector('app-customer-list');
  }
  get createCustomer() {
    return fixture.nativeElement.querySelector('.vitamui-heading vitamui-banner button.btn.primary');
  }
}

let page: Page;

@Component({
  selector: 'app-customer-list',
  template: '',
  imports: [MatMenuModule, MatSidenavModule, VitamUICommonTestModule],
})
class CustomerListStubComponent {
  search() {}
}

@Component({
  selector: 'app-customer-preview',
  template: '',
  imports: [MatMenuModule, MatSidenavModule, VitamUICommonTestModule],
})
class CustomerPreviewStubComponent {
  @Input()
  customer: any;
  @Output()
  previewClose = new EventEmitter();
  @Input()
  gdprReadOnlySettingStatus: boolean;
}

@Component({
  selector: 'app-owner-preview',
  template: '',
  imports: [MatMenuModule, MatSidenavModule, VitamUICommonTestModule],
})
class OwnerPreviewStubComponent {
  @Input()
  owner: any;
  @Input()
  tenant: any;
  @Output()
  previewClose = new EventEmitter();
}

describe('CustomerComponent', () => {
  const customerServiceSpy = {
    getGdprReadOnlySettingStatus: () => of(true),
    updated: new Subject(),
  };
  const tenantServiceSpy = {
    updated: new Subject(),
  };
  const ownerServiceSpy = {
    updated: new Subject(),
  };
  const startupServiceStub = {
    getPortalUrl: () => 'https://dev.vitamui.com',
    getConfigStringValue: () => 'https://dev.vitamui.com/identity',
    getConfigNumberValue: () => 0,
  };

  beforeEach(async () => {
    const matDialogSpy = {
      open: vi.fn().mockName('MatDialog.open'),
    };
    matDialogSpy.open.mockReturnValue({ afterClosed: () => of(true) });

    await TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatSidenavModule,
        VitamUICommonTestModule,
        InjectorModule,
        LoggerModule.forRoot(),
        CustomerComponent,
        CustomerListStubComponent,
        CustomerPreviewStubComponent,
        OwnerPreviewStubComponent,
      ],
      providers: [
        { provide: CustomerService, useValue: customerServiceSpy },
        { provide: TenantService, useValue: tenantServiceSpy },
        { provide: OwnerService, useValue: ownerServiceSpy },
        { provide: CustomerDataService, useValue: {} },
        { provide: StartupService, useValue: startupServiceStub },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ActivatedRoute, useValue: { data: EMPTY, snapshot: { data: { appId: 'CUSTOMERS_APP' } } } },
        { provide: ENVIRONMENT, useValue: environment },
      ],
    })
      .overrideComponent(CustomerComponent, {
        remove: {
          imports: [CustomerListComponent, CustomerPreviewComponent, OwnerPreviewComponent],
        },
        add: {
          imports: [CustomerListStubComponent, CustomerPreviewStubComponent, OwnerPreviewStubComponent],
        },
      })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a app-customer-list', () => {
    expect(page.customerList).toBeTruthy();
  });

  it('should have a "create customer" button', () => {
    expect(page.createCustomer).toBeTruthy();
  });

  it('should open a modal with CustomerCreateComponent', () => {
    const matDialogSpy = TestBed.inject(MatDialog);
    page.createCustomer.click();
    expect(matDialogSpy.open).toHaveBeenCalledWith(CustomerCreateComponent, {
      data: { gdprReadOnlySettingStatus: true },
      disableClose: true,
    });
  });
});
