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
  AdminUserProfile, AuthService, ConfirmDialogService, Customer, isRootLevel, OtpState, ProfileSelection
} from 'ui-frontend-common';

import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

import { distinctUntilChanged, map } from 'rxjs/operators';
import { GroupService } from '../../group/group.service';
import { UserService } from '../user.service';
import { UserValidators } from '../user.validators';
import { UserCreateValidators } from './user-create.validators';

const PROGRESS_BAR_MULTIPLICATOR = 100;
const LAST_STEP_INDEX = 2;
// tslint:disable-next-line:max-line-length
const emailValidator: RegExp = /^[a-zA-Z0-9.!#$%&'*+\/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$/;

@Component({
  selector: 'app-user-create',
  templateUrl: './user-create.component.html',
  styleUrls: ['./user-create.component.scss'],
  animations: [
    trigger('expansion', [
      state('collapsed', style({ height: '0px', visibility: 'hidden' })),
      state('expanded', style({ height: '*', visibility: 'visible' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),
  ]
})
export class UserCreateComponent implements OnInit, OnDestroy {

  form: FormGroup;
  formEmail: FormGroup;
  customer: Customer;
  groups: ProfileSelection[] = [];
  groupName: string;
  stepIndex = 0;
  connectedUserInfo: AdminUserProfile;
  addressEmpty = true;
  creating = false;

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 4;
  private keyPressSubscription: Subscription;

  constructor(
    public dialogRef: MatDialogRef<UserCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { userInfo: AdminUserProfile, customer: Customer },
    private formBuilder: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private userCreateValidators: UserCreateValidators,
    private groupService: GroupService,
    private confirmDialogService: ConfirmDialogService
  ) { }

  ngOnInit() {
    this.groupService.getAll(true).subscribe((groups) => {
      this.groups = groups.map((group) => Object({ id: group.id, name: group.name, description: group.description, selected: false }));
      if (!isRootLevel(this.authService.user)) {
        this.groups = this.groups.filter((g) => g.id !== this.authService.user.groupId);
      }
      this.groups.sort((a, b) => a.name.toUpperCase() < b.name.toUpperCase() ? -1 : a.name.toUpperCase() > b.name.toUpperCase() ? 1 : 0);
    });
    this.customer = this.data.customer;
    this.connectedUserInfo = this.data.userInfo;
    this.formEmail = this.formBuilder.group({
      emailFirstPart: null,
      domain: [this.customer.emailDomains[0]]
    });

    this.form = this.formBuilder.group(
      {
        enabled: true,
        email: [null, [
          Validators.required,
          Validators.pattern(emailValidator)
        ],
          this.userCreateValidators.uniqueEmail()],
        firstname: [null, Validators.required],
        lastname: [null, Validators.required],
        mobile: [null, [Validators.pattern(/^[+]{1}[0-9]{11,12}$/)]],
        phone: [null, [Validators.pattern(/^[+]{1}[0-9]{11,12}$/)]],
        domain: [this.customer.emailDomains[0], Validators.required],
        groupId: [null, Validators.required],
        customerId: this.authService.user.customerId,
        language: this.customer.language,
        otp: [{
          value: this.customer.otp !== OtpState.DEACTIVATED,
          disabled: this.customer.otp !== OtpState.OPTIONAL
        }],
        type: [{ value: 'NOMINATIVE', disabled: !this.connectedUserInfo.genericAllowed }],
        subrogeable: false,
        status: null,
        address: this.formBuilder.group({
          street: [null],
          zipCode: [null],
          city: [null],
          country: ['FR']
        })
      },
      { validator: UserValidators.missingPhoneNumber }
    );
    this.applyUserProfile();
    this.onChanges();
    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    this.form.get('address').valueChanges.pipe(
      map((value) => !value.street && !value.zipCode && !value.city),
      distinctUntilChanged()
    ).subscribe((addressEmpty) => {
      this.addressEmpty = addressEmpty;
      if (addressEmpty) {
        this.form.get('address.street').clearValidators();
        this.form.get('address.zipCode').clearValidators();
        this.form.get('address.city').clearValidators();
      } else {
        this.form.get('address.street').setValidators(Validators.required);
        this.form.get('address.zipCode').setValidators(Validators.required);
        this.form.get('address.city').setValidators(Validators.required);
      }
      this.form.get('address').updateValueAndValidity({ emitEvent: false });
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
      this.groups = this.connectedUserInfo.profilGroup
        .map((group) => ({ id: group.id, name: group.name, description: group.description, selected: false }));
      this.groups.sort((a, b) => a.name < b.name ? -1 : a.name > b.name ? 1 : 0);
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
    if (this.form.invalid) { return; }
    this.creating = true;
    let status = '';
    this.form.get('enabled').value ? status = 'ENABLED' : status = 'DISABLED';
    this.form.get('status').setValue(status);
    this.userService.create(this.form.getRawValue()).subscribe(
      () => this.dialogRef.close(true),
      (error) => {
        this.creating = false;
        console.error(error);
      });
  }

  onChanges(): void {
    this.formEmail.valueChanges.subscribe((emailData) => {
      this.form.patchValue({ email: emailData.emailFirstPart + '@' + emailData.domain });
    });
  }

  firstStepInvalid(): boolean {
    return this.form.pending ||
      this.form.get('email').invalid ||
      this.form.get('firstname').invalid ||
      this.form.get('lastname').invalid ||
      this.form.get('domain').invalid ||
      this.form.get('enabled').invalid;
  }

  passGroupStep() {
    this.stepIndex = LAST_STEP_INDEX;
  }

  formInvalid(): boolean {
    return this.form.pending || this.form.invalid;
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

  updateGroupe(groupId: string, groupName: string) {
    this.groupName = groupName;
    this.unselectAllGroups();
    const selectedGroup = this.groups.find((group) => group.id === groupId);
    selectedGroup.selected = true;
    this.form.patchValue({ groupId });
  }

  removeGroup() {
    this.groupName = null;
    this.unselectAllGroups();
    this.form.patchValue({ groupId: null });
  }

  unselectAllGroups() {
    this.groups.forEach((group) => group.selected = false);
  }

}
