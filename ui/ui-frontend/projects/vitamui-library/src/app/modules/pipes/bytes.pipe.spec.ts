/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { BytesPipe } from './bytes.pipe';
import { TestBed } from '@angular/core/testing';
import { DecimalPipe } from '@angular/common';

describe('BytesPipe', () => {
  let pipe: BytesPipe;
  beforeAll(() => {
    TestBed.configureTestingModule({
      providers: [DecimalPipe, BytesPipe],
    });
    pipe = TestBed.inject(BytesPipe);
  });

  it('create an instance', () => {
    expect(pipe).toBeTruthy();
  });

  it('should display human readable sizes', () => {
    expect(pipe.transform('0', 0)).toBe('0 octet');
    expect(pipe.transform('0', 2)).toBe('0 octet');
    expect(pipe.transform('1', 0)).toBe('1 octet');
    expect(pipe.transform('1', 2)).toBe('1 octet');
    expect(pipe.transform('2', 0)).toBe('2 octets');
    expect(pipe.transform('2', 2)).toBe('2 octets');
    expect(pipe.transform('595', 0)).toBe('595 octets');
    expect(pipe.transform('595', 2)).toBe('595 octets');
    expect(pipe.transform('78800', 0)).toBe('77 ko');
    expect(pipe.transform('78800', 2)).toBe('76,95 ko');
    expect(pipe.transform('57489487', 0)).toBe('55 Mo');
    expect(pipe.transform('57489487', 2)).toBe('54,83 Mo');
    expect(pipe.transform('45628658811', 0)).toBe('42 Go');
    expect(pipe.transform('45628658811', 2)).toBe('42,5 Go');
    expect(pipe.transform('45628658811645', 0)).toBe('41 To');
    expect(pipe.transform('45628658811645', 2)).toBe('41,5 To');
    expect(pipe.transform('45628658811645832', 0)).toBe('41 Po');
    expect(pipe.transform('45628658811645832', 2)).toBe('40,53 Po');
    expect(pipe.transform('4562865881164583245', 0)).toBe('4\u{202f}053 Po');
    expect(pipe.transform('4562865881164583245', 2)).toBe('4\u{202f}052,64 Po');
    expect(pipe.transform('456286588116458324532', 0)).toBe('405\u{202f}264 Po');
    expect(pipe.transform('456286588116458324532', 2)).toBe('405\u{202f}263,9 Po');
  });

  it('should return unformatted same value when provide negative value', () => {
    expect(pipe.transform('-70')).toBe('-70');
  });
});
