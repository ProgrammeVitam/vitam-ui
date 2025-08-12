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
import { Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild, AfterViewInit, OnDestroy, Renderer2 } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatLegacyDialog as MatDialog, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { MatLegacyTabGroup as MatTabGroup } from '@angular/material/legacy-tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, combineLatest, Observable, scan } from 'rxjs';
import { distinctUntilChanged, map, mergeMap } from 'rxjs/operators';
import {
  DEFAULT_PAGE_SIZE,
  Direction,
  getProjectIcon,
  getProjectWorkflow,
  LegalStatus,
  PageRequest,
  PaginatedResponse,
  Project,
  Transaction,
  TransactionStatus,
  Workflow,
} from 'vitamui-library';
import { ProjectsApiService } from '../../core/api/project-api.service';
import { ProjectsService } from '../projects.service';
import { MatDialogConfig } from '@angular/material/dialog';

@Component({
  selector: 'app-project-preview',
  templateUrl: './project-preview.component.html',
  styleUrls: ['./project-preview.component.scss'],
})
export class ProjectPreviewComponent implements OnInit, AfterViewInit, OnDestroy {
  @Output()
  backToNormalLateralPanel: EventEmitter<any> = new EventEmitter();
  @Output()
  previewClose: EventEmitter<any> = new EventEmitter();
  @Output()
  showExtendedLateralPanel: EventEmitter<any> = new EventEmitter();

  @ViewChild(MatTabGroup) tabGroup: MatTabGroup;

  form: FormGroup;

  project: Project;

  acquisitionInformationsList: string[];
  legalStatusList: LegalStatus[] = [];

  protected readonly Workflow = Workflow;
  getProjectIcon = getProjectIcon;
  getProjectWorkflow = getProjectWorkflow;

  @ViewChild('confirmEditProject', { static: true }) confirmEditProject: TemplateRef<ProjectPreviewComponent>;
  @ViewChild('cancelDialog') cancelDialog: TemplateRef<ProjectPreviewComponent>;

  @Input()
  get projectId(): string {
    return this.projectId$.getValue();
  }

  set projectId(value: string) {
    this.projectId$.next(value);
    this.selectedTabIndex = 0;
  }

  private projectId$ = new BehaviorSubject<string>(null);
  private tenantIdentifier: string;
  private clickOutSideListener!: () => void;
  private readonly dialogConfig: MatDialogConfig = { panelClass: 'vitamui-dialog' };

  editMode = false;
  isPanelextended = false;
  selectedTabIndex = 0;
  dialogRefToClose: MatDialogRef<ProjectPreviewComponent>;
  selectedValue = 'YES';

  transactions$: BehaviorSubject<PaginatedResponse<Transaction>> = new BehaviorSubject<PaginatedResponse<Transaction>>(null);
  openedTransactions$ = this.transactions$.pipe(map((ts) => ts?.values?.filter((t) => t?.status === 'OPEN')));

  constructor(
    private formBuilder: FormBuilder,
    private projectService: ProjectsService,
    private projectApiService: ProjectsApiService,
    private route: ActivatedRoute,
    private router: Router,
    public dialog: MatDialog,
    private translationService: TranslateService,
    private snackBar: MatSnackBar,
    private renderer: Renderer2,
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.projectId$
      .pipe(
        scan((acc, newValue) => (this.isModified() ? acc : newValue), this.projectId$.getValue()), // Keep the old value if we have edited data not yet saved; **isModified()** is true.
        distinctUntilChanged(), // Avoid calling multiple times with the same value.
        mergeMap((projectId) => this.projectService.getProjectById(projectId)),
      )
      .subscribe((project) => {
        this.project = project;
        this.showNormalPanel();
        this.initForm();
      });

    this.legalStatusList = this.projectService.getLegalStatusList();
    this.acquisitionInformationsList = this.projectService.getAcquisitionInformationsList();

    this.configForm();
  }
  ngAfterViewInit() {
    // Listen for clicks on the #projectList div (outside the panel)
    const projectList = document.getElementById('projectList');
    if (projectList) {
      this.clickOutSideListener = this.renderer.listen(projectList, 'click', () => {
        if (this.isModified() && this.dialogRefToClose?.getState() !== 0) {
          this.openCancelDialog();
        }
      });
    }
  }

  searchArchiveUnitsByProject() {
    this.router.navigate(['collect/tenant/' + this.tenantIdentifier + '/units', this.project.id]);
  }

  emitClose() {
    this.isPanelextended = false;
    this.editMode = false;
    this.previewClose.emit();
    this.backToNormalLateralPanel.emit();
    this.selectedTabIndex = 0;
  }

  showNormalPanel() {
    this.isPanelextended = false;
    this.backToNormalLateralPanel.emit();
    this.editMode = false;
  }

  showExtendedPanel() {
    this.isPanelextended = true;
    this.showExtendedLateralPanel.emit();
  }

  configForm() {
    this.form = this.formBuilder.group({
      messageIdentifier: [null, [Validators.required]],
      id: [null],
      comment: [],
      originatingAgencyIdentifier: [null, [Validators.required]],
      submissionAgencyIdentifier: [],
      archivalAgencyIdentifier: [null, Validators.required],
      transferringAgencyIdentifier: [null, Validators.required],
      archivalAgreement: [null, Validators.required],
      archiveProfile: [null],
      acquisitionInformation: [null],
      legalStatus: [null],
    });
  }

  isModified(): boolean {
    // use pristine to check if the form is unchanged.
    return this.editMode && !this.form.pristine;
  }

  showEditProject() {
    this.form.markAsPristine();
    this.editMode = true;
    this.showExtendedPanel();
    this.initForm();
  }

  initForm() {
    if (this.form) {
      this.form.get('messageIdentifier').setValue(this.project.messageIdentifier);
      this.form.get('comment').setValue(this.project.comment);
      this.form.get('originatingAgencyIdentifier').setValue(this.project.originatingAgencyIdentifier);
      this.form.get('submissionAgencyIdentifier').setValue(this.project.submissionAgencyIdentifier);
      this.form.get('archivalAgencyIdentifier').setValue(this.project.archivalAgencyIdentifier);
      this.form.get('transferringAgencyIdentifier').setValue(this.project.transferringAgencyIdentifier);
      this.form.get('archivalAgreement').setValue(this.project.archivalAgreement);
      this.form.get('archiveProfile').setValue(this.project.archiveProfile);
      this.form.get('acquisitionInformation').setValue(this.project.acquisitionInformation);
      this.form.get('legalStatus').setValue(this.project.legalStatus);
    }
  }

  launchUpdate() {
    const dialogToOpen = this.confirmEditProject;
    this.selectedValue = 'YES';
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'id', Direction.ASCENDANT);
    this.projectApiService.getTransactionsByProjectId(pageRequest, this.projectId$.getValue()).subscribe((transactions) => {
      this.transactions$.next(transactions);
    });
    this.dialogRefToClose = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
  }

  mapProjectInternalFields(projectToUpdate: Project) {
    projectToUpdate.id = this.project.id;
    projectToUpdate.createdOn = this.project.createdOn;
    projectToUpdate.unitUp = this.project.unitUp;
    projectToUpdate.status = this.project.status;
    projectToUpdate.unitUps = this.project.unitUps;
  }

  fillTransactionFromProject(transaction: Transaction) {
    transaction.archivalAgreement = this.project.archivalAgreement;
    transaction.messageIdentifier = this.project.messageIdentifier;
    transaction.archivalAgencyIdentifier = this.project.archivalAgencyIdentifier;
    transaction.transferringAgencyIdentifier = this.project.transferringAgencyIdentifier;
    transaction.originatingAgencyIdentifier = this.project.originatingAgencyIdentifier;
    transaction.submissionAgencyIdentifier = this.project.submissionAgencyIdentifier;
    transaction.archiveProfile = this.project.archiveProfile;
    transaction.legalStatus = this.project.legalStatus;
    transaction.comment = this.project.comment;
    transaction.acquisitionInformation = this.project.acquisitionInformation;
  }

  onConfirm() {
    const projectToUpdate = {
      ...this.form.value,
      name: this.form.value.messageIdentifier,
      automaticIngest: this.project?.automaticIngest,
    };
    this.mapProjectInternalFields(projectToUpdate);

    const updateProjectOperation$ = this.projectService.updateProject(projectToUpdate);
    const previousProject = this.project;
    this.project = null;
    if (this.selectedValue !== 'NO') {
      updateProjectOperation$
        .pipe(
          mergeMap((project): Observable<PaginatedResponse<Transaction>> => {
            this.dialogRefToClose.close(true);
            this.project = project;
            this.projectService.nextUpdatedProject(project);

            this.showNormalPanel();
            return this.transactions$;
          }),
          map((paginated) => paginated.values),
          mergeMap((transactions: Transaction[]) => {
            const updateTransactionOperation$: Observable<Transaction>[] = [];
            const transactionsKO: Transaction[] = [];
            transactions.forEach((transaction) => {
              if (transaction.status === TransactionStatus.OPEN) {
                this.fillTransactionFromProject(transaction);
                updateTransactionOperation$.push(this.projectApiService.updateTransaction(transaction));
              } else if (transaction.status === TransactionStatus.KO) {
                transactionsKO.push(transaction);
              }
            });
            return combineLatest(updateTransactionOperation$).pipe(map(() => transactionsKO));
          }),
        )
        .subscribe(
          (transactionsKO: Transaction[]) => {
            this.showNormalPanel();
            let transactionMessage = this.translationService.instant('COLLECT.UPDATE_PROJECT.TERMINATED');
            if (transactionsKO.length > 0) {
              transactionMessage += ' ' + this.translationService.instant('COLLECT.UPDATE_PROJECT.TRANSACTIONS_KO');
            }
            this.snackBar.open(transactionMessage, null, {
              panelClass: 'vitamui-snack-bar',
              duration: 10000,
            });
          },
          () => {
            this.project = previousProject;
            this.showNormalPanel();
          },
        );
    } else {
      updateProjectOperation$.subscribe(
        (project) => {
          this.snackBar.open(this.translationService.instant('COLLECT.UPDATE_PROJECT.TERMINATED'), null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          });
          this.dialogRefToClose?.close(true);
          this.showNormalPanel();
          this.project = project;
          this.projectService.nextUpdatedProject(project);
        },
        () => {
          this.project = previousProject;
          this.showNormalPanel();
        },
      );
    }
  }

  onCancel() {
    this.showNormalPanel();
    this.dialogRefToClose?.close(true);
  }

  openCancelDialog() {
    if (!this.isModified()) {
      this.onCancel();
      return;
    }
    this.dialog
      .open(this.cancelDialog, this.dialogConfig)
      .afterClosed()
      .subscribe((result) => {
        if (result) {
          this.selectedValue = 'NO';
          this.onConfirm();
        } else {
          this.onCancel();
        }
      });
  }

  ngOnDestroy() {
    if (this.clickOutSideListener) {
      this.clickOutSideListener();
    }
  }
}
