/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
/* tslint:disable: no-use-before-declare */

import { coerceBooleanProperty } from '@angular/cdk/coercion';
import { Component, ElementRef, forwardRef, HostListener, Input, OnChanges, OnInit, SimpleChanges, ViewChild } from '@angular/core';
import {
  AbstractControl,
  ControlValueAccessor,
  FormControl,
  NG_VALIDATORS,
  NG_VALUE_ACCESSOR,
  ValidationErrors,
  Validator,
} from '@angular/forms';
import { MatAutocompleteSelectedEvent, MatAutocompleteTrigger } from '@angular/material/autocomplete';
import { isEqual } from 'lodash';
import { merge, Observable, Subject } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { Option } from '../utils/option.interface';

export const VITAMUI_AUTOCOMPLETE_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => VitamUIAutocompleteComponent),
  multi: true,
};

export const VITAMUI_AUTOCOMPLETE_NG_VALIDATORS: any = {
  provide: NG_VALIDATORS,
  useExisting: forwardRef(() => VitamUIAutocompleteComponent),
  multi: true,
};

@Component({
  selector: 'vitamui-common-vitamui-autocomplete',
  templateUrl: './vitamui-autocomplete.component.html',
  styleUrls: ['./vitamui-autocomplete.component.scss'],
  providers: [VITAMUI_AUTOCOMPLETE_VALUE_ACCESSOR, VITAMUI_AUTOCOMPLETE_NG_VALIDATORS],
})
export class VitamUIAutocompleteComponent implements ControlValueAccessor, OnInit, Validator, OnChanges {
  @ViewChild('input', { read: ElementRef, static: true }) inputElement: ElementRef;
  @ViewChild('input', { read: MatAutocompleteTrigger }) autoComplete: MatAutocompleteTrigger;

  @Input() placeholder: string;
  public control = new FormControl('');
  public filteredOptions: Observable<Option[]>;
  public labelUp = false;
  public autocompleteLabel: string;
  private focused = false;
  private optionsChanges = new Subject<Option[]>();

  constructor() {
    /* Empty constructor */
  }

  // tslint:disable-next-line:variable-name
  private _options: Option[] = [];

  get options(): Option[] {
    return this._options;
  }

  @Input()
  set options(options: Option[]) {
    this._options = options;
    this.optionsChanges.next(this._options);
  }

  // tslint:disable-next-line: variable-name
  private _customSorting: (a: Option, b: Option) => number;

  get customSorting(): (a: Option, b: Option) => number {
    return this._customSorting;
  }

  @Input()
  set customSorting(customSorting: (a: Option, b: Option) => number) {
    this._customSorting = customSorting;
    this.optionsChanges.next(this._options);
  }

  // tslint:disable-next-line:variable-name
  private _required = false;

  @Input()
  get required(): boolean {
    return this._required;
  }
  // tslint:disable-next-line: variable-name

  set required(value: boolean) {
    this._required = coerceBooleanProperty(value);
  }

  ngOnChanges(changes: SimpleChanges): void {
    // if a new data input is setted for options, we update formcontrol value
    // (because when we set a new value, it may not be in the options list and it will not be displayed)
    if (changes?.options && !isEqual(changes?.options?.currentValue, changes?.options?.previousValue)) {
      this.control.setValue(this.control.value, { emitEvent: false });
      this.onChange(this.control.value);
      this.updatePlaceholderPosition();
    }
  }

  ngOnInit() {
    const valueChanges = this.control.valueChanges.pipe(
      startWith<string | Option>(''),
      map((value) => {
        if (value) {
          return typeof value === 'string' ? value : value.label;
        }

        return '';
      }),
      map((name) => (name ? this.filter(name) : this.options.slice())),
    );

    this.filteredOptions = merge(valueChanges, this.optionsChanges.pipe(map((options) => this.sortedOption(options))));
  }

  @HostListener('window:scroll', ['$event'])
  scrollEvent(event: any): void {
    if (this.autoComplete.panelOpen) {
      this.autoComplete.updatePosition();
    }
  }

  inputChange(value: string) {
    this.onChange(this.findKeyByLabel(value));
    this.updatePlaceholderPosition();
  }

  selectionChange(event: MatAutocompleteSelectedEvent) {
    this.onChange(event?.option?.value);
    this.autocompleteLabel = this.findLabelByKey(event?.option?.value);
    if (this.inputElement.nativeElement.scrollWidth <= this.inputElement.nativeElement.offsetWidth) {
      this.autocompleteLabel = '';
    }
    this.updatePlaceholderPosition();
    this.inputElement.nativeElement.setSelectionRange(0, 0);
    this.inputElement.nativeElement.focus();
    setTimeout(() => {
      this.inputElement.nativeElement.blur();
    }, 200);
  }

  writeValue(value: string) {
    this.control.setValue(value);
    this.updatePlaceholderPosition();
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  filter(name: string): Option[] {
    return this.options.filter((option) => {
      return option.label ? option.label.toLowerCase().indexOf(name.toLowerCase()) !== -1 : false;
    });
  }

  displayFn(options: Option[]): (value: string) => string | undefined {
    return (key: any) => {
      const selected = options.find((option) => option.key === key);
      return selected ? selected.label : undefined;
    };
  }

  focus() {
    if (this.inputElement) {
      this.inputElement.nativeElement.focus();
    }
  }

  setDisabledState(disabled: boolean) {
    if (disabled) {
      this.control.disable({ emitEvent: false });
    } else {
      this.control.enable({ emitEvent: false });
    }
  }

  updatePlaceholderPosition() {
    this.labelUp = !!this.control.value || this.focused;
  }

  onBlur() {
    this.focused = false;
    this.onTouched();
    this.updatePlaceholderPosition();
  }

  onFocus() {
    this.focused = true;
    this.optionsChanges.next(this._options);
    this.updatePlaceholderPosition();
  }

  validate(control: AbstractControl): ValidationErrors | null {
    if (!this.control.value || this.control.hasError('required')) {
      return null;
    }

    const isOptionValid = this.isOptionValid(control.value);
    return isOptionValid ? null : { match: true };
  }

  private sortedOption(options: Option[]): Option[] {
    if (this._customSorting) {
      options?.sort(this._customSorting);
    } else {
      options?.sort((a, b) => (a.label.toLocaleLowerCase() > b.label.toLocaleLowerCase() ? 1 : -1));
    }
    return options;
  }

  private isOptionValid(value: string): boolean {
    return this._options.filter((option) => option.key === value).length > 0;
  }

  private findKeyByLabel(value: string): string {
    return this._options.find((option) => option.label === value)?.key || value;
  }

  private findLabelByKey(value: any): string {
    return this._options.find((option) => option.key === value)?.label;
  }

  private onChange = (_: any) => {};
  private onTouched = () => {};
}
