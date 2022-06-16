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
import { AfterViewChecked, ChangeDetectorRef, Component, Inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { animate, state, style, transition, trigger } from '@angular/animations';
import { Observable, Subscription } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmDialogService, ExternalParameters, ExternalParametersService, Logger, Project, ProjectStatus } from 'ui-frontend-common';
import { FilingPlanMode } from 'vitamui-library';

import { ProjectsService } from '../projects.service';
import { CollectUploadService } from '../../shared/collect-upload/collect-upload.service';
import { CollectUploadFile, CollectZippedUploadFile } from '../../shared/collect-upload/collect-upload-file';

import * as moment from 'moment';

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
})
export class CreateProjectComponent implements OnInit, OnDestroy, AfterViewChecked {
  public stepIndex = 0;
  public stepCount = 5;
  projectForm: FormGroup;
  hasDropZoneOver = false;
  hasError = false;
  uploadFiles$: Observable<CollectUploadFile[]>;
  zippedFile$: Observable<CollectZippedUploadFile>;
  @ViewChild('fileSearch', { static: false }) fileSearch: any;
  FILLING_PLAN_MODE = FilingPlanMode;
  tenantIdentifier: number;
  projectId: string;
  createDialogSub: Subscription;
  updateDialogSub: Subscription;
  acquisitionInformations = [
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
  legalStatus = [
    this.translationService.instant('LEGAL_STATUS.PUBLIC_ARCHIVE'),
    this.translationService.instant('LEGAL_STATUS.PRIVATE_ARCHIVE'),
    this.translationService.instant('LEGAL_STATUS.PUBLIC_PRIVATE_ARCHIVE'),
  ];
  dateErrorMessage = '';
  closeModal = false;

  constructor(
    private formBuilder: FormBuilder,
    private dialogRef: MatDialogRef<CreateProjectComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private projectsService: ProjectsService,
    private uploadService: CollectUploadService,
    private snackBar: MatSnackBar,
    private logger: Logger,
    private externalParameterService: ExternalParametersService,
    private confirmDialogService: ConfirmDialogService,
    private cdr: ChangeDetectorRef,
    private translationService: TranslateService
  ) {}

  get linkParentIdControl() {
    return this.projectForm.controls['linkParentIdControl'] as FormControl;
  }

  get accessContractSelect() {
    return this.projectForm.controls['accessContractSelect'];
  }

  ngOnInit(): void {
    this.tenantIdentifier = this.data.tenantIdentifier;
    this.initForm();
    this.accessContract();
    this.uploadFiles$ = this.uploadService.getUploadingFiles();
    this.zippedFile$ = this.uploadService.getZipFile();
  }

  ngOnDestroy(): void {
    this.uploadService.reinitializeZip();
    this.createDialogSub?.unsubscribe();
    this.updateDialogSub?.unsubscribe();
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

  onCancel() {
    if (this.projectForm.dirty) {
      this.confirmDialogService.confirmBeforeClosing(this.dialogRef);
    } else {
      this.uploadService.reinitializeZip();
      this.dialogRef.close();
    }
  }

  move() {
    this.stepIndex = this.stepIndex + 1;
  }

  back() {
    this.stepIndex = this.stepIndex - 1;
  }

  /*** Step 1 : Upload Fichiers ***/
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
      this.snackBar.open(this.translationService.instant('COLLECT.UPLOAD_FILE_ALREADY_IMPORTED'), null, { duration: 3000 });
      return;
    }
    await this.uploadService.handleDragAndDropUpload(items);
  }

  async handleFile(event: any) {
    event.preventDefault();
    const items = event.target.files;
    const exists = this.uploadService.directoryExistInZipFile(items, false);
    if (exists) {
      this.snackBar.open(this.translationService.instant('COLLECT.UPLOAD_FILE_ALREADY_IMPORTED'), null, { duration: 3000 });
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

  async validateAndProcessUpload() {
    const project = {
      status: ProjectStatus.OPEN,
      unitUp: this.linkParentIdControl.value.included[0],
    } as Project;
    this.move();
    this.closeModal = false;
    await this.projectsService
      .create(project)
      .toPromise()
      .then((response) => {
        this.projectId = response.id;
        return this.uploadService.uploadZip(this.tenantIdentifier, this.projectId);
      })
      .then((uploadOperation) => {
        uploadOperation.subscribe(
          () => {},
          (error: any) => {
            this.logger.error(error);
          },
          () => {
            this.closeModal = true;
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
  }

  /*** Step 3 : Description du versement ***/
  validateThirdStep() {
    const startDate = this.projectForm.controls['startDate'].value;
    const endDate = this.projectForm.controls['endDate'].value;

    if (startDate !== null && endDate !== null && moment(startDate).isAfter(endDate)) {
      this.dateErrorMessage = this.translationService.instant('COLLECT.PROJECT_DESCRIPTION_DATE_ERROR');
      return true;
    }
    this.dateErrorMessage = '';

    return (
      this.projectForm.controls['originatingAgencyIdentifier'].invalid || this.projectForm.controls['submissionAgencyIdentifier'].invalid
    );
  }

  /*** Step 4 : Contexte de versement ***/
  validateFourStep() {
    return (
      this.projectForm.controls['archivalAgencyIdentifier'].invalid ||
      this.projectForm.controls['transferringAgencyIdentifier'].invalid ||
      this.projectForm.controls['archivalAgreement'].invalid
    );
  }

  updateProject() {
    const project = { ...this.projectForm.value, id: this.projectId };
    this.updateDialogSub = this.projectsService.updateProject(project).subscribe();
    this.move();
  }

  /*** Step 5 : Téléchargements ***/
  close() {
    this.dialogRef.close(true);
    this.projectId = null;
  }

  /*** All Steps ***/
  private initForm() {
    this.projectForm = this.formBuilder.group({
      messageIdentifier: [null],
      comment: [null],
      startDate: [null],
      endDate: [null],
      originatingAgencyIdentifier: [null, Validators.required],
      submissionAgencyIdentifier: [null, Validators.required],
      referentialCheckup: [true],
      archivalAgencyIdentifier: [null, Validators.required],
      transferringAgencyIdentifier: [null, Validators.required],
      archivalAgreement: [null, Validators.required],
      archivalProfile: [null],
      acquisitionInformation: [null],
      status: [null],
      linkParentIdControl: [{ included: [], excluded: [] }],
      accessContractSelect: [null],
    });
  }
}
