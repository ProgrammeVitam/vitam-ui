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
import { ENVIRONMENT } from 'ui-frontend-common';
import { BASE_URL, IdentityProvider, LoggerModule, Operators, SearchQuery, WINDOW_LOCATION } from 'ui-frontend-common';
import { environment } from './../../../../environments/environment';

import { Type } from '@angular/core';
import { VitamUISnackBar, VitamUISnackBarComponent } from '../../../shared/vitamui-snack-bar';
import { IdentityProviderService } from './identity-provider.service';

describe('IdentityProviderService', () => {
  let httpTestingController: HttpTestingController;
  let identityProviderService: IdentityProviderService;
  let identityProviders: any[];
  let keystore: File;
  let idpMetadata: File;

  beforeEach(() => {
    keystore = new File(['keystore content'], 'test.jks');
    idpMetadata = new File(['metadata content'], 'test.jks');
    identityProviders = [
      {
        id: '42',
        customerId: '1234',
        name: 'Test IDP',
        internal: true,
        keystorePassword: 'testpassword1234',
        patterns: ['test.com', 'test.fr'],
        enabled: true,
        keystore,
        idpMetadata
      },
    ];
    const snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open', 'openFromComponent']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerModule.forRoot()],
      providers: [
        IdentityProviderService,
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
        { provide: WINDOW_LOCATION, useValue: {} },
        { provide: ENVIRONMENT, useValue: environment }
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    identityProviderService = TestBed.inject(IdentityProviderService);
  });

  it('should be created', inject([IdentityProviderService], (service: IdentityProviderService) => {
    expect(service).toBeTruthy();
  }));

  describe('create', () => {

    it('should call /fake-api/providers and display a succes message', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      identityProviderService.create(identityProviders[0]).subscribe(
        (response: IdentityProvider) => {
          expect(response).toEqual(identityProviders[0]);
          expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
          expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'providerCreate', name: identityProviders[0].name },
            duration: 10000
          });
        },
        fail
      );
      const req = httpTestingController.expectOne('/fake-api/providers');
      expect(req.request.method).toEqual('POST');
      req.flush(identityProviders[0]);
    });

    it('should display an error message', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      identityProviderService.create(identityProviders[0]).subscribe(
        fail,
        () => {
          expect(snackBar.open).toHaveBeenCalledTimes(1);
          expect(snackBar.open).toHaveBeenCalledWith('Expected message', null, { panelClass: 'vitamui-snack-bar', duration: 10000 });
        }
      );
      const req = httpTestingController.expectOne('/fake-api/providers');
      expect(req.request.method).toEqual('POST');
      req.flush({ message: 'Expected message' }, { status: 400, statusText: 'Bad request' });
    });

  });

  describe('getAll', () => {

    it('should call /fake-api/providers?criteria...', () => {
      identityProviderService.getAll('4242').subscribe(
        (result: IdentityProvider[]) => {
          expect(result).toEqual(identityProviders);
        },
        fail
      );
      const criterionArray: any[] = [{ key: 'customerId', value: '4242', operator: Operators.equals }];
      const query: SearchQuery = { criteria: criterionArray };
      const req = httpTestingController.expectOne('/fake-api/providers?criteria=' + encodeURI(JSON.stringify(query)));
      expect(req.request.method).toEqual('GET');
      req.flush(identityProviders);
    });

  });

  describe('update', () => {

    it('should call PATCH /fake-api/providers/42', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      identityProviderService.updated.subscribe((provider: IdentityProvider) => expect(provider).toEqual(identityProviders[0]), fail);
      identityProviderService.patch(identityProviders[0]).subscribe(
        (provider: IdentityProvider) => {
          expect(provider).toEqual(identityProviders[0]);
          expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
          expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'providerUpdate', name: identityProviders[0].name },
            duration: 10000
          });
        },
        fail
      );
      const req = httpTestingController.expectOne('/fake-api/providers/42');
      expect(req.request.method).toEqual('PATCH');
      expect(req.request.body).toEqual(identityProviders[0]);
      req.flush(identityProviders[0]);
    });

    it('should display an error message', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      identityProviderService.patch(identityProviders[0]).subscribe(
        fail,
        () => {
          expect(snackBar.open).toHaveBeenCalledTimes(1);
          expect(snackBar.open).toHaveBeenCalledWith('Expected message', null, { panelClass: 'vitamui-snack-bar', duration: 10000 });
        }
      );
      const req = httpTestingController.expectOne('/fake-api/providers/42');
      expect(req.request.method).toEqual('PATCH');
      req.flush({ message: 'Expected message' }, { status: 400, statusText: 'Bad request' });
    });

  });

  describe('updateMetadataFile', () => {

    it('should call PATCH /fake-api/providers/42/idpMetadata', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      const expectedFile = new File([''], 'metadata.xml');
      identityProviderService.updated.subscribe((provider: IdentityProvider) => expect(provider).toEqual(identityProviders[0]), fail);
      identityProviderService.updateMetadataFile('42', expectedFile).subscribe(
        (provider: IdentityProvider) => {
          expect(provider).toEqual(identityProviders[0]);
          expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
          expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'providerUpdate', name: identityProviders[0].name },
            duration: 10000
          });
        },
        fail
      );
      const req = httpTestingController.expectOne('/fake-api/providers/42/idpMetadata');
      expect(req.request.method).toEqual('PATCH');
      const formData = new FormData();
      formData.append('idpMetadata', expectedFile, expectedFile.name);
      formData.append('provider', JSON.stringify({ id: identityProviders[0].id }));
      expect(req.request.body).toEqual(formData);
      req.flush(identityProviders[0]);
    });

    it('should display an error message', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      identityProviderService.updateMetadataFile('42', new File([''], 'metadata.xml')).subscribe(
        fail,
        () => {
          expect(snackBar.open).toHaveBeenCalledTimes(1);
          expect(snackBar.open).toHaveBeenCalledWith('Expected message', null, { panelClass: 'vitamui-snack-bar', duration: 10000 });
        }
      );
      const req = httpTestingController.expectOne('/fake-api/providers/42/idpMetadata');
      expect(req.request.method).toEqual('PATCH');
      req.flush({ message: 'Expected message' }, { status: 400, statusText: 'Bad request' });
    });

  });

  describe('updateKeystore', () => {

    it('should call PATCH /fake-api/providers/42/keystore', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      const expectedFile = new File([''], 'keystore.jks');
      identityProviderService.updated.subscribe((provider: IdentityProvider) => expect(provider).toEqual(identityProviders[0]), fail);
      identityProviderService.updateKeystore('42', expectedFile, 'password').subscribe(
        (provider: IdentityProvider) => {
          expect(provider).toEqual(identityProviders[0]);
          expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
          expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            data: { type: 'providerUpdate', name: identityProviders[0].name },
            duration: 10000
          });
        },
        fail
      );
      const req = httpTestingController.expectOne('/fake-api/providers/42/keystore');
      expect(req.request.method).toEqual('PATCH');
      const formData = new FormData();
      formData.append('idpMetadata', expectedFile, expectedFile.name);
      formData.append('provider', JSON.stringify({ id: identityProviders[0].id, keystorePassword: 'password' }));
      expect(req.request.body).toEqual(formData);
      req.flush(identityProviders[0]);
    });

    it('should display an error message', () => {
      const snackBar = TestBed.inject(VitamUISnackBar);
      identityProviderService.updateKeystore('42', new File([''], 'keystore.jks'), 'password').subscribe(
        fail,
        () => {
          expect(snackBar.open).toHaveBeenCalledTimes(1);
          expect(snackBar.open).toHaveBeenCalledWith('Expected message', null, { panelClass: 'vitamui-snack-bar', duration: 10000 });
        }
      );
      const req = httpTestingController.expectOne('/fake-api/providers/42/keystore');
      expect(req.request.method).toEqual('PATCH');
      req.flush({ message: 'Expected message' }, { status: 400, statusText: 'Bad request' });
    });

  });
});
