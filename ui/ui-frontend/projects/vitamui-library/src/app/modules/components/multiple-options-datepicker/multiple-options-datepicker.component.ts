import { Component, ElementRef, forwardRef, HostBinding, HostListener, Injector, Input, OnInit, ViewChild } from '@angular/core';
import { FormControl, NG_VALUE_ACCESSOR, NgControl, Validators } from '@angular/forms';
import { PickerType } from './multiple-options-datepicker.interface';
import { DatePipe } from '@angular/common';
import { MatDatepicker } from '@angular/material/datepicker';
import { CustomValidators, DatePattern } from '../../object-editor/pattern.validator';
import { AbstractFormInputDirective } from '../../../../lib/components/abstract-form-input.directive';

export const MULTIPLE_OPTIONS_DATEPICKER_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => MultipleOptionsDatepickerComponent),
  multi: true,
};

@Component({
  selector: 'vitamui-common-multiple-options-datepicker',
  templateUrl: './multiple-options-datepicker.component.html',
  styleUrl: './multiple-options-datepicker.component.scss',
  providers: [MULTIPLE_OPTIONS_DATEPICKER_VALUE_ACCESSOR],
})
export class MultipleOptionsDatepickerComponent extends AbstractFormInputDirective implements OnInit {
  @Input() pickerType: PickerType = 'day';
  @Input() startView: MatDatepicker<Date>['startView'];
  @Input() label = 'DATE.DATE';

  // We store a value specific for the datepicker in order to store a Date object and not a String for datepicker to keep the currently selected value
  datePickerValue: Date;

  @HostBinding('class.vitamui-float')
  get labelFloat(): boolean {
    return !!this.control.value;
  }

  @ViewChild('vitamUIInput') private vitamUIInput: ElementRef;
  @ViewChild('datepicker') private datepicker: MatDatepicker<Date>;
  @ViewChild('hintArea') private hintArea: ElementRef;

  onChange = (_: any) => {};
  onTouched = () => {};

  @HostListener('click', ['$event.target'])
  onClick(target: HTMLElement) {
    if (!this.hintArea.nativeElement.contains(target)) {
      this.vitamUIInput.nativeElement.focus();
    }
  }

  private startViewMapping: Map<PickerType, MatDatepicker<Date>['startView']> = new Map([
    ['year', 'multi-year'],
    ['month', 'year'],
    ['day', 'month'],
  ]);

  private datePatternMapping = new Map<PickerType, DatePattern>([
    ['year', DatePattern.YEAR],
    ['month', DatePattern.YEAR_MONTH],
    ['day', DatePattern.YEAR_MONTH_DAY],
  ]);

  constructor(
    private datePipe: DatePipe,
    injector: Injector,
  ) {
    super(injector);
  }

  ngOnInit() {
    const ngControl: NgControl = this.injector.get(NgControl);
    this.control = ngControl.control as FormControl;
    if (!this.startView) this.startView = this.startViewMapping.get(this.pickerType);
    this.control.addValidators(CustomValidators.date(this.datePatternMapping.get(this.pickerType)));
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
    if (!this.control.disabled) {
      this.onTouched();
    }
  }

  onBlur() {
    this.onTouched();
  }

  onTextChange(value: string) {
    this.control.setValue(value);
    this.onChange(value);
  }

  setYearMonthAndDay(date: Date) {
    if (this.pickerType === 'day') {
      this.datePickerValue = date;
      this.control.setValue(this.datePipe.transform(date, 'yyyy-MM-dd'));
      this.onChange(this.control.value);
    }
  }

  setYear(year: Date) {
    if (this.pickerType === 'year') {
      this.datePickerValue = year;
      this.control.setValue(this.datePipe.transform(year, 'yyyy'));
      this.onChange(this.control.value);
      this.datepicker.close();
    }
  }

  setYearAndMonth(monthAndYear: Date) {
    if (this.pickerType === 'month') {
      this.datePickerValue = monthAndYear;
      this.control.setValue(this.datePipe.transform(monthAndYear, 'yyyy-MM'));
      this.onChange(this.control.value);
      this.datepicker.close();
    }
  }

  protected readonly Validators = Validators;
}
