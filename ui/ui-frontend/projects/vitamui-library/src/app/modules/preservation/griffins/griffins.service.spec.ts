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
import { GriffinsService } from './griffins.service';
import { GriffinsApiService } from './griffins-api.service';
import { of } from 'rxjs';
import { CreateGriffin, Griffin } from './griffin.type';
import { vi } from 'vitest';
import { BASE_URL } from '../../injection-tokens';
import { LoggerModule } from '../../logger/logger.module';

describe('GriffinsService', () => {
  let service: GriffinsService;
  let apiSpy: any;

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
    const spy = {
      getAll: vi.fn(),
      put: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
    };

    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot()],
      providers: [GriffinsService, { provide: GriffinsApiService, useValue: spy }, { provide: BASE_URL, useValue: '/fake-api' }],
    });

    service = TestBed.inject(GriffinsService);
    apiSpy = TestBed.inject(GriffinsApiService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('list', () => {
    it('should call api.getAll()', () => {
      apiSpy.getAll.mockReturnValue(of([mockGriffin]));
      service.list().subscribe((griffins) => {
        expect(griffins.length).toBe(1);
        expect(apiSpy.getAll).toHaveBeenCalled();
      });
    });
  });

  describe('put', () => {
    it('should call api.put()', () => {
      const griffins = [mockGriffin];
      apiSpy.put.mockReturnValue(of(undefined));
      service.put(griffins).subscribe(() => {
        expect(apiSpy.put).toHaveBeenCalledWith(griffins);
      });
    });
  });

  describe('create', () => {
    it('should call api.create()', () => {
      const newGriffin: CreateGriffin = { ...mockGriffin, Identifier: 'NEW' } as any;
      apiSpy.create.mockReturnValue(of(mockGriffin));
      service.create(newGriffin).subscribe((res) => {
        expect(apiSpy.create).toHaveBeenCalledWith(newGriffin);
        expect(res.Identifier).toBe('ID-G1');
      });
    });
  });

  describe('update', () => {
    it('should call api.update()', () => {
      apiSpy.update.mockReturnValue(of(mockGriffin));
      service.update(mockGriffin).subscribe(() => {
        expect(apiSpy.update).toHaveBeenCalledWith(mockGriffin);
      });
    });
  });

  describe('delete', () => {
    it('should call api.delete()', () => {
      apiSpy.delete.mockReturnValue(of(undefined));
      service.delete(mockGriffin).subscribe(() => {
        expect(apiSpy.delete).toHaveBeenCalledWith(mockGriffin);
      });
    });
  });
});
