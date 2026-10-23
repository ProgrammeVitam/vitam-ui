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
import { AgencyCreateValidators } from './agency-create.validators';

describe('Agency Create Validators', () => {
  let agencyServiceSpy: { existsProperties: ReturnType<typeof vi.fn> };
  let agencyCreateValidators: AgencyCreateValidators;

  beforeEach(() => {
    vi.useFakeTimers();
    agencyServiceSpy = {
      existsProperties: vi.fn().mockName('AgencyService.existsProperties'),
    };
    agencyCreateValidators = new AgencyCreateValidators(agencyServiceSpy as any);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  function nameForm(name: string, nameToIgnore?: string) {
    return form(
      signal({ name }),
      (path) => {
        agencyCreateValidators.onlyWhitespaces(path.name);
        agencyCreateValidators.uniqueName(path.name, () => nameToIgnore);
      },
      { injector: TestBed.inject(Injector) },
    );
  }

  async function validate(name: string, nameToIgnore?: string) {
    const testForm = nameForm(name, nameToIgnore);
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    return testForm
      .name()
      .errors()
      .map((error) => error.kind);
  }

  describe('uniqueName', () => {
    it('should not return any error', async () => {
      agencyServiceSpy.existsProperties.mockReturnValue(of(false));

      expect(await validate('123456')).toEqual([]);
      expect(agencyServiceSpy.existsProperties).toHaveBeenCalledWith({ name: '123456' });
    });

    it('should return nameExists', async () => {
      agencyServiceSpy.existsProperties.mockReturnValue(of(true));

      expect(await validate('123456')).toEqual(['nameExists']);
      expect(agencyServiceSpy.existsProperties).toHaveBeenCalledWith({ name: '123456' });
    });

    it('should return onlyWhitespaces', async () => {
      expect(await validate('   ')).toEqual(['onlyWhitespaces']);
      expect(agencyServiceSpy.existsProperties).not.toHaveBeenCalled();
    });

    it('should not call the service', async () => {
      agencyServiceSpy.existsProperties.mockReturnValue(of(true));

      expect(await validate('123456', '123456')).toEqual([]);
      expect(agencyServiceSpy.existsProperties).not.toHaveBeenCalled();
    });

    it('should call the service and return nameExists', async () => {
      agencyServiceSpy.existsProperties.mockReturnValue(of(true));

      expect(await validate('111111', '123456')).toEqual(['nameExists']);
      expect(agencyServiceSpy.existsProperties).toHaveBeenCalledWith({ name: '111111' });
    });
  });

  describe('uniqueIdentifier', () => {
    it('should return identifierExists', async () => {
      agencyServiceSpy.existsProperties.mockReturnValue(of(true));
      const testForm = form(signal({ identifier: 'id' }), (path) => agencyCreateValidators.uniqueIdentifier(path.identifier), {
        injector: TestBed.inject(Injector),
      });
      TestBed.tick();
      await vi.advanceTimersByTimeAsync(400);

      expect(
        testForm
          .identifier()
          .errors()
          .map((error) => error.kind),
      ).toEqual(['identifierExists']);
      expect(agencyServiceSpy.existsProperties).toHaveBeenCalledWith({ identifier: 'id' });
    });
  });
});
