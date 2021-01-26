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
/* tslint:disable:no-use-before-declare */

import { coerceBooleanProperty } from '@angular/cdk/coercion';
import { ENTER } from '@angular/cdk/keycodes';
import { AfterContentInit, Component, ContentChildren, forwardRef, Input, QueryList } from '@angular/core';
import { AsyncValidatorFn, ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR, ValidatorFn, Validators } from '@angular/forms';

import { VitamUIFieldErrorComponent } from '../vitamui-field-error/vitamui-field-error.component';


export const LIST_INPUT_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => VitamUIListInputComponent),
  multi: true
};

@Component({
  selector: 'vitamui-common-list-input',
  templateUrl: './vitamui-list-input.component.html',
  styleUrls: ['./vitamui-list-input.component.scss'],
  providers: [LIST_INPUT_ACCESSOR]
})
export class VitamUIListInputComponent implements AfterContentInit, ControlValueAccessor {

  @Input() placeholder: string;
  @Input() spinnerDiameter = 25;
  @Input()
  set validator(validator: ValidatorFn) {
    this.control.setValidators(Validators.compose([Validators.required, validator]));
    this.control.updateValueAndValidity({ emitEvent: false });
  }

  @Input()
  set asyncValidator(asyncValidator: AsyncValidatorFn) {
    this.control.setAsyncValidators(asyncValidator);
    this.control.updateValueAndValidity({ emitEvent: false });
  }
  @Input()
  get required(): boolean { return this._required; }
  set required(value: boolean) { this._required = coerceBooleanProperty(value); }
  // tslint:disable-next-line:variable-name
  private _required = false;

  @ContentChildren(VitamUIFieldErrorComponent) errors: QueryList<VitamUIFieldErrorComponent>;

  values: string[] = [];
  control: FormControl;
  separatorKeysCodes = [ENTER];

  onChange: (_: any) => void;
  onTouched: () => void;

  constructor() {
    this.control = new FormControl(
      null,
      [ Validators.required ]
    );
  }

  ngAfterContentInit() {
    this.control.statusChanges.subscribe(() => {
      this.errors.forEach((error: VitamUIFieldErrorComponent) => {
        error.show = this.control.errors ? !!this.control.errors[error.errorKey] : false;
      });
    });
  }

  writeValue(values: string[]) {
    this.values = (values || []).slice();
  }

  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }

  add(): void {
    if (this.control.invalid || this.control.pending) { return; }
    const val = this.control.value.trim();
    if (this.values.includes(val)) { return; }
    this.values.push(val);
    this.onChange(this.values);
    this.control.reset();
  }

  remove(val: string): void {
    const index = this.values.indexOf(val);

    if (index >= 0) {
      this.values.splice(index, 1);
      this.onChange(this.values);
    }
  }

  buttonAddDisabled(): boolean {
    return this.control.pending || this.control.invalid || this.valueExists;
  }

  get valueExists(): boolean {
    return this.values.includes((this.control.value || '').trim());
  }

}
