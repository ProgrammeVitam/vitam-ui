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
import { ENVIRONMENT, LoggerModule } from 'ui-frontend-common';
import { BASE_URL, Customer, Operators, OtpState, SearchQuery } from 'ui-frontend-common';
import { environment } from './../../environments/environment';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { inject, TestBed } from '@angular/core/testing';

import { Type } from '@angular/core';
import { VitamUISnackBar, VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';
import { CustomerService } from './customer.service';

describe('CustomerService', () => {
  let httpTestingController: HttpTestingController;
  let customerService: CustomerService;

  beforeEach(() => {
    const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerModule.forRoot()],
      providers: [
        CustomerService,
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment }
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    customerService = TestBed.inject(CustomerService);
  });

  it('should be created', inject([CustomerService], (service: CustomerService) => {
    expect(service).toBeTruthy();
  }));

  it('should call /fake-api/customers and display a success message', () => {
    const snackBar = TestBed.inject(VitamUISnackBar);
    customerService.create(expectedCustomer).subscribe(
      (response: Customer) => {
        expect(response).toEqual(expectedCustomer);
        expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
        expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          data: { type: 'customerCreate', code: expectedCustomer.code },
          duration: 10000
        });
      },
      fail
    );
    const req = httpTestingController.expectOne('/fake-api/customers');
    expect(req.request.method).toEqual('POST');
    req.flush(expectedCustomer);
  });

  it('should display an error message', () => {
    const snackBar = TestBed.inject(VitamUISnackBar);
    customerService.create(expectedCustomer).subscribe(
      fail,
      () => {
        expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
        expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          data: { type: 'customerCreateError' },
          duration: 10000
        });
      }
    );
    const req = httpTestingController.expectOne('/fake-api/customers');
    expect(req.request.method).toEqual('POST');
    req.flush({ message: 'Expected message' }, { status: 400, statusText: 'Bad request' });
  });

  it('should return true if the customer exists', () => {
    customerService.exists({ code: '015000' }).subscribe((found) => expect(found).toBeTruthy(), fail);

    const criterionArray: any[] = [{ key: 'code', value: '015000', operator: Operators.equals }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/customers/check?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('HEAD');
    req.flush('');
  });

  it('should return false if the customer does not exist', () => {
    customerService.exists({ code: '123456' }).subscribe((found) => expect(found).toBeFalsy(), fail);

    const criterionArray: any[] = [{ key: 'code', value: '123456', operator: Operators.equals }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/customers/check?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('HEAD');
    req.flush('', { status: 204, statusText: 'No Content' });
  });

  it('should return true if the domain exists', () => {
    customerService.exists({ domain: 'test.com' }).subscribe((found) => expect(found).toBeTruthy(), fail);

    const criterionArray: any[] = [{ key: 'emailDomains', value: 'test.com', operator: Operators.containsIgnoreCase }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/customers/check?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('HEAD');
    req.flush('');
  });

  it('should return false if the domain does not exist', () => {
    customerService.exists({ domain: 'test.com' }).subscribe((found) => expect(found).toBeFalsy(), fail);

    const criterionArray: any[] = [{ key: 'emailDomains', value: 'test.com', operator: Operators.containsIgnoreCase }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/customers/check?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('HEAD');
    req.flush('', { status: 204, statusText: 'No Content' });
  });

  it('should call /fake-api/customers/42', () => {
    customerService.get('42').subscribe((customer: Customer) => expect(customer).toEqual(expectedCustomer), fail);
    const req = httpTestingController.expectOne('/fake-api/customers/42');
    expect(req.request.method).toEqual('GET');
    req.flush(expectedCustomer);
  });

});

const expectedCustomer: Customer = {
  id: '42',
  identifier: '42',
  enabled: true,
  readonly: false,
  hasCustomGraphicIdentity: false,
  idp: false,
  code: '424242',
  name: 'John Doe',
  companyName: 'John Co.',
  passwordRevocationDelay: 3,
  otp: OtpState.OPTIONAL,
  address: {
    street: 'street',
    zipCode: '12345',
    city: 'New York',
    country: 'US',
  },
  language: 'en',
  emailDomains: ['test.com', 'toto.co.uk'],
  defaultEmailDomain: 'test.com',
  owners: [{
    id: '6',
    identifier: '6',
    customerId: '42',
    code: '666666',
    name: 'Alice Vans',
    companyName: 'Vans',
    address: {
      street: 'street2',
      zipCode: '43121',
      city: 'Paris',
      country: 'FR',
    },
    readonly: false
  }],
  themeColors: {},
  gdprAlert : false,
  gdprAlertDelay : 72
};
