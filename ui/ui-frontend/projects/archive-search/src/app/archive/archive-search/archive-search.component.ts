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
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { merge, Subject, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { diff, Direction, VitamuiRoles } from 'ui-frontend-common';
import { ArchiveSharedDataServiceService } from '../../core/archive-shared-data-service.service';
import { ArchiveService } from '../archive.service';
import { FilingHoldingSchemeNode } from '../models/node.interface';
import { NodeData } from '../models/nodedata.interface';
import { SearchCriteriaEltements, SearchCriteriaHistory, SearchCriterias } from '../models/search-criteria-history.interface';
import { PagedResult, SearchCriteria, SearchCriteriaEltDto, SearchCriteriaStatusEnum } from '../models/search.criteria';
import { Unit } from '../models/unit.interface';
import { SearchCriteriaSaverComponent } from './search-criteria-saver/search-criteria-saver.component';

const UPDATE_DEBOUNCE_TIME = 200;
const BUTTON_MAX_TEXT = 40;
const DESCRIPTION_MAX_TEXT = 60;
const PAGE_SIZE = 10;
const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-archive-search',
  templateUrl: './archive-search.component.html',
  styleUrls: ['./archive-search.component.scss'],
})
export class ArchiveSearchComponent implements OnInit {
  @Output() archiveUnitClick = new EventEmitter<any>();

  dataToSearchWithRules: any;

  private readonly orderChange = new Subject<string>();
  orderBy = 'Title';
  direction = Direction.ASCENDANT;
  @Input()
  accessContract: string;
  nbQueryCriteria = 0;
  subscriptionNodes: Subscription;
  subscriptionEntireNodes: Subscription;
  subscriptionFilingHoldingSchemeNodes: Subscription;
  currentPage = 0;
  pageNumbers = 0;
  totalResults = 0;
  pending = false;
  included = false;
  canLoadMore = false;
  tenantIdentifier: string;
  form: FormGroup;
  submited = false;
  searchCriterias: Map<string, SearchCriteria>;
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
  searchedCriteriaList: SearchCriteriaEltDto[] = [];
  searchedCriteriaNodesList: string[] = [];

  private readonly filterChange = new Subject<{ [key: string]: any[] }>();

  searchCriteriaHistory: SearchCriteriaHistory[] = [];
  searchCriteriaHistoryToSave: Map<string, SearchCriteriaHistory>;
  searchCriteriaHistoryLength: number = null;
  hasResults = false;
  previousValue: {
    archiveCriteria: '';
    title: '';
    identifier: '';
    description: '';
    guid: '';
    uaid: '';
    beginDt: '';
    endDt: '';
    serviceProdLabel: '';
    serviceProdCode: '';
    serviceProdCommunicability: '';
    serviceProdCommunicabilityDt: '';
    otherCriteria: '';
    otherCriteriaValue: '';
  };
  emptyForm = {
    archiveCriteria: '',
    title: '',
    identifier: '',
    description: '',
    guid: '',
    uaid: '',
    beginDt: '',
    endDt: '',
    serviceProdLabel: '',
    serviceProdCode: '',
    serviceProdCommunicability: '',
    serviceProdCommunicabilityDt: '',
    otherCriteria: '',
    otherCriteriaValue: '',
  };

  show = true;
  showUnitPreviewBlock = false;
  nodeArray: FilingHoldingSchemeNode[] = [];
  nodeData: NodeData;

  entireNodesIds: string[];

  constructor(
    private formBuilder: FormBuilder,
    private archiveService: ArchiveService,
    private translateService: TranslateService,
    private route: ActivatedRoute,
    private archiveExchangeDataService: ArchiveSharedDataServiceService,
    private datePipe: DatePipe,
    public dialog: MatDialog
  ) {
    this.subscriptionEntireNodes = this.archiveExchangeDataService.getEntireNodes().subscribe((nodes) => {
      this.entireNodesIds = nodes;
    });

    this.subscriptionNodes = this.archiveExchangeDataService.getNodes().subscribe((node) => {
      if (node.checked) {
        this.addCriteria('NODE', 'NODE', node.id, node.title, true);
      } else {
        node.count = null;
        this.removeCriteria('NODE', node.id);
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

    this.previousValue = {
      archiveCriteria: '',
      title: '',
      identifier: '',
      description: '',
      guid: '',
      uaid: '',
      beginDt: '',
      endDt: '',
      serviceProdLabel: '',
      serviceProdCode: '',
      serviceProdCommunicability: '',
      serviceProdCommunicabilityDt: '',
      otherCriteria: '',
      otherCriteriaValue: '',
    };

    this.form = this.formBuilder.group({
      archiveCriteria: ['', []],
      title: ['', []],
      description: ['', []],
      guid: ['', []],
      uaid: ['', []],
      beginDt: ['', []],
      endDt: ['', []],
      serviceProdLabel: ['', []],
      serviceProdCode: ['', []],
      serviceProdCommunicability: ['', []],
      serviceProdCommunicabilityDt: ['', []],
      otherCriteria: ['', []],
      otherCriteriaValue: ['', []],
    });
    merge(this.form.statusChanges, this.form.valueChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        filter(() => this.form.valid),
        map(() => this.form.value),
        map(() => diff(this.form.value, this.previousValue)),
        filter((formData) => this.isEmpty(formData))
      )
      .subscribe(() => {
        this.resetForm();
      });
  }

  isEmpty(formData: any): boolean {
    if (formData) {
      if (formData.archiveCriteria) {
        this.addCriteria(
          'titleAndDescription',
          'TITLE_OR_DESCRIPTION',
          formData.archiveCriteria.trim(),
          formData.archiveCriteria.trim(),
          true
        );
        return true;
      } else if (formData.title) {
        this.addCriteria('Title', 'TITLE', formData.title.trim(), formData.title.trim(), true);
        return true;
      } else if (formData.description) {
        this.addCriteria('Description', 'DESCRIPTION', formData.description.trim(), formData.description.trim(), true);
        return true;
      } else if (formData.beginDt) {
        this.addCriteria(
          'StartDate',
          'START_DATE',
          this.form.value.beginDt,
          this.datePipe.transform(this.form.value.beginDt, 'dd/MM/yyyy'),
          true
        );
        return true;
      } else if (formData.endDt) {
        this.addCriteria('EndDate', 'END_DATE', this.form.value.endDt, this.datePipe.transform(this.form.value.endDt, 'dd/MM/yyyy'), true);
        return true;
      } else if (formData.serviceProdCode) {
        this.addCriteria('#originating_agency', 'SP_CODE', formData.serviceProdCode.trim(), formData.serviceProdCode.trim(), true);
        return true;
      } else if (formData.serviceProdLabel) {
        this.addCriteria('originating_agency_label', 'SP_LABEL', formData.serviceProdLabel.trim(), formData.serviceProdLabel.trim(), true);
        return true;
      } else if (formData.uaid) {
        this.addCriteria('#id', 'ID', formData.uaid, formData.uaid, true);
        return true;
      } else if (formData.guid) {
        this.addCriteria('#opi', 'GUID', formData.guid, formData.guid, true);
        return true;
      } else if (formData.serviceProdCommunicability) {
        this.addCriteria(
          'serviceProdCommunicability',
          'SP_COMM',
          formData.serviceProdCommunicability,
          formData.serviceProdCommunicability,
          true
        );
        return true;
      } else if (formData.serviceProdCommunicabilityDt) {
        this.addCriteria(
          'serviceProdCommunicabilityDt',
          'SP_COMM_DT',
          formData.serviceProdCommunicabilityDt,
          formData.serviceProdCommunicabilityDt,
          true
        );
        return true;
      } else if (formData.otherCriteriaValue) {
        const ontologyElt = this.ontologies.find((ontoElt: any) => ontoElt.Value === formData.otherCriteria);
        if (this.otherCriteriaValueType === 'DATE') {
          this.addCriteria(
            ontologyElt.Value,
            ontologyElt.Label,
            this.form.value.otherCriteriaValue,
            this.datePipe.transform(this.form.value.otherCriteriaValue, 'dd/MM/yyyy'),
            false
          );
        } else {
          this.addCriteria(
            ontologyElt.Value,
            ontologyElt.Label,
            formData.otherCriteriaValue.trim(),
            formData.otherCriteriaValue.trim(),
            false
          );
        }
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  private resetForm() {
    this.form.reset(this.emptyForm);
  }

  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.searchCriterias = new Map();
    this.filterMapType.Type = ['Folder', 'Document'];
    const searchCriteriaChange = merge(this.orderChange, this.filterChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      this.submit();
    });
    this.dataToSearchWithRules = {
      appId: this.route.snapshot.data.appId,
      tenantIdentifier: +this.tenantIdentifier,
      role: VitamuiRoles.ROLE_SEARCH_WITH_RULES,
    };
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

  showHidePanel() {
    this.showCriteriaPanel = !this.showCriteriaPanel;
  }

  showStoredSearchCriteria(event: SearchCriteriaHistory) {
    if (this.searchCriterias.size > 0) {
      this.searchCriterias = new Map();
      this.included = false;
    }
    this.reMapSearchCriteriaFromSearchCriteriaHistory(event);
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  removeCriteria(keyElt: string, valueElt: string) {
    if (this.searchCriterias && this.searchCriterias.size > 0) {
      this.searchCriterias.forEach((val, key) => {
        if (key === keyElt) {
          let values = val.values;
          values = values.filter((item) => item.value !== valueElt);
          if (values.length === 0) {
            this.searchCriterias.delete(keyElt);
          } else {
            val.values = values;
            this.searchCriterias.set(keyElt, val);
          }

          this.nbQueryCriteria--;
        }
        if (key === 'NODE') {
          this.archiveExchangeDataService.emitNodeTarget(valueElt);
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

  onSelectOtherCriteria() {
    this.form.get('otherCriteria').valueChanges.subscribe((selectedcriteria) => {
      if (selectedcriteria === '') {
        this.otherCriteriaValueEnabled = false;
        this.selectedValueOntolonogy = null;
      } else {
        this.form.controls.otherCriteriaValue.setValue('');
        this.otherCriteriaValueEnabled = true;
        const selectedValueOntolonogyValue = this.form.get('otherCriteria').value;
        const selectedValueOntolonogyElt = this.ontologies.find((ontoElt: any) => ontoElt.Value === selectedValueOntolonogyValue);
        if (selectedValueOntolonogyElt) {
          this.selectedValueOntolonogy = selectedValueOntolonogyElt.Label;
          this.otherCriteriaValueType = selectedValueOntolonogyElt.Type;
        }
      }
    });
  }

  addCriteria(keyElt: string, keyLabel: string, valueElt: string, labelElt: string, translated: boolean) {
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
          const filtredValues = values.filter((elt) => elt.value === valueElt);
          if (filtredValues.length === 0) {
            values.push({
              value: valueElt,
              label: labelElt,
              valueShown: true,
              status: SearchCriteriaStatusEnum.NOT_INCLUDED,
              translated,
            });
            criteria.values = values;
            this.searchCriterias.set(keyElt, criteria);
          }
        } else {
          const values = [];
          values.push({
            value: valueElt,
            label: labelElt,
            valueShown: true,
            status: SearchCriteriaStatusEnum.NOT_INCLUDED,
            translated,
          });
          const criteria = { key: keyElt, label: keyLabel, values };
          this.searchCriterias.set(keyElt, criteria);
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
    this.searchedCriteriaList = this.buildCriteriaListForQUery();
    this.searchedCriteriaNodesList = this.buildNodesListForQUery();
    if (
      (this.searchedCriteriaList && this.searchedCriteriaList.length > 0) ||
      (this.searchedCriteriaNodesList && this.searchedCriteriaNodesList.length > 0)
    ) {
      this.callVitamApiService();
    }
  }

  getNodesId(): string[] {
    const nodesIdList: string[] = [];
    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      if (criteria.key === 'NODE') {
        criteria.values.forEach((elt) => {
          nodesIdList.push(elt.value);
        });
      }
    });
    return nodesIdList;
  }

  buildNodesListForQUery(): string[] {
    const nodesIdList: string[] = [];
    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      if (criteria.key === 'NODE') {
        criteria.values.forEach((elt) => {
          nodesIdList.push(elt.value);
        });
      }
    });

    return nodesIdList;
  }

  buildCriteriaListForQUery(): SearchCriteriaEltDto[] {
    const criteriaList: SearchCriteriaEltDto[] = [];
    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      const strValues: string[] = [];
      this.updateCriteriaStatus(SearchCriteriaStatusEnum.NOT_INCLUDED, SearchCriteriaStatusEnum.IN_PROGRESS);
      if (criteria.key !== 'NODE') {
        criteria.values.forEach((elt) => {
          strValues.push(elt.value);
        });
        criteriaList.push({ criteria: criteria.key, values: strValues });
      }
    });

    const typesFilterValues: string[] = [];
    this.filterMapType.Type.forEach((filter) => {
      if (filter === 'Folder') {
        typesFilterValues.push('RecordGrp');
      }
      if (filter === 'Document') {
        typesFilterValues.push('File');
        typesFilterValues.push('Item');
      }
    });
    if (typesFilterValues.length > 0) {
      criteriaList.push({ criteria: 'DescriptionLevel', values: typesFilterValues });
    }
    return criteriaList;
  }

  private callVitamApiService() {
    this.pending = true;

    const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
    const searchCriteria = {
      nodes: this.searchedCriteriaNodesList,
      criteriaList: this.searchedCriteriaList,
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

    let _searchCriteriaList: SearchCriterias[] = [];

    let _criteriaList: SearchCriteriaEltements[] = [];

    this.searchCriterias.forEach((criteria: SearchCriteria) => {
      const strValues: string[] = [];

      if (criteria.key !== 'NODE') {
        criteria.values.forEach((elt) => {
          strValues.push(elt.value);
        });
        _criteriaList.push({ criteria: criteria.key, values: strValues });
      }
    });

    const nodesId = this.getNodesId();
    _searchCriteriaList.push({ criteriaList: _criteriaList, nodes: nodesId });

    _searchCriteriaHistory = {
      id: null,
      name: '',
      savingDate: new Date().toISOString(),
      searchCriteriaList: _searchCriteriaList,
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
        this.addCriteria('NODE', 'NODE', nodeId, node.title, true);
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

    storedSearchCriteriaHistory.searchCriteriaList.forEach((searchCriteriaList: SearchCriterias) => {
      this.fillTreeNodeAsSearchCriteriaHistory(searchCriteriaList);

      if (searchCriteriaList.criteriaList.length > 0) {
        searchCriteriaList.criteriaList.forEach((criteria) => {
          const c = criteria.criteria;
          criteria.values.forEach((value) => {
            const keyLabel = this.getKeyLabel(c);
            if (keyLabel !== 'ONTOLOGY_TYPE') {
              // Standard Filters other than ontology criteria
              if (keyLabel.includes('DATE')) {
                const specifiDate = keyLabel === 'START_DATE' ? 'Date de début' : 'Date de fin';
                this.addCriteria(c, specifiDate, value, this.datePipe.transform(value, 'dd/MM/yyyy'), false);
              } else {
                this.addCriteria(c, keyLabel, value, value, true);
              }
            } else {
              this.addOntologyFilter(c, value);
            }
          });
        });
      }
    });
  }

  addOntologyFilter(criteriaValue: string, value: string): any {
    const ontologyElt = this.ontologies.find((ontoElt: any) => ontoElt.Value === criteriaValue);
    if (ontologyElt.Type === 'DATE') {
      this.addCriteria(ontologyElt.Value, ontologyElt.Label, value, this.datePipe.transform(value, 'dd/MM/yyyy'), false);
    } else {
      this.addCriteria(ontologyElt.Value, ontologyElt.Label, value, value, false);
    }
  }

  fillTreeNodeAsSearchCriteriaHistory(searchCriteriaList: SearchCriterias) {
    if (searchCriteriaList.nodes.length > 0) {
      searchCriteriaList.nodes.forEach((nodeId) => {
        this.fillNodeTitle(this.nodeArray, nodeId);
      });
      this.nodeArray = null;
      this.archiveExchangeDataService.emitToggle(true);
    }
  }

  getKeyLabel(keyElement: string) {
    const keyLabels: { [index: string]: string } = {
      '#id': 'ID',
      '#opi': 'GUID',
      '#originating_agency': 'SP_CODE',
      titleAndDescription: 'TITLE_OR_DESCRIPTION',
      Title: 'TITLE',
      Description: 'DESCRIPTION',
      StartDate: 'START_DATE',
      EndDate: 'END_DATE',
      originating_agency_label: 'SP_LABEL',
      serviceProdCommunicability: 'SP_COMM',
      serviceProdCommunicabilityDt: 'SP_COMM_DT',
      ONTOLOGY_TYPE: 'ONTOLOGY_TYPE',
    };
    return keyLabels[keyElement] || keyLabels.ONTOLOGY_TYPE;
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
      this.searchedCriteriaList = this.buildCriteriaListForQUery();
      this.searchedCriteriaNodesList = this.buildNodesListForQUery();
      if (
        (this.searchedCriteriaList && this.searchedCriteriaList.length > 0) ||
        (this.searchedCriteriaNodesList && this.searchedCriteriaNodesList.length > 0)
      ) {
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
  }

  exportArchiveUnitsToCsvFile() {
    if (
      (this.searchedCriteriaList && this.searchedCriteriaList.length > 0) ||
      (this.searchedCriteriaNodesList && this.searchedCriteriaNodesList.length > 0)
    ) {
      const sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
      const searchCriteria = {
        nodes: this.searchedCriteriaNodesList,
        criteriaList: this.searchedCriteriaList,
        pageNumber: this.currentPage,
        size: PAGE_SIZE,
        sortingCriteria,
        language: this.translateService.currentLang,
      };
      this.archiveService.exportCsvSearchArchiveUnitsByCriteria(searchCriteria, this.accessContract);
    }
  }

  clearCriterias() {
    this.searchCriterias = new Map();
    this.included = false;
    this.nbQueryCriteria = 0;
    this.setFilingHoldingScheme();
    this.archiveExchangeDataService.emitFilingHoldingNodes(this.nodeArray);
    this.checkAllNodes(false);
  }

  get uaid() {
    return this.form.controls.uaid;
  }
  get archiveCriteria() {
    return this.form.controls.archiveCriteria;
  }
  get title() {
    return this.form.controls.title;
  }
  get description() {
    return this.form.controls.description;
  }
  get guid() {
    return this.form.controls.guid;
  }
  get beginDt() {
    return this.form.controls.beginDt;
  }
  get endDt() {
    return this.form.controls.endDt;
  }
  get serviceProdLabel() {
    return this.form.controls.serviceProdLabel;
  }
  get serviceProdCommunicability() {
    return this.form.controls.serviceProdCommunicability;
  }
  get serviceProdCode() {
    return this.form.controls.serviceProdCode;
  }
  get serviceProdCommunicabilityDt() {
    return this.form.controls.serviceProdCommunicabilityDt;
  }
  get otherCriteria() {
    return this.form.controls.otherCriteria;
  }
  get otherCriteriaValue() {
    return this.form.controls.otherCriteriaValue;
  }
}
