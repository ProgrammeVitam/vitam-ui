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
import {ConfirmDialogService, Logger, Option} from 'ui-frontend-common';
import { ManagementContract } from 'projects/vitamui-library/src/public-api';
import { ManagementContractService } from '../management-contract.service';
import { ManagementContractCreateValidators } from './management-contract-create.validators';
import {StorageStrategy} from "vitamui-library";
import {PreservationPolicyMetadata, RETENTION_POLICY_DEFAULT, USAGES} from "../management-contract.constants";
import {MatTableDataSource} from "@angular/material/table";

const PROGRESS_BAR_MULTIPLICATOR = 100;

@Component({
  selector: 'app-management-contract-create',
  templateUrl: './management-contract-create.component.html',
  styleUrls: ['./management-contract-create.component.scss'],
})
export class ManagementContractCreateComponent implements OnInit, OnDestroy {
  form: FormGroup;
  stepIndex = 0;
  isDisabledButton = false;
  isSlaveMode: boolean;

  // stepCount is the total number of steps and is used to calculate the advancement of the progress bar.
  // We could get the number of steps using ViewChildren(StepComponent) but this triggers a
  // "Expression has changed after it was checked" error so we instead manually define the value.
  // Make sure to update this value whenever you add or remove a step from the  template.
  private stepCount = 1;
  private keyPressSubscription: Subscription;

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
  statusControlValueChangesSubscribe: Subscription;

  usages = USAGES;
  initialVersionConservations : Option[] = [
    { key: 'yes', label: 'Oui', info: '' },
    { key: 'no', label: 'Non', info: '' }
  ];

  versionsToPreserves : Option[] = [
    { key: 'all', label: 'Toutes', info: '' }
  ];

  matDataSource: MatTableDataSource<PreservationPolicyMetadata>;

  displayedColumns: string[] = ['usage', 'initialVersionConservation', 'versionsToPreserve'];
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
      // Step 3
      versionRetentionPolicy: this.formBuilder.group({
        initialVersion: ['True', Validators.required],
        intermediaryVersion: ['ALL', Validators.required],
        usage: this.formBuilder.group(({
          usageName: ['BinaryMaster', Validators.required],
          initialVersion: ['true', Validators.required],
          intermediaryVersion: ['ALL', Validators.required]
        }))
      })
    });

    this.statusControlValueChangesSubscribe = this.statusControl.valueChanges.subscribe((value:boolean) => {
      this.form.controls.status.setValue(value === false ? 'INACTIVE' : 'ACTIVE');
    });

    this.matDataSource = new MatTableDataSource<PreservationPolicyMetadata>(RETENTION_POLICY_DEFAULT);

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.keyPressSubscription?.unsubscribe();
    this.statusControlValueChangesSubscribe?.unsubscribe();
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
      this.isDisabledButton = true;
      return;
    }
    this.isDisabledButton = true;
    const managementContract = this.form.value as ManagementContract;
    managementContract.status === 'ACTIVE'
      ? (managementContract.activationDate = new Date().toISOString())
      : (managementContract.deactivationDate = new Date().toISOString());
    if (!managementContract.storage) {
      let storage = {
        unitStrategy: null,
        objectGroupStrategy: null,
        objectStrategy: null
      } as StorageStrategy;
      managementContract.storage = storage;
    }
    this.managementContractService.create(managementContract).subscribe(
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

  thirdStepInvalid(): boolean {
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
