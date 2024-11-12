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
import { Component, forwardRef, Input } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'vitamui-common-datepicker',
  template: `
    <div class="vitamui-input" [ngClass]="{ filled: !!value }" (click)="picker.open()">
      <span class="search-date-label">{{ label }}</span>
      <input matInput [matDatepicker]="picker" [ngModel]="value" (ngModelChange)="onChange($event)" [disabled]="disabled" />
      <i class="vitamui-icon vitamui-icon-calendar primary"></i>
      <mat-datepicker #picker></mat-datepicker>
    </div>
    <div class="vitamui-input-errors">
      <ng-content select="vitamui-common-field-error"></ng-content>
    </div>
  `,
  styleUrls: ['./datepicker.component.scss'],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DatepickerComponent),
      multi: true,
    },
  ],
})
export class DatepickerComponent implements ControlValueAccessor {
  @Input() label!: string;
  @Input() value: string;
  /**
   * When true:
   * - do not display time
   * - emits the date as a ISOString (without the time part)
   * When false:
   * - displays date and time
   * - emits a Date object (with time)
   */
  @Input() onlyDate = false;
  disabled = false;

  propagateChange = (_: any) => {};

  propagateTouched = (_: any) => {};

  writeValue(value: any): void {
    if (value instanceof Date) {
      this.value = value.toISOString();
    } else {
      this.value = value;
    }
  }

  registerOnChange(fn: any): void {
    this.propagateChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.propagateTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.disabled = isDisabled;
  }

  onChange(date: Date): void {
    const isoDatetime = date?.toISOString();

    if (this.onlyDate) {
      this.value = isoDatetime && isoDatetime.split('T')[0];
      this.propagateChange(this.value);
    } else {
      this.value = isoDatetime;
      this.propagateChange(date);
    }
  }
}
