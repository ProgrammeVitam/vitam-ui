/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { ConfirmDialogService, Logger, ManagementContract, StorageStrategy } from 'ui-frontend-common';
import * as uuid from 'uuid';
import { ManagementContractService } from '../management-contract.service';
import { ManagementContractCreateValidators } from '../validators/management-contract-create.validators';

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'app-management-contract-create',
  templateUrl: './management-contract-create.component.html',
  styleUrls: ['./management-contract-create.component.scss'],
})
export class ManagementContractCreateComponent implements OnInit, OnDestroy {
  form: FormGroup;
  stepIndex = 0;
  stepCount = 2;

  isDisabledButton = false;
  isSlaveMode: boolean;

  keyPressSubscription: Subscription;
  apiSubscriptions: Subscription;
  statusControlValueChangesSubscribe: Subscription;

  constructor(
    public dialogRef: MatDialogRef<ManagementContractCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService,
    private managementContractService: ManagementContractService,
    private managementContractCreateValidators: ManagementContractCreateValidators,
    private logger: Logger
  ) {}

  statusControl = new FormControl(false);

  ngOnInit() {
    this.form = this.formBuilder.group({
      // Step 1
      identifier: [null, Validators.required, this.managementContractCreateValidators.uniqueIdentifier()],
      status: ['INACTIVE'],
      name: [null, [Validators.required], this.managementContractCreateValidators.uniqueName()],
      description: [null, Validators.required],
      // Step 2
      storage: this.formBuilder.group({
        unitStrategy: ['default', Validators.required],
        objectGroupStrategy: ['default', Validators.required],
        objectStrategy: ['default', Validators.required],
      }),
    });

    this.statusControlValueChangesSubscribe = this.statusControl.valueChanges.subscribe((value: boolean) => {
      this.form.controls.status.setValue(value === false ? 'INACTIVE' : 'ACTIVE');
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription?.unsubscribe();
    this.statusControlValueChangesSubscribe?.unsubscribe();
    this.apiSubscriptions?.unsubscribe();
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    this.isDisabledButton = true;
    const managementContract = this.form.value as ManagementContract;
    managementContract.status === 'ACTIVE'
      ? (managementContract.activationDate = new Date().toISOString())
      : (managementContract.deactivationDate = new Date().toISOString());
    if (this.form.get('identifier').value === null) {
      managementContract.identifier = uuid.v4();
    }
    if (!managementContract.storage) {
      const storage: StorageStrategy = {
        unitStrategy: null,
        objectGroupStrategy: null,
        objectStrategy: null,
      };
      managementContract.storage = storage;
    }
    this.apiSubscriptions = this.managementContractService.create(managementContract).subscribe(
      () => {
        this.isDisabledButton = false;
        this.dialogRef.close({ success: true, action: 'none' });
      },
      (error: any) => {
        this.dialogRef.close({ success: false, action: 'none' });
        this.logger.error(error);
      }
    );
  }

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

  firstStepInvalid(): boolean {
    if (this.isSlaveMode) {
      return (
        this.form.get('identifier').invalid ||
        this.form.get('identifier').pending ||
        this.form.get('name').invalid ||
        this.form.get('name').pending ||
        this.form.get('description').invalid ||
        this.form.get('description').pending ||
        this.form.get('status').invalid ||
        this.form.get('status').pending
      );
    } else {
      return (
        this.form.get('name').invalid ||
        this.form.get('name').pending ||
        this.form.get('description').invalid ||
        this.form.get('description').pending ||
        this.form.get('status').invalid ||
        this.form.get('status').pending
      );
    }
  }

  secondStepInvalid(): boolean {
    return (
      this.form.controls.storage.get('unitStrategy').invalid ||
      this.form.controls.storage.get('unitStrategy').pending ||
      this.form.controls.storage.get('objectGroupStrategy').invalid ||
      this.form.controls.storage.get('objectGroupStrategy').pending ||
      this.form.controls.storage.get('objectStrategy').invalid ||
      this.form.controls.storage.get('objectStrategy').pending
    );
  }
}
