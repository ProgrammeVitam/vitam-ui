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
import { Subscription } from 'rxjs';
import {
  ApplicationId,
  AuthService,
  ConfirmDialogService,
  DialogHeaderComponent,
  InputComponent,
  LevelInputComponent,
  Profile,
  Role,
  RoleComponent,
  RoleToggleComponent,
  SlideToggleComponent,
} from 'vitamui-library';

import { Component, inject, OnDestroy, OnInit, signal } from '@angular/core';
import { form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';

import { CustomerService } from '../../core/customer.service';
import { ProfileService } from '../profile.service';
import { ProfileValidators } from '../profile.validators';
import { TranslatePipe } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { levelRules } from '../../shared/signal-forms/signal-form-validators';
import { hasError } from '../../shared/signal-forms/signal-form-utils';

interface ProfileCreateModel {
  enabled: boolean;
  name: string;
  description: string;
  level: string;
  customerId: string;
  applicationName: string;
  tenantIdentifier: string;
  roles: { name: Role }[];
}

@Component({
  selector: 'app-profile-create',
  templateUrl: './profile-create.component.html',
  styleUrls: ['./profile-create.component.scss'],
  imports: [
    DialogHeaderComponent,
    FormField,
    FormRoot,
    MatDialogContent,
    SlideToggleComponent,
    InputComponent,
    MatDialogActions,
    TranslatePipe,
    CommonModule,
    LevelInputComponent,
    RoleComponent,
    RoleToggleComponent,
  ],
})
export class ProfileCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<ProfileCreateComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  rngProfileService = inject(ProfileService);
  authService = inject(AuthService);
  customerService = inject(CustomerService);
  profileValidators = inject(ProfileValidators);
  private readonly confirmDialogService = inject(ConfirmDialogService);

  tenantWithProofId: string = this.authService.user.proofTenantIdentifier;
  roleEnum = Role;

  private readonly model = signal<ProfileCreateModel>({
    enabled: true,
    name: null,
    description: null,
    level: '',
    customerId: this.authService.user.customerId,
    applicationName: 'USERS_APP',
    tenantIdentifier: this.tenantWithProofId,
    roles: [{ name: this.roleEnum.ROLE_GET_USERS }, { name: this.roleEnum.ROLE_GET_GROUPS }, { name: this.roleEnum.ROLE_GET_USER_INFOS }],
  });
  readonly adminProfileForm = form(
    this.model,
    (path) => {
      required(path.name);
      minLength(path.name, 2);
      maxLength(path.name, 100);
      // The name must be unique for the selected level
      this.profileValidators.nameExistsRule(path.name, ({ valueOf }) => ({
        tenantIdentifier: Number(this.tenantWithProofId),
        level: valueOf(path.level),
        applicationName: ApplicationId.USERS_APP,
      }));
      required(path.description);
      minLength(path.description, 2);
      maxLength(path.description, 250);
      levelRules(path.level, this.authService.user);
    },
    { submission: { action: async () => this.onSubmit(), ignoreValidators: 'all' } },
  );
  protected readonly hasError = hasError;

  private keyPressSubscription: Subscription;

  ngOnInit() {
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.adminProfileForm().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  firstStepInvalid(): boolean {
    return (
      this.adminProfileForm.name().invalid() ||
      this.adminProfileForm.name().pending() ||
      this.adminProfileForm.description().invalid() ||
      this.adminProfileForm.description().pending() ||
      this.adminProfileForm.level().invalid()
    );
  }

  completeUpdateRoles(profile: Profile) {
    // add ROLE_UPDATE_USERS when an user can update standard informations or MFA data
    // remove ROLE_UPDATE_USERS when an user can't update standard informations and MFA data
    const userUpdateRolesNames = [Role.ROLE_MFA_USERS.toString(), Role.ROLE_UPDATE_STANDARD_USERS.toString()];

    const hasUpdateRole = profile.roles.some((r: any) => userUpdateRolesNames.includes(r.name));
    const roleUpdateUsersIndex = profile.roles.findIndex((role: any) => role.name === Role.ROLE_UPDATE_USERS);
    const roleUpdateUsersInfoIndex = profile.roles.findIndex((role: any) => role.name === Role.ROLE_UPDATE_USER_INFOS);

    if (hasUpdateRole) {
      if (roleUpdateUsersIndex === -1) {
        profile.roles.push({ name: Role.ROLE_UPDATE_USERS });
      }

      if (roleUpdateUsersInfoIndex === -1) {
        profile.roles.push({ name: Role.ROLE_UPDATE_USER_INFOS });
      }
    } else {
      if (roleUpdateUsersIndex !== -1) {
        profile.roles.splice(roleUpdateUsersIndex, 1);
      }

      if (roleUpdateUsersInfoIndex !== -1) {
        profile.roles.splice(roleUpdateUsersInfoIndex, 1);
      }
    }
  }

  completeCreateRoles(profile: Profile) {
    const userCreateRolesNames = [Role.ROLE_CREATE_USERS.toString()];

    const hasUserCreateRole = profile.roles.some((r: any) => userCreateRolesNames.includes(r.name));
    const userCreateInfoRoleIndex = profile.roles.findIndex((role: any) => role.name === Role.ROLE_CREATE_USER_INFOS);

    if (hasUserCreateRole) {
      if (userCreateInfoRoleIndex === -1) {
        profile.roles.push({ name: Role.ROLE_CREATE_USER_INFOS });
      }
    } else if (userCreateInfoRoleIndex !== -1) {
      profile.roles.splice(userCreateInfoRoleIndex, 1);
    }
  }

  onSubmit() {
    if (this.adminProfileForm().invalid()) {
      return;
    }
    // The roles are completed below: a copy is used to keep the form model unchanged
    const profile = { ...this.model(), roles: [...this.model().roles] } as unknown as Profile;
    this.completeUpdateRoles(profile);
    this.completeCreateRoles(profile);
    this.rngProfileService.create(profile).subscribe(
      () => this.dialogRef.close(true),
      (error) => {
        console.error(error);
      },
    );
  }
}
