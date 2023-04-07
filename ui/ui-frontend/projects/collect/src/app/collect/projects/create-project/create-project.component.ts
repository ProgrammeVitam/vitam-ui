/*
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
import { animate, state, style, transition, trigger } from '@angular/animations';
import {
  AfterViewChecked,
  ChangeDetectorRef,
  Component,
  Inject,
  OnDestroy,
  OnInit,
  TemplateRef,
  ViewChild
} from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import {
  ExternalParameters,
  ExternalParametersService,
  Logger,
  MetadataUnitUp,
  Ontology,
  Project,
  ProjectStatus,
  Transaction,
  TransactionStatus
} from 'ui-frontend-common';

import { FilingPlanMode } from 'vitamui-library';
import {
  oneIncludedNodeRequired
} from '../../../../../../vitamui-library/src/lib/components/filing-plan/filing-plan.service';
import { ArchiveCollectService } from '../../archive-search-collect/archive-collect.service';
import { FlowType, Workflow } from '../../core/models/create-project.interface';
import { CollectUploadFile, CollectZippedUploadFile } from '../../shared/collect-upload/collect-upload-file';
import { CollectUploadService } from '../../shared/collect-upload/collect-upload.service';
import { ProjectsService } from '../projects.service';
import { TransactionsService } from '../transactions.service';

@Component({
  selector: 'app-create-project',
  templateUrl: './create-project.component.html',
  styleUrls: ['./create-project.component.scss'],
  animations: [
    trigger('rotateAnimation', [
      state('collapse', style({transform: 'rotate(-180deg)'})),
      state('expand', style({transform: 'rotate(0deg)'})),
      transition('expand <=> collapse', animate('200ms ease-out')),
    ]),
  ],
})
export class CreateProjectComponent implements OnInit, OnDestroy, AfterViewChecked {
  // enums for html
  Workflow = Workflow;
  FilingPlanMode = FilingPlanMode;
  FlowType = FlowType;
  // http calls
  pending: boolean;

  selectedWorkflow: Workflow = Workflow.MANUAL;
  selectedFlowType: FlowType = FlowType.FIX;
  public stepIndex = 0;
  public stepCount = 6;

  projectForm: FormGroup;

  hasDropZoneOver = false;
  hasError = false;
  uploadFiles$: Observable<CollectUploadFile[]>;
  zippedFile$: Observable<CollectZippedUploadFile>;
  @ViewChild('fileSearch', {static: false}) fileSearch: any;
  tenantIdentifier: number;
  createdProject: Project;
  createdTransaction: Transaction;
  ontologies: Ontology[];

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

  legalStatusList = [
    {id: 'Public Archive', value: this.translationService.instant('LEGAL_STATUS.PUBLIC_ARCHIVE')},
    {id: 'Private Archive', value: this.translationService.instant('LEGAL_STATUS.PRIVATE_ARCHIVE')},
    {id: 'Public and Private Archive', value: this.translationService.instant('LEGAL_STATUS.PUBLIC_PRIVATE_ARCHIVE')},
  ];

  uploadZipCompleted = false;

  @ViewChild('confirmDeleteAddRuleDialog', {static: true}) confirmDeleteAddRuleDialog: TemplateRef<CreateProjectComponent>;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<CreateProjectComponent>,
    private dialogRefToClose: MatDialogRef<CreateProjectComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private projectsService: ProjectsService,
    private transactionsService: TransactionsService,
    private uploadService: CollectUploadService,
    private snackBar: MatSnackBar,
    private logger: Logger,
    private externalParameterService: ExternalParametersService,
    private cdr: ChangeDetectorRef,
    private translationService: TranslateService,
    private archiveCollectService: ArchiveCollectService,
    public dialog: MatDialog
  ) {
  }

  get linkParentIdControl() {
    return this.projectForm.controls.linkParentIdControl as FormControl;
  }

  get accessContractSelect() {
    return this.projectForm.controls.accessContractSelect;
  }

  ngOnInit(): void {
    this.tenantIdentifier = this.data.tenantIdentifier;
    this.initForm();
    this.accessContract();
    this.uploadFiles$ = this.uploadService.getUploadingFiles();
    this.zippedFile$ = this.uploadService.getZipFile();
    this.archiveCollectService.getOntologiesFromJson().subscribe((data: Ontology[]) => {
      this.ontologies = data.filter((ontology) => ontology.ApiField !== undefined);
      this.ontologies.sort((a: any, b: any) => {
        const shortNameA = a.Identifier;
        const shortNameB = b.Identifier;
        return shortNameA < shortNameB ? -1 : shortNameA > shortNameB ? 1 : 0;
      });
    });
  }

  ngOnDestroy(): void {
    this.uploadService.reinitializeZip();
  }

  ngAfterViewChecked(): void {
    this.cdr.detectChanges();
  }

  accessContract() {
    this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessContratId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessContratId && accessContratId.length > 0) {
        this.accessContractSelect.setValue(accessContratId);
      } else {
        this.snackBar.open(this.translationService.instant('COLLECT.NO_ACCESS_CONTRACT'), null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });
      }
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

  onDragOver(event: any) {
    event.preventDefault();
    this.hasDropZoneOver = true;
  }

  onDragLeave(event: any) {
    event.preventDefault();
    this.hasDropZoneOver = false;
  }

  async onDropped(event: any) {
    this.hasDropZoneOver = false;
    event.preventDefault();
    const items = event.dataTransfer.items;
    const exists = this.uploadService.directoryExistInZipFile(items, true);
    if (exists) {
      this.snackBar.open(this.translationService.instant('COLLECT.UPLOAD_FILE_ALREADY_IMPORTED'), null, {duration: 3000});
      return;
    }
    await this.uploadService.handleDragAndDropUpload(items);
  }

  async handleFile(event: any) {
    event.preventDefault();
    const items = event.target.files;
    const exists = this.uploadService.directoryExistInZipFile(items, false);
    if (exists) {
      this.snackBar.open(this.translationService.instant('COLLECT.UPLOAD_FILE_ALREADY_IMPORTED'), null, {duration: 3000});
      return;
    }
    await this.uploadService.handleUpload(items);
  }

  addFolder() {
    this.fileSearch.nativeElement.click();
  }

  removeFolder(file: CollectUploadFile) {
    this.uploadService.removeFolder(file);
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
    return (
      this.projectForm.controls.rulesParams.invalid
    );
  }

  /*** Step 5 : Téléchargements ***/
  close() {
    this.dialogRef.close(true);
    this.createdProject = null;
  }

  /*** All Steps ***/
  initForm() {
    this.projectForm = this.formBuilder.group({
      accessContractSelect: [null],
      selectedWorkflow: [null, Validators.required],
      selectedFlowType: [null],
      referentialCheckup: [false],

      archivalAgreement: [null, Validators.required],
      messageIdentifier: [null],
      archivalAgencyIdentifier: [null, Validators.required],
      transferringAgencyIdentifier: [null, Validators.required],
      originatingAgencyIdentifier: [null, Validators.required],
      submissionAgencyIdentifier: [null, Validators.required],
      // add archivalProfile ?
      archiveProfile: [null],
      acquisitionInformation: [null],
      legalStatus: [null],
      // for unitUp :
      linkParentIdControl: [{included: [], excluded: []}],
      // for unitUps :
      rulesParams: this.formBuilder.array([], Validators.required),
      comment: [null],
      status: [null],
    });
  }

  formToProject(): Project {
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
      status: ProjectStatus.OPEN,
    } as Project;
    if (this.selectedWorkflow === Workflow.MANUAL || this.selectedFlowType === FlowType.FIX) {
      project.unitUp = this.linkParentIdControl.value.included[0];
    } else {
      project.unitUps = this.convertRuleParamsToMetadata();
    }
    return project as Project;
  }

  convertRuleParamsToMetadata(): Array<MetadataUnitUp> {
    return this.rulesParams.controls.map((ruleParamControl: FormControl) => {
      const ruleParam = ruleParamControl.value;
      return {
        metadataKey: ruleParam.ontology.ApiField,
        metadataValue: ruleParam.metadataValue,
        unitUp: ruleParam.unitUp.included[0],
      }
    })
  }

  get rulesParams(): FormArray {
    return this.projectForm.controls.rulesParams as FormArray;
  }

  openCloseRuleParam(ruleParam: any) {
    ruleParam.opened = !ruleParam.opened;
  }

  addRuleParam() {
    for (const ruleParamForm of this.rulesParams.controls) {
      ruleParamForm.value.opened = false;
    }
    // rulesParams interface:
    const newRuleParamForm = this.formBuilder.group({
      opened: [true],
      ontology: ['', Validators.required],
      metadataValue: ['', Validators.required],
      unitUp: [{included: [], excluded: []}, oneIncludedNodeRequired()],
    });
    this.rulesParams.push(newRuleParamForm);
  }

  deleteRuleParam(index: number) {
    this.rulesParams.removeAt(index);
  }

  async validateAndCreateProject() {
    if (this.selectedWorkflow === Workflow.MANUAL) {
      this.createProjectAndTransactionAndUpload();
    } else {
      this.createProject();
    }
  }

  async createProject() {
    this.pending = true;
    const project: Project = this.formToProject();
    this.moveToNextStep();
    await this.projectsService.create(project).subscribe(
      _result => {
        this.pending = false;
        this.snackBar.open(this.translationService.instant('COLLECT.MODAL.PROJECT_CREATED'), null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });
      },
      _error => {
        this.pending = false;
        this.snackBar.open(this.translationService.instant('COLLECT.MODAL.PROJECT_CREATION_ERROR'), null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });
      },
    );
  }

  async createProjectAndTransactionAndUpload() {
    this.pending = true;
    const project: Project = this.formToProject();
    const transaction = {
      status: TransactionStatus.OPEN,
    } as Transaction;
    this.uploadZipCompleted = false;
    this.moveToNextStep();
    await this.projectsService.create(project).toPromise()
      .then((createProjectResponse) => {
        this.createdProject = createProjectResponse;
        transaction.projectId = this.createdProject.id;
        this.transactionsService.create(transaction)
          .toPromise()
          .then((createTransactionResponse) => {
            this.createdTransaction = createTransactionResponse;
            return this.uploadService.uploadZip(this.tenantIdentifier, this.createdTransaction.id);
          })
          .then((uploadOperation) => {
            uploadOperation.subscribe(
              () => {
              },
              (error: any) => {
                this.logger.error(error);
              },
              () => {
                this.uploadZipCompleted = true;
                this.pending = false;
                this.snackBar.open(this.translationService.instant('COLLECT.UPLOAD.TERMINATED'), null, {
                  panelClass: 'vitamui-snack-bar',
                  duration: 10000,
                });
              }
            );
          })
          .catch((error) => {
            this.logger.error(error);
          });
      })
  }

  asFormGroup(control: AbstractControl) {
    return control as FormGroup;
  }

  asFormControl(control: AbstractControl) {
    return control as FormControl;
  }

}
