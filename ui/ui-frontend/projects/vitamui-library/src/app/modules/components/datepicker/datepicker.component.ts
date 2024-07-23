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
