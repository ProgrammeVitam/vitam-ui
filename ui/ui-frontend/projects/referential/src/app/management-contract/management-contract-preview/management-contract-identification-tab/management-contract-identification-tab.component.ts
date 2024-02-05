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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable, Subscription, of } from 'rxjs';
import { mergeMap, tap } from 'rxjs/operators';
import { ManagementContract, PersistentIdentifierPolicyTypeEnum } from 'ui-frontend-common';
import { FormGroupToManagementContractConverterService } from '../../components/form-group-to-management-contract-converter.service';
import { ManagementContractToFormGroupConverterService } from '../../components/management-contract-to-form-group-converter.service';
import { ManagementContractService } from '../../management-contract.service';

interface PersistentIdentifierPolicyTypeOption {
  label: string;
  value: PersistentIdentifierPolicyTypeEnum | string;
}

@Component({
  selector: 'app-management-contract-identification-tab',
  templateUrl: './management-contract-identification-tab.component.html',
  styleUrls: ['./management-contract-identification-tab.component.scss'],
  providers: [ManagementContractToFormGroupConverterService],
})
export class ManagementContractIdentificationTabComponent implements OnChanges {
  @Input() managementContract: ManagementContract;
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  contractForm: FormGroup;
  sending = false;

  policyTypeOptions: PersistentIdentifierPolicyTypeOption[] = [
    { label: 'CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.NONE.LABEL', value: '' },
    ...Object.values(PersistentIdentifierPolicyTypeEnum).map((pipt) => ({
      label: `CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.${pipt.toUpperCase()}.LABEL`,
      value: pipt,
    })),
  ];

  private subscriptions: Subscription = new Subscription();

  constructor(
    private managementContractToFormGroupConverterService: ManagementContractToFormGroupConverterService,
    private formGroupToManagementContractConverterService: FormGroupToManagementContractConverterService,
    private managementContractService: ManagementContractService,
    private formBuilder: FormBuilder,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.managementContract) {
      const managementConctract: ManagementContract = changes.managementContract.currentValue;

      this.resetForm(managementConctract);
    }
  }

  submit(): void {
    const subscription = this.prepareSubmit()
      .pipe(tap((managementContract) => (this.managementContract = managementContract)))
      .subscribe(() => subscription.unsubscribe());
  }

  prepareSubmit(): Observable<ManagementContract> {
    return of((this.sending = true)).pipe(
      mergeMap(() => this.managementContractService.patch(this.getUpdatedManagementContract())),
      tap(
        () => (this.sending = false),
        () => (this.sending = false),
      ),
      tap(() => this.updated.emit(false)),
      tap(() => {
        this.contractForm.markAsPristine();
      }),
    );
  }

  resetForm(managementContract: ManagementContract): void {
    this.contractForm = this.managementContractToFormGroupConverterService.convert(managementContract);

    const persistentIdentifierPolicies = managementContract.persistentIdentifierPolicyList || [];
    const [persistentIdentifierPolicy] = persistentIdentifierPolicies;
    const policyTypeOptionValue = persistentIdentifierPolicy?.persistentIdentifierPolicyType || '';
    this.contractForm.addControl('policyTypeOption', this.formBuilder.control(policyTypeOptionValue, [Validators.required]));

    this.updated.emit(false);

    this.contractForm.get('policyTypeOption').valueChanges.subscribe((value) => {
      let persistentIdentifierPolicyFormArray: FormArray = this.formBuilder.array([]);

      if (value === PersistentIdentifierPolicyTypeEnum.ARK) {
        persistentIdentifierPolicyFormArray = this.managementContractToFormGroupConverterService
          .convert(managementContract)
          .get('persistentIdentifierPolicies') as FormArray;
        if (persistentIdentifierPolicyFormArray.controls.length === 0) {
          persistentIdentifierPolicyFormArray = this.managementContractToFormGroupConverterService
            .getDefaultManagementContractForm()
            .get('persistentIdentifierPolicies') as FormArray;
        }
        persistentIdentifierPolicyFormArray.patchValue([{ policyTypeOption: this.policyTypeOptions[1].value }]);
      }
      this.contractForm.removeControl('persistentIdentifierPolicies');
      this.contractForm.setControl('persistentIdentifierPolicies', persistentIdentifierPolicyFormArray);
    });

    this.subscriptions.add(
      this.contractForm.valueChanges.subscribe(() => {
        this.updated.emit(true);
      }),
    );
  }

  getUpdatedManagementContract(): ManagementContract {
    const updates = this.formGroupToManagementContractConverterService.convert(this.contractForm);

    return {
      ...this.managementContract,
      persistentIdentifierPolicyList: updates.persistentIdentifierPolicyList,
    };
  }

  getPersistentIdentifierPolicies(): FormArray {
    return this.contractForm.get('persistentIdentifierPolicies') as FormArray;
  }

  isSubmitButtonDisabled(): boolean {
    const hasNoChanges = this.deepEqual(this.managementContract, this.getUpdatedManagementContract());

    if (hasNoChanges) {
      this.updated.emit(false);
    }

    return this.contractForm.invalid || this.contractForm.pristine || hasNoChanges;
  }

  private deepEqual(obj1: any, obj2: any): boolean {
    if (obj1 === obj2) {
      return true;
    }

    if (typeof obj1 !== 'object' || obj1 === null || typeof obj2 !== 'object' || obj2 === null) {
      return false;
    }

    const keys1 = Object.keys(obj1);
    const keys2 = Object.keys(obj2);

    if (keys1.length !== keys2.length) {
      return false;
    }

    for (const key of keys1) {
      if (!keys2.includes(key) || !this.deepEqual(obj1[key], obj2[key])) {
        return false;
      }
    }

    return true;
  }
}
