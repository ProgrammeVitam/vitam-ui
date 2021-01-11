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
/* tslint:disable: no-magic-numbers */

import { inject, TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { Customer, OtpState } from 'ui-frontend-common';
import { CustomerService } from '../core/customer.service';
import { CustomerResolver } from './customer-resolver.service';

const expectedCustomer: Customer = {
  id: '11',
  identifier : '11',
  code: '011000',
  name: 'Kouygues Telecom',
  companyName: 'Kouygues Telecom',
  enabled: true,
  readonly: false,
  hasCustomGraphicIdentity: false,
  language: 'ENGLISH',
  passwordRevocationDelay: 1,
  otp: OtpState.OPTIONAL,
  idp: false,
  emailDomains: [
    'kouygues.com',
  ],
  defaultEmailDomain: 'kouygues.com',
  address: {
    street: '13 rue faubourg',
    zipCode: '75009',
    city: 'paris',
    country: 'france'
  },
  owners: [],
  themeColors: {}
};

describe('CustomerResolver', () => {
  let customerResolver: CustomerResolver;

  beforeEach(() => {
    const customerServiceSpy = jasmine.createSpyObj('CustomerService', ['get']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        CustomerResolver,
        { provide: CustomerService, useValue: customerServiceSpy },
        { provide: Router, useValue: routerSpy },
      ]
    });

    customerResolver = TestBed.get(CustomerResolver);
  });

  it('should be created', inject([CustomerResolver], (service: CustomerResolver) => {
    expect(service).toBeTruthy();
  }));

  it('should get the client with the id', () => {
    const route = new ActivatedRouteSnapshot();
    spyOn(route.paramMap, 'get').and.returnValue('42');

    const customerService = TestBed.get(CustomerService);
    customerService.get.and.returnValue(of(expectedCustomer));
    customerResolver.resolve(route).subscribe((customer) => {
      expect(customer).toEqual(expectedCustomer);
    });

    expect(route.paramMap.get).toHaveBeenCalledWith('id');
    expect(customerService.get).toHaveBeenCalledWith('42');
  });

  it('should redirect to / if an error occurs', () => {
    const route = new ActivatedRouteSnapshot();
    spyOn(route.paramMap, 'get').and.returnValue('42');
    const customerService = TestBed.get(CustomerService);
    customerService.get.and.returnValue(of(null));
    const router = TestBed.get(Router);
    customerResolver.resolve(route).subscribe(() => {
      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });
    expect(route.paramMap.get).toHaveBeenCalledWith('id');
    expect(customerService.get).toHaveBeenCalledWith('42');
  });

});
