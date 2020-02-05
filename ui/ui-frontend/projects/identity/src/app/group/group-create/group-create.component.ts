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
import { AuthService, buildValidators, ConfirmDialogService } from 'ui-frontend-common';

import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';


import { GroupService } from '../group.service';
import { GroupValidators } from '../group.validators';

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'app-group-create',
  templateUrl: './group-create.component.html',
  styleUrls: ['./group-create.component.scss'],
  animations: [
    trigger('expansion', [
      state('collapsed', style({height: '0px', visibility: 'hidden'})),
      state('expanded', style({height: '*', visibility: 'visible'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),

    trigger('arrow', [
      state('collapsed', style({transform: 'rotate(180deg)'})),
      state('expanded', style({transform: 'none'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),
  ]
})
export class GroupCreateComponent implements OnInit, OnDestroy {

  form: FormGroup;
  stepIndex = 0;
  subLevelIsRequired: boolean;

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 2;
  private keyPressSubscription: Subscription;

  constructor(
    public dialogRef: MatDialogRef<GroupCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    public authService: AuthService,
    private formBuilder: FormBuilder,
    private groupService: GroupService,
    private groupValidators: GroupValidators,
    private confirmDialogService: ConfirmDialogService
  ) {}

  ngOnInit() {
    this.form = this.formBuilder.group({
      name: [null, Validators.required, this.groupValidators.nameExists(this.authService.user.customerId)],
      enabled : [true],
      level : ['', buildValidators(this.authService.user)],
      description: [null, Validators.required],
      profileIds: [null, Validators.required],
      customerId: [this.authService.user.customerId]
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription.unsubscribe();
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
    this.groupService.create(this.form.getRawValue()).subscribe(
      () => this.dialogRef.close(true),
      (error) => {
        console.error(error);
      });
  }

  firstStepInvalid(): boolean {
    return this.form.get('name').invalid ||
      this.form.get('description').invalid || this.form.get('level').invalid;
  }

  formValid(): boolean {
    return this.form.pending || this.form.invalid;
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

}
