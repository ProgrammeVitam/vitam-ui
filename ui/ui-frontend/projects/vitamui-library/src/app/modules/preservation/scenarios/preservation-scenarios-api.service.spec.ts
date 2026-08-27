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
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';

import { PreservationScenariosApiService } from './preservation-scenarios-api.service';
import { BASE_URL } from '../../injection-tokens';
import { PreservationScenario } from './preservation-scenario.type';

describe('PreservationScenariosApiService', () => {
  let service: PreservationScenariosApiService;
  let httpMock: HttpTestingController;

  const baseUrl = 'http://localhost:8080';
  const endpoint = `${baseUrl}/preservation-scenarios`;

  const mockScenario: PreservationScenario = {
    Identifier: 'ID-123',
    Name: 'Scenario 1',
    Description: 'Description 1',
    CreationDate: new Date(),
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
    TestBed.configureTestingModule({
      providers: [
        PreservationScenariosApiService,
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: BASE_URL, useValue: baseUrl },
      ],
    });

    service = TestBed.inject(PreservationScenariosApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getAll should return scenarios', () => {
    service.getAll().subscribe((scenarios) => {
      expect(scenarios).toHaveLength(1);
      expect(scenarios[0].Identifier).toBe('ID-123');
    });

    const req = httpMock.expectOne(endpoint);
    expect(req.request.method).toBe('GET');

    req.flush([mockScenario]);
  });

  it('put should send PUT request', () => {
    const scenarios = [mockScenario];

    service.put(scenarios).subscribe();

    const req = httpMock.expectOne(endpoint);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(scenarios);

    req.flush(null);
  });

  it('create should send POST request', () => {
    const newScenario: PreservationScenario = {
      ...mockScenario,
      Identifier: 'NEW',
    };

    service.create(newScenario).subscribe((res) => {
      expect(res.Identifier).toBe('NEW');
    });

    const req = httpMock.expectOne(endpoint);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(newScenario);

    req.flush(newScenario);
  });

  it('update should send POST request', () => {
    service.update(mockScenario).subscribe((res) => {
      expect(res).toEqual(mockScenario);
    });

    const req = httpMock.expectOne(endpoint);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockScenario);

    req.flush(mockScenario);
  });

  it('delete should send DELETE request', () => {
    service.delete(mockScenario).subscribe();

    const req = httpMock.expectOne(endpoint);
    expect(req.request.method).toBe('DELETE');
    expect(req.request.body).toEqual(mockScenario);

    req.flush(null);
  });
});
