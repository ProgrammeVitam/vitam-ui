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

import { Component, ElementRef, forwardRef, Input, OnInit, ViewChild } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR } from '@angular/forms';
import { merge, Observable, Subject } from 'rxjs';
import { map, startWith } from 'rxjs/operators';
import { Option } from './option.interface';

export const VITAMUI_AUTOCOMPLETE_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => VitamUIAutocompleteComponent),
  multi: true
};

@Component({
  selector: 'vitamui-common-vitamui-autocomplete',
  templateUrl: './vitamui-autocomplete.component.html',
  styleUrls: ['./vitamui-autocomplete.component.scss'],
  providers: [VITAMUI_AUTOCOMPLETE_VALUE_ACCESSOR]
})
export class VitamUIAutocompleteComponent implements ControlValueAccessor, OnInit {

  @Input()
  set options(options: Option[]) {
    this._options = options.sort((a, b) => a.label.toLocaleLowerCase() > b.label.toLocaleLowerCase() ? 1 : -1);
    this.optionsChanges.next(this._options);

    if (this._options.length === 1) {
      // Re-setting the value so the input shows the updated option's label.
      this.control.setValue(this.control.value, { emitEvent: false });
    } else {
      this.control.reset(null, { emitEvent: false });
    }
    this.updatePlaceholderPosition();
  }
  get options(): Option[] { return this._options; }
  // tslint:disable-next-line:variable-name
  private _options: Option[] = [];

  @Input() placeholder: string;

  @ViewChild('input', { static: true }) inputElement: ElementRef;

  control = new FormControl('');
  optionsChanges = new Subject<Option[]>();
  filteredOptions: Observable<Option[]>;
  labelUp = false;

  private focused = false;

  constructor() { }

  ngOnInit() {
    const valueChanges = this.control.valueChanges
      .pipe(
        startWith<string | Option>(''),
        map((value) => {
          if (value) {
            return typeof value === 'string' ? value : value.label;
          }

          return '';
        }),
        map((name) => name ? this.filter(name) : this.options.slice()),
      );
    this.filteredOptions = merge(valueChanges, this.optionsChanges);
    this.control.valueChanges.subscribe((value) => this.onChange(value));
    this.control.valueChanges.subscribe(() => this.updatePlaceholderPosition());
  }

  onChange = (_: any) => {};
  onTouched = () => {};

  writeValue(value: string) {
    this.control.setValue(value);
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  filter(name: string): Option[] {
    return this.options.filter((option) => {
      return option.label ? option.label.toLowerCase().indexOf(name.toLowerCase()) === 0 : false;
    });
  }

  displayFn(options: Option[]): (value: string) => string | undefined {
    return (key: string) => {
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
    this.updatePlaceholderPosition();
  }

  onFocus() {
    this.focused = true;
    this.updatePlaceholderPosition();
  }

}
