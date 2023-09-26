/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, TemplateRef, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { merge, Subject, Subscription } from 'rxjs';
import { debounceTime, filter } from 'rxjs/operators';
import { CriteriaDataType, CriteriaOperator, Direction, StartupService } from 'ui-frontend-common';
import { ArchiveSharedDataServiceService } from '../../core/archive-shared-data-service.service';
import { ArchiveService } from '../archive.service';
import { FilingHoldingSchemeNode } from '../models/node.interface';
import { NodeData } from '../models/nodedata.interface';
import { SearchCriteriaEltements, SearchCriteriaHistory } from '../models/search-criteria-history.interface';
import {
  CriteriaValue,
  PagedResult,
  SearchCriteria,
  SearchCriteriaCategory,
  SearchCriteriaEltDto,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum,
  SearchCriteriaValue,
} from '../models/search.criteria';
import { Unit } from '../models/unit.interface';
import { VitamUISnackBarComponent } from '../shared/vitamui-snack-bar';
import { DipRequestCreateComponent } from './dip-request-create/dip-request-create.component';
import { SearchCriteriaSaverComponent } from './search-criteria-saver/search-criteria-saver.component';

const MAX_DIP_EXPORT_THRESHOLD = 10000;
const MAX_ELIMINATION_ACTION_THRESHOLD = 10000;
const BUTTON_MAX_TEXT = 40;
const DESCRIPTION_MAX_TEXT = 60;
const PAGE_SIZE = 10;
const FILTER_DEBOUNCE_TIME_MS = 400;
const MAX_ELIMINATION_ANALYSIS_THRESHOLD = 10000;
const ELIMINATION_TECHNICAL_ID = 'ELIMINATION_TECHNICAL_ID';

@Component({
  selector: 'app-archive-search',
  templateUrl: './archive-search.component.html',
  styleUrls: ['./archive-search.component.scss'],
})
export class ArchiveSearchComponent implements OnInit, OnChanges, OnDestroy {
  constructor(
    private archiveService: ArchiveService,
    private translateService: TranslateService,
    private route: ActivatedRoute,
    private archiveExchangeDataService: ArchiveSharedDataServiceService,
    public snackBar: MatSnackBar,
    public dialog: MatDialog,
    private startupService: StartupService
  ) {
    this.subscriptionEntireNodes = this.archiveExchangeDataService.getEntireNodes().subscribe((nodes) => {
      this.entireNodesIds = nodes;
    });

    this.subscriptionNodes = this.archiveExchangeDataService.getNodes().subscribe((node) => {
      if (node.checked) {
        this.addCriteria(
          'NODE',
          { id: node.id, value: node.id },
          node.title,
          true,
          'EQ',
          SearchCriteriaTypeEnum.NODES,
          false,
          'STRING',
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
          this.addCriteria(
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
        this.addCriteria(
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
      this.ontologies.sort(function (a: any, b: any) {
        const shortNameA = a.Label;
        const shortNameB = b.Label;
        return shortNameA < shortNameB ? -1 : shortNameA > shortNameB ? 1 : 0;
      });
    });
  }
  @Output() archiveUnitClick = new EventEmitter<any>();

  private readonly orderChange = new Subject<string>();
  orderBy = 'Title';
  direction = Direction.ASCENDANT;
  @Input()
  accessContract: string;
  nbQueryCriteria = 0;
  subscriptionNodes: Subscription;
  subscriptionSimpleSearchCriteriaAdd: Subscription;
  subscriptionEntireNodes: Subscription;
  subscriptionFilingHoldingSchemeNodes: Subscription;
  currentPage = 0;
  pageNumbers = 0;
  totalResults = 0;
  pending = false;
  included = false;
  canLoadMore = false;
  tenantIdentifier: string;
  appraisalRuleCriteriaForm: FormGroup;
  submited = false;
  searchCriterias: Map<string, SearchCriteria>;
  searchCriteriaKeys: string[];
  otherCriteriaValueEnabled = false;
  otherCriteriaValueType = 'DATE';
  showCriteriaPanel = true;
  showSearchCriteriaPanel = false;
  selectedValueOntolonogy: any;
  archiveUnits: Unit[];
  ontologies: any;
  filterMapType: { [key: string]: string[] } = {
    status: ['Folder', 'Document'],
  };
  shouldShowPreviewArchiveUnit = false;

  criteriaSearchList: SearchCriteriaEltDto[] = [];
  listOfUACriteriaSearch: SearchCriteriaEltDto[] = [];

  additionalSearchCriteriaCategories: SearchCriteriaCategory[];
  additionalSearchCriteriaCategoryIndex = 0;
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  showDuaEndDate = false;
  searchCriteriaHistory: SearchCriteriaHistory[] = [];
  searchCriteriaHistoryToSave: Map<string, SearchCriteriaHistory>;
  searchCriteriaHistoryLength: number = null;
  hasResults = false;

  show = true;
  showUnitPreviewBlock = false;
  nodeArray: FilingHoldingSchemeNode[] = [];
  nodeData: NodeData;
  entireNodesIds: string[];

  itemSelected = 0;
  itemNotSelected = 0;
  isAllchecked: boolean;
  listOfUAIdToInclude: CriteriaValue[] = [];
  listOfUAIdToExclude: CriteriaValue[] = [];
  isIndeterminate: boolean;
  hasDipExportRole = false;
  hasEliminationActionRole = false;
  hasEliminationAnalysisRole = false;
  openDialogSubscription: Subscription;
  analysisliminationSuscription: Subscription;
  showConfirmEliminationSuscription: Subscription;
  actionliminationSuscription: Subscription;

  eliminationAnalysisResponse: any;
  eliminationActionResponse: any;

  @ViewChild('confirmEliminationActionDialog', { static: true }) confirmEliminationActionDialog: TemplateRef<ArchiveSearchComponent>;

  selectedCategoryChange(selectedCategoryIndex: number) {
    this.additionalSearchCriteriaCategoryIndex = selectedCategoryIndex;
  }

  addCriteriaCategory(categoryName: string) {
    const indexOfCategory = this.additionalSearchCriteriaCategories.findIndex((element) => element.name === categoryName);
    if (indexOfCategory === -1) {
      this.additionalSearchCriteriaCategories.push({ name: categoryName, index: this.additionalSearchCriteriaCategories.length + 1 });

      this.additionalSearchCriteriaCategories.forEach((category, index) => {
        category.index = index + 1;
      });
      this.additionalSearchCriteriaCategoryIndex = this.additionalSearchCriteriaCategories.length;
    }
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
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.searchCriterias = new Map();
    this.searchCriteriaKeys = [];
    this.filterMapType.Type = ['Folder', 'Document'];

    const searchCriteriaChange = merge(this.orderChange, this.filterChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      this.submit();
    });
    this.checkUserHasRole('DIPExport', 'ROLE_EXPORT_DIP', +this.tenantIdentifier);
    this.checkUserHasRole('EliminationAnalysis', 'ROLE_ELIMINATION', +this.tenantIdentifier);
    this.checkUserHasRole('EliminationAction', 'ROLE_ELIMINATION', +this.tenantIdentifier);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.accessContract) {
      this.show = true;
      this.archiveExchangeDataService.emitToggle(this.show);
    }
  }

  onFilterChange(key: string, values: any[]) {
    this.filterMapType[key] = values;
    this.filterChange.next(this.filterMapType);
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
    if (this.searchCriterias && this.searchCriterias.size > 0) {
      this.searchCriterias.forEach((val, key) => {
        if (key === keyElt) {
          let values = val.values;
          values = values.filter((item) => item.value.id !== valueElt.id);
          if (values.length === 0) {
            this.searchCriteriaKeys.forEach((element, index) => {
              if (element == keyElt) {
                this.searchCriteriaKeys.splice(index, 1);
              }
            });
            this.searchCriterias.delete(keyElt);
          } else {
            val.values = values;
            this.searchCriterias.set(keyElt, val);
          }
          this.nbQueryCriteria--;
          if (emit === true && key === 'NODE') {
            this.archiveExchangeDataService.emitNodeTarget(valueElt.value);
          }

          if (emit === true && val.category === SearchCriteriaTypeEnum.APPRAISAL_RULE) {
            this.archiveExchangeDataService.sendAppraisalFromMainSearchCriteriaAction({
              keyElt,
              valueElt,
              action: 'REMOVE',
            });
          }
        }
      });
    }

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
      if (category === 'APPRAISAL_RULE') {
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

  addCriteria(
    keyElt: string,
    valueElt: CriteriaValue,
    labelElt: string,
    keyTranslated: boolean,
    operator: string,
    category: SearchCriteriaTypeEnum,
    valueTranslated: boolean,
    dataType: string,
    emit: boolean
  ) {
    if (keyElt && valueElt) {
      if (this.searchCriterias) {
        this.nbQueryCriteria++;
        let criteria: SearchCriteria;
        if (this.searchCriterias.has(keyElt)) {
          criteria = this.searchCriterias.get(keyElt);
          let values = criteria.values;
          if (!values || values.length === 0) {
            values = [];
          }

          const filtredValues = values.filter((elt) =>
            criteria.dataType === 'STRING' || criteria.dataType === 'DATE'
              ? elt.value.value === valueElt.value
              : elt.value.beginInterval === valueElt.beginInterval && elt.value.endInterval === valueElt.endInterval
          );
          if (filtredValues.length === 0) {
            values.push({
              value: valueElt,
              label: labelElt,
              valueShown: true,
              status: SearchCriteriaStatusEnum.NOT_INCLUDED,
              keyTranslated,
              valueTranslated,
            });
            criteria.values = values;
            this.searchCriterias.set(keyElt, criteria);
          }
        } else {
          if (this.searchCriteriaKeys.indexOf(keyElt) === -1) {
            if (category === SearchCriteriaTypeEnum.NODES) {
              this.searchCriteriaKeys.unshift(keyElt);
            } else {
              this.searchCriteriaKeys.push(keyElt);
            }
          }
          const values = [];
          values.push({
            value: valueElt,
            label: labelElt,
            id: valueElt.id,
            valueShown: true,
            status: SearchCriteriaStatusEnum.NOT_INCLUDED,
            keyTranslated,
            valueTranslated,
          });
          const criteria = {
            key: keyElt,
            values,
            operator,
            category,
            keyTranslated,
            valueTranslated,
            dataType,
          };
          this.searchCriterias.set(keyElt, criteria);
        }
        if (emit === true && category === SearchCriteriaTypeEnum.APPRAISAL_RULE) {
          this.archiveExchangeDataService.sendAppraisalFromMainSearchCriteriaAction({ keyElt, valueElt, action: 'ADD' });
        }
      }
    }
  }

  submit() {
    this.pending = true;
    this.submited = true;
    this.showCriteriaPanel = false;
    this.showSearchCriteriaPanel = false;
    this.currentPage = 0;
    this.archiveUnits = [];
    this.criteriaSearchList = [];
    this.initializeSelectionParams();
    this.buildNodesListForQUery();
    this.buildFieldsCriteriaListForQUery();
    this.buildAppraisalCriteriaListForQUery();
    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
      this.callVitamApiService();
    }
  }

  getNodesId(): CriteriaValue[] {
    const nodesIdList: CriteriaValue[] = [];
    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      if (criteria.key === 'NODE') {
        criteria.values.forEach((elt) => {
          nodesIdList.push(elt.value);
        });
      }
    });
    return nodesIdList;
  }

  buildNodesListForQUery() {
    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      if (criteria.category === SearchCriteriaTypeEnum.NODES) {
        const strValues: CriteriaValue[] = [];
        criteria.values.forEach((elt) => {
          strValues.push(elt.value);
        });
        this.updateCriteriaStatus(SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);
        this.criteriaSearchList.push({
          criteria: 'NODE',
          values: strValues,
          operator: criteria.operator,
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.NODES],
          dataType: criteria.dataType,
        });
      }
    });
  }

  buildFieldsCriteriaListForQUery() {
    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      if (criteria.category === SearchCriteriaTypeEnum.FIELDS) {
        this.updateCriteriaStatus(SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);
        this.criteriaSearchList.push({
          criteria: criteria.key,
          values: criteria.values.map((elt: SearchCriteriaValue) => elt.value),
          operator: criteria.operator,
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
          dataType: criteria.dataType,
        });
      }
    });

    if (this.filterMapType.Type.length != 2) {
      const typesFilterValues: CriteriaValue[] = [];

      this.filterMapType.Type.forEach((filter) => {
        if (filter === 'Document') {
          typesFilterValues.push({ id: 'Item', value: 'Item' });
        } else if (filter === 'Folder') {
          typesFilterValues.push({ id: 'RecordGrp', value: 'RecordGrp' });
          typesFilterValues.push({ id: 'File', value: 'File' });
          typesFilterValues.push({ id: 'Subfonds', value: 'Subfonds' });
          typesFilterValues.push({ id: 'Class', value: 'Class' });
          typesFilterValues.push({ id: 'Subgrp', value: 'Subgrp' });
          typesFilterValues.push({ id: 'Otherlevel', value: 'Otherlevel' });
          typesFilterValues.push({ id: 'Series', value: 'Series' });
          typesFilterValues.push({ id: 'Subseries', value: 'Subseries' });
          typesFilterValues.push({ id: 'Collection', value: 'Collection' });
          typesFilterValues.push({ id: 'Fonds', value: 'Fonds' });
        }
      });

      this.criteriaSearchList.push({
        criteria: 'DescriptionLevel',
        values: typesFilterValues,
        operator: CriteriaOperator.EQ,
        category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
        dataType: CriteriaDataType.STRING,
      });
    }
  }

  buildAppraisalCriteriaListForQUery() {
    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      if (criteria.category === SearchCriteriaTypeEnum.APPRAISAL_RULE) {
        if (criteria.key === 'APPRAISAL_RULE_ORIGIN') {
          const originRuleCriteria: Map<CriteriaValue, string> = new Map();
          criteria.values.forEach((elt) => {
            originRuleCriteria.set(elt.value, 'true');
          });
          originRuleCriteria.forEach((value: string, key: CriteriaValue) => {
            this.criteriaSearchList.push({
              criteria: key.value,
              values: [...new Array({ id: value, value })],
              operator: 'EQ',
              category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.APPRAISAL_RULE],
              dataType: criteria.dataType,
            });
          });
        } else {
          const strValues: CriteriaValue[] = [];
          criteria.values.forEach((elt) => {
            strValues.push(elt.value);
          });
          this.criteriaSearchList.push({
            criteria: criteria.key,
            values: strValues,
            operator: criteria.operator,
            category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.APPRAISAL_RULE],
            dataType: criteria.dataType,
          });
        }
        this.updateCriteriaStatus(SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);
      }
    });
  }

  private callVitamApiService() {
    this.pending = true;

    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    const searchCriteria = {
      criteriaList: this.criteriaSearchList,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      sortingCriteria,
    };
    this.archiveService.searchArchiveUnitsByCriteria(searchCriteria, this.accessContract).subscribe(
      (pagedResult: PagedResult) => {
        if (this.currentPage === 0) {
          this.archiveUnits = pagedResult.results;
          this.archiveExchangeDataService.emitFacets(pagedResult.facets);
          this.hasResults = true;
        } else {
          if (pagedResult.results) {
            this.hasResults = true;
            pagedResult.results.forEach((elt) => this.archiveUnits.push(elt));
          }
        }

        this.pageNumbers = pagedResult.pageNumbers;
        this.totalResults = pagedResult.totalResults;
        if (this.isAllchecked) {
          this.itemSelected = this.totalResults - this.itemNotSelected;
        }
        this.canLoadMore = this.currentPage < this.pageNumbers - 1;
        this.updateCriteriaStatus(SearchCriteriaStatusEnum.IN_PROGRESS, SearchCriteriaStatusEnum.INCLUDED);
        this.pending = false;
        this.included = true;
      },
      (error: HttpErrorResponse) => {
        this.canLoadMore = false;
        this.pending = false;
        console.log('error : ', error.message);
        this.archiveExchangeDataService.emitFacets([]);
        this.updateCriteriaStatus(SearchCriteriaStatusEnum.IN_PROGRESS, SearchCriteriaStatusEnum.NOT_INCLUDED);
      }
    );
  }

  public mapSearchCriteriaHistory() {
    let _searchCriteriaHistory: SearchCriteriaHistory;

    let _criteriaList: SearchCriteriaEltements[] = [];

    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      const strValues: CriteriaValue[] = [];

      criteria.values.forEach((elt) => {
        strValues.push(elt.value);
      });
      _criteriaList.push({
        criteria: criteria.key,
        values: strValues,
        category: SearchCriteriaTypeEnum[criteria.category],
        operator: criteria.operator,
        keyTranslated: criteria.keyTranslated,
        valueTranslated: criteria.valueTranslated,
        dataType: criteria.dataType,
      });
    });

    _searchCriteriaHistory = {
      id: null,
      name: '',
      savingDate: new Date().toISOString(),
      searchCriteriaList: _criteriaList,
    };

    this.openCriteriaPopup(_searchCriteriaHistory);
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

  fillNodeTitle(nodeArray: FilingHoldingSchemeNode[], nodeId: string) {
    nodeArray.forEach((node) => {
      if (node.id === nodeId) {
        node.checked = true;
        node.hidden = false;
        this.addCriteria(
          'NODE',
          { id: nodeId, value: nodeId },
          node.title,
          true,
          'EQ',
          SearchCriteriaTypeEnum.NODES,
          false,
          'STRING',
          false
        );
      } else if (node.children.length > 0) {
        this.fillNodeTitle(node.children, nodeId);
      }
    });
  }
  setFilingHoldingScheme() {
    this.subscriptionFilingHoldingSchemeNodes = this.archiveExchangeDataService.getFilingHoldingNodes().subscribe((nodes) => {
      this.nodeArray = nodes;
    });
  }

  checkAllNodes(show: boolean) {
    this.recursiveCheck(this.nodeArray, show);
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

  public reMapSearchCriteriaFromSearchCriteriaHistory(storedSearchCriteriaHistory: SearchCriteriaHistory) {
    this.setFilingHoldingScheme();
    this.checkAllNodes(false);

    storedSearchCriteriaHistory.searchCriteriaList.forEach((criteria: SearchCriteriaEltements) => {
      this.fillTreeNodeAsSearchCriteriaHistory(criteria);
      const c = criteria.criteria;
      criteria.values.forEach((value) => {
        if (criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.APPRAISAL_RULE]) {
          this.addCriteriaCategory(SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.APPRAISAL_RULE]);
          if (criteria.dataType === 'INTERVAL') {
            this.addCriteria(
              c,
              value,
              value.value,
              criteria.keyTranslated,
              criteria.operator,
              SearchCriteriaTypeEnum.APPRAISAL_RULE,
              criteria.valueTranslated,
              criteria.dataType,
              true
            );
          } else {
            this.addCriteria(
              c,
              value,
              value.value,
              criteria.keyTranslated,
              criteria.operator,
              SearchCriteriaTypeEnum.APPRAISAL_RULE,
              criteria.valueTranslated,
              criteria.dataType,
              true
            );
          }
        } else if (criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.NODES]) {
          this.addCriteria(
            c,
            value,
            value.value,
            criteria.keyTranslated,
            criteria.operator,
            SearchCriteriaTypeEnum.NODES,
            criteria.valueTranslated,
            criteria.dataType,
            true
          );
        } else if (criteria.category === SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS]) {
          this.addCriteria(
            c,
            value,
            value.value,
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
        this.fillNodeTitle(this.nodeArray, nodeId.value);
      });
      this.nodeArray = null;
      this.archiveExchangeDataService.emitToggle(true);
    }
  }

  updateCriteriaStatus(oldStatusFilter: SearchCriteriaStatusEnum, newStatus: SearchCriteriaStatusEnum) {
    this.searchCriterias.forEach((value: SearchCriteria) => {
      value.values.forEach((elt) => {
        if (elt.status === oldStatusFilter) {
          elt.status = newStatus;
        }
      });
    });
  }

  getButtonSubText(originText: string): string {
    return this.getSubText(originText, BUTTON_MAX_TEXT);
  }

  getDescriptionSubText(originText: string): string {
    return this.getSubText(originText, DESCRIPTION_MAX_TEXT);
  }

  getSubText(originText: string, limit: number): string {
    let subText = originText;
    if (originText && originText.length > limit) {
      subText = originText.substring(0, limit) + '...';
    }
    return subText;
  }

  loadMore() {
    this.canLoadMore = this.currentPage < this.pageNumbers - 1;
    if (this.canLoadMore && !this.pending) {
      this.submited = true;
      this.currentPage = this.currentPage + 1;
      this.criteriaSearchList = [];
      this.buildFieldsCriteriaListForQUery();
      this.buildAppraisalCriteriaListForQUery();
      this.buildNodesListForQUery();
      if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
        this.callVitamApiService();
      }
    }
  }
  hiddenTreeBlock(hidden: boolean): void {
    this.show = !hidden;
    this.archiveExchangeDataService.emitToggle(this.show);
  }

  ngOnDestroy() {
    // unsubscribe to ensure no memory leaks
    this.subscriptionNodes.unsubscribe();
    this.subscriptionEntireNodes.unsubscribe();
    this.subscriptionFilingHoldingSchemeNodes.unsubscribe();
    this.subscriptionSimpleSearchCriteriaAdd.unsubscribe();
    this.openDialogSubscription.unsubscribe();
    this.showConfirmEliminationSuscription.unsubscribe();
    this.actionliminationSuscription?.unsubscribe();
    this.analysisliminationSuscription?.unsubscribe();
  }

  exportArchiveUnitsToCsvFile() {
    if (this.criteriaSearchList && this.criteriaSearchList.length > 0) {
      const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
      const searchCriteria = {
        criteriaList: this.criteriaSearchList,
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
      this.listOfUAIdToInclude = [];
      this.listOfUAIdToExclude = [];
      this.listOfUACriteriaSearch = [];
    } else {
      this.isAllchecked = false;
      this.isIndeterminate = false;
      this.itemSelected = 0;
      this.listOfUAIdToInclude = [];
      this.listOfUAIdToExclude = [];
      this.listOfUACriteriaSearch = [];
    }
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

  prepareUAIdList(criteriaSearchList: SearchCriteriaEltDto[], listOfUAIdToInclude: CriteriaValue[], listOfUAIdToExclude: CriteriaValue[]) {
    const listOfUACriteriaSearch = [];
    if (criteriaSearchList && criteriaSearchList.length > 0) {
      if (this.isAllchecked || this.isIndeterminate) {
        criteriaSearchList.forEach((element) => {
          listOfUACriteriaSearch.push(element);
        });
      }

      if (listOfUAIdToInclude && listOfUAIdToInclude.length > 0) {
        listOfUACriteriaSearch.push({
          criteria: 'GUID',
          values: listOfUAIdToInclude,
          operator: 'EQ',
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
          dataType: 'String',
        });
      }

      if (listOfUAIdToExclude && listOfUAIdToExclude.length > 0) {
        listOfUACriteriaSearch.push({
          criteria: 'GUID',
          values: listOfUAIdToExclude,
          operator: 'NOT_EQ',
          category: SearchCriteriaTypeEnum[SearchCriteriaTypeEnum.FIELDS],
          dataType: 'String',
        });
      }
    }

    return listOfUACriteriaSearch;
  }

  private initializeSelectionParams() {
    this.itemSelected = 0;
    this.isIndeterminate = false;
    this.itemNotSelected = 0;
    this.isAllchecked = false;
  }

  openSnackBarForWorkflow(message: string, serviceUrl?: string) {
    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      panelClass: 'vitamui-snack-bar',
      data: {
        type: 'WorkflowSuccessSnackBar',
        message,
        serviceUrl,
      },
      duration: 100000,
    });
  }

  checkUserHasRole(operation: string, role: string, tenantIdentifier: number) {
    this.archiveService.hasArchiveSearchRole(role, tenantIdentifier).subscribe((result) => {
      switch (operation) {
        case 'DIPExport':
          this.hasDipExportRole = result;
          break;
        case 'EliminationAnalysis':
          this.hasEliminationAnalysisRole = result;
          break;
        case 'EliminationAction':
          this.hasEliminationActionRole = result;
          break;
        default:
          break;
      }
    });
  }

  openExportDIPRequestCreateDialog() {
    if (this.itemSelected > MAX_DIP_EXPORT_THRESHOLD) {
      this.snackBar.openFromComponent(VitamUISnackBarComponent, {
        panelClass: 'vitamui-snack-bar',
        data: { type: 'exportDIPLimitReached' },
        duration: 10000,
      });
      return;
    }

    this.listOfUACriteriaSearch = this.prepareUAIdList(this.criteriaSearchList, this.listOfUAIdToInclude, this.listOfUAIdToExclude);
    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    const exportDIPSearchCriteria = {
      criteriaList: this.listOfUACriteriaSearch,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      sortingCriteria,
      language: this.translateService.currentLang,
    };

    const dialogRef = this.dialog.open(DipRequestCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: false,
      data: {
        itemSelected: this.itemSelected,
        exportDIPSearchCriteria,
        accessContract: this.accessContract,
        tenantIdentifier: this.tenantIdentifier,
      },
    });
    this.openDialogSubscription = dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        return;
      }
    });
  }

  startEliminationAnalysis() {
    if (this.itemSelected > MAX_ELIMINATION_ANALYSIS_THRESHOLD) {
      this.snackBar.openFromComponent(VitamUISnackBarComponent, {
        panelClass: 'vitamui-snack-bar',
        data: { type: 'thresholdExceeded', name: 'thresholdExceeded' },
        duration: 10000,
      });
      return;
    }

    this.listOfUACriteriaSearch = this.prepareUAIdList(this.criteriaSearchList, this.listOfUAIdToInclude, this.listOfUAIdToExclude);
    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    const exportDIPSearchCriteria = {
      criteriaList: this.listOfUACriteriaSearch,
      pageNumber: this.currentPage,
      size: PAGE_SIZE,
      sortingCriteria,
      language: this.translateService.currentLang,
    };

    this.analysisliminationSuscription = this.archiveService
      .startEliminationAnalysis(exportDIPSearchCriteria, this.accessContract)
      .subscribe((data) => {
        this.eliminationAnalysisResponse = data.$results;

        if (this.eliminationAnalysisResponse && this.eliminationAnalysisResponse[0].itemId) {
          const guid = this.eliminationAnalysisResponse[0].itemId;
          const message = this.translateService.instant('ARCHIVE_SEARCH.ELIMINATION.ELIMINATION_ANALYSIS_LAUNCHED');
          const serviceUrl =
            this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + this.tenantIdentifier + '?guid=' + guid;

          this.openSnackBarForWorkflow(message, serviceUrl);
        }
      });
  }

  launchEliminationAction() {
    if (this.itemSelected > MAX_ELIMINATION_ACTION_THRESHOLD) {
      this.snackBar.openFromComponent(VitamUISnackBarComponent, {
        panelClass: 'vitamui-snack-bar',
        data: { type: 'thresholdExceeded', name: 'thresholdExceeded' },
        duration: 10000,
      });
      return;
    }
    const dialogToOpen = this.confirmEliminationActionDialog;
    const dialogRef = this.dialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });

    this.showConfirmEliminationSuscription = dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.listOfUACriteriaSearch = this.prepareUAIdList(this.criteriaSearchList, this.listOfUAIdToInclude, this.listOfUAIdToExclude);
        const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
        const exportDIPSearchCriteria = {
          criteriaList: this.listOfUACriteriaSearch,
          pageNumber: this.currentPage,
          size: PAGE_SIZE,
          sortingCriteria,
          language: this.translateService.currentLang,
        };

        this.actionliminationSuscription = this.archiveService
          .launchEliminationAction(exportDIPSearchCriteria, this.accessContract)
          .subscribe((response) => {
            this.eliminationActionResponse = response.$results;

            if (this.eliminationActionResponse && this.eliminationActionResponse[0].itemId) {
              const guid = this.eliminationActionResponse[0].itemId;
              const message = this.translateService.instant('ARCHIVE_SEARCH.ELIMINATION.ELIMINATION_LAUNCHED');
              const serviceUrl =
                this.startupService.getReferentialUrl() + '/logbook-operation/tenant/' + this.tenantIdentifier + '?guid=' + guid;

              this.openSnackBarForWorkflow(message, serviceUrl);
            }
          });
      });
  }
}
