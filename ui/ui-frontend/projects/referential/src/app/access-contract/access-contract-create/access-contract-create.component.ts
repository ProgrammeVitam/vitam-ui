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
import { Component, Inject, Input, OnDestroy, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import {
  AccessContract,
  AccessRightType,
  ConfirmDialogService,
  ExternalParameters,
  ExternalParametersService,
  FilingPlanMode,
  Option,
  Status,
  VitamuiAutocompleteMultiselectOptions,
  VitamUISnackBarService,
  AccessContractService,
} from 'vitamui-library';
import { AgencyService } from '../../agency/agency.service';
import { RULE_TYPES } from '../../rule/rules.constants';
import { AccessContractCreateValidators } from './access-contract-create.validators';

import { finalize, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-access-contract-create',
  templateUrl: './access-contract-create.component.html',
  styleUrls: ['./access-contract-create.component.scss'],
})
export class AccessContractCreateComponent implements OnInit, OnDestroy {
  protected readonly AccessRightType = AccessRightType;
  protected readonly FILLING_PLAN_MODE = FilingPlanMode;
  @Input() tenantIdentifier: number;
  @Input() isSlaveMode: boolean;

  form: FormGroup;

  stepIndex = 0;
  stepCount = 4;
  accessRightSelected: AccessRightType = AccessRightType.ACCESS_FULL;

  private unsubscribe = new Subject<void>();

  allNodes = new FormControl(false);
  ruleFilter = new FormControl(false);
  selectNodesControl = new FormControl({ included: [], excluded: [] });
  accessContractSelect = new FormControl(null, Validators.required);

  isLoading = false;

  originatingAgenciesOptions: VitamuiAutocompleteMultiselectOptions = { options: [] };
  ruleTypesOptions: VitamuiAutocompleteMultiselectOptions = { options: RULE_TYPES };
  // FIXME: Get list from common var ?
  usages: Option[] = [
    { key: 'BinaryMaster', label: 'Archives numériques originales', info: '' },
    { key: 'Dissemination', label: 'Copies de diffusion', info: '' },
    { key: 'Thumbnail', label: 'Vignettes', info: '' },
    { key: 'TextContent', label: 'Contenu textuel', info: '' },
    { key: 'PhysicalMaster', label: 'Archives physiques', info: '' },
  ];
  private secondStepData: AccessContract;

  constructor(
    public dialogRef: MatDialogRef<AccessContractCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private accessContractCreateValidators: AccessContractCreateValidators,
    private accessContractService: AccessContractService,
    private agencyService: AgencyService,
    private confirmDialogService: ConfirmDialogService,
    private externalParameterService: ExternalParametersService,
    private vitamUISnackBarService: VitamUISnackBarService,
  ) {}

  ngOnInit() {
    this.loadAccessContract();
    this.agencyService.getOriginatingAgenciesAsOptions().subscribe((options: Option[]) => (this.originatingAgenciesOptions = { options }));
    this.initForm();

    this.form.get('thirdStep.downloadChoose').valueChanges.subscribe((val) => {
      this.form.get('thirdStep.everyDataObjectVersion').setValue(val === 'ALL', { emitEvent: false });
      if (val !== 'SELECTION') {
        this.form.get('thirdStep.dataObjectVersion').setValue([], { emitEvent: false });
      }
    });
    this.onWritingRestrictedDescChanges();
    this.selectNodesControl.valueChanges.subscribe((value: { included: string[]; excluded: string[] }) => {
      this.form.controls.rootUnits.setValue(value.included);
      this.form.controls.excludedRootUnits.setValue(value.excluded);
    });

    this.form.controls.name.valueChanges.subscribe((value) => {
      if (!this.isSlaveMode) {
        this.form.controls.identifier.setValue(value);
      }
    });

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntil(this.unsubscribe))
      .subscribe(() => this.onCancel());
  }

  ngOnDestroy() {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  onCancel(): void {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef, { subTitle: 'ACCESS_CONTRACT.CREATE_DIALOG.TITLE' });
    } else {
      this.dialogRef.close();
    }
  }

  private loadAccessContract(): void {
    this.externalParameterService
      .getUserExternalParameters()
      .pipe(takeUntil(this.unsubscribe))
      .subscribe((parameters) => {
        const accessContractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
        if (accessContractId && accessContractId.length > 0) {
          this.accessContractSelect.setValue(accessContractId);
        } else {
          this.vitamUISnackBarService.open({
            message: 'SNACKBAR.NO_ACCESS_CONTRACT_LINKED',
          });
        }
      });
  }

  private initForm(): void {
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required, this.accessContractCreateValidators.uniqueIdentifier()],
      status: [false],
      name: [null, [Validators.required], this.accessContractCreateValidators.uniqueName()],
      description: [null],
      accessLog: [false],
      /* <- step 3 -> */
      thirdStep: this.formBuilder.group(
        {
          writingPermission: [false],
          downloadChoose: ['ALL'],
          everyDataObjectVersion: [true],
          dataObjectVersion: [new Array<string>()],
          writingAuthorizedDesc: [false],
        },
        {
          validators: [this.thirdStepValidator()],
        },
      ),
      /* <- step 4 -> */
      rootUnits: [[], Validators.required],
      excludedRootUnits: [[]],
    });
  }

  onSubmit(): void {
    if (this.lastStepInvalid()) {
      return;
    }
    this.isLoading = true;
    const accessContract: AccessContract = this.mapToAccessContract(this.form);
    accessContract.status === 'ACTIVE'
      ? (accessContract.activationDate = new Date().toISOString())
      : (accessContract.deactivationDate = new Date().toISOString());

    this.accessContractService
      .create(accessContract)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (_accessContract) => this.dialogRef.close(true),
        error: (e) => console.error(e),
      });
  }

  public firstStepInvalid(): boolean {
    return (
      this.form.get('identifier').invalid ||
      this.form.get('identifier').pending ||
      this.form.get('name').invalid ||
      this.form.get('name').pending ||
      this.form.get('description').invalid ||
      this.form.get('description').pending ||
      this.form.get('status').invalid ||
      this.form.get('status').pending ||
      this.form.get('accessLog').invalid ||
      this.form.get('accessLog').pending ||
      (this.ruleFilter.value === true && (this.form.get('ruleCategoryToFilter').invalid || this.form.get('ruleCategoryToFilter').pending))
    );
  }

  public saveSecondStepData(accessContract: AccessContract): void {
    this.secondStepData = accessContract;
  }

  onWritingRestrictedDescChanges(): void {
    this.form.get('thirdStep.writingAuthorizedDesc').valueChanges.subscribe((val) => {
      if (val) {
        this.form.get('thirdStep.writingPermission').setValue(true, { emitEvent: false });
      }
    });
    this.form.get('thirdStep.writingPermission').valueChanges.subscribe((val) => {
      if (!val) {
        this.form.get('thirdStep.writingAuthorizedDesc').setValue(false, { emitEvent: false });
      }
    });
  }

  private thirdStepValidator(): ValidatorFn {
    return (form: FormGroup): ValidationErrors | null => {
      const downloadChoose = form.get('downloadChoose').value;
      const dataObjectVersion = form.get('dataObjectVersion').value;
      if (downloadChoose === 'SELECTION' && dataObjectVersion.length === 0) {
        return { dataObjectVersion: true };
      }
      return null;
    };
  }

  public lastStepInvalid(): boolean {
    return (
      this.allNodes.invalid ||
      this.allNodes.pending ||
      (this.allNodes.value === false && (this.form.controls.rootUnits.invalid || this.form.controls.rootUnits.value.length === 0))
    );
  }

  private mapToAccessContract(form: FormGroup): AccessContract {
    return {
      ...form.value,
      ...this.secondStepData,
      writingPermission: this.getControl(form, 'thirdStep.writingPermission').value,
      downloadChoose: this.getControl(form, 'thirdStep.downloadChoose').value,
      everyDataObjectVersion: this.getControl(form, 'thirdStep.everyDataObjectVersion').value,
      dataObjectVersion: this.getControl(form, 'thirdStep.dataObjectVersion').value,
      writingRestrictedDesc: !this.getControl(form, 'thirdStep.writingAuthorizedDesc').value,
      status: this.mapStatus(this.getControl(form, 'status').value),
      accessLog: this.mapStatus(this.getControl(form, 'accessLog').value),
    } as AccessContract;
  }

  private getControl(form: FormGroup, name: string): AbstractControl {
    return form.get(name);
  }

  private mapStatus(value: boolean): Status {
    return value ? Status.ACTIVE : Status.INACTIVE;
  }
}
