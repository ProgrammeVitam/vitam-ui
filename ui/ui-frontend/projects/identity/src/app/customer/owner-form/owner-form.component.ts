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
import { Component, effect, forwardRef, inject, input, OnChanges, OnInit, SimpleChanges, computed, signal, untracked } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';
import { form, FormField, maxLength, minLength, pattern, required } from '@angular/forms/signals';
import { CountryOption, CountryService, Customer, InputComponent, Option, Owner, SelectComponent, StartupService } from 'vitamui-library';
import {
  ALPHA_NUMERIC_REGEX,
  OWNER_CITY_MAX_LENGTH,
  OWNER_CODE_MAX_LENGTH,
  OWNER_CODE_MIN_LENGTH,
  OWNER_COMPANY_NAME_MAX_LENGTH,
  OWNER_INTERNAL_CODE_MAX_LENGTH,
  OWNER_NAME_MAX_LENGTH,
  OWNER_ZIP_CODE_MAX_LENGTH,
  OwnerFormValidators,
} from './owner-form.validators';
import { TranslatePipe } from '@ngx-translate/core';

export const OWNER_FORM_VALUE_ACCESSOR: any = {
  provide: NG_VALUE_ACCESSOR,

  useExisting: forwardRef(() => OwnerFormComponent),
  multi: true,
};

export interface OwnerFormModel {
  id: string;
  customerId: string;
  identifier: string;
  code: string;
  name: string;
  companyName: string;
  internalCode: string;
  address: {
    street: string;
    zipCode: string;
    city: string;
    country: string;
  };
  readonly: boolean;
}

function toOwnerFormModel(owner: Partial<Owner>, customerId: string): OwnerFormModel {
  if (!owner) {
    return {
      id: null,
      customerId,
      identifier: null,
      code: null,
      name: null,
      companyName: null,
      internalCode: null,
      address: { street: null, zipCode: null, city: null, country: 'FR' },
      readonly: false,
    };
  }
  return {
    id: owner.id ?? null,
    customerId: owner.customerId ?? null,
    identifier: owner.identifier ?? null,
    code: owner.code ?? null,
    name: owner.name ?? null,
    companyName: owner.companyName ?? null,
    internalCode: owner.internalCode ?? null,
    address: {
      street: owner.address?.street ?? null,
      zipCode: owner.address?.zipCode ?? null,
      city: owner.address?.city ?? null,
      country: owner.address?.country ?? null,
    },
    readonly: owner.readonly ?? null,
  };
}

@Component({
  selector: 'app-owner-form',
  templateUrl: './owner-form.component.html',
  styleUrls: ['./owner-form.component.scss'],
  providers: [OWNER_FORM_VALUE_ACCESSOR],
  imports: [FormField, InputComponent, SelectComponent, TranslatePipe],
})
export class OwnerFormComponent implements OnChanges, ControlValueAccessor, OnInit {
  private readonly ownerFormValidators = inject(OwnerFormValidators);
  private readonly countryService = inject(CountryService);
  private readonly startupService = inject(StartupService);

  public readonly maxStreetLength: number = this.startupService.getConfigNumberValue('MAX_STREET_LENGTH');
  public countries: Option[];

  readonly customerId = input<string>();

  readonly customerInfo = input<Pick<Customer, 'code' | 'name' | 'companyName'>>();

  private readonly model = signal<OwnerFormModel>(toOwnerFormModel(null, null));
  public readonly form = form(this.model, (path) => {
    required(path.code);
    pattern(path.code, ALPHA_NUMERIC_REGEX);
    maxLength(path.code, OWNER_CODE_MAX_LENGTH);
    minLength(path.code, OWNER_CODE_MIN_LENGTH);
    this.ownerFormValidators.uniqueCodeRule(path.code);
    required(path.name);
    maxLength(path.name, OWNER_NAME_MAX_LENGTH);
    required(path.companyName);
    maxLength(path.companyName, OWNER_COMPANY_NAME_MAX_LENGTH);
    maxLength(path.internalCode, OWNER_INTERNAL_CODE_MAX_LENGTH);
    maxLength(path.address.street, this.maxStreetLength);
    maxLength(path.address.zipCode, OWNER_ZIP_CODE_MAX_LENGTH);
    maxLength(path.address.city, OWNER_CITY_MAX_LENGTH);
  });

  // The owner is propagated to the parent only when the form is valid (null otherwise)
  private readonly value = computed(() => (this.form().pending() || this.form().invalid() ? null : this.model()));
  // Last value received from or sent to the parent
  private lastValue: OwnerFormModel = null;

  onChange = (_: any) => {};

  onTouched = () => {};

  constructor() {
    effect(() => {
      const value = this.value();
      untracked(() => {
        if (value !== this.lastValue) {
          this.lastValue = value;
          this.onChange(value);
        }
      });
    });
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['customerId']) {
      this.model.update((owner) => ({ ...owner, customerId: this.customerId() }));
    }
    if (changes['customerInfo']) {
      const customerInfo = this.customerInfo();
      if (customerInfo) {
        this.model.update((owner) => ({
          ...owner,
          code: customerInfo.code,
          name: customerInfo.name,
          companyName: customerInfo.companyName,
        }));
        if (this.form.code().invalid()) {
          this.form.code().markAsTouched();
        }
      }
    }
  }

  ngOnInit() {
    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((country) => ({ key: country.code, label: country.name }));
    });
  }

  writeValue(owner: Owner) {
    // Ignore the value we have just sent to the parent
    if (owner === this.lastValue) {
      return;
    }
    this.lastValue = owner as OwnerFormModel;
    this.form().reset(toOwnerFormModel(owner, this.customerId()));
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }
}
