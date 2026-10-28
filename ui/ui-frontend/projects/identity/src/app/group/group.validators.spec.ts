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
import { signal } from '@angular/core';
import { form } from '@angular/forms/signals';
import { TestBed } from '@angular/core/testing';
import { FormControl } from '@angular/forms';
import { from, of } from 'rxjs';

import { GroupService } from './group.service';
import { GroupValidators } from './group.validators';

const createValidators = (groupServiceSpy: { exists?: ReturnType<typeof vi.fn>; unitExists?: ReturnType<typeof vi.fn> }) => {
  TestBed.configureTestingModule({
    providers: [{ provide: GroupService, useValue: groupServiceSpy }],
  });
  return TestBed.runInInjectionContext(() => new GroupValidators());
};

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
});

describe('ProfileGroupValidators nameExist', () => {
  it('should return null', () => {
    const groupServiceSpy = {
      exists: vi.fn().mockName('GroupService.exists'),
    };
    groupServiceSpy.exists.mockReturnValue(of(false));
    const groupValidators = createValidators(groupServiceSpy);
    from(groupValidators.nameExists('42')(new FormControl('123456'))).subscribe((result) => {
      expect(result).toBeNull();
    });
    vi.advanceTimersByTime(400);
    expect(groupServiceSpy.exists).toHaveBeenCalledWith('42', '123456');
  });

  it('should return { nameExists: true }', () => {
    const groupServiceSpy = {
      exists: vi.fn().mockName('GroupService.exists'),
    };
    groupServiceSpy.exists.mockReturnValue(of(true));
    const profileGroupValidators = createValidators(groupServiceSpy);
    from(profileGroupValidators.nameExists('42')(new FormControl('123456'))).subscribe((result) => {
      expect(result).toEqual({ nameExists: true });
    });
    vi.advanceTimersByTime(400);
    expect(groupServiceSpy.exists).toHaveBeenCalledWith('42', '123456');
  });

  it('should not call the service', () => {
    const groupServiceSpy = {
      exists: vi.fn().mockName('GroupService.exists'),
    };
    groupServiceSpy.exists.mockReturnValue(of(true));
    const profileGroupValidators = createValidators(groupServiceSpy);
    from(profileGroupValidators.nameExists('42', '123456')(new FormControl('123456'))).subscribe((result) => {
      expect(result).toEqual(null);
    });
    vi.advanceTimersByTime(400);
    expect(groupServiceSpy.exists).not.toHaveBeenCalled();
  });

  it('should call the service', () => {
    const groupServiceSpy = {
      exists: vi.fn().mockName('GroupService.exists'),
    };
    groupServiceSpy.exists.mockReturnValue(of(true));
    const profileGroupValidators = createValidators(groupServiceSpy);
    from(profileGroupValidators.nameExists('42', '123456')(new FormControl('111111'))).subscribe((result) => {
      expect(result).toEqual({ nameExists: true });
    });
    vi.advanceTimersByTime(400);
    expect(groupServiceSpy.exists).toHaveBeenCalledWith('42', '111111');
  });
});

describe('ProfileGroupValidators nameExistsRule', () => {
  it('should set the nameExists error', async () => {
    const groupServiceSpy = { exists: vi.fn().mockName('GroupService.exists').mockReturnValue(of(true)) };
    const groupValidators = createValidators(groupServiceSpy);
    const groupForm = TestBed.runInInjectionContext(() =>
      form(signal({ name: '' }), (path) => groupValidators.nameExistsRule(path.name, '42')),
    );
    groupForm.name().value.set('123456');
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(groupServiceSpy.exists).toHaveBeenCalledWith('42', '123456');
    expect(
      groupForm
        .name()
        .errors()
        .map((error) => error.kind),
    ).toEqual(['nameExists']);
  });
});

describe('ProfileGroupValidators unitExistsRule', () => {
  const createForm = (exists: boolean, unitsToIgnore: string[] = []) => {
    const groupServiceSpy = { unitExists: vi.fn().mockName('GroupService.unitExists').mockReturnValue(of(exists)) };
    const groupValidators = createValidators(groupServiceSpy);
    const unitForm = TestBed.runInInjectionContext(() =>
      form(signal({ unit: '' }), (path) =>
        groupValidators.unitExistsRule(
          path.unit,
          () => 'customerId',
          () => unitsToIgnore,
        ),
      ),
    );
    return { unitForm, groupServiceSpy };
  };

  const setUnit = async (unitForm: ReturnType<typeof createForm>['unitForm'], unit: string) => {
    unitForm.unit().value.set(unit);
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
  };

  it('should not set an error', async () => {
    const { unitForm, groupServiceSpy } = createForm(false);
    await setUnit(unitForm, 'unite1');
    expect(groupServiceSpy.unitExists).toHaveBeenCalledWith('customerId', 'unite1');
    expect(unitForm.unit().valid()).toBe(true);
  });

  it('should set the unitExists error', async () => {
    const { unitForm, groupServiceSpy } = createForm(true);
    await setUnit(unitForm, 'unite1');
    expect(groupServiceSpy.unitExists).toHaveBeenCalledWith('customerId', 'unite1');
    expect(
      unitForm
        .unit()
        .errors()
        .map((error) => error.kind),
    ).toEqual(['unitExists']);
  });

  it('should not call the service', async () => {
    const { unitForm, groupServiceSpy } = createForm(true, ['unite2']);
    await setUnit(unitForm, 'unite2');
    expect(groupServiceSpy.unitExists).not.toHaveBeenCalled();
    expect(unitForm.unit().valid()).toBe(true);
  });

  it('should call the service with the trimmed unit', async () => {
    const { unitForm, groupServiceSpy } = createForm(true);
    await setUnit(unitForm, ' unite2 ');
    expect(groupServiceSpy.unitExists).toHaveBeenCalledWith('customerId', 'unite2');
  });
});
