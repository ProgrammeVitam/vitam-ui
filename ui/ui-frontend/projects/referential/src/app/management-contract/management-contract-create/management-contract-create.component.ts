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
import { Component, effect, inject, Injector, OnDestroy, OnInit, signal, untracked } from '@angular/core';
import { applyEach, applyWhen, FieldTree, form, FormField, FormRoot, maxLength, minLength, required } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import * as uuid from 'uuid';
import {
  ConfirmDialogService,
  Logger,
  ManagementContract,
  Option,
  PersistentIdentifierPolicy,
  PersistentIdentifierPolicyTypeEnum,
  StorageStrategy,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';
import { FormModelToManagementContractConverterService } from '../components/form-model-to-management-contract-converter.service';
import {
  ManagementContractToFormModelConverterService,
  PersistentIdentifierPolicyForm,
} from '../components/management-contract-to-form-model-converter.service';
import { ManagementContractService } from '../management-contract.service';
import { ManagementContractCreateValidators } from '../validators/management-contract-create.validators';
import { persistentIdentifierPolicySchema } from '../validators/management-contract-validators';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';

import { CreatePersistentIdentifierPolicyFormComponent } from '../components/create-persistent-identifier-policy-form/create-persistent-identifier-policy-form.component';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';

type ValidatableField = () => { valid: () => boolean };

interface ManagementContractCreateForm {
  identifier: string;
  status: boolean;
  name: string;
  description: string;
  storage: StorageStrategy;
  persistentIdentifierPolicies: PersistentIdentifierPolicyForm[];
  policyTypeOption: string;
}

@Component({
  selector: 'app-management-contract-create',
  templateUrl: './management-contract-create.component.html',
  styleUrls: ['./management-contract-create.component.scss'],
  imports: [
    FormRoot,
    FormField,
    MatButtonToggleModule,
    MatCheckboxModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatRadioModule,
    MatSelectModule,
    CreatePersistentIdentifierPolicyFormComponent,
    VitamUICommonModule,
    VitamUILibraryModule,
    TranslatePipe,
  ],
})
export class ManagementContractCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<ManagementContractCreateComponent>>(MatDialogRef);
  data = inject<{
    isSlaveMode: boolean;
  }>(MAT_DIALOG_DATA);
  private readonly injector = inject(Injector);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly managementContractService = inject(ManagementContractService);
  private readonly managementContractCreateValidators = inject(ManagementContractCreateValidators);
  private readonly managementContractToFormModelConverterService = inject(ManagementContractToFormModelConverterService);
  private readonly formModelToManagementContractConverterService = inject(FormModelToManagementContractConverterService);
  private readonly logger = inject(Logger);
  private readonly translateService = inject(TranslateService);

  constructor() {
    const data = this.data;

    this.isSlaveMode = data.isSlaveMode;
  }

  form: FieldTree<ManagementContractCreateForm>;

  isDisabledButton = signal(false);
  isSlaveMode: boolean;

  keyPressSubscription: Subscription;
  apiSubscriptions: Subscription;

  policyTypeOptions: Option[] = [
    { label: this.translateService.instant('CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.NONE.LABEL'), key: '' },
    ...Object.values(PersistentIdentifierPolicyTypeEnum).map((pipt) => ({
      label: this.translateService.instant(
        `CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.${pipt.toUpperCase()}.LABEL`,
      ),
      key: pipt,
    })),
  ];

  ngOnInit() {
    // Built in ngOnInit as the identifier field depends on isSlaveMode
    this.form = form(
      signal<ManagementContractCreateForm>({
        // Step 1
        identifier: '',
        status: true,
        name: '',
        description: null,
        // Step 2
        storage: {
          unitStrategy: 'default',
          objectGroupStrategy: 'default',
          objectStrategy: 'default',
        },
        // Step 3
        persistentIdentifierPolicies: this.managementContractToFormModelConverterService.getDefaultPersistentIdentifierPolicies(),
        policyTypeOption: '', // For option behavior
      }),
      (path) => {
        // The identifier is only filled in slave mode
        applyWhen(
          path,
          () => this.isSlaveMode,
          (contractPath) => {
            required(contractPath.identifier);
            minLength(contractPath.identifier, 5);
            maxLength(contractPath.identifier, 100);
            this.managementContractCreateValidators.uniqueIdentifier(contractPath.identifier);
          },
        );
        required(path.name);
        minLength(path.name, 3);
        maxLength(path.name, 100);
        this.managementContractCreateValidators.uniqueName(path.name);
        required(path.storage.unitStrategy);
        required(path.storage.objectGroupStrategy);
        required(path.storage.objectStrategy);
        applyEach(path.persistentIdentifierPolicies, persistentIdentifierPolicySchema);
      },
      {
        injector: this.injector,
        submission: {
          action: async () => {
            this.onSubmit();
          },
          ignoreValidators: 'all',
        },
      },
    );

    this.keyPressSubscription = this.confirmDialogService.listenToEscapeKeyPress(this.dialogRef).subscribe(() => this.onCancel());

    let previousPolicyTypeOption = this.form.policyTypeOption().value();
    effect(
      () => {
        const policyTypeOption = this.form.policyTypeOption().value();
        untracked(() => {
          if (policyTypeOption !== previousPolicyTypeOption) {
            previousPolicyTypeOption = policyTypeOption;
            this.onPolicyTypeOptionChange(policyTypeOption);
          }
        });
      },
      { injector: this.injector },
    );
  }

  private onPolicyTypeOptionChange(policyTypeOption: string) {
    let persistentIdentifierPolicies: PersistentIdentifierPolicyForm[] = [];

    if (policyTypeOption === PersistentIdentifierPolicyTypeEnum.ARK) {
      const [defaultPolicy, ...otherPolicies] = this.managementContractToFormModelConverterService.getDefaultPersistentIdentifierPolicies();
      persistentIdentifierPolicies = [{ ...defaultPolicy, policyTypeOption: this.policyTypeOptions[1].key }, ...otherPolicies];
    }
    this.form.persistentIdentifierPolicies().value.set(persistentIdentifierPolicies);
  }

  ngOnDestroy() {
    this.keyPressSubscription?.unsubscribe();
    this.apiSubscriptions?.unsubscribe();
  }

  onCancel() {
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.isDisabledButton()) return;
    this.isDisabledButton.set(true);
    this.apiSubscriptions = this.managementContractService.create(this.generateManagementContract()).subscribe(
      () => {
        this.isDisabledButton.set(false);
        this.dialogRef.close({ success: true, action: 'none' });
      },
      (error: any) => {
        this.isDisabledButton.set(false);
        this.dialogRef.close({ success: false, action: 'none' });
        this.logger.error(error);
      },
    );
  }

  private isInvalid(fields: ValidatableField[]): boolean {
    return fields.some((field) => !field().valid());
  }

  firstStepInvalid(): boolean {
    const baseFields: ValidatableField[] = [this.form.name, this.form.description, this.form.status];
    const fields = this.isSlaveMode ? [...baseFields, this.form.identifier] : baseFields;

    return this.isInvalid(fields);
  }

  secondStepInvalid(): boolean {
    const storage = this.form.storage;

    return this.isInvalid([storage.unitStrategy, storage.objectGroupStrategy, storage.objectStrategy]);
  }

  hasValidPersistentIdentifierPolicies(): boolean {
    const managementContract: ManagementContract = this.formModelToManagementContractConverterService.convert(this.form().value());
    const consistentPersistentIdentifierPolicies = managementContract.persistentIdentifierPolicyList.filter(
      (persistentIdentifierPolicy: PersistentIdentifierPolicy) =>
        Object.values(PersistentIdentifierPolicyTypeEnum).includes(persistentIdentifierPolicy.persistentIdentifierPolicyType),
    );

    if (consistentPersistentIdentifierPolicies.length === 0) {
      return true;
    }

    return this.form().valid();
  }

  private generateManagementContract(): ManagementContract {
    const managementContract: ManagementContract = this.formModelToManagementContractConverterService.convert(this.form().value());
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
}
