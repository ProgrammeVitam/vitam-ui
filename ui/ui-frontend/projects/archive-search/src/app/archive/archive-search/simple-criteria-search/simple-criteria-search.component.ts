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
import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { merge, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { diff, Direction } from 'ui-frontend-common';
import { ArchiveSharedDataServiceService } from '../../../core/archive-shared-data-service.service';
import { ArchiveService } from '../../archive.service';
import { FilingHoldingSchemeNode } from '../../models/node.interface';
import { NodeData } from '../../models/nodedata.interface';
import { SearchCriteriaHistory } from '../../models/search-criteria-history.interface';
import {
  SearchCriteria,
  SearchCriteriaCategory,
  SearchCriteriaEltDto,
  SearchCriteriaStatusEnum,
  SearchCriteriaTypeEnum,
} from '../../models/search.criteria';
import { Unit } from '../../models/unit.interface';

const UPDATE_DEBOUNCE_TIME = 200;
const BUTTON_MAX_TEXT = 40;
const DESCRIPTION_MAX_TEXT = 60;
const PAGE_SIZE = 10;

@Component({
  selector: 'simple-criteria-search',
  templateUrl: './simple-criteria-search.component.html',
  styleUrls: ['./simple-criteria-search.component.css'],
})
export class SimpleCriteriaSearchComponent implements OnInit {
  @Output() archiveUnitClick = new EventEmitter<any>();

  orderBy = 'Title';
  direction = Direction.ASCENDANT;
  @Input()
  accessContract: string;
  nbQueryCriteria: number = 0;
  subscriptionNodes: Subscription;
  subscriptionEntireNodes: Subscription;
  subscriptionFilingHoldingSchemeNodes: Subscription;
  currentPage: number = 0;
  pageNumbers: number = 0;
  totalResults: number = 0;
  pending: boolean = false;
  included: boolean = false;
  canLoadMore: boolean = false;
  tenantIdentifier: string;
  simpleCriteriaForm: FormGroup;
  submited: boolean = false;
  searchCriterias: Map<string, SearchCriteria>;
  searchCriteriaKeys: string[];
  otherCriteriaValueEnabled: boolean = false;
  otherCriteriaValueType: string = 'DATE';
  showCriteriaPanel: boolean = true;
  showSearchCriteriaPanel: boolean = false;
  selectedValueOntolonogy: any;
  archiveUnits: Unit[];
  ontologies: any;

  shouldShowPreviewArchiveUnit = false;
  fieldsCriteriaList: SearchCriteriaEltDto[] = [];
  appraisalCriteriaList: SearchCriteriaEltDto[] = [];

  searchedCriteriaNodesList: string[] = [];

  additionalSearchCriteriaCategories: SearchCriteriaCategory[];
  additionalSearchCriteriaCategoryIndex = 0;
  showDuaEndDate = false;
  searchCriteriaHistory: SearchCriteriaHistory[] = [];
  searchCriteriaHistoryToSave: Map<string, SearchCriteriaHistory>;
  searchCriteriaHistoryLength: number = null;
  hasResults = false;
  previousSimpleCriteriaValue: {
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
    otherCriteria: '';
    otherCriteriaValue: '';
  };
  emptySimpleCriteriaForm = {
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
        this.addCriteriaNode('NODE', 'NODE', node.id, node.title, true, 'EQ', false);
      } else {
        node.count = null;
        this.removeCriteria('NODE', node.id);
      }
    });

    this.archiveService.getOntologiesFromJson().subscribe((data: any) => {
      this.ontologies = data;
      this.ontologies.sort(function (a: any, b: any) {
        var shortNameA = a.Label;
        var shortNameB = b.Label;
        return shortNameA < shortNameB ? -1 : shortNameA > shortNameB ? 1 : 0;
      });
    });

    this.previousSimpleCriteriaValue = {
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
      otherCriteria: '',
      otherCriteriaValue: '',
    };

    this.simpleCriteriaForm = this.formBuilder.group({
      archiveCriteria: ['', []],
      title: ['', []],
      description: ['', []],
      guid: ['', []],
      uaid: ['', []],
      beginDt: ['', []],
      endDt: ['', []],
      serviceProdLabel: ['', []],
      serviceProdCode: ['', []],
      otherCriteria: ['', []],
      otherCriteriaValue: ['', []],
    });
    merge(this.simpleCriteriaForm.statusChanges, this.simpleCriteriaForm.valueChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        filter(() => this.simpleCriteriaForm.valid),
        map(() => this.simpleCriteriaForm.value),
        map(() => diff(this.simpleCriteriaForm.value, this.previousSimpleCriteriaValue)),
        filter((formData) => this.isEmpty(formData))
      )
      .subscribe(() => {
        this.resetSimpleCriteriaForm();
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
          true,
          'EQ',
          false
        );
        return true;
      } else if (formData.title) {
        this.addCriteria('Title', 'TITLE', formData.title.trim(), formData.title.trim(), true, 'EQ', false);
        return true;
      } else if (formData.description) {
        this.addCriteria('Description', 'DESCRIPTION', formData.description.trim(), formData.description.trim(), true, 'EQ', false);
        return true;
      } else if (formData.beginDt) {
        this.addCriteria(
          'StartDate',
          'START_DATE',
          this.simpleCriteriaForm.value.beginDt,
          this.datePipe.transform(this.simpleCriteriaForm.value.beginDt, 'dd/MM/yyyy'),
          true,
          'GTE',
          false
        );
        return true;
      } else if (formData.endDt) {
        this.addCriteria(
          'EndDate',
          'END_DATE',
          this.simpleCriteriaForm.value.endDt,
          this.datePipe.transform(this.simpleCriteriaForm.value.endDt, 'dd/MM/yyyy'),
          true,
          'LTE',
          false
        );
        return true;
      } else if (formData.serviceProdCode) {
        this.addCriteria(
          '#originating_agency',
          'SP_CODE',
          formData.serviceProdCode.trim(),
          formData.serviceProdCode.trim(),
          true,
          'EQ',
          false
        );
        return true;
      } else if (formData.serviceProdLabel) {
        this.addCriteria(
          'originating_agency_label',
          'SP_LABEL',
          formData.serviceProdLabel.trim(),
          formData.serviceProdLabel.trim(),
          true,
          'EQ',
          false
        );
        return true;
      } else if (formData.uaid) {
        this.addCriteria('#id', 'ID', formData.uaid, formData.uaid, true, 'EQ', false);
        return true;
      } else if (formData.guid) {
        this.addCriteria('#opi', 'GUID', formData.guid, formData.guid, true, 'EQ', false);
        return true;
      } else if (formData.otherCriteriaValue) {
        const ontologyElt = this.ontologies.find((ontoElt: any) => ontoElt.Value === formData.otherCriteria);
        if (this.otherCriteriaValueType === 'DATE') {
          this.addCriteria(
            ontologyElt.Value,
            ontologyElt.Label,
            this.simpleCriteriaForm.value.otherCriteriaValue,
            this.datePipe.transform(this.simpleCriteriaForm.value.otherCriteriaValue, 'dd/MM/yyyy'),
            false,
            'EQ',
            false
          );
        } else {
          this.addCriteria(
            ontologyElt.Value,
            ontologyElt.Label,
            formData.otherCriteriaValue.trim(),
            formData.otherCriteriaValue.trim(),
            false,
            'EQ',
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

  private resetSimpleCriteriaForm() {
    this.simpleCriteriaForm.reset(this.emptySimpleCriteriaForm);
  }

  ngOnInit() {
    this.additionalSearchCriteriaCategoryIndex = 0;
    this.additionalSearchCriteriaCategories = [];
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });

    this.searchCriterias = new Map();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.accessContract) {
      this.show = true;
      this.archiveExchangeDataService.emitToggle(this.show);
    }
  }

  removeCriteriaEvent(criteriaToRemove: any) {
    this.removeCriteria(criteriaToRemove.keyElt, criteriaToRemove.valueElt);
  }
  removeCriteria(keyElt: string, valueElt: string) {
    if (this.searchCriterias && this.searchCriterias.size > 0) {
      this.searchCriterias.forEach((val, key) => {
        if (key === keyElt) {
          let values = val.values;
          values = values.filter((item) => item.value !== valueElt);
          if (values.length === 0) {
            this.searchCriteriaKeys.forEach((element, index) => {
              if (element == keyElt) this.searchCriteriaKeys.splice(index, 1);
            });
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
    this.simpleCriteriaForm.get('otherCriteria').valueChanges.subscribe((selectedcriteria) => {
      if (selectedcriteria === '') {
        this.otherCriteriaValueEnabled = false;
        this.selectedValueOntolonogy = null;
      } else {
        this.simpleCriteriaForm.controls.otherCriteriaValue.setValue('');
        this.otherCriteriaValueEnabled = true;
        let selectedValueOntolonogyValue = this.simpleCriteriaForm.get('otherCriteria').value;
        const selectedValueOntolonogyElt = this.ontologies.find((ontoElt: any) => ontoElt.Value === selectedValueOntolonogyValue);
        if (selectedValueOntolonogyElt) {
          this.selectedValueOntolonogy = selectedValueOntolonogyElt.Label;
          this.otherCriteriaValueType = selectedValueOntolonogyElt.Type;
        }
      }
    });
  }

  addCriteria(
    keyElt: string,
    keyLabel: string,
    valueElt: string,
    labelElt: string,
    keyTranslated: boolean,
    operator: string,
    valueTranslated: boolean
  ) {
    if (keyElt && valueElt) {
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubject({
        keyElt: keyElt,
        keyLabel: keyLabel,
        valueElt: valueElt,
        labelElt: labelElt,
        keyTranslated: keyTranslated,
        operator: operator,
        category: SearchCriteriaTypeEnum.FIELDS,
        valueTranslated: valueTranslated,
      });
    }
  }

  addCriteriaNode(
    keyElt: string,
    keyLabel: string,
    valueElt: string,
    labelElt: string,
    keyTranslated: boolean,
    operator: string,
    valueTranslated: boolean
  ) {
    if (keyElt && valueElt) {
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubject({
        keyElt: keyElt,
        keyLabel: keyLabel,
        valueElt: valueElt,
        labelElt: labelElt,
        keyTranslated: keyTranslated,
        operator: operator,
        category: SearchCriteriaTypeEnum.NODES,
        valueTranslated: valueTranslated,
      });
    }
  }

  addOntologyFilter(criteriaValue: string, value: string, operator: string): any {
    const ontologyElt = this.ontologies.find((ontoElt: any) => ontoElt.Value === criteriaValue);
    if (ontologyElt.Type === 'DATE') {
      this.addCriteria(ontologyElt.Value, ontologyElt.Label, value, this.datePipe.transform(value, 'dd/MM/yyyy'), false, operator, false);
    } else {
      this.addCriteria(ontologyElt.Value, ontologyElt.Label, value, value, false, operator, false);
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
      (this.fieldsCriteriaList && this.fieldsCriteriaList.length > 0) ||
      (this.searchedCriteriaNodesList && this.searchedCriteriaNodesList.length > 0) ||
      (this.appraisalCriteriaList && this.appraisalCriteriaList.length > 0)
    ) {
      let sortingCriteria = { criteria: this.orderBy, sorting: this.direction };
      let searchCriteria = {
        nodes: this.searchedCriteriaNodesList,
        criteriaList: this.fieldsCriteriaList,
        appraisalMgtRulesCriteriaList: this.appraisalCriteriaList,
        pageNumber: this.currentPage,
        size: PAGE_SIZE,
        sortingCriteria: sortingCriteria,
        language: this.translateService.currentLang,
      };
      this.archiveService.exportCsvSearchArchiveUnitsByCriteria(searchCriteria, this.accessContract);
    }
  }

  get uaid() {
    return this.simpleCriteriaForm.controls.uaid;
  }
  get archiveCriteria() {
    return this.simpleCriteriaForm.controls.archiveCriteria;
  }
  get title() {
    return this.simpleCriteriaForm.controls.title;
  }
  get description() {
    return this.simpleCriteriaForm.controls.description;
  }
  get guid() {
    return this.simpleCriteriaForm.controls.guid;
  }
  get beginDt() {
    return this.simpleCriteriaForm.controls.beginDt;
  }
  get endDt() {
    return this.simpleCriteriaForm.controls.endDt;
  }
  get serviceProdLabel() {
    return this.simpleCriteriaForm.controls.serviceProdLabel;
  }

  get serviceProdCode() {
    return this.simpleCriteriaForm.controls.serviceProdCode;
  }

  get otherCriteria() {
    return this.simpleCriteriaForm.controls.otherCriteria;
  }
  get otherCriteriaValue() {
    return this.simpleCriteriaForm.controls.otherCriteriaValue;
  }
}
