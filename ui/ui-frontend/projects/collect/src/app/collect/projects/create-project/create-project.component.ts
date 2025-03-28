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
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { TranslateService } from '@ngx-translate/core';
import { finalize, Observable, throwError } from 'rxjs';
import { catchError, last, map, switchMap, tap } from 'rxjs/operators';
import {
  FilingPlanMode,
  FlowType,
  IOntology,
  Logger,
  MetadataUnitUp,
  MiscValidators,
  oneIncludedNodeRequired,
  OntologyService,
  Option,
  Project,
  ProjectStatus,
  Transaction,
  TransactionStatus,
  Workflow,
  ZipFile,
  ZipFileStatus,
} from 'vitamui-library';
import { ProjectsService } from '../projects.service';
import { TransactionsService } from '../transactions.service';
import { ArchiveCollectService } from '../../archive-search-collect/archive-collect.service';
import { HttpEventType } from '@angular/common/http';

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
  ontologies: Option[];
  filesToUpload: File[] = [];
  zipFileStatus$: Observable<ZipFileStatus>;

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
    private transactionsService: TransactionsService,
    private archiveCollectService: ArchiveCollectService,
    private snackBar: MatSnackBar,
    private logger: Logger,
    private cdr: ChangeDetectorRef,
    private translationService: TranslateService,
    public dialog: MatDialog,
    private ontologyService: OntologyService,
  ) {}

  get linkParentIdControl() {
    return this.projectForm.controls.linkParentIdControl as FormControl;
  }

  ngOnInit(): void {
    this.initForm();
    this.ontologyService.getInternalOntologyFieldsList().subscribe((data: IOntology[]) => {
      this.ontologies = data
        .sort((a: any, b: any) => {
          const shortNameA = a.Identifier;
          const shortNameB = b.Identifier;
          return shortNameA < shortNameB ? -1 : shortNameA > shortNameB ? 1 : 0;
        })
        .map((ontology: IOntology) => ({ key: ontology, label: ontology.Identifier }));
    });
  }

  ngAfterViewChecked(): void {
    this.cdr.detectChanges();
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

  setFilesToUpload(files: File[]) {
    this.filesToUpload = files;
  }

  uploadJsltFile(files: File[]) {
    const jsltFile = files?.length ? files[0] : undefined;
    if (jsltFile) {
      this.readFileContent(jsltFile)
        .then((content: string) => {
          this.projectForm.get('transformationRules').setValue(content);
        })
        .catch((error: any) => {
          this.logger.error('Error reading JSLT file:', error);
          this.isLoading = false;
        });
    }
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

      archivalAgreement: [null, MiscValidators.requiredNotBlank],
      messageIdentifier: [null, MiscValidators.requiredNotBlank],
      archivalAgencyIdentifier: [null, MiscValidators.requiredNotBlank],
      transferringAgencyIdentifier: [null, MiscValidators.requiredNotBlank],
      originatingAgencyIdentifier: [null, MiscValidators.requiredNotBlank],
      submissionAgencyIdentifier: [null, MiscValidators.requiredNotBlank],
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
    });
  }

  private formToProject(): Project {
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
      transformationRules: this.projectForm.value.transformationRules,
      automaticIngest: this.selectedWorkflow === Workflow.MANUAL ? null : this.projectForm.value.automaticIngest === true,
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
      };
    });
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
      unitUp: [{ included: [], excluded: [] }, oneIncludedNodeRequired()],
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
          this.snackBar.open(this.translationService.instant('COLLECT.MODAL.PROJECT_CREATED'), null, {
            duration: 10000,
          });
        },
        error: (_error) => {
          this.snackBar.open(this.translationService.instant('COLLECT.MODAL.PROJECT_CREATION_ERROR'), null, {
            duration: 10000,
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
          zipFile.setZipName(transactionId + '.zip');
        }),
        switchMap(() => zipFile.addFiles(this.filesToUpload).generateZip()),
        switchMap((content) => this.archiveCollectService.uploadZip(content, transactionId)),
        tap((httpEvent) => zipFile.updateUploadingZipFileStatus(httpEvent)),
        last((httpEvent) => httpEvent.type === HttpEventType.Response),
        finalize(() => {
          this.isLoading = false;
          this.snackBar.open(this.translationService.instant('COLLECT.UPLOAD.TERMINATED'), null, {
            duration: 10000,
          });
        }),
        catchError((error) => {
          this.logger.error(error);
          return throwError(error);
        }),
      )
      .subscribe();
  }

  asFormGroup(control: AbstractControl) {
    return control as FormGroup;
  }

  asFormControl(control: AbstractControl) {
    return control as FormControl;
  }

  private readFileContent(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = (event) => resolve(event.target?.result as string);
      reader.onerror = (error) => reject(error);
      reader.readAsText(file);
    });
  }
}
