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
import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'dateTime',
})
export class DateTimePipe implements PipeTransform {
  constructor(private datePipe: DatePipe) {}

  transform(value?: string | Date | undefined, format?: string): string | null {
    return this.datePipe.transform(this.appendZIfNoTimezone(value), format);
  }

  /**
   * If the value is a Date instance, returns a String expressed in timezone Z.
   * If the value is a String, appends a Z (UTC) timezone if it has no timezone already.
   * This pipe is useful for "dates" that are instances of String and with no defined timezone. When Vitam API sends that kind of date, it should be interpreted in the UTC timezone.
   */
  private appendZIfNoTimezone(value?: string | Date | undefined): string | null | undefined {
    if (!value) return value as string;
    if (value instanceof Date) return value.toISOString();
    const hasTimezone = /Z$|[+-]\d{2}:\d{2}$|GMT[+-]\d{4}$/.test(value);
    return hasTimezone ? value : value + 'Z';
  }
}
