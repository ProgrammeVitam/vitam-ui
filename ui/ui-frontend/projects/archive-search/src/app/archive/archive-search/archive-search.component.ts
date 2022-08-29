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
import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { merge, Subject, Subscription } from 'rxjs';
import { debounceTime, filter } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, Direction, Logger, VitamuiRoles } from 'ui-frontend-common';
import { ArchiveSharedDataService } from '../../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../../core/management-rules-shared-data.service';
import { ArchiveService } from '../archive.service';
import { ArchiveFacetsService } from '../common-services/archive-facets.service';
import { ArchiveSearchHelperService } from '../common-services/archive-search-helper.service';
import { ArchiveUnitDipService } from '../common-services/archive-unit-dip.service';
import { ArchiveUnitEliminationService } from '../common-services/archive-unit-elimination.service';
import { ComputeInheritedRulesService } from '../common-services/compute-inherited-rules.service';
import { UpdateUnitManagementRuleService } from '../common-services/update-unit-management-rule.service';
import { FilingHoldingSchemeNode } from '../models/node.interface';
import { NodeData } from '../models/nodedata.interface';
import { ActionsRules } from '../models/ruleAction.interface';
import { SearchCriteriaEltements, SearchCriteriaHistory } from '../models/search-criteria-history.interface';
import {
  ArchiveSearchResultFacets,
  CriteriaValue,
  PagedResult,
  SearchCriteria,
  SearchCriteriaCategory,
  SearchCriteriaEltDto,
  SearchCriteriaMgtRuleEnum,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum,
} from '../models/search.criteria';
import { Unit } from '../models/unit.interface';
import { ActionType } from './action-type.enum';
import { ReclassificationComponent } from './reclassification/reclassification.component';
import { SearchCriteriaSaverComponent } from './search-criteria-saver/search-criteria-saver.component';

const PAGE_SIZE = 10;
const FILTER_DEBOUNCE_TIME_MS = 400;
const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';
const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';

@Component({
  selector: 'app-archive-search',
  templateUrl: './archive-search.component.html',
  styleUrls: ['./archive-search.component.scss'],
})
export class ArchiveSearchComponent implements OnInit, OnChanges, OnDestroy {
  ActionType = ActionType;

  constructor(
    private archiveService: ArchiveService,
    private archiveFacetsService: ArchiveFacetsService,
    private translateService: TranslateService,
    private route: ActivatedRoute,
    private archiveExchangeDataService: ArchiveSharedDataService,
    public snackBar: MatSnackBar,
    public dialog: MatDialog,
    private router: Router,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private archiveHelperService: ArchiveSearchHelperService,
    private logger: Logger,
    private updateUnitManagementRuleService: UpdateUnitManagementRuleService,
    private archiveUnitEliminationService: ArchiveUnitEliminationService,
    private computeInheritedRulesService: ComputeInheritedRulesService,
    private archiveUnitDipService: ArchiveUnitDipService
  ) {
    this.subscriptionEntireNodes = this.archiveExchangeDataService.getEntireNodes().subscribe((nodes) => {
      this.entireNodesIds = nodes;
    });

    this.subscriptionNodes = this.archiveExchangeDataService.getNodes().subscribe((node) => {
      if (node.checked) {
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
          false
        );
      } else {
        node.count = null;
        this.removeCriteria('NODE', { id: node.id, value: node.id }, false);
      }
    });

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

    this.archiveExchangeDataService.receiveRemoveFromChildSearchCriteriaSubject().subscribe((criteria) => {
      if (criteria) {
        if (criteria.valueElt) {
          this.removeCriteria(criteria.keyElt, criteria.valueElt, false);
        } else {
          this.removeCriteriaAllValues(criteria.keyElt, false);
        }
      }
    });

    this.archiveExchangeDataService.receiveAppraisalSearchCriteriaSubject().subscribe((criteria) => {
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

    this.archiveExchangeDataService.receiveAccessSearchCriteriaSubject().subscribe((criteria) => {
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
    this.archiveService.getOntologiesFromJson().subscribe((data: any) => {
      this.ontologies = data;
      this.ontologies.sort((a: any, b: any) => {
        const shortNameA = a.Label;
        const shortNameB = b.Label;
        return shortNameA < shortNameB ? -1 : shortNameA > shortNameB ? 1 : 0;
      });
    });
  }

  DEFAULT_RESULT_THRESHOLD = 10000;

  direction = Direction.ASCENDANT;
  @Input()
  accessContract: string;
  @Output() archiveUnitClick = new EventEmitter<any>();

  tenantIdentifier: number;
  nodeData: NodeData;
  searchCriterias: Map<string, SearchCriteria>;
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly orderChange = new Subject<string>();

  orderBy = 'Title';
  isIndeterminate: boolean;
  isAllchecked: boolean;
  hasResults = false;
  hasAppraisalRulesCriteria = false;

  show = true;
  hasDipExportRole = false;
  hasTransferRequestRole = false;
  hasUpdateManagementRuleRole = false;
  @Input()
  hasAccessContractManagementPermissions: boolean;
  hasEliminationAnalysisOrActionRole = false;
  hasComputedInheritedRulesRole = false;
  hasReclassificationRole = false;
  waitingToGetFixedCount = false;
  showDuaEndDate = false;
  pending = false;
  pendingComputeFacets = false;
  submited = false;
  pendingGetFixedCount = false;
  submitedGetFixedCount = false;
  included = false;
  canLoadMore = false;
  showCriteriaPanel = true;
  showSearchCriteriaPanel = false;
  defaultFacetTabIndex = 1;
  currentPage = 0;
  pageNumbers = 0;
  totalResults = 0;
  itemSelected = 0;
  itemNotSelected = 0;
  numberOfHoldingUnitType = 0;
  numberOfHoldingUnitTypeOnComputedRules = 0;
  additionalSearchCriteriaCategoryIndex = 0;
  nbQueryCriteria = 0;

  listOfUAIdToInclude: CriteriaValue[] = [];
  listOfUAIdToExclude: CriteriaValue[] = [];
  nodeArray: FilingHoldingSchemeNode[] = [];
  entireNodesIds: string[];
  archiveUnits: Unit[];
  searchCriteriaHistory: SearchCriteriaHistory[] = [];
  criteriaSearchList: SearchCriteriaEltDto[] = [];
  listOfUACriteriaSearch: SearchCriteriaEltDto[] = [];
  searchCriteriaKeys: string[];
  additionalSearchCriteriaCategories: SearchCriteriaCategory[];

  openDialogSubscription: Subscription;
  showConfirmBigNumberOfResultsSuscription: Subscription;
  updateArchiveUnitAlerteMessageDialogSubscription: Subscription;
  reclassificationAlerteMessageDialogSubscription: Subscription;
  subscriptionNodes: Subscription;
  subscriptionSimpleSearchCriteriaAdd: Subscription;
  subscriptionEntireNodes: Subscription;
  subscriptionFilingHoldingSchemeNodes: Subscription;

  eliminationAnalysisResponse: any;
  eliminationActionResponse: any;
  rulesFacetsCanBeComputed = false;
  rulesFacetsComputed = false;
  showingFacets = false;
  isComputedRulesFacets: boolean;
  ontologies: any;
  selectedItemCount: boolean;

  archiveUnitGuidSelected: string;
  archiveUnitAllunitup: string[];
  hasAccessContractManagementPermissionsMessage = '';
  @ViewChild('confirmSecondActionBigNumberOfResultsActionDialog', { static: true })
  confirmSecondActionBigNumberOfResultsActionDialog: TemplateRef<ArchiveSearchComponent>;

  @ViewChild('updateArchiveUnitAlerteMessageDialog', { static: true })
  updateArchiveUnitAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>;

  @ViewChild('reclassificationAlerteMessageDialog', { static: true })
  reclassificationAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>;

  @ViewChild('launchComputeInheritedRuleAlerteMessageDialog', { static: true })
  launchComputeInheritedRuleAlerteMessageDialog: TemplateRef<ArchiveSearchComponent>;
  archiveSearchResultFacets: ArchiveSearchResultFacets = new ArchiveSearchResultFacets();

  selectedCategoryChange(selectedCategoryIndex: number) {
    this.additionalSearchCriteriaCategoryIndex = selectedCategoryIndex;
  }

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
    this.additionalSearchCriteriaCategoryIndex = 0;
    this.additionalSearchCriteriaCategories = [];
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = +params.tenantIdentifier;
    });
    this.hasAccessContractManagementPermissionsMessage = this.translateService.instant('UNIT_UPDATE.NO_PERMISSION');
    this.searchCriterias = new Map();
    this.searchCriteriaKeys = [];
    const searchCriteriaChange = merge(this.orderChange, this.filterChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));
    searchCriteriaChange.subscribe(() => {
      this.submit();
    });

    this.checkUserHasRole(VitamuiRoles.ROLE_EXPORT_DIP, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_TRANSFER_REQUEST, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_ELIMINATION, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_UPDATE_MANAGEMENT_RULES, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_COMPUTED_INHERITED_RULES, +this.tenantIdentifier);
    this.checkUserHasRole(VitamuiRoles.ROLE_RECLASSIFICATION, +this.tenantIdentifier);
    const ruleActions: ActionsRules[] = [];
    this.managementRulesSharedDataService.emitRuleActions(ruleActions);
    this.managementRulesSharedDataService.emitManagementRules([]);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.accessContract) {
      this.show = true;
      this.archiveExchangeDataService.emitToggle(this.show);
    }
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
    this.reMapSearchCriteriaFromSearchCriteriaHistory(event);
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
      this.submited = false;
      this.showCriteriaPanel = true;
      this.showSearchCriteriaPanel = false;
      this.archiveUnits = [];
      this.archiveExchangeDataService.emitNodeTarget(null);
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

  containsWaitingToRecalculateInheritenceRuleCriteria() {
    return this.searchCriterias && this.searchCriterias.has('WAITING_RECALCULATE');
  }

  findDefaultFacetTab(): number {
    return this.archiveHelperService.findDefaultFacetTabIndex(this.searchCriterias);
  }

  submit() {
    this.initializeSelectionParams();
    this.archiveHelperService.buildNodesListForQUery(this.searchCriterias, this.criteriaSearchList);
    this.archiveHelperService.buildFieldsCriteriaListForQUery(this.searchCriterias, this.criteriaSearchList);

    for (var mgtRuleType in SearchCriteriaMgtRuleEnum) {
      this.archiveHelperService.buildManagementRulesCriteriaListForQuery(mgtRuleType, this.searchCriterias, this.criteriaSearchList);
    }
    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
      this.rulesFacetsComputed = false;
      this.rulesFacetsCanBeComputed = this.archiveHelperService.checkIfRulesFacetsCanBeComputed(this.searchCriterias);
      this.callVitamApiService(this.rulesFacetsCanBeComputed);
      this.showingFacets = this.rulesFacetsCanBeComputed;
    }
  }

  loadExactCount() {
    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
      this.pendingGetFixedCount = true;
      this.submitedGetFixedCount = true;
      this.archiveService.getTotalTrackHitsByCriteria(this.criteriaSearchList, this.accessContract).subscribe(
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

  prepareListOfUACriteriaSearch() {
    return this.archiveHelperService.prepareUAIdList(
      this.criteriaSearchList,
      this.listOfUAIdToInclude,
      this.listOfUAIdToExclude,
      this.isAllchecked,
      this.isIndeterminate
    );
  }

  getArchiveUnitType(archiveUnit: any) {
    if (archiveUnit) {
      return archiveUnit['#unitType'];
    }
  }

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

    this.archiveService.searchArchiveUnitsByCriteria(searchCriteria, this.accessContract).subscribe(
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

  private callVitamApiService(includeFacets: boolean) {
    if (includeFacets) {
      this.pendingComputeFacets = true;
      this.showingFacets = false;
    }
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
    this.archiveExchangeDataService.emitLastSearchCriteriaDtoSubject(searchCriteria);
    this.archiveService.searchArchiveUnitsByCriteria(searchCriteria, this.accessContract)
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
        if (this.totalResults === this.DEFAULT_RESULT_THRESHOLD) {
          this.waitingToGetFixedCount = true;
        } else {
          this.waitingToGetFixedCount = false;
        }
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
        this.canLoadMore = false;
        this.pending = false;
        if (includeFacets) {
          this.pendingComputeFacets = false;
          this.archiveExchangeDataService.emitFacets([]);
        }
        this.logger.error('Error message :', error.message);

        this.archiveHelperService.updateCriteriaStatus(
          this.searchCriterias,
          SearchCriteriaStatusEnum.IN_PROGRESS,
          SearchCriteriaStatusEnum.NOT_INCLUDED
        );
      }
    );
  }

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
      }
    });
  }

  setFilingHoldingScheme() {
    this.subscriptionFilingHoldingSchemeNodes = this.archiveExchangeDataService.getFilingHoldingNodes().subscribe((nodes) => {
      this.nodeArray = nodes;
    });
  }

  checkAllNodes(show: boolean) {
    this.archiveHelperService.recursiveCheck(this.nodeArray, show);
  }

  public reMapSearchCriteriaFromSearchCriteriaHistory(storedSearchCriteriaHistory: SearchCriteriaHistory) {
    this.setFilingHoldingScheme();
    this.checkAllNodes(false);

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
              ? this.translateService.instant('ARCHIVE_SEARCH.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.' + value.id)
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

  loadMore() {
    this.canLoadMore = this.currentPage < this.pageNumbers - 1;
    if (this.canLoadMore && !this.pending) {
      this.submited = true;
      this.currentPage = this.currentPage + 1;
      this.criteriaSearchList = [];
      for (var mgtRuleType in SearchCriteriaMgtRuleEnum) {
        this.archiveHelperService.buildManagementRulesCriteriaListForQuery(mgtRuleType, this.searchCriterias, this.criteriaSearchList);
      }

      if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
        this.callVitamApiService(false);
      }
    }
  }

  launchFacetsComputing() {
    if (!this.pendingComputeFacets && this.criteriaSearchList && this.criteriaSearchList.length > 0) {
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
    this.show = !hidden;
    this.archiveExchangeDataService.emitToggle(this.show);
  }

  ngOnDestroy() {
    this.subscriptionNodes?.unsubscribe();
    this.subscriptionEntireNodes?.unsubscribe();
    this.subscriptionFilingHoldingSchemeNodes?.unsubscribe();
    this.subscriptionSimpleSearchCriteriaAdd?.unsubscribe();
    this.openDialogSubscription?.unsubscribe();
    this.showConfirmBigNumberOfResultsSuscription?.unsubscribe();
    this.updateArchiveUnitAlerteMessageDialogSubscription?.unsubscribe();
    this.reclassificationAlerteMessageDialogSubscription?.unsubscribe();
  }

  exportArchiveUnitsToCsvFile() {
    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
      this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
      const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
      const searchCriteria = {
        criteriaList: this.listOfUACriteriaSearch,
        pageNumber: this.currentPage,
        size: PAGE_SIZE,
        sortingCriteria,
        language: this.translateService.currentLang,
      };
      this.archiveService.exportCsvSearchArchiveUnitsByCriteria(searchCriteria, this.accessContract);
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
    this.setFilingHoldingScheme();
    this.archiveExchangeDataService.emitFilingHoldingNodes(this.nodeArray);
    this.checkAllNodes(false);
  }

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
    this.isAllchecked = false;
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
        case VitamuiRoles.ROLE_UPDATE_MANAGEMENT_RULES:
          this.hasUpdateManagementRuleRole = result;
          break;
        case VitamuiRoles.ROLE_COMPUTED_INHERITED_RULES:
          this.hasComputedInheritedRulesRole = result;
          break;
        case VitamuiRoles.ROLE_RECLASSIFICATION:
          this.hasReclassificationRole = result;
          break;
        default:
          break;
      }
    });
  }

  launchReclassification() {
    if (this.itemSelected > 1) {
      const dialogToOpen = this.reclassificationAlerteMessageDialog;
      const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
      this.reclassificationAlerteMessageDialogSubscription = dialogRef
        .afterClosed()
        .pipe(filter((result) => !!result))
        .subscribe(() => {});
    } else if (this.itemSelected === 1) {
      this.archiveUnitGuidSelected = this.isAllchecked ? this.archiveUnits[0]['#id'] : this.listOfUAIdToInclude[0].id;
      this.archiveUnitAllunitup = this.archiveUnits.find((archiveUnit) => archiveUnit['#id'] === this.archiveUnitGuidSelected)['#unitups'];
      this.listOfUACriteriaSearch = this.prepareListOfUACriteriaSearch();
      const reclassificationCriteria = {
        criteriaList: this.listOfUACriteriaSearch,
        pageNumber: this.currentPage,
        size: PAGE_SIZE,
        language: this.translateService.currentLang,
      };

      const dialogRef = this.dialog.open(ReclassificationComponent, {
        panelClass: 'vitamui-modal',
        disableClose: false,
        data: {
          itemSelected: this.itemSelected,
          reclassificationCriteria,
          accessContract: this.accessContract,
          tenantIdentifier: this.tenantIdentifier,
          archiveUnitGuidSelected: this.archiveUnitGuidSelected,
          archiveUnitAllunitup: this.archiveUnitAllunitup,
        },
      });
      this.openDialogSubscription = dialogRef.afterClosed().subscribe((result) => {
        if (result) {
          return;
        }
      });
    }
  }

  selectedItemCountKnown(): boolean {
    return !this.waitingToGetFixedCount || !this.isAllchecked;
  }

  async prepareToLaunchVitamAction() {
    if (!this.selectedItemCountKnown()) {
      if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
        this.pendingGetFixedCount = true;
        this.submitedGetFixedCount = true;
        const exactCountResults: number = await this.archiveService
          .getTotalTrackHitsByCriteria(this.criteriaSearchList, this.accessContract)
          .toPromise();
        if (exactCountResults !== -1) {
          this.totalResults = exactCountResults;
          if (this.isAllchecked) {
            this.itemSelected = this.totalResults - this.itemNotSelected;
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

  async launchComputedInheritedRulesModal() {
    await this.prepareToLaunchVitamAction();
    this.computeInheritedRulesService.launchComputedInheritedRulesModal(
      this.listOfUACriteriaSearch,
      this.accessContract,
      this.numberOfHoldingUnitTypeOnComputedRules,
      this.tenantIdentifier,
      this.currentPage,
      this.launchComputeInheritedRuleAlerteMessageDialog,
      this.confirmSecondActionBigNumberOfResultsActionDialog
    );
  }

  async goToUpdateManagementRule() {
    await this.prepareToLaunchVitamAction();
    this.updateUnitManagementRuleService.goToUpdateManagementRule(
      this.listOfUACriteriaSearch,
      this.criteriaSearchList,
      this.accessContract,
      this.currentPage,
      this.tenantIdentifier,
      this.numberOfHoldingUnitType,
      this.router,
      this.itemSelected,
      this.updateArchiveUnitAlerteMessageDialogSubscription,
      this.updateArchiveUnitAlerteMessageDialog,
      this.confirmSecondActionBigNumberOfResultsActionDialog
    );
  }

  async launchEliminationAnalysisModal() {
    await this.prepareToLaunchVitamAction();
    this.selectedItemCount = this.selectedItemCountKnown();
    this.archiveUnitEliminationService.launchEliminationAnalysisModal(
      this.listOfUACriteriaSearch,
      this.eliminationAnalysisResponse,
      this.accessContract,
      this.selectedItemCount,
      this.itemSelected,
      this.tenantIdentifier,
      this.currentPage,
      this.confirmSecondActionBigNumberOfResultsActionDialog,
      this.showConfirmBigNumberOfResultsSuscription
    );
  }

  async launchEliminationModal() {
    await this.prepareToLaunchVitamAction();
    this.archiveUnitEliminationService.launchEliminationModal(
      this.listOfUACriteriaSearch,
      this.eliminationActionResponse,
      this.accessContract,
      this.tenantIdentifier,
      this.currentPage,
      this.confirmSecondActionBigNumberOfResultsActionDialog
    );
  }

  async launchExportDipModal() {
    await this.prepareToLaunchVitamAction();
    const selectedItemCount = this.selectedItemCountKnown();
    this.archiveUnitDipService.launchExportDipModal(
      this.listOfUACriteriaSearch,
      selectedItemCount,
      this.accessContract,
      this.tenantIdentifier,
      this.itemSelected,
      this.currentPage,
      this.isAllchecked,
      this.confirmSecondActionBigNumberOfResultsActionDialog
    );
  }

  async launchTransferRequestModal() {
    await this.prepareToLaunchVitamAction();
    const selectedItemCount = this.selectedItemCountKnown();
    this.archiveUnitDipService.launchTransferRequestModal(
      this.listOfUACriteriaSearch,
      selectedItemCount,
      this.accessContract,
      this.tenantIdentifier,
      this.itemSelected,
      this.currentPage,
      this.isAllchecked,
      this.confirmSecondActionBigNumberOfResultsActionDialog
    );
  }
}
