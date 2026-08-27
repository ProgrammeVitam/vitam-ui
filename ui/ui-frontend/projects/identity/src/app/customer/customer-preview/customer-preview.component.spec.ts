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
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, Input, NO_ERRORS_SCHEMA, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import type { Customer } from 'vitamui-library';
import { ENVIRONMENT, OperationHistoryTabComponent, StartupService, WINDOW_LOCATION } from 'vitamui-library';
import { CustomerService } from '../../core/customer.service';
import { environment } from './../../../environments/environment';
import { CustomerPreviewComponent } from './customer-preview.component';
import { InformationTabComponent } from './information-tab/information-tab.component';
import { SsoTabComponent } from './sso-tab/sso-tab.component';
import { GraphicIdentityTabComponent } from './graphic-identity-tab/graphic-identity-tab.component';
import { HomepageMessageTabComponent } from './homepage-message-tab/homepage-message-tab.component';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

@Component({
  selector: 'app-information-tab',
  template: '',
})
export class InformationTabStubComponent {
  @Input() customer: Customer;
  @Input() readOnly: boolean;
  @Input() gdprReadOnlyStatus: boolean;
}

@Component({
  selector: 'app-sso-tab',
  template: '',
})
export class SsoTabStubComponent {
  @Input() customer: Customer;
  @Input() readOnly: boolean;
}

@Component({
  selector: 'app-graphic-identity-tab',
  template: '',
})
export class GraphicIdentityTabStubComponent {
  @Input() customer: Customer;
  @Input() readOnly: boolean;
}

@Component({
  selector: 'app-homepage-message-tab',
  template: '',
})
export class HomepageMessageTabStubComponent {
  @Input() customer: Customer;
  @Input() readOnly: boolean;
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'vitamui-common-operation-history-tab',
  template: '',
})
export class OperationHistoryTabStubComponent {
  @Input() id: string;
  @Input() identifier: string;
  @Input() collectionName: string;
}

@Component({
  template: '<app-customer-preview [customer]="customer" [gdprReadOnlyStatus]="false"></app-customer-preview>',
  imports: [CustomerPreviewComponent],
  schemas: [NO_ERRORS_SCHEMA],
})
class TestHostComponent {
  customer: any;

  @ViewChild(CustomerPreviewComponent, { static: false })
  component: CustomerPreviewComponent;
}

describe('CustomerPreviewComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(async () => {
    const customerServiceSpy = {
      updated: new Subject(),
    };
    const startupServiceStub = {
      getPortalUrl: () => 'https://dev.vitamui.com',
      getConfigStringValue: () => 'https://dev.vitamui.com/identity',
      getConfigNumberValue: () => 0,
    };
    await TestBed.configureTestingModule({
      imports: [TestHostComponent],
      providers: [
        { provide: CustomerService, useValue: customerServiceSpy },
        { provide: StartupService, useValue: startupServiceStub },
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: ENVIRONMENT, useValue: environment },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
      ],
    })
      .overrideComponent(CustomerPreviewComponent, {
        remove: {
          imports: [
            InformationTabComponent,
            SsoTabComponent,
            GraphicIdentityTabComponent,
            HomepageMessageTabComponent,
            OperationHistoryTabComponent,
          ],
        },
        add: {
          imports: [
            InformationTabStubComponent,
            SsoTabStubComponent,
            GraphicIdentityTabStubComponent,
            HomepageMessageTabStubComponent,
            OperationHistoryTabStubComponent,
          ],
        },
      })
      .compileComponents();
  });

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
    const openSpy = vi.spyOn(window, 'open');
    openSpy.mockImplementation(() => null as any);
    testhost.component.openPopup();
    expect(openSpy).toHaveBeenCalledWith(
      'https://dev.vitamui.com/identity/customer/11',
      'detailPopup',
      'width=584, height=713, resizable=no, location=no',
    );
  });
});
