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
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { Type } from '@angular/core';
import { BASE_URL, Owner } from 'ui-frontend-common';
import { VitamUISnackBar, VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';
import { OwnerService } from './owner.service';

const expectedOwner: Owner = {
  id: '42',
  identifier : '42',
  customerId: '43',
  name: 'Julien Cornille',
  code: '10234665',
  companyName: 'vitamui',
  address: {
    street: '73 rue du Faubourg Poissonnière ',
    zipCode: '75009',
    city: 'Paris',
    country: 'France'
  },
  readonly : false
};

describe('OwnerService', () => {
  let httpTestingController: HttpTestingController;
  let ownerService: OwnerService;

  beforeEach(() => {
    const snackBarSpy = jasmine.createSpyObj('VitamUISnackBar', ['open', 'openFromComponent']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, NoopAnimationsModule],
      providers: [
        OwnerService,
        { provide: VitamUISnackBar, useValue: snackBarSpy },
        { provide: BASE_URL, useValue: '/fake-api' },
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    ownerService = TestBed.inject(OwnerService);
  });

  it('should be created', inject([OwnerService], (service: OwnerService) => {
    expect(service).toBeTruthy();
  }));

  it('should call /fake-api/owners/42', () => {
    ownerService.get('42').subscribe((response) => expect(response).toEqual(expectedOwner), fail);
    const req = httpTestingController.expectOne('/fake-api/owners/42');
    expect(req.request.method).toEqual('GET');
    req.flush(expectedOwner);
  });

  it('should call /fake-api/owners and display a success message', () => {
    const snackBar = TestBed.inject(VitamUISnackBar);
    ownerService.create(expectedOwner).subscribe(
      (response: Owner) => {
        expect(response).toEqual(expectedOwner);
        expect(snackBar.openFromComponent).toHaveBeenCalledTimes(1);
        expect(snackBar.openFromComponent).toHaveBeenCalledWith(VitamUISnackBarComponent, {
          panelClass: 'vitamui-snack-bar',
          data: { type: 'ownerCreate', name: expectedOwner.name },
          duration: 10000
        });
      },
      fail
    );
    const req = httpTestingController.expectOne('/fake-api/owners');
    expect(req.request.method).toEqual('POST');
    req.flush(expectedOwner);
  });

  it('should display an error message', () => {
    const snackBar = TestBed.inject(VitamUISnackBar);
    ownerService.create(expectedOwner).subscribe(
      fail,
      () => {
        expect(snackBar.open).toHaveBeenCalledTimes(1);
        expect(snackBar.open).toHaveBeenCalledWith('Expected message', null, { panelClass: 'vitamui-snack-bar', duration: 10000 });
      }
    );
    const req = httpTestingController.expectOne('/fake-api/owners');
    expect(req.request.method).toEqual('POST');
    req.flush({ message: 'Expected message' }, {status: 400, statusText: 'Bad request'});
  });
});
