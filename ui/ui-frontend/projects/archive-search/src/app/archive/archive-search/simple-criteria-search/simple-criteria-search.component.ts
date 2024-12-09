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
import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import {
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  ItemNode,
  SchemaElement,
  SchemaService,
  SearchCriteriaAddAction,
  searchCriteriaConfigs,
  SearchCriteriaEltDto,
  SearchCriteriaTypeEnum,
  CriteriaAction,
  QueryParamsService,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../../../core/management-rules-shared-data.service';
import { debounceTime, filter } from 'rxjs/operators';
import { ArchiveSearchConstsEnum } from '../../models/archive-search-consts-enum';
import { Params } from '@angular/router';

const FINAL_ACTION_TYPE = 'FINAL_ACTION_TYPE';
const ARCHIVE_UNIT_FILING_UNIT = 'ARCHIVE_UNIT_FILING_UNIT';
const ARCHIVE_UNIT_HOLDING_UNIT = 'ARCHIVE_UNIT_HOLDING_UNIT';
const ARCHIVE_UNIT_WITH_OBJECTS = 'ARCHIVE_UNIT_WITH_OBJECTS';
const ARCHIVE_UNIT_WITHOUT_OBJECTS = 'ARCHIVE_UNIT_WITHOUT_OBJECTS';
const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';

type ArchiveUnitType =
  | 'ARCHIVE_UNIT_FILING_UNIT'
  | 'ARCHIVE_UNIT_HOLDING_UNIT'
  | 'ARCHIVE_UNIT_WITH_OBJECTS'
  | 'ARCHIVE_UNIT_WITHOUT_OBJECTS';

@Component({
  selector: 'app-simple-criteria-search',
  templateUrl: './simple-criteria-search.component.html',
  styleUrls: ['./simple-criteria-search.component.css'],
})
export class SimpleCriteriaSearchComponent implements OnInit {
  form: FormGroup;
  criteriaSearchListToSave: SearchCriteriaEltDto[] = [];

  archiveUnitTypesCriteria: Map<any, boolean> = new Map<any, boolean>([
    [ARCHIVE_UNIT_FILING_UNIT, false],
    [ARCHIVE_UNIT_HOLDING_UNIT, false],
    [ARCHIVE_UNIT_WITH_OBJECTS, true],
    [ARCHIVE_UNIT_WITHOUT_OBJECTS, true],
  ]);

  otherCriteriaOptions$: Observable<ItemNode<SchemaElement>[]>;
  getOtherCriteriaDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

  private queryParams: Params;

  constructor(
    public dialog: MatDialog,
    private formBuilder: FormBuilder,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private translateService: TranslateService,
    private schemaService: SchemaService,
    private queryParamsService: QueryParamsService,
  ) {
    this.otherCriteriaOptions$ = this.schemaService.getDescriptiveSchemaTree();

    this.translateService.onLangChange.subscribe(() => {
      if (this.archiveUnitTypesCriteria.get(ARCHIVE_UNIT_WITH_OBJECTS)) {
        this.synchronizeArchiveUnitCriteria(ARCHIVE_UNIT_WITH_OBJECTS);
      }

      if (this.archiveUnitTypesCriteria.get(ARCHIVE_UNIT_WITHOUT_OBJECTS)) {
        this.synchronizeArchiveUnitCriteria(ARCHIVE_UNIT_WITHOUT_OBJECTS);
      }
    });

    const otherCriteriaListControl = this.formBuilder.control<SchemaElement[]>([]);
    const otherCriteriaControl = this.formBuilder.group({});

    this.form = this.formBuilder.group({
      title: ['', []],
      description: ['', []],
      guidopi: ['', []],
      guid: ['', [Validators.pattern('^[a-z0-9_, ]+')]],
      serviceProdLabel: ['', []],
      serviceProdCode: ['', []],
      beginDt: ['', []],
      endDt: ['', []],
      otherCriteriaList: otherCriteriaListControl,
      otherCriteria: otherCriteriaControl,
    });

    otherCriteriaListControl.valueChanges.subscribe((schemaElements) => {
      const currentPaths = Object.keys(otherCriteriaControl.controls);
      const expectedPaths = schemaElements.map((element) => element.Path);

      this.addMissingControls(expectedPaths, currentPaths, otherCriteriaControl, formBuilder);
      this.removeObsoleteControls(expectedPaths, currentPaths, otherCriteriaControl);
    });

    Object.entries(this.form.controls)
      .filter(([key, _value]) => !['otherCriteriaList'].includes(key))
      .forEach(([key, control]) => {
        control.valueChanges
          .pipe(
            debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
            filter((value) => Boolean(value)),
          )
          .subscribe((value) => {
            this.addCriteriaFromObject({ [key]: value });
            control.reset(undefined, { emitEvent: false });
          });
      });

    this.archiveExchangeDataService
      .receiveRemoveFromChildSearchCriteriaSubject()
      .pipe(
        filter((criteria) => Boolean(criteria?.valueElt?.id)),
        filter((criteria) => Boolean(criteria?.action)),
      )
      .subscribe((criteria) => {
        this.archiveUnitTypesCriteria.set(criteria.valueElt.id, criteria.action === 'ADD');
      });
  }

  ngOnInit() {
    this.managementRulesSharedDataService.getCriteriaSearchListToSave().subscribe((data) => {
      this.criteriaSearchListToSave = data;
    });

    this.criteriaSearchListToSave.forEach((searchCriteria) => {
      if (searchCriteria.criteria === ALL_ARCHIVE_UNIT_TYPES) {
        searchCriteria.values.forEach((criteriaValue) => {
          this.archiveUnitTypesCriteria.set(criteriaValue.id, true);
        });
      }
      searchCriteria.values.forEach((value) => {
        const criteria: SearchCriteriaAddAction = {
          keyElt: searchCriteria.criteria,
          valueElt: value,
          labelElt: value.label,
          keyTranslated: true,
          operator: searchCriteria.operator as CriteriaOperator,
          valueTranslated: this.isValueTranslated(searchCriteria.criteria),
          dataType: searchCriteria.dataType as CriteriaDataType,
          category: searchCriteria.category as SearchCriteriaTypeEnum,
        };

        this.addCriteria(criteria);
      });
    });

    this.queryParamsService.getQueryParams().subscribe((queryParams) => {
      this.queryParams = queryParams;

      const formData: any = this.form.value;
      const hasChanged = Object.entries(queryParams).filter(([key, value]) => formData[key] !== value).length > 0;

      if (!hasChanged) return;

      this.form.patchValue(queryParams, { onlySelf: false });
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
          const criteriaValue = value instanceof Date ? value.toISOString() : value.trim();
          const defaultSearchCriteriaAddAction: Partial<SearchCriteriaAddAction> = {
            valueElt: { value: criteriaValue, id: key },
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
          this.queryParamsService.setQueryParams({ ...this.queryParams, [key]: criteriaValue });
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

  getCriteriaName(criteria: SchemaElement, otherCriteriaOptions: ItemNode<SchemaElement>[]) {
    const path = criteria.Path.split('.').slice(0, -1);
    const parent = path.reduce((acc, p) => acc.children.find((o) => o.item.FieldName === p), {
      children: otherCriteriaOptions,
    } as ItemNode<SchemaElement>);
    return `${criteria.ShortName}${parent?.item ? ` (${parent.item.ShortName})` : ''}`;
  }

  toggleArchiveUnitCriteria(archiveUnitType: ArchiveUnitType, event: any) {
    const action = event.target.checked ? 'ADD' : 'REMOVE';

    this.archiveUnitTypesCriteria.set(archiveUnitType, event.target.checked);
    this.processCriteriaAction(action, archiveUnitType);
  }

  addCriteria(criteria: SearchCriteriaAddAction) {
    const { keyElt, valueElt } = criteria;

    if (!keyElt || !valueElt) return;

    this.archiveExchangeDataService.addSimpleSearchCriteriaSubject(criteria);
  }

  get guid() {
    return this.form.controls.guid;
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

  get guidopi() {
    return this.form.controls.guidopi;
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

  get serviceProdCode() {
    return this.form.controls.serviceProdCode;
  }

  get otherCriteriaList(): AbstractControl<SchemaElement[]> {
    return this.form.controls.otherCriteriaList;
  }

  private addMissingControls(expectedPaths: string[], currentPaths: string[], formGroup: FormGroup, formBuilder: FormBuilder): void {
    expectedPaths.forEach((path) => {
      if (!currentPaths.includes(path)) {
        formGroup.addControl(path, formBuilder.control(undefined));
      }
    });
  }

  private removeObsoleteControls(expectedPaths: string[], currentPaths: string[], formGroup: FormGroup): void {
    currentPaths.forEach((path) => {
      if (!expectedPaths.includes(path)) {
        formGroup.removeControl(path);
      }
    });
  }

  private synchronizeArchiveUnitCriteria(archiveUnitType: ArchiveUnitType) {
    this.processCriteriaAction('REMOVE', archiveUnitType);
    this.processCriteriaAction('ADD', archiveUnitType);
    this.archiveUnitTypesCriteria.set(archiveUnitType, true);
  }

  private getTranslationKey(archiveUnitType: ArchiveUnitType): string {
    const translationPrefix = 'ARCHIVE_SEARCH.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE';

    return `${translationPrefix}.${archiveUnitType}`;
  }

  private processCriteriaAction(action: CriteriaAction, unitType: ArchiveUnitType): void {
    const criteria = this.generateCriteria(unitType);
    const criteriaValue = criteria.valueElt;

    if (action === 'ADD') this.addCriteria(criteria);
    if (action === 'REMOVE') this.removeCriteria(ALL_ARCHIVE_UNIT_TYPES, criteriaValue);
  }

  private removeCriteria(keyElt: string, valueElt?: CriteriaValue) {
    this.archiveExchangeDataService.sendRemoveFromChildSearchCriteriaAction({
      keyElt,
      valueElt,
      action: 'REMOVE',
    });
  }

  private generateCriteria(archiveUnitType: ArchiveUnitType): SearchCriteriaAddAction {
    const translationKey = this.getTranslationKey(archiveUnitType);
    const criteriaValue: CriteriaValue = {
      value: archiveUnitType,
      id: archiveUnitType,
    };

    return {
      keyElt: ALL_ARCHIVE_UNIT_TYPES,
      valueElt: criteriaValue,
      labelElt: this.translateService.instant(translationKey),
      keyTranslated: true,
      operator: CriteriaOperator.EQ,
      valueTranslated: false,
      dataType: CriteriaDataType.STRING,
      category: SearchCriteriaTypeEnum.FIELDS,
    };
  }
}
