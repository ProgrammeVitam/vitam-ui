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

import { ArchiveService } from './archive.service';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { BASE_URL, LoggerModule, Unit } from 'vitamui-library';
import { TranslateModule } from '@ngx-translate/core';
import { ArchiveApiService } from '../core/api/archive-api.service';
import createSpyObj = jasmine.createSpyObj;
import { of } from 'rxjs';
import { MatLegacySnackBarModule as MatSnackBarModule } from '@angular/material/legacy-snack-bar';

describe('ArchiveService', () => {
  let service: ArchiveService;
  const archiveApiService = createSpyObj('ArchiveApiService', ['searchArchiveUnitsByCriteria']);

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot(), TranslateModule.forRoot(), MatSnackBarModule],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
        provideHttpClient(withInterceptorsFromDi()),
        provideHttpClientTesting(),
        { provide: ArchiveApiService, useValue: archiveApiService },
      ],
    });
    service = TestBed.inject(ArchiveService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should build archive unit path', () => {
    const mockedUnits = [
      {
        '#id': 'aeaqaaaabieci5gnciz5kam2cby53jaaaaaq',
        Title: 'Justice',
        '#unitups': ['aeaqaaaabieci5gnciz5kam2cby53eyaaafa'],
        '#allunitups': ['aeaqaaaabieci5gnciz5kam2cby53eyaaafa'],
      },
      {
        '#id': 'aeaqaaaabiec7aytaxuy2am2cdqibnyaaafa',
        Title: 'Cabinet de Michel Mercier',
        '#unitups': ['aeaqaaaabieci5gnciz5kam2cby53jaaaabq'],
        '#allunitups': [
          'aeaqaaaabieci5gnciz5kam2cby53jaaaaaq',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaabq',
          'aeaqaaaabieci5gnciz5kam2cby53eyaaafa',
        ],
      },
      {
        '#id': 'aeaqaaaabieci5gnciz5kam2cdqpgiqaaapq',
        Title: 'Assemblée nationale',
        '#unitups': ['aeaqaaaabieci5gnciz5kam2cdqpgiiaaaca'],
        '#allunitups': [
          'aeaqaaaabieci5gnciz5kam2cdqpgiiaaaca',
          'aeaqaaaabiec7aytaxuy2am2cdqiboaaaadq',
          'aeaqaaaabiec7aytaxuy2am2cdqibnyaaafa',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaaaq',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaabq',
          'aeaqaaaabieci5gnciz5kam2cdqpftiaaabq',
          'aeaqaaaabiec7aytaxuy2am2cdqiboaaaaeq',
          'aeaqaaaabieci5gnciz5kam2cby53eyaaafa',
        ],
      },
      {
        '#id': 'aeaqaaaabieci5gnciz5kam2cdqpftiaaabq',
        Title: 'Discours et interventions de Michel Mercier, garde des sceaux de 2010 à 2012',
        '#unitups': ['aeaqaaaabiec7aytaxuy2am2cdqiboaaaaeq'],
        '#allunitups': [
          'aeaqaaaabiec7aytaxuy2am2cdqiboaaaadq',
          'aeaqaaaabiec7aytaxuy2am2cdqibnyaaafa',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaaaq',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaabq',
          'aeaqaaaabiec7aytaxuy2am2cdqiboaaaaeq',
          'aeaqaaaabieci5gnciz5kam2cby53eyaaafa',
        ],
      },
      {
        '#id': 'aeaqaaaabieci5gnciz5kam2cdqpgiiaaaca',
        Title: 'Discours prononcés devant le parlement',
        '#unitups': ['aeaqaaaabieci5gnciz5kam2cdqpftiaaabq'],
        '#allunitups': [
          'aeaqaaaabiec7aytaxuy2am2cdqiboaaaadq',
          'aeaqaaaabiec7aytaxuy2am2cdqibnyaaafa',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaaaq',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaabq',
          'aeaqaaaabieci5gnciz5kam2cdqpftiaaabq',
          'aeaqaaaabiec7aytaxuy2am2cdqiboaaaaeq',
          'aeaqaaaabieci5gnciz5kam2cby53eyaaafa',
        ],
      },
      {
        '#id': 'aeaqaaaabiec7aytaxuy2am2cdqiboaaaadq',
        Title: 'Communication',
        '#unitups': ['aeaqaaaabiec7aytaxuy2am2cdqibnyaaafa'],
        '#allunitups': [
          'aeaqaaaabiec7aytaxuy2am2cdqibnyaaafa',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaaaq',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaabq',
          'aeaqaaaabieci5gnciz5kam2cby53eyaaafa',
        ],
      },
      {
        '#id': 'aeaqaaaabiec7aytaxuy2am2cdqiboaaaaeq',
        Title: 'Discours du ministre',
        '#unitups': ['aeaqaaaabiec7aytaxuy2am2cdqiboaaaadq'],
        '#allunitups': [
          'aeaqaaaabiec7aytaxuy2am2cdqiboaaaadq',
          'aeaqaaaabiec7aytaxuy2am2cdqibnyaaafa',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaaaq',
          'aeaqaaaabieci5gnciz5kam2cby53jaaaabq',
          'aeaqaaaabieci5gnciz5kam2cby53eyaaafa',
        ],
      },
      {
        '#id': 'aeaqaaaabieci5gnciz5kam2cby53jaaaabq',
        Title: 'Cabinet du ministre',
        '#unitups': ['aeaqaaaabieci5gnciz5kam2cby53jaaaaaq'],
        '#allunitups': ['aeaqaaaabieci5gnciz5kam2cby53jaaaaaq', 'aeaqaaaabieci5gnciz5kam2cby53eyaaafa'],
      },
      {
        '#id': 'aeaqaaaabieci5gnciz5kam2cby53eyaaafa',
        Title: 'Archives postérieures à 1789',
        '#unitups': [],
        '#allunitups': [],
      },
    ] as unknown as Unit[];
    archiveApiService.searchArchiveUnitsByCriteria.and.returnValue(
      of({
        $hits: {
          limit: mockedUnits.length,
          offset: 0,
          total: mockedUnits.length,
          size: mockedUnits.length,
        },
        $results: mockedUnits,
      }),
    );
    service
      .buildArchiveUnitPath({
        '#allunitups': mockedUnits.map((u) => u['#id']),
      } as Unit)
      .subscribe(({ fullPath, resumePath }) => {
        expect(fullPath).toEqual(
          '/Archives postérieures à 1789/Justice/Cabinet du ministre/Cabinet de Michel Mercier/Communication/Discours du ministre/Discours et interventions de Michel Mercier, garde des sceaux de 2010 à 2012/Discours prononcés devant le parlement/Assemblée nationale',
        );
        expect(resumePath).toEqual(
          '/Archives postérieures à 1789/Justice/Cabinet du ministre/.../Discours et interventions de Michel Mercier, garde des sceaux de 2010 à 2012/Discours prononcés devant le parlement/Assemblée nationale',
        );
      });
  });
});
