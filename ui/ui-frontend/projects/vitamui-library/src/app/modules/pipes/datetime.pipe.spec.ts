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
import { DATE_PIPE_DEFAULT_OPTIONS, DatePipe } from '@angular/common';
import { DateTimePipe } from './datetime.pipe';
import { TestBed } from '@angular/core/testing';

describe('DateTimePipe', () => {
  describe('in UTC', () => {
    let pipe: DateTimePipe;
    beforeAll(() => {
      TestBed.configureTestingModule({
        providers: [
          DatePipe,
          { provide: DATE_PIPE_DEFAULT_OPTIONS, useValue: { timezone: '+0000' } }, // Simulate browser in UTC timezone
        ],
      });
      const datePipe = TestBed.inject(DatePipe);
      pipe = new DateTimePipe(datePipe);
    });
    it('formats date correctly', () => {
      expect(pipe.transform(null)).toBe(null);
      expect(pipe.transform(undefined)).toBe(null);

      expect(pipe.transform('2024-10-24T00:00:00.000Z', 'dd/MM/yyyy')).toBe('24/10/2024');
      expect(pipe.transform('2024-10-24T23:59:59.999Z', 'dd/MM/yyyy')).toBe('24/10/2024');
      expect(pipe.transform('2024-10-24T00:00:00.000', 'dd/MM/yyyy')).toBe('24/10/2024'); // No timezone (defaults to 'Z')
      expect(pipe.transform('2024-10-24T23:59:59.999', 'dd/MM/yyyy')).toBe('24/10/2024'); // No timezone (defaults to 'Z')

      expect(pipe.transform('2024-10-24T00:00:00.000Z', 'HH:mm:ss')).toBe('00:00:00');
      expect(pipe.transform('2024-10-24T23:59:59.999Z', 'HH:mm:ss')).toBe('23:59:59');
      expect(pipe.transform('2024-10-24T14:02:36.000', 'HH:mm:ss')).toBe('14:02:36'); // No timezone (defaults to 'Z')

      expect(pipe.transform(1729728000.0, 'dd/MM/yyyy')).toBe('24/10/2024');
    });
  });

  describe('in UTC+2', () => {
    let pipe: DateTimePipe;
    beforeAll(() => {
      TestBed.configureTestingModule({
        providers: [
          DatePipe,
          { provide: DATE_PIPE_DEFAULT_OPTIONS, useValue: { timezone: '+0200' } }, // Simulate browser in UTC+2 timezone
        ],
      });
      const datePipe = TestBed.inject(DatePipe);
      pipe = new DateTimePipe(datePipe);
    });
    it('formats date correctly', () => {
      expect(pipe.transform(null)).toBe(null);
      expect(pipe.transform(undefined)).toBe(null);

      expect(pipe.transform('2024-10-24T00:00:00.000Z', 'dd/MM/yyyy')).toBe('24/10/2024');
      expect(pipe.transform('2024-10-24T23:59:59.999Z', 'dd/MM/yyyy')).toBe('25/10/2024');
      expect(pipe.transform('2024-10-24T01:00:00.000', 'dd/MM/yyyy')).toBe('24/10/2024'); // No timezone (defaults to 'Z')
      expect(pipe.transform('2024-10-24T23:00:00.000', 'dd/MM/yyyy')).toBe('25/10/2024'); // No timezone (defaults to 'Z')

      expect(pipe.transform('2024-10-24T00:00:00.000Z', 'HH:mm:ss')).toBe('02:00:00');
      expect(pipe.transform('2024-10-24T23:59:59.999Z', 'HH:mm:ss')).toBe('01:59:59');
      expect(pipe.transform('2024-10-24T14:02:36.000', 'HH:mm:ss')).toBe('16:02:36'); // No timezone (defaults to 'Z')

      expect(pipe.transform(1729728000.0, 'dd/MM/yyyy')).toBe('24/10/2024');
    });
  });

  describe('in UTC-2', () => {
    let pipe: DateTimePipe;
    beforeAll(() => {
      TestBed.configureTestingModule({
        providers: [
          DatePipe,
          { provide: DATE_PIPE_DEFAULT_OPTIONS, useValue: { timezone: '-0200' } }, // Simulate browser in UTC-2 timezone
        ],
      });
      const datePipe = TestBed.inject(DatePipe);
      pipe = new DateTimePipe(datePipe);
    });
    it('formats date correctly', () => {
      expect(pipe.transform(null)).toBe(null);
      expect(pipe.transform(undefined)).toBe(null);

      expect(pipe.transform('2024-10-24T00:00:00.000Z', 'dd/MM/yyyy')).toBe('23/10/2024');
      expect(pipe.transform('2024-10-24T23:59:59.999Z', 'dd/MM/yyyy')).toBe('24/10/2024');
      expect(pipe.transform('2024-10-24T01:00:00.000', 'dd/MM/yyyy')).toBe('23/10/2024'); // No timezone (defaults to 'Z')
      expect(pipe.transform('2024-10-24T23:00:00.000', 'dd/MM/yyyy')).toBe('24/10/2024'); // No timezone (defaults to 'Z')

      expect(pipe.transform('2024-10-24T00:00:00.000Z', 'HH:mm:ss')).toBe('22:00:00');
      expect(pipe.transform('2024-10-24T23:59:59.999Z', 'HH:mm:ss')).toBe('21:59:59');
      expect(pipe.transform('2024-10-24T14:02:36.000', 'HH:mm:ss')).toBe('12:02:36'); // No timezone (defaults to 'Z')

      expect(pipe.transform(1729728000.0, 'dd/MM/yyyy')).toBe('23/10/2024');
    });
  });
});
