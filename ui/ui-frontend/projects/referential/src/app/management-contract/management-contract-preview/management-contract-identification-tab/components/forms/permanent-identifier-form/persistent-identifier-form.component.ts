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
import { AbstractControl, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { PersistentIdentifierPolicyTypeEnum } from 'ui-frontend-common';

interface PersistentIdentifierPolicyTypeOption {
  label: string;
  value: PersistentIdentifierPolicyTypeEnum | string;
}

interface ObjectUsageOption {
  label: string;
  value: string;
  disabled: false;
}

@Component({
  selector: 'app-persistent-identifier-form',
  templateUrl: './persistent-identifier-form.component.html',
  styleUrls: ['./persistent-identifier-form.component.scss'],
})
export class PersistentIdentifierFormComponent implements OnChanges {
  @Input() form: FormGroup;
  @Output() objectUsagePolicyAdded: EventEmitter<void> = new EventEmitter<void>();
  @Output() objectUsagePolicyRemoved: EventEmitter<void> = new EventEmitter<void>();

  policyTypeOptions: PersistentIdentifierPolicyTypeOption[] = [
    { label: 'CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.NONE.LABEL', value: '' },
    ...Object.values(PersistentIdentifierPolicyTypeEnum).map((pipt) => ({
      label: `CONTRACT_MANAGEMENT.FORM_UPDATE.PERMANENT_IDENTIFIER_POLICY_OPTION.${pipt.toUpperCase()}.LABEL`,
      value: pipt,
    })),
  ];
  objectUsageOptions: ObjectUsageOption[] = [
    { label: 'CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.BINARYMASTER.LABEL', value: 'BinaryMaster', disabled: false },
    { label: 'CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.DISSEMINATION.LABEL', value: 'Dissemination', disabled: false },
    { label: 'CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.PHYSICALMASTER.LABEL', value: 'PhysicalMaster', disabled: false },
    { label: 'CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.TEXTCONTENT.LABEL', value: 'TextContent', disabled: false },
    { label: 'CONTRACT_MANAGEMENT.FORM_UPDATE.OBJECT_USAGE_OPTION.THUMBNAIL.LABEL', value: 'Thumbnail', disabled: false },
  ];

  objectUsagePoliciesToggle = true;
  addButtonDisabled = false;

  constructor(private formBuilder: FormBuilder) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.form) {
      this.updateAddButtonState();

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
        objectUsage: [objectUsageOption.value, Validators.required],
        initialVersion: [true, Validators.required],
        intermediaryVersion: ['ALL', Validators.required],
      },
      { validators: [this.objectUsagePolicyValidator] },
    );

    this.objectUsagePolicies.push(objectUsagePolicy);
    this.objectUsagePolicyAdded.emit();
    this.updateAddButtonState();
  }

  findAvailableObjectUsageOption(): ObjectUsageOption {
    return this.objectUsageOptions.find((objectUsageOption) => {
      return this.getObjectUsagePolicies().every((objectUsagePolicy) => objectUsagePolicy.value.objectUsage !== objectUsageOption.value);
    });
  }

  getObjectUsagePolicies(): AbstractControl[] {
    return this.objectUsagePolicies.controls;
  }

  isObjectUsageOptionDisabled(optionValue: string): boolean {
    return this.getObjectUsagePolicies().some((policy: FormGroup) => policy.get('objectUsage').value === optionValue);
  }

  private get objectUsagePolicies(): FormArray {
    return this.form.get('objectUsagePolicies') as FormArray;
  }

  private updateAddButtonState(): void {
    this.addButtonDisabled = this.objectUsagePolicies.length >= this.objectUsageOptions.length;
  }

  private objectUsagePolicyValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const initialVersion = control.get('initialVersion');
    const intermediaryVersion = control.get('intermediaryVersion');

    if (initialVersion.value === false && intermediaryVersion.value === 'NONE') {
      return { invalidObjectUsagePolicy: true };
    }

    return null;
  }
}
