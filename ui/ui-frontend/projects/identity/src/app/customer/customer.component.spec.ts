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


import { Component, EventEmitter, Input, Output } from '@angular/core';
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { EMPTY, of } from 'rxjs';
import { ENVIRONMENT, InjectorModule, LoggerModule } from 'ui-frontend-common';
import { environment } from './../../environments/environment';

import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatSidenavModule } from '@angular/material/sidenav';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { CustomerCreateComponent } from './customer-create/customer-create.component';
import { CustomerComponent } from './customer.component';

import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { CustomerService } from '../core/customer.service';

let component: CustomerComponent;
let fixture: ComponentFixture<CustomerComponent>;

class Page {

  get customerList() { return fixture.nativeElement.querySelector('app-customer-list'); }
  get createCustomer() { return fixture.nativeElement.querySelector('.actions button:first-child'); }

}

let page: Page;

@Component({ selector: 'app-customer-list', template: '' })
class CustomerListStubComponent {
  search() { }
}

@Component({ selector: 'app-customer-preview', template: '' })
class CustomerPreviewStubComponent {
  @Input() customer: any;
  @Output() previewClose = new EventEmitter();
  @Input() gdprReadOnlySettingStatus: boolean;
}

@Component({ selector: 'app-owner-preview', template: '' })
class OwnerPreviewStubComponent {
  @Input() owner: any;
  @Input() tenant: any;
  @Output() previewClose = new EventEmitter();
}

describe('CustomerComponent', () => {
  const customerServiceSpy = {
    getGdprReadOnlySettingStatus: () => of(true)
  };


  beforeEach(async(() => {
    const matDialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
    matDialogSpy.open.and.returnValue({ afterClosed: () => of(true) });

    TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatSidenavModule,
        NoopAnimationsModule,
        VitamUICommonTestModule,
        InjectorModule,
        LoggerModule.forRoot()
      ],
      declarations: [
        CustomerComponent,
        CustomerListStubComponent,
        CustomerPreviewStubComponent,
        OwnerPreviewStubComponent,
      ],
      providers: [
        { provide: CustomerService, useValue: customerServiceSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: ActivatedRoute, useValue: { data: EMPTY } },
        { provide: ENVIRONMENT, useValue: environment }
      ]
    })
      .compileComponents();
  }));

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
    const matDialogSpy = TestBed.get(MatDialog);
    page.createCustomer.click();
    expect(matDialogSpy.open).toHaveBeenCalledWith(CustomerCreateComponent, { panelClass: 'vitamui-modal', disableClose: true });
  });

});
