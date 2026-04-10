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
import { HttpErrorResponse } from '@angular/common/http';
import {
  AfterContentChecked,
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { merge, Observable, Subject, Subscription } from 'rxjs';
import { debounceTime, filter, map, tap } from 'rxjs/operators';
import {
  ACCESS_RULE,
  AccessContract,
  AccessContractService,
  AlertDialogComponent,
  ALL_DESCENDANTS_FACET,
  APPRAISAL_RULE,
  ArchiveSearchResultFacets,
  ConfigService,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaSearchCriteria,
  CriteriaValue,
  Direction,
  DISSEMINATION_RULE,
  FilingHoldingSchemeNode,
  Logger,
  MANAGEMENT_RULE_SHARED_DATA_SERVICE,
  NODES,
  ORIGIN_WAITING_RECALCULATE,
  ORPHANS_NODE_ID,
  PagedResult,
  QueryParamsService,
  ReclassificationDialogComponent,
  REUSE_RULE,
  Rule,
  RuleService,
  SearchCriteriaAddAction,
  SearchCriteriaCategory,
  SearchCriteriaEltDto,
  SearchCriteriaEltements,
  SearchCriteriaHistory,
  SearchCriteriaMgtRuleEnum,
  SearchCriteriaRemoveAction,
  SearchCriteriaService,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum,
  SecurityService,
  STORAGE_RULE,
  TermsFacet,
  toManagementRuleType,
  Unit,
  UnitType,
  VALID_COMPUTED_INHERITED_RULES_FACET,
  VitamuiRoles,
  WAITING_RECALCULATE,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../../core/management-rules-shared-data.service';
import { ArchiveService } from '../archive.service';
import { ArchiveFacetsService } from '../common-services/archive-facets.service';
import { ArchiveSearchHelperService } from '../common-services/archive-search-helper.service';
import { ArchiveUnitDipService } from '../common-services/archive-unit-dip.service';
import { ArchiveUnitEliminationService } from '../common-services/archive-unit-elimination.service';
import { ComputeInheritedRulesService } from '../common-services/compute-inherited-rules.service';
import { UpdateUnitManagementRuleService } from '../common-services/update-unit-management-rule.service';
import { ActionsRules } from '../models/ruleAction.interface';
import { SearchCriteriaSaverComponent } from './search-criteria-saver/search-criteria-saver.component';
import { TransferAcknowledgmentComponent } from './transfer-acknowledgment/transfer-acknowledgment.component';
import { PuaUpdateDialogComponent, PuaUpdateDialogComponentData } from './pua-update-dialog/pua-update-dialog.component';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { ReassignmentDialogService } from './additional-actions-search/originating-agency-reassignment-dialog/reassignment-dialog.service';
import { ReassignmentMode } from '../models/reassign-request.interface';

const PAGE_SIZE = 10;
const FILTER_DEBOUNCE_TIME_MS = 400;
const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';

@Component({
  selector: 'app-archive-search',
  templateUrl: './archive-search.component.html',
  styleUrls: ['./archive-search.component.scss'],
  standalone: false,
  providers: [
    {
      provide: MANAGEMENT_RULE_SHARED_DATA_SERVICE,
      useExisting: ArchiveSharedDataService,
    },
  ],
})
export class ArchiveSearchComponent implements OnInit, OnChanges, OnDestroy, AfterContentChecked, AfterViewInit {
  readonly UnitType = UnitType;
  readonly ReassignmentMode = ReassignmentMode;

  DEFAULT_RESULT_THRESHOLD = 10_000;
  DEFAULT_ELIMINATION_THRESHOLD = 10_000;
  DEFAULT_ELIMINATION_ANALYSIS_THRESHOLD = 100_000;
  DEFAULT_DIP_EXPORT_THRESHOLD = 100_000;
  DEFAULT_TRANSFER_THRESHOLD = 100_000;
  DEFAULT_UPDATE_MGT_RULES_THRESHOLD = 100_000;
  RECLASSIFICATION_THRESHOLD = 10_000;
  DEFAULT_PUA_UPDATE_THRESHOLD = 100_000;
  DEFAULT_ORIGINATING_AGENCY_REASSIGNMENT_THRESHOLD = 100_000;

  search$: Observable<number>;

  direction = Direction.ASCENDANT;
  accessContractId: string;
  accessContractAllowUpdating: boolean;
  accessContractUpdatingRestrictedDesc: boolean;
  @Output() archiveUnitClick = new EventEmitter<any>();

  tenantIdentifier: number;
  searchCriterias: Map<string, CriteriaSearchCriteria>;
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly orderChange = new Subject<void>();

  orderBy = 'Title';
  isIndeterminate: boolean;
  isAllChecked: boolean;
  hasResults = false;

  hasReassignmentRole = false;
  hasDipExportRole = false;
  hasTransferRequestRole = false;
  hasUpdateManagementRuleRole = false;
  hasEliminationAnalysisOrActionRole = false;
  hasComputedInheritedRulesRole = false;
  hasReclassificationRole = false;
  waitingToGetFixedCount = false;
  showDuaEndDate = false;
  pending = false;
  pendingComputeFacets = false;
  submitted = false;
  pendingGetFixedCount = false;
  submitedGetFixedCount = false;
  included = false;
  canLoadMore = false;
  showCriteriaPanel = true;
  defaultFacetTabIndex = 1;
  currentPage = 0;
  pageNumbers = 0;
  totalResults = 0;
  selectedItemCount = 0;
  selectedHoldingUnitItemCount = 0;
  itemNotSelected = 0;
  numberOfHoldingUnitTypeOnComputedRules = 0;
  additionalSearchCriteriaCategoryIndex = 0;
  nbQueryCriteria = 0;

  listOfUAIdToInclude: CriteriaValue[] = [];
  listOfUAIdToExclude: CriteriaValue[] = [];
  nodeArray: FilingHoldingSchemeNode[] = [];
  archiveUnits: Unit[];
  searchCriteriaHistory: SearchCriteriaHistory[] = [];
  criteriaSearchList: SearchCriteriaEltDto[] = [];
  listOfUACriteriaSearch: SearchCriteriaEltDto[] = [];
  searchCriteriaKeys: string[];
  additionalSearchCriteriaCategories: SearchCriteriaCategory[];

  subscriptions: Subscription = new Subscription();
  showConfirmBigNumberOfResultsSuscription: Subscription;
  transferAcknowledgmentDialogSub: Subscription;
  actionsWithThresholdReachedAlerteMessageDialogSubscription: Subscription;

  rulesFacetsCanBeComputed = false;
  rulesFacetsComputed = false;
  showingFacets = false;

  archiveUnitAllunitup: string[];
  hasAccessContractManagementPermissionsMessage = '';
  bulkOperationsThreshold = -1;
  hasTransferAcknowledgmentRole = false;

  selectedArchive$: Observable<Unit>;
  rulesToExport$: Observable<Rule[]>;

  displayedColumns = ['checkbox', 'type', 'name_description', 'start_date', 'end_date', 'originating_agency'];
  criteriaCategoriesList = [STORAGE_RULE, APPRAISAL_RULE, ACCESS_RULE, DISSEMINATION_RULE, REUSE_RULE];

  @ViewChild('confirmSecondActionBigNumberOfResultsActionDialog', { static: true })
  confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchComponent>;
  @ViewChild('reclassificationAlerteMessageDialog', { static: true })
  reclassificationAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>;
  @ViewChild('launchComputeInheritedRuleAlerteMessageDialog', { static: true })
  launchComputeInheritedRuleAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>;
  @ViewChild('launchSelectionContainsHoldingUnitAlertMessageDialog', { static: true })
  launchSelectionContainsHoldingUnitAlertMessageDialog: TemplateRef<ArchiveSearchComponent>;
  archiveSearchResultFacets: ArchiveSearchResultFacets = new ArchiveSearchResultFacets();
  @ViewChild('confirmImportantAllowedBulkOperationsDialog', { static: true })
  confirmImportantAllowedBulkOperationsDialog: TemplateRef<ArchiveSearchComponent>;
  @ViewChild('actionsWithThresholdReachedAlerteMessageDialog', { static: true })
  actionsWithThresholdReachedAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>;

  constructor(
    public archiveService: ArchiveService,
    private archiveFacetsService: ArchiveFacetsService,
    private translateService: TranslateService,
    private route: ActivatedRoute,
    private archiveSharedDataService: ArchiveSharedDataService,
    public dialog: MatDialog,
    private router: Router,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private archiveHelperService: ArchiveSearchHelperService,
    private logger: Logger,
    private updateUnitManagementRuleService: UpdateUnitManagementRuleService,
    private archiveUnitEliminationService: ArchiveUnitEliminationService,
    private computeInheritedRulesService: ComputeInheritedRulesService,
    private archiveUnitDipService: ArchiveUnitDipService,
    private accessContractService: AccessContractService,
    private cdr: ChangeDetectorRef,
    private queryParamsService: QueryParamsService,
    private searchCriteriaService: SearchCriteriaService,
    private ruleService: RuleService,
    private reassignmentDialogService: ReassignmentDialogService,
    protected configService: ConfigService,
    private securityService: SecurityService,
  ) {
    this.subscriptions.add(
      this.managementRulesSharedDataService.getBulkOperationsThreshold().subscribe((bulkOperationsThreshold) => {
        this.bulkOperationsThreshold = bulkOperationsThreshold;
      }),
    );

    this.subscriptions.add(
      this.archiveSharedDataService.getNodes().subscribe((node) => {
        if (!node) {
          return;
        }
        if (!node.checked) {
          node.count = null;
          if (node.id === ORPHANS_NODE_ID) {
            this.removeCriteria(ORPHANS_NODE_ID, { id: 'position', value: ORPHANS_NODE_ID }, true);
          } else if (node.isVirtual) {
            this.removeCriteria(
              'VIRTUAL',
              {
                id: 'VIRTUAL',
                value: `/${node.virtualPath}`,
                virtualNodeRealParentId: node.realParentId,
                virtualNodeRealParentTitle: node.realParentTitle,
              },
              true,
            );
          } else {
            this.removeCriteria('NODE', { id: NODES, value: node.id }, true);
          }
          return;
        }
        if (node.id === ORPHANS_NODE_ID) {
          this.addCriteria(
            ORPHANS_NODE_ID,
            { id: 'position', value: ORPHANS_NODE_ID },
            node.title,
            true,
            CriteriaOperator.MISSING,
            SearchCriteriaTypeEnum.FIELDS,
            false,
            CriteriaDataType.STRING,
            true,
          );
        } else {
          if (node.isVirtual) {
            this.addCriteria(
              'VIRTUAL',
              {
                id: 'VIRTUAL',
                value: `/${node.virtualPath}`,
                virtualNodeRealParentId: node.realParentId,
                virtualNodeRealParentTitle: node.realParentTitle,
              },
              `/${node.virtualPath}`,
              true,
              CriteriaOperator.EQ,
              SearchCriteriaTypeEnum.FIELDS,
              false,
              CriteriaDataType.STRING,
              true,
            );
          } else {
            this.addCriteria(
              'NODE',
              { id: NODES, value: node.id },
              node.title,
              true,
              CriteriaOperator.EQ,
              SearchCriteriaTypeEnum.NODES,
              false,
              CriteriaDataType.STRING,
              true,
            );
          }
        }
      }),
    );

    this.subscriptions.add(
      this.archiveSharedDataService.receiveSimpleSearchCriteriaSubject().subscribe((criteria) => this.searchCriteriaAddAction(criteria)),
    );

    this.archiveSharedDataService
      .receiveRemoveFromChildSearchCriteriaSubject()
      .subscribe((criteria) => this.searchCriteriaRemoveAction(criteria));

    this.archiveSharedDataService.receiveAppraisalSearchCriteriaSubject().subscribe((criteria) => this.searchCriteriaAddAction(criteria));

    this.archiveSharedDataService.receiveAccessSearchCriteriaSubject().subscribe((criteria) => this.searchCriteriaAddAction(criteria));

    this.selectedArchive$ = archiveSharedDataService.selectedUnit$;
  }

  selectedCategoryChange(selectedCategoryIndex: number) {
    this.additionalSearchCriteriaCategoryIndex = selectedCategoryIndex;
  }

  private searchCriteriaAddAction(criteria: SearchCriteriaAddAction): void {
    if (!criteria) {
      return;
    }
    this.addCriteria(
      criteria.keyElt,
      criteria.valueElt,
      criteria.labelElt,
      criteria.keyTranslated,
      criteria.operator,
      criteria.category,
      criteria.valueTranslated,
      criteria.dataType,
      false,
    );
  }

  private searchCriteriaRemoveAction(criteria: SearchCriteriaRemoveAction) {
    if (!criteria) {
      return;
    }
    if (criteria.valueElt) {
      this.removeCriteria(criteria.keyElt, criteria.valueElt, false);
    } else {
      this.removeCriteriaAllValues(criteria.keyElt, false);
    }
  }

  addCriteriaCategory(categoryName: string) {
    this.archiveSharedDataService.emitRuleCategory(categoryName);
    const indexOfCategory = this.additionalSearchCriteriaCategories.findIndex((element) => element.name === categoryName);
    if (indexOfCategory === -1) {
      this.additionalSearchCriteriaCategories.push({
        name: categoryName,
        index: this.additionalSearchCriteriaCategories.length + 1,
      });
      this.additionalSearchCriteriaCategories.forEach((category, index) => {
        category.index = index + 1;
      });
      this.additionalSearchCriteriaCategoryIndex = this.additionalSearchCriteriaCategories.length;
    }
  }

  sendRuleCategorySelected(categoryName: string) {
    this.archiveSharedDataService.emitRuleCategory(categoryName);
  }

  isCategoryAdded(categoryName: string): boolean {
    const indexOfCategory = this.additionalSearchCriteriaCategories.findIndex((element) => element.name === categoryName);
    return indexOfCategory !== -1;
  }

  showHideDuaEndDate(status: boolean) {
    this.showDuaEndDate = status;
  }

  removeCriteriaCategory(categoryName: string) {
    this.additionalSearchCriteriaCategories.forEach((element, index) => {
      if (element.name === categoryName) {
        this.additionalSearchCriteriaCategories.splice(index, 1);
        if (index === this.additionalSearchCriteriaCategoryIndex - 1) {
          this.additionalSearchCriteriaCategoryIndex = 0;
        } else {
          if (this.additionalSearchCriteriaCategoryIndex > 0) {
            this.additionalSearchCriteriaCategoryIndex = this.additionalSearchCriteriaCategoryIndex - 1;
          }
        }
      }
    });
    this.additionalSearchCriteriaCategories.forEach((category, index) => {
      category.index = index + 1;
    });
    this.removeCriteriaByCategory(categoryName);
  }

  ngOnInit() {
    this.accessContractService.currentAccessContract$.subscribe((ac: AccessContract) => {
      this.accessContractAllowUpdating = ac.writingPermission;
      this.accessContractUpdatingRestrictedDesc = ac.writingRestrictedDesc;
    });
    this.additionalSearchCriteriaCategoryIndex = 0;
    this.additionalSearchCriteriaCategories = [];
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = +params.tenantIdentifier;
      this.hasRole('ROLE_ORIGINATING_AGENCY_REASSIGNMENT');
    });
    this.hasAccessContractManagementPermissionsMessage = this.translateService.instant('UNIT_UPDATE.NO_PERMISSION');
    this.searchCriterias = new Map();
    this.searchCriteriaKeys = [];

    if (!this.route.snapshot.queryParamMap.keys.length) {
      this.queryParamsService
        .builder()
        .addQueryParam('archiveUnitType', 'ARCHIVE_UNIT_WITH_OBJECTS')
        .addQueryParam('archiveUnitType', 'ARCHIVE_UNIT_WITHOUT_OBJECTS')
        .navigate({ replaceUrl: true });
    }

    const searchCriteriaChange = merge(this.orderChange, this.filterChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));
    searchCriteriaChange.subscribe(() => {
      this.submit(true);
    });

    this.checkUserHasRole(VitamuiRoles.ROLE_EXPORT_DIP, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_TRANSFER_REQUEST, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_ELIMINATION, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_ARCHIVE_SEARCH_UPDATE_ARCHIVE_UNIT, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_COMPUTED_INHERITED_RULES, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_RECLASSIFICATION, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_TRANSFER_ACKNOWLEDGMENT, +this.tenantIdentifier);
    const ruleActions: ActionsRules[] = [];
    this.managementRulesSharedDataService.emitRuleActions(ruleActions);
    this.managementRulesSharedDataService.emitManagementRules([]);

    this.rulesToExport$ = this.ruleService
      .getAllForTenant(this.tenantIdentifier.toString())
      .pipe(map((rules) => rules.sort((a, b) => a.ruleId.localeCompare(b.ruleId))));
  }

  ngAfterViewInit() {
    // Trigger the search if we land on the page with query params, but only after searchCriteriaService is ready (i.e.: schema has been retrieved) in order to trigger search only after criteria have been set from the URL query params
    if (this.route.snapshot.queryParamMap.keys.length) this.searchCriteriaService.ready().then(() => setTimeout(() => this.submit(true)));
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.accessContract) {
      this.archiveSharedDataService.emitToggle(true);
    }
  }

  ngAfterContentChecked(): void {
    this.cdr.detectChanges();
  }

  toManagementRuleType = toManagementRuleType;

  toUpdateOn(category: SearchCriteriaCategory) {
    const { name } = category;

    if (name === ACCESS_RULE) return this.archiveSharedDataService.accessFromMainSearchCriteriaObservable;
    if (name === APPRAISAL_RULE) return this.archiveSharedDataService.appraisalFromMainSearchCriteriaObservable;
    if (name === DISSEMINATION_RULE) return this.archiveSharedDataService.disseminationFromMainSearchCriteriaObservable;
    if (name === REUSE_RULE) return this.archiveSharedDataService.reuseFromMainSearchCriteriaObservable;
    if (name === STORAGE_RULE) return this.archiveSharedDataService.storageFromMainSearchCriteriaObservable;

    throw new Error(`Unknown management rule category ${name}`);
  }

  showHidePanel(show: boolean) {
    this.showCriteriaPanel = show;
  }

  showStoredSearchCriteria(event: SearchCriteriaHistory) {
    if (this.searchCriterias.size > 0) {
      this.searchCriterias = new Map();
      this.searchCriteriaKeys = [];
      this.included = false;
    }
    this.clearCriteria();
    setTimeout(() => this.applySearchCriteriaHistory(event));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  removeCriteriaEvent(criteriaToRemove: any) {
    if (criteriaToRemove.valueElt) {
      this.removeCriteria(criteriaToRemove.keyElt, criteriaToRemove.valueElt, true);
    } else {
      this.removeCriteriaAllValues(criteriaToRemove.keyElt, true);
    }
  }

  removeCriteria(keyElt: string, valueElt: CriteriaValue, emit: boolean) {
    this.archiveHelperService.removeCriteria(keyElt, valueElt, emit, this.searchCriteriaKeys, this.searchCriterias, this.nbQueryCriteria);
    if (this.searchCriterias && this.searchCriterias.size === 0) {
      this.submitted = false;
      this.showCriteriaPanel = true;
      this.archiveUnits = [];
      this.archiveSharedDataService.emitNodeTarget(null);
    }
  }

  removeCriteriaAllValues(keyElt: string, emit: boolean) {
    if (this.searchCriterias && this.searchCriterias.size > 0) {
      this.searchCriterias.forEach((val, key) => {
        if (key === keyElt) {
          val.values.forEach((value) => {
            this.removeCriteria(key, value.value, emit);
          });
        }
      });
    }
  }

  removeCriteriaByCategory(category: string) {
    if (this.searchCriterias && this.searchCriterias.size > 0) {
      const builder = this.queryParamsService.builder();
      if (category === SearchCriteriaTypeEnum.APPRAISAL_RULE) {
        this.searchCriterias.forEach((criteriaValues, key) => {
          if (key === ELIMINATION_TECHNICAL_ID) {
            criteriaValues.values.forEach((value) => {
              this.removeCriteria(key, value.value, true);
              builder.removeQueryParam(value.value.id, value.value.value);
              builder.navigate();
            });
          }
        });
      }
      this.searchCriterias.forEach((val, key) => {
        if (SearchCriteriaTypeEnum[val.category] === category || key === WAITING_RECALCULATE) {
          val.values.forEach((value) => {
            this.removeCriteria(key, value.value, true);
            const keyToRemove = key === WAITING_RECALCULATE ? ORIGIN_WAITING_RECALCULATE : value.value.id;
            builder.removeQueryParam(keyToRemove, value.value.value);
            builder.navigate();
          });
        }
      });
    }
  }

  containsWaitingToRecalculateInheritenceRuleCriteria() {
    return this.searchCriterias && this.searchCriterias.has(WAITING_RECALCULATE);
  }

  submit(refreshArchiveUnitsWithoutAttachment?: boolean) {
    this.listOfUAIdToInclude = [];
    this.listOfUAIdToExclude = [];

    this.archiveSharedDataService.emitSelectedUnit(null);
    this.initializeSelectionParams();
    this.archiveHelperService.buildNodesListForQUery(this.searchCriterias, this.criteriaSearchList);
    this.archiveHelperService.buildFieldsCriteriaListForQUery(this.searchCriterias, this.criteriaSearchList);

    // eslint-disable-next-line guard-for-in
    for (const mgtRuleType in SearchCriteriaMgtRuleEnum) {
      this.archiveHelperService.buildManagementRulesCriteriaListForQuery(mgtRuleType, this.searchCriterias, this.criteriaSearchList);
    }
    if (this.hasSearchCriteria()) {
      this.search$ = this.archiveService.getTotalTrackHitsByCriteria(this.criteriaSearchList);
      this.rulesFacetsComputed = false;
      this.rulesFacetsCanBeComputed = this.archiveHelperService.checkIfRulesFacetsCanBeComputed(this.searchCriterias);
      this.callVitamApiService(this.rulesFacetsCanBeComputed);
      this.showingFacets = this.rulesFacetsCanBeComputed;
      if (refreshArchiveUnitsWithoutAttachment) this.existsArchiveUnitWithoutAttachment();
    }
  }

  prepareListOfUACriteriaSearch() {
    return this.archiveHelperService.prepareUAIdList(
      this.criteriaSearchList,
      this.listOfUAIdToInclude,
      this.listOfUAIdToExclude,
      this.isAllChecked,
      this.isIndeterminate,
    );
  }

  getArchiveUnitType(archiveUnit: Unit): UnitType {
    if (archiveUnit) {
      return archiveUnit['#unitType'];
    }
  }

  private launchComputingManagementRulesFacets() {
    this.pendingComputeFacets = true;
    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    let facets: TermsFacet[] = [];
    facets.push(ALL_DESCENDANTS_FACET);
    facets.push(VALID_COMPUTED_INHERITED_RULES_FACET);

    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: 0,
      size: 1,
      sortingCriteria,
      trackTotalHits: this.totalResults >= 10000,
      computeMgtRulesFacets: true,
      facets: facets,
    };

    this.archiveService.searchArchiveUnitsByCriteria(searchCriteria).subscribe(
      (pagedResult: PagedResult) => {
        this.archiveSearchResultFacets = this.archiveFacetsService.extractRulesFacetsResults(pagedResult.facets);

        this.pendingComputeFacets = false;
        this.rulesFacetsComputed = true;
        this.showingFacets = true;
        this.defaultFacetTabIndex = this.archiveHelperService.findDefaultFacetTabIndex(this.searchCriterias);
      },
      (error: HttpErrorResponse) => {
        this.pendingComputeFacets = false;
        this.logger.error('Error message :', error.message);
      },
    );
  }

  private callVitamApiService(includeFacets: boolean) {
    if (includeFacets) {
      this.pendingComputeFacets = true;
      this.showingFacets = false;
    }
    this.pending = true;
    let facets: TermsFacet[] = [];
    facets.push(ALL_DESCENDANTS_FACET);
    if (includeFacets) {
      facets.push(VALID_COMPUTED_INHERITED_RULES_FACET);
    }
    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    const searchCriterias = {
      criteriaList: this.criteriaSearchList,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      sortingCriteria,
      trackTotalHits: false,
      computeMgtRulesFacets: includeFacets,
      facets: facets,
    };
    this.archiveSharedDataService.emitSearchCriterias(searchCriterias);
    this.archiveService.searchArchiveUnitsByCriteria(searchCriterias).subscribe(
      (pagedResult: PagedResult) => {
        if (includeFacets) {
          this.archiveSearchResultFacets = this.archiveFacetsService.extractRulesFacetsResults(pagedResult.facets);
          this.defaultFacetTabIndex = this.archiveHelperService.findDefaultFacetTabIndex(this.searchCriterias);
          this.pendingComputeFacets = false;
          this.rulesFacetsComputed = true;
        }
        if (this.currentPage === 0) {
          this.archiveUnits = pagedResult.results;
          this.archiveSearchResultFacets.nodesFacets = this.archiveFacetsService.extractNodesFacetsResults(pagedResult.facets);
          this.archiveSharedDataService.emitFacets(this.archiveSearchResultFacets.nodesFacets);
          this.hasResults = true;
          this.totalResults = pagedResult.totalResults;
          this.archiveSharedDataService.emitTotalResults(this.totalResults);
        } else if (pagedResult.results) {
          this.hasResults = true;
          this.archiveUnits = [...this.archiveUnits, ...pagedResult.results];
        }
        this.pageNumbers = pagedResult.pageNumbers;
        this.waitingToGetFixedCount = this.totalResults === this.DEFAULT_RESULT_THRESHOLD;
        if (this.isAllChecked) {
          this.selectedItemCount = this.totalResults - this.itemNotSelected;
        }
        this.canLoadMore = this.currentPage < this.pageNumbers - 1;
        this.archiveHelperService.updateCriteriaStatus(
          this.searchCriterias,
          SearchCriteriaStatusEnum.IN_PROGRESS,
          SearchCriteriaStatusEnum.INCLUDED,
        );
        this.pending = false;
        this.included = true;
      },
      (error: HttpErrorResponse) => {
        this.canLoadMore = false;
        this.pending = false;
        if (includeFacets) {
          this.pendingComputeFacets = false;
          this.archiveSharedDataService.emitFacets([]);
        }
        this.logger.error('Error message :', error.message);

        this.archiveHelperService.updateCriteriaStatus(
          this.searchCriterias,
          SearchCriteriaStatusEnum.IN_PROGRESS,
          SearchCriteriaStatusEnum.NOT_INCLUDED,
        );
      },
    );
  }

  onArchiveUnitCountChange(resultCount: number) {
    this.totalResults = resultCount;
    this.archiveSharedDataService.emitTotalResults(resultCount);
  }

  mapSearchCriteriaHistory() {
    let searchCriteriaHistoryObject: SearchCriteriaHistory;
    const criteriaListObject: SearchCriteriaEltements[] = [];
    this.searchCriterias.forEach((criteria: CriteriaSearchCriteria) => {
      const strValues: CriteriaValue[] = [];
      criteria.values.forEach((elt) => {
        strValues.push(elt.value);
      });
      criteriaListObject.push({
        criteria: criteria.key,
        values: strValues,
        category: SearchCriteriaTypeEnum[criteria.category],
        operator: criteria.operator,
        keyTranslated: criteria.keyTranslated,
        valueTranslated: criteria.valueTranslated,
        dataType: criteria.dataType,
      });
    });
    searchCriteriaHistoryObject = {
      id: null,
      name: '',
      savingDate: new Date().toISOString(),
      searchCriteriaList: criteriaListObject,
    };
    this.openCriteriaPopup(searchCriteriaHistoryObject);
  }

  openCriteriaPopup(searchCriteriaHistory$: SearchCriteriaHistory) {
    const dialogConfig: MatDialogConfig = {
      panelClass: ['p-0', 'search-criteria-dialog'],
      disableClose: false,
      data: {
        searchCriteriaHistory: searchCriteriaHistory$,
        originalSearchCriteria: this.searchCriterias,
        nbCriterias: this.archiveSharedDataService.nbFilters(searchCriteriaHistory$),
      },
    };

    const dialogRef = this.dialog.open(SearchCriteriaSaverComponent, dialogConfig);
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
      }
    });
  }

  // TODO: it may add multiple subscription for each clear criteria
  subscribeResetNodesOnFilingHoldingNodesChanges() {
    this.subscriptions.add(
      this.archiveSharedDataService.getFilingHoldingNodes().subscribe((nodes) => {
        this.nodeArray = nodes;
      }),
    );
  }

  recursiveCheck(nodes: FilingHoldingSchemeNode[], show: boolean) {
    if (nodes.length === 0) {
      return;
    }
    for (const node of nodes) {
      node.hidden = false;
      node.checked = show;
      node.count = null;
      this.recursiveCheck(node.children, show);
    }
  }

  checkAllNodes(show: boolean) {
    this.recursiveCheck(this.nodeArray, show);
  }

  private applySearchCriteriaHistory(storedSearchCriteriaHistory: SearchCriteriaHistory) {
    this.subscribeResetNodesOnFilingHoldingNodesChanges();
    this.recursiveCheck(this.nodeArray, false);

    // Collect all criteria to update URL at once
    const criteriaToAddToUrl: any[] = [];

    storedSearchCriteriaHistory.searchCriteriaList.forEach((criteria: SearchCriteriaEltements) => {
      this.fillTreeNodeAsSearchCriteriaHistory(criteria);

      const category = criteria.category as SearchCriteriaTypeEnum;
      const isRuleCategory = Object.keys(SearchCriteriaTypeEnum)
        .filter((key) => key.includes('RULE'))
        .includes(category);

      if (isRuleCategory) {
        this.addCriteriaCategory(category);
      }

      criteria.values.forEach((value) => {
        this.addCriteria(
          criteria.criteria,
          value,
          value.value,
          criteria.keyTranslated,
          criteria.operator,
          category,
          criteria.valueTranslated,
          criteria.dataType,
          false,
        );

        criteriaToAddToUrl.push({
          keyElt: criteria.criteria,
          valueElt: value,
          labelElt: value.value,
          keyTranslated: criteria.keyTranslated,
          operator: criteria.operator,
          category,
          valueTranslated: criteria.valueTranslated,
          dataType: criteria.dataType,
        });
      });
    });

    // Update URL with all restored criteria at once
    if (criteriaToAddToUrl.length > 0) {
      this.archiveSharedDataService.addSimpleSearchCriteriaSubjects(criteriaToAddToUrl);
    }
  }

  fillTreeNodeAsSearchCriteriaHistory(searchCriteriaList: SearchCriteriaEltements) {
    if (searchCriteriaList && searchCriteriaList.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.NODES]) {
      searchCriteriaList.values.forEach((nodeId) => {
        this.archiveHelperService.fillNodeTitle(
          this.nodeArray,
          nodeId.value,
          this.searchCriterias,
          this.searchCriteriaKeys,
          this.nbQueryCriteria,
        );
      });
      this.nodeArray = null;
      this.archiveSharedDataService.emitToggle(true);
    }
  }

  loadMore() {
    if (this.pending) {
      return;
    }
    this.canLoadMore = this.currentPage < this.pageNumbers - 1;
    if (!this.canLoadMore) {
      return;
    }
    this.submitted = true;
    this.currentPage = this.currentPage + 1;
    if (!this.hasSearchCriteria()) {
      return;
    }
    this.callVitamApiService(false);
  }

  private hasSearchCriteria() {
    return this.criteriaSearchList && this.criteriaSearchList.length > 0;
  }

  async launchFacetsComputing() {
    if (this.pendingComputeFacets || !this.hasSearchCriteria()) {
      return;
    }

    if (this.waitingToGetFixedCount) {
      if (this.hasSearchCriteria()) {
        this.pendingGetFixedCount = true;
        this.submitedGetFixedCount = true;
        const exactCountResults: number = await this.archiveService.getTotalTrackHitsByCriteria(this.criteriaSearchList).toPromise();
        if (exactCountResults !== -1) {
          this.totalResults = exactCountResults;
          this.waitingToGetFixedCount = false;
          this.managementRulesSharedDataService.emitHasExactCount(true);
          this.launchComputingManagementRulesFacets();
        }
        this.pendingGetFixedCount = false;
      }
    } else {
      this.managementRulesSharedDataService.emitHasExactCount(false);
      this.launchComputingManagementRulesFacets();
    }
  }

  showHideFacets(show: boolean) {
    if (show === true) {
      if (this.rulesFacetsComputed === true) {
        this.showingFacets = true;
      } else {
        this.launchFacetsComputing();
      }
    } else {
      this.showingFacets = false;
    }
  }

  hiddenTreeBlock(hidden: boolean): void {
    this.archiveSharedDataService.emitToggle(!hidden);
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
    this.showConfirmBigNumberOfResultsSuscription?.unsubscribe();
    this.transferAcknowledgmentDialogSub?.unsubscribe();
    this.actionsWithThresholdReachedAlerteMessageDialogSubscription?.unsubscribe();
  }

  exportArchiveUnitsToCsvFile() {
    if (this.hasSearchCriteria()) {
      this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
      const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
      const searchCriteria = {
        criteriaList: this.listOfUACriteriaSearch,
        pageNumber: this.currentPage,
        size: PAGE_SIZE,
        sortingCriteria,
        language: this.translateService.currentLang,
      };
      this.archiveService.exportCsvSearchArchiveUnitsByCriteria(searchCriteria);
    }
  }

  clearCriteria() {
    const searchCriteriaKeysCloned = Object.assign([], this.searchCriteriaKeys);
    searchCriteriaKeysCloned.forEach((criteriaKey) => {
      if (this.searchCriterias.has(criteriaKey)) {
        const criteria = this.searchCriterias.get(criteriaKey);
        const values = criteria.values;
        values.forEach((value) => {
          this.removeCriteria(criteriaKey, value.value, true);
        });
      }
    });
    this.criteriaCategoriesList.forEach((category) => {
      this.removeCriteriaCategory(category);
    });
    this.searchCriterias = new Map();
    this.searchCriteriaKeys = [];
    this.included = false;
    this.nbQueryCriteria = 0;
    this.pageNumbers = 0;
    this.totalResults = 0;
    this.selectedItemCount = 0;
    this.isAllChecked = false;
    this.isIndeterminate = false;
    this.itemNotSelected = 0;
    this.canLoadMore = false;
    this.subscribeResetNodesOnFilingHoldingNodesChanges();
    this.archiveSharedDataService.emitFilingHoldingNodes(this.nodeArray);
    this.recursiveCheck(this.nodeArray, false);

    this.queryParamsService.setQueryParams({}, {});
  }

  checkParentBoxChange(event: MatCheckboxChange) {
    const { checked } = event;

    this.isAllChecked = checked;
    this.selectedItemCount = checked ? this.totalResults : 0;
    this.selectedHoldingUnitItemCount = 0;
    if (!checked) {
      this.isIndeterminate = false;
    } else {
      this.itemNotSelected = 0;
    }
    this.listOfUAIdToInclude = [];
    this.listOfUAIdToExclude = [];
    this.listOfUACriteriaSearch = [];
  }

  checkChildrenBoxChange(archiveUnit: Unit, event: MatCheckboxChange) {
    const id = archiveUnit['#id'];
    const unitType: UnitType = archiveUnit['#unitType'];
    const action = event.checked;

    if (this.isAllChecked && !action) {
      this.listOfUACriteriaSearch = [];
      this.isIndeterminate = true;
      this.listOfUAIdToExclude.push({ value: id, id });
      this.listOfUAIdToInclude = [];
      if (this.selectedItemCount > 0) {
        this.selectedItemCount--;
        this.itemNotSelected++;
      }
      if (this.selectedHoldingUnitItemCount > 0 && UnitType.HOLDING_UNIT === unitType) {
        this.selectedHoldingUnitItemCount--;
      }
    } else {
      if (action) {
        if (UnitType.HOLDING_UNIT === unitType) {
          this.selectedHoldingUnitItemCount++;
        }
        this.listOfUACriteriaSearch = [];
        this.selectedItemCount++;
        if (this.selectedItemCount === this.totalResults) {
          this.isIndeterminate = false;
        }
        if (this.isAllChecked) {
          this.listOfUAIdToExclude = this.listOfUAIdToExclude.filter((element) => element.id !== id);
          this.itemNotSelected--;
        } else {
          this.listOfUAIdToInclude.push({ value: id, id });
          this.listOfUAIdToExclude.splice(0, this.listOfUAIdToExclude.length);
          this.itemNotSelected = 0;
        }
      } else {
        this.listOfUAIdToInclude = this.listOfUAIdToInclude.filter((element) => element.id !== id);
        if (this.selectedItemCount > 0) {
          this.selectedItemCount--;
          this.itemNotSelected++;
        }
      }
    }
  }

  private initializeSelectionParams() {
    this.pending = true;
    this.submitted = true;
    this.showCriteriaPanel = false;
    this.currentPage = 0;
    this.archiveUnits = [];
    this.criteriaSearchList = [];
    this.selectedItemCount = 0;
    this.selectedHoldingUnitItemCount = 0;
    this.isIndeterminate = false;
    this.itemNotSelected = 0;
    this.isAllChecked = false;
  }

  checkUserHasRole(role: VitamuiRoles, tenantIdentifier: number) {
    this.archiveService.hasArchiveSearchRole(role, tenantIdentifier).subscribe((result) => {
      switch (role) {
        case VitamuiRoles.ROLE_EXPORT_DIP:
          this.hasDipExportRole = result;
          break;
        case VitamuiRoles.ROLE_TRANSFER_REQUEST:
          this.hasTransferRequestRole = result;
          break;
        case VitamuiRoles.ROLE_ELIMINATION:
          this.hasEliminationAnalysisOrActionRole = result;
          break;
        case VitamuiRoles.ROLE_ARCHIVE_SEARCH_UPDATE_ARCHIVE_UNIT:
          this.hasUpdateManagementRuleRole = result;
          break;
        case VitamuiRoles.ROLE_COMPUTED_INHERITED_RULES:
          this.hasComputedInheritedRulesRole = result;
          break;
        case VitamuiRoles.ROLE_RECLASSIFICATION:
          this.hasReclassificationRole = result;
          break;
        case VitamuiRoles.ROLE_TRANSFER_ACKNOWLEDGMENT:
          this.hasTransferAcknowledgmentRole = result;
          break;
        default:
          break;
      }
    });
  }

  launchReclassification() {
    this.search$.subscribe((totalHits) => {
      if (
        (this.isAllChecked && totalHits - this.itemNotSelected > this.RECLASSIFICATION_THRESHOLD) ||
        this.selectedItemCount > this.RECLASSIFICATION_THRESHOLD
      ) {
        const dialogToOpen = this.reclassificationAlerteMessageDialog;
        const dialogRef = this.dialog.open(dialogToOpen);
        this.subscriptions.add(
          dialogRef
            .afterClosed()
            .pipe(filter((result) => !!result))
            .subscribe(() => {}),
        );
      } else {
        const archiveUnitGuidSelected = this.isAllChecked
          ? this.archiveUnits
              .map((unit) => unit['#id'])
              .filter((unit) => !this.listOfUAIdToExclude.some((unitToExclude) => unit === unitToExclude.id))
          : this.listOfUAIdToInclude.map((unit) => unit.id);
        let obj = this.archiveUnits
          .filter((archiveUnit) => archiveUnitGuidSelected.includes(archiveUnit['#id']))
          .map((archiveUnit) => archiveUnit['#unitups']);
        this.archiveUnitAllunitup = this.initArchiveUnitAllunitup(obj);
        this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
        const selectedItems = this.isAllChecked ? totalHits - this.itemNotSelected : this.selectedItemCount;
        const reclassificationCriteria = {
          criteriaList: this.listOfUACriteriaSearch,
          pageNumber: 0,
          size: selectedItems,
          language: this.translateService.currentLang,
        };
        const dialogRef = this.dialog.open(ReclassificationDialogComponent, {
          disableClose: false,
          data: {
            appName: 'ARCHIVE',
            itemSelected: selectedItems,
            reclassificationCriteria,
            tenantIdentifier: this.tenantIdentifier,
            archiveUnitGuidSelected: archiveUnitGuidSelected,
            archiveUnitAllunitup: this.archiveUnitAllunitup,
          },
        });
        this.subscriptions.add(
          dialogRef.afterClosed().subscribe((result) => {
            if (result) {
              return;
            }
          }),
        );
      }
    });
  }

  public initArchiveUnitAllunitup(values: string[][]) {
    return [...new Set(values.flat())];
  }

  public shouldReadSelectedItemCount(): boolean {
    return !this.waitingToGetFixedCount || !this.isAllChecked;
  }

  async prepareToLaunchVitamAction() {
    if (!this.shouldReadSelectedItemCount()) {
      if (this.hasSearchCriteria()) {
        this.pendingGetFixedCount = true;
        this.submitedGetFixedCount = true;
        const exactCountResults: number = await this.archiveService.getTotalTrackHitsByCriteria(this.criteriaSearchList).toPromise();
        if (exactCountResults !== -1) {
          this.totalResults = exactCountResults;
          if (this.isAllChecked) {
            this.selectedItemCount = this.totalResults - this.itemNotSelected;
          }
          this.waitingToGetFixedCount = false;
          this.managementRulesSharedDataService.emitHasExactCount(true);
        }
        this.pendingGetFixedCount = false;
      }
    } else {
      this.managementRulesSharedDataService.emitHasExactCount(false);
    }
    this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
  }

  async launchOriginatingAgencyReassignmentModal(reassignmentMode: ReassignmentMode) {
    if (reassignmentMode === ReassignmentMode.BY_ID) {
      await this.launchBulkOperationWorkflow(
        () =>
          this.reassignmentDialogService.launchReassignmentModal(
            this.prepareListOfUACriteriaSearch(),
            this.selectedItemCount,
            this.tenantIdentifier,
          ),
        this.DEFAULT_ORIGINATING_AGENCY_REASSIGNMENT_THRESHOLD,
      );
    } else {
      this.reassignmentDialogService.launchEntryOperationReassignmentModal(this.tenantIdentifier);
    }
  }

  async launchComputedInheritedRulesModal() {
    await this.prepareToLaunchVitamAction();
    this.computeInheritedRulesService.launchComputedInheritedRulesModal(
      this.listOfUACriteriaSearch,
      this.numberOfHoldingUnitTypeOnComputedRules,
      this.tenantIdentifier,
      this.currentPage,
      this.launchComputeInheritedRuleAlerteMessageDialog,
      this.confirmSecondActionBigNumberOfResultsActionDialog,
    );
  }

  private hasRole(role: string): void {
    const appId = 'ARCHIVE_SEARCH_MANAGEMENT_APP';
    this.securityService.hasRole$(appId, role, this.tenantIdentifier).subscribe((result) => {
      return (this.hasReassignmentRole = result);
    });
  }

  private bulkOperationWarningWorkflow(operation: () => void): void {
    const dialogConfirmActionWithImportantAllowedCount = this.confirmImportantAllowedBulkOperationsDialog;
    const dialogConfirmActionWithImportantAllowedCountRef = this.dialog.open(dialogConfirmActionWithImportantAllowedCount);

    dialogConfirmActionWithImportantAllowedCountRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(operation);
  }

  private bulkOperationErrorWorkflow(): void {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.data = {
      subhead: 'ARCHIVE_SEARCH.ORIGINATING_AGENCY_REASSIGNMENT.ALERTE_MESSAGES.SUBHEAD',
      title: 'ARCHIVE_SEARCH.OTHER_ACTIONS.DIALOG_MESSAGE.PLATEFORM_THRESHOLD_REACHED_ALERT_MESSAGE',
      icon: 'cancel',
      message: 'ARCHIVE_SEARCH.OTHER_ACTIONS.DIALOG_MESSAGE.PLATEFORM_THRESHOLD_REACHED_ALERT_MESSAGE_2',
      cancelLabel: 'RULES.ALERTE_MESSAGES.BACK_TO_SELECTION',
    };

    this.dialog.open(AlertDialogComponent, dialogConfig);
  }

  private async launchBulkOperationWorkflow(operation: () => void, defaultBulkOperationThreshold: number) {
    await this.prepareToLaunchVitamAction();

    if (!(this.shouldReadSelectedItemCount() && this.selectedItemCount > 0)) {
      return;
    }

    const hasBulkOperationThreshold = this.bulkOperationsThreshold !== -1;
    const isGreaterThanBulkOperationThreshold = this.selectedItemCount > this.bulkOperationsThreshold;
    const isGreaterThanDefaultBulkOperationThreshold = this.selectedItemCount > defaultBulkOperationThreshold;

    if (hasBulkOperationThreshold) {
      if (isGreaterThanBulkOperationThreshold) {
        this.bulkOperationErrorWorkflow();
      } else if (isGreaterThanDefaultBulkOperationThreshold) {
        this.bulkOperationWarningWorkflow(operation);
      } else {
        operation();
      }
    } else if (isGreaterThanDefaultBulkOperationThreshold) {
      this.bulkOperationErrorWorkflow();
    } else {
      operation();
    }
  }

  async launchEliminationAnalysisModal(): Promise<void> {
    this.launchBulkOperationWorkflow(
      () =>
        this.archiveUnitEliminationService.launchEliminationAnalysisModal(
          this.listOfUACriteriaSearch,
          this.shouldReadSelectedItemCount(),
          this.selectedItemCount,
          this.tenantIdentifier,
          this.currentPage,
          this.confirmSecondActionBigNumberOfResultsActionDialog,
          this.showConfirmBigNumberOfResultsSuscription,
        ),
      this.DEFAULT_ELIMINATION_ANALYSIS_THRESHOLD,
    );
  }

  async launchEliminationModal() {
    const listAUHoldingUnit = this.prepareListOfUACriteriaSearch();
    listAUHoldingUnit.push({
      criteria: 'ALL_ARCHIVE_UNIT_TYPES',
      values: [{ value: 'ARCHIVE_UNIT_HOLDING_UNIT', id: 'ARCHIVE_UNIT_HOLDING_UNIT' }],
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
      dataType: CriteriaDataType.STRING,
    });

    this.archiveService
      .getTotalTrackHitsByCriteria(listAUHoldingUnit)
      .pipe(
        tap((value: number) => {
          if (value !== 0) {
            const dialogConfig = new MatDialogConfig();

            dialogConfig.data = {
              title: 'ARCHIVE_SEARCH.ELIMINATION.ALERTE_MESSAGES.ACTION_ALERTE_TITLE',
              icon: 'cancel',
              message: 'RULES.ALERTE_MESSAGES.ACTION_ALERTE_FIRST_MESSAGE',
              cancelLabel: 'RULES.ALERTE_MESSAGES.BACK_TO_SELECTION',
            };

            this.dialog.open(AlertDialogComponent, dialogConfig);
          } else {
            this.launchBulkOperationWorkflow(
              () =>
                this.archiveUnitEliminationService.launchEliminationModal(
                  this.listOfUACriteriaSearch,
                  this.tenantIdentifier,
                  this.currentPage,
                  this.confirmSecondActionBigNumberOfResultsActionDialog,
                  true,
                ),
              this.DEFAULT_ELIMINATION_THRESHOLD,
            );
          }
        }),
      )
      .subscribe();
  }

  async launchDeleteUnitTreeModal() {
    const listAUHoldingUnit = this.prepareListOfUACriteriaSearch();
    listAUHoldingUnit.push({
      criteria: 'ALL_ARCHIVE_UNIT_TYPES',
      values: [{ value: 'ARCHIVE_UNIT_HOLDING_UNIT', id: 'ARCHIVE_UNIT_HOLDING_UNIT' }],
      operator: CriteriaOperator.EQ,
      category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
      dataType: CriteriaDataType.STRING,
    });
    this.archiveService
      .getTotalTrackHitsByCriteria(listAUHoldingUnit)
      .pipe(
        tap((value: number) => {
          if (value === this.selectedItemCount) {
            this.launchBulkOperationWorkflow(
              () =>
                this.archiveUnitEliminationService.launchEliminationModal(
                  this.listOfUACriteriaSearch,
                  this.tenantIdentifier,
                  this.currentPage,
                  this.confirmSecondActionBigNumberOfResultsActionDialog,
                  false,
                ),
              this.DEFAULT_ELIMINATION_THRESHOLD,
            );
          } else {
            const dialogConfig = new MatDialogConfig();
            dialogConfig.data = {
              title: 'ARCHIVE_SEARCH.ELIMINATION.ALERTE_MESSAGES.ACTION_ALERTE_TITLE',
              icon: 'cancel',
              message: 'ARCHIVE_SEARCH.ELIMINATION.ALERTE_MESSAGES.ACTION_ALERTE_FIRST_MESSAGE',
              cancelLabel: 'RULES.ALERTE_MESSAGES.BACK_TO_SELECTION',
            };

            this.dialog.open(AlertDialogComponent, dialogConfig);
          }
        }),
      )
      .subscribe();
  }

  async launchExportDipModal() {
    this.launchBulkOperationWorkflow(
      () =>
        this.archiveUnitDipService.launchExportDipModal(
          this.listOfUACriteriaSearch,
          this.shouldReadSelectedItemCount(),
          this.accessContractId,
          this.tenantIdentifier,
          this.selectedItemCount,
          this.currentPage,
          this.isAllChecked,
          this.confirmSecondActionBigNumberOfResultsActionDialog,
        ),
      this.DEFAULT_DIP_EXPORT_THRESHOLD,
    );
  }

  async launchTransferRequestModal() {
    this.launchBulkOperationWorkflow(
      () =>
        this.archiveUnitDipService.launchTransferRequestModal(
          this.listOfUACriteriaSearch,
          this.shouldReadSelectedItemCount(),
          this.accessContractId,
          this.tenantIdentifier,
          this.selectedItemCount,
          this.currentPage,
          this.isAllChecked,
          this.confirmSecondActionBigNumberOfResultsActionDialog,
        ),
      this.DEFAULT_TRANSFER_THRESHOLD,
    );
  }

  async launchUpdateManagementRuleModal() {
    this.launchBulkOperationWorkflow(
      () =>
        this.updateUnitManagementRuleService.goToUpdateManagementRule(
          this.listOfUACriteriaSearch,
          this.criteriaSearchList,
          this.currentPage,
          this.tenantIdentifier,
          this.selectedItemCount,
          this.router,
          this.selectedItemCount,
          this.actionsWithThresholdReachedAlerteMessageDialogSubscription,
          this.launchSelectionContainsHoldingUnitAlertMessageDialog,
          this.confirmSecondActionBigNumberOfResultsActionDialog,
        ),
      this.DEFAULT_UPDATE_MGT_RULES_THRESHOLD,
    );
  }

  async launchPUAUpdateModal() {
    this.launchBulkOperationWorkflow(() => {
      this.dialog.open<PuaUpdateDialogComponent, PuaUpdateDialogComponentData>(PuaUpdateDialogComponent, {
        data: {
          selectedItemCount: this.selectedItemCount,
          tenantIdentifier: this.tenantIdentifier,
          listOfUACriteriaSearch: this.listOfUACriteriaSearch,
        },
      });
    }, this.DEFAULT_PUA_UPDATE_THRESHOLD);
  }

  showAcknowledgmentTransferForm() {
    const dialogRef = this.dialog.open(TransferAcknowledgmentComponent, {
      disableClose: true,
      data: {
        accessContract: this.accessContractId,
        tenantIdentifier: this.tenantIdentifier.toString(),
      },
    });

    this.transferAcknowledgmentDialogSub = dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        return;
      }
    });
  }

  goToPersistentIdentifierSearchPage(): void {
    this.router.navigate(['archive-search/persistent-identifier-search'], {});
  }

  private addCriteria(
    keyElt: string,
    valueElt: CriteriaValue,
    labelElt: string,
    keyTranslated: boolean,
    operator: string,
    category: SearchCriteriaTypeEnum,
    valueTranslated: boolean,
    dataType: string,
    emit: boolean,
  ) {
    this.archiveHelperService.addCriteria(
      this.searchCriterias,
      this.searchCriteriaKeys,
      this.nbQueryCriteria,
      keyElt,
      valueElt,
      labelElt,
      keyTranslated,
      operator,
      category,
      valueTranslated,
      dataType,
      emit,
    );
  }

  trackBy(_: number, unit: Unit) {
    return unit['#id'];
  }

  existsArchiveUnitWithoutAttachment(): void {
    let orphanCriteriaList = [...this.criteriaSearchList];
    orphanCriteriaList.push({
      criteria: '#unitups',
      values: [
        {
          id: 'true',
          value: 'true',
        },
      ],
      category: 'FIELDS',
      operator: 'MISSING',
      dataType: 'STRING',
    });
    const searchCriteria = {
      criteriaList: orphanCriteriaList,
      pageNumber: 0,
      size: 1,
      trackTotalHits: false,
      computeMgtRulesFacets: false,
      includedFields: ['#id'],
    };
    this.archiveService.searchArchiveUnitsByCriteria(searchCriteria).subscribe((response: PagedResult) => {
      let resultCount = 0;
      if (response?.results?.length) {
        resultCount = response.totalResults;
      }
      this.archiveSharedDataService.emitNumberOfAUsWithoutAttachment(resultCount);
    });
  }
}
