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
import { Component, inject, OnDestroy, OnInit, signal, ChangeDetectorRef } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { disabled, FieldTree, form, FormField, FormRoot, maxLength, minLength, pattern, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { skip } from 'rxjs/operators';
import {
  AdminUserProfile,
  AuthService,
  ConfirmDialogService,
  CountryOption,
  CountryService,
  Customer,
  DialogHeaderComponent,
  Group,
  InputComponent,
  isRootLevel,
  Logger,
  NextStepComponent,
  Option,
  OtpState,
  PreviousStepComponent,
  SelectComponent,
  SlideToggleComponent,
  StartupService,
  StepperComponent,
  UserInfo,
  validateObservable,
} from 'vitamui-library';
import { GroupSelection } from './../group-selection.interface';
import { UserInfoService } from './../user-info.service';

import { UserService } from '../user.service';
import { UserCreateValidators } from './user-create.validators';
import { CdkStep } from '@angular/cdk/stepper';
import { MatButtonToggle, MatButtonToggleGroup } from '@angular/material/button-toggle';
import { GroupListComponent } from '../group-attribution/group-list/group-list.component';
import { TranslatePipe } from '@ngx-translate/core';

const emailFirstPartValidator: RegExp = /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+$/;
const phoneValidator: RegExp = /^[+]{1}[0-9]{11,12}$/;

interface UserCreateModel {
  enabled: boolean;
  email: string;
  firstname: string;
  lastname: string;
  mobile: string;
  phone: string;
  groupId: string;
  customerId: string;
  otp: boolean;
  type: string;
  subrogeable: boolean;
  status: string;
  userInfoId: string;
  address: {
    street: string;
    zipCode: string;
    city: string;
    country: string;
  };
  internalCode: string;
  siteCode: string;
  centerCodes: string;
  autoProvisioningEnabled: boolean;
}

interface UserEmailModel {
  emailFirstPart: string;
  domain: string;
}

@Component({
  selector: 'app-user-create',
  templateUrl: './user-create.component.html',
  styleUrls: ['./user-create.component.scss'],
  imports: [
    DialogHeaderComponent,
    FormField,
    FormRoot,
    StepperComponent,
    CdkStep,
    MatDialogContent,
    SlideToggleComponent,
    InputComponent,
    SelectComponent,
    MatButtonToggleGroup,
    MatButtonToggle,
    MatDialogActions,
    NextStepComponent,
    GroupListComponent,
    PreviousStepComponent,
    TranslatePipe,
  ],
})
export class UserCreateComponent implements OnInit, OnDestroy {
  private readonly cdr = inject(ChangeDetectorRef);
  dialogRef = inject<MatDialogRef<UserCreateComponent>>(MatDialogRef);
  data = inject<{
    userInfo: AdminUserProfile;
    customer: Customer;
    groups: Group[];
  }>(MAT_DIALOG_DATA);
  private readonly userService = inject(UserService);
  private readonly userInfoService = inject(UserInfoService);
  private readonly authService = inject(AuthService);
  private readonly userCreateValidators = inject(UserCreateValidators);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly countryService = inject(CountryService);
  private readonly startupService = inject(StartupService);
  private readonly loggerService = inject(Logger);

  public maxStreetLength: number = this.startupService.getConfigNumberValue('MAX_STREET_LENGTH');
  public customer: Customer = this.data.customer;
  public connectedUserInfo: AdminUserProfile = this.data.userInfo;

  private readonly emailModel = signal<UserEmailModel>({ emailFirstPart: null, domain: this.customer.emailDomains[0] });
  public readonly formEmail = form(this.emailModel, (path) => {
    required(path.emailFirstPart);
    maxLength(path.emailFirstPart, 50);
    pattern(path.emailFirstPart, emailFirstPartValidator);
    required(path.domain);
    // The uniqueness of the whole email is checked, the error is displayed on its first part
    validateObservable(path, {
      stream: ({ emailFirstPart, domain }) => this.userCreateValidators.emailExists(`${emailFirstPart}@${domain}`),
      onSuccess: (exists, { fieldTree }) =>
        exists ? { kind: 'uniqueEmail', fieldTree: fieldTree.emailFirstPart as unknown as FieldTree<unknown> } : undefined,
    });
  });

  private readonly model = signal<UserCreateModel>({
    enabled: this.connectedUserInfo.type !== 'NONE',
    email: null,
    firstname: null,
    lastname: null,
    mobile: null,
    phone: null,
    groupId: null,
    customerId: this.authService.user.customerId,
    otp: this.customer.otp !== OtpState.DEACTIVATED,
    type: 'NOMINATIVE',
    subrogeable: false,
    status: null,
    userInfoId: null,
    address: {
      street: null,
      zipCode: null,
      city: null,
      country: 'FR',
    },
    internalCode: null,
    siteCode: null,
    centerCodes: null,
    autoProvisioningEnabled: false,
  });
  public readonly form = form(
    this.model,
    (path) => {
      disabled(path.enabled, () => this.connectedUserInfo.type === 'NONE');
      required(path.firstname);
      minLength(path.firstname, 2);
      maxLength(path.firstname, 50);
      required(path.lastname);
      minLength(path.lastname, 2);
      maxLength(path.lastname, 50);
      required(path.mobile, { when: ({ valueOf }) => !!valueOf(path.otp) });
      pattern(path.mobile, phoneValidator);
      pattern(path.phone, phoneValidator);
      required(path.groupId, { when: () => this.connectedUserInfo.type !== 'NONE' });
      disabled(path.otp, () => this.customer.otp !== OtpState.OPTIONAL);
      disabled(path.type, () => !this.connectedUserInfo.genericAllowed);
      // The address is optional, but must be complete when one of its fields is filled
      const addressFilled = (address: UserCreateModel['address']) => !!(address.street || address.zipCode || address.city);
      required(path.address.street, { when: ({ valueOf }) => addressFilled(valueOf(path.address)) });
      required(path.address.zipCode, { when: ({ valueOf }) => addressFilled(valueOf(path.address)) });
      required(path.address.city, { when: ({ valueOf }) => addressFilled(valueOf(path.address)) });
      maxLength(path.address.street, this.maxStreetLength);
      maxLength(path.address.zipCode, 10);
      maxLength(path.address.city, 100);
      maxLength(path.internalCode, 20);
    },
    { submission: { action: async () => this.onSubmit(), ignoreValidators: 'all' } },
  );
  public groups: GroupSelection[] = [];
  public fullGroup: Group[];
  public groupName: string;
  public readonly creating = signal(false);
  private keyPressSubscription: Subscription;
  public countries: Option[];

  LAST_STEP_INDEX = 2;

  constructor() {
    toObservable(this.emailModel)
      .pipe(skip(1))
      .subscribe((emailData) => this.form.email().value.set(`${emailData.emailFirstPart}@${emailData.domain}`));
  }

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

    if (!isRootLevel(this.authService.user)) {
      this.groups = this.groups.filter((group: GroupSelection) => group.id !== this.authService.user.groupId);
    }

    this.groups.sort((a, b) => (a.name.toUpperCase() < b.name.toUpperCase() ? -1 : a.name.toUpperCase() > b.name.toUpperCase() ? 1 : 0));

    this.applyUserProfile();
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((country) => ({ key: country.code, label: country.name }));
      this.cdr.markForCheck();
    });
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  applyUserProfile() {
    // When the connected user type is 'NONE', the user is disabled and no group is required (see the form schema)
    if (this.connectedUserInfo.type === 'LIST') {
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
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form().invalid()) {
      return;
    }
    this.creating.set(true);
    let status = '';
    this.form.enabled().value() ? (status = 'ENABLED') : (status = 'DISABLED');
    this.form.status().value.set(status);
    this.userInfoService.create({ id: null, language: this.customer.language }).subscribe(
      (response: UserInfo) => {
        this.form.userInfoId().value.set(response.id);
        const formData = this.model();
        // centerCodes is used for auto-provisioning (Xelians) and should be an array
        const centerCodes = formData.centerCodes?.split(',')?.map((v: string) => v.trim());
        this.userService.create({ ...formData, centerCodes } as any).subscribe(
          () => this.dialogRef.close(true),
          (error) => {
            this.creating.set(false);
            this.loggerService.error(error);
          },
        );
      },
      (error) => {
        this.creating.set(false);
        this.loggerService.error(error);
      },
    );
  }

  firstStepInvalid(): boolean {
    return (
      this.form().pending() ||
      this.form.firstname().invalid() ||
      this.form.lastname().invalid() ||
      this.formEmail.emailFirstPart().invalid() ||
      this.formEmail.emailFirstPart().pending() ||
      this.formEmail.domain().invalid() ||
      this.form.enabled().invalid()
    );
  }

  public thirdStepInvalid(): boolean {
    return (
      this.form.address().pending() ||
      this.form.address().invalid() ||
      this.form.internalCode().pending() ||
      this.form.internalCode().invalid()
    );
  }

  formInvalid(): boolean {
    return this.form().pending() || this.form().invalid();
  }

  updateGroup(event: any) {
    const selectedGroup: GroupSelection = event;
    this.groupName = selectedGroup.name;
    this.form.groupId().value.set(selectedGroup.id);
  }
}
