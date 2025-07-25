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
import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  QueryList,
  Renderer2,
  TemplateRef,
  ViewChild,
  ViewChildren,
} from '@angular/core';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog, MatDialogConfig, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTab, MatTabGroup, MatTabHeader, MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, combineLatest, firstValueFrom, Observable, scan } from 'rxjs';
import { distinctUntilChanged, map, mergeMap } from 'rxjs/operators';

import { ProjectsApiService } from '../../core/api/project-api.service';
import { ProjectsService } from '../projects.service';
import { MatMenuModule } from '@angular/material/menu';
import { AsyncPipe, NgClass, NgTemplateOutlet } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import {
  ApplicationId,
  DEFAULT_PAGE_SIZE,
  Direction,
  download,
  FilingPlanMode,
  FilingPlanService,
  getProjectIcon,
  getProjectWorkflow,
  ItemNode,
  MiscValidators,
  Option,
  PageRequest,
  PaginatedResponse,
  Project,
  ProjectAttachments,
  SchemaElement,
  SchemaService,
  SecurityService,
  Transaction,
  TransactionStatus,
  Unit,
  VitamUICommonModule,
  VitamUILibraryModule,
  VitamUISnackBarService,
  Workflow,
} from 'vitamui-library';

@Component({
  selector: 'app-project-preview',
  templateUrl: './project-preview.component.html',
  styleUrls: ['./project-preview.component.scss'],
  imports: [
    AsyncPipe,
    FormsModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatDialogModule,
    MatMenuModule,
    MatProgressSpinner,
    MatTabsModule,
    NgClass,
    NgTemplateOutlet,
    ReactiveFormsModule,
    TranslateModule,
    VitamUICommonModule,
    VitamUILibraryModule,
  ],
})
export class ProjectPreviewComponent implements OnInit, AfterViewInit, OnDestroy {
  @Output()
  backToNormalLateralPanel: EventEmitter<any> = new EventEmitter();
  @Output()
  previewClose: EventEmitter<any> = new EventEmitter();
  @Output()
  showExtendedLateralPanel: EventEmitter<any> = new EventEmitter();

  tenantId: number;
  checkEditRole = new Observable<boolean>();
  checkEditAttachmentRole = new Observable<boolean>();

  protected readonly FilingPlanMode = FilingPlanMode;

  form: FormGroup;

  project: Project;
  jsltFilename = 'TransformationRules.jslt';

  acquisitionInformationsList: string[];
  legalStatusList: Option[] = [];
  schemaOptions: ItemNode<SchemaElement>[];
  units: Unit[];

  protected readonly Workflow = Workflow;
  getProjectIcon = getProjectIcon;
  getProjectWorkflow = getProjectWorkflow;

  @ViewChild('tabs', { static: false }) tabGroup: MatTabGroup;
  @ViewChildren(MatTab) tabs: QueryList<MatTab>;
  @ViewChild('confirmEditProject', { static: true }) confirmEditProject: TemplateRef<ProjectPreviewComponent>;
  @ViewChild('confirmEditAttachments', { static: true }) confirmEditAttachments: TemplateRef<ProjectPreviewComponent>;
  @ViewChild('cancelDialog') cancelDialog: TemplateRef<ProjectPreviewComponent>;
  @ViewChild('cancelAttachmentsDialog') cancelAttachmentsDialog: TemplateRef<ProjectPreviewComponent>;

  @Input()
  get projectId(): string {
    return this.projectId$.getValue();
  }

  set projectId(value: string) {
    this.projectId$.next(value);
    if (this.tabGroup) this.tabGroup.selectedIndex = 0;
  }

  private projectId$ = new BehaviorSubject<string>(null);
  private tenantIdentifier: string;
  private clickOutSideListener!: () => void;
  private readonly dialogConfig: MatDialogConfig = { panelClass: 'vitamui-dialog' };

  editMode = false;
  isPanelExtended = false;
  dialogRefToClose: MatDialogRef<ProjectPreviewComponent>;
  selectedValue = 'YES';

  transactions$: BehaviorSubject<PaginatedResponse<Transaction>> = new BehaviorSubject<PaginatedResponse<Transaction>>(null);
  openedTransactions$ = this.transactions$.pipe(map((ts) => ts?.values?.filter((t) => t?.status === 'OPEN')));

  constructor(
    private formBuilder: FormBuilder,
    private projectService: ProjectsService,
    private projectApiService: ProjectsApiService,
    private securityService: SecurityService,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private translationService: TranslateService,
    private snackBarService: VitamUISnackBarService,
    private renderer: Renderer2,
    private schemaService: SchemaService,
    filingPlanService: FilingPlanService,
  ) {
    filingPlanService.loadFilingPlan().subscribe((units) => (this.units = units));
    this.route.params.subscribe((params) => {
      if (params.tenantIdentifier) {
        // eslint-disable-next-line radix
        this.tenantId = parseInt(params.tenantIdentifier);
      }
    });
  }

  ngOnInit(): void {
    this.checkEditRole = this.securityService.hasRole$(ApplicationId.COLLECT_APP, 'ROLE_UPDATE_PROJECTS', this.tenantId);
    this.checkEditAttachmentRole = this.securityService.hasRole$(ApplicationId.COLLECT_APP, 'ROLE_UPDATE_TRANSACTIONS', this.tenantId);
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.configForm();
    this.loadProject();

    this.legalStatusList = this.projectService.getLegalStatusList();
    this.acquisitionInformationsList = this.projectService.getAcquisitionInformationsList();
  }

  ngAfterViewInit() {
    this.tabGroup._handleClick = this.interceptTabChange.bind(this);

    // Listen for clicks on the #projectList div (outside the panel)
    const projectList = document.getElementById('projectList');
    if (projectList) {
      this.clickOutSideListener = this.renderer.listen(projectList, 'click', () => {
        this.shouldCancelNavigation();
      });
    }
  }

  private async interceptTabChange(_tab: MatTab, _tabHeader: MatTabHeader, idx: number) {
    if (!(this.isModified() && (await this.shouldCancelNavigation()))) {
      this.editMode = false;
      this.tabGroup.selectedIndex = idx;
    }
  }

  private async shouldCancelNavigation(): Promise<boolean> {
    if (this.isModified() && this.dialogRefToClose?.getState() !== 0) {
      const currentTab = this.getCurrentTab();

      if (['description', 'context'].includes(currentTab)) {
        return await this.openCancelDialog(); // TODO
      } else if (currentTab === 'attachment') {
        return await this.openCancelAttachmentsDialog();
      }
    }
    return false;
  }

  loadProject() {
    this.projectId$
      .pipe(
        scan((acc, newValue) => (this.isModified() ? acc : newValue), this.projectId$.getValue()), // Keep the old value if we have edited data not yet saved; **isModified()** is true.
        distinctUntilChanged(), // Avoid calling multiple times with the same value.
        mergeMap((projectId) => this.projectService.getProjectById(projectId)),
      )
      .subscribe((project) => {
        this.project = project;
        this.showNormalPanel();
        this.configForm();
        this.initForm();
        if (!project.unitUp && project.unitUps) {
          this.initRuleParams(project);
        }
      });
  }

  private initRuleParams(project: Project) {
    const keys = project.unitUps.map((units) => units.metadataKey);
    this.schemaService.getDescriptiveSchemaTree().subscribe((schema) => {
      this.schemaOptions = schema;
      const res = this.schemaService.getMetadataKeysByKeys(keys, schema);
      project.unitUps.forEach((metadataUnitUp) => {
        const item = res.find((sc) => sc.item.ApiField === metadataUnitUp.metadataKey).item;
        const ontologyListControl = this.formBuilder.control<SchemaElement>(item, Validators.required);
        const metadataValueControl = this.formBuilder.control(metadataUnitUp.metadataValue, Validators.required);
        ontologyListControl.valueChanges.subscribe(() => {
          metadataValueControl.setValidators(Validators.required); // To override Validators that may be added by datepicker if changing from datepicker to input
          metadataValueControl.markAsTouched(); // To make sure error messages appear after changing the ontology
        });

        this.unitUps.push(
          this.formBuilder.group({
            ontologyList: ontologyListControl,
            metadataValue: metadataValueControl,
            unitUp: this.project.connectedToArchivingSystem
              ? {
                  included: metadataUnitUp.unitUp ? [metadataUnitUp.unitUp] : [],
                  excluded: [],
                }
              : this.formBuilder.control(metadataUnitUp.unitUp),
          }),
        );
      });
    });
  }

  get unitUps(): FormArray<FormGroup> {
    return this.form.controls.unitUps as FormArray<FormGroup>;
  }

  getName(item: SchemaElement): string {
    const path = item.Path.split('.').slice(0, -1);
    const parent = path.reduce((acc, p) => acc.children.find((o) => o.item.FieldName === p), {
      children: this.schemaOptions,
    } as ItemNode<SchemaElement>);
    return `${item.ShortName}${parent?.item ? ` (${parent.item.ShortName})` : ''}`;
  }

  addRuleParam() {
    for (const ruleParamForm of this.unitUps.controls) {
      ruleParamForm.value.opened = false;
    }

    const ontologyListControl = this.formBuilder.control<SchemaElement>(undefined, Validators.required);
    const metadataValueGroup = this.formBuilder.group({}, { validators: Validators.required });

    ontologyListControl.valueChanges.subscribe((schemaElement) => {
      metadataValueGroup.addControl(schemaElement?.Path, this.formBuilder.control(undefined));
    });

    // rulesParams interface:
    const newRuleParamForm = this.formBuilder.group({
      opened: [true],
      ontologyList: ontologyListControl,
      metadataValue: undefined,
      unitUp: [this.project.connectedToArchivingSystem ? { included: [], excluded: [] } : ''],
    });

    this.unitUps.push(newRuleParamForm);
  }

  deleteRuleParam(index: number) {
    this.unitUps.removeAt(index);
    this.form.markAsDirty(); // We should be able to submit the form if we only remove a rule
  }

  searchArchiveUnitsByProject() {
    this.router.navigate(['collect/tenant/' + this.tenantIdentifier + '/units', this.project.id]);
  }

  emitClose() {
    this.isPanelExtended = false;
    this.editMode = false;
    this.previewClose.emit();
    this.backToNormalLateralPanel.emit();
    this.tabGroup.selectedIndex = 0;
    this.projectService.selectedProjectId$.next(null);
  }

  showNormalPanel() {
    this.isPanelExtended = false;
    this.backToNormalLateralPanel.emit();
    this.editMode = false;
  }

  showExtendedPanel() {
    this.isPanelExtended = true;
    this.showExtendedLateralPanel.emit();
  }

  getSchemaElementDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

  configForm() {
    this.form = this.formBuilder.group({
      messageIdentifier: [null, [MiscValidators.requiredNotBlank]],
      id: [null],
      comment: [],
      originatingAgencyIdentifier: [null, [MiscValidators.requiredNotBlank]],
      submissionAgencyIdentifier: [null, [MiscValidators.requiredNotBlank]],
      archivalAgencyIdentifier: [null, MiscValidators.requiredNotBlank],
      transferringAgencyIdentifier: [null, MiscValidators.requiredNotBlank],
      archivalAgreement: [null, MiscValidators.requiredNotBlank],
      archiveProfile: [null],
      acquisitionInformation: [null],
      legalStatus: [null],
      unitUp: [null],
      unitUps: this.formBuilder.array([], this.project?.unitUps?.length ? Validators.required : null),
    });
  }

  isModified(): boolean {
    // use pristine to check if the form is unchanged.
    return this.editMode && !this.form.pristine;
  }

  showEdit(tab: MatTab) {
    const tabIndex = this.tabs.toArray().indexOf(tab);
    if (tabIndex !== -1) {
      this.form.markAsPristine();
      this.editMode = true;
      this.tabGroup.selectedIndex = tabIndex;
      this.showExtendedPanel();
      this.initForm();
    }
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
      this.form
        .get('unitUp')
        .setValue(
          this.project.connectedToArchivingSystem
            ? { included: this.project.unitUp ? [this.project.unitUp] : [], excluded: [] }
            : this.project.unitUp,
        );
    }
  }

  update() {
    const currentTab = this.getCurrentTab();

    if (['description', 'context'].includes(currentTab)) {
      this.launchUpdate();
    } else if (currentTab === 'attachment') {
      this.launchAttachmentsUpdate();
    }
  }

  async cancel() {
    const currentTab = this.getCurrentTab();

    if (['description', 'context'].includes(currentTab)) {
      await this.openCancelDialog();
    } else if (currentTab === 'attachment') {
      await this.openCancelAttachmentsDialog();
    }
  }

  private getCurrentTab() {
    return ['description', 'context', 'attachment'][this.tabGroup.selectedIndex];
  }

  private launchUpdate = () => {
    const dialogToOpen = this.confirmEditProject;
    this.selectedValue = 'YES';
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'id', Direction.ASCENDANT);
    this.projectApiService.getTransactionsByProjectId(pageRequest, this.projectId$.getValue()).subscribe((transactions) => {
      this.transactions$.next(transactions);
    });
    this.dialogRefToClose = this.dialog.open(dialogToOpen);
  };

  private launchAttachmentsUpdate = () => {
    const dialogToOpen = this.confirmEditAttachments;
    this.dialogRefToClose = this.dialog.open(dialogToOpen);
  };

  mapProjectInternalFields(projectToUpdate: Project) {
    projectToUpdate.id = this.project.id;
    projectToUpdate.createdOn = this.project.createdOn;
    projectToUpdate.status = this.project.status;
    delete projectToUpdate.unitUp;
    delete projectToUpdate.unitUps;
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
    // transaction.unitUp = this.project.unitUp;
    // transaction.unitUps = this.project.unitUps;
  }

  downloadJSLT() {
    const blob = new Blob([this.project.transformationRules], { type: 'octet/stream' });
    download(blob, this.jsltFilename);
  }

  onConfirm() {
    const projectToUpdate = {
      ...this.form.value,
      name: this.form.value.messageIdentifier,
      automaticIngest: this.project?.automaticIngest,
      archivingSystemId: this.project.archivingSystemId,
      archivingSystemTenant: this.project.archivingSystemTenant,
      connectedToArchivingSystem: this.project.connectedToArchivingSystem,
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
            this.snackBarService.open({
              message: transactionMessage,
              translate: false,
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
          this.snackBarService.open({
            message: 'COLLECT.UPDATE_PROJECT.TERMINATED',
            duration: 10000,
          });
          this.dialogRefToClose?.close(true);
          this.showNormalPanel();
          if (this.projectId === project.id) {
            this.project = project;
          } else {
            this.projectId$.next(this.projectId);
          }
          this.projectService.nextUpdatedProject(project);
        },
        () => {
          this.projectId$.next(this.projectId);
          if (this.projectId === previousProject.id) {
            this.project = previousProject;
          } else {
            this.projectId$.next(this.projectId);
          }
          this.showNormalPanel();
        },
      );
    }
  }

  onConfirmAttachments() {
    const projectToUpdate: ProjectAttachments = {
      id: this.project.id,
      unitUp: this.form.value.unitUp
        ? (this.form.value.unitUp.included ? this.form.value.unitUp.included[0] : this.form.value.unitUp) || ''
        : '',
      unitUps: this.form.value.unitUps?.map(
        (ruleParam: {
          ontologyList: { ApiField: string };
          metadataValue: string;
          unitUp: {
            included: string[];
          };
        }) => {
          return {
            metadataKey: ruleParam.ontologyList.ApiField,
            metadataValue: ruleParam.metadataValue,
            unitUp: this.project.connectedToArchivingSystem ? ruleParam.unitUp.included[0] : ruleParam.unitUp,
          };
        },
      ),
    };
    const previousProject = this.project;
    this.project = null;
    const updateProjectAttachmentsOperation$ = this.projectService.updateProjectAttachments(projectToUpdate);
    updateProjectAttachmentsOperation$.subscribe({
      next: (project) => {
        this.snackBarService.open({
          message: 'COLLECT.UPDATE_PROJECT_ATTACHMENTS.TERMINATED',
          duration: 10000,
        });
        this.dialogRefToClose?.close(true);
        this.showNormalPanel();
        if (this.projectId === project.id) {
          this.project = project;
        } else {
          this.projectId$.next(this.projectId);
        }
        this.projectService.nextUpdatedProject(project);
      },
      error: () => {
        this.dialogRefToClose?.close(true);
        this.showNormalPanel();
        this.projectId$.next(this.projectId);
        if (this.projectId === previousProject.id) {
          this.project = previousProject;
        } else {
          this.projectId$.next(this.projectId);
        }
      },
    });
  }

  private async onCancel() {
    this.showNormalPanel();
    if (this.projectId !== this.project.id) {
      this.projectId$.next(this.projectId);
    }
    this.dialogRefToClose?.close(true);
  }

  openCancelDialog = async (): Promise<boolean> => {
    if (!this.isModified()) {
      await this.onCancel();
      return;
    }
    const result = await firstValueFrom(this.dialog.open(this.cancelDialog, this.dialogConfig).afterClosed());
    if (result) {
      this.selectedValue = 'NO';
      this.onConfirm();
    } else {
      await this.onCancel();
    }
  };

  openCancelAttachmentsDialog = async (): Promise<boolean> => {
    if (!this.isModified()) {
      await this.onCancel();
      return false;
    }
    const result = await firstValueFrom(this.dialog.open(this.cancelAttachmentsDialog, this.dialogConfig).afterClosed());
    if (result) {
      this.onConfirmAttachments();
    }
    return !result;
  };

  ngOnDestroy() {
    if (this.clickOutSideListener) {
      this.clickOutSideListener();
    }
  }
}
