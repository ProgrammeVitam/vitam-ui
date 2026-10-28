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
import { Component, forwardRef, input, output, linkedSignal, signal } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { form, FormField, pattern, required } from '@angular/forms/signals';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TranslatePipe } from '@ngx-translate/core';

export const DOMAINS_INPUT_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,

  useExisting: forwardRef(() => DomainsInputComponent),
  multi: true,
};

@Component({
  selector: 'app-domains-input',
  templateUrl: './domains-input.component.html',
  styleUrls: ['./domains-input.component.scss'],
  providers: [DOMAINS_INPUT_ACCESSOR],
  imports: [FormField, MatProgressSpinner, TranslatePipe],
})
export class DomainsInputComponent implements ControlValueAccessor {
  readonly placeholder = input<string>(undefined);
  readonly selected = input<string>(undefined);
  protected readonly selectedState = linkedSignal(() => this.selected());
  readonly spinnerDiameter = input(25);

  readonly selectedChange = output<string>();

  domains: string[] = [];
  readonly domainForm = form(signal({ domain: '' }), (path) => {
    required(path.domain);
    pattern(path.domain, /^\s*([a-z0-9]+(-[a-z0-9]+)*\.)+[a-z]{2,}\s*$/);
  });
  separatorKeysCodes = [ENTER];

  onChange: (_: any) => void;
  onTouched: () => void;

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
    const domainField = this.domainForm.domain();
    if (domainField.invalid() || domainField.pending()) {
      return;
    }
    const domain = domainField.value().trim();
    if (this.domains.includes(domain)) {
      return;
    }
    // A new array is emitted so that signal forms detect the change
    this.domains = [...this.domains, domain];
    this.onChange(this.domains);
    this.domainForm().reset({ domain: '' });
    if (!this.selectedState()) {
      this.setSelected(domain);
    }
  }

  remove(domain: string): void {
    const index = this.domains.indexOf(domain);

    if (index >= 0) {
      if (domain === this.selectedState()) {
        this.setSelected(null);
      }
      this.domains = this.domains.filter((_, i) => i !== index);
      this.onChange(this.domains);
    }
  }

  setSelected(value: string) {
    this.selectedState.set(value);
    this.selectedChange.emit(this.selectedState());
  }

  buttonAddDisabled(): boolean {
    const domainField = this.domainForm.domain();
    return domainField.pending() || domainField.invalid() || this.domainExists;
  }

  get domainExists(): boolean {
    return this.domains.includes((this.domainForm.domain().value() || '').trim());
  }

  hasError(kind: string): boolean {
    return this.domainForm
      .domain()
      .errors()
      .some((error) => error.kind === kind);
  }
}
