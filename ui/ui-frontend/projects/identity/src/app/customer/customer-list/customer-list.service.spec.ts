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

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Type } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ENVIRONMENT } from 'ui-frontend-common';
import { BASE_URL, Customer, Direction, LoggerModule, OtpState, PageRequest } from 'ui-frontend-common';
import { environment } from './../../../environments/environment';
import { CustomerListService } from './customer-list.service';

const expectedCustomersPage: { values: Customer[], pageNum: number, pageSize: number, hasMore: boolean } = {
  values: [
    {
      id: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
      identifier: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
      code: '015000',
      name: 'TeamVitamUI',
      companyName: 'vitamui',
      enabled: true,
      readonly: false,
      hasCustomGraphicIdentity: false,
      language: 'FRENCH',
      passwordRevocationDelay: 365,
      otp: OtpState.OPTIONAL,
      emailDomains: [
        'vitamui.com',
      ],
      defaultEmailDomain: 'vitamui.com',
      owners: [
        {
          id: '1',
          identifier: '1',
          customerId: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
          name: 'Emmanuel Deviller',
          code: '002345',
          companyName: 'vitamui',
          address: {
            street: '73 rue du Faubourg Poissonnière ',
            zipCode: '75009',
            city: 'Paris',
            country: 'France'
          },
          readonly: false,

        },
        {
          id: '2',
          identifier: '2',
          customerId: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
          name: 'Julien Cornille',
          code: '002345',
          companyName: 'vitamui',
          address: {
            street: '73 rue du Faubourg Poissonnière ',
            zipCode: '75009',
            city: 'Paris',
            country: 'France'
          },
          readonly: false
        },
      ],
      internalCode: '1',
      address: {
        street: '73 rue du Faubourg Poissonnière ',
        zipCode: '75009',
        city: 'Paris',
        country: 'France'
      },
      themeColors: {},
      gdprAlert : false,
      gdprAlertDelay : 72,
      portalMessage: 'message',
      portalTitle: 'title'
    },
    {
      id: '5acc6bd8b75bfb2e46aeec41e0973280907b4bc7a918b07df78df36f501b3ba5',
      identifier: '5acc6bd8b75bfb2e46aeec41e0973280907b4bc7a918b07df78df36f501b3ba5',
      code: '025000',
      name: 'EDF',
      companyName: 'Electricité de france',
      enabled: true,
      readonly: false,
      hasCustomGraphicIdentity: false,
      language: 'ENGLISH',
      passwordRevocationDelay: 365,
      otp: OtpState.OPTIONAL,
      emailDomains: [
        'edf.com',
      ],
      defaultEmailDomain: 'edf.com',
      owners: [
        {
          id: '4',
          identifier: '4',
          customerId: '5acc6bd8b75bfb2e46aeec41e0973280907b4bc7a918b07df78df36f501b3ba5',
          name: 'Mr Président',
          code: '022345',
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
          id: '5',
          identifier: '5',
          customerId: '5acc6bd8b75bfb2e46aeec41e0973280907b4bc7a918b07df78df36f501b3ba5',
          name: 'Jean Dupond',
          code: '025000',
          companyName: 'Electricité de france',
          address: {
            street: '22-30 Avenue de WAGRAM',
            zipCode: '75008',
            city: 'Paris',
            country: 'France'
          },
          readonly: false
        },
      ],
      internalCode: '1',
      address: {
        street: '22-30 Avenue de WAGRAM',
        zipCode: '75008',
        city: 'Paris',
        country: 'France'
      },
      themeColors: {},
      gdprAlert : false,
      gdprAlertDelay : 72,
      portalMessage: 'message',
      portalTitle: 'title'
    },
  ],
  pageNum: 0,
  pageSize: 20,
  hasMore: false
};

const customersPage: { values: Customer[], pageNum: number, pageSize: number, hasMore: boolean } = {
  values: [
    {
      id: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
      identifier: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
      code: '015000',
      name: 'TeamVitamUI',
      companyName: 'vitamui',
      enabled: true,
      readonly: false,
      hasCustomGraphicIdentity: false,
      language: 'FRENCH',
      passwordRevocationDelay: 365,
      otp: OtpState.OPTIONAL,
      emailDomains: [
        'vitamui.com',
      ],
      defaultEmailDomain: 'vitamui.com',
      owners: [
        {
          id: '1',
          identifier: '1',
          customerId: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
          name: 'Emmanuel Deviller',
          code: '002345',
          companyName: 'vitamui',
          address: {
            street: '73 rue du Faubourg Poissonnière ',
            zipCode: '75009',
            city: 'Paris',
            country: 'France'
          },
          readonly: false
        },
        {
          id: '2',
          identifier: '2',
          customerId: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
          name: 'Julien Cornille',
          code: '002345',
          companyName: 'vitamui',
          address: {
            street: '73 rue du Faubourg Poissonnière ',
            zipCode: '75009',
            city: 'Paris',
            country: 'France'
          },
          readonly: false
        },
      ],
      internalCode: '1',
      address: {
        street: '73 rue du Faubourg Poissonnière ',
        zipCode: '75009',
        city: 'Paris',
        country: 'France'
      },
      themeColors: {},
      gdprAlert : false,
      gdprAlertDelay : 72,
      portalMessage: 'message',
      portalTitle: 'title'
    },
    {
      id: '5acc6bd8b75bfb2e46aeec41e0973280907b4bc7a918b07df78df36f501b3ba5',
      identifier: '5acc6bd8b75bfb2e46aeec41e0973280907b4bc7a918b07df78df36f501b3ba5',
      code: '025000',
      name: 'EDF',
      companyName: 'Electricité de france',
      enabled: true,
      readonly: false,
      hasCustomGraphicIdentity: false,
      language: 'ENGLISH',
      passwordRevocationDelay: 365,
      otp: OtpState.OPTIONAL,
      emailDomains: [
        'edf.com',
      ],
      defaultEmailDomain: 'edf.com',
      owners: [
        {
          id: '4',
          identifier: '4',
          customerId: '5acc6bd8b75bfb2e46aeec41e0973280907b4bc7a918b07df78df36f501b3ba5',
          name: 'Mr Président',
          code: '022345',
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
          id: '5',
          identifier: '5',
          customerId: '5acc6bd8b75bfb2e46aeec41e0973280907b4bc7a918b07df78df36f501b3ba5',
          name: 'Jean Dupond',
          code: '025000',
          companyName: 'Electricité de france',
          address: {
            street: '22-30 Avenue de WAGRAM',
            zipCode: '75008',
            city: 'Paris',
            country: 'France'
          },
          readonly: false
        },
      ],
      internalCode: '1',
      address: {
        street: '22-30 Avenue de WAGRAM',
        zipCode: '75008',
        city: 'Paris',
        country: 'France'
      },
      themeColors: {},
      gdprAlert : false,
      gdprAlertDelay : 72,
      portalMessage: 'message',
      portalTitle: 'title'
    },
  ],
  pageNum: 0,
  pageSize: 20,
  hasMore: false
};

describe('CustomerListService', () => {
  let httpTestingController: HttpTestingController;
  let customerListService: CustomerListService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerModule.forRoot()],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment }
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    customerListService = TestBed.inject(CustomerListService);
  });

  it('should be created', () => {
    const service: CustomerListService = TestBed.inject(CustomerListService);
    expect(service).toBeTruthy();
  });

  it('should call /fake-api/customers?page=0&size=20&orderBy=code&direction=ASC', () => {
    customerListService.search().subscribe((response) => expect(response).toEqual(expectedCustomersPage.values), fail);
    const req = httpTestingController.expectOne('/fake-api/customers?page=0&size=20&orderBy=code&direction=ASC&embedded=OWNER,TENANT');
    expect(req.request.method).toEqual('GET');
    req.flush(customersPage);
  });

  it('should call /fake-api/customers?page=42&size=15&orderBy=code&direction=DESC', () => {
    customerListService.search(new PageRequest(42, 15, 'name', Direction.DESCENDANT))
      .subscribe((response) => expect(response).toEqual(expectedCustomersPage.values), fail);
    const req = httpTestingController.expectOne(
      '/fake-api/customers?page=42&size=15&orderBy=name&direction=DESC&embedded=OWNER,TENANT');
    expect(req.request.method).toEqual('GET');
    req.flush(customersPage);
  });

  it('should call /fake-api/customers?page=0&size=15&orderBy=companyName&direction=DESC', () => {
    customersPage.hasMore = true;
    customersPage.pageSize = 15;
    customerListService.search(new PageRequest(0, 15, 'companyName', Direction.DESCENDANT))
      .subscribe((response) => expect(response).toEqual(expectedCustomersPage.values), fail);
    let req = httpTestingController.expectOne(
      '/fake-api/customers?page=0&size=15&orderBy=companyName&direction=DESC&embedded=OWNER,TENANT');
    expect(req.request.method).toEqual('GET');
    req.flush(customersPage);

    customersPage.pageNum = 1;
    customersPage.hasMore = false;
    customerListService.loadMore().subscribe(
      (response) => expect(response).toEqual(expectedCustomersPage.values.concat(expectedCustomersPage.values)),
      fail
    );
    req = httpTestingController.expectOne(
      '/fake-api/customers?page=1&size=15&orderBy=companyName&direction=DESC&embedded=OWNER,TENANT');
    expect(req.request.method).toEqual('GET');
    req.flush(customersPage);
  });

  it('should not load more results', () => {
    customersPage.hasMore = false;
    customersPage.pageSize = 20;
    customerListService.search().subscribe((response) => expect(response).toEqual(expectedCustomersPage.values), fail);
    const req = httpTestingController.expectOne('/fake-api/customers?page=0&size=20&orderBy=code&direction=ASC&embedded=OWNER,TENANT');
    expect(req.request.method).toEqual('GET');
    req.flush(customersPage);

    customerListService.loadMore().subscribe(
      (response) => expect(response).toEqual(expectedCustomersPage.values),
      fail
    );
    httpTestingController.expectNone('/fake-api/customers?page=1&size=20&orderBy=code&direction=ASC&embedded=OWNER,TENANT');
  });

  it('should return false', () => {
    expect(customerListService.canLoadMore).toBeFalsy();
  });

  it('should return true', () => {
    customersPage.hasMore = true;
    customerListService.search().subscribe(
      (response) => {
        expect(response).toEqual(expectedCustomersPage.values);
        expect(customerListService.canLoadMore).toBeTruthy();
      },
      fail
    );
    const req = httpTestingController.expectOne('/fake-api/customers?page=0&size=20&orderBy=code&direction=ASC&embedded=OWNER,TENANT');
    expect(req.request.method).toEqual('GET');
    req.flush(customersPage);
  });
});
