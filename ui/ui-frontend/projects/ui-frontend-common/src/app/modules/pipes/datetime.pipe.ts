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

  transform(value: any, format?: string, local?: string): any {
    if (value) {
      value = this.formatDateTime(value);
      return this.datePipe.transform(value, format, this.getTimezone(), local);
    }
  }

  private getTimezone(): string {
    const hours = (new Date().getTimezoneOffset() / 60) * -1;
    let timezone = 'UTC';

    if (hours < 0) {
      timezone = 'UTC' + hours;
    } else if (hours > 0) {
      timezone = 'UTC+' + hours;
    }
    return timezone;
  }

  formatDateTime(value): string {
    const day = this.getDay(new Date(value).getDate());
    const year = new Date(value).getFullYear();
    const month = this.getMonth(new Date(value).getMonth() + 1);
    const hour = this.getHour(new Date(value).getHours());
    const minutes = this.getMinutes(new Date(value).getMinutes());
    const seconds = this.getSeconds(new Date(value).getSeconds());
    return year.toString() + '-' + month + '-' + day + 'T' + hour + ':' + minutes + ':' + seconds + '.000Z';
  }

  getMonth(num: number): string {
    if (num > 9) {
      return num.toString();
    } else {
      return '0' + num.toString();
    }
  }

  getDay(day: number): string {
    if (day > 9) {
      return day.toString();
    } else {
      return '0' + day.toString();
    }
  }

  getHour(hour: number): string {
    if (hour > 9) {
      return hour.toString();
    } else {
      return '0' + hour.toString();
    }
  }

  getSeconds(seconds: number): string {
    if (seconds > 9) {
      return seconds.toString();
    } else {
      return '0' + seconds.toString();
    }
  }

  getMinutes(minutes: number): string {
    if (minutes > 9) {
      return minutes.toString();
    } else {
      return '0' + minutes.toString();
    }
  }
}
