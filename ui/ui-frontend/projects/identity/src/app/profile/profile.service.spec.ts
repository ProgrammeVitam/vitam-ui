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
import { inject, TestBed } from '@angular/core/testing';

import { Type } from '@angular/core';
import { BASE_URL, Profile } from 'ui-frontend-common';
import { VitamUISnackBar, VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';
import { ProfileService } from './profile.service';

describe('ProfileService', () => {
  let httpTestingController: HttpTestingController;
  let rngProfileService: ProfileService;

  beforeEach(() => {
    const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ProfileService,
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    rngProfileService = TestBed.inject(ProfileService);
  });

  it('should be created', inject([ProfileService], (service: ProfileService) => {
    expect(service).toBeTruthy();
  }));

  describe('get', () => {
    it('should call /fake-api/profiles/42', () => {
      const expectedProfile: Profile = {
          id: '42',
          name: 'Profile Name',
          description: 'Profile Description',
          applicationName: 'USERS_APP',
          level : '',
          customerId: 'customerId',
          groupsCount : 1,
          enabled: true,
          usersCount: 0,
          tenantName: 'tenant name',
          tenantIdentifier: 420,
          roles: [],
          readonly : false,
          externalParamId : null
      };
      rngProfileService.get('42').subscribe((profile) => expect(profile).toEqual(expectedProfile), fail);
      const req = httpTestingController.expectOne('/fake-api/profiles/42?embedded=ALL');
      expect(req.request.method).toEqual('GET');
      req.flush(expectedProfile);
    });
  });

  describe('patch', () => {
    it('should call PATCH /fake-api/profiles/42', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      const expectedRequest = {
        id: '42',
        name: 'Profile Group Name',
      };
      const expectedResponse: Profile = {
          id: '42',
          name: 'Profile Group Name',
          description: 'Profile Group Description',
          level : '',
          customerId: 'customerId',
          groupsCount : 1,
          applicationName: 'USERS_APP',
          enabled: true,
          usersCount: 42,
          tenantName: 'tenant name',
          tenantIdentifier: 420,
          roles: [],
          readonly : false,
          externalParamId : null
      };
      rngProfileService.updated.subscribe((profileGroup) => expect(profileGroup).toEqual(expectedResponse), fail);
      rngProfileService.patch(expectedRequest).subscribe(
        (profileGroup) => {
          expect(profileGroup).toEqual(expectedResponse);
          expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
          expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'profileUpdate', name: expectedResponse.name },
            duration: 10000
          });
        },
        fail
      );
      const req = httpTestingController.expectOne('/fake-api/profiles/42');
      expect(req.request.method).toEqual('PATCH');
      expect(req.request.body).toEqual(expectedRequest);
      req.flush(expectedResponse);
    });

    it('should display an error message', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      const expectedRequest = {
        id: '42',
        name: 'Profile Group Name',
      };
      rngProfileService.patch(expectedRequest).subscribe(
        fail,
        () => {
          expect(snackBar.open).toHaveBeenCalledTimes(1);
          expect(snackBar.open).toHaveBeenCalledWith('Expected message', null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000
          });
        }
      );
      const req = httpTestingController.expectOne('/fake-api/profiles/42');
      expect(req.request.method).toEqual('PATCH');
      req.flush({ message: 'Expected message' }, {status: 400, statusText: 'Bad request'});
    });
  });
});
