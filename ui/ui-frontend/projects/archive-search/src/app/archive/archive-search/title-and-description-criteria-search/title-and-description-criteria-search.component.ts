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
import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { merge, Subscription } from 'rxjs';
import { debounceTime, filter, map } from 'rxjs/operators';
import { diff } from 'ui-frontend-common';
import { ArchiveSharedDataServiceService } from '../../../core/archive-shared-data-service.service';
import { FilingHoldingSchemeNode } from '../../models/node.interface';
import { NodeData } from '../../models/nodedata.interface';
import { SearchCriteriaHistory } from '../../models/search-criteria-history.interface';
import { SearchCriteria, SearchCriteriaCategory, SearchCriteriaEltDto, SearchCriteriaTypeEnum } from '../../models/search.criteria';
import { Unit } from '../../models/unit.interface';

const UPDATE_DEBOUNCE_TIME = 200;

@Component({
  selector: 'title-and-description-criteria-search',
  templateUrl: './title-and-description-criteria-search.component.html',
  styleUrls: ['./title-and-description-criteria-search.component.css'],
})
export class TitleAndDescriptionCriteriaSearchComponent implements OnInit {
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
  quickSearchCriteriaForm: FormGroup;
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
  previousTitleDescriptionCriteriaValue: {
    archiveCriteria: '';
  };
  emptyTitleDescriptionCriteriaForm = {
    archiveCriteria: '',
  };

  show = true;
  showUnitPreviewBlock = false;
  nodeArray: FilingHoldingSchemeNode[] = [];
  nodeData: NodeData;
  entireNodesIds: string[];

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private archiveExchangeDataService: ArchiveSharedDataServiceService,
    public dialog: MatDialog
  ) {
    this.previousTitleDescriptionCriteriaValue = {
      archiveCriteria: '',
    };

    this.quickSearchCriteriaForm = this.formBuilder.group({
      archiveCriteria: ['', []],
    });
    merge(this.quickSearchCriteriaForm.statusChanges, this.quickSearchCriteriaForm.valueChanges)
      .pipe(
        debounceTime(UPDATE_DEBOUNCE_TIME),
        filter(() => this.quickSearchCriteriaForm.valid),
        map(() => this.quickSearchCriteriaForm.value),
        map(() => diff(this.quickSearchCriteriaForm.value, this.previousTitleDescriptionCriteriaValue)),
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
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  private resetSimpleCriteriaForm() {
    this.quickSearchCriteriaForm.reset(this.emptyTitleDescriptionCriteriaForm);
  }

  ngOnInit() {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
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

  addCriteria(
    keyElt: string,
    keyLabel: string,
    valueElt: string,
    labelElt: string,
    translated: boolean,
    operator: string,
    valueTranslated: boolean
  ) {
    if (keyElt && valueElt) {
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubject({
        keyElt: keyElt,
        keyLabel: keyLabel,
        valueElt: valueElt,
        labelElt: labelElt,
        keyTranslated: translated,
        operator: operator,
        category: SearchCriteriaTypeEnum.FIELDS,
        valueTranslated: valueTranslated,
      });
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

  ngOnDestroy() {
    // unsubscribe to ensure no memory leaks
  }

  get archiveCriteria() {
    return this.quickSearchCriteriaForm.controls.archiveCriteria;
  }
}
