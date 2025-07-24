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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ValidationErrors, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Observable, Subscription } from 'rxjs';
import {
  AdminUserProfile,
  AuthService,
  ConfirmDialogService,
  CountryOption,
  CountryService,
  Customer,
  Group,
  isRootLevel,
  Logger,
  Option,
  OtpState,
  StartupService,
  UserInfo,
} from 'vitamui-library';
import { GroupSelection } from './../group-selection.interface';
import { UserInfoService } from './../user-info.service';

import { distinctUntilChanged, map, tap } from 'rxjs/operators';
import { UserService } from '../user.service';
import { UserCreateValidators } from './user-create.validators';

const emailFirstPartValidator: RegExp = /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+$/;

@Component({
  selector: 'app-user-create',
  templateUrl: './user-create.component.html',
  styleUrls: ['./user-create.component.scss'],
  standalone: false,
})
export class UserCreateComponent implements OnInit, OnDestroy {
  public maxStreetLength: number;
  public form: FormGroup;
  public formEmail: FormGroup;
  public customer: Customer;
  public groups: GroupSelection[] = [];
  public fullGroup: Group[];
  public groupName: string;
  public connectedUserInfo: AdminUserProfile;
  public creating = false;
  private keyPressSubscription: Subscription;
  public countries: Option[];

  LAST_STEP_INDEX = 2;

  constructor(
    public dialogRef: MatDialogRef<UserCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { userInfo: AdminUserProfile; customer: Customer; groups: Group[] },
    private formBuilder: FormBuilder,
    private userService: UserService,
    private userInfoService: UserInfoService,
    private authService: AuthService,
    private userCreateValidators: UserCreateValidators,
    private confirmDialogService: ConfirmDialogService,
    private countryService: CountryService,
    private startupService: StartupService,
    private loggerService: Logger,
  ) {}

  ngOnInit() {
    this.fullGroup = this.data.groups;
    this.groups = this.fullGroup.map((group) => {
      return {
        id: group.id,
        name: group.name,
        description: group.description,
        selected: false,
        profiles: group.profiles,
        level: group.level,
      };
    }) as GroupSelection[];

    this.maxStreetLength = this.startupService.getConfigNumberValue('MAX_STREET_LENGTH');
    if (!isRootLevel(this.authService.user)) {
      this.groups = this.groups.filter((group: GroupSelection) => group.id !== this.authService.user.groupId);
    }

    this.groups.sort((a, b) => (a.name.toUpperCase() < b.name.toUpperCase() ? -1 : a.name.toUpperCase() > b.name.toUpperCase() ? 1 : 0));

    this.customer = this.data.customer;
    this.connectedUserInfo = this.data.userInfo;

    this.formEmail = this.formBuilder.group(
      {
        emailFirstPart: [null, [Validators.required, Validators.maxLength(50), Validators.pattern(emailFirstPartValidator)]],
        domain: [this.customer.emailDomains[0], Validators.required],
      },
      {
        asyncValidators: () => {
          const emailFirstPart = this.formEmail.get('emailFirstPart');
          emailFirstPart.markAsPending();
          return (this.userCreateValidators.uniqueEmail()(this.form.get('email')) as Observable<ValidationErrors | null>).pipe(
            tap((validationErrors) => emailFirstPart.setErrors(validationErrors)),
          );
        },
      },
    );

    this.form = this.formBuilder.group({
      enabled: true,
      email: [null],
      firstname: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      lastname: [null, [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      mobile: [
        null,
        [...(this.customer.otp !== OtpState.DEACTIVATED ? [Validators.required] : []), Validators.pattern(/^[+]{1}[0-9]{11,12}$/)],
      ],
      phone: [null, [Validators.pattern(/^[+]{1}[0-9]{11,12}$/)]],
      groupId: [null, Validators.required],
      customerId: this.authService.user.customerId,
      otp: [
        {
          value: this.customer.otp !== OtpState.DEACTIVATED,
          disabled: this.customer.otp !== OtpState.OPTIONAL,
        },
      ],
      type: [{ value: 'NOMINATIVE', disabled: !this.connectedUserInfo.genericAllowed }],
      subrogeable: false,
      status: null,
      userInfoId: null,
      address: this.formBuilder.group({
        street: [null, Validators.maxLength(this.maxStreetLength)],
        zipCode: [null, [Validators.maxLength(10)]],
        city: [null, [Validators.maxLength(100)]],
        country: ['FR'],
      }),
      internalCode: [null, [Validators.maxLength(20)]],
      siteCode: [null],
      centerCodes: [null],
      autoProvisioningEnabled: false,
    });
    this.applyUserProfile();
    this.onChanges();
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    const addressControl = this.form.get('address');
    addressControl.valueChanges
      .pipe(
        map((value) => !value.street && !value.zipCode && !value.city),
        distinctUntilChanged(),
      )
      .subscribe((addressEmpty) => {
        const streetControl = addressControl.get('street');
        const zipCodeControl = addressControl.get('zipCode');
        const cityControl = addressControl.get('city');
        if (addressEmpty) {
          [streetControl, zipCodeControl, cityControl].forEach((control) => control.removeValidators(Validators.required));
        } else {
          [streetControl, zipCodeControl, cityControl].forEach((control) => control.addValidators(Validators.required));
        }
        [streetControl, zipCodeControl, cityControl].forEach((control) => control.updateValueAndValidity());
      });

    this.form.get('otp').valueChanges.subscribe((otp) => {
      const mobileControl = this.form.get('mobile');
      if (otp) {
        mobileControl.addValidators(Validators.required);
      } else {
        mobileControl.removeValidators(Validators.required);
      }
      mobileControl.updateValueAndValidity();
    });

    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((country) => ({ key: country.code, label: country.name }));
    });
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  applyUserProfile() {
    if (this.connectedUserInfo.type === 'NONE') {
      this.form.get('enabled').setValue(false);
      this.form.get('enabled').disable();
      this.form.get('groupId').setValidators(null);
      this.form.updateValueAndValidity({ emitEvent: false });
    } else if (this.connectedUserInfo.type === 'LIST') {
      this.groups = this.connectedUserInfo.profilGroup.map((group) => {
        const profilGroup = this.groups.find((g) => g.id === group.id);
        return Object({
          id: group.id,
          name: group.name,
          description: group.description,
          selected: false,
          profiles: profilGroup?.profiles,
        });
      });
      this.groups.sort((a, b) => (a.name < b.name ? -1 : a.name > b.name ? 1 : 0));
    }
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form.invalid) {
      return;
    }
    this.creating = true;
    let status = '';
    this.form.get('enabled').value ? (status = 'ENABLED') : (status = 'DISABLED');
    this.form.get('status').setValue(status);
    this.userInfoService.create({ id: null, language: this.customer.language }).subscribe(
      (response: UserInfo) => {
        this.form.get('userInfoId').setValue(response.id);
        const formData = this.form.getRawValue();
        // centerCodes is used for auto-provisioning (Xelians) and should be an array
        const centerCodes = formData.centerCodes?.split(',')?.map((v: string) => v.trim());
        this.userService.create({ ...formData, centerCodes }).subscribe(
          () => this.dialogRef.close(true),
          (error) => {
            this.creating = false;
            this.loggerService.error(error);
          },
        );
      },
      (error) => {
        this.creating = false;
        this.loggerService.error(error);
      },
    );
  }

  onChanges(): void {
    this.formEmail.valueChanges.subscribe((emailData) => {
      this.form.patchValue({ email: `${emailData.emailFirstPart}@${emailData.domain}` });
    });
  }

  firstStepInvalid(): boolean {
    return (
      this.form.pending ||
      this.form.get('firstname').invalid ||
      this.form.get('lastname').invalid ||
      this.formEmail.get('emailFirstPart').invalid ||
      this.formEmail.get('emailFirstPart').pending ||
      this.formEmail.get('domain').invalid ||
      this.form.get('enabled').invalid
    );
  }

  public thirdStepInvalid(): boolean {
    return (
      this.form.get('address').pending ||
      this.form.get('address').invalid ||
      this.form.get('internalCode').pending ||
      this.form.get('internalCode').invalid
    );
  }

  formInvalid(): boolean {
    return this.form.pending || this.form.invalid;
  }

  updateGroup(event: any) {
    const selectedGroup: GroupSelection = event;
    this.groupName = selectedGroup.name;
    const groupId = selectedGroup.id;
    this.form.patchValue({ groupId });
  }
}
