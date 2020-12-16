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
import { Component, ElementRef, forwardRef, HostBinding, HostListener, Input, ViewChild } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

export const VITAMUI_INPUT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => VitamUIInputPositiveNumberComponent),
  multi: true
};

@Component({
  selector: 'vitamui-common-input-positive-number',
  templateUrl: './vitamui-input-positive-number.component.html',
  styleUrls: ['./vitamui-input-positive-number.component.scss'],
  providers: [VITAMUI_INPUT_VALUE_ACCESSOR]
})
export class VitamUIInputPositiveNumberComponent implements ControlValueAccessor {

  @Input() type = 'text';
  @Input() maxlength: number;
  @Input() min: number;
  @Input() placeholder: string;
  @Input() autofocus: boolean;
  @Input()
  get required(): boolean { return this._required; }
  set required(value: boolean) { this._required = coerceBooleanProperty(value); }
  // tslint:disable-next-line:variable-name
  private _required = false;

  @Input()
  get disabled(): boolean { return this._disabled; }
  set disabled(value: boolean) { this._disabled = coerceBooleanProperty(value); }
  // tslint:disable-next-line:variable-name
  private _disabled = false;
  @ViewChild('vitamUIInputPositiveNumber') private input: ElementRef;

  @HostBinding('class.vitamui-focused') focused = false;
  @HostBinding('class.vitamui-float') labelFloat = false;

  value: string | number;

  onChange = (_: any) => { };
  onTouched = () => { };
  onKeyPress = (_: any) => {};

  @HostListener('click')
  onClick() {
    this.input.nativeElement.focus();
  }

  writeValue(value: string | number) {
    this.value = value;
    this.labelFloat = !!this.value;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  onTextChange(value: string) {
    this.labelFloat = !!this.value;
    this.onChange(value);
  }

  onNumberChange(value: number) {
    const badInput = this.input.nativeElement.validity.badInput;
    this.labelFloat = badInput || this.value !== null;
    this.onChange(value);
  }

  onValueChange(value: string) {
    if (this.type !== 'number' || value === '') {
      this.onTextChange(value);
    } else {
      this.onNumberChange(Number(value));
    }
  }

  onFocus() {
    this.focused = true;
  }

  onBlur() {
    this.focused = false;
    this.onTouched();
  }

  setDisabledState(isDisabled: boolean) {
    this.disabled = isDisabled;
  }

}
