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
import { Component, inject, OnInit, output, signal } from '@angular/core';
import { disabled, form, FormField, validate, ValidationError } from '@angular/forms/signals';
import { MatRadioChange } from '@angular/material/radio';
import {
  AccessContract,
  AccessContractDisplay,
  AccessContractService,
  AccessRightType,
  AgencyService,
  DialogHeaderComponent,
  NextStepComponent,
  Option,
  PreviousStepComponent,
  requiredValue,
  SelectComponent,
  SlideToggleComponent,
  TooltipDirective,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef } from '@angular/material/dialog';
import { RULE_TYPES } from '../../../../rule/rules.constants';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import { TranslatePipe } from '@ngx-translate/core';

interface AccessContractAuthorizationsForm {
  accessRightSelected: AccessRightType;
  originatingAgencies: string[];
  ruleCategoryToFilter: string[];
  ruleCategoryToFilterForTheOtherOriginatingAgencies: string[];
  doNotFilterFilingSchemes: boolean;
}

const ORIGINATING_AGENCIES_ACCESS_RIGHTS = [
  AccessRightType.ACCESS_BY_PRODUCERS,
  AccessRightType.ACCESS_BY_PRODUCERS_OR_EXPIRED_MANAGEMENT_RULES,
  AccessRightType.ACCESS_BY_PRODUCERS_AND_EXPIRED_MANAGEMENT_RULES,
];
const RULE_CATEGORY_ACCESS_RIGHTS = [
  AccessRightType.ACCESS_BY_EXPIRED_MANAGEMENT_RULES,
  AccessRightType.ACCESS_BY_PRODUCERS_AND_EXPIRED_MANAGEMENT_RULES,
];

@Component({
  selector: 'app-access-contract-authorizations-update',
  templateUrl: './access-contract-authorizations-update.component.html',
  styleUrls: ['./access-contract-authorizations-update.component.scss'],
  imports: [
    DialogHeaderComponent,
    FormField,
    MatDialogContent,
    MatRadioGroup,
    MatRadioButton,
    SelectComponent,
    SlideToggleComponent,
    TooltipDirective,
    MatDialogActions,
    NextStepComponent,
    PreviousStepComponent,
    TranslatePipe,
  ],
})
export class AccessContractAuthorizationsUpdateComponent implements OnInit {
  dialogRef = inject<MatDialogRef<AccessContractAuthorizationsUpdateComponent>>(MatDialogRef);
  private readonly accessContractService = inject(AccessContractService);
  private readonly agencyService = inject(AgencyService);
  data = inject<{
    accessContract: AccessContractDisplay;
  }>(MAT_DIALOG_DATA);

  protected readonly AccessRightType = AccessRightType;
  originatingAgenciesOptions: VitamuiSelectOptions;
  ruleTypesOptions: VitamuiSelectOptions = { options: RULE_TYPES };

  accessContract: AccessContract;
  readonly validateEvent = output<any>();
  readonly cancelEvent = output<any>();

  updateMode = false;

  readonly form = form(
    signal<AccessContractAuthorizationsForm>({
      accessRightSelected: AccessRightType.ACCESS_FULL,
      originatingAgencies: [],
      ruleCategoryToFilter: [],
      ruleCategoryToFilterForTheOtherOriginatingAgencies: [],
      doNotFilterFilingSchemes: true,
    }),
    (path) => {
      requiredValue(path.originatingAgencies, {
        when: ({ valueOf }) => ORIGINATING_AGENCIES_ACCESS_RIGHTS.includes(valueOf(path.accessRightSelected)),
      });
      requiredValue(path.ruleCategoryToFilter, {
        when: ({ valueOf }) => RULE_CATEGORY_ACCESS_RIGHTS.includes(valueOf(path.accessRightSelected)),
      });
      requiredValue(path.ruleCategoryToFilterForTheOtherOriginatingAgencies, {
        when: ({ valueOf }) => valueOf(path.accessRightSelected) === AccessRightType.ACCESS_BY_PRODUCERS_OR_EXPIRED_MANAGEMENT_RULES,
      });
      disabled(path.doNotFilterFilingSchemes, ({ valueOf }) => valueOf(path.accessRightSelected) === AccessRightType.ACCESS_FULL);
      validate(path, ({ value }) => this.checkFormValidity(value()));
    },
  );

  constructor() {
    const data = this.data;

    if (data && data.accessContract) {
      this.updateMode = true;
      this.form().value.set({
        accessRightSelected: data.accessContract.accessRightType,
        originatingAgencies: data.accessContract.originatingAgencies || [],
        ruleCategoryToFilter: data.accessContract.ruleCategoryToFilter || [],
        ruleCategoryToFilterForTheOtherOriginatingAgencies: data.accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies || [],
        doNotFilterFilingSchemes: data.accessContract.doNotFilterFilingSchemes ?? null,
      });
    }
  }

  get accessRightSelected(): AccessRightType {
    return this.form.accessRightSelected().value();
  }

  onAccessRightChange({ value }: MatRadioChange): void {
    if (value === AccessRightType.ACCESS_FULL) {
      this.form.doNotFilterFilingSchemes().value.set(true);
    }
  }

  hasFormErrors(): boolean {
    return this.form().errors().length > 0;
  }

  ngOnInit() {
    this.getOriginatingAgenciesOptions();
  }

  private getOriginatingAgenciesOptions(): void {
    this.agencyService.getOriginatingAgenciesAsOptions().subscribe((options: Option[]) => (this.originatingAgenciesOptions = { options }));
  }

  onCancel() {
    this.cancelEvent.emit(undefined);
    this.dialogRef.close();
  }

  onValidate() {
    const formData = this.mapToAccessContract();
    if (this.updateMode) {
      this.updateAccessContract(formData);
    } else {
      this.validateEvent.emit(formData);
    }
  }

  updateAccessContract(formData: AccessContract) {
    this.accessContractService.patch(formData).subscribe((updatedAccessContract) => {
      this.dialogRef.close(updatedAccessContract);
    });
  }

  private mapToAccessContract(): AccessContract {
    const accessContract = {} as AccessContract;
    if (this.updateMode) {
      accessContract.id = this.data.accessContract.id;
      accessContract.identifier = this.data.accessContract.identifier;
    }
    switch (this.accessRightSelected) {
      case AccessRightType.ACCESS_FULL:
        accessContract.everyOriginatingAgency = true;
        accessContract.originatingAgencies = [];
        accessContract.ruleCategoryToFilter = [];
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = [];
        accessContract.doNotFilterFilingSchemes = this.form.doNotFilterFilingSchemes().value();
        break;
      case AccessRightType.ACCESS_BY_PRODUCERS:
        accessContract.everyOriginatingAgency = false;
        accessContract.originatingAgencies = this.form.originatingAgencies().value();
        accessContract.ruleCategoryToFilter = [];
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = [];
        accessContract.doNotFilterFilingSchemes = this.form.doNotFilterFilingSchemes().value();
        break;
      case AccessRightType.ACCESS_BY_EXPIRED_MANAGEMENT_RULES:
        accessContract.everyOriginatingAgency = true;
        accessContract.originatingAgencies = [];
        accessContract.ruleCategoryToFilter = this.form.ruleCategoryToFilter().value();
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = [];
        accessContract.doNotFilterFilingSchemes = this.form.doNotFilterFilingSchemes().value();
        break;
      case AccessRightType.ACCESS_BY_PRODUCERS_OR_EXPIRED_MANAGEMENT_RULES:
        accessContract.everyOriginatingAgency = false;
        accessContract.originatingAgencies = this.form.originatingAgencies().value();
        accessContract.ruleCategoryToFilter = [];
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = this.form
          .ruleCategoryToFilterForTheOtherOriginatingAgencies()
          .value();
        accessContract.doNotFilterFilingSchemes = this.form.doNotFilterFilingSchemes().value();
        break;
      case AccessRightType.ACCESS_BY_PRODUCERS_AND_EXPIRED_MANAGEMENT_RULES:
        accessContract.everyOriginatingAgency = false;
        accessContract.originatingAgencies = this.form.originatingAgencies().value();
        accessContract.ruleCategoryToFilter = this.form.ruleCategoryToFilter().value();
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = [];
        accessContract.doNotFilterFilingSchemes = this.form.doNotFilterFilingSchemes().value();
        break;
    }
    return accessContract;
  }

  private checkFormValidity(value: AccessContractAuthorizationsForm): ValidationError.WithoutFieldTree[] {
    const errors: ValidationError.WithoutFieldTree[] = [];
    if (ORIGINATING_AGENCIES_ACCESS_RIGHTS.includes(value.accessRightSelected) && value.originatingAgencies.length < 1) {
      errors.push({ kind: 'originatingAgencies' });
    }
    if (RULE_CATEGORY_ACCESS_RIGHTS.includes(value.accessRightSelected) && value.ruleCategoryToFilter.length < 1) {
      errors.push({ kind: 'ruleCategoryToFilter' });
    }
    if (
      value.accessRightSelected === AccessRightType.ACCESS_BY_PRODUCERS_OR_EXPIRED_MANAGEMENT_RULES &&
      value.ruleCategoryToFilterForTheOtherOriginatingAgencies.length < 1
    ) {
      errors.push({ kind: 'ruleCategoryToFilterForTheOtherOriginatingAgencies' });
    }
    return errors;
  }
}
