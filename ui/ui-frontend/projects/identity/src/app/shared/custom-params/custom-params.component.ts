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
/* eslint-disable @typescript-eslint/no-use-before-define */

import { ENTER } from '@angular/cdk/keycodes';
import { AfterContentInit, Component, ContentChildren, forwardRef, Input, QueryList } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR } from '@angular/forms';
import { VitamUIFieldErrorComponent } from 'vitamui-library';
/*eslint no-use-before-define: "error"*/
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
})
export class CustomParamsComponent implements AfterContentInit, ControlValueAccessor {
  @Input() keyPlaceholder: string;
  @Input() valuePlaceholder: string;
  @Input() spinnerDiameter = 25;

  @ContentChildren(VitamUIFieldErrorComponent) errors: QueryList<VitamUIFieldErrorComponent>;
  @Input() selectedValues: Map<string, string>;
  values = new Map<string, string>();
  controlKey: FormControl;
  controlValue: FormControl;
  separatorKeysCodes = [ENTER];
  onChange: (_: any) => void;
  onTouched: () => void;

  constructor() {
    this.controlKey = new FormControl(null);
    this.controlValue = new FormControl(null);
  }

  ngAfterContentInit() {
    this.controlKey.statusChanges.subscribe(() => {
      this.errors.forEach((error: VitamUIFieldErrorComponent) => {
        error.show = this.controlKey.errors ? !!this.controlKey.errors[error.errorKey] : false;
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
    if (this.controlKey.invalid || this.controlKey.pending) {
      return;
    }
    const key = this.controlKey.value?.trim();
    const value = this.controlValue.value?.trim();
    if (!!this.values.get(key) || !key || !value) {
      return;
    }
    this.values.set(key, value);
    this.onChange(this.strMapToObj(this.values));
    this.controlKey.reset();
    this.controlValue.reset();
  }

  remove(val: string): void {
    const elem = this.values.get(val);

    if (elem) {
      this.values.delete(val);
      this.onChange(this.strMapToObj(this.values));
    }
  }

  buttonAddDisabled(): boolean {
    return !this.controlValue.value?.trim() || !this.controlKey.value?.trim() || this.valueExists;
  }

  get valueExists(): boolean {
    if (!this.controlKey.value) {
      return true;
    }
    return !!this.values.get(this.controlKey.value);
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
