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
import { of } from 'rxjs';
import { allowedIdentifier, requiredNotBlank, requiredValue, validateObservable } from './signal-form-validators';
import { toValidationErrors } from './signal-form-errors';

describe('signal form validators', () => {
  let injector: Injector;

  beforeEach(() => {
    injector = TestBed.inject(Injector);
  });

  it('requiredValue should accept false and reject empty arrays', () => {
    const testForm = form(
      signal({ enabled: false, items: [] as string[] }),
      (path) => {
        requiredValue(path.enabled);
        requiredValue(path.items);
      },
      { injector },
    );

    expect(testForm.enabled().valid()).toBe(true);
    expect(testForm.enabled().required()).toBe(true);
    expect(
      testForm
        .items()
        .errors()
        .map((error) => error.kind),
    ).toEqual(['required']);

    testForm.items().value.set(['a']);
    expect(testForm.items().valid()).toBe(true);
  });

  it('requiredNotBlank should reject blank values', () => {
    const testForm = form(signal({ name: '  ' }), (path) => requiredNotBlank(path.name), { injector });

    expect(testForm.name().required()).toBe(true);
    expect(
      testForm
        .name()
        .errors()
        .map((error) => error.kind),
    ).toEqual(['requiredNotBlank']);
  });

  it('allowedIdentifier should reject forbidden characters', () => {
    const testForm = form(signal({ id: 'a b' }), (path) => allowedIdentifier(path.id), { injector });

    expect(
      testForm
        .id()
        .errors()
        .map((error) => error.kind),
    ).toEqual(['incorrectIdentifier']);
  });

  it('validateObservable should report the errors of the stream after the debounce time', async () => {
    vi.useFakeTimers();
    const stream = vi.fn((value: string) => of(value === 'taken'));
    const testForm = form(
      signal({ code: 'taken' }),
      (path) => validateObservable(path.code, { stream, onSuccess: (exists) => (exists ? { kind: 'uniqueCode' } : undefined) }),
      { injector },
    );

    TestBed.tick();
    expect(testForm.code().pending()).toBe(true);
    await vi.advanceTimersByTimeAsync(400);
    expect(stream).toHaveBeenCalledWith('taken');
    expect(testForm.code().pending()).toBe(false);
    expect(
      testForm
        .code()
        .errors()
        .map((error) => error.kind),
    ).toEqual(['uniqueCode']);

    testForm.code().value.set('free');
    TestBed.tick();
    expect(testForm.code().pending()).toBe(true);
    await vi.advanceTimersByTimeAsync(400);
    expect(stream).toHaveBeenCalledWith('free');
    expect(testForm.code().valid()).toBe(true);
    vi.useRealTimers();
  });

  it('toValidationErrors should map built-in errors to reactive forms errors', () => {
    const testForm = form(signal({ name: 'abcd' }), (path) => requiredValue(path.name), { injector });
    testForm.name().value.set('');

    expect(toValidationErrors(testForm.name().errors())).toEqual({ required: true });
    expect(toValidationErrors([])).toBeNull();
  });
});
