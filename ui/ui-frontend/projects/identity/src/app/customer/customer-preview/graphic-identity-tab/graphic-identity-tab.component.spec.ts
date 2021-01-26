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
import { Component } from '@angular/core';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { of } from 'rxjs';
import { ENVIRONMENT } from 'ui-frontend-common';
import { BASE_URL, Customer, LoggerModule, OtpState } from 'ui-frontend-common';


import { VitamUISnackBar } from '../../../shared/vitamui-snack-bar';
import { SafeStylePipe } from './../../../../../../../../ui-frontend-common/src/app/modules/pipes/safe-style.pipe';
import { environment } from './../../../../environments/environment';
import { GraphicIdentityTabComponent } from './graphic-identity-tab.component';

let expectedCustomer: Customer = {
  id: 'idCustomer',
  identifier: '1',
  enabled: true,
  readonly: false,
  hasCustomGraphicIdentity: false,
  code: '154785',
  name: 'nom du client',
  companyName: 'nom de la société',
  passwordRevocationDelay: 6,
  otp: OtpState.DEACTIVATED,
  idp: true,
  address: {
    street: '85 rue des bois',
    zipCode: '75013',
    city: 'Paris',
    country: 'France'
  },
  language: 'FRENCH',
  emailDomains: [
    'domain.com',
  ],
  defaultEmailDomain: 'domain.com',
  owners: [{
    id: 'znvuzhvyvg',
    identifier: '41',
    code: '254791',
    name: 'owner name',
    companyName: 'company name',
    address: {
      street: '85 rue des bois',
      zipCode: '75013',
      city: 'Paris',
      country: 'France'
    },
    customerId: 'idCustomer',
    readonly: false
  }],
  themeColors: {},
  gdprAlert : false,
  gdprAlertDelay : 72
};

@Component({
  template: `
    <app-graphic-identity-tab
      [customer]="customer"
      [readOnly]="readOnly"
    ></app-graphic-identity-tab>
  `
})
class TestHostComponent {
  customer = expectedCustomer;
  readOnly = false;
}

describe('GraphicIdentityTabComponent', () => {
  let testhost: TestHostComponent;
  let fixture: ComponentFixture<TestHostComponent>;

  beforeEach(waitForAsync(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const matDialogSpy = jasmine.createSpyObj('MatDialog', { open: { afterClosed: () => of(true) } });
    expectedCustomer = {
      id: 'idCustomer',
      identifier: '41',
      enabled: true,
      readonly: false,
      hasCustomGraphicIdentity: false,
      code: '154785',
      name: 'nom du client',
      companyName: 'nom de la société',
      passwordRevocationDelay: 6,
      otp: OtpState.DEACTIVATED,
      idp: true,
      address: {
        street: '85 rue des bois',
        zipCode: '75013',
        city: 'Paris',
        country: 'France'
      },
      language: 'FRENCH',
      emailDomains: [
        'domain.com',
      ],
      defaultEmailDomain: 'domain.com',
      owners: [{
        id: 'znvuzhvyvg',
        identifier: '41',
        code: '254791',
        name: 'owner name',
        companyName: 'company name',
        address: {
          street: '85 rue des bois',
          zipCode: '75013',
          city: 'Paris',
          country: 'France'
        },
        customerId: 'idCustomer',
        readonly: false
      }],
      themeColors: {},
      gdprAlert : false,
      gdprAlertDelay : 72
    };
    const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        LoggerModule.forRoot()
      ],
      declarations: [GraphicIdentityTabComponent, TestHostComponent, SafeStylePipe],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: MatDialog, useValue: matDialogSpy },
        { provide: MatDialogRef, useValue: matDialogRefSpy },
        { provide: MAT_DIALOG_DATA, useValue: {} },
        { provide: ENVIRONMENT, useValue: environment }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestHostComponent);
    testhost = fixture.componentInstance;
    fixture.detectChanges();

    testhost.customer = expectedCustomer;
  });

  it('should create', waitForAsync(() => {
    expect(testhost).toBeTruthy();
  }));

});
