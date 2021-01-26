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
import { BASE_URL } from '../injection-tokens';
import { ProfileService } from './profile.service';

describe('ProfileService', () => {
  let httpTestingController: HttpTestingController;
  let rngProfileService: ProfileService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ProfileService,
        { provide: BASE_URL, useValue: '/fake-api' },
      ]
    });

    httpTestingController = TestBed.inject(HttpTestingController as Type<HttpTestingController>);
    rngProfileService = TestBed.inject(ProfileService);
  });

  it('should be created', inject([ProfileService], (service: ProfileService) => {
    expect(service).toBeTruthy();
  }));

  describe('list', () => {

    it('should call the API', () => {

      rngProfileService.list().subscribe((response) => expect(response).toEqual([]), fail);

      // tslint:disable-next-line:max-line-length
      const req = httpTestingController.expectOne('/fake-api/profiles?criteria=%7B%22criteria%22:%5B%7B%22key%22:%22enabled%22,%22value%22:true,%22operator%22:%22EQUALS%22%7D%5D%7D&embedded=ALL');
      expect(req.request.method).toEqual('GET');
      req.flush([]);

    });

  });
});
