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

import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, merge, Subject, Subscription } from 'rxjs';
import { debounceTime, map, mergeMap } from 'rxjs/operators';
import {
  AccessContract,
  CriteriaDataType,
  CriteriaOperator,
  Direction,
  ExternalParameters,
  ExternalParametersService,
  FilingHoldingSchemeNode,
  GlobalEventService,
  SidenavPage,
  Transaction,
  TransactionStatus,
  Unit
} from 'ui-frontend-common';
import {
  ArchiveSearchResultFacets,
  CriteriaValue,
  PagedResult,
  SearchCriteria,
  SearchCriteriaCategory,
  SearchCriteriaEltDto,
  SearchCriteriaEltements,
  SearchCriteriaHistory,
  SearchCriteriaMgtRuleEnum,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum
} from '../core/models';
import { ArchiveCollectService } from './archive-collect.service';
import {
  SearchCriteriaSaverComponent
} from './archive-search-criteria/components/search-criteria-saver/search-criteria-saver.component';
import { ArchiveFacetsService } from './archive-search-criteria/services/archive-facets.service';
import { ArchiveSearchHelperService } from './archive-search-criteria/services/archive-search-helper.service';
import { ArchiveSharedDataService } from './archive-search-criteria/services/archive-shared-data.service';
import { UpdateUnitsaMetadataComponent } from './update-units-metadata/update-units-metadata.component';

const PAGE_SIZE = 10;
const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';
const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';
const FILTER_DEBOUNCE_TIME_MS = 400;

const ARCHIVE_UNIT_WITH_OBJECTS = 'ARCHIVE_UNIT_WITH_OBJECTS';
const ARCHIVE_UNIT_WITHOUT_OBJECTS = 'ARCHIVE_UNIT_WITHOUT_OBJECTS';

@Component({
  selector: 'app-archive-search-collect',
  templateUrl: './archive-search-collect.component.html',
  styleUrls: ['./archive-search-collect.component.scss'],
})
export class ArchiveSearchCollectComponent extends SidenavPage<any> implements OnInit, OnDestroy {
  accessContract: string;
  accessContractSub: Subscription;
  accessContractSubscription: Subscription;
  transactionSubscription: Subscription;
  errorMessageSub: Subscription;
  subscriptionFilingHoldingSchemeNodes: Subscription;
  subscriptionSimpleSearchCriteriaAdd: Subscription;
  subscriptionNodes: Subscription;
  searchCriteriaChangeSubscription: Subscription;
  subscriptions: Subscription = new Subscription();
  uaMetadataUpdateDialogSub: Subscription;

  transaction: Transaction;
  projectId: string;
  foundAccessContract = false;
  accessContractAllowUpdating = false;
  accessContractUpdatingRestrictedDesc: boolean;
  hasUpdateDescriptiveUnitMetadataRole = false;
  isLPExtended = false;
  show = true;

  searchCriteriaKeys: string[];
  searchCriterias: Map<string, SearchCriteria>;
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
  isAllchecked: boolean;
  waitingToGetFixedCount = false;
  totalResults = 0;
  orderBy = 'Title';
  direction = Direction.ASCENDANT;
  DEFAULT_RESULT_THRESHOLD = 10000;
  hasResults = false;
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

  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly orderChange = new Subject<string>();
  isNotOpen$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);
  isNotReady$: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(true);

  tenantIdentifier: string;
  projectName: string;

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
    public dialog: MatDialog
  ) {
    super(route, globalEventService);
    this.subscriptionSimpleSearchCriteriaAdd = this.archiveExchangeDataService
      .receiveSimpleSearchCriteriaSubject()
      .subscribe((criteria) => {
        if (criteria) {
          this.archiveHelperService.addCriteria(
            this.searchCriterias,
            this.searchCriteriaKeys,
            this.nbQueryCriteria,
            criteria.keyElt,
            criteria.valueElt,
            criteria.labelElt,
            criteria.keyTranslated,
            criteria.operator,
            criteria.category,
            criteria.valueTranslated,
            criteria.dataType,
            false
          );
        }
      });

    this.subscriptions.add(
      this.archiveExchangeDataService.getNodes().subscribe((node) => {
        if (node.checked) {
          this.archiveHelperService.addCriteria(
            this.searchCriterias,
            this.searchCriteriaKeys,
            this.nbQueryCriteria,
            'NODE',
            {id: node.id, value: node.id},
            node.title,
            true,
            CriteriaOperator.EQ,
            SearchCriteriaTypeEnum.NODES,
            false,
            CriteriaDataType.STRING,
            false
          );
        } else {
          node.count = null;
          this.removeCriteria('NODE', {id: node.id, value: node.id}, false);
        }
      })
    );

    this.archiveExchangeDataService.receiveRemoveFromChildSearchCriteriaSubject().subscribe((criteria) => {
      if (criteria) {
        if (criteria.valueElt) {
          this.removeCriteria(criteria.keyElt, criteria.valueElt, false);
        } else {
          this.removeCriteriaAllValues(criteria.keyElt, false);
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.accessContractSub?.unsubscribe();
    this.accessContractSubscription?.unsubscribe();
    this.subscriptionSimpleSearchCriteriaAdd?.unsubscribe();
    this.subscriptionFilingHoldingSchemeNodes?.unsubscribe();
    this.subscriptionNodes?.unsubscribe();
    this.errorMessageSub?.unsubscribe();
    this.searchCriteriaChangeSubscription?.unsubscribe();
    this.subscriptions?.unsubscribe();
    this.transactionSubscription?.unsubscribe();
    this.uaMetadataUpdateDialogSub?.unsubscribe();
  }

  ngOnInit(): void {
    this.additionalSearchCriteriaCategoryIndex = 0;
    this.additionalSearchCriteriaCategories = [];
    this.searchCriteriaKeys = [];
    this.searchCriterias = new Map();
    this.initializeSelectionParams();
    this.addInitalCriteriaValues();
    this.transactionSubscription = this.route.params
      .pipe(
        mergeMap((params) => {
          this.projectId = params.projectId;
          this.tenantIdentifier = params.tenantIdentifier;
          return params.transactionId
            ? this.archiveUnitCollectService.getTransactionById(params.transactionId)
            : this.archiveUnitCollectService.getLastTransactionByProjectId(this.projectId);
        })
      )
      .subscribe((transaction) => {
        this.transaction = transaction;
        this.fetchUserAccessContractFromExternalParameters();
        if (!!this.transaction) {
          this.isNotOpen$.next(this.transaction.status !== TransactionStatus.OPEN);
          this.isNotReady$.next(this.transaction.status !== TransactionStatus.READY);
        } else {
          this.isNotOpen$.next(true);
          this.isNotReady$.next(true);
        }
      });

    this.projectName = this.route.snapshot.queryParamMap.get('projectName');
    const searchCriteriaChange = merge(this.orderChange, this.filterChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));
    this.searchCriteriaChangeSubscription = searchCriteriaChange.subscribe(() => {
      this.submit();
    });
    this.archiveExchangeDataService.getToggle().subscribe((hidden) => {
      this.show = hidden;
    });
  }

  private addInitalCriteriaValues() {
    this.archiveHelperService.addCriteria(
      this.searchCriterias,
      this.searchCriteriaKeys,
      this.nbQueryCriteria,
      ALL_ARCHIVE_UNIT_TYPES,
      {value: ARCHIVE_UNIT_WITH_OBJECTS, id: ARCHIVE_UNIT_WITH_OBJECTS},
      this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.ARCHIVE_UNIT_WITH_OBJECTS'),
      true,
      CriteriaOperator.EQ,
      SearchCriteriaTypeEnum.FIELDS,
      false,
      CriteriaDataType.STRING,
      false
    );

    this.archiveHelperService.addCriteria(
      this.searchCriterias,
      this.searchCriteriaKeys,
      this.nbQueryCriteria,
      ALL_ARCHIVE_UNIT_TYPES,
      {value: ARCHIVE_UNIT_WITHOUT_OBJECTS, id: ARCHIVE_UNIT_WITHOUT_OBJECTS},
      this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.ARCHIVE_UNIT_WITHOUT_OBJECTS'),
      true,
      CriteriaOperator.EQ,
      SearchCriteriaTypeEnum.FIELDS,
      false,
      CriteriaDataType.STRING,
      false
    );
  }

  private initializeSelectionParams() {
    this.pending = true;
    this.showCriteriaPanel = false;
    this.showSearchCriteriaPanel = false;
    this.currentPage = 0;
    this.archiveUnits = [];
    this.criteriaSearchList = [];
    this.itemSelected = 0;
    this.isIndeterminate = false;
    this.itemNotSelected = 0;
    this.isAllchecked = false;
  }

  // Search Data
  fetchUserAccessContractFromExternalParameters() {
    this.accessContractSub = this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessConctractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessConctractId && accessConctractId.length > 0) {
        this.accessContract = accessConctractId;
        this.foundAccessContract = true;
        this.fetchVitamAccessContract();
        if (this.archiveUnits.length === 0) {
          this.searchArchiveUnits(true);
        }
      } else {
        this.errorMessageSub = this.translateService
          .get('COLLECT.ACCESS_CONTRACT_NOT_FOUND')
          .pipe(
            map((message) => {
              this.snackBar.open(message, null, {
                panelClass: 'vitamui-snack-bar',
                duration: 10000,
              });
            })
          )
          .subscribe();
      }
    });
  }

  fetchVitamAccessContract() {
    this.accessContractSubscription = this.archiveUnitCollectService.getAccessContractById(this.accessContract).subscribe(
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
      }
    );
  }

  submit() {
    this.submited = true;
    this.initializeSelectionParams();
    this.archiveHelperService.buildNodesListForQUery(this.searchCriterias, this.criteriaSearchList);
    this.archiveHelperService.buildFieldsCriteriaListForQUery(this.searchCriterias, this.criteriaSearchList);
    for (const mgtRuleType in SearchCriteriaMgtRuleEnum) {
      this.archiveHelperService.buildManagementRulesCriteriaListForQuery(mgtRuleType, this.searchCriterias, this.criteriaSearchList);
    }
    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
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
    const sortingCriteria = {criteria: this.orderBy, sorting: this.direction};
    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      sortingCriteria,
      trackTotalHits: false,
      computeFacets: includeFacets,
    };
    this.archiveExchangeDataService.emitLastSearchCriteriaDtoSubject(searchCriteria);
    this.archiveUnitCollectService
      .searchArchiveUnitsByCriteria(searchCriteria, !!this.transaction ? this.transaction.id : null, this.accessContract)
      .subscribe(
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
            this.archiveExchangeDataService.emitFacets(this.archiveSearchResultFacets.nodesFacets);
            this.hasResults = true;
            this.totalResults = pagedResult.totalResults;
          } else {
            if (pagedResult.results) {
              this.hasResults = true;
              pagedResult.results.forEach((elt) => this.archiveUnits.push(elt));
            }
          }
          this.pageNumbers = pagedResult.pageNumbers;

          this.waitingToGetFixedCount = this.totalResults === this.DEFAULT_RESULT_THRESHOLD;

          if (this.isAllchecked) {
            this.itemSelected = this.totalResults - this.itemNotSelected;
          }

          this.canLoadMore = this.currentPage < this.pageNumbers - 1;
          this.archiveHelperService.updateCriteriaStatus(
            this.searchCriterias,
            SearchCriteriaStatusEnum.IN_PROGRESS,
            SearchCriteriaStatusEnum.INCLUDED
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
        }
      );
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
  }

  // Manage criteria filters methods

  checkParentBoxChange(event: any) {
    const action = event.target.checked;
    if (action) {
      this.itemSelected = this.totalResults;
      this.isAllchecked = true;
    } else {
      this.isAllchecked = false;
      this.isIndeterminate = false;
      this.itemSelected = 0;
    }
    this.listOfUAIdToInclude = [];
    this.listOfUAIdToExclude = [];
    this.listOfUACriteriaSearch = [];
  }

  checkChildrenBoxChange(id: string, event: any) {
    const action = event.target.checked;

    if (this.isAllchecked && !action) {
      this.listOfUACriteriaSearch = [];
      this.isIndeterminate = true;
      this.listOfUAIdToExclude.push({value: id, id});
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
        this.listOfUAIdToInclude.push({value: id, id});
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

  clearCriterias() {
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
    this.isAllchecked = false;
    this.isIndeterminate = false;
    this.itemNotSelected = 0;
    this.canLoadMore = false;
    // TODO : to uncomment when filing will be available
    // this.setFilingHoldingScheme();
    // this.archiveExchangeDataService.emitFilingHoldingNodes(this.nodeArray);
    // this.checkAllNodes(false);
  }

  setFilingHoldingScheme() {
    this.subscriptionFilingHoldingSchemeNodes = this.archiveExchangeDataService.getFilingHoldingNodes().subscribe((nodes) => {
      this.nodeArray = nodes;
    });
  }

  checkAllNodes(show: boolean) {
    this.archiveHelperService.recursiveCheck(this.nodeArray, show);
  }

  selectedCategoryChange(selectedCategoryIndex: number) {
    this.additionalSearchCriteriaCategoryIndex = selectedCategoryIndex;
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
        if (SearchCriteriaTypeEnum[val.category] === category) {
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
    this.searchCriterias.forEach((criteria: SearchCriteria) => {
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
    const sortingCriteria = {criteria: this.orderBy, sorting: this.direction};
    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: 0,
      size: 1,
      sortingCriteria,
      trackTotalHits: false,
      computeFacets: true,
    };

    this.loadExactCount();

    this.archiveUnitCollectService
      .searchArchiveUnitsByCriteria(searchCriteria, !!this.transaction ? this.transaction.id : null).subscribe(
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
      }
    );
  }

  loadExactCount() {
    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
      this.pendingGetFixedCount = true;
      this.submitedGetFixedCount = true;
      this.archiveUnitCollectService
        .getTotalTrackHitsByCriteria(this.criteriaSearchList, !!this.transaction ? this.transaction.id : null)
        .subscribe(
          (exactCountResults: number) => {
            if (exactCountResults !== -1) {
              this.totalResults = exactCountResults;
              if (this.isAllchecked) {
                this.itemSelected = this.totalResults - this.itemNotSelected;
              }
              this.waitingToGetFixedCount = false;
            }
            this.pendingGetFixedCount = false;
          },
          (error: HttpErrorResponse) => {
            this.pendingGetFixedCount = false;
            this.logger.error('Error message :', error.message);
          }
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
    if (!this.hasSearchCriterias()) {
      return;
    }
    this.searchArchiveUnits(false);
  }

  private hasSearchCriterias() {
    return (this.criteriaSearchList && this.criteriaSearchList.length > 0) || this.totalResults >= 10;
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
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.APPRAISAL_RULE] ||
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.NODES] ||
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.ACCESS_RULE] ||
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.STORAGE_RULE] ||
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.REUSE_RULE] ||
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.DISSEMINATION_RULE]
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
            criteria.category,
            criteria.valueTranslated,
            criteria.dataType,
            true
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
            true
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
          this.nbQueryCriteria
        );
      });
      this.nodeArray = null;
      this.archiveExchangeDataService.emitToggle(true);
    }
  }

  // Export data to CSV

  exportArchiveUnitsToCsvFile() {

    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
      this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
      const sortingCriteria = {criteria: this.orderBy, sorting: this.direction};
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
      this.isAllchecked,
      this.isIndeterminate
    );
  }

  validateTransaction() {
    this.archiveUnitCollectService.validateTransaction(this.transaction.id).subscribe(() => {
      this.isNotOpen$.next(true);
      this.isNotReady$.next(false);
      const message = this.translateService.instant('COLLECT.VALIDATE_TRANSACTION_VALIDATED');
      this.snackBar.open(message + ': ' + this.accessContract, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
      });
    });
  }

  sendTransaction() {
    this.archiveUnitCollectService.sendTransaction(this.transaction.id).subscribe(() => {
      this.isNotReady$.next(true);
      const message = this.translateService.instant('COLLECT.INGEST_TRANSACTION_LAUNCHED');
      this.snackBar.open(message + ': ' + this.accessContract, null, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
      });
    });
  }

  // Udpate archive units metadata

  openUpdateUnitsForm() {
    const dialogRef = this.dialog.open(UpdateUnitsaMetadataComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: {
        selectedTransaction: this.transaction,
        tenantIdentifier: this.tenantIdentifier,
      },
    });

    this.uaMetadataUpdateDialogSub = dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        return;
      }
    });
  }

  updateUnitsMetadataDisabled(): boolean {
    return (!this.transaction) || this.transaction.status !== TransactionStatus.OPEN;
  }

  isArchiveUnitsEmpty(): boolean {
    return this.archiveUnits.length === 0;
  }

  getArchiveUnitType(archiveUnit: any) {
    if (archiveUnit) {
      return archiveUnit['#unitType'];
    }
  }
}
