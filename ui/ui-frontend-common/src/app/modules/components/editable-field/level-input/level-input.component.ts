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
// tslint:disable:no-use-before-declare

import { coerceBooleanProperty } from '@angular/cdk/coercion';
import { Component, forwardRef, HostBinding, Input, OnInit } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { extractSubLevel } from '../../../utils';

export const LEVEL_INPUT_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => LevelInputComponent),
  multi: true
};

@Component({
  selector: 'vitamui-common-level-input',
  templateUrl: './level-input.component.html',
  styleUrls: ['./level-input.component.scss'],
  providers: [LEVEL_INPUT_VALUE_ACCESSOR]
})
export class LevelInputComponent implements OnInit, ControlValueAccessor {

  @Input() prefix: string;

  @Input()
  get isEditableComponent(): boolean { return this._isEditableComponent; }
  set isEditableComponent(value: boolean) { this._isEditableComponent = coerceBooleanProperty(value); }
  // tslint:disable-next-line:variable-name
  private _isEditableComponent = false;

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

  subLevel: string;

  @HostBinding('class.vitamui-focused') focused = false;
  @HostBinding('class.vitamui-float') labelFloat = false;

  onChange: (_: any) => void;
  onTouched: () => void;

  constructor() { }

  ngOnInit() {
  }

  writeValue(level: string): void {
    this.labelFloat = !!this.subLevel;
    this.subLevel = extractSubLevel(this.prefix, level);
  }

  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this._disabled = isDisabled;
  }

  onFocus() {
    this.focused = true;
  }

  onBlur() {
    this.focused = false;
    this.onTouched();
  }

  onValueChange(_: string) {
    this.labelFloat = !!this.subLevel;

    const level = this.prefix ? this.prefix + '.' +  this.subLevel :  this.subLevel ;

    this.onChange(level.toLocaleUpperCase());
  }

}
