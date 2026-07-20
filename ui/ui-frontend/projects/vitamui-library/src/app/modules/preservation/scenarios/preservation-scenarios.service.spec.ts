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
import { firstValueFrom, of } from 'rxjs';
import { vi } from 'vitest';

import { PreservationScenariosService } from './preservation-scenarios.service';
import { PreservationScenariosApiService } from './preservation-scenarios-api.service';
import { PreservationScenario } from './preservation-scenario.type';

describe('PreservationScenariosService', () => {
  let service: PreservationScenariosService;
  let apiSpy: {
    getAll: ReturnType<typeof vi.fn>;
    put: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };

  const mockScenario = {
    '#id': '1',
    '#tenant': 1,
    '#version': 1,
    Identifier: 'ID-123',
    Name: 'Scenario 1',
    Description: 'Description 1',
    CreationDate: new Date(),
    LastUpdate: new Date(),
    ActionList: [],
    GriffinByFormat: [],
    DefaultGriffin: {
      GriffinIdentifier: 'G1',
      Timeout: 100,
      MaxSize: 100,
      Debug: false,
      ActionDetail: [],
    },
    TransformationRules: '',
  } as PreservationScenario;

  beforeEach(() => {
    apiSpy = {
      getAll: vi.fn(),
      put: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        PreservationScenariosService,
        {
          provide: PreservationScenariosApiService,
          useValue: apiSpy,
        },
      ],
    });

    service = TestBed.inject(PreservationScenariosService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('list', () => {
    it('should call api.getAll()', async () => {
      apiSpy.getAll.mockReturnValue(of([mockScenario]));

      const scenarios = await firstValueFrom(service.list());

      expect(scenarios).toHaveLength(1);
      expect(apiSpy.getAll).toHaveBeenCalledTimes(1);
    });
  });

  describe('put', () => {
    it('should call api.put()', async () => {
      const scenarios = [mockScenario];

      apiSpy.put.mockReturnValue(of(undefined));

      await firstValueFrom(service.put(scenarios));

      expect(apiSpy.put).toHaveBeenCalledWith(scenarios);
    });
  });

  describe('create', () => {
    it('should call api.create()', async () => {
      const newScenario = {
        ...mockScenario,
        Identifier: 'NEW',
      } as PreservationScenario;

      apiSpy.create.mockReturnValue(of(mockScenario));

      const result = await firstValueFrom(service.create(newScenario));

      expect(apiSpy.create).toHaveBeenCalledWith(newScenario);
      expect(result.Identifier).toBe('ID-123');
    });
  });

  describe('update', () => {
    it('should call api.update()', async () => {
      apiSpy.update.mockReturnValue(of(mockScenario));

      await firstValueFrom(service.update(mockScenario));

      expect(apiSpy.update).toHaveBeenCalledWith(mockScenario);
    });
  });

  describe('delete', () => {
    it('should call api.delete()', async () => {
      apiSpy.delete.mockReturnValue(of(undefined));

      await firstValueFrom(service.delete(mockScenario));

      expect(apiSpy.delete).toHaveBeenCalledWith(mockScenario);
    });
  });
});
