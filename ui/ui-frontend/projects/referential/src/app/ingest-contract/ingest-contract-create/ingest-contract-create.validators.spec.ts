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
import { form, ValidationError } from '@angular/forms/signals';
import { of } from 'rxjs';
import { IngestContract } from 'vitamui-library';
import { IngestContractCreateValidators } from './ingest-contract-create.validators';

describe('IngestContractCreateValidators', () => {
  let injector: Injector;
  let ingestContractServiceSpy: { existsProperties: ReturnType<typeof vi.fn> };

  function errorKinds(field: () => { errors: () => readonly ValidationError[] }): string[] {
    return field()
      .errors()
      .map((error) => error.kind);
  }

  beforeEach(() => {
    injector = TestBed.inject(Injector);
    ingestContractServiceSpy = {
      existsProperties: vi.fn().mockName('IngestContractService.existsProperties'),
    };
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should create an instance', () => {
    expect(new IngestContractCreateValidators(ingestContractServiceSpy as any)).toBeTruthy();
  });

  it('uniqueName should return a nameExists error', async () => {
    ingestContractServiceSpy.existsProperties.mockReturnValue(of(true));
    const validators = new IngestContractCreateValidators(ingestContractServiceSpy as any);
    const testForm = form(signal({ name: ' name ' }), (path) => validators.uniqueName(path.name), { injector });

    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ingestContractServiceSpy.existsProperties).toHaveBeenCalledWith({ name: 'name' });
    expect(errorKinds(testForm.name)).toEqual(['nameExists']);
  });

  it('uniqueIdentifier should not call the service for the ignored identifier', async () => {
    const validators = new IngestContractCreateValidators(ingestContractServiceSpy as any);
    const testForm = form(signal({ identifier: 'id' }), (path) => validators.uniqueIdentifier(path.identifier, () => 'id'), {
      injector,
    });

    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ingestContractServiceSpy.existsProperties).not.toHaveBeenCalled();
    expect(errorKinds(testForm.identifier)).toEqual([]);
  });

  it('uniqueNameWhileEdit should ignore the name of the edited ingest contract', async () => {
    ingestContractServiceSpy.existsProperties.mockReturnValue(of(true));
    const validators = new IngestContractCreateValidators(ingestContractServiceSpy as any);
    const ingestContract = { name: 'current' } as IngestContract;
    const testForm = form(signal({ name: 'current' }), (path) => validators.uniqueNameWhileEdit(path.name, () => ingestContract), {
      injector,
    });

    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ingestContractServiceSpy.existsProperties).not.toHaveBeenCalled();
    expect(errorKinds(testForm.name)).toEqual([]);

    testForm.name().value.set('other');
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ingestContractServiceSpy.existsProperties).toHaveBeenCalledWith({ name: 'other' });
    expect(errorKinds(testForm.name)).toEqual(['nameExists']);
  });

  it('uniqueName should not return any error when the name is free', async () => {
    ingestContractServiceSpy.existsProperties.mockReturnValue(of(false));
    const validators = new IngestContractCreateValidators(ingestContractServiceSpy as any);
    const testForm = form(signal({ name: 'free' }), (path) => validators.uniqueName(path.name), { injector });

    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ingestContractServiceSpy.existsProperties).toHaveBeenCalledWith({ name: 'free' });
    expect(errorKinds(testForm.name)).toEqual([]);
  });

  it('uniqueIdentifier should return an identifierExists error for a trimmed existing identifier', async () => {
    ingestContractServiceSpy.existsProperties.mockReturnValue(of(true));
    const validators = new IngestContractCreateValidators(ingestContractServiceSpy as any);
    const testForm = form(signal({ identifier: ' IC-0001 ' }), (path) => validators.uniqueIdentifier(path.identifier), { injector });

    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ingestContractServiceSpy.existsProperties).toHaveBeenCalledWith({ identifier: 'IC-0001' });
    expect(errorKinds(testForm.identifier)).toEqual(['identifierExists']);
  });

  it('uniqueNameWhileEdit should be valid without calling the service when no ingest contract is edited', async () => {
    ingestContractServiceSpy.existsProperties.mockReturnValue(of(true));
    const validators = new IngestContractCreateValidators(ingestContractServiceSpy as any);
    const testForm = form(signal({ name: 'any' }), (path) => validators.uniqueNameWhileEdit(path.name, () => undefined), { injector });

    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ingestContractServiceSpy.existsProperties).not.toHaveBeenCalled();
    expect(errorKinds(testForm.name)).toEqual([]);
  });

  it('uniqueNameWhileEdit should read the edited ingest contract at validation time', async () => {
    ingestContractServiceSpy.existsProperties.mockReturnValue(of(true));
    const validators = new IngestContractCreateValidators(ingestContractServiceSpy as any);
    let ingestContract = { name: 'before' } as IngestContract;
    const testForm = form(signal({ name: 'after' }), (path) => validators.uniqueNameWhileEdit(path.name, () => ingestContract), {
      injector,
    });
    ingestContract = { name: 'after' } as IngestContract;

    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    expect(ingestContractServiceSpy.existsProperties).not.toHaveBeenCalled();
    expect(errorKinds(testForm.name)).toEqual([]);
  });
});
