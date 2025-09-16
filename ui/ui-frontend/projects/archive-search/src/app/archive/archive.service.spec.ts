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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import createSpyObj = jasmine.createSpyObj;
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';
import { BASE_URL, LoggerModule, Unit } from 'ui-frontend-common';
import { ArchiveApiService } from '../core/api/archive-api.service';
import { ArchiveService } from './archive.service';

describe('ArchiveService', () => {
  let service: ArchiveService;
  const archiveApiService = createSpyObj('ArchiveApiService', ['searchArchiveUnitsByCriteria']);

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot(), TranslateModule.forRoot(), HttpClientTestingModule, MatSnackBarModule],
      providers: [
        { provide: BASE_URL, useValue: '/fake-api' },
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
        '#id': 'aeaqaaaaaeeioj2zae62wamzgjq5saaaaaac',
        Title: 'Sous dossier 2',
        '#unitups': ['aeaqaaaaaeeioj2zae62wamzgjq5saaaaaab'],
      },
      {
        '#id': 'aeaqaaaaaeeioj2zae62wamzgjq5saaaaaab',
        Title: 'Sous dossier 1',
        '#unitups': ['aeaqaaaaaeeioj2zae62wamzgjq5saaaaaaa'],
      },
      {
        '#id': 'aeaqaaaaaeeioj2zae62wamzgjq5saaaaaaa',
        Title: 'Dossier 1',
        '#unitups': ['aeaqaaaaaeeioj2zae62wamzgjq5s7iaaaca'],
      },
      {
        '#id': 'aeaqaaaaaeeioj2zae62wamzgjq5s7iaaaca',
        Title: 'Supports de présentation',
        '#unitups': ['aeaqaaaaaeeioj2zae62wamzgjq5tcaaaaba'],
      },
      {
        '#id': 'aeaqaaaaaeeioj2zae62wamzgjq5tcaaaaba',
        Title: 'Multilatérales',
        '#unitups': ['aeaqaaaaaeeioj2zae62wamzgjq5tciaaaba'],
      },
      {
        '#id': 'aeaqaaaaaeeioj2zae62wamzgjq5tciaaaba',
        Title: 'VaS',
        '#unitups': ['aeaqaaaaaeeioj2zae62wamzgjq5tciaaaca'],
      },
      {
        '#id': 'aeaqaaaaaeeioj2zae62wamzgjq5tciaaaca',
        Title_: {
          fr: 'VITAM',
        },
        '#unitups': [],
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
        expect(fullPath).toEqual('/VITAM/VaS/Multilatérales/Supports de présentation/Dossier 1/Sous dossier 1/Sous dossier 2');
        expect(resumePath).toEqual('/VITAM/VaS/Multilatérales/.../Dossier 1/Sous dossier 1/Sous dossier 2');
      });
  });
});
