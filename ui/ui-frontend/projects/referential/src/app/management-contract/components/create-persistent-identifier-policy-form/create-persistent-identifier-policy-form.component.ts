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
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges, inject } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Option, PersistentIdentifierPolicyTypeEnum } from 'vitamui-library';
import { ManagementContractValidationErrors, ManagementContractValidators } from '../../validators/management-contract-validators';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-create-persistent-identifier-policy-form',
  templateUrl: './create-persistent-identifier-policy-form.component.html',
  styleUrls: ['./create-persistent-identifier-policy-form.component.scss'],
  standalone: false,
})
export class CreatePersistentIdentifierPolicyFormComponent implements OnChanges {
  private formBuilder = inject(FormBuilder);
  private translateService = inject(TranslateService);

  @Input() form: FormGroup;
  @Output() objectUsagePolicyAdded: EventEmitter<void> = new EventEmitter<void>();
  @Output() objectUsagePolicyRemoved: EventEmitter<void> = new EventEmitter<void>();

  authorityErrorMap = {
    [ManagementContractValidationErrors.INVALID_AUTHORITY]: this.translateService.instant(
      'CONTRACT_MANAGEMENT.FORM_UPDATE.ERROR_MESSAGES.INVALID_AUTHORITY',
    ),
  };

  policyTypeOptions: Option[] = [
    { label: 'CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.NONE.LABEL', key: '' },
    ...Object.values(PersistentIdentifierPolicyTypeEnum).map((pipt) => ({
      label: `CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.${pipt.toUpperCase()}.LABEL`,
      key: pipt,
    })),
  ];
  objectUsageOptions: Option[] = [
    {
      key: 'BinaryMaster',
      label: this.translateService.instant('CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.BINARYMASTER.LABEL'),
      disabled: false,
    },
    {
      key: 'Dissemination',
      label: this.translateService.instant('CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.DISSEMINATION.LABEL'),
      disabled: false,
    },
    {
      key: 'PhysicalMaster',
      label: this.translateService.instant('CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.PHYSICALMASTER.LABEL'),
      disabled: false,
    },
    {
      key: 'TextContent',
      label: this.translateService.instant('CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.TEXTCONTENT.LABEL'),
      disabled: false,
    },
    {
      key: 'Thumbnail',
      label: this.translateService.instant('CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.THUMBNAIL.LABEL'),
      disabled: false,
    },
  ];

  objectUsagePoliciesToggle = false;
  addButtonDisabled = false;
  isExistingTypeOption = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.form) {
      this.updateAddButtonState();
      this.isExistingTypeOption = this.form.get('policyTypeOption').value !== '';
      this.objectUsagePoliciesToggle = this.form.get('shouldConcernObjects').value;

      this.form.get('shouldConcernObjects').valueChanges.subscribe((shouldConcernObjects) => {
        this.objectUsagePoliciesToggle = shouldConcernObjects;

        const formArray = this.form.get('objectUsagePolicies') as FormArray;
        if (!shouldConcernObjects) {
          while (formArray.length !== 0) {
            formArray.removeAt(0);
          }
        }

        if (formArray.length === 0 && shouldConcernObjects) {
          this.addObjectUsagePolicy();
        }
      });

      this.form.get('objectUsagePolicies').valueChanges.subscribe((objectUsagePolicies) => {
        this.objectUsageOptions.forEach(
          (objectUsageOption) => (objectUsageOption.disabled = this.isObjectUsageOptionDisabled(objectUsageOption.key)),
        );

        this.form.get('shouldConcernObjects').setValue(objectUsagePolicies.length > 0);
      });

      this.form.get('policyTypeOption').valueChanges.subscribe((policyTypeOption: string) => {
        this.isExistingTypeOption = policyTypeOption !== '';
      });
    }
  }

  toggle($event: Event): void {
    const element = $event.target as any;

    if (['col', 'row', 'header'].some((cssClass) => element.className.includes(cssClass))) {
      this.objectUsagePoliciesToggle = !this.objectUsagePoliciesToggle;
    }
  }

  removeObjectUsagePolicy(index: number): void {
    this.objectUsagePolicies.removeAt(index);
    this.objectUsagePolicyRemoved.emit();
    this.updateAddButtonState();
  }

  addObjectUsagePolicy(): void {
    const objectUsageOption = this.findAvailableObjectUsageOption();
    if (!objectUsageOption) {
      this.updateAddButtonState();
      return;
    }

    const objectUsagePolicy: FormGroup = this.formBuilder.group(
      {
        objectUsage: [objectUsageOption.key, Validators.required],
        initialVersion: [true, Validators.required],
        intermediaryVersion: ['ALL', Validators.required],
      },
      { validators: [ManagementContractValidators.objectUsagePolicyValidator] },
    );

    this.objectUsagePolicies.push(objectUsagePolicy);
    this.objectUsagePolicyAdded.emit();
    this.updateAddButtonState();
  }

  private findAvailableObjectUsageOption(): Option {
    return this.objectUsageOptions.find((objectUsageOption) => {
      return this.getObjectUsagePolicies().every((objectUsagePolicy) => objectUsagePolicy.value.objectUsage !== objectUsageOption.key);
    });
  }

  getObjectUsagePolicies(): AbstractControl[] {
    return this.objectUsagePolicies.controls;
  }

  private isObjectUsageOptionDisabled(optionValue: string): boolean {
    return this.getObjectUsagePolicies().some((policy: FormGroup) => policy.get('objectUsage').value === optionValue);
  }

  private get objectUsagePolicies(): FormArray {
    return this.form.get('objectUsagePolicies') as FormArray;
  }

  private updateAddButtonState(): void {
    this.addButtonDisabled = this.objectUsagePolicies.length >= this.objectUsageOptions.length;
  }
}
