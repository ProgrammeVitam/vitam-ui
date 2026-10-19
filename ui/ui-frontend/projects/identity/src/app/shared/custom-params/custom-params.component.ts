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
import { ENTER } from '@angular/cdk/keycodes';
import { Component, forwardRef, input, contentChildren, effect, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { form, FormField } from '@angular/forms/signals';
import { InputComponent, toValidationErrors, VitamUIFieldErrorComponent } from 'vitamui-library';
import { KeyValuePipe } from '@angular/common';
import { TranslatePipe } from '@ngx-translate/core';

export const LIST_INPUT_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,

  useExisting: forwardRef(() => CustomParamsComponent),
  multi: true,
};

@Component({
  selector: 'app-custom-params',
  templateUrl: './custom-params.component.html',
  styleUrls: ['./custom-params.component.scss'],
  providers: [LIST_INPUT_ACCESSOR],
  imports: [InputComponent, FormField, KeyValuePipe, TranslatePipe],
})
export class CustomParamsComponent implements ControlValueAccessor {
  readonly keyPlaceholder = input<string>(undefined);
  readonly valuePlaceholder = input<string>(undefined);
  readonly spinnerDiameter = input(25);

  readonly errors = contentChildren(VitamUIFieldErrorComponent);
  readonly selectedValues = input<Map<string, string>>(undefined);
  values = new Map<string, string>();
  readonly paramForm = form(signal<{ key: string; value: string }>({ key: null, value: null }));
  separatorKeysCodes = [ENTER];
  onChange: (_: any) => void;
  onTouched: () => void;

  constructor() {
    effect(() => {
      const keyErrors = toValidationErrors(this.paramForm.key().errors());
      this.errors().forEach((error: VitamUIFieldErrorComponent) => {
        error.show = keyErrors ? !!keyErrors[error.errorKey()] : false;
      });
    });
  }

  writeValue(values: any) {
    if (values !== null && values !== undefined)
      Object.keys(values).forEach((key) => {
        this.values.set(key, values[key]);
      });
  }

  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }

  add(): void {
    const keyField = this.paramForm.key();
    if (keyField.invalid() || keyField.pending()) {
      return;
    }
    const key = keyField.value()?.trim();
    const value = this.paramForm.value().value()?.trim();
    if (!!this.values.get(key) || !key || !value) {
      return;
    }
    this.values.set(key, value);
    this.onChange(this.strMapToObj(this.values));
    this.paramForm().reset({ key: null, value: null });
  }

  remove(val: string): void {
    const elem = this.values.get(val);

    if (elem) {
      this.values.delete(val);
      this.onChange(this.strMapToObj(this.values));
    }
  }

  buttonAddDisabled(): boolean {
    return !this.paramForm.value().value()?.trim() || !this.paramForm.key().value()?.trim() || this.valueExists;
  }

  get valueExists(): boolean {
    const key = this.paramForm.key().value();
    if (!key) {
      return true;
    }
    return !!this.values.get(key);
  }

  private strMapToObj(strMap: Map<string, string>) {
    const obj = Object.create(null);
    for (const [k, v] of strMap) {
      // We don’t escape the key '__proto__'
      // which can cause problems on older engines
      obj[k] = v;
    }
    return obj;
  }
}
