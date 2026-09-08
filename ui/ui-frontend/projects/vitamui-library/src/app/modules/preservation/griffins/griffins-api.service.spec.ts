/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { GriffinsApiService } from './griffins-api.service';
import { CreateGriffin, Griffin } from './griffin.type';

describe('GriffinsApiService', () => {
  let service: GriffinsApiService;
  let httpMock: HttpTestingController;
  const baseUrl = '/fake-api';

  const mockGriffin: Griffin = {
    '#id': '1',
    '#tenant': 1,
    '#version': 1,
    Identifier: 'ID-G1',
    Name: 'Griffin 1',
    Description: 'Desc 1',
    ExecutableName: 'exec1',
    ExecutableVersion: '1.0',
    CreationDate: new Date(),
    LastUpdate: new Date(),
  } as any;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [GriffinsApiService],
    });
    service = TestBed.inject(GriffinsApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAll', () => {
    it('should return an array of Griffins', () => {
      service.getAll().subscribe((griffins) => {
        expect(griffins.length).toBe(1);
        expect(griffins[0].Identifier).toBe('ID-G1');
      });

      const req = httpMock.expectOne(`${baseUrl}/griffins`);
      expect(req.request.method).toBe('GET');
      req.flush([mockGriffin]);
    });
  });

  describe('put', () => {
    it('should send a PUT request with the griffins array', () => {
      const griffins = [mockGriffin];
      service.put(griffins).subscribe(() => {});

      const req = httpMock.expectOne(`${baseUrl}/griffins`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(griffins);
      req.flush(null);
    });
  });

  describe('create', () => {
    it('should send a POST request with the new griffin', () => {
      const newGriffin: CreateGriffin = { ...mockGriffin, Identifier: 'NEW' } as any;
      service.create(newGriffin).subscribe((res) => {
        expect(res.Identifier).toBe('NEW');
      });

      const req = httpMock.expectOne(`${baseUrl}/griffins`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newGriffin);
      req.flush(newGriffin);
    });
  });

  describe('update', () => {
    it('should send a POST request for update', () => {
      service.update(mockGriffin).subscribe((res) => {
        expect(res).toBeTruthy();
      });

      const req = httpMock.expectOne(`${baseUrl}/griffins`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockGriffin);
      req.flush(mockGriffin);
    });
  });

  describe('delete', () => {
    it('should send a DELETE request with the griffin in body', () => {
      service.delete(mockGriffin).subscribe(() => {});

      const req = httpMock.expectOne(`${baseUrl + '/griffins'}`);
      expect(req.request.method).toBe('DELETE');
      expect(req.request.body).toEqual(mockGriffin);
      req.flush(null);
    });
  });
});
