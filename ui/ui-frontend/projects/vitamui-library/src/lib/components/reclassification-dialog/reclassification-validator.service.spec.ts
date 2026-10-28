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
import { Injector, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { form, SchemaFn } from '@angular/forms/signals';
import { of } from 'rxjs';
import { CriteriaDataType, CriteriaOperator } from '../../../app/modules/models/criteria/criteria.enums';
import { SearchCriteriaDto } from '../../../app/modules/models/criteria/search-criteria.interface';
import { ReclassificationService } from '../../../app/modules/services/reclassification.service';
import { ReclassificationValidatorService } from './reclassification-validator.service';

describe('ReclassificationValidatorService', () => {
  let service: ReclassificationValidatorService;
  let reclassificationServiceMock: { searchArchiveUnitsByCriteria: ReturnType<typeof vi.fn> };

  const criteriaDto: SearchCriteriaDto = { pageNumber: 2, size: 5, criteriaList: [] };

  const lastEmittedTitle = () => {
    let title: string;
    service
      .getArchiveUnitTitle()
      .subscribe((value) => (title = value))
      .unsubscribe();
    return title;
  };

  /** Builds a signal form on a single `guid` field and returns its error kinds getter. */
  const createForm = (guid: string | null, schema: SchemaFn<{ guid: string | null }>) => {
    const testForm = form(signal({ guid }), schema, { injector: TestBed.inject(Injector) });
    TestBed.tick();
    return () =>
      testForm
        .guid()
        .errors()
        .map((error) => error.kind);
  };

  beforeEach(() => {
    reclassificationServiceMock = { searchArchiveUnitsByCriteria: vi.fn() };
    TestBed.configureTestingModule({
      providers: [{ provide: ReclassificationService, useValue: reclassificationServiceMock }],
    });
    service = TestBed.inject(ReclassificationValidatorService);
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  describe('archive unit title', () => {
    it('should start with an empty title and replay the last emitted one', () => {
      expect(lastEmittedTitle()).toEqual('');

      service.emitArchiveUnitTitle('first');
      service.emitArchiveUnitTitle('second');

      expect(lastEmittedTitle()).toEqual('second');
    });
  });

  describe('isAlreadyExistingParentValue', () => {
    it('should be true when the id is one of the parents', () => {
      expect(service.isAlreadyExistingParentValue('b', ['a', 'b', 'c'])).toBe(true);
    });

    it('should be false when the id is not one of the parents', () => {
      expect(service.isAlreadyExistingParentValue('z', ['a', 'b'])).toBe(false);
      expect(service.isAlreadyExistingParentValue('a', [])).toBe(false);
    });
  });

  describe('alreadyExistParents', () => {
    it('should report an error after the debounce time when the value is already a parent', async () => {
      const errors = createForm('parent-2', (path) => service.alreadyExistParents(path.guid, ['parent-1', 'parent-2']));

      await vi.advanceTimersByTimeAsync(service.debounceTime - 1);
      expect(errors()).toEqual([]);

      await vi.advanceTimersByTimeAsync(1);
      expect(errors()).toEqual(['alreadyExistParents']);
    });

    it('should be valid when the value is not a parent', async () => {
      const errors = createForm('other', (path) => service.alreadyExistParents(path.guid, ['parent-1']));

      await vi.advanceTimersByTimeAsync(service.debounceTime);

      expect(errors()).toEqual([]);
    });

    it('should be valid when the value is the code to ignore, even if it is a parent', async () => {
      const errors = createForm('parent-1', (path) => service.alreadyExistParents(path.guid, ['parent-1'], 'parent-1'));

      await vi.advanceTimersByTimeAsync(service.debounceTime);

      expect(errors()).toEqual([]);
    });

    it('should honour a custom debounce time', async () => {
      service.debounceTime = 50;
      const errors = createForm('parent-1', (path) => service.alreadyExistParents(path.guid, ['parent-1']));

      await vi.advanceTimersByTimeAsync(50);

      expect(errors()).toEqual(['alreadyExistParents']);
    });
  });

  describe('existArchiveUnit', () => {
    it('should search the unit by GUID with the paging of the given criteria after the debounce time', async () => {
      reclassificationServiceMock.searchArchiveUnitsByCriteria.mockReturnValue(of({ totalResults: 1, results: [{ Title: 'My unit' }] }));

      createForm('guid-1', (path) => service.existArchiveUnit(path.guid, () => criteriaDto));
      await vi.advanceTimersByTimeAsync(service.debounceTime - 1);
      expect(reclassificationServiceMock.searchArchiveUnitsByCriteria).not.toHaveBeenCalled();
      await vi.advanceTimersByTimeAsync(1);

      expect(reclassificationServiceMock.searchArchiveUnitsByCriteria).toHaveBeenCalledWith({
        pageNumber: 2,
        size: 5,
        criteriaList: [
          {
            criteria: 'GUID',
            operator: CriteriaOperator.EQ,
            category: 'FIELDS',
            dataType: CriteriaDataType.STRING,
            values: [{ value: 'guid-1', id: 'guid-1' }],
          },
        ],
      });
    });

    it('should read the criteria at validation time', async () => {
      reclassificationServiceMock.searchArchiveUnitsByCriteria.mockReturnValue(of({ totalResults: 1, results: [{}] }));
      let currentCriteria: SearchCriteriaDto = criteriaDto;

      createForm('guid-1', (path) => service.existArchiveUnit(path.guid, () => currentCriteria));
      currentCriteria = { pageNumber: 7, size: 20, criteriaList: [] };
      await vi.advanceTimersByTimeAsync(service.debounceTime);

      expect(reclassificationServiceMock.searchArchiveUnitsByCriteria).toHaveBeenCalledWith(
        expect.objectContaining({ pageNumber: 7, size: 20 }),
      );
    });

    it('should be valid and emit the unit title when exactly one unit is found', async () => {
      reclassificationServiceMock.searchArchiveUnitsByCriteria.mockReturnValue(
        of({ totalResults: 1, results: [{ Title_: { fr: 'Unité trouvée' } }] }),
      );

      const errors = createForm('guid-1', (path) => service.existArchiveUnit(path.guid, () => criteriaDto));
      await vi.advanceTimersByTimeAsync(service.debounceTime);

      expect(errors()).toEqual([]);
      expect(lastEmittedTitle()).toEqual('Unité trouvée');
    });

    it('should report a targetGuid error and reset the title when no unit is found', async () => {
      service.emitArchiveUnitTitle('previous');
      reclassificationServiceMock.searchArchiveUnitsByCriteria.mockReturnValue(of({ totalResults: 0, results: [] }));

      const errors = createForm('unknown', (path) => service.existArchiveUnit(path.guid, () => criteriaDto));
      await vi.advanceTimersByTimeAsync(service.debounceTime);

      expect(errors()).toEqual(['targetGuid']);
      expect(lastEmittedTitle()).toBeNull();
    });

    it('should report a targetGuid error when the GUID matches several units', async () => {
      reclassificationServiceMock.searchArchiveUnitsByCriteria.mockReturnValue(of({ totalResults: 2, results: [{}, {}] }));

      const errors = createForm('ambiguous', (path) => service.existArchiveUnit(path.guid, () => criteriaDto));
      await vi.advanceTimersByTimeAsync(service.debounceTime);

      expect(errors()).toEqual(['targetGuid']);
    });

    it('should be valid without searching when the value is null', async () => {
      const errors = createForm(null, (path) => service.existArchiveUnit(path.guid, () => criteriaDto));
      await vi.advanceTimersByTimeAsync(service.debounceTime);

      expect(errors()).toEqual([]);
      expect(reclassificationServiceMock.searchArchiveUnitsByCriteria).not.toHaveBeenCalled();
    });
  });
});
