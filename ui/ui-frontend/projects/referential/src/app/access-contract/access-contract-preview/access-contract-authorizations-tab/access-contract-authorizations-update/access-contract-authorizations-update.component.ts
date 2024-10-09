/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, EventEmitter, Inject, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn } from '@angular/forms';
import {
  AccessContract,
  AccessRightType,
  Option,
  VitamuiAutocompleteMultiselectOptions,
  AccessContractDisplay,
  AccessContractService,
} from 'vitamui-library';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { AgencyService } from '../../../../agency/agency.service';
import { RULE_TYPES } from '../../../../rule/rules.constants';

@Component({
  selector: 'app-access-contract-authorizations-update',
  templateUrl: './access-contract-authorizations-update.component.html',
  styleUrls: ['./access-contract-authorizations-update.component.scss'],
})
export class AccessContractAuthorizationsUpdateComponent implements OnInit {
  protected readonly AccessRightType = AccessRightType;
  originatingAgenciesOptions: VitamuiAutocompleteMultiselectOptions;
  ruleTypesOptions: VitamuiAutocompleteMultiselectOptions = { options: RULE_TYPES };

  accessContract: AccessContract;
  @Output() validateEvent: EventEmitter<any> = new EventEmitter();
  @Output() cancelEvent: EventEmitter<any> = new EventEmitter();

  form: FormGroup;
  updateMode = false;

  accessRightSelected: FormControl = new FormControl();

  constructor(
    public dialogRef: MatDialogRef<AccessContractAuthorizationsUpdateComponent>,
    private formBuilder: FormBuilder,
    private accessContractService: AccessContractService,
    private agencyService: AgencyService,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      accessContract: AccessContractDisplay;
    },
  ) {
    this.form = this.formBuilder.group(
      {
        accessRightSelected: this.accessRightSelected,
        originatingAgencies: [[]],
        ruleCategoryToFilter: [[]],
        ruleCategoryToFilterForTheOtherOriginatingAgencies: [[]],
        doNotFilterFilingSchemes: [true],
      },
      {
        validators: [this.checkFormValidity()],
      },
    );
    this.accessRightSelected.valueChanges.subscribe((value: AccessRightType) => {
      const doNotFilterFilingSchemesControl = this.form.controls.doNotFilterFilingSchemes;
      if (value === AccessRightType.ACCESS_FULL) {
        doNotFilterFilingSchemesControl.setValue(true);
        doNotFilterFilingSchemesControl.disable({ onlySelf: true });
      } else {
        doNotFilterFilingSchemesControl.enable({ onlySelf: true });
      }
    });
    this.form.controls.accessRightSelected.setValue(AccessRightType.ACCESS_FULL);
    if (data && data.accessContract) {
      this.updateMode = true;
      this.form.controls.accessRightSelected.setValue(data.accessContract.accessRightType);
      this.form.controls.originatingAgencies.setValue(data.accessContract.originatingAgencies || []);
      this.form.controls.ruleCategoryToFilter.setValue(data.accessContract.ruleCategoryToFilter || []);
      this.form.controls.ruleCategoryToFilterForTheOtherOriginatingAgencies.setValue(
        data.accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies || [],
      );
      this.form.controls.doNotFilterFilingSchemes.setValue(data.accessContract.doNotFilterFilingSchemes);
    }
  }

  ngOnInit() {
    this.getOriginatingAgenciesOptions();
  }

  private getOriginatingAgenciesOptions(): void {
    this.agencyService.getOriginatingAgenciesAsOptions().subscribe((options: Option[]) => (this.originatingAgenciesOptions = { options }));
  }

  onCancel() {
    this.cancelEvent.emit();
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
    switch (this.accessRightSelected.value) {
      case AccessRightType.ACCESS_FULL:
        accessContract.everyOriginatingAgency = true;
        accessContract.originatingAgencies = [];
        accessContract.ruleCategoryToFilter = [];
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = [];
        accessContract.doNotFilterFilingSchemes = this.form.get('doNotFilterFilingSchemes').value;
        break;
      case AccessRightType.ACCESS_BY_PRODUCERS:
        accessContract.everyOriginatingAgency = false;
        accessContract.originatingAgencies = this.form.get('originatingAgencies').value;
        accessContract.ruleCategoryToFilter = [];
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = [];
        accessContract.doNotFilterFilingSchemes = this.form.get('doNotFilterFilingSchemes').value;
        break;
      case AccessRightType.ACCESS_BY_EXPIRED_MANAGEMENT_RULES:
        accessContract.everyOriginatingAgency = true;
        accessContract.originatingAgencies = [];
        accessContract.ruleCategoryToFilter = this.form.get('ruleCategoryToFilter').value;
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = [];
        accessContract.doNotFilterFilingSchemes = this.form.get('doNotFilterFilingSchemes').value;
        break;
      case AccessRightType.ACCESS_BY_PRODUCERS_OR_EXPIRED_MANAGEMENT_RULES:
        accessContract.everyOriginatingAgency = false;
        accessContract.originatingAgencies = this.form.get('originatingAgencies').value;
        accessContract.ruleCategoryToFilter = [];
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = this.form.get(
          'ruleCategoryToFilterForTheOtherOriginatingAgencies',
        ).value;
        accessContract.doNotFilterFilingSchemes = this.form.get('doNotFilterFilingSchemes').value;
        break;
      case AccessRightType.ACCESS_BY_PRODUCERS_AND_EXPIRED_MANAGEMENT_RULES:
        accessContract.everyOriginatingAgency = false;
        accessContract.originatingAgencies = this.form.get('originatingAgencies').value;
        accessContract.ruleCategoryToFilter = this.form.get('ruleCategoryToFilter').value;
        accessContract.ruleCategoryToFilterForTheOtherOriginatingAgencies = [];
        accessContract.doNotFilterFilingSchemes = this.form.get('doNotFilterFilingSchemes').value;
        break;
    }
    return accessContract;
  }

  checkFormValidity(): ValidatorFn {
    return (form: FormGroup): ValidationErrors | null => {
      let errors = null;
      switch (this.accessRightSelected.value) {
        case AccessRightType.ACCESS_FULL:
          return null;
        case AccessRightType.ACCESS_BY_PRODUCERS:
          if (form.get('originatingAgencies').value.length < 1) {
            errors = { originatingAgencies: true };
          }
          return errors;
        case AccessRightType.ACCESS_BY_EXPIRED_MANAGEMENT_RULES:
          if (form.get('ruleCategoryToFilter').value.length < 1) {
            errors = { ruleCategoryToFilter: true };
          }
          return errors;
        case AccessRightType.ACCESS_BY_PRODUCERS_OR_EXPIRED_MANAGEMENT_RULES:
          if (form.get('originatingAgencies').value.length < 1) {
            errors = { originatingAgencies: true };
          }
          if (form.get('ruleCategoryToFilterForTheOtherOriginatingAgencies').value.length < 1) {
            errors = { ...errors, ruleCategoryToFilterForTheOtherOriginatingAgencies: true };
          }
          return errors;
        case AccessRightType.ACCESS_BY_PRODUCERS_AND_EXPIRED_MANAGEMENT_RULES:
          if (form.get('originatingAgencies').value.length < 1) {
            errors = { originatingAgencies: true };
          }
          if (form.get('ruleCategoryToFilter').value.length < 1) {
            errors = { ...errors, ruleCategoryToFilter: true };
          }
          return errors;
      }
    };
  }
}
