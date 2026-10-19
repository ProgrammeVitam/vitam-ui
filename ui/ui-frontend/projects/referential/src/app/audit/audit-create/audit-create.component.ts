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
import { Component, inject, Injector, OnDestroy, OnInit, viewChild, input, signal, Signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { form, FormField, FormRoot } from '@angular/forms/signals';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { EMPTY, forkJoin, of, Subject } from 'rxjs';
import { catchError, map, skip, switchMap, take, takeUntil } from 'rxjs/operators';
import {
  AccessContractService,
  AccessionRegisterSummary,
  AgencyService,
  ConfirmDialogService,
  ExternalParameters,
  ExternalParametersService,
  FilingPlanMode,
  Option,
  SelectComponent,
  SnackBarService,
  StartupService,
  VitamUICommonModule,
  VitamuiHttpHeaders,
  VitamUILibraryModule,
  VitamuiSelectOptions,
  requiredValue,
} from 'vitamui-library';
import { AuditAction, AuditPerimeter } from '../../models/audit.interface';
import { AuditService } from '../audit.service';
import { AuditCreateValidators } from './audit-create-validator';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { CdkStepper } from '@angular/cdk/stepper';

interface AuditCreateForm {
  auditActions: AuditAction;
  auditPerimeter: AuditPerimeter;
  evidenceAudit: string;
  objectId: string | number;
  originatingAgencyIds: string[];
  ingestOperationIds: string[];
  attachmentPositionIds: string[];
  startDate: string;
  endDate: string;
}

/** Fields of the dialog which are not sent in the request (their values are copied in the request fields) */
interface AuditCreateUiForm {
  allProducerServices: boolean;
  selectedNodes: { included: string[]; excluded: string[] };
  producerServicesMultiSelect: string[];
  ingestOperationsEntries: string;
  startDate: string;
  endDate: string;
}

type RequiredField =
  | 'auditPerimeter'
  | 'evidenceAudit'
  | 'objectId'
  | 'originatingAgencyIds'
  | 'ingestOperationIds'
  | 'attachmentPositionIds'
  | 'startDate';

@Component({
  selector: 'app-audit-create',
  templateUrl: './audit-create.component.html',
  styleUrls: ['./audit-create.component.scss'],
  imports: [
    CommonModule,
    MatButtonToggleModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    FormRoot,
    FormField,
    SelectComponent,
    VitamUICommonModule,
    VitamUILibraryModule,
    MatDialogModule,
    TranslatePipe,
  ],
})
export class AuditCreateComponent implements OnInit, OnDestroy {
  dialogRef = inject<MatDialogRef<AuditCreateComponent>>(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  private readonly confirmDialogService = inject(ConfirmDialogService);
  private readonly auditService = inject(AuditService);
  private readonly agencyService = inject(AgencyService);
  private readonly startupService = inject(StartupService);
  protected accessContractService = inject(AccessContractService);
  private readonly auditCreateValidator = inject(AuditCreateValidators);
  private readonly externalParameterService = inject(ExternalParametersService);
  private readonly snackBarService = inject(SnackBarService);
  private readonly injector = inject(Injector);

  readonly tenantIdentifier = input<number>(undefined);

  // Fields whose required validator is added/removed depending on the selected audit action and perimeter
  private readonly requiredFields = signal<Record<RequiredField, boolean>>({
    auditPerimeter: true,
    evidenceAudit: false,
    objectId: false,
    originatingAgencyIds: false,
    ingestOperationIds: false,
    attachmentPositionIds: false,
    startDate: false,
  });

  public readonly form = form(
    signal<AuditCreateForm>({
      auditActions: AuditAction.AUDIT_FILE_INTEGRITY,
      auditPerimeter: null,
      evidenceAudit: null,
      objectId: this.startupService.getTenantIdentifier(),
      originatingAgencyIds: [],
      ingestOperationIds: [],
      attachmentPositionIds: [],
      startDate: null,
      endDate: null,
    }),
    (path) => {
      requiredValue(path.auditActions);
      requiredValue(path.auditPerimeter, { when: () => this.requiredFields().auditPerimeter });
      requiredValue(path.evidenceAudit, { when: () => this.requiredFields().evidenceAudit });
      requiredValue(path.objectId, { when: () => this.requiredFields().objectId });
      requiredValue(path.originatingAgencyIds, { when: () => this.requiredFields().originatingAgencyIds });
      requiredValue(path.ingestOperationIds, { when: () => this.requiredFields().ingestOperationIds });
      requiredValue(path.attachmentPositionIds, { when: () => this.requiredFields().attachmentPositionIds });
      requiredValue(path.startDate, { when: () => this.requiredFields().startDate });
      this.auditCreateValidator.checkEvidenceAuditId(path.evidenceAudit);
    },
    {
      submission: {
        action: async () => {
          this.onSubmit();
        },
        ignoreValidators: 'all',
      },
    },
  );
  public readonly uiForm = form(
    signal<AuditCreateUiForm>({
      allProducerServices: false,
      selectedNodes: { included: [], excluded: [] },
      producerServicesMultiSelect: null,
      ingestOperationsEntries: null,
      startDate: '',
      endDate: '',
    }),
    (path) => {
      requiredValue(path.producerServicesMultiSelect);
      requiredValue(path.startDate, { when: () => this.isStartDateRequired() });
    },
  );
  public accessContractId: string = null;
  public producerServicesOptions: Option[] = [];
  public producerServicesMultiSelectOptions: VitamuiSelectOptions;
  public isDisabledButton = false;
  public refiningStep = false;
  public idsArray = ['originatingAgencyIds', 'ingestOperationIds', 'attachmentPositionIds'];
  public FILLING_PLAN_MODE_INCLUDE = FilingPlanMode.INCLUDE_ONLY;

  public auditPerimetersOptions: Option[];

  private readonly destroyer$ = new Subject<void>();

  private readonly stepper = viewChild<CdkStepper>('stepper');

  constructor() {
    const translateService = inject(TranslateService);

    this.auditPerimetersOptions = Object.keys(AuditPerimeter).map((key) => ({
      key: key,
      label: translateService.instant(`AUDIT.CREATE_DIALOG.PERIMETERS.${key}`),
    }));
  }

  ngOnInit() {
    this.externalParameterService
      .getUserExternalParameters()
      .pipe(switchMap((params: Map<string, string>) => this.loadAccessContractAndProducerServicesOptions(params)))
      .pipe(take(1))
      .subscribe(({ accessContractId, producerServicesOptions }) => {
        this.accessContractId = accessContractId;
        this.producerServicesOptions = producerServicesOptions;
        this.producerServicesMultiSelectOptions = {
          options: this.producerServicesOptions,
          customSorting: this.sortAlphabetically,
        };
      });

    this.confirmDialogService
      .listenToEscapeKeyPress(this.dialogRef)
      .pipe(takeUntil(this.destroyer$))
      .subscribe(() => this.onCancel());

    this.onValueChange(this.form.auditActions().value, (auditActions) => this.changeDefaultOnActionSelection(auditActions));
    this.onValueChange(this.form.auditPerimeter().value, (value) => this.updateFieldsOnAuditPerimeterChange(value));
    this.onValueChange(this.uiForm.allProducerServices().value, (value) => this.updateFieldsOnAllProducerServicesChange(value));
    this.onValueChange(this.uiForm.selectedNodes().value, (value) => this.updateAttachmentPositionIdsOnSelectionNodesChange(value));
    this.onValueChange(this.uiForm.producerServicesMultiSelect().value, (value) => this.updateOriginatingAgencyIdsOnChange(value));
    this.onValueChange(this.uiForm.ingestOperationsEntries().value, (value) => this.updateIngestOperationsEntriesOnChange(value));
    this.onValueChange(this.form.evidenceAudit().value, (value) => {
      if (this.form.auditActions().value() === AuditAction.AUDIT_FILE_RECTIFICATION) {
        this.form.objectId().value.set(value);
      }
    });
    this.onValueChange(this.uiForm.startDate().value, (value) => this.form.startDate().value.set(value));
    this.onValueChange(this.uiForm.endDate().value, (value) => this.form.endDate().value.set(value));
  }

  /** Calls `callback` when the value changes (not with the initial value) */
  private onValueChange<T>(value: Signal<T>, callback: (value: T) => void) {
    toObservable(value, { injector: this.injector }).pipe(skip(1), takeUntil(this.destroyer$)).subscribe(callback);
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public getAuditActions(): String[] {
    return Object.keys(AuditAction);
  }

  onPreviousStep() {
    if (this.refiningStep) {
      this.refiningStep = false;
      this.uiForm.startDate().value.set(null);
      this.uiForm.endDate().value.set(null);
      this.setRequired('startDate', false);
      this.form.startDate().reset();
      this.form.endDate().reset();
    }
  }

  private readonly sortAlphabetically = (a: Option, b: Option): number => {
    return a.label.toLocaleLowerCase() > b.label.toLocaleLowerCase() ? 1 : -1;
  };

  public showProducerToggle(): boolean {
    const selectedAuditAction = this.form.auditActions().value();
    return selectedAuditAction !== AuditAction.AUDIT_FILE_RECTIFICATION;
  }

  public showAuditPerimeter(): boolean {
    const selectedAuditAction = this.form.auditActions().value();
    return selectedAuditAction !== AuditAction.AUDIT_FILE_RECTIFICATION;
  }

  public showProducerSelection(): boolean {
    return this.showProducerToggle() && this.uiForm.allProducerServices().value() === false;
  }

  public secondStepTitleKey(): string {
    if (this.chooseOriginatingAgency()) return 'AUDIT.CREATE_DIALOG.CHOOSE_ORIGINATING_AGENCY';
    if (this.enterIngestOperationsIdentifiers()) return 'AUDIT.CREATE_DIALOG.ENTER_INGEST_OPERATIONS_IDENTIFIERS';
    if (this.chooseAttachmentPosition()) return 'AUDIT.CREATE_DIALOG.CHOOSE_ATTACHMENT_POSITION';
    if (this.chooseIngestOperationPeriod()) return 'AUDIT.CREATE_DIALOG.CHOOSE_INGEST_OPERATIONS_PERIOD';
    return null;
  }

  public chooseAttachmentPosition(): boolean {
    return this.form.auditPerimeter().value() === 'AUDIT_PERIMETER_ATTACHMENT_POSITION';
  }

  public enterIngestOperationsIdentifiers(): boolean {
    return this.form.auditPerimeter().value() === 'AUDIT_PERIMETER_INGEST_OPERATION_IDENTIFIER';
  }

  public chooseOriginatingAgency(): boolean {
    return this.form.auditPerimeter().value() === 'AUDIT_PERIMETER_ORIGINATING_AGENCY';
  }

  public chooseIngestOperationPeriod(): boolean {
    return this.form.auditPerimeter().value() === 'AUDIT_PERIMETER_INGEST_OPERATION_PERIOD';
  }

  public canShowPeriodErrorMessage(): boolean {
    return this.uiForm.startDate().value() && this.uiForm.endDate().value() && this.form.endDate().valid() && this.isDateIntervalInvalid();
  }

  public isStartDateRequired() {
    return this.requiredFields().startDate;
  }

  private loadAccessContractAndProducerServicesOptions(params: Map<string, string>) {
    const accessContractId = params.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
    if (!accessContractId || accessContractId.length < 1) {
      this.snackBarService.open({ message: 'SNACKBAR.NO_ACCESS_CONTRACT_LINKED' });
      return EMPTY;
    }

    return forkJoin({
      accessionRegisterSummaries: this.auditService.getAllAccessionRegister(accessContractId),
      originatingAgenciesOptions: this.agencyService.getOriginatingAgenciesAsOptions().pipe(catchError(() => of([]))),
    }).pipe(
      map(({ accessionRegisterSummaries, originatingAgenciesOptions }) => ({
        accessContractId,
        producerServicesOptions: this.buildProducerServicesOptions({ accessionRegisterSummaries, originatingAgenciesOptions }),
      })),
    );
  }

  private changeDefaultOnActionSelection(auditActions: AuditAction): void {
    // Update the validators
    if (auditActions === AuditAction.AUDIT_FILE_RECTIFICATION) {
      this.setRequired('evidenceAudit', true);
      this.setRequired('auditPerimeter', false);
    } else {
      this.clearField('evidenceAudit');
      this.setRequired('auditPerimeter', true);
    }
    this.clearField('originatingAgencyIds');
    this.clearField('ingestOperationIds');
    this.clearField('attachmentPositionIds');

    this.setRequired('startDate', false);
    this.form.startDate().reset();

    this.resetUiFields();
    this.form.objectId().value.set(this.startupService.getTenantIdentifier());
    this.updateObjectIdValidators();
  }

  private resetUiFields(): void {
    this.uiForm.producerServicesMultiSelect().value.set([]);
    this.uiForm.ingestOperationsEntries().value.set('');
    this.uiForm.startDate().value.set(null);
    this.uiForm.endDate().value.set(null);
    this.uiForm.allProducerServices().value.set(false);
  }

  private setRequired(field: RequiredField, required: boolean): void {
    this.requiredFields.update((requiredFields) => ({ ...requiredFields, [field]: required }));
  }

  private clearField(fieldName: Exclude<RequiredField, 'auditPerimeter' | 'objectId' | 'startDate'>): void {
    this.setRequired(fieldName, false);
    if (fieldName === 'evidenceAudit') {
      this.form.evidenceAudit().reset(null);
    } else {
      this.form[fieldName]().reset([]);
    }
  }

  private updateFieldsOnAllProducerServicesChange(allProducerServices: boolean): void {
    if (this.form.auditActions().value() !== AuditAction.AUDIT_FILE_RECTIFICATION) {
      const allOptions = this.producerServicesOptions.map((option) => option.key);
      this.form.originatingAgencyIds().value.set(allProducerServices ? allOptions : this.uiForm.producerServicesMultiSelect().value());
    }
    this.form.objectId().value.set(this.startupService.getTenantIdentifier());
    this.updateObjectIdValidators();
  }

  private updateFieldsOnAuditPerimeterChange(auditPerimeter: AuditPerimeter) {
    if (auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_ORIGINATING_AGENCY) {
      this.setRequired('originatingAgencyIds', true);
      this.clearField('ingestOperationIds');
      this.clearField('attachmentPositionIds');
      this.setRequired('startDate', false);
      this.form.startDate().reset();
    } else if (auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_INGEST_OPERATION_IDENTIFIER) {
      this.setRequired('ingestOperationIds', true);
      this.clearField('originatingAgencyIds');
      this.clearField('attachmentPositionIds');
      this.setRequired('startDate', false);
      this.form.startDate().reset();
    } else if (auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_ATTACHMENT_POSITION) {
      this.setRequired('attachmentPositionIds', true);
      this.clearField('originatingAgencyIds');
      this.clearField('ingestOperationIds');
      this.setRequired('startDate', false);
      this.form.startDate().reset();
    } else if (auditPerimeter === AuditPerimeter.AUDIT_PERIMETER_INGEST_OPERATION_PERIOD) {
      this.setRequired('startDate', true);
      this.clearField('originatingAgencyIds');
      this.clearField('attachmentPositionIds');
      this.clearField('ingestOperationIds');
    }
    this.resetUiFields();
    this.form.objectId().value.set(this.startupService.getTenantIdentifier());
    this.updateObjectIdValidators();
  }

  private updateIngestOperationsEntriesOnChange(value: string) {
    if (value?.length > 0) {
      const values = value.split(',');
      this.form.ingestOperationIds().value.set(values);
    } else {
      this.form.ingestOperationIds().value.set([]);
    }
  }

  private updateOriginatingAgencyIdsOnChange(values: Array<string>) {
    this.form.originatingAgencyIds().value.set(values);
  }

  private updateAttachmentPositionIdsOnSelectionNodesChange(value: { included: Array<string>; excluded: Array<string> }) {
    if (value && value.included && value.included.length > 0) {
      this.form.attachmentPositionIds().value.set(value.included);
    } else {
      this.form.attachmentPositionIds().value.set([]);
    }
  }

  /**
   * Add or remove the required validator on the field 'objectId'
   */
  private updateObjectIdValidators() {
    this.setRequired('objectId', this.form.auditActions().value() !== AuditAction.AUDIT_FILE_RECTIFICATION);
  }

  public initPeriodScreen() {
    this.refiningStep = true;
    setTimeout(() => this.stepper().next()); // Go to next step after it's been created (thanks to refiningStep=true)
    this.setRequired('startDate', true);
  }

  public isDateIntervalInvalid(): boolean {
    if (this.form.startDate().value() === null) return true;
    let startDate = new Date(this.form.startDate().value());
    if (this.form.endDate().value() === null) return false;
    let endDate = new Date(this.form.endDate().value());
    return this.form.endDate().invalid() || this.form.endDate().pending() || endDate < startDate;
  }

  isStepOneValid(): boolean {
    if (this.form.auditActions().value() === AuditAction.AUDIT_FILE_RECTIFICATION) {
      return !this.form.evidenceAudit().invalid() && !this.form.evidenceAudit().pending() && this.accessContractId != null;
    }
    return !this.form.auditPerimeter().invalid() && !this.form.auditPerimeter().pending();
  }

  canSubmit(): boolean {
    if (this.stepper().selectedIndex === 0) return this.isStepOneValid();

    const isOriginatingAgencyValid =
      this.form.auditPerimeter().value() === AuditPerimeter.AUDIT_PERIMETER_ORIGINATING_AGENCY &&
      this.accessContractId != null &&
      (this.uiForm.allProducerServices().value() ||
        (this.uiForm.producerServicesMultiSelect().value() && this.uiForm.producerServicesMultiSelect().value().length > 0));

    const isIngestOperationValid =
      this.form.auditPerimeter().value() === AuditPerimeter.AUDIT_PERIMETER_INGEST_OPERATION_IDENTIFIER &&
      this.accessContractId != null &&
      !this.form.ingestOperationIds().invalid() &&
      !this.form.ingestOperationIds().pending();

    const isAttachmentPositionValid =
      this.form.auditPerimeter().value() === AuditPerimeter.AUDIT_PERIMETER_ATTACHMENT_POSITION &&
      this.accessContractId != null &&
      this.uiForm.selectedNodes().value().included.length > 0;

    const isIngestPeriodValid =
      this.form.auditPerimeter().value() === AuditPerimeter.AUDIT_PERIMETER_INGEST_OPERATION_PERIOD &&
      this.accessContractId != null &&
      !this.form.startDate().invalid() &&
      !this.form.startDate().pending();

    if (this.refiningStep) {
      return (isOriginatingAgencyValid || isAttachmentPositionValid) && !this.isDateIntervalInvalid();
    }

    return (
      isAttachmentPositionValid ||
      isIngestOperationValid ||
      isOriginatingAgencyValid ||
      (isIngestPeriodValid && !this.isDateIntervalInvalid())
    );
  }

  onCancel() {
    if (this.form().dirty()) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.dialogRef.close();
    }
  }

  onSubmit() {
    if (this.form().invalid()) {
      this.isDisabledButton = true;
      return;
    }
    this.isDisabledButton = true;

    this.auditService
      .create(this.form().value(), new HttpHeaders().set(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID, this.accessContractId))
      .subscribe(
        () => {
          this.isDisabledButton = false;
          this.dialogRef.close({ success: true, action: 'none' });
        },
        () => {
          this.dialogRef.close({ success: false, action: 'none' });
        },
      );
  }

  private buildProducerServicesOptions({
    accessionRegisterSummaries,
    originatingAgenciesOptions,
  }: {
    accessionRegisterSummaries: AccessionRegisterSummary[];
    originatingAgenciesOptions: Option[];
  }): Option[] {
    const originatingAgencyLabelById = new Map(originatingAgenciesOptions.map((option) => [option.key, option.label]));

    return accessionRegisterSummaries.map((summary) => ({
      key: summary.originatingAgency,
      label: originatingAgencyLabelById.get(summary.originatingAgency) || summary.originatingAgency,
    }));
  }
}
