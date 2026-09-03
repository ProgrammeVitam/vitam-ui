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

import { TranslateService } from '@ngx-translate/core';
import { ReclassificationService } from '../../../app/modules/services/reclassification.service';
import { Observable, of } from 'rxjs';
import { ArchiveUnit } from '../../../app/modules/archive-unit/models/archive-unit';
import { LoggerModule } from '../../../app/modules/logger/logger.module';
import { PagedResult } from '../../../app/modules/models/criteria/search-criteria.interface';
import { BaseReclassificationDialogService } from './reclassification-dialog.service';
import { VitamTenantConfigService } from '../../../app/modules/vitam-tenant-config.service';
import { tenantConfigServiceMock } from '../../../../testing/src/tenant-config.service.mock';

const fakeArchiveUnits = (count: number): ArchiveUnit[] => {
  return [...Array(count).keys()].map((n): ArchiveUnit => ({ '#id': `${n}`, '#unitups': [] }));
};

const waitForSignalEffects = () => new Promise((resolve) => setTimeout(resolve));

describe('ReclassificationDialogService', () => {
  let service: BaseReclassificationDialogService;
  let reclassificationServiceSpy: any;
  let translateServiceSpy: any;

  beforeEach(() => {
    reclassificationServiceSpy = {
      searchArchiveUnitsByCriteria: vi.fn().mockName('ReclassificationService.searchArchiveUnitsByCriteria'),
    };
    translateServiceSpy = {
      instant: vi.fn().mockName('TranslateService.instant'),
    };

    TestBed.configureTestingModule({
      imports: [LoggerModule.forRoot()],
      providers: [
        BaseReclassificationDialogService,
        { provide: ReclassificationService, useValue: reclassificationServiceSpy },
        { provide: TranslateService, useValue: translateServiceSpy },
        {
          provide: VitamTenantConfigService,
          useValue: tenantConfigServiceMock,
        },
      ],
    });

    service = TestBed.inject(BaseReclassificationDialogService);
  });

  it('should be created with default values', () => {
    expect(service).toBeTruthy();
    expect(service.transactionId()).toBeNull();
    expect(service.initialQuery()).toBeNull();
    expect(service.childrenCount()).toBe(0);
    expect(service.childrenCountLoaded()).toBe(false);
    expect(service.exactChildrenCountLoaded()).toBe(false);
    expect(service.parentCount()).toBe(0);
    expect(service.parentIds()).toEqual([]);
  });

  it('should compute shouldProposeExactChildrenCount correctly', () => {
    service.childrenCount.set(100000); // > RECLASSIFICATION_THRESHOLD
    service.exactChildrenCountLoaded.set(false);
    expect(service.shouldProposeExactChildrenCount()).toBe(true);

    service.exactChildrenCountLoaded.set(true);
    expect(service.shouldProposeExactChildrenCount()).toBe(false);
  });

  it('should return badgeMessage with "more than" when shouldProposeExactChildrenCount is true', () => {
    service.childrenCount.set(100000);
    service.exactChildrenCountLoaded.set(false);

    translateServiceSpy.instant.mockImplementation((key: string) => {
      return {
        'ARCHIVE_SEARCH.MORE_THAN': 'Plus de',
        'RECLASSIFICATION.FIRST_STEP.CHILDS': 'éléments',
      }[key];
    });

    const message = service.badgeMessage();
    expect(message).toBe('Plus de 100000 éléments');
  });

  it('should return badgeMessage with exact count otherwise', () => {
    service.childrenCount.set(42);
    service.exactChildrenCountLoaded.set(true);

    translateServiceSpy.instant.mockReturnValue('Inclut 42 documents/dossiers');

    const message = service.badgeMessage();
    expect(message).toBe('Inclut 42 documents/dossiers');
  });

  it('should load and update children count via triggerLoadChildrenCount', async () => {
    const mockTotal = 123;
    const mockArchiveUnits = fakeArchiveUnits(mockTotal);

    // Le mock sera appelé 2 fois : pour unitIdsFromResult$ puis pour childrenCount$
    reclassificationServiceSpy.searchArchiveUnitsByCriteria
      .mockReturnValueOnce(of({ results: mockArchiveUnits, totalResults: mockArchiveUnits.length, pageNumbers: 1 }))
      .mockReturnValueOnce(of({ results: [], totalResults: mockTotal, pageNumbers: 1 }));

    const mockQuery = {
      criteriaList: [
        {
          criteria: 'GUID',
          operator: '',
          category: 'Fields',
          values: [{ id: 'foo', value: 'foo' }],
          dataType: '',
        },
      ],
      pageNumber: 0,
      size: 0,
    };

    service.transactionId.set(null);
    service.initialQuery.set(mockQuery);

    // Force l'effet du computed à s'exécuter
    TestBed.tick();
    await waitForSignalEffects(); // Laisse le temps au toObservable de s'initialiser

    service.triggerLoadChildrenCount();

    await waitForSignalEffects(); // Laisse le temps aux observables de se résoudre

    expect(service.childrenCount()).toBe(mockTotal);
    expect(service.childrenCountLoaded()).toBe(true);
    expect(reclassificationServiceSpy.searchArchiveUnitsByCriteria).toHaveBeenCalledTimes(2);
  });

  it('should load and update exact children count via triggerLoadExactChildrenCount', async () => {
    const mockTotal = 456;
    const mockArchiveUnits = fakeArchiveUnits(mockTotal);

    // Le mock sera appelé 2 fois : pour unitIdsFromResult$ puis pour childrenCount$
    reclassificationServiceSpy.searchArchiveUnitsByCriteria
      .mockReturnValueOnce(of({ results: mockArchiveUnits, totalResults: mockArchiveUnits.length, pageNumbers: 1 }))
      .mockReturnValueOnce(of({ results: [], totalResults: mockTotal, pageNumbers: 1 }));

    const mockQuery = {
      criteriaList: [
        {
          criteria: 'GUID',
          operator: '',
          category: 'Fields',
          values: [{ id: 'foo', value: 'foo' }],
          dataType: '',
        },
      ],
      pageNumber: 0,
      size: 0,
    };

    service.transactionId.set(null);
    service.initialQuery.set(mockQuery);

    reclassificationServiceSpy.searchArchiveUnitsByCriteria.mockReturnValue(
      of({ results: [], totalResults: mockTotal }) as Observable<PagedResult>,
    );

    TestBed.tick();
    await waitForSignalEffects(); // Laisse le temps au toObservable de s'initialiser

    service.triggerLoadExactChildrenCount();
    await waitForSignalEffects();

    expect(service.childrenCount()).toBe(mockTotal);
    expect(service.exactChildrenCountLoaded()).toBe(true);
  });

  it('should load parent units and compute hasParent', async () => {
    const mockUnits = [{ '#unitups': ['parent1', 'parent2'] }];

    const mockParents = [{ '#id': 'parent1' }, { '#id': 'parent2' }];

    reclassificationServiceSpy.searchArchiveUnitsByCriteria
      .mockReturnValueOnce(of({ results: mockUnits }) as Observable<PagedResult>)
      .mockReturnValueOnce(
        // targetedUnits$
        of({ results: mockParents }) as Observable<PagedResult>,
      );

    TestBed.tick();
    await waitForSignalEffects(); // Laisse le temps au toObservable de s'initialiser

    const parents = service.parents();
    expect(parents.length).toBe(2);
    expect(parents).toEqual(mockParents);
    expect(service.hasParent()).toBe(true);
  });
});
