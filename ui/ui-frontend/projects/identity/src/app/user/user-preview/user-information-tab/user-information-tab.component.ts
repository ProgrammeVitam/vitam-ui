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
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { merge, of } from 'rxjs';
import { catchError, debounceTime, filter, map, switchMap } from 'rxjs/operators';
import { extend, isEmpty } from 'underscore';
import {
  AdminUserProfile,
  CountryOption,
  CountryService,
  Customer,
  diff,
  Option,
  OtpState,
  StartupService,
  User,
  UserInfo,
} from 'vitamui-library';
import { UserInfoService } from '../../user-info.service';

import { UserCreateValidators } from '../../user-create/user-create.validators';
import { UserService } from '../../user.service';

const UPDATE_DEBOUNCE_TIME = 200;

@Component({
  selector: 'app-user-info-tab',
  templateUrl: './user-information-tab.component.html',
  styleUrls: ['./user-information-tab.component.scss'],
  standalone: false,
})
export class UserInfoTabComponent implements OnChanges, OnInit {
  @Input() user: User;
  @Input() userInfo: UserInfo;
  @Input() customer: Customer;
  @Input() readOnly: boolean;
  @Input() adminUserProfile: AdminUserProfile;

  public maxStreetLength: number;
  public form: FormGroup;
  public userInfoForm: FormGroup;
  public showTooltip: boolean;
  public isPopup: boolean;
  public customerEmailDomains: string[];
  public countries: Option[];
  public previousValue: {
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
    identifier: string;
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
  };

  public previousUserInfoValue: {
    language: string;
  };

  constructor(
    private userService: UserService,
    private userInfoService: UserInfoService,
    private formBuilder: FormBuilder,
    private userCreateValidators: UserCreateValidators,
    private countryService: CountryService,
    private startupService: StartupService,
  ) {
    this.maxStreetLength = this.startupService.getConfigNumberValue('MAX_STREET_LENGTH');
    this.form = this.formBuilder.group({
      id: [null],
      identifier: [{ value: null, disabled: true }, Validators.required],
      firstname: [null, Validators.required],
      lastname: [null, Validators.required],
      email: [null, [Validators.required, Validators.email], this.userCreateValidators.uniqueEmail()],
      mobile: [null, [Validators.pattern(/^[+]{1}[0-9]{11,12}$/)]],
      phone: [null, [Validators.pattern(/^[+]{1}[0-9]{11,12}$/)]],
      level: [{ value: '', disabled: true }],
      otp: [null],
      type: [null],
      status: [null],
      customerId: [null],
      groupId: [null],
      subrogeable: false,
      address: this.formBuilder.group({
        street: [null, [Validators.required, Validators.maxLength(this.maxStreetLength)]],
        zipCode: [null, Validators.required],
        city: [null, Validators.required],
        country: [null, Validators.required],
      }),
      siteCode: [null],
      centerCodes: [null],
      internalCode: [null],
      autoProvisioningEnabled: null,
    });
    this.userInfoForm = this.formBuilder.group({
      language: [null, Validators.required],
    });
  }

  ngOnInit() {
    this.form.get('mobile').valueChanges.subscribe(() => {
      this.updateOtpState(this.form);
    });
    this.form.get('otp').valueChanges.subscribe(() => {
      this.initMobileValidators(this.form);
    });

    merge(this.form.valueChanges, this.form.statusChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.form.getRawValue(), this.previousValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => {
          // centerCodes is used for auto-provisioning (Xelians) and should be an array
          const centerCodes = formData.centerCodes?.split(',')?.map((v: string) => v.trim());
          return { ...formData, id: this.user.id, centerCodes };
        }),
        switchMap((formData) => this.userService.patch(formData).pipe(catchError(() => of(null)))),
      )
      .subscribe((user: User) => {
        if (user) {
          this.resetForm(user);
        }
      });

    merge(this.userInfoForm.valueChanges, this.userInfoForm.statusChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        map(() => diff(this.userInfoForm.getRawValue(), this.previousUserInfoValue)),
        filter((formData) => !isEmpty(formData)),
        map((formData) => extend({ id: this.userInfo.id }, formData)),
        switchMap((formData) => this.userInfoService.patch(formData, this.user).pipe(catchError(() => of(null)))),
      )
      .subscribe((userInfo: UserInfo) => this.resetUserInfoForm(userInfo));

    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((country) => ({ key: country.code, label: country.name }));
    });
  }

  private updateOtpState(form: FormGroup): void {
    if (this.canModifyOtp()) {
      if (form.get('mobile') && (!form.get('mobile').value || form.get('mobile').value === '')) {
        this.showTooltip = true;
        form.get('otp').disable({ emitEvent: false });
      } else if (form.get('otp').disable) {
        this.showTooltip = false;
        form.get('otp').enable({ emitEvent: true });
      }
    }
  }

  private canModifyOtp() {
    return this.adminUserProfile || !this.adminUserProfile.multifactorAllowed || (this.customer && this.customer.otp !== OtpState.OPTIONAL);
  }

  ngOnChanges(changes: SimpleChanges) {
    if (
      changes.hasOwnProperty('user') ||
      changes.hasOwnProperty('readOnly') ||
      changes.hasOwnProperty('customer') ||
      changes.hasOwnProperty('adminUserProfile')
    ) {
      if (this.user && this.customer && this.adminUserProfile) {
        this.resetForm(this.user);
      }
    }
    if (changes.hasOwnProperty('userInfo') && this.userInfo) {
      this.resetUserInfoForm(this.userInfo);
    }
  }

  private resetForm(user: User) {
    this.form.reset(user, { emitEvent: false });
    this.previousValue = this.form.getRawValue();
    this.initFormValidators(this.form, user);
    this.initFormActivationState(this.form);
    this.form.updateValueAndValidity({ emitEvent: false });
  }
  private resetUserInfoForm(userInfo: UserInfo) {
    if (userInfo) {
      this.userInfoForm.reset(userInfo, { emitEvent: false });
      this.previousUserInfoValue = this.userInfoForm.getRawValue();
      this.userInfoForm.updateValueAndValidity({ emitEvent: false });
    }
  }

  private initFormValidators(form: FormGroup, user: User) {
    form.get('email').setAsyncValidators(this.userCreateValidators.uniqueEmail(user.email));
    this.initMobileValidators(form);
  }

  private initMobileValidators(form: FormGroup) {
    const mobileValidators = [Validators.pattern(/^[+]{1}[0-9]{11,12}$/)];
    if (form.get('otp').value) {
      mobileValidators.push(Validators.required);
    }
    form.get('mobile').setValidators(mobileValidators);
  }

  private initFormActivationState(form: FormGroup) {
    // get customer email domains
    this.customerEmailDomains = [];
    this.customer.emailDomains.forEach((domain) => this.customerEmailDomains.push(domain.replace('*.', '')));
    if (this.readOnly || !this.adminUserProfile.standardAttrsAllowed) {
      form.disable({ emitEvent: false });

      return;
    }

    form.enable({ emitEvent: false });

    form.get('identifier').disable({ emitEvent: false });
    form.get('level').disable({ emitEvent: false });
    if (!this.adminUserProfile.genericAllowed) {
      form.get('type').disable({ emitEvent: false });
    }

    this.updateOtpState(form);
  }
}
