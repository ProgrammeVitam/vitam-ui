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
import { HttpHeaders } from '@angular/common/http';
import { Component, Inject, Input, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { EMPTY, Subject } from 'rxjs';
import { map, switchMap, take, takeUntil } from 'rxjs/operators';
import {
  AccessContractService,
  AccessionRegisterSummary,
  ConfirmDialogService,
  ExternalParameters,
  ExternalParametersService,
  FilingPlanMode,
  Option,
  StartupService,
  VitamuiAutocompleteMultiselectOptions,
  VitamUISnackBarService,
  CustomValidators,
  DatePattern,
} from 'vitamui-library';
import { AuditAction, AuditPerimeter } from '../../models/audit.interface';
import { AuditService } from '../audit.service';
import { AuditCreateValidators } from './audit-create-validator';

@Component({
  selector: 'app-audit-create',
  templateUrl: './audit-create.component.html',
  styleUrls: ['./audit-create.component.scss'],
})
export class AuditCreateComponent implements OnInit, OnDestroy {
  @Input() tenantIdentifier: number;

  public form: FormGroup;
  public stepIndex = 0;
  public stepCount = 2;
  public allProducerServices = new FormControl(false);
  public selectedNodes = new FormControl({ included: [], excluded: [] });
  public producerServicesMultiSelect = new FormControl();
  public ingestOperationsEntries = new FormControl();
  public startDateControl = new FormControl('');
  public endDateControl = new FormControl('');
  public accessContractId: string = null;
  public accessionRegisterSummaries: AccessionRegisterSummary[];
  public producerServicesOptions: Option[] = [];
  public producerServicesMultiSelectOptions: VitamuiAutocompleteMultiselectOptions;
  public isDisabledButton = false;
  public refiningScreen = false;
  public idsArray = ['originatingAgencyIds', 'ingestOperationIds', 'attachmentPositionIds'];
  public FILLING_PLAN_MODE_INCLUDE = FilingPlanMode.INCLUDE_ONLY;

  private destroyer$ = new Subject<void>();

  constructor(
    public dialogRef: MatDialogRef<AuditCreateComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private formBuilder: FormBuilder,
    private confirmDialogService: ConfirmDialogService,
    private auditService: AuditService,
    private startupService: StartupService,
    protected accessContractService: AccessContractService,
    private auditCreateValidator: AuditCreateValidators,
    private externalParameterService: ExternalParametersService,
    private snackBarService: VitamUISnackBarService,
  ) {}

  ngOnInit() {
    this.form = this.formBuilder.group({
      auditActions: [AuditAction.AUDIT_FILE_INTEGRITY, Validators.required],
      auditPerimeter: [null, Validators.required],
      evidenceAudit: [null, null, this.auditCreateValidator.checkEvidenceAuditId()],
      objectId: [this.startupService.getTenantIdentifier()],
      originatingAgencyIds: [[]],
      ingestOperationIds: [[]],
      attachmentPositionIds: [[]],
      startDate: [null],
      endDate: [null],
    });

    this.externalParameterService
      .getUserExternalParameters()
      .pipe(switchMap((params: Map<string, string>) => this.extractAccesContractIdAndGetAccessionRegisterSummaries(params)))
      .pipe(take(1))
      .subscribe((accessContractAndRegisterSummary) => {
        this.accessContractId = accessContractAndRegisterSummary.accessContractId;
        this.accessionRegisterSummaries = accessContractAndRegisterSummary.accessionRegisterSummaries;
        this.producerServicesOptions = this.accessionRegisterSummaries.map((summary) => ({
          key: summary.originatingAgency,
          label: summary.originatingAgency,
        }));
        this.producerServicesMultiSelectOptions = {
          options: this.producerServicesOptions,
          customSorting: this.sortAlphabetically,
        };
      });

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntil(this.destroyer$))
      .subscribe(() => this.onCancel());

    this.form.controls.auditActions.valueChanges
      .pipe(takeUntil(this.destroyer$))
      .subscribe((auditActions) => this.changeDefaultOnActionSelection(auditActions));

    this.form.controls.auditPerimeter.valueChanges
      .pipe(takeUntil(this.destroyer$))
      .subscribe((value) => this.updateFieldsOnAuditPerimeterChange(value));

    this.allProducerServices.valueChanges
      .pipe(takeUntil(this.destroyer$))
      .subscribe((value) => this.updateFieldsOnAllProducerServicesChange(value));

    this.selectedNodes.valueChanges
      .pipe(takeUntil(this.destroyer$))
      .subscribe((value) => this.updateAttachmentPositionIdsOnSelectionNodesChange(value));

    this.producerServicesMultiSelect.valueChanges
      .pipe(takeUntil(this.destroyer$))
      .subscribe((value) => this.updateOriginatingAgencyIdsOnChange(value));

    this.ingestOperationsEntries.valueChanges
      .pipe(takeUntil(this.destroyer$))
      .subscribe((value) => this.updateIngestOperationsEntriesOnChange(value));

    this.form.controls.evidenceAudit.valueChanges.pipe(takeUntil(this.destroyer$)).subscribe((value) => {
      if (this.form.get('auditActions').value === AuditAction.AUDIT_FILE_RECTIFICATION) {
        this.form.controls.objectId.setValue(value);
      }
    });

    this.startDateControl.valueChanges.pipe(takeUntil(this.destroyer$)).subscribe((value) => {
      this.form.controls.startDate.setValue(value);
    });

    this.endDateControl.valueChanges.pipe(takeUntil(this.destroyer$)).subscribe((value) => {
      this.form.controls.endDate.setValue(value);
    });
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public getAuditActions(): String[] {
    return Object.keys(AuditAction);
  }

  public getAuditPerimeters(): String[] {
    return Object.keys(AuditPerimeter);
  }

  moveToNextStep() {
    this.stepIndex = this.stepIndex + 1;
  }

  backToPreviousStep() {
    if (this.refiningScreen) {
      this.refiningScreen = false;
      this.startDateControl.setValue(null);
      this.endDateControl.setValue(null);
      this.form.get('startDate').clearValidators();
      this.form.get('startDate').markAsUntouched();
      this.form.get('endDate').clearValidators();
      this.form.get('endDate').markAsUntouched();
      this.form.get('startDate').updateValueAndValidity();
      this.form.get('endDate').updateValueAndValidity();
    } else {
      this.stepIndex = this.stepIndex - 1;
    }
  }

  private sortAlphabetically = (a: Option, b: Option): number => {
    return a.label.toLocaleLowerCase() > b.label.toLocaleLowerCase() ? 1 : -1;
  };

  public showProducerToggle(): boolean {
    const selectedAuditAction = this.form.get('auditActions').value;
    return selectedAuditAction !== AuditAction.AUDIT_FILE_RECTIFICATION;
  }

  public showAuditPerimeter(): boolean {
    const selectedAuditAction = this.form.get('auditActions').value;
    return selectedAuditAction !== AuditAction.AUDIT_FILE_RECTIFICATION;
  }

  public showProducerSelection(): boolean {
    return this.showProducerToggle() && this.allProducerServices.value === false;
  }

  public getStepCount(): number {
    return this.form.get('auditActions').value === AuditAction.AUDIT_FILE_RECTIFICATION ? 1 : 2;
  }

  public canShowRefiningScreen() {
    return (
      this.form.get('auditPerimeter')?.value === 'AUDIT_PERIMETER_INGEST_OPERATION_PERIOD' ||
      (['AUDIT_PERIMETER_ORIGINATING_AGENCY', 'AUDIT_PERIMETER_ATTACHMENT_POSITION'].includes(this.form.get('auditPerimeter')?.value) &&
        this.refiningScreen)
    );
  }

  public canShowPeriodErrorMessage() {
    return this.startDateControl.value && this.endDateControl.value && this.form.get('endDate').valid && this.isDateIntevalInvalid();
  }

  public checkifStartDateIsRequired() {
    return this.form.controls.startDate.hasValidator(Validators.required);
  }

  private extractAccesContractIdAndGetAccessionRegisterSummaries(params: Map<string, string>) {
    const accessContractId = params.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
    if (!accessContractId || accessContractId.length < 1) {
      this.snackBarService.open({ message: 'SNACKBAR.NO_ACCESS_CONTRACT_LINKED' });
      return EMPTY;
    }

    return this.auditService
      .getAllAccessionRegister(accessContractId)
      .pipe(map((accessionRegisterSummaries) => ({ accessContractId, accessionRegisterSummaries })));
  }

  private changeDefaultOnActionSelection(auditActions: AuditAction): void {
    // Update the validators
    if (auditActions === AuditAction.AUDIT_FILE_RECTIFICATION) {
      this.form.get('evidenceAudit').setValidators(Validators.required);
      this.form.get('auditPerimeter').clearValidators();
    } else {
      this.clearField('evidenceAudit');
      this.form.get('auditPerimeter').setValidators(Validators.required);
    }
    this.clearField('originatingAgencyIds');
    this.clearField('ingestOperationIds');
    this.clearField('attachmentPositionIds');

    this.form.get('startDate').clearValidators();
    this.form.get('startDate').markAsUntouched();

    this.producerServicesMultiSelect.setValue([]);
    this.ingestOperationsEntries.setValue([]);
    this.startDateControl.setValue(null);
    this.endDateControl.setValue(null);
    this.allProducerServices.setValue(false);
    this.form.get('objectId').setValue(this.startupService.getTenantIdentifier());
    this.form.get('evidenceAudit').updateValueAndValidity();
    this.form.get('originatingAgencyIds').updateValueAndValidity();
    this.form.get('ingestOperationIds').updateValueAndValidity();
    this.form.get('auditPerimeter').updateValueAndValidity();
    this.form.get('startDate').updateValueAndValidity();
    this.form.get('endDate').updateValueAndValidity();
    this.updateObjectIdValidators();
    this.form.updateValueAndValidity();
  }

  private clearField(fieldName: string): void {
    this.form.get(fieldName).clearValidators();
    this.form.get(fieldName).setValue(this.idsArray.includes(fieldName) ? [] : null);
    this.form.get(fieldName).markAsUntouched();
  }

  private updateFieldsOnAllProducerServicesChange(allProducerServices: boolean): void {
    if (this.form.controls.auditActions.value !== AuditAction.AUDIT_FILE_RECTIFICATION) {
      const allOptions = this.producerServicesOptions.map((option) => option.key);
      this.form.controls.originatingAgencyIds.setValue(allProducerServices ? allOptions : this.producerServicesMultiSelect.value);
    }
    this.form.controls.objectId.setValue(this.startupService.getTenantIdentifier());
    this.form.get('originatingAgencyIds').updateValueAndValidity();
    this.updateObjectIdValidators();
    this.form.updateValueAndValidity();
  }

  private updateFieldsOnAuditPerimeterChange(auditPerimeter: AuditPerimeter) {
    if (auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_ORIGINATING_AGENCY) {
      this.form.get('originatingAgencyIds').setValidators(Validators.required);
      this.clearField('ingestOperationIds');
      this.clearField('attachmentPositionIds');
      this.form.get('startDate').clearValidators();
      this.form.get('startDate').markAsUntouched();
    } else if (auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_INGEST_OPERATION_IDENTIFIER) {
      this.form.get('ingestOperationIds').setValidators(Validators.required);
      this.clearField('originatingAgencyIds');
      this.clearField('attachmentPositionIds');
      this.form.get('startDate').clearValidators();
      this.form.get('startDate').markAsUntouched();
    } else if (auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_ATTACHMENT_POSITION) {
      this.form.get('attachmentPositionIds').setValidators(Validators.required);
      this.clearField('originatingAgencyIds');
      this.clearField('ingestOperationIds');
      this.form.get('startDate').clearValidators();
      this.form.get('startDate').markAsUntouched();
    } else if (auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_INGEST_OPERATION_PERIOD) {
      this.form.get('startDate').setValidators([Validators.required, CustomValidators.date(DatePattern.YEAR_MONTH_DAY)]);
      this.form.get('endDate').setValidators([CustomValidators.date(DatePattern.YEAR_MONTH_DAY)]);
      this.clearField('originatingAgencyIds');
      this.clearField('attachmentPositionIds');
      this.clearField('ingestOperationIds');
    }
    this.producerServicesMultiSelect.setValue([]);
    this.ingestOperationsEntries.setValue([]);
    this.allProducerServices.setValue(false);
    this.startDateControl.setValue(null);
    this.endDateControl.setValue(null);
    this.form.get('objectId').setValue(this.startupService.getTenantIdentifier());
    this.form.get('ingestOperationIds').updateValueAndValidity();
    this.form.get('attachmentPositionIds').updateValueAndValidity();
    this.form.get('originatingAgencyIds').updateValueAndValidity();
    this.form.get('startDate').updateValueAndValidity();
    this.form.get('endDate').updateValueAndValidity();
    this.updateObjectIdValidators();
    this.form.updateValueAndValidity();
  }

  private updateIngestOperationsEntriesOnChange(value: string) {
    if (value.length > 0) {
      const values = value.split(',');
      this.form.controls.ingestOperationIds.setValue(values);
    } else {
      this.form.controls.ingestOperationIds.setValue([]);
    }
  }

  private updateOriginatingAgencyIdsOnChange(values: Array<string>) {
    this.form.controls.originatingAgencyIds.setValue(values);
  }

  private updateAttachmentPositionIdsOnSelectionNodesChange(value: { included: Array<string>; excluded: Array<string> }) {
    if (value && value.included && value.included.length > 0) {
      this.form.controls.attachmentPositionIds.setValue(value.included);
    } else {
      this.form.controls.attachmentPositionIds.setValue([]);
    }
  }

  /**
   * Add or remove the required validator on the field 'objectId'
   */
  private updateObjectIdValidators() {
    if (this.form.value.auditActions !== AuditAction.AUDIT_FILE_RECTIFICATION) {
      this.form.get('objectId').setValidators(Validators.required);
    } else {
      this.form.get('objectId').clearValidators();
    }
  }

  public initPeriodScreen() {
    this.refiningScreen = true;
    this.form.get('startDate').setValidators([Validators.required, CustomValidators.date(DatePattern.YEAR_MONTH_DAY)]);
    this.form.get('endDate').setValidators([CustomValidators.date(DatePattern.YEAR_MONTH_DAY)]);
  }

  public isDateIntevalInvalid(): boolean {
    if (this.form.get('startDate').value === null) return true;
    let startDate = new Date(this.form.get('startDate').value);
    if (this.form.get('endDate').value === null) return false;
    let endDate = new Date(this.form.get('endDate').value);
    return this.form.get('endDate').invalid || this.form.get('endDate').pending || endDate < startDate;
  }

  isStepOneValid(): boolean {
    if (this.form.value.auditActions === AuditAction.AUDIT_FILE_RECTIFICATION) {
      return !this.form.get('evidenceAudit').invalid && !this.form.get('evidenceAudit').pending && this.accessContractId != null;
    }
    return !this.form.get('auditPerimeter').invalid && !this.form.get('auditPerimeter').pending;
  }

  isStepTwoValid(): boolean {
    const isOriginatingAgencyValid =
      this.form.value.auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_ORIGINATING_AGENCY &&
      this.accessContractId != null &&
      (this.allProducerServices.value || (this.producerServicesMultiSelect.value && this.producerServicesMultiSelect.value.length > 0));

    const isIngestOperationValid =
      this.form.value.auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_INGEST_OPERATION_IDENTIFIER &&
      this.accessContractId != null &&
      !this.form.get('ingestOperationIds').invalid &&
      !this.form.get('ingestOperationIds').pending;

    const isAttachmentPositionValid =
      this.form.value.auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_ATTACHMENT_POSITION &&
      this.accessContractId != null &&
      this.selectedNodes.value.included.length > 0;

    const isIngestPeriodValid =
      this.form.value.auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_INGEST_OPERATION_PERIOD &&
      this.accessContractId != null &&
      !this.form.get('startDate').invalid &&
      !this.form.get('startDate').pending;

    if (this.refiningScreen) {
      return (isOriginatingAgencyValid || isAttachmentPositionValid) && !this.isDateIntevalInvalid();
    }

    return (
      isAttachmentPositionValid ||
      isIngestOperationValid ||
      isOriginatingAgencyValid ||
      (isIngestPeriodValid && !this.isDateIntevalInvalid())
    );
  }

  onCancel() {
    if (this.form.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form.invalid) {
      this.isDisabledButton = true;
      return;
    }
    this.isDisabledButton = true;

    this.auditService.create(this.form.value, new HttpHeaders({ 'X-Access-Contract-Id': this.accessContractId })).subscribe(
      () => {
        this.isDisabledButton = false;
        this.dialogRef.close({ success: true, action: 'none' });
      },
      () => {
        this.dialogRef.close({ success: false, action: 'none' });
      },
    );
  }
}
