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


import { ENTER } from '@angular/cdk/keycodes';
import { Component, EventEmitter, forwardRef, Input, Output } from '@angular/core';
import { ControlValueAccessor, FormControl, NG_VALUE_ACCESSOR, Validators } from '@angular/forms';

import { CustomerCreateValidators } from '../../customer/customer-create/customer-create.validators';

export const DOMAINS_INPUT_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,
  useExisting: forwardRef(() => DomainsInputComponent),
  multi: true
};

@Component({
  selector: 'app-domains-input',
  templateUrl: './domains-input.component.html',
  styleUrls: ['./domains-input.component.scss'],
  providers: [DOMAINS_INPUT_ACCESSOR]
})
export class DomainsInputComponent implements ControlValueAccessor {

  @Input() placeholder: string;
  @Input() selected: string;
  @Input() spinnerDiameter = 25;

  @Output() selectedChange = new EventEmitter<string>();

  domains: string[] = [];
  control: FormControl;
  separatorKeysCodes = [ENTER];

  onChange: (_: any) => void;
  onTouched: () => void;

  constructor(private customerCreateValidators: CustomerCreateValidators) {
    this.control = new FormControl(
      null,
      [ Validators.required, Validators.pattern(/^\s*([a-z0-9]+(-[a-z0-9]+)*\.)+[a-z]{2,}\s*$/) ],
      this.customerCreateValidators.uniqueDomain
    );
  }

  writeValue(domains: string[]) {
    this.domains = (domains || []).slice();
  }

  registerOnChange(fn: (_: any) => void) {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void) {
    this.onTouched = fn;
  }

  add(): void {
    if (this.control.invalid || this.control.pending) { return; }
    const domain = this.control.value.trim();
    if (this.domains.includes(domain)) { return; }
    this.domains.push(domain);
    this.onChange(this.domains);
    this.control.reset();
    if (!this.selected) {
      this.setSelected(domain);
    }
  }

  remove(domain: string): void {
    const index = this.domains.indexOf(domain);

    if (index >= 0) {
      if (domain === this.selected) {
        this.setSelected(null);
      }
      this.domains.splice(index, 1);
      this.onChange(this.domains);
    }
  }

  setSelected(value: string) {
    this.selected = value;
    this.selectedChange.emit(this.selected);
  }

  buttonAddDisabled(): boolean {
    return this.control.pending || this.control.invalid || this.domainExists;
  }

  get domainExists(): boolean {
    return this.domains.includes((this.control.value || '').trim());
  }

}
