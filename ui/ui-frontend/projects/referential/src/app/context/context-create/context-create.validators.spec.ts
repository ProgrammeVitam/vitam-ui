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
import { ContextPermission } from 'vitamui-library';
import { ContextCreateValidators } from './context-create.validators';

describe('Context Create Validators', () => {
  let injector: Injector;

  beforeEach(() => {
    injector = TestBed.inject(Injector);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  function errorKinds(field: () => { errors: () => readonly ValidationError[] }): string[] {
    return field()
      .errors()
      .map((error) => error.kind);
  }

  describe('uniqueName', () => {
    let contextServiceSpy: { existsProperties: ReturnType<typeof vi.fn> };

    function createNameForm(name: string, nameToIgnore?: string) {
      const contextCreateValidators = new ContextCreateValidators(contextServiceSpy as any);
      return form(
        signal({ name }),
        (path) => contextCreateValidators.uniqueName(path.name, nameToIgnore === undefined ? undefined : () => nameToIgnore),
        { injector },
      );
    }

    beforeEach(() => {
      vi.useFakeTimers();
      contextServiceSpy = {
        existsProperties: vi.fn().mockName('ContextService.existsProperties'),
      };
    });

    it('should return no error', async () => {
      contextServiceSpy.existsProperties.mockReturnValue(of(false));
      const nameForm = createNameForm('123456');

      TestBed.tick();
      await vi.advanceTimersByTimeAsync(400);
      expect(contextServiceSpy.existsProperties).toHaveBeenCalledWith({ name: '123456' });
      expect(errorKinds(nameForm.name)).toEqual([]);
    });

    it('should return a nameExists error', async () => {
      contextServiceSpy.existsProperties.mockReturnValue(of(true));
      const nameForm = createNameForm('123456');

      TestBed.tick();
      await vi.advanceTimersByTimeAsync(400);
      expect(contextServiceSpy.existsProperties).toHaveBeenCalledWith({ name: '123456' });
      expect(errorKinds(nameForm.name)).toEqual(['nameExists']);
    });

    it('should not call the service', async () => {
      contextServiceSpy.existsProperties.mockReturnValue(of(true));
      const nameForm = createNameForm('123456', '123456');

      TestBed.tick();
      await vi.advanceTimersByTimeAsync(400);
      expect(contextServiceSpy.existsProperties).not.toHaveBeenCalled();
      expect(errorKinds(nameForm.name)).toEqual([]);
    });

    it('should call the service', async () => {
      contextServiceSpy.existsProperties.mockReturnValue(of(true));
      const nameForm = createNameForm('111111', '123456');

      TestBed.tick();
      await vi.advanceTimersByTimeAsync(400);
      expect(contextServiceSpy.existsProperties).toHaveBeenCalledWith({ name: '111111' });
      expect(errorKinds(nameForm.name)).toEqual(['nameExists']);
    });
  });

  describe('permissionInvalid', () => {
    function createPermissionsForm(permissions: ContextPermission[], enabled = true) {
      const contextCreateValidators = new ContextCreateValidators({} as any);
      return form(signal({ permissions }), (path) => contextCreateValidators.permissionInvalid(path.permissions, () => enabled), {
        injector,
      });
    }

    it('should return a noPermissions error', () => {
      expect(errorKinds(createPermissionsForm([]).permissions)).toEqual(['noPermissions']);
    });

    it('should return a permissionsTenant error', () => {
      expect(errorKinds(createPermissionsForm([new ContextPermission('', [], [])]).permissions)).toEqual(['permissionsTenant']);
    });

    it('should return no error', () => {
      expect(errorKinds(createPermissionsForm([new ContextPermission('1', [], [])]).permissions)).toEqual([]);
      expect(errorKinds(createPermissionsForm([], false).permissions)).toEqual([]);
    });
  });
});
