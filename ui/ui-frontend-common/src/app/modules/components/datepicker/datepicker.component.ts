import { Component, Input, forwardRef } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'vitamui-common-datepicker',
  template: `
    <div class="vitamui-input" [ngClass]="{ filled: !!value }" (click)="picker.open()">
      <span class="search-date-label">{{ label }}</span>
      <input matInput [matDatepicker]="picker" [(ngModel)]="value" (ngModelChange)="onChange($event)" [disabled]="disabled" />
      <i class="vitamui-icon vitamui-icon-calendar primary"></i>
      <mat-datepicker #picker></mat-datepicker>
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
  @Input() value!: string;

  propagateChange = (_: any) => {};
  propagateTouched = (_: any) => {};

  disabled = false;

  writeValue(value: any): void {
    if (value instanceof Date) this.value = value.toISOString();
    else this.value = value;
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

  onChange(value: Date): void {
    this.value = value.toISOString();
    this.propagateChange(this.value);
  }
}
