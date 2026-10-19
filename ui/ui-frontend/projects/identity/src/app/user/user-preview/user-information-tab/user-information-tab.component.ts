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
import { ChangeDetectorRef, Component, computed, inject, OnChanges, OnInit, SimpleChanges, input, signal } from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { AsyncValidatorFn, ValidatorFn, Validators } from '@angular/forms';
import { disabled, form, FormField, required } from '@angular/forms/signals';
import { of } from 'rxjs';
import { catchError, debounceTime, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import {
  AdminUserProfile,
  CountryOption,
  CountryService,
  Customer,
  diff,
  EditableEmailInputComponent,
  EditableInputComponent,
  FormFieldValueWrapperComponent,
  Option,
  OtpState,
  PipesModule,
  SelectComponent,
  SlideToggleComponent,
  StartupService,
  TooltipDirective,
  User,
  UserInfo,
  VitamUIFieldErrorComponent,
} from 'vitamui-library';
import { UserInfoService } from '../../user-info.service';

import { UserCreateValidators } from '../../user-create/user-create.validators';
import { UserService } from '../../user.service';
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
const PHONE_PATTERN = /^[+]{1}[0-9]{11,12}$/;

interface UserInformationModel {
  id: string;
  identifier: string;
  firstname: string;
  lastname: string;
  email: string;
  mobile: string;
  phone: string;
  level: string;
  otp: boolean;
  type: string;
  status: string;
  customerId: string;
  groupId: string;
  subrogeable: boolean;
  address: {
    street: string;
    zipCode: string;
    city: string;
    country: string;
  };
  siteCode: string;
  centerCodes: string[];
  internalCode: string;
  autoProvisioningEnabled: boolean;
}

function toUserInformationModel(user: User): UserInformationModel {
  return {
    id: user?.id ?? null,
    identifier: user?.identifier ?? null,
    firstname: user?.firstname ?? null,
    lastname: user?.lastname ?? null,
    email: user?.email ?? null,
    mobile: user?.mobile ?? null,
    phone: user?.phone ?? null,
    level: user?.level ?? null,
    otp: user?.otp ?? null,
    type: user?.type ?? null,
    status: user?.status ?? null,
    customerId: user?.customerId ?? null,
    groupId: user?.groupId ?? null,
    subrogeable: user?.subrogeable ?? null,
    address: {
      street: user?.address?.street ?? null,
      zipCode: user?.address?.zipCode ?? null,
      city: user?.address?.city ?? null,
      country: user?.address?.country ?? null,
    },
    siteCode: user?.siteCode ?? null,
    centerCodes: user?.centerCodes ?? null,
    internalCode: user?.internalCode ?? null,
    autoProvisioningEnabled: user?.autoProvisioningEnabled ?? null,
  };
}

@Component({
  selector: 'app-user-info-tab',
  templateUrl: './user-information-tab.component.html',
  styleUrls: ['./user-information-tab.component.scss'],
  imports: [
    FormField,
    VitamUIFieldErrorComponent,
    FormFieldValueWrapperComponent,
    SelectComponent,
    SlideToggleComponent,
    TooltipDirective,
    PipesModule,
    TranslatePipe,
    CommonModule,
    EditableEmailInputComponent,
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
export class UserInfoTabComponent implements OnChanges, OnInit {
  private readonly userService = inject(UserService);
  private readonly userInfoService = inject(UserInfoService);
  private readonly userCreateValidators = inject(UserCreateValidators);
  private readonly countryService = inject(CountryService);
  private readonly startupService = inject(StartupService);
  private readonly cdr = inject(ChangeDetectorRef);

  readonly user = input<User>(undefined);
  readonly userInfo = input<UserInfo>(undefined);
  readonly customer = input<Customer>(undefined);
  readonly readOnly = input<boolean>(undefined);
  readonly adminUserProfile = input<AdminUserProfile>(undefined);

  public maxStreetLength: number = this.startupService.getConfigNumberValue('MAX_STREET_LENGTH');
  public isPopup: boolean;
  public customerEmailDomains: string[];
  public countries: Option[];
  public previousValue: UserInformationModel;

  public previousUserInfoValue: {
    language: string;
  };

  private readonly model = signal<UserInformationModel>({ ...toUserInformationModel(null), level: '', subrogeable: false });
  // Values set by the reset methods: like a reset with emitEvent: false, they must not trigger an update
  private resetValue = this.model();
  // Activation state of the form, updated by resetForm
  private readonly formDisabled = signal(false);
  private readonly typeDisabled = signal(false);
  private readonly otpStateManaged = signal(false);
  public readonly form = form(this.model, (path) => {
    disabled(path, () => this.formDisabled());
    // The identifier and the level are never editable
    disabled(path.identifier);
    disabled(path.level);
    disabled(path.type, () => this.typeDisabled());
    // The OTP can be enabled only when the user has a mobile phone number
    disabled(path.otp, ({ valueOf }) => this.otpStateManaged() && !valueOf(path.mobile));
    required(path.address.country);
  });
  public readonly showTooltip = computed(() => this.otpStateManaged() && !this.form.mobile().value());

  private readonly userInfoModel = signal<{ language: string }>({ language: null });
  private userInfoResetValue = this.userInfoModel();
  public readonly userInfoForm = form(this.userInfoModel, (path) => required(path.language));

  // Validators of the editable fields (they validate the value being edited, before it is set in the form)
  private readonly userEmail = signal<string>(undefined);
  readonly requiredValidator: ValidatorFn = Validators.required;
  readonly emailValidator: ValidatorFn = Validators.compose([Validators.required, Validators.email]);
  readonly emailAsyncValidator = computed<AsyncValidatorFn>(() => this.userCreateValidators.uniqueEmail(this.userEmail()));
  readonly mobileValidator = computed<ValidatorFn>(() =>
    Validators.compose([Validators.pattern(PHONE_PATTERN), ...(this.form.otp().value() ? [Validators.required] : [])]),
  );
  readonly phoneValidator: ValidatorFn = Validators.pattern(PHONE_PATTERN);
  readonly streetValidator: ValidatorFn = Validators.compose([Validators.required, Validators.maxLength(this.maxStreetLength)]);

  constructor() {
    toObservable(computed(() => ({ value: this.model(), valid: this.form().valid() })))
      .pipe(
        filter(({ value }) => value !== this.resetValue),
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.model(), this.previousValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => {
          // centerCodes is used for auto-provisioning (Xelians) and should be an array
          const centerCodes = formData['centerCodes']?.split(',')?.map((v: string) => v.trim());
          return { ...formData, id: this.user().id, centerCodes };
        }),
        switchMap((formData) => this.userService.patch(formData).pipe(catchError(() => of(null)))),
        takeUntilDestroyed(),
      )
      .subscribe((user: User) => {
        if (user) {
          this.resetForm(user);
          this.cdr.markForCheck();
        }
      });

    toObservable(this.userInfoModel)
      .pipe(
        filter((value) => value !== this.userInfoResetValue),
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.userInfoModel(), this.previousUserInfoValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.userInfo().id }, formData)),
        switchMap((formData) => this.userInfoService.patch(formData, this.user()).pipe(catchError(() => of(null)))),
        takeUntilDestroyed(),
      )
      .subscribe((userInfo: UserInfo) => this.resetUserInfoForm(userInfo));
  }

  ngOnInit() {
    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((country) => ({ key: country.code, label: country.name }));
      this.cdr.markForCheck();
    });
  }

  private canModifyOtp() {
    const adminUserProfile = this.adminUserProfile();
    const customer = this.customer();
    return adminUserProfile || !adminUserProfile.multifactorAllowed || (customer && customer.otp !== OtpState.OPTIONAL);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (
      changes.hasOwnProperty('user') ||
      changes.hasOwnProperty('readOnly') ||
      changes.hasOwnProperty('customer') ||
      changes.hasOwnProperty('adminUserProfile')
    ) {
      const user = this.user();
      if (user && this.customer() && this.adminUserProfile()) {
        this.resetForm(user);
      }
    }
    const userInfo = this.userInfo();
    if (changes.hasOwnProperty('userInfo') && userInfo) {
      this.resetUserInfoForm(userInfo);
    }
  }

  private resetForm(user: User) {
    this.resetValue = toUserInformationModel(user);
    this.model.set(this.resetValue);
    this.form().reset();
    this.previousValue = this.resetValue;
    this.userEmail.set(user.email);
    this.initFormActivationState();
  }

  private resetUserInfoForm(userInfo: UserInfo) {
    if (userInfo) {
      this.userInfoResetValue = { language: userInfo.language ?? null };
      this.userInfoModel.set(this.userInfoResetValue);
      this.userInfoForm().reset();
      this.previousUserInfoValue = this.userInfoResetValue;
    }
  }

  private initFormActivationState() {
    // get customer email domains
    this.customerEmailDomains = [];
    this.customer().emailDomains.forEach((domain) => this.customerEmailDomains.push(domain.replace('*.', '')));
    const adminUserProfile = this.adminUserProfile();
    if (this.readOnly() || !adminUserProfile.standardAttrsAllowed) {
      this.formDisabled.set(true);

      return;
    }

    this.formDisabled.set(false);
    this.typeDisabled.set(!adminUserProfile.genericAllowed);
    this.otpStateManaged.set(!!this.canModifyOtp());
  }
}
