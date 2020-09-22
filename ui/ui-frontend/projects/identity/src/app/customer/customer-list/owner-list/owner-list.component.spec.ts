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
/* tslint:disable: max-classes-per-file no-magic-numbers */
import { Component, ViewChild } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { of ,  Subject } from 'rxjs';

import { AddressType, OtpState, Owner, Tenant } from 'ui-frontend-common';
import { extend } from 'underscore';
import { CustomerDataService } from '../../customer.data.service';
import { OwnerCreateComponent } from '../../owner-create/owner-create.component';
import { OwnerService } from '../../owner.service';
import { TenantService } from '../../tenant.service';
import { OwnerListComponent } from './owner-list.component';

const tenants = [
  {
    id: '5ad5f14c894e6a414edc7b6c12bbdb29306b4297a692146319e86e7beabb31c8',
    customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
    ownerId: '5ad5f14c894e6a414edc7b6d',
    name: 'edf',
    identifier: 1,
    enabled: true,
    proof: true,
    readonly: false,
    accessContractHoldingIdentifier: 'AC-000001',
    accessContractLogbookIdentifier: 'AC-000002',
    ingestContractHoldingIdentifier: 'IC-000001',
    itemIngestContractIdentifier: 'IC-000001'
  },
  {
    id: '5ad5f14c894e6a414edc7b6666641b9490fa4d7eaa1e32ad37c4546a4c7eebc5',
    customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
    ownerId: '5ad5f14c894e6a414edc7b67',
    name: 'Mr Président',
    identifier: 8,
    enabled: true,
    proof: false,
    readonly: false,
    accessContractHoldingIdentifier: 'AC-000001',
    accessContractLogbookIdentifier: 'AC-000002',
    ingestContractHoldingIdentifier: 'IC-000001',
    itemIngestContractIdentifier: 'IC-000001'
  },
];

@Component({
  template: `
    <app-owner-list [customer]='customer'></app-owner-list>
  `
})
class TesthostComponent {

  @ViewChild(OwnerListComponent, { static: false }) ownerListComponent: OwnerListComponent;

  customer = {
    id: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
    code: '025000',
    name: 'EDF',
    readonly: false,
    companyName: 'Electricité de france',
    language: 'ENGLISH',
    passwordRevocationDelay: 365,
    otp: OtpState.OPTIONAL,
    emailDomains: ['edf.com'],
    defaultEmailDomain: 'edf.com',
    owners: [
      {
        id: '5ad5f14c894e6a414edc7b6d',
        customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
        name: 'Jean Dupond',
        code: '135000',
        companyName: 'Electricité de france',
        address: {
          street: '22-30 Avenue de WAGRAM',
          zipCode: '75008',
          city: 'Paris',
          country: 'France'
        },
        readonly: false
      },
      {
        id: '5ad5f14c894e6a414edc7b67',
        customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
        name: 'Mr Président',
        code: '02234512',
        companyName: 'Electricité de france',
        address: {
          street: '22-30 Avenue de WAGRAM',
          zipCode: '75008',
          city: 'Paris',
          country: 'France'
        },
        readonly: false
      },
      {
        id: '5ad8709e1a15ff1a47443874',
        customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
        name: 'test6',
        code: '111111111',
        companyName: 'test6',
        address: {
          street: 'test 7',
          zipCode: '12345',
          city: 'Paris',
          country: 'FR'
        },
        readonly: false
      },
      {
        id: '5ad87a7c1a15ff1a47443875',
        customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
        name: 'n1',
        code: '111111111111111',
        companyName: 'r1',
        address: {
          street: 's1',
          zipCode: 'z1',
          city: 'c1',
          country: 'FR'
        },
        readonly: false
      },
    ],
    address: {
      street: '22-30 Avenue de WAGRAM',
      zipCode: '75008',
      city: 'Paris',
      country: 'France'
    }
  };

}

let fixture: ComponentFixture<TesthostComponent>;
let testhost: TesthostComponent;

class Page {

  get rows() { return fixture.nativeElement.querySelectorAll('table > tbody > tr'); }
  get addOwnerBtn() { return fixture.nativeElement.querySelector('.footer > button'); }

}

let page: Page;

describe('OwnerListComponent', () => {

  beforeEach(async(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    const matDialogSpy = jasmine.createSpyObj('MatDialog', { open: { afterClosed: () => of(true) } });

    TestBed.configureTestingModule({
      imports: [NoopAnimationsModule],
      declarations: [ TesthostComponent, OwnerListComponent ],
      providers: [
        { provide: Router, useValue: routerSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: OwnerService, useValue: { updated: new Subject() } },
        { provide: TenantService, useValue: { updated: new Subject() } },
        CustomerDataService,
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    const customerDataService = TestBed.get(CustomerDataService);
    customerDataService.addTenants(tenants);

    fixture = TestBed.createComponent(TesthostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();
    page = new Page();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should have 6 rows', () => {
    const ownersWithoutTenant = testhost.customer.owners
    .filter((owner) => tenants.filter((tenant) => tenant.ownerId === owner.id).length > 0);

    expect(page.rows.length).toBe(ownersWithoutTenant.length + tenants.length);
  });

  it('should have the right content in the table', () => {
    let cells = page.rows[0].querySelectorAll('td');
    expect(cells.length).toBe(5);
    expect(cells[1].textContent).toContain(testhost.customer.owners[0].code);
    expect(cells[2].textContent).toContain(testhost.customer.owners[0].name);
    expect(cells[3].textContent).toContain(tenants[0].identifier);

    cells = page.rows[1].querySelectorAll('td');
    expect(cells.length).toBe(5);
    expect(cells[1].textContent).toContain(testhost.customer.owners[1].code);
    expect(cells[2].textContent).toContain(testhost.customer.owners[1].name);
    expect(cells[3].textContent).toContain(tenants[1].identifier);

    cells = page.rows[2].querySelectorAll('td');
    expect(cells.length).toBe(5);
    expect(cells[1].textContent).toContain(testhost.customer.owners[2].code);
    expect(cells[2].textContent).toContain(testhost.customer.owners[2].name);

    cells = page.rows[3].querySelectorAll('td');
    expect(cells.length).toBe(5);
    expect(cells[1].textContent).toContain(testhost.customer.owners[3].code);
    expect(cells[2].textContent).toContain(testhost.customer.owners[3].name);
  });

  it('should open the owner creation dialog', () => {
    page.addOwnerBtn.click();
    const matDialogSpy = TestBed.get(MatDialog);
    expect(matDialogSpy.open.calls.count()).toBe(1);
    expect(matDialogSpy.open).toHaveBeenCalledWith(OwnerCreateComponent, {
      data: { customer: testhost.customer },
      panelClass: 'vitamui-modal',
      disableClose: true
    });
  });

  it('should add the new owner to the list', () => {
    const newOwner: Owner = {
      id: '42',
      identifier: '42',
      code: '00042',
      customerId: testhost.customer.id,
      name: 'Toto',
      companyName: 'Toto & Co',
      addressType: AddressType.POSTAL,
      address: {
        street: null,
        zipCode: null,
        city: null,
        country: null
      },
      readonly : false
    };
    const matDialogSpy = TestBed.get(MatDialog);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of({ owner: newOwner }) });
    page.addOwnerBtn.click();
    expect(testhost.customer.owners.length).toBe(5);
    expect(testhost.customer.owners).toContain(newOwner);
  });

  it('should not add anything to the owners list', () => {
    const matDialogSpy = TestBed.get(MatDialog);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(undefined) });
    page.addOwnerBtn.click();
    expect(testhost.customer.owners.length).toBe(4);
  });

  it('should add the new tenant to the list', () => {
    const newTenant: Tenant = {
      id: '5ad5f14c894e6a414edc7b6666641b9490fa4d7eaa1e32ad37c4546a4c7eebc5',
      customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
      ownerId: '5ad5f14c894e6a414edc7b67',
      name: 'Mr Président',
      identifier: 8,
      enabled: true,
      proof: false,
      readonly : false,
      accessContractHoldingIdentifier: 'AC-000001',
      accessContractLogbookIdentifier: 'AC-000002',
      ingestContractHoldingIdentifier: 'IC-000001',
      itemIngestContractIdentifier: 'IC-000001'
    };
    const matDialogSpy = TestBed.get(MatDialog);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of({ tenant: newTenant }) });
    page.addOwnerBtn.click();
    expect(tenants.length).toBe(2);
    expect(tenants).toContain(newTenant);
  });

  it('should open the tenant creation dialog', () => {
    page.rows[2].querySelector('.actions > button:first-child').click();
    const matDialogSpy = TestBed.get(MatDialog);
    expect(matDialogSpy.open.calls.count()).toBe(1);
    expect(matDialogSpy.open).toHaveBeenCalled();
  });

  it('should add the new tenant to the list and remove the owner', () => {
    const newTenant: Tenant = {
      id: '5ad5f14c894e6a414edc7b6666641b9490fa4d7eaa1e32ad37c4546a4c7eebc5',
      customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
      ownerId: '5ad5f14c894e6a414edc7b67',
      name: 'Mr Président',
      identifier: 8,
      enabled: true,
      proof: false,
      readonly : false,
      accessContractHoldingIdentifier: 'AC-000001',
      accessContractLogbookIdentifier: 'AC-000002',
      ingestContractHoldingIdentifier: 'IC-000001',
      itemIngestContractIdentifier: 'IC-000001'
    };
    const matDialogSpy = TestBed.get(MatDialog);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of({ tenant: newTenant }) });
    page.rows[2].querySelector('.actions > button:first-child').click();
    expect(tenants.length).toBe(2);
    expect(tenants).toContain(newTenant);
    expect(testhost.customer.owners.length).toBe(4);
    expect(testhost.customer.owners).toContain({
      id: '5ad5f14c894e6a414edc7b67',
      customerId: '5ad5f14c894e6a414edc7b6438c3dd882b4145bb8a8a240726d66d64c9e878bc',
      name: 'Mr Président',
      code: '02234512',
      companyName: 'Electricité de france',
      address: {
        street: '22-30 Avenue de WAGRAM',
        zipCode: '75008',
        city: 'Paris',
        country: 'France'
      },
      readonly : false
    });
  });

  it('should update the owner', () => {
    const ownerService = TestBed.get(OwnerService);
    ownerService.updated.next({ id: '5ad5f14c894e6a414edc7b6d', name: 'Updated owner' });
    expect(testhost.customer.owners[0].name).toBe('Updated owner');
  });

  it('should update the tenant', () => {
    const tenantService = TestBed.get(TenantService);
    const tenant: Tenant = extend({}, tenants[0]);
    tenant.name = 'Updated tenant';

    tenantService.updated.next(tenant);
    expect(testhost.ownerListComponent.myTenants[0].name).toBe('Updated tenant');
  });

});
