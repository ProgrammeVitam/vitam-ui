/*Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
and the signatories of the "VITAM - Accord du Contributeur" agreement.

contact@programmevitam.fr

This software is a computer program whose purpose is to implement
implement a digital archiving front-office system for the secure and
efficient high volumetry VITAM solution.

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import {
  ActionOnCriteria,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  SchemaElement,
  SchemaService,
  SearchCriteriaAddAction,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
} from 'vitamui-library';
import { ArchiveSearchConstsEnum } from '../../models/archive-search-consts-enum';
import { ArchiveSharedDataService } from '../../services/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../../services/management-rules-shared-data.service';
import { ItemNode } from 'vitamui-library/app/modules/components/autocomplete/vitamui-autocomplete-multi-select-tree/vitamui-autocomplete-multi-select-tree.component';

const FINAL_ACTION_TYPE = 'FINAL_ACTION_TYPE';
const ARCHIVE_UNIT_WITH_OBJECTS = 'ARCHIVE_UNIT_WITH_OBJECTS';
const ARCHIVE_UNIT_WITHOUT_OBJECTS = 'ARCHIVE_UNIT_WITHOUT_OBJECTS';
const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';

const searchCriteriaConfigs: { [key: string]: Partial<SearchCriteriaAddAction> } = {
  title: {
    keyElt: 'TITLE',
    keyTranslated: true,
  },
  description: {
    keyElt: 'DESCRIPTION',
    keyTranslated: true,
  },
  beginDt: {
    keyElt: 'START_DATE',
    keyTranslated: true,
    operator: CriteriaOperator.GTE,
    dataType: CriteriaDataType.DATE,
  },
  endDt: {
    keyElt: 'END_DATE',
    keyTranslated: true,
    operator: CriteriaOperator.LTE,
    dataType: CriteriaDataType.DATE,
  },
  serviceProdCode: {
    keyElt: 'SP_CODE',
    keyTranslated: true,
  },
  serviceProdLabel: {
    keyElt: 'SP_LABEL',
    keyTranslated: true,
  },
  guid: {
    keyElt: 'GUID',
    keyTranslated: true,
  },
  guidopi: {
    keyElt: 'GUID_OPI',
    keyTranslated: true,
    operator: CriteriaOperator.IN,
  },
};
searchCriteriaConfigs.Title = searchCriteriaConfigs.title;
searchCriteriaConfigs.Description = searchCriteriaConfigs.description;
searchCriteriaConfigs.StartDate = searchCriteriaConfigs.beginDt;
searchCriteriaConfigs.EndDate = searchCriteriaConfigs.endDt;

@Component({
  selector: 'app-simple-criteria-search',
  templateUrl: './simple-criteria-search.component.html',
  styleUrls: ['./simple-criteria-search.component.css'],
})
export class SimpleCriteriaSearchComponent implements OnInit {
  simpleCriteriaForm: FormGroup;
  criteriaSearchListToSave: SearchCriteriaEltDto[] = [];

  archiveUnitTypesCriteria: Map<any, boolean> = new Map<any, boolean>([
    [ARCHIVE_UNIT_WITH_OBJECTS, true],
    [ARCHIVE_UNIT_WITHOUT_OBJECTS, true],
  ]);

  otherCriteriaOptions$: Observable<ItemNode<SchemaElement>[]>;
  getOtherCriteriaDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

  constructor(
    private formBuilder: FormBuilder,
    private archiveExchangeDataService: ArchiveSharedDataService,
    public dialog: MatDialog,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private translateService: TranslateService,
    private schemaService: SchemaService,
  ) {
    this.otherCriteriaOptions$ = this.schemaService.getDescriptiveSchemaTree();

    this.translateService.onLangChange.subscribe(() => {
      if (this.archiveUnitTypesCriteria.get(ARCHIVE_UNIT_WITH_OBJECTS)) {
        this.manageUnitObjectUnitCriteria(ARCHIVE_UNIT_WITH_OBJECTS);
      }

      if (this.archiveUnitTypesCriteria.get(ARCHIVE_UNIT_WITHOUT_OBJECTS)) {
        this.manageUnitObjectUnitCriteria(ARCHIVE_UNIT_WITHOUT_OBJECTS);
      }
    });

    const otherCriteriaListControl = this.formBuilder.control<SchemaElement[]>([]);
    const otherCriteriaControl = this.formBuilder.group({});

    this.simpleCriteriaForm = this.formBuilder.group({
      title: ['', []],
      description: ['', []],
      guid: ['', [Validators.pattern('^[a-z0-9_, ]+')]],
      serviceProdLabel: ['', []],
      serviceProdCode: ['', []],
      beginDt: ['', []],
      endDt: ['', []],
      otherCriteriaList: otherCriteriaListControl,
      otherCriteria: otherCriteriaControl,
    });

    otherCriteriaListControl.valueChanges.subscribe((schemaElements) => {
      schemaElements.forEach((schemaElement) => {
        const path = schemaElement.Path;
        if (!otherCriteriaControl.get(path)) {
          const control = formBuilder.control(undefined);
          otherCriteriaControl.addControl(path, control);
        }
      });
      const paths = schemaElements.map((se) => se.Path);
      Object.keys(otherCriteriaControl.controls).forEach((path) => {
        if (!paths.includes(path)) {
          otherCriteriaControl.removeControl(path);
        }
      });
    });

    Object.entries(this.simpleCriteriaForm.controls)
      .filter(([key, _value]) => !['otherCriteriaList'].includes(key))
      .forEach(([key, control]) => {
        control.valueChanges.pipe(debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME)).subscribe((value) => {
          if (value) {
            this.addCriteriaFromObject({ [key]: value });
            control.reset(undefined, { emitEvent: false });
          }
        });
      });

    this.archiveExchangeDataService.receiveRemoveFromChildSearchCriteriaSubject().subscribe((criteria) => {
      if (criteria) {
        if (criteria.action === ActionOnCriteria.ADD) {
          this.archiveUnitTypesCriteria.set(criteria.valueElt.id, true);
        } else if (criteria.action === ActionOnCriteria.REMOVE) {
          this.archiveUnitTypesCriteria.set(criteria.valueElt.id, false);
        }
      }
    });
  }

  addCriteriaFromObject(object: any) {
    Object.entries(object)
      .filter(([_key, value]) => !!value)
      .forEach(([key, value]) => {
        if (key === 'guid' && value.toString().includes(',')) {
          value
            .toString()
            .split(',')
            .forEach((v) => this.addCriteriaFromObject({ guid: v }));
        } else if (typeof value === 'string' || value instanceof Date) {
          console.log(`Handle ${key}: ${value}`);

          const criteriaValue = value instanceof Date ? value.toISOString() : value.trim();
          const defaultSearchCriteriaAddAction: Partial<SearchCriteriaAddAction> = {
            valueElt: { value: criteriaValue, id: criteriaValue },
            labelElt: criteriaValue,
            keyTranslated: false,
            operator: CriteriaOperator.EQ,
            category: SearchCriteriaTypeEnum.FIELDS,
            dataType: value instanceof Date ? CriteriaDataType.DATE : CriteriaDataType.STRING,
          };

          const searchCriteriaAddAction: SearchCriteriaAddAction = {
            ...defaultSearchCriteriaAddAction,
            ...(searchCriteriaConfigs[key] || { keyElt: key }),
          } as SearchCriteriaAddAction;

          const searchCriteria = {
            ...searchCriteriaAddAction,
            valueTranslated: this.isValueTranslated(searchCriteriaAddAction.keyElt),
          };
          console.log(searchCriteria);
          this.archiveExchangeDataService.addSimpleSearchCriteriaSubject(searchCriteria);
        } else if (typeof value === 'object' && Object.entries(value).length) {
          this.addCriteriaFromObject(value);
        } else {
          console.error(`Unhandled case`, object, key, value);
        }
      });
  }

  isValueTranslated(criteria: string) {
    return criteria === FINAL_ACTION_TYPE || criteria === ALL_ARCHIVE_UNIT_TYPES;
  }
  ngOnInit() {
    this.managementRulesSharedDataService.getCriteriaSearchListToSave().subscribe((data) => {
      this.criteriaSearchListToSave = data;
    });

    this.criteriaSearchListToSave.forEach((criteriaSearch) => {
      if (criteriaSearch.criteria === ALL_ARCHIVE_UNIT_TYPES) {
        criteriaSearch.values.forEach((criteriaValue) => {
          this.archiveUnitTypesCriteria.set(criteriaValue.id, true);
        });
      }
      criteriaSearch.values.forEach((value) => {
        this.addCriteria(
          criteriaSearch.criteria,
          value,
          value.id,
          true,
          criteriaSearch.operator,
          this.isValueTranslated(criteriaSearch.criteria),
          criteriaSearch.dataType,
          criteriaSearch.category as SearchCriteriaTypeEnum,
        );
      });
    });
    if (this.criteriaSearchListToSave.length === 0) {
      this.addCriteria(
        ALL_ARCHIVE_UNIT_TYPES,
        { value: ARCHIVE_UNIT_WITH_OBJECTS, id: ARCHIVE_UNIT_WITH_OBJECTS },
        this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.ARCHIVE_UNIT_WITH_OBJECTS'),
        true,
        CriteriaOperator.EQ,
        false,
        CriteriaDataType.STRING,
        SearchCriteriaTypeEnum.FIELDS,
      );

      this.addCriteria(
        ALL_ARCHIVE_UNIT_TYPES,
        { value: ARCHIVE_UNIT_WITHOUT_OBJECTS, id: ARCHIVE_UNIT_WITHOUT_OBJECTS },
        this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.ARCHIVE_UNIT_WITHOUT_OBJECTS'),
        true,
        CriteriaOperator.EQ,
        false,
        CriteriaDataType.STRING,
        SearchCriteriaTypeEnum.FIELDS,
      );
    }
  }

  getCriteriaName(criteria: SchemaElement, otherCriteriaOptions: ItemNode<SchemaElement>[]) {
    const path = criteria.Path.split('.').slice(0, -1);
    const parent = path.reduce((acc, p) => acc.children.find((o) => o.item.FieldName === p), {
      children: otherCriteriaOptions,
    } as ItemNode<SchemaElement>);
    return `${criteria.ShortName}${parent?.item ? ` (${parent.item.ShortName})` : ''}`;
  }

  addCriteria(
    keyElt: string,
    valueElt: CriteriaValue,
    labelElt: string,
    keyTranslated: boolean,
    operator: string,
    valueTranslated: boolean,
    dataType: string,
    category?: SearchCriteriaTypeEnum,
  ) {
    if (keyElt && valueElt) {
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubject({
        keyElt,
        valueElt,
        labelElt,
        keyTranslated,
        operator,
        category: category ? category : SearchCriteriaTypeEnum.FIELDS,
        valueTranslated,
        dataType,
      });
    }
  }

  addArchiveUnitTypeCriteria(unitType: string, event: any) {
    const action = event.target.checked;
    this.archiveUnitTypesCriteria.set(unitType, action);
    switch (unitType) {
      case ARCHIVE_UNIT_WITH_OBJECTS:
        if (action) {
          this.addCriteria(
            ALL_ARCHIVE_UNIT_TYPES,
            { value: ARCHIVE_UNIT_WITH_OBJECTS, id: ARCHIVE_UNIT_WITH_OBJECTS },
            this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.ARCHIVE_UNIT_WITH_OBJECTS'),
            true,
            CriteriaOperator.EQ,
            false,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.FIELDS,
          );
        } else {
          this.emitRemoveCriteriaEvent(ALL_ARCHIVE_UNIT_TYPES, { value: ARCHIVE_UNIT_WITH_OBJECTS, id: ARCHIVE_UNIT_WITH_OBJECTS });
        }
        break;
      case ARCHIVE_UNIT_WITHOUT_OBJECTS:
        if (action) {
          this.addCriteria(
            ALL_ARCHIVE_UNIT_TYPES,
            { value: ARCHIVE_UNIT_WITHOUT_OBJECTS, id: ARCHIVE_UNIT_WITHOUT_OBJECTS },
            this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.ARCHIVE_UNIT_WITHOUT_OBJECTS'),
            true,
            CriteriaOperator.EQ,
            false,
            CriteriaDataType.STRING,
            SearchCriteriaTypeEnum.FIELDS,
          );
        } else {
          this.emitRemoveCriteriaEvent(ALL_ARCHIVE_UNIT_TYPES, { value: ARCHIVE_UNIT_WITHOUT_OBJECTS, id: ARCHIVE_UNIT_WITHOUT_OBJECTS });
        }
        break;
      default:
        break;
    }
  }

  emitRemoveCriteriaEvent(keyElt: string, valueElt?: CriteriaValue) {
    this.archiveExchangeDataService.sendRemoveFromChildSearchCriteriaAction({ keyElt, valueElt, action: ActionOnCriteria.REMOVE });
  }

  manageUnitObjectUnitCriteria(unitObjectProperty: string) {
    this.emitRemoveCriteriaEvent(ALL_ARCHIVE_UNIT_TYPES, { value: unitObjectProperty, id: unitObjectProperty });
    this.addCriteria(
      ALL_ARCHIVE_UNIT_TYPES,
      { value: unitObjectProperty, id: unitObjectProperty },
      this.translateService.instant('COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE.' + unitObjectProperty),
      true,
      CriteriaOperator.EQ,
      false,
      CriteriaDataType.STRING,
      SearchCriteriaTypeEnum.FIELDS,
    );
    this.archiveUnitTypesCriteria.set(unitObjectProperty, true);
  }

  get guid() {
    return this.simpleCriteriaForm.controls.guid;
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

  get otherCriteriaList(): AbstractControl<SchemaElement[]> {
    return this.simpleCriteriaForm.controls.otherCriteriaList;
  }
}
