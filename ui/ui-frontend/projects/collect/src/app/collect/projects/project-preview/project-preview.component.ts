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
  inject,
  input,
  linkedSignal,
  OnChanges,
  OnDestroy,
  OnInit,
  Renderer2,
  signal,
  SimpleChanges,
  TemplateRef,
  output,
  untracked,
  viewChild,
  viewChildren,
} from '@angular/core';
import { applyEach, disabled, form, FormField, required, validate } from '@angular/forms/signals';
import { MatDialog, MatDialogConfig, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatTab, MatTabGroup, MatTabHeader, MatTabsModule } from '@angular/material/tabs';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
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
  Option,
  PageRequest,
  PaginatedResponse,
  Project,
  readFileContent,
  requiredNotBlank,
  requiredValue,
  SchemaElement,
  SchemaService,
  SecurityService,
  SnackBarService,
  TENANT_SEPARATOR,
  TenantSelectionService,
  Transaction,
  TransactionStatus,
  Unit,
  VitamUICommonModule,
  VitamUILibraryModule,
  Workflow,
} from 'vitamui-library';
import { AttachmentMode, AttachmentNodes, LOCAL_ARCHIVING_SYSTEM_ID, RuleParamForm } from '../create-project/create-project.component';

interface ProjectPreviewForm {
  messageIdentifier: string;
  id: string;
  comment: string;
  originatingAgencyIdentifier: string;
  submissionAgencyIdentifier: string;
  archivalAgencyIdentifier: string;
  transferringAgencyIdentifier: string;
  archivalAgreement: string;
  archiveProfile: string;
  acquisitionInformation: string;
  legalStatus: string;
  // Either a tree selection or a free GUID
  unitUp: AttachmentNodes | string;
  unitUps: RuleParamForm[];
  // toggle for the default fixed attachment position (key/value rules flow)
  defaultAttachmentEnabled: boolean;
  automaticIngest: boolean;
  transformationRules: string;
}

const emptyProjectPreviewForm = (): ProjectPreviewForm => ({
  messageIdentifier: null,
  id: null,
  comment: null,
  originatingAgencyIdentifier: null,
  submissionAgencyIdentifier: null,
  archivalAgencyIdentifier: null,
  transferringAgencyIdentifier: null,
  archivalAgreement: null,
  archiveProfile: null,
  acquisitionInformation: null,
  legalStatus: null,
  unitUp: null,
  unitUps: [],
  defaultAttachmentEnabled: false,
  automaticIngest: null,
  transformationRules: null,
});

const emptyAttachmentNodes = (): AttachmentNodes => ({ included: [], excluded: [] });

@Component({
  selector: 'app-project-preview',
  templateUrl: './project-preview.component.html',
  styleUrls: ['./project-preview.component.scss'],
  imports: [
    AsyncPipe,
    FormField,
    MatButtonModule,
    MatButtonToggleModule,
    MatDialogModule,
    MatMenuModule,
    MatProgressSpinner,
    MatTabsModule,
    NgClass,
    NgTemplateOutlet,
    VitamUICommonModule,
    VitamUILibraryModule,
    TranslatePipe,
  ],
})
export class ProjectPreviewComponent implements OnChanges, OnInit, AfterViewInit, OnDestroy {
  private readonly projectService = inject(ProjectsService);
  private readonly projectApiService = inject(ProjectsApiService);
  private readonly securityService = inject(SecurityService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly translationService = inject(TranslateService);
  private readonly snackBarService = inject(SnackBarService);
  private readonly renderer = inject(Renderer2);
  private readonly schemaService = inject(SchemaService);
  private readonly externalReferentialService = inject(ExternalReferentialService);
  private readonly tenantSelectionService = inject(TenantSelectionService);

  readonly backToNormalLateralPanel = output<void>();
  readonly previewClose = output<void>();
  readonly showExtendedLateralPanel = output<void>();

  tenantId: number;
  checkEditDescriptionRole = new Observable<boolean>();
  checkEditContextRole = new Observable<boolean>();
  checkEditAttachmentRole = new Observable<boolean>();
  checkEditConfigurationRole = new Observable<boolean>();

  protected readonly FilingPlanMode = FilingPlanMode;
  protected readonly FixedAttachmentMode = AttachmentMode;

  // Attachment position mode (Arbres & Plans tree vs free GUID input) for the fixed/default position
  fixedAttachmentMode: AttachmentMode = AttachmentMode.TREE;

  project = signal<Project>({} as Project);

  private readonly model = signal<ProjectPreviewForm>(emptyProjectPreviewForm());
  readonly form = form(this.model, (path) => {
    requiredNotBlank(path.messageIdentifier);
    requiredNotBlank(path.originatingAgencyIdentifier);
    requiredNotBlank(path.submissionAgencyIdentifier);
    requiredNotBlank(path.archivalAgencyIdentifier);
    requiredNotBlank(path.transferringAgencyIdentifier);
    requiredNotBlank(path.archivalAgreement);
    requiredValue(path.unitUps, { when: () => !!this.project().unitUps?.length });
    applyEach(path.unitUps, (ruleParam) => {
      requiredValue(ruleParam.ontologyList);
      requiredValue(ruleParam.metadataValue);
      validate(ruleParam.unitUp, ({ value }) => {
        const unitUp = value();
        if (typeof unitUp !== 'string') {
          // Same errors as oneIncludedNodeRequired()
          if (!unitUp) return { kind: 'missingNodes' };
          return unitUp.included?.length ? undefined : { kind: 'missingIncludedNodes' };
        }
        // A free GUID can only be typed when connected to the local EAS
        return this.connectedToLocalEasWithCurrentTenant && !unitUp ? { kind: 'required' } : undefined;
      });
    });
  });

  // Read-only fields
  readonly projectIdField = form(
    linkedSignal(() => this.project().id ?? null),
    (path) => disabled(path),
  );
  readonly archivingSystemField = form(
    linkedSignal(() => this.project().archivingSystemId + TENANT_SEPARATOR + this.project().archivingSystemTenant),
    (path) => {
      disabled(path);
      required(path);
    },
  );

  jsltFilename = 'TransformationRules.jslt';
  readonly transformationRulesForm = form(
    linkedSignal<File[]>(() =>
      this.project().transformationRules && untracked(() => !this.form.transformationRules().dirty())
        ? [new File([this.project().transformationRules], this.jsltFilename)]
        : [],
    ),
  );

  acquisitionInformationsList: string[];
  legalStatusList: Option[] = [];
  schemaOptions = signal<ItemNode<SchemaElement>[]>(undefined);
  units = signal<Unit[]>(undefined);
  archivalAgreementOptions$: Observable<Option[]>;
  archiveProfileOptions$: Observable<Option[]>;
  agenciesOptions$: Observable<Option[]>;
  easOptions$: Observable<Option[]> = this.externalReferentialService.getElectronicArchivingSystemOptions$();

  protected readonly Workflow = Workflow;
  getProjectIcon = getProjectIcon;
  getProjectWorkflow = getProjectWorkflow;

  readonly tabGroup = viewChild<MatTabGroup>('tabs');
  readonly tabs = viewChildren(MatTab);
  readonly confirmEditProject = viewChild<TemplateRef<ProjectPreviewComponent>>('confirmEditProject');
  readonly confirmEditAttachments = viewChild<TemplateRef<ProjectPreviewComponent>>('confirmEditAttachments');
  readonly cancelDialog = viewChild<TemplateRef<ProjectPreviewComponent>>('cancelDialog');

  readonly projectId = input<string>();

  private readonly projectId$ = new BehaviorSubject<string>(null);
  private tenantIdentifier: string;
  private clickOutSideListener!: () => void;
  private readonly dialogConfig: MatDialogConfig = { panelClass: 'vitamui-dialog' };

  #editMode = false;
  isPanelExtended = false;
  dialogRefToClose: MatDialogRef<ProjectPreviewComponent>;
  readonly updateTransactionsField = form(signal<'YES' | 'NO'>('YES'));

  transactions$: BehaviorSubject<PaginatedResponse<Transaction>> = new BehaviorSubject<PaginatedResponse<Transaction>>(null);
  openedTransactions$ = this.transactions$.pipe(map((ts) => ts?.values?.filter((t) => t?.status === 'OPEN')));

  constructor() {
    const filingPlanService = inject(FilingPlanService);

    filingPlanService.loadFilingPlan().subscribe((units) => this.units.set(units));
    this.route.params.subscribe((params) => {
      if (params['tenantIdentifier']) {
        this.tenantId = parseInt(params['tenantIdentifier']);
      }
    });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['projectId']) {
      this.projectId$.next(this.projectId());
      const tabGroup = this.tabGroup();
      if (tabGroup) tabGroup.selectedIndex = 0;
    }
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
      this.tenantIdentifier = params['tenantIdentifier'];
    });

    this.loadProject();

    this.legalStatusList = this.projectService.getLegalStatusList();
    this.acquisitionInformationsList = this.projectService.getAcquisitionInformationsList();
  }

  ngAfterViewInit() {
    this.tabGroup()._handleClick = this.interceptTabChange.bind(this);

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
      this.tabGroup().selectedIndex = idx;
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
      // Build the rules whenever the project carries rules, even if it also has a default attachment position
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
      const res = this.schemaService.getMetadataKeysByKeys(keys, schema);
      const ruleParams = project.unitUps.map((metadataUnitUp): RuleParamForm => {
        const item = res.find((sc) => sc.item.ApiField === metadataUnitUp.metadataKey).item;

        // Restore the saved position: tree selection if the GUID is a node of the filing plan, free GUID input otherwise
        const attachmentMode = this.resolveAttachmentMode(metadataUnitUp.unitUp);
        const useTree = this.connectedToLocalEasWithCurrentTenant && attachmentMode === AttachmentMode.TREE;

        return {
          ontologyList: item,
          metadataValue: metadataUnitUp.metadataValue ?? null,
          // Per-rule attachment position mode: tree (Arbres & Plans) or free GUID input
          attachmentMode,
          unitUp: useTree
            ? { included: metadataUnitUp.unitUp ? [metadataUnitUp.unitUp] : [], excluded: [] }
            : (metadataUnitUp.unitUp ?? null),
        };
      });
      this.form.unitUps().value.update((unitUps) => [...unitUps, ...ruleParams]);
      this.schemaOptions.set(schema);
    });
  }

  /** Determines whether a saved attachment position was picked in the filing plan (tree) or typed as a free GUID. */
  private resolveAttachmentMode(unitId: string): AttachmentMode {
    if (!this.connectedToLocalEasWithCurrentTenant || !unitId) {
      return AttachmentMode.TREE;
    }
    return this.units()?.some((unit) => unit['#id'] === unitId) ? AttachmentMode.TREE : AttachmentMode.GUID;
  }

  getName(item: SchemaElement): string {
    const path = item.Path.split('.').slice(0, -1);
    const parent = path.reduce((acc, p) => acc.children.find((o) => o.item.FieldName === p), {
      children: this.schemaOptions(),
    } as ItemNode<SchemaElement>);
    return `${item.ShortName}${parent?.item ? ` (${parent.item.ShortName})` : ''}`;
  }

  addRuleParam() {
    const newRuleParam: RuleParamForm = {
      ontologyList: null,
      metadataValue: null,
      // Per-rule attachment position mode: tree (Arbres & Plans) or free GUID input
      attachmentMode: AttachmentMode.TREE,
      unitUp: this.connectedToLocalEasWithCurrentTenant ? emptyAttachmentNodes() : '',
    };
    this.form.unitUps().value.update((unitUps) => [...unitUps, newRuleParam]);
  }

  onOntologyChange(index: number) {
    // To make sure error messages appear after changing the ontology
    this.form.unitUps[index].metadataValue().markAsTouched();
  }

  deleteRuleParam(index: number) {
    this.form.unitUps().value.update((unitUps) => unitUps.filter((_, i) => i !== index));
    this.form().markAsDirty(); // We should be able to submit the form if we only remove a rule
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
      this.tabGroup().selectedIndex = 0;
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
    this.form().reset(emptyProjectPreviewForm());
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
    return this.form.defaultAttachmentEnabled().value() === true;
  }

  onDefaultAttachmentToggle(): void {
    if (!this.defaultAttachmentEnabled) {
      this.fixedAttachmentMode = AttachmentMode.TREE;
      this.form.unitUp().value.set(this.connectedToLocalEasWithCurrentTenant ? emptyAttachmentNodes() : null);
    }
    this.form().markAsDirty();
  }

  setFixedAttachmentMode(value: AttachmentMode): void {
    this.fixedAttachmentMode = value;
    // The single unitUp control holds either a tree selection ({ included, excluded }) or a free GUID string
    this.form.unitUp().value.set(value === AttachmentMode.TREE ? emptyAttachmentNodes() : '');
    this.form().markAsDirty();
  }

  setRuleAttachmentMode(index: number, mode: AttachmentMode): void {
    this.form
      .unitUps()
      .value.update((unitUps) =>
        unitUps.map((ruleParam, i) =>
          i === index
            ? { ...ruleParam, attachmentMode: mode, unitUp: mode === AttachmentMode.TREE ? emptyAttachmentNodes() : '' }
            : ruleParam,
        ),
      );
    this.form().markAsDirty();
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
    const rulesValid = this.form.unitUps().valid();
    const defaultFilled = this.defaultAttachmentEnabled && !this.defaultAttachmentPositionIsEmpty();
    return !(rulesValid || defaultFilled);
  }

  private defaultAttachmentPositionIsEmpty(): boolean {
    if (this.connectedToLocalEasWithCurrentTenant && this.fixedAttachmentMode === AttachmentMode.TREE) {
      return !(this.form.unitUp().value() as AttachmentNodes)?.included?.length;
    }
    return !this.form.unitUp().value();
  }

  private resolveFixedUnitUp(): string {
    const value = this.form.unitUp().value();
    return this.connectedToLocalEasWithCurrentTenant && this.fixedAttachmentMode === AttachmentMode.TREE
      ? ((value as AttachmentNodes)?.included?.[0] ?? null)
      : (value as string) || null;
  }

  isModified(): boolean {
    // use pristine to check if the form is unchanged.
    return this.#editMode && this.form().dirty();
  }

  showEdit(tab: MatTab) {
    const tabIndex = this.tabs().indexOf(tab);
    if (tabIndex !== -1) {
      this.form().reset();
      this.#editMode = true;
      this.tabGroup().selectedIndex = tabIndex;
      this.showExtendedPanel();
      this.initForm();
    }
  }

  initForm() {
    const project = this.project();
    // Restore the saved position mode: tree if the GUID is a node of the filing plan, free GUID input otherwise
    this.fixedAttachmentMode = this.resolveAttachmentMode(project.unitUp);
    // A rules-flow project may also carry a fixed default attachment position (project.unitUp)
    const hasRules = !!project.unitUps?.length;
    this.model.update((value) => ({
      ...value,
      messageIdentifier: project.messageIdentifier ?? null,
      comment: project.comment ?? null,
      originatingAgencyIdentifier: project.originatingAgencyIdentifier ?? null,
      submissionAgencyIdentifier: project.submissionAgencyIdentifier ?? null,
      archivalAgencyIdentifier: project.archivalAgencyIdentifier ?? null,
      transferringAgencyIdentifier: project.transferringAgencyIdentifier ?? null,
      archivalAgreement: project.archivalAgreement ?? null,
      archiveProfile: project.archiveProfile ?? null,
      acquisitionInformation: project.acquisitionInformation ?? null,
      legalStatus: project.legalStatus ?? null,
      defaultAttachmentEnabled: hasRules && !!project.unitUp,
      unitUp:
        this.connectedToLocalEasWithCurrentTenant && this.fixedAttachmentMode === AttachmentMode.TREE
          ? { included: project.unitUp ? [project.unitUp] : [], excluded: [] }
          : (project.unitUp ?? null),
      automaticIngest: project.automaticIngest ?? null,
      transformationRules: project.transformationRules ?? null,
    }));
  }

  async update(): Promise<boolean> {
    const currentTab = this.getCurrentTab();

    if (['description', 'context'].includes(currentTab)) {
      return await this.launchUpdate();
    }
    if (['attachment', 'configuration'].includes(currentTab)) {
      return await this.launchAttachmentsUpdate();
    }
    return false;
  }

  private getCurrentTab() {
    return ['description', 'context', 'attachment', 'configuration'][this.tabGroup().selectedIndex];
  }

  private readonly launchUpdate = (): Promise<boolean> => {
    const dialogToOpen = this.confirmEditProject();
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'id', Direction.ASCENDANT);
    this.projectApiService.getTransactionsByProjectId(pageRequest, this.projectId$.getValue()).subscribe((transactions) => {
      this.transactions$.next(transactions);
    });
    this.dialogRefToClose = this.dialog.open(dialogToOpen);
    return lastValueFrom(this.dialogRefToClose.afterClosed());
  };

  private readonly launchAttachmentsUpdate = (): Promise<boolean> => {
    const dialogToOpen = this.confirmEditAttachments();
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
    const projectToUpdate: Omit<Project, 'archivalProfile'> = {
      ...this.model(),
      id: this.project().id,
      createdOn: this.project().createdOn,
      status: this.project().status,
      name: this.form.messageIdentifier().value(),
      archivingSystemId: this.project().archivingSystemId,
      archivingSystemTenant: this.project().archivingSystemTenant,
      connectedToArchivingSystem: this.project().connectedToArchivingSystem,
      // Fixed flow: the position; rules flow: the optional default attachment position
      unitUp: isRulesFlow ? (this.defaultAttachmentEnabled ? this.resolveFixedUnitUp() : null) : this.resolveFixedUnitUp(),
      unitUps: isRulesFlow
        ? Array.from(this.form.unitUps)
            // Only keep fully filled rules (incomplete ones are allowed when a default attachment is set)
            .filter((ruleParamField) => ruleParamField().valid() && ruleParamField().value().ontologyList)
            .map((ruleParamField) => {
              const ruleParam = ruleParamField().value();
              return {
                metadataKey: ruleParam.ontologyList.ApiField,
                metadataValue: ruleParam.metadataValue as string,
                unitUp:
                  this.connectedToLocalEasWithCurrentTenant && ruleParam.attachmentMode === AttachmentMode.TREE
                    ? (ruleParam.unitUp as AttachmentNodes).included[0]
                    : (ruleParam.unitUp as string),
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

    const updateProjectOperation$ = updateProjectFunction(projectToUpdate as Project);
    const previousProject = this.project();
    try {
      const project = await lastValueFrom(updateProjectOperation$);
      this.dialogRefToClose.close(true);
      this.showNormalPanel();
      if (this.projectId() === project.id) {
        this.project.set(project);
      } else {
        this.projectId$.next(this.projectId());
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
      if (this.projectId() === previousProject.id) {
        this.project.set(previousProject);
      } else {
        this.projectId$.next(this.projectId());
      }
    } finally {
      this.showNormalPanel();
    }
  }

  private async onCancel() {
    this.showNormalPanel();
    if (this.projectId() !== this.project().id) {
      this.projectId$.next(this.projectId());
    }
    this.dialogRefToClose?.close(true);
  }

  openCancelDialog = async (): Promise<void> => {
    if (!this.isModified()) {
      await this.onCancel();
      return;
    }

    const result = await firstValueFrom(this.dialog.open(this.cancelDialog(), this.dialogConfig).afterClosed());
    if (result === true) {
      await this.update();
    } else if (result === false) {
      await this.onCancel();
    } else {
      // Back to form in edition mode
    }
  };

  getUnitName(unitId: string): string {
    const foundNode = this.units()?.find((unit) => unit['#id'] === unitId);
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
    return this.#editMode && (!tab || this.tabs()[this.tabGroup().selectedIndex] === tab);
  }

  protected readonly LOCAL_ARCHIVING_SYSTEM_ID = LOCAL_ARCHIVING_SYSTEM_ID;
  protected readonly TENANT_SEPARATOR = TENANT_SEPARATOR;

  async handleJsltFile(files: File[]) {
    const jsltFile = files?.length ? files[0] : undefined;
    const content: string = jsltFile ? await readFileContent(jsltFile) : null;
    this.form.transformationRules().markAsDirty();
    this.form.transformationRules().value.set(content);
  }
}
