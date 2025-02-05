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
import { MatLegacyDialog as MatDialog, MatLegacyDialogConfig as MatDialogConfig } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, merge, Observable, Subject, Subscription, zip } from 'rxjs';
import { debounceTime, filter, map, mergeMap, share, take, tap } from 'rxjs/operators';
import { isEmpty } from 'underscore';
import {
  AccessContract,
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
  ORPHANS_NODE_ID,
  PagedResult,
  QueryParamsService,
  SearchCriteriaAddAction,
  SearchCriteriaCategory,
  SearchCriteriaEltDto,
  SearchCriteriaEltements,
  SearchCriteriaHistory,
  SearchCriteriaMgtRuleEnum,
  SearchCriteriaRemoveAction,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum,
  SidenavPage,
  Transaction,
  TransactionStatus,
  Unit,
  UnitType,
  ReclassificationDialogComponent,
} from 'vitamui-library';
import { ArchiveCollectService } from './archive-collect.service';
import { SearchCriteriaSaverComponent } from './archive-search-criteria/components/search-criteria-saver/search-criteria-saver.component';
import { ArchiveFacetsService } from './archive-search-criteria/services/archive-facets.service';
import { ArchiveSearchHelperService } from './archive-search-criteria/services/archive-search-helper.service';
import { ArchiveSharedDataService } from '../core/archive-shared-data.service';
import { UpdateUnitsMetadataComponent } from './update-units-metadata/update-units-metadata.component';
import { AddUnitsComponent } from './add-units/add-units.component';

const PAGE_SIZE = 10;
const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';
const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';
const FILTER_DEBOUNCE_TIME_MS = 400;

const ARCHIVE_UNIT_WITH_OBJECTS = 'ARCHIVE_UNIT_WITH_OBJECTS';
const ARCHIVE_UNIT_WITHOUT_OBJECTS = 'ARCHIVE_UNIT_WITHOUT_OBJECTS';

const STATIC_ATTACHEMENT = 'STATIC_ATTACHEMENT';
const DYNAMIC_ATTACHEMENT = 'DYNAMIC_ATTACHEMENT_';

@Component({
  selector: 'app-archive-search-collect',
  templateUrl: './archive-search-collect.component.html',
  styleUrls: ['./archive-search-collect.component.scss'],
})
export class ArchiveSearchCollectComponent extends SidenavPage<any> implements OnInit, OnDestroy, AfterViewInit {
  readonly UnitType = UnitType;

  DEFAULT_DELETION_THRESHOLD = 10_000;

  accessContract: string;

  subscriptions: Subscription = new Subscription();

  transaction: Transaction;
  private transaction$: Observable<Transaction>;
  foundAccessContract = false;
  accessContractAllowUpdating = false;
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
  hasDynamicAttachment = false;
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
  breadcrumbData: BreadCrumbData[];

  archiveUnitGuidSelected: string[];
  archiveUnitAllunitup: string[];

  selectedArchive$: Observable<Unit>;

  search$: Observable<number>;

  @ViewChild('confirmImportantAllowedBulkOperationsDialog', { static: true })
  confirmImportantAllowedBulkOperationsDialog: TemplateRef<ArchiveSearchCollectComponent>;
  @ViewChild('actionsWithThresholdReachedAlerteMessageDialog', { static: true })
  actionsWithThresholdReachedAlerteMessageDialog: TemplateRef<ArchiveSearchCollectComponent>;
  @ViewChild('confirmSecondActionBigNumberOfResultsActionDialog', { static: true })
  confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchCollectComponent>;

  actionsWithThresholdReachedAlerteMessageDialogSubscription: Subscription;

  constructor(
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private externalParameterService: ExternalParametersService,
    private translateService: TranslateService,
    private archiveUnitCollectService: ArchiveCollectService,
    private archiveHelperService: ArchiveSearchHelperService,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private archiveFacetsService: ArchiveFacetsService,
    private snackBar: MatSnackBar,
    public dialog: MatDialog,
    private queryParamsService: QueryParamsService,
  ) {
    super(route, globalEventService);

    this.subscriptions.add(
      this.archiveExchangeDataService.getNodes().subscribe((node) => {
        if (!node.checked) {
          node.count = null;
          if (node.id === ORPHANS_NODE_ID) {
            this.removeCriteria(ORPHANS_NODE_ID, { id: node.id, value: node.id }, false);
          } else {
            this.removeCriteria('NODE', { id: node.id, value: node.id }, false);
          }
          return;
        }
        if (node.id === ORPHANS_NODE_ID) {
          this.archiveHelperService.addCriteria(
            this.searchCriterias,
            this.searchCriteriaKeys,
            this.nbQueryCriteria,
            ORPHANS_NODE_ID,
            { id: ORPHANS_NODE_ID, value: ORPHANS_NODE_ID },
            node.title,
            true,
            CriteriaOperator.MISSING,
            SearchCriteriaTypeEnum.FIELDS,
            false,
            CriteriaDataType.STRING,
            false,
          );
        } else {
          this.archiveHelperService.addCriteria(
            this.searchCriterias,
            this.searchCriteriaKeys,
            this.nbQueryCriteria,
            'NODE',
            { id: node.id, value: node.id },
            node.title,
            true,
            CriteriaOperator.EQ,
            SearchCriteriaTypeEnum.NODES,
            false,
            CriteriaDataType.STRING,
            false,
          );
        }
      }),
    );

    this.subscriptions.add(
      this.archiveExchangeDataService.receiveSimpleSearchCriteriaSubject().subscribe((criteria) => this.searchCriteriaAddAction(criteria)),
    );

    this.archiveExchangeDataService
      .receiveRemoveFromChildSearchCriteriaSubject()
      .subscribe((criteria) => this.searchCriteriaRemoveAction(criteria));

    this.archiveExchangeDataService.receiveRemoveFromChildSearchCriteriaSubject().subscribe((criteria) => {
      if (criteria) {
        if (criteria.valueElt) {
          this.removeCriteria(criteria.keyElt, criteria.valueElt, false);
        } else {
          this.removeCriteriaAllValues(criteria.keyElt, false);
        }
      }
    });

    this.selectedArchive$ = archiveExchangeDataService.selectedUnit$;
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
    this.addInitialCriteriaValues();

    this.transaction$ = this.route.params.pipe(
      tap((params) => (this.tenantIdentifier = params.tenantIdentifier)),
      mergeMap((params) => {
        const { projectId, transactionId } = params;
        return transactionId
          ? this.archiveUnitCollectService.getTransactionById(transactionId)
          : this.archiveUnitCollectService.getLastTransactionByProjectId(projectId);
      }),
      tap((transaction) => (this.transaction = transaction)),
      share(),
    );
    this.subscriptions.add(
      this.transaction$.subscribe((transaction) => {
        this.fetchUserAccessContractFromExternalParameters();
        if (!!transaction) {
          this.isNotOpen$.next(transaction.status !== TransactionStatus.OPEN);
          this.isNotReady$.next(transaction.status !== TransactionStatus.READY);
          this.existsArchiveUnitWithDynamicAttachment();
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
                map((project) => ({
                  label: project.messageIdentifier,
                  redirectUrl: `collect/transactions/${projectId}`,
                })),
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
      this.archiveExchangeDataService.getToggle().subscribe((hidden) => {
        this.show = hidden;
      }),
    );

    this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const threshold = Number(parameters.get(ExternalParameters.PARAM_BULK_OPERATIONS_THRESHOLD) || -1);
      this.bulkOperationsThreshold = threshold;
    });

    this.checkUpdateUnitPermissions();
  }

  ngAfterViewInit() {
    // Trigger the search after getting the transaction and the view is init
    this.transaction$.pipe().subscribe(() => {
      this.archiveExchangeDataService
        .receiveSimpleSearchCriteriaSubject()
        .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS), take(1)) // For some reason, we have to use that complex observable to trigger the submit() at the correct time (i.e.: the criteria have been set from the URL query params, if any)
        .subscribe((_criteria) => setTimeout(() => this.submit()));
    });
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

    this.archiveUnitCollectService.hasCollectRole('ROLE_RECLASSIFICATION', Number(this.tenantIdentifier)).subscribe((result) => {
      this.hasReclassificationRole = result;
    });
  }

  launchReclassification() {
    this.archiveUnitGuidSelected = this.isAllChecked
      ? this.archiveUnits.map((unit) => unit['#id'])
      : this.listOfUAIdToInclude.map((unit) => unit.id);
    let unitUps = this.archiveUnits
      .filter((archiveUnit) => this.archiveUnitGuidSelected.includes(archiveUnit['#id']))
      .map((archiveUnit) => archiveUnit['#unitups']);
    this.archiveUnitAllunitup = this.initArchiveUnitAllunitup(unitUps);
    this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
    const reclassificationCriteria = {
      criteriaList: this.listOfUACriteriaSearch,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      language: this.translateService.currentLang,
      tenantIdentifier: this.tenantIdentifier,
    };
    const dialogRef = this.dialog.open(ReclassificationDialogComponent, {
      panelClass: 'vitamui-modal',
      disableClose: false,
      data: {
        appName: 'COLLECT',
        reclassificationCriteria,
        itemSelected: this.itemSelected,
        archiveUnitGuidSelected: this.archiveUnitGuidSelected,
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

  public initArchiveUnitAllunitup(values: string[][]) {
    return [...new Set(values.flat())];
  }

  private addInitialCriteriaValues() {
    this.archiveHelperService.addCriteria(
      this.searchCriterias,
      this.searchCriteriaKeys,
      this.nbQueryCriteria,
      ALL_ARCHIVE_UNIT_TYPES,
      { value: ARCHIVE_UNIT_WITH_OBJECTS, id: ARCHIVE_UNIT_WITH_OBJECTS },
      this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.ARCHIVE_UNIT_WITH_OBJECTS'),
      true,
      CriteriaOperator.EQ,
      SearchCriteriaTypeEnum.FIELDS,
      false,
      CriteriaDataType.STRING,
      false,
    );

    this.archiveHelperService.addCriteria(
      this.searchCriterias,
      this.searchCriteriaKeys,
      this.nbQueryCriteria,
      ALL_ARCHIVE_UNIT_TYPES,
      { value: ARCHIVE_UNIT_WITHOUT_OBJECTS, id: ARCHIVE_UNIT_WITHOUT_OBJECTS },
      this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.ARCHIVE_UNIT_WITHOUT_OBJECTS'),
      true,
      CriteriaOperator.EQ,
      SearchCriteriaTypeEnum.FIELDS,
      false,
      CriteriaDataType.STRING,
      false,
    );
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
        const accessConctractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
        if (accessConctractId && accessConctractId.length > 0) {
          this.accessContract = accessConctractId;
          this.foundAccessContract = true;
          this.fetchVitamAccessContract();
          if (!this.archiveUnits?.length) {
            this.searchArchiveUnits(true);
          }
        } else {
          this.subscriptions.add(
            this.translateService
              .get('COLLECT.ACCESS_CONTRACT_NOT_FOUND')
              .pipe(
                map((message) => {
                  this.snackBar.open(message, null, {
                    panelClass: 'vitamui-snack-bar',
                    duration: 10000,
                  });
                }),
              )
              .subscribe(),
          );
        }
      }),
    );
  }

  fetchVitamAccessContract() {
    this.subscriptions.add(
      this.archiveUnitCollectService.getAccessContractById(this.accessContract).subscribe(
        (ac: AccessContract) => {
          this.accessContractAllowUpdating = ac.writingPermission;
          this.accessContractUpdatingRestrictedDesc = ac.writingRestrictedDesc;
        },
        (error: any) => {
          this.logger.error('AccessContract not found :', error.message);
          const message = this.translateService.instant('COLLECT.ACCESS_CONTRACT_NOT_FOUND_IN_VITAM');
          this.snackBar.open(message + ': ' + this.accessContract, null, {
            panelClass: 'vitamui-snack-bar',
            duration: 10000,
          });
        },
      ),
    );
  }

  submit() {
    this.listOfUAIdToInclude = [];
    this.listOfUAIdToExclude = [];

    this.archiveExchangeDataService.emitSelectedUnit(null);
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
    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      sortingCriteria,
      trackTotalHits: false,
      computeFacets: includeFacets,
    };
    this.archiveExchangeDataService.emitSearchCriterias(searchCriteria);
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
          this.archiveExchangeDataService.emitTotalResults(this.totalResults);
          this.archiveExchangeDataService.emitFacets(this.archiveSearchResultFacets.nodesFacets);
        } else if (pagedResult.results) {
          pagedResult.results.forEach((elt) => this.archiveUnits.push(elt));
        }
        this.filterAttachementUnit();
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
          this.archiveExchangeDataService.emitFacets([]);
        }
      },
    );
  }

  ofStaticAttachementUnit = (unit: Unit) => {
    return unit.Title === STATIC_ATTACHEMENT;
  };

  ofDynamicAttachementUnit = (unit: Unit) => {
    return unit.Title.startsWith(DYNAMIC_ATTACHEMENT);
  };

  private filterAttachementUnit() {
    let result: number = this.archiveUnits.findIndex(this.ofStaticAttachementUnit);
    if (result > -1) {
      this.archiveUnits.splice(result, 1);
      this.totalResults -= 1;
    }
    result = this.archiveUnits.findIndex(this.ofDynamicAttachementUnit);
    if (result > -1) {
      this.archiveUnits.splice(result, 1);
      this.totalResults -= 1;
    }
  }

  onArchiveUnitCountChange(event: number) {
    this.totalResults = event;
    this.archiveExchangeDataService.emitTotalResults(event);
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
    this.archiveExchangeDataService.emitSelectedUnit(item);
  }

  // Manage criteria filters methods

  checkParentBoxChange(event: any) {
    const { checked } = event.target;

    this.isAllChecked = checked;
    this.itemSelected = checked ? this.totalResults : 0;
    if (!checked) {
      this.isIndeterminate = false;
    }
    this.listOfUAIdToInclude = [];
    this.listOfUAIdToExclude = [];
    this.listOfUACriteriaSearch = [];
  }

  checkChildrenBoxChange(id: string, event: any) {
    event.stopPropagation();
    const action = event.target.checked;

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
      this.itemNotSelected = 0;
      if (action) {
        this.listOfUACriteriaSearch = [];
        this.itemSelected++;
        if (this.itemSelected === this.totalResults) {
          this.isIndeterminate = false;
        }
        this.listOfUAIdToInclude.push({ value: id, id });
        this.listOfUAIdToExclude.splice(0, this.listOfUAIdToExclude.length);
      } else {
        this.listOfUAIdToInclude = this.listOfUAIdToInclude.filter((element) => element.id !== id);
        if (this.itemSelected > 0) {
          this.itemSelected--;
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
      this.archiveExchangeDataService.emitNodeTarget(null);
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

  setFilingHoldingScheme() {
    this.subscriptions.add(
      this.archiveExchangeDataService.getFilingHoldingNodes().subscribe((nodes) => {
        this.nodeArray = nodes;
      }),
    );
  }

  checkAllNodes(show: boolean) {
    this.archiveHelperService.recursiveCheck(this.nodeArray, show);
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
    this.archiveExchangeDataService.emitRuleCategory(categoryName);
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
    this.archiveExchangeDataService.emitRuleCategory(categoryName);
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
      if (category === SearchCriteriaTypeEnum.APPRAISAL_RULE) {
        this.searchCriterias.forEach((criteriaValues, key) => {
          if (key === ELIMINATION_TECHNICAL_ID) {
            criteriaValues.values.forEach((value) => {
              this.removeCriteria(key, value.value, true);
            });
          }
        });
      }
      this.searchCriterias.forEach((val, key) => {
        if (SearchCriteriaTypeEnum[val.category] === category || key === 'WAITING_RECALCULATE') {
          val.values.forEach((value) => {
            this.removeCriteria(key, value.value, true);
          });
        }
      });
    }
  }

  showHidePanel(show: boolean) {
    this.showCriteriaPanel = show;
  }

  containsWaitingToRecalculateInheritenceRuleCriteria() {
    return this.searchCriterias && this.searchCriterias.has('WAITING_RECALCULATE');
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
    const dialogConfig = new MatDialogConfig();
    dialogConfig.panelClass = 'vitamui-modal';
    dialogConfig.disableClose = false;
    dialogConfig.data = {
      searchCriteriaHistory: searchCriteriaHistory$,
      originalSearchCriteria: this.searchCriterias,
      nbCriterias: this.archiveExchangeDataService.nbFilters(searchCriteriaHistory$),
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
    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: 0,
      size: 1,
      sortingCriteria,
      trackTotalHits: false,
      computeFacets: true,
    };

    this.loadExactCount();

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
    this.loadExactCount();
    this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
  }

  private bulkOperationWarningWorkflow(operation: () => void): void {
    const dialogConfirmActionWithImportantAllowedCount = this.confirmImportantAllowedBulkOperationsDialog;
    const dialogConfirmActionWithImportantAllowedCountRef = this.dialog.open(dialogConfirmActionWithImportantAllowedCount, {
      panelClass: 'vitamui-dialog',
    });

    dialogConfirmActionWithImportantAllowedCountRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(operation);
  }

  private bulkOperationErrorWorkflow(): void {
    const dialogRef = this.dialog.open(this.actionsWithThresholdReachedAlerteMessageDialog, { panelClass: 'vitamui-dialog' });

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

  loadExactCount() {
    if (this.hasSearchCriteria()) {
      this.pendingGetFixedCount = true;
      this.submitedGetFixedCount = true;
      this.archiveUnitCollectService.getTotalTrackHitsByCriteria(this.criteriaSearchList, this?.transaction?.id || null).subscribe(
        (exactCountResults: number) => {
          if (exactCountResults !== -1) {
            this.totalResults = exactCountResults;
            if (this.isAllChecked) {
              this.itemSelected = this.totalResults - this.itemNotSelected;
            }
            this.waitingToGetFixedCount = false;
          }
          this.pendingGetFixedCount = false;
        },
        (error: HttpErrorResponse) => {
          this.pendingGetFixedCount = false;
          this.logger.error('Error message :', error.message);
        },
      );
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

  launchFacetsComputing() {
    if (!this.pendingComputeFacets && this.criteriaSearchList && this.criteriaSearchList.length > 0) {
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
    this.reMapSearchCriteriaFromSearchCriteriaHistory(event);
  }

  public reMapSearchCriteriaFromSearchCriteriaHistory(storedSearchCriteriaHistory: SearchCriteriaHistory) {
    // TODO : to uncomment when filing will be available
    // this.setFilingHoldingScheme();
    // this.checkAllNodes(false);
    storedSearchCriteriaHistory.searchCriteriaList.forEach((criteria: SearchCriteriaEltements) => {
      this.fillTreeNodeAsSearchCriteriaHistory(criteria);
      const c = criteria.criteria;
      criteria.values.forEach((value) => {
        if (
          [
            SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.APPRAISAL_RULE],
            SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.NODES],
            SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.ACCESS_RULE],
            SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.STORAGE_RULE],
            SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.REUSE_RULE],
            SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.DISSEMINATION_RULE],
          ].includes(criteria.category as SearchCriteriaTypeEnum)
        ) {
          this.addCriteriaCategory(criteria.category);
          this.archiveHelperService.addCriteria(
            this.searchCriterias,
            this.searchCriteriaKeys,
            this.nbQueryCriteria,
            c,
            value,
            value.value,
            criteria.keyTranslated,
            criteria.operator,
            criteria.category as SearchCriteriaTypeEnum,
            criteria.valueTranslated,
            criteria.dataType,
            true,
          );
        } else if (criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS]) {
          this.archiveHelperService.addCriteria(
            this.searchCriterias,
            this.searchCriteriaKeys,
            this.nbQueryCriteria,
            c,
            value,
            c === ALL_ARCHIVE_UNIT_TYPES
              ? this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.' + value.id)
              : value.value,
            criteria.keyTranslated,
            criteria.operator,
            SearchCriteriaTypeEnum.FIELDS,
            criteria.valueTranslated,
            criteria.dataType,
            true,
          );
        }
      });
    });
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
      this.archiveExchangeDataService.emitToggle(true);
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
    this.archiveUnitCollectService.validateTransaction(this.transaction.id).subscribe(() => {
      this.isNotOpen$.next(true);
      this.isNotReady$.next(false);
      const message = this.translateService.instant('COLLECT.VALIDATE_TRANSACTION_VALIDATED');
      this.snackBar.open(message, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
      });
    });
  }

  sendTransaction() {
    this.archiveUnitCollectService.sendTransaction(this.transaction.id).subscribe(() => {
      this.isNotReady$.next(true);
      const message = this.translateService.instant('COLLECT.INGEST_TRANSACTION_LAUNCHED');
      this.snackBar.open(message, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
      });
    });
  }

  // Udpate archive units metadata
  openUpdateUnitsForm() {
    const updateUnitsMetadataDialog = this.dialog.open(UpdateUnitsMetadataComponent, {
      panelClass: 'vitamui-modal',
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
      panelClass: 'vitamui-modal',
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

  updateUnitsMetadataDisabled(): boolean {
    return !this.transaction || this.transaction.status !== TransactionStatus.OPEN;
  }

  isArchiveUnitsEmpty(): boolean {
    return this.archiveUnits?.length === 0;
  }

  getArchiveUnitType(archiveUnit: any) {
    if (archiveUnit) {
      return archiveUnit['#unitType'];
    }
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

  existsArchiveUnitWithDynamicAttachment(): void {
    const criteriaList = [
      {
        criteria: 'ALL_ARCHIVE_UNIT_TYPES',
        values: [
          {
            value: 'ARCHIVE_UNIT_WITH_OBJECTS',
            id: 'ARCHIVE_UNIT_WITH_OBJECTS',
          },
          {
            value: 'ARCHIVE_UNIT_WITHOUT_OBJECTS',
            id: 'ARCHIVE_UNIT_WITHOUT_OBJECTS',
          },
        ],
        operator: 'EQ',
        category: 'FIELDS',
        dataType: 'STRING',
      },
      {
        criteria: 'TITLE_OR_DESCRIPTION',
        values: [
          {
            value: 'DYNAMIC_ATTACHEMENT',
            id: 'DYNAMIC_ATTACHEMENT',
          },
        ],
        operator: 'EQ',
        category: 'FIELDS',
        dataType: 'STRING',
      },
    ];
    const searchCriteria = {
      criteriaList: criteriaList,
      pageNumber: 0,
      size: 1,
      sortingCriteria: { criteria: this.orderBy, sorting: this.direction },
      trackTotalHits: false,
      computeFacets: false,
    };
    this.archiveUnitCollectService
      .searchArchiveUnitsByCriteria(searchCriteria, this.transaction?.id || null)
      .subscribe((response: PagedResult) => {
        this.hasDynamicAttachment = response.results != null && !isEmpty(response.results);
      });
  }
}
