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
import { form } from '@angular/forms/signals';
import { of, throwError } from 'rxjs';
import { ArchiveService } from '../../../archive.service';
import { EntryOperationValidatorService } from './entry-operation-validator.service';

const ID_1 = 'aeeaaaaaaghduohdabjwgamdq6ymgaaaaaaq'; // 36 lowercase alphanumeric chars
const ID_2 = 'aeeaaaaaaghduohdabjwgamdq6ymgbbbbbbq';
const ID_3 = 'aeeaaaaaaghduohdabjwgamdq6ymgccccccq';

describe('EntryOperationValidatorService', () => {
  let service: EntryOperationValidatorService;
  let archiveServiceMock: { checkOperationIdsExistence: ReturnType<typeof vi.fn> };

  /** Builds a signal form on the `ids` field and returns its errors getter (without the field reference). */
  const createForm = (ids: string) => {
    const testForm = form(signal({ ids }), (path) => service.validateEntryOperationIds(path.ids), {
      injector: TestBed.inject(Injector),
    });
    TestBed.tick();
    return () =>
      testForm
        .ids()
        .errors()
        .map(({ fieldTree, ...error }) => error);
  };

  const validate = async (ids: string) => {
    const errors = createForm(ids);
    await vi.advanceTimersByTimeAsync(service.debounceTime);
    return errors();
  };

  beforeEach(() => {
    vi.useFakeTimers();
    archiveServiceMock = { checkOperationIdsExistence: vi.fn() };
    TestBed.configureTestingModule({
      providers: [{ provide: ArchiveService, useValue: archiveServiceMock }],
    });
    service = TestBed.inject(EntryOperationValidatorService);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it.each([null, ''])('should not report any error for empty value %s', async (value) => {
    expect(await validate(value)).toEqual([]);
    expect(archiveServiceMock.checkOperationIdsExistence).not.toHaveBeenCalled();
  });

  it('should flag whitespace-only input immediately without debounce', () => {
    const errors = createForm('   \t  ');

    expect(errors()).toEqual([{ kind: 'whitespaceOnly' }]);
    expect(archiveServiceMock.checkOperationIdsExistence).not.toHaveBeenCalled();
  });

  it('should wait for the debounce time before calling the backend', async () => {
    archiveServiceMock.checkOperationIdsExistence.mockReturnValue(of({ [ID_1]: false }));

    const errors = createForm(ID_1);
    await vi.advanceTimersByTimeAsync(service.debounceTime - 1);
    expect(archiveServiceMock.checkOperationIdsExistence).not.toHaveBeenCalled();
    expect(errors()).toEqual([]);

    await vi.advanceTimersByTimeAsync(1);
    expect(archiveServiceMock.checkOperationIdsExistence).toHaveBeenCalledWith([ID_1]);
    expect(errors()).toEqual([{ kind: 'invalidOperationIds', invalidIds: ID_1 }]);
  });

  it('should return emptyAfterTrim when only separators are provided', async () => {
    expect(await validate(' , ,, ')).toEqual([{ kind: 'emptyAfterTrim' }]);
    expect(archiveServiceMock.checkOperationIdsExistence).not.toHaveBeenCalled();
  });

  it('should report each duplicated id only once', async () => {
    expect(await validate(`${ID_1}, ${ID_2},${ID_1} , ${ID_1}, ${ID_2}, ${ID_3}`)).toEqual([
      { kind: 'duplicateIds', duplicateIds: `${ID_1}, ${ID_2}` },
    ]);
    expect(archiveServiceMock.checkOperationIdsExistence).not.toHaveBeenCalled();
  });

  it('should report ids with an invalid format (length, uppercase, special chars)', async () => {
    const tooShort = 'abc123';
    const upperCase = ID_1.toUpperCase();
    const withDash = ID_2.slice(0, 35) + '-';

    expect(await validate(`${ID_1}, ${tooShort}, ${upperCase}, ${withDash}`)).toEqual([
      { kind: 'invalidFormat', invalidIds: `${tooShort}, ${upperCase}, ${withDash}` },
    ]);
    expect(archiveServiceMock.checkOperationIdsExistence).not.toHaveBeenCalled();
  });

  it('should send trimmed ids to the backend and report no error when all exist', async () => {
    archiveServiceMock.checkOperationIdsExistence.mockReturnValue(of({ [ID_1]: true, [ID_2]: true }));

    expect(await validate(`  ${ID_1} ,${ID_2},`)).toEqual([]);
    expect(archiveServiceMock.checkOperationIdsExistence).toHaveBeenCalledWith([ID_1, ID_2]);
  });

  it('should report ids that are unknown or missing from the backend existence map', async () => {
    archiveServiceMock.checkOperationIdsExistence.mockReturnValue(of({ [ID_1]: true, [ID_2]: false }));

    expect(await validate(`${ID_1}, ${ID_2}, ${ID_3}`)).toEqual([{ kind: 'invalidOperationIds', invalidIds: `${ID_2}, ${ID_3}` }]);
  });

  it('should consider every id invalid when the backend call fails', async () => {
    archiveServiceMock.checkOperationIdsExistence.mockReturnValue(throwError(() => new Error('backend down')));

    expect(await validate(`${ID_1}, ${ID_2}`)).toEqual([{ kind: 'invalidOperationIds', invalidIds: `${ID_1}, ${ID_2}` }]);
  });
});
