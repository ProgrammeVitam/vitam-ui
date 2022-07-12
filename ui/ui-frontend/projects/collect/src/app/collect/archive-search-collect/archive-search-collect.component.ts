import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { FilingHoldingSchemeNode } from 'projects/archive-search/src/app/archive/models/node.interface';
import { ArchiveSharedDataService } from 'projects/archive-search/src/app/core/archive-shared-data.service';
import { SearchCriteriaEltements, SearchCriteriaHistory } from 'projects/vitamui-library/src/lib/models/search-criteria-history.interface';
import { Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { AccessContract, Direction, ExternalParameters, ExternalParametersService} from 'ui-frontend-common';
import { Unit } from 'vitamui-library/lib/models/unit.interface';
import { CriteriaValue, PagedResult, SearchCriteria, SearchCriteriaCategory, SearchCriteriaEltDto, SearchCriteriaTypeEnum } from '../models/search.criteria';
import { ArchiveUnitCollectService } from '../archive-unit-collect.service';
import { HttpErrorResponse } from '@angular/common/http';

const PAGE_SIZE = 10;
//const FILTER_DEBOUNCE_TIME_MS = 400;
const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';
//const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';

@Component({
  selector: 'archive-search-collect',
  templateUrl: './archive-search-collect.component.html',
  styleUrls: ['./archive-search-collect.component.scss']
})
export class ArchiveSearchCollectComponent implements OnInit, OnDestroy {

  accessContract: string;
  @Output()
  archiveUnitClick = new EventEmitter<any>();

  accessContractSub: Subscription;
  errorMessageSub: Subscription;

  projectId: string;
  foundAccessContract = false;
  hasAccessContractManagementPermissions = false;
  isLPExtended = false;

  // Criteria properties
  subscriptionEntireNodes: Subscription;
  subscriptionNodes: Subscription;
  subscriptionSimpleSearchCriteriaAdd: Subscription;
  searchCriteriaKeys: string[];
  searchCriterias: Map<string, SearchCriteria>;
  nbQueryCriteria = 0;
  additionalSearchCriteriaCategoryIndex = 0;
  entireNodesIds: string[];
  nodeArray: FilingHoldingSchemeNode[] = [];
  criteriaSearchList: SearchCriteriaEltDto[] = [];
  additionalSearchCriteriaCategories: SearchCriteriaCategory[];
  included = false;
  showCriteriaPanel = true;
  showSearchCriteriaPanel = false;
  archiveUnits: Unit[];
  ontologies: any;

  // AU Search Properties
  pending = false;
  submited = false;
  currentPage = 0
  itemSelected = 0;
  itemNotSelected = 0;
  isIndeterminate: boolean;
  isAllchecked: boolean;
  waitingToGetFixedCount = false;
  pendingGetFixedCount = false;
  totalResults = 0;
  orderBy = 'Title';
  direction = Direction.ASCENDANT;
  DEFAULT_RESULT_THRESHOLD = 10000;
  hasResults = false;
  pageNumbers = 0;
  canLoadMore = false;


  constructor(private route: ActivatedRoute,
    private externalParameterService: ExternalParametersService, private translateService: TranslateService,
    private archiveUnitCollectService: ArchiveUnitCollectService, private snackBar: MatSnackBar, private archiveExchangeDataService: ArchiveSharedDataService,
  ) {

    this.subscriptionEntireNodes = this.archiveExchangeDataService.getEntireNodes().subscribe((nodes) => {
      this.entireNodesIds = nodes;
    });

    this.subscriptionNodes = this.archiveExchangeDataService.getNodes().subscribe((node) => {
      if (node.checked) {
        /**
         * TODO
         
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
        */
      } else {
        node.count = null;
        this.removeCriteria('NODE', { id: node.id, value: node.id }, false);
      }
    });

    this.subscriptionSimpleSearchCriteriaAdd = this.archiveExchangeDataService
      .receiveSimpleSearchCriteriaSubject()
      .subscribe((criteria) => {
        if (criteria) {
          /**
        * TODO
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
          */
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
        /**
        * TODO
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
       */
      }
    });

    this.archiveExchangeDataService.receiveAccessSearchCriteriaSubject().subscribe((criteria) => {
      if (criteria) {
        /**
        * TODO
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
       */
      }
    });
    // this.archiveUnitCollectService.getOntologiesFromJson().subscribe((data: any) => {
    //   this.ontologies = data;
    //   this.ontologies.sort((a: any, b: any) => {
    //     const shortNameA = a.Label;
    //     const shortNameB = b.Label;
    //     return shortNameA < shortNameB ? -1 : shortNameA > shortNameB ? 1 : 0;
    //   });
    // });
  }
  ngOnDestroy(): void {
    throw new Error('Method not implemented.');
  }

  ngOnInit(): void {
    debugger;
    this.route.params.subscribe((params) => {
      this.projectId = params.id;
    });

    this.searchCriteriaKeys = [];
    this.searchCriterias = new Map();

    this.fetchUserAccessContractFromExternalParameters();
    this.submit();
  }

  fetchUserAccessContractFromExternalParameters() {
    this.accessContractSub = this.externalParameterService.getUserExternalParameters().subscribe((parameters) => {
      const accessConctractId: string = parameters.get(ExternalParameters.PARAM_ACCESS_CONTRACT);
      if (accessConctractId && accessConctractId.length > 0) {
        this.accessContract = accessConctractId;
        this.foundAccessContract = true;
        this.fetchVitamAccessContract();
        if (this.archiveUnits.length === 0) {
          this.callVitamApiService(false);
        }
      } else {
        this.errorMessageSub = this.translateService
          .get('ARCHIVE_SEARCH.ACCESS_CONTRACT_NOT_FOUND')
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
    this.archiveUnitCollectService.getAccessContractById(this.accessContract).subscribe(
      (ac: AccessContract) => {
        this.hasAccessContractManagementPermissions = this.archiveUnitCollectService.hasAccessContractManagementPermissions(ac);
      },
      (error: any) => {
        console.error('AccessContract not found', error);
        const message = this.translateService.instant('COLLECT.ACCESS_CONTRACT_NOT_FOUND_IN_VITAM');
        this.snackBar.open(message + ': ' + this.accessContract, null, {
          panelClass: 'vitamui-snack-bar',
          duration: 10000,
        });
      }
    );
  }

  showStoredSearchCriteria(event: SearchCriteriaHistory) {
    if (this.searchCriterias.size > 0) {
      this.searchCriterias = new Map();
      this.searchCriteriaKeys = [];
      this.included = false;
    }
    this.reMapSearchCriteriaFromSearchCriteriaHistory(event);
  }

  public reMapSearchCriteriaFromSearchCriteriaHistory(storedSearchCriteriaHistory: SearchCriteriaHistory) {
    // TODO
    //this.setFilingHoldingScheme();
    this.checkAllNodes(false);

    storedSearchCriteriaHistory.searchCriteriaList.forEach((criteria: SearchCriteriaEltements) => {
      this.fillTreeNodeAsSearchCriteriaHistory(criteria);
      const c = criteria.criteria;
      console.log("------" + c);
      criteria.values.forEach((value) => {
        if (
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.APPRAISAL_RULE] ||
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.NODES] ||
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.ACCESS_RULE] ||
          criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.STORAGE_RULE]
        ) {
          this.addCriteriaCategory(criteria.category);
          console.log("****" + value);
          /**
        * TODO
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
         */
        } else if (criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS]) {
          /**
        * TODO
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
         */
        }
      });
    });
  }

  fillTreeNodeAsSearchCriteriaHistory(searchCriteriaList: SearchCriteriaEltements) {
    if (searchCriteriaList && searchCriteriaList.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.NODES]) {
      searchCriteriaList.values.forEach((nodeId) => {
        console.log("****" + nodeId);
        /**
        * TODO
       this.archiveHelperService.fillNodeTitle(
         this.nodeArray,
         nodeId.value,
         this.searchCriterias,
         this.searchCriteriaKeys,
         this.nbQueryCriteria
       );
       */
      });
      this.nodeArray = null;
      this.archiveExchangeDataService.emitToggle(true);
    }
  }

  checkAllNodes(show: boolean) {
    console.log("****" + show);
    //this.archiveHelperService.recursiveCheck(this.nodeArray, show);
  }

  addCriteriaCategory(categoryName: string) {
    this.archiveExchangeDataService.emitRuleCategory(categoryName);
    const indexOfCategory = this.additionalSearchCriteriaCategories.findIndex((element) => element.name === categoryName);
    if (indexOfCategory === -1) {
      this.additionalSearchCriteriaCategories.push({
        name: categoryName,
        index: this.additionalSearchCriteriaCategories.length + 1
      });
      this.additionalSearchCriteriaCategories.forEach((category, index) => {
        category.index = index + 1;
      });
      this.additionalSearchCriteriaCategoryIndex = this.additionalSearchCriteriaCategories.length;
    }
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

  removeCriteria(keyElt: string, valueElt: CriteriaValue, emit: boolean) {
    console.log("****" + keyElt + "-" + valueElt + "-" + emit);
    // this.archiveHelperService.removeCriteria(keyElt, valueElt, emit, this.searchCriteriaKeys, this.searchCriterias, this.nbQueryCriteria);

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

  // SEARCH DATA

  submit() {
    this.initializeSelectionParams();
    /**
    this.archiveHelperService.buildNodesListForQUery(this.searchCriterias, this.criteriaSearchList);
    this.archiveHelperService.buildFieldsCriteriaListForQUery(this.searchCriterias, this.criteriaSearchList);
    this.archiveHelperService.buildManagementRulesCriteriaListForQuery('APPRAISAL_RULE', this.searchCriterias, this.criteriaSearchList);
    this.archiveHelperService.buildManagementRulesCriteriaListForQuery('STORAGE_RULE', this.searchCriterias, this.criteriaSearchList);
    this.archiveHelperService.buildManagementRulesCriteriaListForQuery('ACCESS_RULE', this.searchCriterias, this.criteriaSearchList);
    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
      this.rulesFacetsComputed = false;
      this.rulesFacetsCanBeComputed = this.archiveHelperService.checkIfRulesFacetsCanBeComputed(this.searchCriterias);
      this.callVitamApiService(this.rulesFacetsCanBeComputed);
      this.showingFacets = this.rulesFacetsCanBeComputed;
    }
     */
  }

  private initializeSelectionParams() {
    console.log("*****initializeSelectionParams******");
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

  loadMore() {
    this.canLoadMore = this.currentPage < this.pageNumbers - 1;
    if (this.canLoadMore && !this.pending) {
      this.submited = true;
      this.currentPage = this.currentPage + 1;
      this.criteriaSearchList = [];
      if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
        this.callVitamApiService(false);
      }
    }
  }

  private callVitamApiService(includeFacets: boolean) {
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
    this.archiveUnitCollectService.searchArchiveUnitsByCriteria(searchCriteria, this.projectId, this.accessContract).subscribe(
      (pagedResult: PagedResult) => {
        if (this.currentPage === 0) {
          this.archiveUnits = pagedResult.results;
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
        // this.archiveHelperService.updateCriteriaStatus(
        //   this.searchCriterias,
        //   SearchCriteriaStatusEnum.IN_PROGRESS,
        //   SearchCriteriaStatusEnum.INCLUDED
        // );
        this.pending = false;
        this.included = true;
        console.log("------------------------------------------");
      },
      (error: HttpErrorResponse) => {
        console.log(error)
        this.canLoadMore = false;
        this.pending = false;
      }
    );
  }

  loadExactCount() {
    console.log("*****loadExactCount******");
    /**
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
     */
  }
}
