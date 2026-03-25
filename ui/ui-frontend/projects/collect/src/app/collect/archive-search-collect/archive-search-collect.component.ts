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
import { AfterViewInit, Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, finalize, merge, Observable, Subject, Subscription, zip } from 'rxjs';
import { debounceTime, filter, map, mergeMap, share, take, tap } from 'rxjs/operators';
import { isEmpty } from 'underscore';
import {
  AccessContract,
  ALL_DESCENDANTS_FACET,
  ApplicationId,
  ArchiveSearchResultFacets,
  BreadCrumbData,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaSearchCriteria,
  CriteriaValue,
  Direction,
  ExternalParameters,
  ExternalParametersService,
  FilingHoldingSchemeNode,
  GlobalEventService,
  ORIGIN_WAITING_RECALCULATE,
  ORPHANS_NODE_ID,
  PagedResult,
  QueryParamsService,
  ReclassificationDialogComponent,
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
  SidenavPage,
  SnackBarService,
  TermsFacet,
  Transaction,
  TransactionStatus,
  ArchiveUnit,
  addErrorStatusBadgeIfArchiveUnitHasErrors,
  Unit,
  UnitType,
  VALID_COMPUTED_INHERITED_RULES_FACET,
  STORAGE_RULE,
  APPRAISAL_RULE,
  ACCESS_RULE,
  DISSEMINATION_RULE,
  REUSE_RULE,
  WAITING_RECALCULATE,
  NODES,
  toManagementRuleType,
  MANAGEMENT_RULE_SHARED_DATA_SERVICE,
} from 'vitamui-library';
import { ArchiveCollectService } from './archive-collect.service';
import { SearchCriteriaSaverComponent } from './archive-search-criteria/components/search-criteria-saver/search-criteria-saver.component';
import { ArchiveFacetsService } from './archive-search-criteria/services/archive-facets.service';
import { ArchiveSearchHelperService } from './archive-search-criteria/services/archive-search-helper.service';
import { ArchiveSharedDataService } from '../core/archive-shared-data.service';
import { UpdateUnitsMetadataComponent } from './update-units-metadata/update-units-metadata.component';
import { AddUnitsComponent } from './add-units/add-units.component';
import { TransactionsService } from '../transactions/transactions.service';
import { MatCheckboxChange } from '@angular/material/checkbox';

const PAGE_SIZE = 10;
const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';
const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-archive-search-collect',
  templateUrl: './archive-search-collect.component.html',
  styleUrls: ['./archive-search-collect.component.scss'],
  standalone: false,
  providers: [
    {
      provide: MANAGEMENT_RULE_SHARED_DATA_SERVICE,
      useExisting: ArchiveSharedDataService,
    },
  ],
})
export class ArchiveSearchCollectComponent extends SidenavPage<any> implements OnInit, OnDestroy, AfterViewInit {
  readonly UnitType = UnitType;

  DEFAULT_DELETION_THRESHOLD = 10_000;
  RECLASSIFICATION_THRESHOLD = 10_000;

  accessContract: string;

  subscriptions: Subscription = new Subscription();

  transaction: Transaction;
  private transaction$: Observable<Transaction>;
  foundAccessContract = false;
  accessContractUpdatingRestrictedDesc: boolean;
  hasUnitaryUpdateUnitRole = false;
  hasDeleteArchiveUnitActionRole = false;
  hasBulkUpdateUnitRole = false;
  isLPExtended = false;
  show = true;
  hasSendTransactionRole = false;
  hasCloseTransactionRole = false;
  hasReclassificationRole = false;

  searchCriteriaKeys: string[];
  searchCriterias: Map<string, CriteriaSearchCriteria>;
  criteriaSearchList: SearchCriteriaEltDto[] = [];
  additionalSearchCriteriaCategories: SearchCriteriaCategory[];
  nbQueryCriteria = 0;
  additionalSearchCriteriaCategoryIndex = 0;
  included = false;
  showCriteriaPanel = true;
  showSearchCriteriaPanel = false;
  archiveUnits: Unit[];

  listOfUAIdToInclude: CriteriaValue[] = [];
  listOfUAIdToExclude: CriteriaValue[] = [];
  listOfUACriteriaSearch: SearchCriteriaEltDto[] = [];
  nodeArray: FilingHoldingSchemeNode[] = [];

  // AU Search Properties
  pending = false;
  submited = false;
  currentPage = 0;
  itemSelected = 0;
  itemNotSelected = 0;
  isIndeterminate: boolean;
  isAllChecked: boolean;
  waitingToGetFixedCount = false;
  totalResults = 0;
  orderBy = 'Title';
  direction = Direction.ASCENDANT;
  DEFAULT_RESULT_THRESHOLD = 10000;
  searchHasResults = false;
  pageNumbers = 0;
  canLoadMore = false;

  // Facets properties
  pendingGetFixedCount = false;
  pendingComputeFacets = false;
  archiveSearchResultFacets: ArchiveSearchResultFacets = new ArchiveSearchResultFacets();
  rulesFacetsComputed = false;
  showingFacets = false;
  defaultFacetTabIndex = 1;
  submitedGetFixedCount = false;
  rulesFacetsCanBeComputed = false;

  bulkOperationsThreshold = -1;

  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly orderChange = new Subject<void>();
  isNotOpen$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  isNotReady$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

  tenantIdentifier: string;
  projectName: string;
  isAutomaticIngest = false;
  breadcrumbData: BreadCrumbData[];

  archiveUnitAllunitup: string[];

  selectedArchive$: Observable<Unit>;
  rulesToExport$: Observable<Rule[]>;

  search$: Observable<number>;

  displayedColumns = ['checkbox', 'type', 'name_description', 'start_date', 'end_date', 'originating_agency'];
  criteriaCategoriesList = [STORAGE_RULE, APPRAISAL_RULE, ACCESS_RULE, DISSEMINATION_RULE, REUSE_RULE];

  @ViewChild('confirmImportantAllowedBulkOperationsDialog', { static: true })
  confirmImportantAllowedBulkOperationsDialog: TemplateRef<ArchiveSearchCollectComponent>;
  @ViewChild('actionsWithThresholdReachedAlerteMessageDialog', { static: true })
  actionsWithThresholdReachedAlerteMessageDialog: TemplateRef<ArchiveSearchCollectComponent>;
  @ViewChild('confirmSecondActionBigNumberOfResultsActionDialog', { static: true })
  confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchCollectComponent>;
  @ViewChild('reclassificationAlerteMessageDialog', { static: true })
  reclassificationAlerteMessageDialog: TemplateRef<ArchiveSearchCollectComponent>;
  @ViewChild('deletionAlerteMessageDialog', { static: true })
  deletionAlerteMessageDialog: TemplateRef<ArchiveSearchCollectComponent>;

  actionsWithThresholdReachedAlerteMessageDialogSubscription: Subscription;

  constructor(
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private externalParameterService: ExternalParametersService,
    private translateService: TranslateService,
    private archiveUnitCollectService: ArchiveCollectService,
    private archiveHelperService: ArchiveSearchHelperService,
    private archiveSharedDataService: ArchiveSharedDataService,
    private archiveFacetsService: ArchiveFacetsService,
    public dialog: MatDialog,
    private queryParamsService: QueryParamsService,
    private searchCriteriaService: SearchCriteriaService,
    private ruleService: RuleService,
    private snackBarService: SnackBarService,
    private transactionService: TransactionsService,
  ) {
    super(route, globalEventService);

    this.subscriptions.add(
      this.archiveSharedDataService.getNodes().subscribe((node) => {
        if (node && node.id && !node.checked) {
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
          this.archiveHelperService.addCriteria(
            this.searchCriterias,
            this.searchCriteriaKeys,
            this.nbQueryCriteria,
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
        } else if (node.isVirtual) {
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
          this.archiveHelperService.addCriteria(
            this.searchCriterias,
            this.searchCriteriaKeys,
            this.nbQueryCriteria,
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
      }),
    );

    this.subscriptions.add(
      this.archiveSharedDataService.receiveSimpleSearchCriteriaSubject().subscribe((criteria) => this.searchCriteriaAddAction(criteria)),
    );

    this.archiveSharedDataService
      .receiveRemoveFromChildSearchCriteriaSubject()
      .subscribe((criteria) => this.searchCriteriaRemoveAction(criteria));

    this.archiveSharedDataService.receiveRemoveFromChildSearchCriteriaSubject().subscribe((criteria) => {
      if (criteria) {
        if (criteria.valueElt) {
          this.removeCriteria(criteria.keyElt, criteria.valueElt, false);
        } else {
          this.removeCriteriaAllValues(criteria.keyElt, false);
        }
      }
    });

    this.selectedArchive$ = archiveSharedDataService.selectedUnit$;
  }

  ngOnDestroy() {
    this.subscriptions.unsubscribe();
    this.actionsWithThresholdReachedAlerteMessageDialogSubscription?.unsubscribe();
  }

  public ngOnInit(): void {
    this.additionalSearchCriteriaCategoryIndex = 0;
    this.additionalSearchCriteriaCategories = [];
    this.searchCriteriaKeys = [];
    this.searchCriterias = new Map();

    this.transaction$ = this.route.params.pipe(
      tap((params) => (this.tenantIdentifier = params.tenantIdentifier)),
      mergeMap((params) => {
        const { projectId, transactionId } = params;
        return transactionId
          ? this.archiveUnitCollectService.getTransactionById(transactionId)
          : this.archiveUnitCollectService.getLastTransactionByProjectId(projectId);
      }),
      tap((transaction) => {
        this.transaction = transaction;
        this.existsArchiveUnitWithoutAttachment();
      }),
      share(),
    );
    this.subscriptions.add(
      this.transaction$.subscribe((transaction) => {
        this.fetchUserAccessContractFromExternalParameters();
        if (!!transaction) {
          this.isNotOpen$.next(transaction.status !== TransactionStatus.OPEN);
          this.isNotReady$.next(transaction.status !== TransactionStatus.READY);
        } else {
          this.isNotOpen$.next(true);
          this.isNotReady$.next(true);
        }
      }),
    );
    this.subscriptions.add(
      this.route.params
        .pipe(
          mergeMap((params) => {
            const { projectId, transactionId } = params;
            const path$: Observable<BreadCrumbData>[] = [
              this.archiveUnitCollectService.getProjectById(projectId).pipe(
                tap((project) => (this.isAutomaticIngest = Boolean(project.automaticIngest))),
                map((project) => {
                  return {
                    label: project.messageIdentifier,
                    redirectUrl: `collect/transactions/${projectId}`,
                  };
                }),
              ),
            ];
            if (transactionId) path$.push(this.transaction$.pipe(map((transaction) => ({ label: transaction.messageIdentifier }))));
            return zip(path$);
          }),
        )
        .subscribe((path: BreadCrumbData[]) => {
          this.projectName = path[path.length - 1].label;
          this.breadcrumbData = [{ identifier: ApplicationId.PORTAL_APP }, { identifier: ApplicationId.COLLECT_APP }, ...path];
        }),
    );

    if (!this.route.snapshot.queryParamMap.keys.length) {
      this.queryParamsService
        .builder()
        .addQueryParam('archiveUnitType', 'ARCHIVE_UNIT_WITH_OBJECTS')
        .addQueryParam('archiveUnitType', 'ARCHIVE_UNIT_WITHOUT_OBJECTS')
        .navigate({ replaceUrl: true });
    }

    this.subscriptions.add(
      merge(this.orderChange, this.filterChange)
        .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS))
        .subscribe(() => this.submit()),
    );
    this.subscriptions.add(
      this.archiveSharedDataService.getToggle().subscribe((hidden) => {
        this.show = hidden;
      }),
    );

    this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const threshold = Number(parameters.get(ExternalParameters.PARAM_BULK_OPERATIONS_THRESHOLD) || -1);
      this.bulkOperationsThreshold = threshold;
    });

    this.rulesToExport$ = this.ruleService
      .getAllForTenant(this.tenantIdentifier)
      .pipe(map((rules) => rules.sort((a, b) => a.ruleId.localeCompare(b.ruleId))));

    this.checkUpdateUnitPermissions();
  }

  ngAfterViewInit() {
    // Trigger the search after getting the transaction and the view is init. Also making sure that searchCriteriaService is ready (i.e.: schema has been retrieved) in order to trigger search only after criteria have been set from the URL query params
    zip(this.transaction$, this.searchCriteriaService.ready()).subscribe(() => {
      this.archiveSharedDataService
        .receiveSimpleSearchCriteriaSubject()
        .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS), take(1)) // For some reason, we have to use that complex observable to trigger the submit() at the correct time (i.e.: the criteria have been set from the URL query params, if any)
        .subscribe((_criteria) => setTimeout(() => this.submit()));
    });
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

  private checkUpdateUnitPermissions() {
    this.archiveUnitCollectService
      .hasCollectRole('ROLE_COLLECT_UPDATE_UNITARY_ARCHIVE_UNIT', Number(this.tenantIdentifier))
      .subscribe((result) => {
        this.hasUnitaryUpdateUnitRole = result;
      });

    this.archiveUnitCollectService.hasCollectRole('ROLE_COLLECT_DELETE_ARCHIVE_UNIT', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasDeleteArchiveUnitActionRole = result;
    });

    this.archiveUnitCollectService
      .hasCollectRole('ROLE_COLLECT_UPDATE_BULK_ARCHIVE_UNIT', Number(this.tenantIdentifier))
      .subscribe((result) => {
        this.hasBulkUpdateUnitRole = result;
      });

    this.archiveUnitCollectService.hasCollectRole('ROLE_SEND_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasSendTransactionRole = result;
    });

    this.archiveUnitCollectService.hasCollectRole('ROLE_CLOSE_TRANSACTIONS', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasCloseTransactionRole = result;
    });

    this.archiveUnitCollectService.hasCollectRole('ROLE_COLLECT_RECLASSIFICATION', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasReclassificationRole = result;
    });
  }

  launchReclassification() {
    this.search$.subscribe((totalHits) => {
      if (
        (this.isAllChecked && totalHits - this.itemNotSelected > this.RECLASSIFICATION_THRESHOLD) ||
        this.itemSelected > this.RECLASSIFICATION_THRESHOLD
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
        let unitUps = this.archiveUnits
          .filter((archiveUnit) => archiveUnitGuidSelected.includes(archiveUnit['#id']))
          .map((archiveUnit) => archiveUnit['#unitups']);
        this.archiveUnitAllunitup = this.initArchiveUnitAllunitup(unitUps);
        this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
        const selectedItems = this.isAllChecked ? totalHits - this.itemNotSelected : this.itemSelected;
        const reclassificationCriteria = {
          criteriaList: this.listOfUACriteriaSearch,
          pageNumber: 0,
          size: selectedItems,
          language: this.translateService.currentLang,
          tenantIdentifier: this.tenantIdentifier,
        };
        const dialogRef = this.dialog.open(ReclassificationDialogComponent, {
          panelClass: 'vitamui-modal',
          disableClose: false,
          data: {
            appName: 'COLLECT',
            reclassificationCriteria,
            itemSelected: selectedItems,
            archiveUnitGuidSelected: archiveUnitGuidSelected,
            archiveUnitAllunitup: this.archiveUnitAllunitup,
            transactionId: this.transaction.id,
            tenantIdentifier: this.tenantIdentifier,
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

  private initializeSelectionParams() {
    this.pending = true;
    this.submited = true;
    this.showCriteriaPanel = false;
    this.showSearchCriteriaPanel = false;
    this.currentPage = 0;
    this.archiveUnits = [];
    this.criteriaSearchList = [];
    this.itemSelected = 0;
    this.isIndeterminate = false;
    this.itemNotSelected = 0;
    this.isAllChecked = false;
  }

  // Search Data
  fetchUserAccessContractFromExternalParameters() {
    this.subscriptions.add(
      this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
        const accessContractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
        if (accessContractId && accessContractId.length > 0) {
          this.accessContract = accessContractId;
          this.foundAccessContract = true;
          this.fetchVitamAccessContract();
        } else {
          this.snackBarService.open({
            message: 'COLLECT.ACCESS_CONTRACT_NOT_FOUND',
            duration: 10_000,
          });
        }
      }),
    );
  }

  fetchVitamAccessContract() {
    this.subscriptions.add(
      this.archiveUnitCollectService.getAccessContractById(this.accessContract).subscribe(
        (ac: AccessContract) => {
          this.accessContractUpdatingRestrictedDesc = ac.writingRestrictedDesc;
        },
        (error: any) => {
          this.logger.error('AccessContract not found :', error.message);
          this.snackBarService.open({
            message: 'COLLECT.ACCESS_CONTRACT_NOT_FOUND_IN_VITAM',
            translateParams: { accessContract: this.accessContract },
            duration: 10_000,
          });
        },
      ),
    );
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
      this.search$ = this.archiveUnitCollectService.getTotalTrackHitsByCriteria(this.criteriaSearchList, this.transaction?.id || null);
      this.rulesFacetsComputed = false;
      this.rulesFacetsCanBeComputed = this.archiveHelperService.checkIfRulesFacetsCanBeComputed(this.searchCriterias);
      this.searchArchiveUnits(this.rulesFacetsCanBeComputed);
      this.showingFacets = this.rulesFacetsCanBeComputed;
      if (refreshArchiveUnitsWithoutAttachment) this.existsArchiveUnitWithoutAttachment();
    }
  }

  private searchArchiveUnits(includeFacets: boolean) {
    if (includeFacets) {
      this.pendingComputeFacets = true;
      this.showingFacets = false;
    }
    // Prepare criteria and store them to use for lateral panel
    this.pending = true;
    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };

    let facets: TermsFacet[] = [];
    facets.push(ALL_DESCENDANTS_FACET);
    if (includeFacets) {
      facets.push(VALID_COMPUTED_INHERITED_RULES_FACET);
    }

    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      sortingCriteria,
      trackTotalHits: false,
      computeMgtRulesFacets: includeFacets,
      facets: facets,
    };
    this.archiveSharedDataService.emitSearchCriterias(searchCriteria);
    this.archiveUnitCollectService.searchArchiveUnitsByCriteria(searchCriteria, this.transaction?.id || null).subscribe(
      (pagedResult: PagedResult) => {
        if (includeFacets) {
          this.archiveSearchResultFacets = this.archiveFacetsService.extractRulesFacetsResults(pagedResult.facets);
          this.defaultFacetTabIndex = this.archiveHelperService.findDefaultFacetTabIndex(this.searchCriterias);
          this.pendingComputeFacets = false;
          this.rulesFacetsComputed = true;
        }
        if (this.currentPage === 0) {
          this.archiveUnits = pagedResult.results;
          this.searchHasResults = !isEmpty(pagedResult.results);
          this.archiveSearchResultFacets.nodesFacets = this.archiveFacetsService.extractNodesFacetsResults(pagedResult.facets);
          this.totalResults = pagedResult.totalResults;
          this.archiveSharedDataService.emitTotalResults(this.totalResults);
          this.archiveSharedDataService.emitFacets(this.archiveSearchResultFacets.nodesFacets);
        } else if (pagedResult.results) {
          this.archiveUnits = [...this.archiveUnits, ...pagedResult.results];
        }
        this.pageNumbers = pagedResult.pageNumbers;
        this.waitingToGetFixedCount = this.totalResults === this.DEFAULT_RESULT_THRESHOLD;
        if (this.isAllChecked) {
          this.itemSelected = this.totalResults - this.itemNotSelected;
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
        this.logger.error('Error message :', error.message);
        this.canLoadMore = false;
        this.pending = false;
        if (includeFacets) {
          this.pendingComputeFacets = false;
          this.archiveSharedDataService.emitFacets([]);
        }
      },
    );
  }

  onArchiveUnitCountChange(event: number) {
    this.totalResults = event;
    this.archiveSharedDataService.emitTotalResults(event);
  }

  // Manage lateral panels

  showExtendedLateralPanel() {
    this.isLPExtended = true;
  }

  backToNormalLateralPanel() {
    this.isLPExtended = false;
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  showPreviewArchiveUnit(item: Unit) {
    this.openPanel(item);
    this.archiveSharedDataService.emitSelectedUnit(item);
  }

  // Manage criteria filters methods

  checkParentBoxChange(event: MatCheckboxChange) {
    const { checked } = event;

    this.isAllChecked = checked;
    this.itemSelected = checked ? this.totalResults : 0;
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
    const action = event.checked;

    if (this.isAllChecked && !action) {
      this.listOfUACriteriaSearch = [];
      this.isIndeterminate = true;
      this.listOfUAIdToExclude.push({ value: id, id });
      this.listOfUAIdToInclude = [];
      if (this.itemSelected > 0) {
        this.itemSelected--;
        this.itemNotSelected++;
      }
    } else {
      if (action) {
        this.listOfUACriteriaSearch = [];
        this.itemSelected++;
        if (this.itemSelected === this.totalResults) {
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
        if (this.itemSelected > 0) {
          this.itemSelected--;
          this.itemNotSelected++;
        }
      }
    }
  }

  removeCriteriaEvent(criteriaToRemove: any) {
    if (criteriaToRemove.valueElt) {
      this.removeCriteria(criteriaToRemove.keyElt, criteriaToRemove.valueElt, true);
    } else {
      this.removeCriteriaAllValues(criteriaToRemove.keyElt, true);
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

  removeCriteria(keyElt: string, valueElt: CriteriaValue, emit: boolean) {
    this.archiveHelperService.removeCriteria(keyElt, valueElt, emit, this.searchCriteriaKeys, this.searchCriterias, this.nbQueryCriteria);

    if (this.searchCriterias && this.searchCriterias.size === 0) {
      this.submited = false;
      this.showCriteriaPanel = true;
      this.showSearchCriteriaPanel = false;
      // Get initial AUs by project Id
      this.searchCriteriaKeys = [];
      this.searchCriterias = new Map();
      this.criteriaSearchList = [];
      this.searchArchiveUnits(false);
      this.archiveSharedDataService.emitNodeTarget(null);
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
    this.itemSelected = 0;
    this.isAllChecked = false;
    this.isIndeterminate = false;
    this.itemNotSelected = 0;
    this.canLoadMore = false;
    // TODO : to uncomment when filing will be available
    // this.setFilingHoldingScheme();
    // this.archiveExchangeDataService.emitFilingHoldingNodes(this.nodeArray);
    // this.checkAllNodes(false);

    this.queryParamsService.setQueryParams({}, {});
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

  hiddenTreeBlock(hidden: boolean): void {
    this.show = !hidden;
  }

  // Manage crietria categories

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

  showHidePanel(show: boolean) {
    this.showCriteriaPanel = show;
  }

  containsWaitingToRecalculateInheritenceRuleCriteria() {
    return this.searchCriterias && this.searchCriterias.has(WAITING_RECALCULATE);
  }

  // Save criteria

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
        return;
      }
    });
  }

  // Manage Facets

  private launchComputingManagementRulesFacets() {
    this.pendingComputeFacets = true;
    let facets: TermsFacet[] = [];
    facets.push(ALL_DESCENDANTS_FACET);
    facets.push(VALID_COMPUTED_INHERITED_RULES_FACET);
    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: 0,
      size: 1,
      sortingCriteria,
      trackTotalHits: this.totalResults >= 10000,
      computeMgtRulesFacets: true,
      facets: facets,
    };

    this.archiveUnitCollectService.searchArchiveUnitsByCriteria(searchCriteria, !!this.transaction ? this.transaction.id : null).subscribe(
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

  public shouldReadSelectedItemCount(): boolean {
    return !this.waitingToGetFixedCount || !this.isAllChecked;
  }

  async prepareToLaunchVitamAction() {
    this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
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
    const dialogRef = this.dialog.open(this.actionsWithThresholdReachedAlerteMessageDialog);

    this.actionsWithThresholdReachedAlerteMessageDialogSubscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {});
    this.actionsWithThresholdReachedAlerteMessageDialogSubscription?.unsubscribe();
  }

  private async launchBulkOperationWorkflow(operation: () => void, defaultBulkOperationThreshold: number) {
    await this.prepareToLaunchVitamAction();

    if (!(this.shouldReadSelectedItemCount() && this.itemSelected > 0)) {
      return;
    }

    const hasBulkOperationThreshold = this.bulkOperationsThreshold !== -1;
    const isGreaterThanBulkOperationThreshold = this.itemSelected > this.bulkOperationsThreshold;
    const isGreaterThanDefaultBulkOperationThreshold = this.itemSelected > defaultBulkOperationThreshold;

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

  async launchDeletionModal() {
    this.search$.subscribe((totalHits) => {
      if (
        (this.isAllChecked && totalHits - this.itemNotSelected > this.DEFAULT_DELETION_THRESHOLD) ||
        this.itemSelected > this.DEFAULT_DELETION_THRESHOLD
      ) {
        const dialogToOpen = this.deletionAlerteMessageDialog;
        const dialogRef = this.dialog.open(dialogToOpen);
        this.subscriptions.add(
          dialogRef
            .afterClosed()
            .pipe(filter((result) => !!result))
            .subscribe(() => {}),
        );
      } else {
        this.launchBulkOperationWorkflow(
          () =>
            this.archiveUnitCollectService.launchDeletionModal(
              this.transaction.id,
              this.listOfUACriteriaSearch,
              Number(this.tenantIdentifier),
              this.currentPage,
              this.confirmSecondActionBigNumberOfResultsActionDialog,
            ),
          this.DEFAULT_DELETION_THRESHOLD,
        );
      }
    });
  }

  loadMore() {
    if (this.pending) {
      return;
    }
    this.canLoadMore = this.currentPage < this.pageNumbers - 1;
    if (!this.canLoadMore) {
      return;
    }
    this.submited = true;
    this.currentPage = this.currentPage + 1;
    if (!this.hasSearchCriteriaOrMoreThan10Results()) {
      return;
    }
    this.searchArchiveUnits(false);
  }

  private hasSearchCriteria() {
    return this.criteriaSearchList && this.criteriaSearchList.length > 0;
  }

  private hasSearchCriteriaOrMoreThan10Results() {
    return this.hasSearchCriteria() || this.totalResults >= 10;
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

  async launchFacetsComputing() {
    if (this.pendingComputeFacets || !this.hasSearchCriteria()) {
      return;
    }

    if (this.waitingToGetFixedCount) {
      if (this.hasSearchCriteria()) {
        this.pendingGetFixedCount = true;
        this.submitedGetFixedCount = true;
        const exactCountResults: number = await this.archiveUnitCollectService
          .getTotalTrackHitsByCriteria(this.criteriaSearchList, this.transaction?.id || null)
          .toPromise();
        if (exactCountResults !== -1) {
          this.totalResults = exactCountResults;
          this.waitingToGetFixedCount = false;
          this.launchComputingManagementRulesFacets();
        }
        this.pendingGetFixedCount = false;
      }
    } else {
      this.launchComputingManagementRulesFacets();
    }
  }

  // Manage criteria save

  showStoredSearchCriteria(event: SearchCriteriaHistory) {
    if (this.searchCriterias.size > 0) {
      this.searchCriterias = new Map();
      this.searchCriteriaKeys = [];
      this.included = false;
    }
    this.clearCriteria();
    setTimeout(() => this.applySearchCriteriaHistory(event));
  }

  private applySearchCriteriaHistory(storedSearchCriteriaHistory: SearchCriteriaHistory) {
    // TODO : to uncomment when filing will be available
    // this.setFilingHoldingScheme();
    // this.checkAllNodes(false);

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

  // Export data to CSV

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
      this.archiveUnitCollectService.exportCsvSearchArchiveUnitsByCriteria(searchCriteria, this.transaction.id);
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

  validateTransaction() {
    this.transactionService
      .validate(this.transaction, { isAutomaticIngest: this.isAutomaticIngest })
      .pipe(
        finalize(() => {
          this.snackBarService.open({
            message: 'COLLECT.VALIDATE_TRANSACTION_VALIDATED',
            duration: 10_000,
          });
        }),
      )
      .subscribe((transaction: Transaction) => {
        this.isNotOpen$.next(transaction.status !== TransactionStatus.OPEN);
        this.isNotReady$.next(transaction.status !== TransactionStatus.READY);
        this.transaction = transaction;
      });
  }

  canSend(transaction: Transaction): boolean {
    if (!transaction) return false;

    const allowedStatus = [TransactionStatus.VALIDATED];
    const isAllowedStatus = allowedStatus.includes(transaction.status);

    return this.hasSendTransactionRole && !this.isAutomaticIngest && isAllowedStatus;
  }

  sendTransaction() {
    this.archiveUnitCollectService.sendTransaction(this.transaction.id).subscribe((transaction: Transaction) => {
      this.isNotOpen$.next(transaction.status !== TransactionStatus.OPEN);
      this.isNotReady$.next(transaction.status !== TransactionStatus.READY);
      this.transaction = transaction;
      this.snackBarService.open({
        message: 'COLLECT.INGEST_TRANSACTION_LAUNCHED',
        duration: 10_000,
      });
    });
  }

  // Udpate archive units metadata
  openUpdateUnitsForm() {
    const updateUnitsMetadataDialog = this.dialog.open(UpdateUnitsMetadataComponent, {
      disableClose: true,
      data: {
        selectedTransaction: this.transaction,
        tenantIdentifier: this.tenantIdentifier,
      },
    });

    this.subscriptions.add(updateUnitsMetadataDialog.afterClosed().subscribe());
  }

  openAddUnitsForm() {
    const addUnitsDialog = this.dialog.open(AddUnitsComponent, {
      disableClose: true,
      data: {
        transaction: this.transaction,
      },
    });
    this.subscriptions.add(
      addUnitsDialog.afterClosed().subscribe((filesUploaded) => {
        if (filesUploaded) {
          this.submit();
        }
      }),
    );
  }

  isArchiveUnitsEmpty(): boolean {
    return this.archiveUnits?.length === 0;
  }

  getArchiveUnitType(archiveUnit: any) {
    if (archiveUnit) {
      return archiveUnit['#unitType'];
    }
  }

  getProperArchiveUnitIcon(archiveUnit: ArchiveUnit) {
    return !archiveUnit?.['#object'] ? 'folder' : 'file';
  }

  addStatusBadgeforArchiveUnitWithErrors(archiveUnit: ArchiveUnit) {
    return addErrorStatusBadgeIfArchiveUnitHasErrors(archiveUnit);
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
    // FIXME: for some reason, that trackBy - used to make Angular update the Unit in the list when it's modified in the sidenav - is not always working correctly: sometimes, the Unit is updated, sometimes not. It looks like it is updated for "simple" Units (with only Généralités) and not for "complex" ones.
    return unit['#id'];
  }

  existsArchiveUnitWithoutAttachment(): void {
    const criteriaList = [
      {
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
      },
    ];
    const searchCriteria = {
      criteriaList: criteriaList,
      pageNumber: 0,
      size: 1,
      trackTotalHits: false,
      computeMgtRulesFacets: false,
      includedFields: ['#id'],
    };
    this.archiveUnitCollectService
      .searchArchiveUnitsByCriteria(searchCriteria, this.transaction?.id || null)
      .subscribe((response: PagedResult) => {
        const hasAUWithoutAttachment = response.results != null && !isEmpty(response.results);
        if (hasAUWithoutAttachment) {
          this.archiveSharedDataService.emitNumberOfAUsWithoutAttachment(response.totalResults);
        }
      });
  }

  protected readonly TransactionStatus = TransactionStatus;
}
