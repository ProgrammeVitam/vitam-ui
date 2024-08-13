import { Component, ElementRef, forwardRef, HostBinding, HostListener, Injector, Input, OnInit, ViewChild } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR, NgControl, Validators } from '@angular/forms';
import { PickerType } from './multiple-options-datepicker.interface';
import { DatePipe } from '@angular/common';
import { MatDatepicker } from '@angular/material/datepicker';

export const MULTIPLE_OPTIONS_DATEPICKER_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => MultipleOptionsDatepickerComponent),
  multi: true,
};

const startViewMapping: Map<PickerType, MatDatepicker<Date>['startView']> = new Map([
  ['year', 'multi-year'],
  ['month', 'year'],
  ['day', 'month'],
]);

@Component({
  selector: 'vitamui-common-multiple-options-datepicker',
  templateUrl: './multiple-options-datepicker.component.html',
  styleUrl: './multiple-options-datepicker.component.scss',
  providers: [MULTIPLE_OPTIONS_DATEPICKER_VALUE_ACCESSOR],
})
export class MultipleOptionsDatepickerComponent implements ControlValueAccessor, OnInit {
  @Input() pickerType: PickerType = 'day';
  @Input() startView: MatDatepicker<Date>['startView'];
  @Input() required = false;

  date: FormControl;
  // We store a value specific for the datepicker in order to store a Date object and not a String for datepicker to keep the currently selected value
  datePickerValue: Date;

  @HostBinding('class.vitamui-float')
  get labelFloat(): boolean {
    return !!this.date.value;
  }

  @ViewChild('vitamUIInput') private vitamUIInput: ElementRef;
  @ViewChild('datepicker') private datepicker: MatDatepicker<Date>;
  @ViewChild('hintArea') private hintArea: ElementRef;

  monthYearRegExp = new RegExp('^([1-9]\\d{3})-(0[1-9]|1[0-2]$)');
  yearRegExp = new RegExp('^([1-9]\\d{3})$');

  onChange = (_: any) => {};
  onTouched = () => {};

  @HostListener('click', ['$event.target'])
  onClick(target: HTMLElement) {
    if (!this.hintArea.nativeElement.contains(target)) {
      this.vitamUIInput.nativeElement.focus();
    }
  }

  constructor(
    private datePipe: DatePipe,
    public injector: Injector,
  ) {}

  ngOnInit() {
    const ngControl: NgControl = this.injector.get(NgControl);
    this.date = ngControl.control as FormControl;

    if (!this.startView) this.startView = startViewMapping.get(this.pickerType);

    if (this.pickerType === 'year') {
      this.date.addValidators(Validators.pattern(this.yearRegExp));
    } else if (this.pickerType === 'month') {
      this.date.addValidators(Validators.pattern(this.monthYearRegExp));
    } else {
      this.date.addValidators((control) => {
        function isValidDate(dateString: string) {
          if (!/^\d*-\d*-\d*$/.test(dateString)) {
            return false; // Invalid format
          }

          // Step 1: Split the string into components
          const parts = dateString.split('-');

          // Step 2: Convert to integers
          const year = parseInt(parts[0], 10);
          const month = parseInt(parts[1], 10) - 1; // Months are zero-based (0 = January, 11 = December)
          const day = parseInt(parts[2], 10);

          // Step 3: Create a Date object
          const date = new Date(year, month, day);

          // Step 4: Check for validity
          if (date.getFullYear() !== year || date.getMonth() !== month || date.getDate() !== day) {
            return false; // The date is not valid
          }

          return true; // The date is valid
        }
        return isValidDate(control.value) ? null : { pattern: true };
      });
    }
  }

  writeValue(value: string) {
    this.datePickerValue = value ? new Date(value) : new Date();
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  onFocus() {
    if (!this.date.disabled) {
      this.onTouched();
    }
  }

  onBlur() {
    this.onTouched();
  }

  onTextChange(value: string) {
    this.date.setValue(value);
    this.onChange(value);
  }

  setYearMonthAndDay(date: Date) {
    if (this.pickerType === 'day') {
      this.datePickerValue = date;
      this.date.setValue(this.datePipe.transform(date, 'yyyy-MM-dd'));
      this.onChange(this.date.value);
    }
  }

  setYear(year: Date) {
    if (this.pickerType === 'year') {
      this.datePickerValue = year;
      this.date.setValue(this.datePipe.transform(year, 'yyyy'));
      this.onChange(this.date.value);
      this.datepicker.close();
    }
  }

  setYearAndMonth(monthAndYear: Date) {
    if (this.pickerType === 'month') {
      this.datePickerValue = monthAndYear;
      this.date.setValue(this.datePipe.transform(monthAndYear, 'yyyy-MM'));
      this.onChange(this.date.value);
      this.datepicker.close();
    }
  }
}
