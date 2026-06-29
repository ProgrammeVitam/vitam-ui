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
  computed,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  QueryList,
  Renderer2,
  signal,
  TemplateRef,
  ViewChild,
  ViewChildren,
  inject,
} from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialog, MatDialogConfig, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTab, MatTabGroup, MatTabHeader, MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, combineLatest, firstValueFrom, lastValueFrom, Observable, of, scan } from 'rxjs';
import { catchError, distinctUntilChanged, filter, map, mergeMap, share, shareReplay, startWith } from 'rxjs/operators';

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
  ExternalReferentialService,
  fetchTitle,
  FilingPlanMode,
  FilingPlanService,
  getProjectIcon,
  getProjectWorkflow,
  ItemNode,
  MiscValidators,
  oneIncludedNodeRequired,
  Option,
  PageRequest,
  PaginatedResponse,
  Project,
  readFileContent,
  SchemaElement,
  SchemaService,
  SecurityService,
  TENANT_SEPARATOR,
  Transaction,
  TransactionStatus,
  Unit,
  VitamUICommonModule,
  VitamUILibraryModule,
  SnackBarService,
  TenantSelectionService,
  Workflow,
} from 'vitamui-library';
import { AttachmentMode, LOCAL_ARCHIVING_SYSTEM_ID } from '../create-project/create-project.component';

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
  private formBuilder = inject(FormBuilder);
  private projectService = inject(ProjectsService);
  private projectApiService = inject(ProjectsApiService);
  private securityService = inject(SecurityService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private translationService = inject(TranslateService);
  private snackBarService = inject(SnackBarService);
  private renderer = inject(Renderer2);
  private schemaService = inject(SchemaService);
  private externalReferentialService = inject(ExternalReferentialService);
  private tenantSelectionService = inject(TenantSelectionService);

  @Output()
  backToNormalLateralPanel: EventEmitter<any> = new EventEmitter();
  @Output()
  previewClose: EventEmitter<any> = new EventEmitter();
  @Output()
  showExtendedLateralPanel: EventEmitter<any> = new EventEmitter();

  tenantId: number;
  checkEditDescriptionRole = new Observable<boolean>();
  checkEditContextRole = new Observable<boolean>();
  checkEditAttachmentRole = new Observable<boolean>();
  checkEditConfigurationRole = new Observable<boolean>();

  protected readonly FilingPlanMode = FilingPlanMode;
  protected readonly FixedAttachmentMode = AttachmentMode;

  // Attachment position mode (Arbres & Plans tree vs free GUID input) for the fixed/default position
  fixedAttachmentMode: AttachmentMode = AttachmentMode.TREE;

  form: FormGroup;

  project = signal<Project>({} as Project);
  jsltFilename = 'TransformationRules.jslt';
  transformationRulesControl = computed(() => {
    return new FormControl(
      this.project().transformationRules && this.form.get('transformationRules').pristine
        ? [new File([this.project().transformationRules], this.jsltFilename)]
        : undefined,
    );
  });

  acquisitionInformationsList: string[];
  legalStatusList: Option[] = [];
  schemaOptions: ItemNode<SchemaElement>[];
  units: Unit[];
  archivalAgreementOptions$: Observable<Option[]>;
  archiveProfileOptions$: Observable<Option[]>;
  agenciesOptions$: Observable<Option[]>;
  easOptions$: Observable<Option[]> = this.externalReferentialService.getElectronicArchivingSystemOptions$();

  protected readonly Workflow = Workflow;
  getProjectIcon = getProjectIcon;
  getProjectWorkflow = getProjectWorkflow;

  @ViewChild('tabs', { static: false }) tabGroup: MatTabGroup;
  @ViewChildren(MatTab) tabs: QueryList<MatTab>;
  @ViewChild('confirmEditProject', { static: true }) confirmEditProject: TemplateRef<ProjectPreviewComponent>;
  @ViewChild('confirmEditAttachments', { static: true }) confirmEditAttachments: TemplateRef<ProjectPreviewComponent>;
  @ViewChild('cancelDialog') cancelDialog: TemplateRef<ProjectPreviewComponent>;

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

  #editMode = false;
  isPanelExtended = false;
  dialogRefToClose: MatDialogRef<ProjectPreviewComponent>;
  selectedValue = 'YES';

  transactions$: BehaviorSubject<PaginatedResponse<Transaction>> = new BehaviorSubject<PaginatedResponse<Transaction>>(null);
  openedTransactions$ = this.transactions$.pipe(map((ts) => ts?.values?.filter((t) => t?.status === 'OPEN')));

  constructor() {
    const filingPlanService = inject(FilingPlanService);

    filingPlanService.loadFilingPlan().subscribe((units) => (this.units = units));
    this.route.params.subscribe((params) => {
      if (params.tenantIdentifier) {
        // eslint-disable-next-line radix
        this.tenantId = parseInt(params.tenantIdentifier);
      }
    });
  }

  ngOnInit(): void {
    this.checkEditDescriptionRole = this.securityService.hasRole$(
      ApplicationId.COLLECT_APP,
      'ROLE_UPDATE_PROJECTS_DESCRIPTION',
      this.tenantId,
    );
    this.checkEditContextRole = this.securityService.hasRole$(ApplicationId.COLLECT_APP, 'ROLE_UPDATE_PROJECTS_CONTEXT', this.tenantId);
    this.checkEditAttachmentRole = this.securityService.hasRole$(
      ApplicationId.COLLECT_APP,
      'ROLE_UPDATE_PROJECTS_ATTACHMENT',
      this.tenantId,
    );
    this.checkEditConfigurationRole = this.securityService.hasRole$(
      ApplicationId.COLLECT_APP,
      'ROLE_UPDATE_PROJECTS_CONFIG',
      this.tenantId,
    );
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
      this.clickOutSideListener = this.renderer.listen(
        projectList,
        'click',
        (event: PointerEvent) => {
          if (this.isModified()) event.stopPropagation();
          this.shouldCancelNavigation();
        },
        { capture: true },
      );
    }
  }

  private async interceptTabChange(_tab: MatTab, _tabHeader: MatTabHeader, idx: number) {
    if (!(this.isModified() && (await this.shouldCancelNavigation()))) {
      this.#editMode = false;
      this.tabGroup.selectedIndex = idx;
    }
  }

  private async shouldCancelNavigation(): Promise<boolean> {
    if (this.isModified() && this.dialogRefToClose?.getState() !== 0) {
      await this.openCancelDialog();
      return true;
    }
    return false;
  }

  loadProject() {
    const project$ = this.projectId$.pipe(
      scan((acc, newValue) => (this.isModified() ? acc : newValue), this.projectId$.getValue()), // Keep the old value if we have edited data not yet saved; **isModified()** is true.
      distinctUntilChanged(), // Avoid calling multiple times with the same value.
      mergeMap((projectId) => this.projectService.getProjectById(projectId)),
      share(),
    );
    project$.subscribe((project) => {
      this.project.set(project);
      this.showNormalPanel();
      this.configForm();
      this.initForm();
      // Build the rules FormArray whenever the project carries rules, even if it also has a default attachment position
      if (project.unitUps?.length) {
        this.initRuleParams(project);
      }
    });

    project$
      .pipe(
        filter((project) => project.archivingSystemId && project.archivingSystemTenant !== undefined),
        map((project) => ({ archivingSystemId: project.archivingSystemId, archivingSystemTenant: project.archivingSystemTenant })),
        distinctUntilChanged((p, c) => p.archivingSystemTenant === c.archivingSystemTenant && p.archivingSystemId === c.archivingSystemId),
      )
      .subscribe(({ archivingSystemId, archivingSystemTenant }) => {
        const toOptions = (obs: Observable<any[]>) =>
          obs.pipe(
            map((items: { identifier: string; name: string }[]) =>
              items
                .map(
                  (item) =>
                    ({
                      key: item.identifier,
                      label: `${item.identifier} - ${item.name}`,
                    }) as Option,
                )
                .sort((a1, a2) => a1.label.localeCompare(a2.label)),
            ),
            catchError(() => of([])),
            shareReplay(1),
          );
        this.archivalAgreementOptions$ = toOptions(
          this.externalReferentialService.archivalIngestContracts(archivingSystemId, archivingSystemTenant),
        );
        this.archiveProfileOptions$ = toOptions(this.externalReferentialService.archiveProfiles(archivingSystemId, archivingSystemTenant));
        this.agenciesOptions$ = toOptions(this.externalReferentialService.getAgencies(archivingSystemId, archivingSystemTenant));
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

        // Restore the saved position: tree selection if the GUID is a node of the filing plan, free GUID input otherwise
        const attachmentMode = this.resolveAttachmentMode(metadataUnitUp.unitUp);
        const useTree = this.connectedToLocalEasWithCurrentTenant && attachmentMode === AttachmentMode.TREE;

        this.unitUps.push(
          this.formBuilder.group({
            ontologyList: ontologyListControl,
            metadataValue: metadataValueControl,
            // Per-rule attachment position mode: tree (Arbres & Plans) or free GUID input
            attachmentMode: [attachmentMode],
            unitUp: useTree
              ? [
                  {
                    included: metadataUnitUp.unitUp ? [metadataUnitUp.unitUp] : [],
                    excluded: [],
                  },
                  oneIncludedNodeRequired(),
                ]
              : this.formBuilder.control(metadataUnitUp.unitUp, this.connectedToLocalEasWithCurrentTenant ? Validators.required : null),
          }),
        );
      });
    });
  }

  /** Determines whether a saved attachment position was picked in the filing plan (tree) or typed as a free GUID. */
  private resolveAttachmentMode(unitId: string): AttachmentMode {
    if (!this.connectedToLocalEasWithCurrentTenant || !unitId) {
      return AttachmentMode.TREE;
    }
    return this.units?.some((unit) => unit['#id'] === unitId) ? AttachmentMode.TREE : AttachmentMode.GUID;
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
      // Per-rule attachment position mode: tree (Arbres & Plans) or free GUID input
      attachmentMode: [AttachmentMode.TREE],
      unitUp: this.connectedToLocalEasWithCurrentTenant
        ? [
            {
              included: [],
              excluded: [],
            },
            oneIncludedNodeRequired(),
          ]
        : '',
    });

    this.unitUps.push(newRuleParamForm);
  }

  deleteRuleParam(index: number) {
    this.unitUps.removeAt(index);
    this.form.markAsDirty(); // We should be able to submit the form if we only remove a rule
  }

  searchArchiveUnitsByProject() {
    this.router.navigate([`collect/tenant/${this.tenantIdentifier}/units`, this.project().id]);
  }

  emitClose() {
    if (this.isModified()) {
      this.openCancelDialog();
    } else {
      this.showNormalPanel();
      this.previewClose.emit();
      this.tabGroup.selectedIndex = 0;
      this.projectService.selectedProjectId$.next(null);
    }
  }

  showNormalPanel() {
    this.isPanelExtended = false;
    this.backToNormalLateralPanel.emit();
    this.#editMode = false;
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
      unitUps: this.formBuilder.array([], this.project().unitUps?.length ? Validators.required : null),
      // toggle for the default fixed attachment position (key/value rules flow)
      defaultAttachmentEnabled: [false],
      automaticIngest: [],
      transformationRules: [],
    });
  }

  get connectedToLocalEasWithCurrentTenant(): boolean {
    const currentTenantId = this.tenantSelectionService.getSelectedTenant()?.identifier;
    return (
      this.project().connectedToArchivingSystem &&
      this.project().archivingSystemId === LOCAL_ARCHIVING_SYSTEM_ID &&
      this.project().archivingSystemTenant === currentTenantId
    );
  }

  get defaultAttachmentEnabled(): boolean {
    return this.form?.get('defaultAttachmentEnabled')?.value === true;
  }

  onDefaultAttachmentToggle(): void {
    if (!this.defaultAttachmentEnabled) {
      this.fixedAttachmentMode = AttachmentMode.TREE;
      this.form.get('unitUp')?.setValue(this.connectedToLocalEasWithCurrentTenant ? { included: [], excluded: [] } : null);
    }
    this.form.markAsDirty();
  }

  setFixedAttachmentMode(value: AttachmentMode): void {
    this.fixedAttachmentMode = value;
    // The single unitUp control holds either a tree selection ({ included, excluded }) or a free GUID string
    this.form.get('unitUp')?.setValue(value === AttachmentMode.TREE ? { included: [], excluded: [] } : '');
    this.form.markAsDirty();
  }

  setRuleAttachmentMode(ruleParamForm: FormGroup, mode: AttachmentMode): void {
    ruleParamForm.get('attachmentMode')?.setValue(mode);
    const unitUpControl = ruleParamForm.get('unitUp');
    if (mode === AttachmentMode.TREE) {
      unitUpControl?.setValue({ included: [], excluded: [] });
      unitUpControl?.setValidators(oneIncludedNodeRequired());
    } else {
      unitUpControl?.setValue('');
      unitUpControl?.setValidators(Validators.required);
    }
    unitUpControl?.updateValueAndValidity();
    this.form.markAsDirty();
  }

  getNodeTitle(selectedNode?: { unitUp?: { included?: string[] } | string }): string {
    const unitUp = selectedNode?.unitUp;
    const vitamId = typeof unitUp === 'string' ? unitUp : unitUp?.included?.[0];
    if (!vitamId) return '';
    const name = this.getUnitName(vitamId);
    return name ? ` : ${name}` : ` : ${vitamId}`;
  }

  /** Mirrors create-project's step validation: rules valid OR a default attachment position filled. */
  attachmentStepInvalid(): boolean {
    if (!this.project().unitUps?.length) {
      // Fixed attachment flow: the position is optional
      return false;
    }
    const rulesValid = this.unitUps.valid;
    const defaultFilled = this.defaultAttachmentEnabled && !this.defaultAttachmentPositionIsEmpty();
    return !(rulesValid || defaultFilled);
  }

  private defaultAttachmentPositionIsEmpty(): boolean {
    if (this.connectedToLocalEasWithCurrentTenant && this.fixedAttachmentMode === AttachmentMode.TREE) {
      return !this.form.get('unitUp')?.value?.included?.length;
    }
    return !this.form.get('unitUp')?.value;
  }

  private resolveFixedUnitUp(): string {
    const value = this.form.value.unitUp;
    return this.connectedToLocalEasWithCurrentTenant && this.fixedAttachmentMode === AttachmentMode.TREE
      ? (value?.included?.[0] ?? null)
      : value || null;
  }

  isModified(): boolean {
    // use pristine to check if the form is unchanged.
    return this.#editMode && !this.form.pristine;
  }

  showEdit(tab: MatTab) {
    const tabIndex = this.tabs.toArray().indexOf(tab);
    if (tabIndex !== -1) {
      this.form.markAsPristine();
      this.#editMode = true;
      this.tabGroup.selectedIndex = tabIndex;
      this.showExtendedPanel();
      this.initForm();
    }
  }

  initForm() {
    if (this.form) {
      this.form.get('messageIdentifier').setValue(this.project().messageIdentifier);
      this.form.get('comment').setValue(this.project().comment);
      this.form.get('originatingAgencyIdentifier').setValue(this.project().originatingAgencyIdentifier);
      this.form.get('submissionAgencyIdentifier').setValue(this.project().submissionAgencyIdentifier);
      this.form.get('archivalAgencyIdentifier').setValue(this.project().archivalAgencyIdentifier);
      this.form.get('transferringAgencyIdentifier').setValue(this.project().transferringAgencyIdentifier);
      this.form.get('archivalAgreement').setValue(this.project().archivalAgreement);
      this.form.get('archiveProfile').setValue(this.project().archiveProfile);
      this.form.get('acquisitionInformation').setValue(this.project().acquisitionInformation);
      this.form.get('legalStatus').setValue(this.project().legalStatus);
      // Restore the saved position mode: tree if the GUID is a node of the filing plan, free GUID input otherwise
      this.fixedAttachmentMode = this.resolveAttachmentMode(this.project().unitUp);
      // A rules-flow project may also carry a fixed default attachment position (project.unitUp)
      const hasRules = !!this.project().unitUps?.length;
      this.form.get('defaultAttachmentEnabled').setValue(hasRules && !!this.project().unitUp);
      this.form
        .get('unitUp')
        .setValue(
          this.connectedToLocalEasWithCurrentTenant && this.fixedAttachmentMode === AttachmentMode.TREE
            ? { included: this.project().unitUp ? [this.project().unitUp] : [], excluded: [] }
            : (this.project().unitUp ?? null),
        );
      this.form.get('automaticIngest').setValue(this.project().automaticIngest);
      this.form.get('transformationRules').setValue(this.project().transformationRules);
    }
  }

  async update(): Promise<boolean> {
    const currentTab = this.getCurrentTab();

    if (['description', 'context'].includes(currentTab)) {
      return await this.launchUpdate();
    }
    if (['attachment', 'configuration'].includes(currentTab)) {
      return await this.launchAttachmentsUpdate();
    }
  }

  private getCurrentTab() {
    return ['description', 'context', 'attachment', 'configuration'][this.tabGroup.selectedIndex];
  }

  private launchUpdate = (): Promise<boolean> => {
    const dialogToOpen = this.confirmEditProject;
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'id', Direction.ASCENDANT);
    this.projectApiService.getTransactionsByProjectId(pageRequest, this.projectId$.getValue()).subscribe((transactions) => {
      this.transactions$.next(transactions);
    });
    this.dialogRefToClose = this.dialog.open(dialogToOpen);
    return lastValueFrom(this.dialogRefToClose.afterClosed());
  };

  private launchAttachmentsUpdate = (): Promise<boolean> => {
    const dialogToOpen = this.confirmEditAttachments;
    this.dialogRefToClose = this.dialog.open(dialogToOpen);
    return lastValueFrom(this.dialogRefToClose.afterClosed());
  };

  private fillTransactionFromProject(transaction: Transaction) {
    transaction.archivalAgreement = this.project().archivalAgreement;
    transaction.messageIdentifier = this.project().messageIdentifier;
    transaction.archivalAgencyIdentifier = this.project().archivalAgencyIdentifier;
    transaction.transferringAgencyIdentifier = this.project().transferringAgencyIdentifier;
    transaction.originatingAgencyIdentifier = this.project().originatingAgencyIdentifier;
    transaction.submissionAgencyIdentifier = this.project().submissionAgencyIdentifier;
    transaction.archiveProfile = this.project().archiveProfile;
    transaction.legalStatus = this.project().legalStatus;
    transaction.comment = this.project().comment;
    transaction.acquisitionInformation = this.project().acquisitionInformation;
    // transaction.unitUp = this.project().unitUp;
    // transaction.unitUps = this.project().unitUps;
    return transaction;
  }

  downloadJSLT() {
    const blob = new Blob([this.project().transformationRules], { type: 'octet/stream' });
    download(blob, this.jsltFilename);
  }

  async updateProject(updateTransactions: boolean) {
    const isRulesFlow = !!this.project().unitUps?.length;
    const projectToUpdate = {
      ...this.form.value,
      id: this.project().id,
      createdOn: this.project().createdOn,
      status: this.project().status,
      name: this.form.value.messageIdentifier,
      archivingSystemId: this.project().archivingSystemId,
      archivingSystemTenant: this.project().archivingSystemTenant,
      connectedToArchivingSystem: this.project().connectedToArchivingSystem,
      // Fixed flow: the position; rules flow: the optional default attachment position
      unitUp: isRulesFlow ? (this.defaultAttachmentEnabled ? this.resolveFixedUnitUp() : null) : this.resolveFixedUnitUp(),
      unitUps: isRulesFlow
        ? this.unitUps.controls
            // Only keep fully filled rules (incomplete ones are allowed when a default attachment is set)
            .filter((ruleParamControl: FormGroup) => ruleParamControl.valid && ruleParamControl.value.ontologyList)
            .map((ruleParamControl: FormGroup) => {
              const ruleParam = ruleParamControl.value;
              return {
                metadataKey: ruleParam.ontologyList.ApiField,
                metadataValue: ruleParam.metadataValue,
                unitUp:
                  this.connectedToLocalEasWithCurrentTenant && ruleParam.attachmentMode === AttachmentMode.TREE
                    ? ruleParam.unitUp.included[0]
                    : ruleParam.unitUp,
              };
            })
        : [],
    };

    // Retrieve the correct update function depending on the tab that has been edited
    const updateProjectFunction: (project: Project) => Observable<Project> = {
      description: this.projectService.updateProjectDescription,
      context: this.projectService.updateProjectContext,
      attachment: this.projectService.updateProjectAttachments,
      configuration: this.projectService.updateProjectConfiguration,
    }[this.getCurrentTab()].bind(this.projectService);

    const updateProjectOperation$ = updateProjectFunction(projectToUpdate);
    const previousProject = this.project();
    try {
      const project = await lastValueFrom(updateProjectOperation$);
      this.dialogRefToClose.close(true);
      this.showNormalPanel();
      if (this.projectId === project.id) {
        this.project.set(project);
      } else {
        this.projectId$.next(this.projectId);
      }
      this.projectService.nextUpdatedProject(project);

      const transactions: Transaction[] = this.transactions$.value?.values || [];

      // FIXME: shouldn't it be done on server side?!
      if (updateTransactions) {
        const updateTransactionOperation$: Observable<Transaction>[] = transactions
          .filter((transaction) => transaction.status === TransactionStatus.OPEN)
          .map((transaction) => this.projectApiService.updateTransaction(this.fillTransactionFromProject(transaction)));
        if (updateTransactionOperation$?.length) await lastValueFrom(combineLatest(updateTransactionOperation$));
      }

      const hasTransactionsKO = updateTransactions && transactions.some((transaction) => transaction.status === TransactionStatus.KO);
      if (this.getCurrentTab() === 'attachment') {
        this.snackBarService.open({
          message: 'COLLECT.UPDATE_PROJECT_ATTACHMENTS.TERMINATED',
          duration: 10000,
        });
      } else {
        this.snackBarService.open({
          message: `${this.translationService.instant('COLLECT.UPDATE_PROJECT.TERMINATED')}${hasTransactionsKO ? ` ${this.translationService.instant('COLLECT.UPDATE_PROJECT.TRANSACTIONS_KO')}` : ''}`,
          translate: false,
          duration: 10000,
        });
      }
    } catch (e) {
      console.error(e);
      if (this.projectId === previousProject.id) {
        this.project.set(previousProject);
      } else {
        this.projectId$.next(this.projectId);
      }
    } finally {
      this.showNormalPanel();
    }
  }

  private async onCancel() {
    this.showNormalPanel();
    if (this.projectId !== this.project().id) {
      this.projectId$.next(this.projectId);
    }
    this.dialogRefToClose?.close(true);
  }

  openCancelDialog = async (): Promise<void> => {
    if (!this.isModified()) {
      await this.onCancel();
      return;
    }

    const result = await firstValueFrom(this.dialog.open(this.cancelDialog, this.dialogConfig).afterClosed());
    if (result === true) {
      await this.update();
    } else if (result === false) {
      await this.onCancel();
    } else {
      // Back to form in edition mode
    }
  };

  getUnitName(unitId: string): string {
    const foundNode = this.units?.find((unit) => unit['#id'] === unitId);
    return foundNode ? fetchTitle(foundNode.Title, foundNode.Title_) : '';
  }

  ngOnDestroy() {
    if (this.clickOutSideListener) {
      this.clickOutSideListener();
    }
  }

  getExternalSystemName$(archivingSystemId: string): Observable<string> {
    return this.externalReferentialService
      .getElectronicArchivingSystemList$()
      .pipe(map((list) => (list || []).find((system) => system.archivingSystemId === archivingSystemId)?.name || archivingSystemId));
  }

  getLabel$(attribute: keyof Project): Observable<string> {
    const key = this.project()[attribute];
    if (this.project().connectedToArchivingSystem) {
      const options = [
        'originatingAgencyIdentifier',
        'submissionAgencyIdentifier',
        'archivalAgencyIdentifier',
        'transferringAgencyIdentifier',
      ].includes(attribute)
        ? this.agenciesOptions$
        : attribute === 'archivalAgreement'
          ? this.archivalAgreementOptions$
          : attribute === 'archiveProfile'
            ? this.archiveProfileOptions$
            : of([]);

      return options.pipe(
        map((items) => items.find((item) => item.key === key)?.label || key),
        startWith(key),
      );
    }
    return of(key ? String(key) : null);
  }

  editMode(tab?: MatTab): boolean {
    return this.#editMode && (!tab || this.tabs.toArray()[this.tabGroup.selectedIndex] === tab);
  }

  protected readonly LOCAL_ARCHIVING_SYSTEM_ID = LOCAL_ARCHIVING_SYSTEM_ID;
  protected readonly TENANT_SEPARATOR = TENANT_SEPARATOR;

  async handleJsltFile(files: File[]) {
    const jsltFile = files?.length ? files[0] : undefined;
    const content: string = jsltFile ? await readFileContent(jsltFile) : null;
    this.form.get('transformationRules').markAsDirty();
    this.form.get('transformationRules').setValue(content);
  }
}
