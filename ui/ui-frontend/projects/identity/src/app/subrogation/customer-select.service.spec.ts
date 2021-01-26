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
import { ENVIRONMENT } from 'ui-frontend-common';
import { AuthService, BASE_URL, LoggerModule, Operators, SearchQuery } from 'ui-frontend-common';
import { environment } from './../../environments/environment';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { inject, TestBed } from '@angular/core/testing';

import { Type } from '@angular/core';
import { CustomerSelectService } from './customer-select.service';

describe('CustomerSelectService', () => {
  let httpTestingController: HttpTestingController;
  let searchService: CustomerSelectService;

  beforeEach(() => {
    const authStubService = { user: { id: 'fakeUserId', customerId: 'fakeCustomerId' } };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerModule.forRoot()],
      providers: [
        CustomerSelectService,
        { provide: AuthService, useValue: authStubService },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: ENVIRONMENT, useValue: environment }
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    searchService = TestBed.inject(CustomerSelectService);
  });

  it('should be created', inject([CustomerSelectService], (service: CustomerSelectService) => {
    expect(service).toBeTruthy();
  }));

  it('should call /fake-api/customers with the right params', () => {
    const subrogeable = true;
    searchService.getAll(true).subscribe(
      (response) => {
        expect(response).toEqual([
          { value: 'idteamvitamui', label: '000000 - teamvitamui' },
          { value: 'idtotal', label: '000001 - total' },
        ]);
      },
      fail
    );

    const criterionArray: any[] = [{ key: 'subrogeable', value: subrogeable, operator: Operators.equals }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/customers?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('GET');
    req.flush([
      { id: 'idteamvitamui', name: 'teamvitamui', code: '000000' },
      { id: 'idtotal', name: 'total', code: '000001' },
    ]);
  });

  it('should return an empty list if the API returns an error', () => {
    const subrogeable = true;
    searchService.getAll(true).subscribe(
      (response) => {
        expect(response).toEqual([]);
      },
      fail
    );

    const criterionArray: any[] = [{ key: 'subrogeable', value: subrogeable, operator: Operators.equals }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/customers?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('GET');
    const msg = 'deliberate 404 error';
    req.flush(msg, { status: 404, statusText: 'Not Found' });
  });

  it('should return an empty list if the API returns nothing', () => {
    const subrogeable = true;
    searchService.getAll(true).subscribe(
      (response) => {
        expect(response).toEqual([]);
      },
      fail
    );

    const criterionArray: any[] = [{ key: 'subrogeable', value: subrogeable, operator: Operators.equals }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/customers?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('GET');
    req.flush(null);
  });
});
