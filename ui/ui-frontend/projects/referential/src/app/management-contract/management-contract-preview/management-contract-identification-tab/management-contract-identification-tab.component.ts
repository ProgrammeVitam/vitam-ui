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
import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges, inject } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Observable, of, Subscription } from 'rxjs';
import { mergeMap, tap } from 'rxjs/operators';
import { ManagementContract, Option } from 'vitamui-library';
import { PersistentIdentifierPolicyTypeEnum, SelectComponent, TooltipDirective } from 'vitamui-library';
import { FormGroupToManagementContractConverterService } from '../../components/form-group-to-management-contract-converter.service';
import { ManagementContractToFormGroupConverterService } from '../../components/management-contract-to-form-group-converter.service';
import { ManagementContractService } from '../../management-contract.service';
import { TranslateService, TranslatePipe } from '@ngx-translate/core';
import { UpdatePersistentIdentifierPolicyFormComponent } from '../../components/update-persistent-identifier-policy-form/update-persistent-identifier-policy-form.component';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-management-contract-identification-tab',
  templateUrl: './management-contract-identification-tab.component.html',
  styleUrls: ['./management-contract-identification-tab.component.scss'],
  providers: [ManagementContractToFormGroupConverterService],
  imports: [
    ReactiveFormsModule,
    SelectComponent,
    UpdatePersistentIdentifierPolicyFormComponent,
    TooltipDirective,
    MatProgressSpinner,
    TranslatePipe,
  ],
})
export class ManagementContractIdentificationTabComponent implements OnChanges, OnDestroy {
  private managementContractToFormGroupConverterService = inject(ManagementContractToFormGroupConverterService);
  private formGroupToManagementContractConverterService = inject(FormGroupToManagementContractConverterService);
  private managementContractService = inject(ManagementContractService);
  private formBuilder = inject(FormBuilder);
  private translateService = inject(TranslateService);

  @Input() managementContract: ManagementContract;
  @Output() updated: EventEmitter<boolean> = new EventEmitter<boolean>();

  contractForm: FormGroup;
  sending = false;

  policyTypeOptions: Option[] = [
    { label: this.translateService.instant('CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.NONE.LABEL'), key: '' },
    ...Object.values(PersistentIdentifierPolicyTypeEnum).map((pipt) => ({
      label: this.translateService.instant(
        `CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.${pipt.toUpperCase()}.LABEL`,
      ),
      key: pipt,
    })),
  ];

  private subscriptions: Subscription = new Subscription();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['managementContract']) {
      const managementConctract: ManagementContract = changes['managementContract'].currentValue;

      this.resetForm(managementConctract);
    }
  }

  submit(): void {
    const subscription: Subscription = this.prepareSubmit()
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
    // Nettoyer les souscriptions existantes pour éviter les faux positifs
    this.subscriptions.unsubscribe();
    this.subscriptions = new Subscription();

    this.contractForm = this.managementContractToFormGroupConverterService.convert(managementContract);

    const persistentIdentifierPolicies = managementContract.persistentIdentifierPolicyList || [];
    const [persistentIdentifierPolicy] = persistentIdentifierPolicies;
    const policyTypeOptionValue = persistentIdentifierPolicy?.persistentIdentifierPolicyType || '';
    this.contractForm.addControl('policyTypeOption', this.formBuilder.control(policyTypeOptionValue, [Validators.required]));

    this.updated.emit(false);

    this.subscriptions.add(
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
          persistentIdentifierPolicyFormArray.patchValue([{ policyTypeOption: this.policyTypeOptions[1].key }]);
        }
        this.contractForm.removeControl('persistentIdentifierPolicies');
        this.contractForm.setControl('persistentIdentifierPolicies', persistentIdentifierPolicyFormArray);
      }),
    );

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

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
