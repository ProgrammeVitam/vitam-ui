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
import { BASE_URL, Operators, Owner, SearchQuery, Tenant } from 'ui-frontend-common';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { inject, TestBed } from '@angular/core/testing';

import { Type } from '@angular/core';
import { VitamUISnackBar, VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';
import { TenantService } from './tenant.service';

const expectedTenant: Tenant = {
  id: '42',
  customerId: '43',
  ownerId: '5ad5f14c894e6a414edc7b62',
  name: 'Emmanuel Deviller',
  identifier: 7,
  enabled: true,
  proof: true,
  readonly: false,
  accessContractHoldingIdentifier: 'AC-000001',
  accessContractLogbookIdentifier: 'AC-000002',
  ingestContractHoldingIdentifier: 'IC-000001',
  itemIngestContractIdentifier: 'IC-000001'
};

const expectedOwner: Owner = {
  id: '44',
  identifier: '44',
  customerId: '43',
  name: 'Emmanuel Deviller',
  code: '10234501',
  companyName: 'vitamui',
  readonly: false,
  address: {
    street: '73 rue du Faubourg Poissonnière ',
    zipCode: '75009',
    city: 'Paris',
    country: 'France'
  }
};

const expectedTenants = [
  {
    id: '0',
    customerId: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
    ownerId: '1',
    name: 'Emmanuel Deviller',
    identifier: 0,
    enabled: true,
    proof: true,
    readonly: false,
    accessContractHoldingIdentifier: 'AC-000001',
    accessContractLogbookIdentifier: 'AC-000002',
    ingestContractHoldingIdentifier: 'IC-000001',
    itemIngestContractIdentifier: 'IC-000001'
  },
  {
    id: '1',
    customerId: '5acc6bd8b75bfb2e46aeec3da96bdc5206a641e2babc48ec12473c206c4bb97d',
    ownerId: '3',
    name: 'vitamui',
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
    id: '2',
    customerId: '5acc6bd8b75bfb2e46aeec41e0973280907b4bc7a918b07df78df36f501b3ba5',
    ownerId: '4',
    name: 'Mr Président',
    identifier: 2,
    enabled: true,
    proof: true,
    readonly: false,
    accessContractHoldingIdentifier: 'AC-000001',
    accessContractLogbookIdentifier: 'AC-000002',
    ingestContractHoldingIdentifier: 'IC-000001',
    itemIngestContractIdentifier: 'IC-000001'
  },
];

describe('TenantService', () => {
  let httpTestingController: HttpTestingController;
  let tenantService: TenantService;

  beforeEach(() => {
    const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        TenantService,
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    tenantService = TestBed.inject(TenantService);
  });

  it('should be created', inject([TenantService], (service: TenantService) => {
    expect(service).toBeTruthy();
  }));

  it('should call /fake-api/tenants/42', () => {
    tenantService.get('42').subscribe((response) => expect(response).toEqual(expectedTenant), fail);
    const req = httpTestingController.expectOne('/fake-api/tenants/42');
    expect(req.request.method).toEqual('GET');
    req.flush(expectedTenant);
  });

  it('should call /fake-api/tenants with criteria', () => {
    tenantService.getTenantsByCustomerIds(['42']).subscribe((response) => expect(response).toEqual(expectedTenants), fail);

    const criterionArray: any[] = [{ key: 'customerId', value: ['42'], operator: Operators.in }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/tenants?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('GET');
    req.flush(expectedTenants);
  });

  it('should call /fake-api/tenants and display a success message', () => {
    const snackBar = TestBed.inject(VitamUISnackBar);
    tenantService.create(expectedTenant, expectedOwner.name).subscribe(
      (response: Tenant) => {
        expect(response).toEqual(expectedTenant);
        expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
        expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          data: { type: 'tenantCreate', tenantName: expectedTenant.name, ownerName: expectedOwner.name },
          duration: 10000
        });
      },
      fail
    );
    const req = httpTestingController.expectOne('/fake-api/tenants');
    expect(req.request.method).toEqual('POST');
    req.flush(expectedTenant);
  });

  it('should display an error message', () => {
    const snackBar = TestBed.inject(VitamUISnackBar);
    tenantService.create(expectedTenant, expectedOwner.name).subscribe(
      fail,
      () => {
        expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
        expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          data: { type: 'tenantCreateError' },
          duration: 10000
        });
      }
    );
    const req = httpTestingController.expectOne('/fake-api/tenants');
    expect(req.request.method).toEqual('POST');
    req.flush({ message: 'Expected message' }, { status: 400, statusText: 'Bad request' });
  });

  it('should return true if the tenant exists', () => {
    tenantService.exists('tenantName').subscribe(
      (found) => {
        expect(found).toBeTruthy();
      },
      fail
    );

    const criterionArray: any[] = [ { key: 'name', value: 'tenantName', operator: Operators.equals }];
    const query: SearchQuery = { criteria: criterionArray };
    const req = httpTestingController.expectOne('/fake-api/tenants/check?criteria=' + encodeURI(JSON.stringify(query)));
    expect(req.request.method).toEqual('HEAD');
    req.flush('');
  });

});
