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
import { Component, computed, inject, OnChanges, OnInit, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { AsyncValidatorFn, ValidatorFn, Validators } from '@angular/forms';
import { disabled, form, FormField } from '@angular/forms/signals';
import { of } from 'rxjs';
import { catchError, debounceTime, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import {
  CountryOption,
  CountryService,
  diff,
  EditableInputComponent,
  FormFieldValueWrapperComponent,
  Option,
  Owner,
  SelectComponent,
  StartupService,
  Tenant,
  VitamUIFieldErrorComponent,
} from 'vitamui-library';

import { ALPHA_NUMERIC_REGEX, OWNER_CODE_MAX_LENGTH, OwnerFormValidators } from '../../owner-form/owner-form.validators';
import { OwnerService } from '../../owner.service';
import { TenantFormValidators } from '../../tenant-create/tenant-form.validators';
import { TenantService } from '../../tenant.service';
import { MatDivider } from '@angular/material/divider';
import { TranslatePipe } from '@ngx-translate/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OverlayModule } from '@angular/cdk/overlay';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CommonModule } from '@angular/common';

const UPDATE_DEBOUNCE_TIME = 200;

interface OwnerInformationModel {
  id: string;
  identifier: string;
  customerId: string;
  code: string;
  name: string;
  companyName: string;
  address: {
    street: string;
    zipCode: string;
    city: string;
    country: string;
  };
  internalCode: string;
}

interface TenantInformationModel {
  id: string;
  identifier: number;
  customerId: string;
  ownerId: string;
  name: string;
  enabled: boolean;
  ingestContractHoldingIdentifier: string;
  itemIngestContractIdentifier: string;
  accessContractHoldingIdentifier: string;
  accessContractLogbookIdentifier: string;
}

function toOwnerInformationModel(owner: Owner): OwnerInformationModel {
  return {
    id: owner?.id ?? null,
    identifier: owner?.identifier ?? null,
    customerId: owner?.customerId ?? null,
    code: owner?.code ?? null,
    name: owner?.name ?? null,
    companyName: owner?.companyName ?? null,
    address: {
      street: owner?.address?.street ?? null,
      zipCode: owner?.address?.zipCode ?? null,
      city: owner?.address?.city ?? null,
      country: owner?.address?.country ?? null,
    },
    internalCode: owner?.internalCode ?? null,
  };
}

function toTenantInformationModel(tenant: Tenant): TenantInformationModel {
  return {
    id: tenant?.id ?? null,
    identifier: tenant?.identifier ?? null,
    customerId: tenant?.customerId ?? null,
    ownerId: tenant?.ownerId ?? null,
    name: tenant?.name ?? null,
    enabled: tenant?.enabled ?? null,
    ingestContractHoldingIdentifier: tenant?.ingestContractHoldingIdentifier ?? null,
    itemIngestContractIdentifier: tenant?.itemIngestContractIdentifier ?? null,
    accessContractHoldingIdentifier: tenant?.accessContractHoldingIdentifier ?? null,
    accessContractLogbookIdentifier: tenant?.accessContractLogbookIdentifier ?? null,
  };
}

@Component({
  selector: 'app-information-tab',
  templateUrl: './information-tab.component.html',
  styleUrls: ['./information-tab.component.scss'],
  imports: [
    FormField,
    VitamUIFieldErrorComponent,
    FormFieldValueWrapperComponent,
    SelectComponent,
    MatDivider,
    TranslatePipe,
    CommonModule,
    EditableInputComponent,
    MatButtonToggleModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    OverlayModule,
  ],
})
export class InformationTabComponent implements OnChanges, OnInit {
  private readonly ownerFormValidators = inject(OwnerFormValidators);
  private readonly ownerService = inject(OwnerService);
  private readonly tenantService = inject(TenantService);
  private readonly tenantFormValidators = inject(TenantFormValidators);
  private readonly countryService = inject(CountryService);
  private readonly startupService = inject(StartupService);

  readonly owner = input<Owner>(undefined);
  readonly tenant = input<Tenant>(undefined);
  readonly readOnly = input<boolean>(undefined);

  public ownerCodeMaxLength = OWNER_CODE_MAX_LENGTH;
  public maxStreetLength: number = this.startupService.getConfigNumberValue('MAX_STREET_LENGTH');
  public countries: Option[];
  public previousOwner: OwnerInformationModel;
  public previousTenant: TenantInformationModel;

  private readonly ownerModel = signal<OwnerInformationModel>(toOwnerInformationModel(null));
  // Values set by the reset methods: like a reset with emitEvent: false, they must not trigger an update
  private ownerResetValue = this.ownerModel();
  public readonly ownerForm = form(this.ownerModel, (path) => {
    disabled(path, () => !!this.readOnly());
    // The identifier is never editable
    disabled(path.identifier);
  });

  private readonly tenantModel = signal<TenantInformationModel>(toTenantInformationModel(null));
  private tenantResetValue = this.tenantModel();
  public readonly tenantForm = form(this.tenantModel, (path) => {
    disabled(path, () => !!this.readOnly());
  });

  // Validators of the editable fields (they validate the value being edited, before it is set in the form)
  private readonly ownerCode = signal<string>(undefined);
  private readonly tenantName = signal<string>(undefined);
  readonly requiredValidator: ValidatorFn = Validators.required;
  readonly ownerCodeValidator: ValidatorFn = Validators.compose([
    Validators.required,
    Validators.pattern(ALPHA_NUMERIC_REGEX),
    Validators.maxLength(OWNER_CODE_MAX_LENGTH),
  ]);
  readonly ownerCodeAsyncValidator = computed<AsyncValidatorFn>(() => this.ownerFormValidators.uniqueCode(this.ownerCode()));
  readonly tenantNameAsyncValidator = computed<AsyncValidatorFn>(() => this.tenantFormValidators.uniqueName(this.tenantName()));

  constructor() {
    toObservable(this.ownerModel)
      .pipe(
        filter((value) => value !== this.ownerResetValue),
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.ownerModel(), this.previousOwner)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.owner().id }, formData)),
        switchMap((formData) => this.ownerService.patch(formData).pipe(catchError(() => of(null)))),
        takeUntilDestroyed(),
      )
      .subscribe((owner: Owner) => this.resetOwnerForm(owner));

    toObservable(this.tenantModel)
      .pipe(
        filter((value) => value !== this.tenantResetValue),
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.tenantModel(), this.previousTenant)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.tenant().id }, formData)),
        switchMap((formData) => this.tenantService.patch(formData, this.ownerModel().name).pipe(catchError(() => of(null)))),
        takeUntilDestroyed(),
      )
      .subscribe((tenant: Tenant) => {
        this.resetTenantForm(tenant);
      });
  }

  ngOnInit() {
    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((country) => ({ key: country.code, label: country.name }));
    });
  }

  ngOnChanges() {
    const tenant = this.tenant();
    if (tenant) {
      this.setTenantFormValue(tenant);
    }
    const owner = this.owner();
    if (owner) {
      this.resetOwnerForm(owner);
    }
  }

  private resetOwnerForm(owner: Owner) {
    this.ownerResetValue = toOwnerInformationModel(owner);
    this.ownerModel.set(this.ownerResetValue);
    this.ownerForm().reset();
    this.ownerCode.set(owner.code);

    this.previousOwner = this.ownerResetValue;
  }

  private resetTenantForm(tenant: Tenant) {
    this.tenant().name = tenant.name;
    this.setTenantFormValue(tenant);
  }

  private setTenantFormValue(tenant: Tenant) {
    this.tenantResetValue = toTenantInformationModel(tenant);
    this.tenantModel.set(this.tenantResetValue);
    this.tenantForm().reset();
    this.tenantName.set(tenant.name);

    this.previousTenant = this.tenantResetValue;
  }
}
