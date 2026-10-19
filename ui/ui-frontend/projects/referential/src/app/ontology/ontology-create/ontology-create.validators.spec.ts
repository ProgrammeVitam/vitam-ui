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

import { OntologyCreateValidators } from './ontology-create.validators';

describe('Ontology Create Validators', () => {
  let ontologyServiceSpy: { exists: ReturnType<typeof vi.fn> };
  let ontologyCreateValidators: OntologyCreateValidators;

  beforeEach(() => {
    vi.useFakeTimers();
    ontologyServiceSpy = {
      exists: vi.fn().mockName('OntologyService.exists'),
    };
    ontologyCreateValidators = new OntologyCreateValidators(ontologyServiceSpy as any);
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  async function validate(identifier: string) {
    const testForm = form(
      signal({ identifier }),
      (path) => {
        ontologyCreateValidators.patternID(path.identifier);
        ontologyCreateValidators.uniqueID(path.identifier);
      },
      { injector: TestBed.inject(Injector) },
    );
    TestBed.tick();
    await vi.advanceTimersByTimeAsync(400);
    return testForm
      .identifier()
      .errors()
      .map((error) => error.kind);
  }

  describe('uniqueID', () => {
    it('should not return any error', async () => {
      ontologyServiceSpy.exists.mockReturnValue(of(false));

      expect(await validate('123456')).toEqual([]);
      expect(ontologyServiceSpy.exists).toHaveBeenCalledWith('123456');
    });

    it('should return idExists', async () => {
      ontologyServiceSpy.exists.mockReturnValue(of(true));

      expect(await validate('123456')).toEqual(['idExists']);
      expect(ontologyServiceSpy.exists).toHaveBeenCalledWith('123456');
    });

    it('should return idExists for a domain', async () => {
      ontologyServiceSpy.exists.mockReturnValue(of(true));

      expect(await validate('test.com')).toEqual(['idExists']);
      expect(ontologyServiceSpy.exists).toHaveBeenCalledWith('test.com');
    });
  });

  describe('patternID', () => {
    it('should return idPattern', async () => {
      expect(await validate('_id')).toEqual(['idPattern']);
      expect(await validate('#id')).toEqual(['idPattern']);
      expect(await validate('my id')).toEqual(['idPattern']);
      expect(ontologyServiceSpy.exists).not.toHaveBeenCalled();
    });
  });
});
