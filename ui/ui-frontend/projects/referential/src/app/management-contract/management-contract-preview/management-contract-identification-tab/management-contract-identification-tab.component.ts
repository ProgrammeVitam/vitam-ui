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
import { Component, effect, inject, OnChanges, signal, SimpleChanges, output, model, untracked } from '@angular/core';
import { applyEach, form, FormField, FormRoot, required } from '@angular/forms/signals';
import { Observable, of, Subscription } from 'rxjs';
import { mergeMap, tap } from 'rxjs/operators';
import { ManagementContract, Option, PersistentIdentifierPolicyTypeEnum, SelectComponent, TooltipDirective } from 'vitamui-library';
import { FormModelToManagementContractConverterService } from '../../components/form-model-to-management-contract-converter.service';
import {
  ManagementContractToFormModelConverterService,
  PersistentIdentifierPoliciesForm,
  PersistentIdentifierPolicyForm,
} from '../../components/management-contract-to-form-model-converter.service';
import { persistentIdentifierPolicySchema } from '../../validators/management-contract-validators';
import { ManagementContractService } from '../../management-contract.service';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { UpdatePersistentIdentifierPolicyFormComponent } from '../../components/update-persistent-identifier-policy-form/update-persistent-identifier-policy-form.component';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

interface IdentificationForm extends PersistentIdentifierPoliciesForm {
  policyTypeOption: string;
}

@Component({
  selector: 'app-management-contract-identification-tab',
  templateUrl: './management-contract-identification-tab.component.html',
  styleUrls: ['./management-contract-identification-tab.component.scss'],
  providers: [ManagementContractToFormModelConverterService],
  imports: [
    FormRoot,
    FormField,
    SelectComponent,
    UpdatePersistentIdentifierPolicyFormComponent,
    TooltipDirective,
    MatProgressSpinner,
    TranslatePipe,
  ],
})
export class ManagementContractIdentificationTabComponent implements OnChanges {
  private readonly managementContractToFormModelConverterService = inject(ManagementContractToFormModelConverterService);
  private readonly formModelToManagementContractConverterService = inject(FormModelToManagementContractConverterService);
  private readonly managementContractService = inject(ManagementContractService);
  private readonly translateService = inject(TranslateService);

  readonly managementContract = model<ManagementContract>(undefined);
  readonly updated = output<boolean>();

  readonly contractForm = form(
    signal<IdentificationForm>({ persistentIdentifierPolicies: [], policyTypeOption: '' }),
    (path) => {
      applyEach(path.persistentIdentifierPolicies, persistentIdentifierPolicySchema);
      required(path.policyTypeOption);
    },
    {
      submission: {
        action: async () => {
          this.submit();
        },
        ignoreValidators: 'all',
      },
    },
  );
  sending = signal(false);

  // Management contract and form value at the last reset, to detect the changes
  private resetManagementContract: ManagementContract;
  private resetValue: IdentificationForm = this.contractForm().value();
  private previousPolicyTypeOption = this.contractForm.policyTypeOption().value();

  policyTypeOptions: Option[] = [
    { label: this.translateService.instant('CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.NONE.LABEL'), key: '' },
    ...Object.values(PersistentIdentifierPolicyTypeEnum).map((pipt) => ({
      label: this.translateService.instant(
        `CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.${pipt.toUpperCase()}.LABEL`,
      ),
      key: pipt,
    })),
  ];

  constructor() {
    effect(() => {
      const policyTypeOption = this.contractForm.policyTypeOption().value();
      untracked(() => {
        if (policyTypeOption !== this.previousPolicyTypeOption) {
          this.previousPolicyTypeOption = policyTypeOption;
          this.onPolicyTypeOptionChange(policyTypeOption);
        }
      });
    });

    effect(() => {
      const value = this.contractForm().value();
      untracked(() => {
        if (value !== this.resetValue) {
          this.updated.emit(true);
        }
      });
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['managementContract']) {
      const managementConctract: ManagementContract = changes['managementContract'].currentValue;

      this.resetForm(managementConctract);
    }
  }

  submit(): void {
    const subscription: Subscription = this.prepareSubmit()
      .pipe(tap((managementContract) => this.managementContract.set(managementContract)))
      .subscribe(() => subscription.unsubscribe());
  }

  prepareSubmit(): Observable<ManagementContract> {
    this.sending.set(true);
    return of(true).pipe(
      mergeMap(() => this.managementContractService.patch(this.getUpdatedManagementContract())),
      tap(
        () => this.sending.set(false),
        () => this.sending.set(false),
      ),
      tap(() => this.updated.emit(false)),
      tap(() => {
        this.contractForm().reset();
      }),
    );
  }

  resetForm(managementContract: ManagementContract): void {
    this.resetManagementContract = managementContract;

    const persistentIdentifierPolicies = managementContract.persistentIdentifierPolicyList || [];
    const [persistentIdentifierPolicy] = persistentIdentifierPolicies;
    const policyTypeOptionValue = persistentIdentifierPolicy?.persistentIdentifierPolicyType || '';
    this.resetValue = {
      ...this.managementContractToFormModelConverterService.convert(managementContract),
      policyTypeOption: policyTypeOptionValue,
    };
    this.previousPolicyTypeOption = policyTypeOptionValue;
    this.contractForm().reset(this.resetValue);

    this.updated.emit(false);
  }

  private onPolicyTypeOptionChange(policyTypeOption: string): void {
    let persistentIdentifierPolicies: PersistentIdentifierPolicyForm[] = [];

    if (policyTypeOption === PersistentIdentifierPolicyTypeEnum.ARK) {
      persistentIdentifierPolicies = this.managementContractToFormModelConverterService.convert(
        this.resetManagementContract,
      ).persistentIdentifierPolicies;
      if (persistentIdentifierPolicies.length === 0) {
        persistentIdentifierPolicies = this.managementContractToFormModelConverterService.getDefaultPersistentIdentifierPolicies();
      }
      const [firstPolicy, ...otherPolicies] = persistentIdentifierPolicies;
      persistentIdentifierPolicies = [{ ...firstPolicy, policyTypeOption: this.policyTypeOptions[1].key }, ...otherPolicies];
    }
    this.contractForm.persistentIdentifierPolicies().value.set(persistentIdentifierPolicies);
  }

  getUpdatedManagementContract(): ManagementContract {
    const updates = this.formModelToManagementContractConverterService.convert(this.contractForm().value());

    return {
      ...this.managementContract(),
      persistentIdentifierPolicyList: updates.persistentIdentifierPolicyList,
    };
  }

  isSubmitButtonDisabled(): boolean {
    const hasNoChanges = this.deepEqual(this.managementContract(), this.getUpdatedManagementContract());

    if (hasNoChanges) {
      this.updated.emit(false);
    }

    return this.contractForm().invalid() || !this.contractForm().dirty() || hasNoChanges;
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
