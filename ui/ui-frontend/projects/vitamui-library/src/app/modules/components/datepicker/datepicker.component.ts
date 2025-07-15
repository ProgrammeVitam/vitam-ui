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
import { Component, ElementRef, forwardRef, HostBinding, HostListener, Injector, Input, ViewChild, OnInit } from '@angular/core';
import { FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule, Validators } from '@angular/forms';
import { PickerType } from './datepicker.interface';
import { CommonModule, DatePipe } from '@angular/common';
import { MatDatepicker, MatDatepickerModule } from '@angular/material/datepicker';
import { CustomValidators, DatePattern } from '../../object-editor/pattern.validator';
import { AbstractFormInputDirective } from '../../../../lib/components/abstract-form-input.directive';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { TranslateModule } from '@ngx-translate/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormErrorsComponent } from '../../../../lib/components/form-errors/form-errors.component';

export const DATEPICKER_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => DatepickerComponent),
  multi: true,
};

@Component({
  selector: 'vitamui-datepicker',
  templateUrl: './datepicker.component.html',
  styleUrl: './datepicker.component.scss',
  providers: [DATEPICKER_VALUE_ACCESSOR],
  imports: [
    CommonModule,
    FormErrorsComponent,
    FormsModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    ReactiveFormsModule,
    TranslateModule,
  ],
})
export class DatepickerComponent extends AbstractFormInputDirective implements OnInit {
  @Input() pickerType: PickerType = 'day';
  @Input() startView: MatDatepicker<Date>['startView'];
  @Input() label = 'DATE.DATE';
  @Input() hint: string;
  @Input() min?: Date;
  @Input() max?: Date;

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
    injector: Injector,
    private datePipe: DatePipe,
  ) {
    super(injector);
  }

  ngOnInit() {
    super.ngOnInit();

    if (!this.startView) this.startView = this.startViewMapping.get(this.pickerType);
    this.control.addValidators(CustomValidators.date(this.datePatternMapping.get(this.pickerType)));
  }

  writeValue(value: string) {
    this.datePickerValue = value ? new Date(value) : null;
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
