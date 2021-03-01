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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { Component, Input, NO_ERRORS_SCHEMA, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatMenuModule } from '@angular/material/menu';
import { MatTabsModule } from '@angular/material/tabs';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Subject } from 'rxjs';
import { Customer, ENVIRONMENT, LoggerModule, StartupService, WINDOW_LOCATION } from 'ui-frontend-common';
import { VitamUICommonTestModule } from 'ui-frontend-common/testing';
import { CustomerService } from '../../core/customer.service';
import { environment } from './../../../environments/environment';
import { CustomerPreviewComponent } from './customer-preview.component';

@Component({ selector: 'app-information-tab', template: '' })
export class InformationTabStubComponent {
  @Input() customer: Customer;
  @Input() readOnly: boolean;
  @Input() gdprReadOnlyStatus: boolean;
}

@Component({ selector: 'app-sso-tab', template: '' })
export class SsoTabStubComponent {
  @Input() customer: Customer;
  @Input() readOnly: boolean;
}

@Component({ selector: 'app-graphic-identity-tab', template: '' })
export class GraphicIdentityTabStubComponent {
  @Input() customer: Customer;
  @Input() readOnly: boolean;
}

@Component({ template: '<app-customer-preview [customer]="customer" [gdprReadOnlyStatus]="false"></app-customer-preview>' })
class TestHostComponent {
  customer: any;

  @ViewChild(CustomerPreviewComponent, { static: false }) component: CustomerPreviewComponent;
}

describe('CustomerPreviewComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(waitForAsync((() => {
    const customerServiceSpy = {
      updated: new Subject()
    };
    const startupServiceStub = { getPortalUrl: () => 'https://dev.vitamui.com',
    getConfigStringValue: () => 'https://dev.vitamui.com/identity' };
    TestBed.configureTestingModule({
      imports: [
        MatMenuModule,
        MatTabsModule,
        NoopAnimationsModule,
        ReactiveFormsModule,
        VitamUICommonTestModule,
        HttpClientTestingModule,
        LoggerModule.forRoot()
      ],
      declarations: [
        TestHostComponent,
        CustomerPreviewComponent,
        InformationTabStubComponent,
        SsoTabStubComponent,
        GraphicIdentityTabStubComponent,
      ],
      providers: [
        { provide: CustomerService, useValue: customerServiceSpy },
        { provide: StartupService, useValue: startupServiceStub },
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: ENVIRONMENT, useValue: environment }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .compileComponents();
  })));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    testhost.customer = { id: '11' };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(testhost).toBeTruthy();
  });

  it('should call window.open', () => {
    const openSpy = spyOn(window, 'open');
    openSpy.and.stub();
    testhost.component.openPopup();
    expect(openSpy).toHaveBeenCalledWith(
      'https://dev.vitamui.com/identity/customer/11',
      'detailPopup',
      'width=584, height=713, resizable=no, location=no'
    );
  });

});

