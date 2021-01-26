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
import {HttpClientTestingModule} from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {BASE_URL, Customer, ENVIRONMENT, InjectorModule, LoggerModule, OtpState} from 'ui-frontend-common';
import {VitamUICommonTestModule} from 'ui-frontend-common/testing';
import {environment} from './../../../../../environments/environment';

import {VitamUISnackBar} from '../../../../shared/vitamui-snack-bar';
import {AccessContractService} from '../../../access-contract.service';
import {AccessContractNodeUpdateComponent} from './access-contract-node-update.component';

const expectedCustomer: Customer = {
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
  gdprAlert: false,
  gdprAlertDelay : 72
};

// TODO fix tests with filling plan
xdescribe('AccessContractNodeUpdateComponent', () => {
  let component: AccessContractNodeUpdateComponent;
  let fixture: ComponentFixture<AccessContractNodeUpdateComponent>;

  beforeEach(waitForAsync(() => {
    const matDialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);
    const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);
    TestBed.configureTestingModule({
      imports: [
        ReactiveFormsModule,
        HttpClientTestingModule,
        VitamUICommonTestModule,
        InjectorModule,
        LoggerModule.forRoot()
      ],
      declarations: [AccessContractNodeUpdateComponent],
      providers: [
        {provide: MatDialogRef, useValue: matDialogRefSpy},
        {provide: MAT_DIALOG_DATA, useValue: {customer: expectedCustomer, logo: null}},
        {provide: BASE_URL, useValue: '/fake-api'},
        {provide: VitamUISnackBar, useValue: snackBarSpy},
        {provide: ENVIRONMENT, useValue: environment},
        {provide: AccessContractService, useValue: {}}
      ]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(AccessContractNodeUpdateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
