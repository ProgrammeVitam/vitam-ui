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

import { Component, Directive, Input } from '@angular/core';
import { waitForAsync } from '@angular/core/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { of, Subject } from 'rxjs';

import { Customer, OtpState, Owner, Tenant } from 'vitamui-library';
import { InfiniteScrollStubDirective, VitamUICommonTestModule } from 'vitamui-library/testing';
import { CustomerService } from '../../core/customer.service';
import { CustomerDataService } from '../customer.data.service';
import { OwnerCreateComponent } from '../owner-create/owner-create.component';
import { TenantService } from '../tenant.service';
import { CustomerListComponent } from './customer-list.component';
import { CustomerListService } from './customer-list.service';

// tslint:disable-next-line:directive-selector
@Directive({ selector: '[vitamuiCommonCollapseTriggerFor]' })
class CollapseTriggerForStubDirective {
  @Input() vitamuiCommonCollapseTriggerFor: any;
}

// tslint:disable-next-line:directive-selector
@Directive({ selector: '[vitamuiCommonCollapse]', exportAs: 'vitamuiCommonCollapse' })
class CollapseStubDirective {
  @Input() vitamuiCommonCollapse: any;
}

@Component({ selector: 'app-owner-list', template: '' })
class OwnerListStubComponent {
  @Input() customer: any;
}

let component: CustomerListComponent;
let fixture: ComponentFixture<CustomerListComponent>;

class Page {
  get table() {
    return fixture.nativeElement.querySelector('.vitamui-table');
  }
  get columns() {
    return fixture.nativeElement.querySelectorAll('.vitamui-table-head div');
  }
  get rows() {
    return fixture.nativeElement.querySelectorAll('.vitamui-table-rows .vitamui-row .align-items-center');
  }
  get ownerBtn() {
    return fixture.nativeElement.querySelectorAll('.vitamui-table-rows .vitamui-row .btn.btn-circle.primary');
  }
  get loadMoreButton() {
    return fixture.nativeElement.querySelectorAll('.vitamui-min-content.vitamui-table-message');
  }
  get infiniteScroll() {
    return fixture.debugElement.query(By.directive(InfiniteScrollStubDirective));
  }
}

let page: Page;
let customers: Customer[];
let tenants: Tenant[];

describe('CustomerListComponent', () => {
  beforeEach(waitForAsync(() => {
    customers = [
      {
        id: '11',
        identifier: '11',
        code: '011000',
        name: 'Kouygues Telecom',
        companyName: 'Kouygues Telecom',
        enabled: true,
        readonly: false,
        hasCustomGraphicIdentity: false,
        language: null,
        passwordRevocationDelay: 1,
        otp: OtpState.DEACTIVATED,
        idp: true,
        emailDomains: ['kouygues.com'],
        defaultEmailDomain: 'kouygues.com',
        address: {
          street: '13 rue faubourg',
          zipCode: '75009',
          city: 'paris',
          country: 'france',
        },
        owners: [],
        themeColors: {},
        gdprAlert: false,
        gdprAlertDelay: 72,
        portalMessages: {},
        portalTitles: {},
      },
      {
        id: '12',
        identifier: '12',
        code: '012000',
        name: 'Lrange',
        companyName: 'Lrange',
        enabled: true,
        readonly: false,
        hasCustomGraphicIdentity: false,
        language: null,
        passwordRevocationDelay: 1,
        otp: OtpState.OPTIONAL,
        idp: false,
        emailDomains: ['louygues.com'],
        defaultEmailDomain: 'louygues.com',
        address: {
          street: '13 rue faubourg',
          zipCode: '75009',
          city: 'paris',
          country: 'france',
        },
        owners: [],
        themeColors: {},
        gdprAlert: false,
        gdprAlertDelay: 72,
        portalMessages: {},
        portalTitles: {},
      },
      {
        id: '13',
        identifier: '13',
        code: '013000',
        name: 'Mouygues Telecom',
        companyName: 'Mouygues Telecom',
        enabled: true,
        readonly: false,
        hasCustomGraphicIdentity: false,
        language: null,
        passwordRevocationDelay: 1,
        otp: OtpState.MANDATORY,
        idp: true,
        emailDomains: ['mouygues.com'],
        defaultEmailDomain: 'mouygues.com',
        address: {
          street: '13 rue faubourg',
          zipCode: '75009',
          city: 'paris',
          country: 'france',
        },
        owners: [],
        themeColors: {},
        gdprAlert: false,
        gdprAlertDelay: 72,
        portalMessages: {},
        portalTitles: {},
      },
      {
        id: '14',
        identifier: '14',
        code: '014000',
        name: 'Nrange',
        companyName: 'Nrange',
        enabled: true,
        readonly: false,
        hasCustomGraphicIdentity: false,
        language: null,
        passwordRevocationDelay: 1,
        otp: OtpState.OPTIONAL,
        idp: false,
        emailDomains: ['nrange.com'],
        defaultEmailDomain: 'nrange.com',
        address: {
          street: '13 rue faubourg',
          zipCode: '75009',
          city: 'paris',
          country: 'france',
        },
        owners: [],
        themeColors: {},
        gdprAlert: false,
        gdprAlertDelay: 72,
        portalMessages: {},
        portalTitles: {},
      },
      {
        id: '15',
        identifier: '15',
        code: '015000',
        name: 'Bouygues Telecom',
        companyName: 'Bouygues Telecom',
        enabled: true,
        readonly: false,
        hasCustomGraphicIdentity: false,
        language: null,
        passwordRevocationDelay: 1,
        otp: OtpState.OPTIONAL,
        idp: false,
        emailDomains: ['bouygues.com'],
        defaultEmailDomain: 'bouygues.com',
        address: {
          street: '13 rue faubourg',
          zipCode: '75009',
          city: 'paris',
          country: 'france',
        },
        owners: [],
        themeColors: {},
        gdprAlert: false,
        gdprAlertDelay: 72,
        portalMessages: {},
        portalTitles: {},
      },
    ];

    tenants = [];

    const customerListServiceSpy = {
      search: () => of(customers),
      canLoadMore: true,
      loadMore: () => of(customers),
    };

    const tenantServiceSpy = {
      getTenantsByCustomerIds: () => of(tenants),
    };
    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

    TestBed.configureTestingModule({
      imports: [MatProgressSpinnerModule, NoopAnimationsModule, VitamUICommonTestModule],
      declarations: [CustomerListComponent, CollapseStubDirective, CollapseTriggerForStubDirective, OwnerListStubComponent],
      providers: [
        { provide: CustomerListService, useValue: customerListServiceSpy },
        { provide: CustomerService, useValue: { updated: new Subject() } },
        { provide: TenantService, useValue: tenantServiceSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: Router, useValue: routerSpy },
        CustomerDataService,
      ],
    }).compileComponents();

    const customerListService = TestBed.get(CustomerListService);
    spyOn(customerListService, 'search').and.callThrough();
    spyOn(customerListService, 'loadMore').and.callThrough();

    const customerDataService = TestBed.get(CustomerDataService);
    spyOn(customerDataService, 'addTenants').and.callThrough();
    spyOn(customerDataService, 'updateTenant').and.callThrough();
    spyOn(customerDataService, 'tenantsUpdated$').and.callThrough();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CustomerListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a table', () => {
    expect(page.table).toBeTruthy();
  });

  it('should have the right columns', () => {
    expect(page.columns).toBeTruthy();
    expect(page.columns.length).toBe(6);
    expect(page.columns[1].textContent).toContain('CUSTOMER.HOME.RESULTS_TABLE.CODE');
    expect(page.columns[2].textContent).toContain('CUSTOMER.HOME.RESULTS_TABLE.NAME');
    expect(page.columns[3].textContent).toContain('CUSTOMER.HOME.RESULTS_TABLE.SOCIAL_REASON');
    expect(page.columns[4].textContent).toContain('CUSTOMER.HOME.RESULTS_TABLE.SSO');
    expect(page.columns[5].textContent).toContain('CUSTOMER.HOME.RESULTS_TABLE.VALIDATION');
  });

  it('should have a list of clients', () => {
    const customerListService = TestBed.get(CustomerListService);
    expect(customerListService.search).toHaveBeenCalledTimes(1);
    expect(page.rows).toBeTruthy();
    expect(page.rows.length).toBe(5);
  });

  it('should display the right values in the columns', () => {
    expect(page.rows).toBeTruthy();
    expect(page.rows.length).toBe(5);
    testRow(0);
    testRow(1);
    testRow(2);
    testRow(3);
    testRow(4);
  });

  it('should have a button to load more customers', () => {
    component.infiniteScrollDisabled = true;
    fixture.detectChanges();
    expect(page.loadMoreButton).toBeTruthy();
  });

  it('should hide the "load more" button ', () => {
    const customerListService = TestBed.get(CustomerListService);
    customerListService.canLoadMore = false;
    fixture.detectChanges();
    expect(page.loadMoreButton.length).toBe(0);
  });

  it('should call loadMore()', () => {
    const customerListService = TestBed.get(CustomerListService);
    component.infiniteScrollDisabled = true;
    fixture.detectChanges();
    page.loadMoreButton[0].click();
    expect(customerListService.loadMore).toHaveBeenCalled();
  });

  it('should call loadMore() on scroll', () => {
    const customerListService = TestBed.get(CustomerListService);
    expect(page.infiniteScroll).toBeTruthy();
    const directive = page.infiniteScroll.injector.get<InfiniteScrollStubDirective>(InfiniteScrollStubDirective);
    directive.vitamuiScroll.next();
    expect(customerListService.loadMore).toHaveBeenCalled();
  });

  it('should open the owner creation dialog', () => {
    page.ownerBtn[2].click();
    const matDialogSpy = TestBed.get(MatDialog);
    expect(matDialogSpy.open.calls.count()).toBe(1);
    expect(matDialogSpy.open).toHaveBeenCalledWith(OwnerCreateComponent, {
      data: { customer: customers[2] },
      panelClass: 'vitamui-modal',
      disableClose: true,
    });
  });

  it('should add the new owner to the list', () => {
    const newOwner: Owner = {
      id: '42',
      identifier: '42',
      code: '00042',
      customerId: customers[0].id,
      name: 'Toto',
      companyName: 'Toto & Co',
      address: {
        street: null,
        zipCode: null,
        city: null,
        country: null,
      },
      readonly: false,
    };
    const matDialogSpy = TestBed.get(MatDialog);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of({ owner: newOwner }) });
    const addOwnerBtn = page.rows[0].querySelector('.btn.btn-circle.primary');
    addOwnerBtn.click();
    expect(customers[0].owners).toContain(newOwner);
  });

  it('should not add anything to the owners list', () => {
    const matDialogSpy = TestBed.get(MatDialog);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(undefined) });
    page.ownerBtn[0].click();
    expect(customers[0].owners.length).toBe(0);
  });

  it('should update the customer', () => {
    const customerService = TestBed.get(CustomerService);
    customerService.updated.next({ id: '12', name: 'Updated customer' });
    expect(component.dataSource[1].name).toBe('Updated customer');
  });

  function testRow(index: number) {
    const cells = page.rows[index].querySelectorAll('.d-flex div');
    expect(cells.length).toBe(7);
    expect(cells[1].textContent).toContain(customers[index].code);
    expect(cells[2].textContent).toContain(customers[index].name);
    expect(cells[3].textContent).toContain(customers[index].companyName);
  }
});
