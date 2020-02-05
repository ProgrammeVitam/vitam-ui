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

export const VITAMUI_TEXTAREA_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => VitamUITextareaComponent),
  multi: true
};

@Component({
  selector: 'vitamui-common-textarea',
  templateUrl: './vitamui-textarea.component.html',
  styleUrls: ['./vitamui-textarea.component.scss'],
  providers: [VITAMUI_TEXTAREA_VALUE_ACCESSOR]
})
export class VitamUITextareaComponent implements ControlValueAccessor {

  @Input() maxlength: number;
  @Input() placeholder: string;
  @Input() rows = 2;
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
  @ViewChild('textarea', { static: true }) private textarea: ElementRef;

  @HostBinding('class.vitamui-focused') focused = false;
  @HostBinding('class.vitamui-float') labelFloat = false;

  value: string | number;

  onChange = (_: any) => {};
  onTouched = () => {};

  @HostListener('click')
  onClick() {
    this.textarea.nativeElement.focus();
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

  onValueChange(value: string | number) {
    this.labelFloat = !!this.value;
    this.onChange(value);
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
