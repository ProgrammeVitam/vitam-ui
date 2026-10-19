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

import { OwnerService } from '../owner.service';
import { OwnerFormValidators } from './owner-form.validators';

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
});

describe('Owner Form Validators', () => {
  const createValidators = (ownerServiceSpy: { exists: ReturnType<typeof vi.fn> }) => {
    TestBed.configureTestingModule({
      providers: [{ provide: OwnerService, useValue: ownerServiceSpy }],
    });
    return TestBed.runInInjectionContext(() => new OwnerFormValidators());
  };

  it('should return null', () => {
    const ownerServiceSpy = {
      exists: vi.fn().mockName('OwnerService.exists'),
    };
    ownerServiceSpy.exists.mockReturnValue(of(false));
    const ownerFormValidators = createValidators(ownerServiceSpy);
    from(ownerFormValidators.uniqueCode()(new FormControl('123456'))).subscribe((result) => {
      expect(result).toBeNull();
    });
    vi.advanceTimersByTime(400);
    expect(ownerServiceSpy.exists).toHaveBeenCalledWith('123456');
  });

  it('should return { uniqueCode: true }', () => {
    const ownerServiceSpy = {
      exists: vi.fn().mockName('OwnerService.exists'),
    };
    ownerServiceSpy.exists.mockReturnValue(of(true));
    const ownerFormValidators = createValidators(ownerServiceSpy);
    from(ownerFormValidators.uniqueCode()(new FormControl('123456'))).subscribe((result) => {
      expect(result).toEqual({ uniqueCode: true });
    });
    vi.advanceTimersByTime(400);
    expect(ownerServiceSpy.exists).toHaveBeenCalledWith('123456');
  });

  it('should not call the service', () => {
    const ownerServiceSpy = {
      exists: vi.fn().mockName('OwnerService.exists'),
    };
    ownerServiceSpy.exists.mockReturnValue(of(true));
    const ownerFormValidators = createValidators(ownerServiceSpy);
    from(ownerFormValidators.uniqueCode('123456')(new FormControl('123456'))).subscribe((result) => {
      expect(result).toEqual(null);
    });
    vi.advanceTimersByTime(400);
    expect(ownerServiceSpy.exists).not.toHaveBeenCalled();
  });

  it('should call the service', () => {
    const ownerServiceSpy = {
      exists: vi.fn().mockName('OwnerService.exists'),
    };
    ownerServiceSpy.exists.mockReturnValue(of(true));
    const ownerFormValidators = createValidators(ownerServiceSpy);
    from(ownerFormValidators.uniqueCode('123456')(new FormControl('111111'))).subscribe((result) => {
      expect(result).toEqual({ uniqueCode: true });
    });
    vi.advanceTimersByTime(400);
    expect(ownerServiceSpy.exists).toHaveBeenCalledWith('111111');
  });
});

describe('Owner Form Validators (signal forms)', () => {
  const createForm = (exists: boolean, codeToIgnore?: string) => {
    const ownerServiceSpy = { exists: vi.fn().mockName('OwnerService.exists').mockReturnValue(of(exists)) };
    TestBed.configureTestingModule({ providers: [{ provide: OwnerService, useValue: ownerServiceSpy }] });
    const validators = TestBed.inject(OwnerFormValidators);
    const codeForm = TestBed.runInInjectionContext(() =>
      form(signal({ code: '' }), (path) => validators.uniqueCodeRule(path.code, () => codeToIgnore)),
    );
    return { codeForm, ownerServiceSpy };
  };

  it('should set the uniqueCode error', async () => {
    const { codeForm, ownerServiceSpy } = createForm(true);
    codeForm.code().value.set('123456');
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ownerServiceSpy.exists).toHaveBeenCalledWith('123456');
    expect(
      codeForm
        .code()
        .errors()
        .map((error) => error.kind),
    ).toEqual(['uniqueCode']);
  });

  it('should not call the service for the code to ignore', async () => {
    const { codeForm, ownerServiceSpy } = createForm(true, '123456');
    codeForm.code().value.set('123456');
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ownerServiceSpy.exists).not.toHaveBeenCalled();
    expect(codeForm.code().valid()).toBe(true);
  });
});
