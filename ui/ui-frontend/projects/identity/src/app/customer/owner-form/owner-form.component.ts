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

import { Component, forwardRef, Input, OnDestroy, OnInit } from '@angular/core';
import { ControlValueAccessor, FormBuilder, FormGroup, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';
import { merge } from 'rxjs';
import { distinctUntilChanged, map } from 'rxjs/operators';
import { Customer } from 'ui-frontend-common';

import { Owner } from 'ui-frontend-common';
import { OwnerFormValidators } from './owner-form.validators';

export const OWNER_FORM_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => OwnerFormComponent),
  multi: true
};

@Component({
  selector: 'app-owner-form',
  templateUrl: './owner-form.component.html',
  styleUrls: ['./owner-form.component.scss'],
  providers: [OWNER_FORM_VALUE_ACCESSOR]
})
export class OwnerFormComponent implements ControlValueAccessor, OnDestroy, OnInit {

  form: FormGroup;

  private sub: any;

  @Input()
  set customerId(customerId: string) {
    this._customerId = customerId;
    if (!this.form) { return; }
    this.form.get('customerId').setValue(customerId);
  }

  get customerId() { return this._customerId; }

  private _customerId: string;

  @Input()
  set customerInfo(customerInfo: Customer) {
    this._customerInfo = customerInfo;
    if (customerInfo && this.form) {
      this.form.patchValue({
          code: customerInfo.code,
          name: customerInfo.name,
          companyName: customerInfo.companyName,
        });
    }
  }

  get customerInfo() { return this._customerInfo; }

  private _customerInfo: any;

  constructor(private formBuilder: FormBuilder, private ownerFormValidators: OwnerFormValidators) {}

  onChange = (_: any) => {};

  onTouched = () => {};

  ngOnInit() {
    this.form = this.formBuilder.group({
      id: null,
      customerId: [null],
      identifier: null,
      code: [
        null,
        [Validators.required, Validators.pattern(/^[0-9]{6,20}$/)],
        this.ownerFormValidators.uniqueCode(),
      ],
      name: [null, Validators.required],
      companyName: [null, Validators.required],
      internalCode: [null],
      address: this.formBuilder.group({
        street: null,
        zipCode: null,
        city: null,
        country: 'FR',
      }),
      readonly: false
    });

    this.subscribeToValueChanges();
  }

  ngOnDestroy() {
    this.sub.unsubscribe();
  }

  writeValue(owner: Owner) {
    this.sub.unsubscribe();

    this.form.reset(owner || {
      customerId: this.customerId,
      code: null,
      name: null,
      companyName: null,
      internalCode: null,
      address: {
        street: null,
        zipCode: null,
        city: null,
        country: 'FR'
      },
      readonly: false
    });

    this.subscribeToValueChanges();
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  subscribeToValueChanges() {
    this.sub = merge(this.form.statusChanges, this.form.valueChanges)
      .pipe(
        map(() => this.form.pending || this.form.invalid ? null : this.form.value),
        distinctUntilChanged()
      )
      .subscribe((value) => this.onChange(value));
  }

}
