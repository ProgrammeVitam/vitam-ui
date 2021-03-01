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

import { inject, TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of } from 'rxjs';

import { Tenant } from 'ui-frontend-common';
import { TenantResolver } from './tenant-resolver.service';
import { TenantService } from './tenant.service';

const expectedTenant: Tenant = {
  id: '5ad5f14c894e6a414edc7b61adad48f0b8124fcda07b0ec1886c8d5d61c8f713',
  customerId: '42',
  ownerId: '5ad5f14c894e6a414edc7b62',
  name: 'Emmanuel Deviller',
  identifier: 7,
  enabled: true,
  proof: true,
  accessContractHoldingIdentifier: 'AC-000001',
  readonly : false,
  accessContractLogbookIdentifier: 'AC-000002',
  ingestContractHoldingIdentifier: 'IC-000001',
  itemIngestContractIdentifier: 'IC-000001'
};

describe('TenantResolver', () => {
  let tenantResolver: TenantResolver;

  beforeEach(() => {
    const tenantServiceSpy = jasmine.createSpyObj('TenantService', ['get']);
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        TenantResolver,
        { provide: TenantService, useValue: tenantServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
    });

    tenantResolver = TestBed.inject(TenantResolver);
  });

  it('should be created', inject([TenantResolver], (service: TenantResolver) => {
    expect(service).toBeTruthy();
  }));

  it('should get the tenant with the id', () => {
    const route = new ActivatedRouteSnapshot();
    spyOn(route.paramMap, 'get').and.returnValue('42');

    const tenantService = TestBed.inject(TenantService);
    tenantService.get = jasmine.createSpy().and.returnValue(of(expectedTenant));
    tenantResolver.resolve(route).subscribe((customer) => {
      expect(customer).toEqual(expectedTenant);
    });

    expect(route.paramMap.get).toHaveBeenCalledWith('id');
    expect(tenantService.get).toHaveBeenCalledWith('42');
  });

  it('should redirect to / if an error occurs', () => {
    const route = new ActivatedRouteSnapshot();
    spyOn(route.paramMap, 'get').and.returnValue('42');
    const tenantService = TestBed.inject(TenantService);
    tenantService.get = jasmine.createSpy().and.returnValue(of(null));
    const router = TestBed.inject(Router);
    tenantResolver.resolve(route).subscribe(() => {
      expect(router.navigate).toHaveBeenCalledWith(['/']);
    });
    expect(route.paramMap.get).toHaveBeenCalledWith('id');
    expect(tenantService.get).toHaveBeenCalledWith('42');
  });
});
