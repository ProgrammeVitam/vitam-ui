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
import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewChecked, ChangeDetectorRef, Component, Inject, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { finalize, forkJoin, Observable, of, throwError } from 'rxjs';
import { catchError, last, map, switchMap, tap } from 'rxjs/operators';
import { ProjectsService } from '../projects.service';
import { TransactionsService } from '../transactions.service';
import { ArchiveCollectService } from '../../archive-search-collect/archive-collect.service';
import { HttpEventType } from '@angular/common/http';
import {
  ExternalReferentialService,
  fetchTitle,
  FilingPlanMode,
  FilingPlanService,
  FlowType,
  ItemNode,
  Logger,
  MetadataUnitUp,
  oneIncludedNodeRequired,
  Option,
  Project,
  ProjectStatus,
  readFileContent,
  SchemaElement,
  SchemaService,
  SnackBarService,
  TENANT_SEPARATOR,
  TenantSelectionService,
  Transaction,
  TransactionStatus,
  Unit,
  Workflow,
  ZipFile,
  ZipFileStatus,
} from 'vitamui-library';

export enum ImportType {
  DIRECTORIES_FILES = 'DIRECTORIES_FILES',
  COMPRESSED = 'COMPRESSED',
  SIP = 'SIP',
}

export const LOCAL_ARCHIVING_SYSTEM_ID = 'local';

@Component({
  selector: 'app-create-project',
  templateUrl: './create-project.component.html',
  styleUrls: ['./create-project.component.scss'],
  animations: [
    trigger('rotateAnimation', [
      state('collapse', style({ transform: 'rotate(-180deg)' })),
      state('expand', style({ transform: 'rotate(0deg)' })),
      transition('expand <=> collapse', animate('200ms ease-out')),
    ]),
  ],
  standalone: false,
})
export class CreateProjectComponent implements OnInit, AfterViewChecked {
  protected readonly uploadMaxSizeInBytes = Math.pow(1024, 3); // 1 Gb
  // enums for html
  Workflow = Workflow;
  FilingPlanMode = FilingPlanMode;
  FlowType = FlowType;
  // http calls
  isLoading: boolean;

  selectedWorkflow: Workflow = Workflow.MANUAL;
  selectedFlowType: FlowType = FlowType.FIX;
  stepIndex = 0;

  projectForm: FormGroup;

  hasError = false;
  errorMessage: string;
  schemaOptions: ItemNode<SchemaElement>[];
  filesToUploadControl: FormControl<File[]> = new FormControl([], [Validators.required]);
  zipFileStatus$: Observable<ZipFileStatus>;
  units: Unit[];
  compressedZip: Blob;
  easOptions: Option[] = [];
  archiveProfileOptions: Option[] = [];
  archivalAgreementOptions: Option[] = [];
  agenciesOptions: Option[] = [];

  acquisitionInformationsList = [
    this.translationService.instant('ACQUISITION_INFORMATION.PAYMENT'),
    this.translationService.instant('ACQUISITION_INFORMATION.PROTOCOL'),
    this.translationService.instant('ACQUISITION_INFORMATION.PURCHASE'),
    this.translationService.instant('ACQUISITION_INFORMATION.COPY'),
    this.translationService.instant('ACQUISITION_INFORMATION.DATION'),
    this.translationService.instant('ACQUISITION_INFORMATION.DEPOSIT'),
    this.translationService.instant('ACQUISITION_INFORMATION.DEVOLUTION'),
    this.translationService.instant('ACQUISITION_INFORMATION.DONATION'),
    this.translationService.instant('ACQUISITION_INFORMATION.BEQUEST'),
    this.translationService.instant('ACQUISITION_INFORMATION.REINSTATEMENT'),
    this.translationService.instant('ACQUISITION_INFORMATION.OTHER'),
    this.translationService.instant('ACQUISITION_INFORMATION.UNKNOWN'),
  ];

  legalStatusList: Option[] = [
    { key: 'Public Archive', label: this.translationService.instant('LEGAL_STATUS.PUBLIC_ARCHIVE') },
    { key: 'Private Archive', label: this.translationService.instant('LEGAL_STATUS.PRIVATE_ARCHIVE') },
    { key: 'Public and Private Archive', label: this.translationService.instant('LEGAL_STATUS.PUBLIC_PRIVATE_ARCHIVE') },
  ];

  @ViewChild('confirmDeleteAddRuleDialog', { static: true }) confirmDeleteAddRuleDialog: TemplateRef<CreateProjectComponent>;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<CreateProjectComponent>,
    private dialogRefToClose: MatDialogRef<CreateProjectComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private projectsService: ProjectsService,
    private externalReferentialService: ExternalReferentialService,
    private tenantSelectionService: TenantSelectionService,
    private transactionsService: TransactionsService,
    private archiveCollectService: ArchiveCollectService,
    private logger: Logger,
    private cdr: ChangeDetectorRef,
    private translationService: TranslateService,
    public dialog: MatDialog,
    private schemaService: SchemaService,
    filingPlanService: FilingPlanService,
    private snackBarService: SnackBarService,
  ) {
    filingPlanService.loadFilingPlan().subscribe((units) => (this.units = units));
  }

  get linkParentIdControl() {
    return this.projectForm.controls.linkParentIdControl as FormControl;
  }

  ngOnInit(): void {
    this.initForm();
    this.schemaService.getDescriptiveSchemaTree().subscribe((schema) => (this.schemaOptions = schema));

    this.externalReferentialService.getElectronicArchivingSystemOptions$().subscribe((easOptions) => {
      this.easOptions = easOptions;
      this.projectForm.get('connectedToArchivingSystem')?.setValue(true);
      this.setDefaultArchivingSystemValue();
      this.onArchivingSystemChangeValue();
    });
  }

  ngAfterViewChecked(): void {
    this.cdr.detectChanges();
  }

  onConnectedToArchivingSystemChangeValue(): void {
    this.resetExternalReferentialIdentifiers();
    if (!this.connectedToArchivingSystem) {
      this.projectForm.get('archivingSystem')?.setValue(null);
    } else if (this.easOptions.length < 2) {
      // has single Option
      this.setDefaultArchivingSystemValue();
    }
  }

  resetExternalReferentialIdentifiers(): void {
    this.projectForm.get('submissionAgencyIdentifier')?.setValue(null);
    this.projectForm.get('originatingAgencyIdentifier')?.setValue(null);
    this.projectForm.get('archivalAgencyIdentifier')?.setValue(null);
    this.projectForm.get('transferringAgencyIdentifier')?.setValue(null);
    this.projectForm.get('archivalAgreement')?.setValue(null);
    this.projectForm.get('archiveProfile')?.setValue(null);
  }

  onArchivingSystemChangeValue(): void {
    this.resetExternalReferentialIdentifiers();

    const input: string = this.projectForm.get('archivingSystem')?.value;
    if (!input) return;

    const [archivingSystemId, tenantStr] = input.split(TENANT_SEPARATOR);
    const tenantIdentifier = Number(tenantStr);

    if (!archivingSystemId || isNaN(tenantIdentifier)) return;

    forkJoin({
      archivalAgreements: this.externalReferentialService.archivalIngestContracts(archivingSystemId, tenantIdentifier),
      archiveProfiles: this.externalReferentialService.archiveProfiles(archivingSystemId, tenantIdentifier),
      agencies: this.externalReferentialService.getAgencies(archivingSystemId, tenantIdentifier),
    })
      .pipe(
        map(({ archivalAgreements, archiveProfiles, agencies }) => ({
          archivalAgreementOptions: this.mapToOptions(archivalAgreements),
          archiveProfileOptions: this.mapToOptions(archiveProfiles),
          agenciesOptions: this.mapToOptions(agencies),
        })),
      )
      .subscribe(({ archivalAgreementOptions, archiveProfileOptions, agenciesOptions }) => {
        this.archivalAgreementOptions = archivalAgreementOptions;
        this.archiveProfileOptions = archiveProfileOptions;
        this.agenciesOptions = agenciesOptions;
      });
  }

  onClose() {
    this.dialogRefToClose.close(true);
  }

  setWorkflow(value: Workflow) {
    this.selectedWorkflow = value;
  }

  setFlowType(value: FlowType) {
    this.selectedFlowType = value;
  }

  getSchemaElementDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

  prepareRulesAndMoveToNextStep() {
    if (this.selectedFlowType === FlowType.RULES && this.rulesParams.length === 0) {
      this.addRuleParam();
    }
    this.moveToNextStep();
  }

  moveToNextStep() {
    this.stepIndex = this.stepIndex + 1;
  }

  backToPreviousStep() {
    this.stepIndex = this.stepIndex - 1;
  }

  async handleJsltFile(files: File[]) {
    const jsltFile = files?.length ? files[0] : undefined;
    if (jsltFile) {
      try {
        const content: string = await readFileContent(jsltFile);
        this.projectForm.get('transformationRules').setValue(content);
      } catch (error) {
        this.logger.error('Error reading JSLT file:', error);
        this.isLoading = false;
      }
    }
  }

  /*** Form validator Step : /*** Form validator Step : Description du versement ***/
  stepConnectingToRefEASIsInvalid() {
    return this.connectedToArchivingSystem && this.projectForm.controls.archivingSystem.invalid;
  }

  importTypeIsInvalid() {
    return this.projectForm.controls.importType.invalid;
  }

  /*** Form validator Step : Description du versement ***/
  stepDescriptionIsInvalid() {
    return (
      this.projectForm.controls.originatingAgencyIdentifier.invalid ||
      this.projectForm.controls.messageIdentifier.invalid ||
      this.projectForm.controls.submissionAgencyIdentifier.invalid
    );
  }

  /*** Form validator Step : Contexte du versement ***/
  stepContextIsInvalid() {
    return (
      this.projectForm.controls.archivalAgencyIdentifier.invalid ||
      this.projectForm.controls.transferringAgencyIdentifier.invalid ||
      this.projectForm.controls.archivalAgreement.invalid
    );
  }

  /*** Form validator Step : Parametrer les regles de rattachement ***/
  stepRulesParamsIsInvalid() {
    return this.projectForm.controls.rulesParams.invalid;
  }

  /*** Step 5 : Téléchargements ***/
  close() {
    this.dialogRef.close(true);
  }

  /*** All Steps ***/
  private initForm() {
    this.projectForm = this.formBuilder.group({
      automaticIngest: [true],
      referentialCheckup: [false],
      connectedToArchivingSystem: [false],
      archivingSystem: [null],
      archivalAgreement: [null],
      messageIdentifier: [null],
      archivalAgencyIdentifier: [null],
      transferringAgencyIdentifier: [null],
      originatingAgencyIdentifier: [null],
      submissionAgencyIdentifier: [null],
      // add archivalProfile ?
      archiveProfile: [null],
      acquisitionInformation: [null],
      legalStatus: [null],
      // for unitUp :
      linkParentIdControl: [{ included: [], excluded: [] }],
      // for unitUps :
      rulesParams: this.formBuilder.array([], Validators.required),
      comment: [null],
      status: [null],
      transformationRules: [null],
      unitUp: [null],
      importType: [null],
    });
  }

  private formToProject(): Project {
    const archivingSystemIdAndTenant: string = this.projectForm.get('archivingSystem')?.value;

    const [archivingSystemId, tenantStr] = archivingSystemIdAndTenant ? archivingSystemIdAndTenant.split(TENANT_SEPARATOR) : [null, null];
    const archivingSystemTenant = tenantStr ? Number(tenantStr) : null;

    const project: Project = {
      name: this.projectForm.value.messageIdentifier,
      archivalAgreement: this.projectForm.value.archivalAgreement,
      messageIdentifier: this.projectForm.value.messageIdentifier,
      archivalAgencyIdentifier: this.projectForm.value.archivalAgencyIdentifier,
      transferringAgencyIdentifier: this.projectForm.value.transferringAgencyIdentifier,
      originatingAgencyIdentifier: this.projectForm.value.originatingAgencyIdentifier,
      submissionAgencyIdentifier: this.projectForm.value.submissionAgencyIdentifier,
      archivalProfile: this.projectForm.value.archivalProfile,
      archiveProfile: this.projectForm.value.archiveProfile,
      acquisitionInformation: this.projectForm.value.acquisitionInformation,
      legalStatus: this.projectForm.value.legalStatus,
      comment: this.projectForm.value.comment,
      connectedToArchivingSystem: this.projectForm.value.connectedToArchivingSystem,
      archivingSystemId: archivingSystemId,
      archivingSystemTenant: archivingSystemTenant,
      status: ProjectStatus.OPEN,
      transformationRules: this.projectForm.value.transformationRules,
      automaticIngest: this.selectedWorkflow === Workflow.MANUAL ? null : this.projectForm.value.automaticIngest === true,
    } as Project;
    if (this.selectedWorkflow === Workflow.MANUAL || this.selectedFlowType === FlowType.FIX) {
      project.unitUp = this.connectedToArchivingSystem ? this.linkParentIdControl.value.included[0] : this.projectForm.value.unitUp;
    } else {
      project.unitUps = this.convertRuleParamsToMetadata();
    }
    return project as Project;
  }

  private convertRuleParamsToMetadata(): Array<MetadataUnitUp> {
    return this.rulesParams.controls.map((ruleParamControl: FormGroup) => {
      const ruleParam = ruleParamControl.value;
      const metadataKey = ruleParam.ontologyList.ApiField;
      const metadataValue = ruleParam.metadataValue;
      return {
        metadataKey: metadataKey,
        metadataValue: metadataValue,
        unitUp: this.connectedToArchivingSystem ? ruleParam.unitUp.included[0] : ruleParam.unitUp,
      };
    });
  }

  get rulesParams(): FormArray<FormGroup> {
    return this.projectForm.controls.rulesParams as FormArray<FormGroup>;
  }

  addRuleParam() {
    const ontologyListControl = this.formBuilder.control<SchemaElement>(undefined, Validators.required);
    const metadataValueControl = this.formBuilder.control(undefined, Validators.required);
    ontologyListControl.valueChanges.subscribe(() => {
      metadataValueControl.setValidators(Validators.required); // To override Validators that may be added by datepicker if changing from datepicker to input
      metadataValueControl.markAsTouched(); // To make sure error messages appear after changing the ontology
    });

    // rulesParams interface:
    const newRuleParamForm = this.formBuilder.group({
      ontologyList: ontologyListControl,
      metadataValue: metadataValueControl,
      unitUp: this.connectedToArchivingSystem ? [{ included: [], excluded: [] }, oneIncludedNodeRequired()] : [''],
    });

    this.rulesParams.push(newRuleParamForm);
  }

  deleteRuleParam(index: number) {
    this.rulesParams.removeAt(index);
  }

  validateAndCreateProject() {
    if (this.selectedWorkflow === Workflow.MANUAL) {
      this.createProjectAndTransactionAndUpload();
    } else {
      this.createProject();
    }
  }

  private createProject() {
    this.isLoading = true;
    const project: Project = this.formToProject();
    this.moveToNextStep();
    this.projectsService
      .create(project)
      .pipe(finalize(() => (this.isLoading = false)))
      .subscribe({
        next: (_result) => {
          this.snackBarService.open({
            message: 'COLLECT.MODAL.PROJECT_CREATED',
            duration: 10_000,
          });
        },
        error: (_error) => {
          this.hasError = true;
          this.errorMessage = _error.error.message;
          this.snackBarService.open({
            message: 'COLLECT.MODAL.PROJECT_CREATION_ERROR',
            duration: 10_000,
          });
        },
      });
  }

  private createProjectAndTransactionAndUpload() {
    let transactionId: string;
    this.isLoading = true;
    const project: Project = this.formToProject();
    this.moveToNextStep();
    const zipFile = new ZipFile();
    this.zipFileStatus$ = zipFile.zipFileStatus$;
    if ([ImportType.COMPRESSED, ImportType.SIP].includes(this.importType as ImportType)) {
      this.compressedZip = new Blob(this.filesToUploadControl.value, { type: 'application/zip' });
    }
    this.projectsService
      .create(project)
      .pipe(
        map((createProjectResponse) => createProjectResponse.id as string),
        map(
          (createdProjectId) =>
            ({
              status: TransactionStatus.OPEN,
              projectId: createdProjectId,
            }) as Transaction,
        ),
        switchMap((transaction) => this.transactionsService.create(transaction)),
        tap((createdTransactionResponse) => {
          transactionId = createdTransactionResponse.id;
          if (this.importType === ImportType.SIP) {
            zipFile.setZipName(this.filesToUploadControl.value[0].name + '.zip');
          } else {
            zipFile.setZipName(transactionId + '.zip');
          }
          if ([ImportType.COMPRESSED, ImportType.SIP].includes(this.importType as ImportType)) {
            zipFile.zipFileStatus.size = this.compressedZip.size;
            zipFile.zipFileStatus.currentFileUploadedSize = 100;
          }
        }),
        switchMap(() =>
          this.importType === ImportType.DIRECTORIES_FILES
            ? zipFile.addFiles(this.filesToUploadControl.value).generateZip()
            : of(this.compressedZip).toPromise(),
        ),
        switchMap((content) =>
          this.importType === ImportType.SIP
            ? this.archiveCollectService.uploadSip(content, transactionId)
            : this.archiveCollectService.uploadZip(content, transactionId),
        ),
        tap((httpEvent) => zipFile.updateUploadingZipFileStatus(httpEvent)),
        last((httpEvent) => httpEvent.type === HttpEventType.Response),
        finalize(() => {
          this.isLoading = false;
          this.snackBarService.open({
            message: 'COLLECT.UPLOAD.TERMINATED',
            duration: 10_000,
          });
        }),
        catchError((error) => {
          this.logger.error(error);
          return throwError(error);
        }),
      )
      .subscribe();
  }

  getNodeTitle(selectedNode?: any): string {
    if (this.connectedToArchivingSystem) {
      if (!selectedNode?.unitUp?.included?.length) return '';
      const vitamId = selectedNode?.unitUp?.included[0];
      if (!vitamId || !this.units) return '';
      const foundNode = this.units.find((unit) => unit['#id'] === vitamId);
      return foundNode ? ` : ${fetchTitle(foundNode.Title, foundNode.Title_)}` : '';
    } else {
      return selectedNode?.unitUp ? ` : ${selectedNode.unitUp}` : '';
    }
  }

  getName(item: SchemaElement): string {
    const path = item.Path.split('.').slice(0, -1);
    const parent = path.reduce((acc, p) => acc.children.find((o) => o.item.FieldName === p), {
      children: this.schemaOptions,
    } as ItemNode<SchemaElement>);
    return `${item.ShortName}${parent?.item ? ` (${parent.item.ShortName})` : ''}`;
  }

  get connectedToArchivingSystem(): boolean {
    return this.projectForm.get('connectedToArchivingSystem')?.value === true;
  }

  get connectedToLocalEasWithCurrentTenant(): boolean {
    const currentTenantId = this.tenantSelectionService.getSelectedTenant().identifier;
    const defaultKey = `${LOCAL_ARCHIVING_SYSTEM_ID}${TENANT_SEPARATOR}${currentTenantId}`;
    const archivingSystemValue = this.projectForm.get('archivingSystem')?.value;
    return archivingSystemValue === defaultKey;
  }

  private mapToOptions<T extends { identifier: string; name: string }>(items: T[]): { label: string; key: string }[] {
    return items.map((item) => ({
      label: `${item.identifier} - ${item.name}`,
      key: item.identifier,
    }));
  }

  resetFilesToImportList() {
    this.filesToUploadControl.reset();
  }

  private setDefaultArchivingSystemValue() {
    const currentTenantId = this.tenantSelectionService.getSelectedTenant().identifier;
    const defaultKey = `${LOCAL_ARCHIVING_SYSTEM_ID}${TENANT_SEPARATOR}${currentTenantId}`;
    this.projectForm.get('archivingSystem')?.setValue(defaultKey);
  }

  get importType(): string {
    return this.projectForm.get('importType')?.value;
  }

  protected ImportType = ImportType;
}
