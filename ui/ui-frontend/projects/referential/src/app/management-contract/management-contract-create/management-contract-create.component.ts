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
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import {
  ConfirmDialogService,
  Logger,
  ManagementContract,
  Option,
  PersistentIdentifierPolicy,
  PersistentIdentifierPolicyTypeEnum,
} from 'ui-frontend-common';
import * as uuid from 'uuid';
import { FormGroupToManagementContractConverterService } from '../components/form-group-to-management-contract-converter.service';
import { ManagementContractToFormGroupConverterService } from '../components/management-contract-to-form-group-converter.service';
import { ManagementContractService } from '../management-contract.service';
import { ManagementContractCreateValidators } from '../validators/management-contract-create.validators';

const PROGRESS_BAR_MULTIPLICATOR = 100;

interface PersistentIdentifierPolicyTypeOption {
  label: string;
  value: PersistentIdentifierPolicyTypeEnum | string;
}

@Component({
  selector: 'app-management-contract-create',
  templateUrl: './management-contract-create.component.html',
  styleUrls: ['./management-contract-create.component.scss'],
})
export class ManagementContractCreateComponent implements OnInit, OnDestroy {
  constructor(
    public dialogRef: MatDialogRef<ManagementContractCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService,
    private managementContractService: ManagementContractService,
    private managementContractCreateValidators: ManagementContractCreateValidators,
    private managementContractToFormGroupConverterService: ManagementContractToFormGroupConverterService,
    private formGroupToManagementContractConverterService: FormGroupToManagementContractConverterService,
    private logger: Logger,
  ) {}

  get stepProgress() {
    return ((this.stepIndex + 1) / this.stepCount) * PROGRESS_BAR_MULTIPLICATOR;
  }

  form: FormGroup;
  stepIndex = 0;
  stepCount = 3;

  isDisabledButton = false;
  isSlaveMode: boolean;

  keyPressSubscription: Subscription;
  apiSubscriptions: Subscription;

  gotOpened = false;
  deleteDisabled = true;

  statusControl = new FormControl(true);
  technicalObjectActivated = false;

  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Original numérique', info: '' },
    { key: 'Dissemination', label: 'Copie de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignette', info: '' },
    { key: 'TextContent', label: 'Contenu brut', info: '' },
    { key: 'PhysicalMaster', label: 'Original papier', info: '' },
  ];

  policyTypeOptions: PersistentIdentifierPolicyTypeOption[] = [
    { label: 'CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.NONE.LABEL', value: '' },
    ...Object.values(PersistentIdentifierPolicyTypeEnum).map((pipt) => ({
      label: `CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.${pipt.toUpperCase()}.LABEL`,
      value: pipt,
    })),
  ];

  ngOnInit() {
    this.form = this.formBuilder.group({
      // Step 1
      ...(this.isSlaveMode
        ? {
            identifier: [
              null,
              [Validators.required, Validators.minLength(5), Validators.maxLength(100)],
              this.managementContractCreateValidators.uniqueIdentifier(),
            ],
          }
        : {}),
      status: [true],
      name: [
        null,
        [Validators.required, Validators.minLength(3), Validators.maxLength(100)],
        this.managementContractCreateValidators.uniqueName(),
      ],
      description: [null],
      // Step 2
      storage: this.formBuilder.group({
        unitStrategy: ['default', Validators.required],
        objectGroupStrategy: ['default', Validators.required],
        objectStrategy: ['default', Validators.required],
      }),
      // Step 3
      persistentIdentifierPolicies: this.getDefaultPersistentIdentifierPolicies(),
      policyTypeOption: '', // For option behavior
    });

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());
    this.form.get('policyTypeOption').valueChanges.subscribe((value) => {
      let persistentIdentifierPolicies: FormArray = this.formBuilder.array([]);

      if (value === PersistentIdentifierPolicyTypeEnum.ARK) {
        persistentIdentifierPolicies = this.getDefaultPersistentIdentifierPolicies();
        persistentIdentifierPolicies.patchValue([{ policyTypeOption: this.policyTypeOptions[1].value }]);
      }
      this.form.removeControl('persistentIdentifierPolicies');
      this.form.setControl('persistentIdentifierPolicies', persistentIdentifierPolicies);
    });
  }

  getDefaultPersistentIdentifierPolicies(): FormArray {
    return this.managementContractToFormGroupConverterService
      .getDefaultManagementContractForm()
      .get('persistentIdentifierPolicies') as FormArray;
  }

  getPersistentIdentifierPolicies(): FormArray {
    return this.form.get('persistentIdentifierPolicies') as FormArray;
  }

  ngOnDestroy() {
    this.keyPressSubscription?.unsubscribe();
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
    if (this.isDisabledButton) return;
    this.isDisabledButton = true;
    this.apiSubscriptions = this.managementContractService.create(this.generateManagementContract()).subscribe(
      () => {
        this.isDisabledButton = false;
        this.dialogRef.close({ success: true, action: 'none' });
      },
      (error: any) => {
        this.isDisabledButton = false;
        this.dialogRef.close({ success: false, action: 'none' });
        this.logger.error(error);
      },
    );
  }

  isInvalid(fieldNames: string[], abstractControl: AbstractControl): boolean {
    const fieldIsInvalid = (fieldName: string) => !abstractControl.get(fieldName).valid;

    return fieldNames.some(fieldIsInvalid);
  }

  firstStepInvalid(): boolean {
    const baseFieldNames = ['name', 'description', 'status'];
    const fieldNames = this.isSlaveMode ? [...baseFieldNames, 'identifier'] : baseFieldNames;
    return this.isInvalid(fieldNames, this.form);
  }

  secondStepInvalid(): boolean {
    const fieldNames = ['unitStrategy', 'objectGroupStrategy', 'objectStrategy'];
    return this.isInvalid(fieldNames, this.form.controls.storage);
  }

  hasValidPersistentIdentifierPolicies(): boolean {
    const managementContract: ManagementContract = this.formGroupToManagementContractConverterService.convert(this.form);
    const consistentPersistentIdentifierPolicies = managementContract.persistentIdentifierPolicyList.filter(
      (persistentIdentifierPolicy: PersistentIdentifierPolicy) =>
        Object.values(PersistentIdentifierPolicyTypeEnum).includes(persistentIdentifierPolicy.persistentIdentifierPolicyType),
    );

    if (consistentPersistentIdentifierPolicies.length === 0) {
      return true;
    }

    return this.form.valid;
  }

  private generateManagementContract(): ManagementContract {
    const managementContract: ManagementContract = this.formGroupToManagementContractConverterService.convert(this.form);
    const consistentPersistentIdentifierPolicies = managementContract.persistentIdentifierPolicyList.filter(
      (persistentIdentifierPolicy: PersistentIdentifierPolicy) =>
        Object.values(PersistentIdentifierPolicyTypeEnum).includes(persistentIdentifierPolicy.persistentIdentifierPolicyType),
    );
    if (consistentPersistentIdentifierPolicies.length === 0) {
      delete managementContract.persistentIdentifierPolicyList;
    }

    const timestamp = new Date().toISOString();
    if (managementContract.status === 'ACTIVE') {
      managementContract.activationDate = timestamp;
    } else {
      managementContract.deactivationDate = timestamp;
    }

    if (!managementContract.identifier) {
      managementContract.identifier = uuid.v4();
    }

    if (!managementContract.storage) {
      managementContract.storage = {
        unitStrategy: null,
        objectGroupStrategy: null,
        objectStrategy: null,
      };
    }

    return managementContract;
  }

  get identifier(): AbstractControl | null {
    return this.form.get('identifier');
  }

  get name(): AbstractControl | null {
    return this.form.get('name');
  }

  get description(): AbstractControl | null {
    return this.form.get('description');
  }
}
