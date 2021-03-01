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
import { Subscription } from 'rxjs';
import {
  ApplicationId, AuthService, AuthUser, buildValidators, collapseAnimation, ConfirmDialogService, Group, Profile, Role
} from 'ui-frontend-common';

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { CustomerService } from '../../core/customer.service';
import { ProfileService } from '../profile.service';
import { ProfileValidators } from '../profile.validators';

@Component({
  selector: 'app-profile-create',
  templateUrl: './profile-create.component.html',
  styleUrls: ['./profile-create.component.scss'],
  animations: [
    collapseAnimation,
  ]
})
export class ProfileCreateComponent implements OnInit, OnDestroy {

  adminProfileForm: FormGroup;
  tenantWithProofId: string;
  selectedProfileGroups: Group [] = [];
  selectedProfileGroupsId: string[] = [];
  userLevel: string;
  subLevelIsRequired: boolean;
  roleEnum = Role;

  private keyPressSubscription: Subscription;

  constructor(
    public dialogRef: MatDialogRef<ProfileCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public rngProfileService: ProfileService,
    public authService: AuthService,
    public customerService: CustomerService,
    public profileValidators: ProfileValidators,
    private formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService
    )  {
    this.adminProfileForm = this.formBuilder.group({
      enabled: true,
      name: [null, Validators.required],
      description: [null, Validators.required],
      level: ['',  buildValidators(this.authService.user)],
      customerId: [this.authService.user.customerId],
      applicationName: 'USERS_APP',
      tenantIdentifier: this.tenantWithProofId,
      roles : [[{name : this.roleEnum.ROLE_GET_USERS},
                {name : this.roleEnum.ROLE_GET_GROUPS},
              ]]
    });
  }

  ngOnInit() {
    const user: AuthUser = this.authService.user;
    this.tenantWithProofId = user.proofTenantIdentifier;

    this.adminProfileForm.get('tenantIdentifier').setValue(this.tenantWithProofId);
    this.adminProfileForm.get('name')
    .setAsyncValidators(this.profileValidators.nameExists(Number(this.tenantWithProofId), '', ApplicationId.USERS_APP));

    this.adminProfileForm.get('level').valueChanges.subscribe((level) => {
      this.adminProfileForm.get('name').
      setAsyncValidators(this.profileValidators.nameExists(Number(this.tenantWithProofId), level, ApplicationId.USERS_APP));
      this.adminProfileForm.get('name').updateValueAndValidity();
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
  }

  onCancel() {
    if (this.adminProfileForm.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  firstStepInvalid(): boolean {
    return this.adminProfileForm.get('name').invalid || this.adminProfileForm.get('name').pending ||
      this.adminProfileForm.get('description').invalid || this.adminProfileForm.get('description').pending
      || this.adminProfileForm.get('level').invalid;
  }

  completeRoles(profile: Profile) {
    const userUpdateRolesNames = [Role.ROLE_MFA_USERS.toString(), Role.ROLE_UPDATE_STANDARD_USERS.toString()];
    const hasRole = profile.roles.some((r: any) => userUpdateRolesNames.includes(r.name));
    if (hasRole) {
      profile.roles = profile.roles.concat([{ name: Role.ROLE_UPDATE_USERS }]);
    } else {
      profile.roles = profile.roles.filter((r: any) => Role.ROLE_UPDATE_USERS !== r.name);
    }
  }

  onSubmit() {
    if (this.adminProfileForm.invalid) { return; }
    const profile: Profile = this.adminProfileForm.getRawValue();
    this.completeRoles(profile);
    this.rngProfileService.create(profile).subscribe(
      () => this.dialogRef.close(true),
      (error) => {
        console.error(error);
      });
  }
}
