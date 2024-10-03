/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import { DatePipe } from '@angular/common';
import { DateTimePipe } from './datetime.pipe';

describe('DatePipe', () => {
  const fake = {
    transform: (_value: any, _format?: string, timezone?: string) => {
      return timezone;
    },
  };

  beforeEach(() => {
    jasmine.clock().uninstall();
    jasmine.clock().install();
  });

  it('convert date to a correct Date', () => {
    // Given
    const fixedDate = new Date(Date.UTC(2020, 6, 1));
    jasmine.clock().mockDate(fixedDate);
    const pipe = new DateTimePipe(fake as DatePipe);

    const utcDate = '2018-02-20T11:15:14.00';

    // When
    const timezone = pipe.transform(utcDate);

    // Then
    expect(pipe).toBeTruthy();
    expect(timezone).toContain('UTC');
  });

  it('formatDateTime should return the correct DateFormat', () => {
    // Given
    const expectedResult = '2022-10-18T00:00:00.000Z';
    const dateValue = 'Tue Oct 18 2022 00:00:00 GMT+0200 (heure d’été d’Europe centrale)';
    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.formatDateTime(dateValue);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('formatDateTime should return the correct DateFormat with hours, minutes and seconds', () => {
    // Given
    const expectedResult = '2022-10-18T15:03:14.000Z';
    const dateValue = 'Tue Oct 18 2022 15:03:14 GMT+0200 (heure d’été d’Europe centrale)';
    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.formatDateTime(dateValue);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct month', () => {
    // Given
    const expectedResult = '12';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getMonth(12);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct number of seconds', () => {
    // Given
    const expectedResult = '56';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getMinutes(56);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct number of minutes', () => {
    // Given
    const expectedResult = '56';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getMinutes(56);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct day', () => {
    // Given
    const expectedResult = '26';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getDay(26);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct hour', () => {
    // Given
    const expectedResult = '23';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getHour(23);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct month when month is less than 9', () => {
    // Given
    const expectedResult = '05';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getMonth(5);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct day when day is less than 9', () => {
    // Given
    const expectedResult = '09';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getDay(9);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct hour when hour is less than 9', () => {
    // Given
    const expectedResult = '07';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getHour(7);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct number of minutes when minutes is less than 9', () => {
    // Given
    const expectedResult = '02';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getMinutes(2);

    // Then
    expect(result).toEqual(expectedResult);
  });

  it('getMonth should return the correct number of seconds when seconds is less than 9', () => {
    // Given
    const expectedResult = '04';

    const pipe = new DateTimePipe(fake as DatePipe);

    // When
    const result = pipe.getSeconds(4);

    // Then
    expect(result).toEqual(expectedResult);
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });
});
