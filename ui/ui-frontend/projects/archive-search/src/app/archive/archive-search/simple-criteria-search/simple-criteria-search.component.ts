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
import { MatDialog } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import {
  AgencyService,
  ArchiveUnitProfilesService,
  CriteriaAction,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  ItemNode,
  Option,
  SchemaElement,
  SchemaService,
  SearchCriteriaAddAction,
  SearchCriteriaEltDto,
  SearchCriteriaService,
  SearchCriteriaTypeEnum,
  SearchType,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ArchiveSharedDataService } from '../../../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../../../core/management-rules-shared-data.service';
import { debounceTime, filter, map, share } from 'rxjs/operators';
import { combineLatest } from 'rxjs';
import { ArchiveSearchConstsEnum } from '../../models/archive-search-consts-enum';
import { ActivatedRoute, Params } from '@angular/router';
import { ArchiveSearchHelperService } from '../../common-services/archive-search-helper.service';

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

const COMPLEX_INPUTS = ['otherCriteriaList'];

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
    [ARCHIVE_UNIT_WITH_OBJECTS, false],
    [ARCHIVE_UNIT_WITHOUT_OBJECTS, false],
  ]);

  otherCriteriaOptions: ItemNode<SchemaElement>[];
  getOtherCriteriaDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

  titleSearchTypes: SearchType[];
  titleSelectedType?: SearchType;

  selectOptions = {
    agency: { options: [] as Option[] },
    archiveUnitProfile: { options: [] as Option[] },
  } satisfies { [key: string]: VitamuiSelectOptions };

  constructor(
    public dialog: MatDialog,
    private formBuilder: FormBuilder,
    private archiveExchangeDataService: ArchiveSharedDataService,
    private managementRulesSharedDataService: ManagementRulesSharedDataService,
    private translateService: TranslateService,
    private route: ActivatedRoute,
    private searchCriteriaService: SearchCriteriaService,
    private archiveHelperService: ArchiveSearchHelperService,
    schemaService: SchemaService,
    agencyService: AgencyService,
    archiveUnitProfilesService: ArchiveUnitProfilesService,
  ) {
    agencyService
      .getAll()
      .pipe(
        map(
          (agencies): VitamuiSelectOptions => ({
            options: agencies.map((agency) => ({
              key: agency.identifier,
              label: `${agency.identifier} - ${agency.name}`,
              info: agency.description,
            })),
          }),
        ),
      )
      .subscribe((options) => (this.selectOptions.agency = options));

    archiveUnitProfilesService
      .getAll()
      .pipe(
        map(
          (archiveUnitProfiles): VitamuiSelectOptions => ({
            options: archiveUnitProfiles.map((archiveUnitProfile) => ({
              key: archiveUnitProfile.identifier,
              label: `${archiveUnitProfile.identifier} - ${archiveUnitProfile.name}`,
            })),
          }),
        ),
      )
      .subscribe((options) => (this.selectOptions.archiveUnitProfile = options));

    const descriptiveSchemaTree$ = schemaService.getDescriptiveSchemaTree().pipe(share());
    descriptiveSchemaTree$.subscribe((schema) => (this.otherCriteriaOptions = schema));

    const titleSearchTypes$ = descriptiveSchemaTree$.pipe(
      map((schema) => this.searchTypes(schema, 'Title')),
      share(),
    );
    titleSearchTypes$.subscribe((titleSearchTypes) => (this.titleSearchTypes = titleSearchTypes));

    const otherCriteriaListControl = this.formBuilder.control<SchemaElement[]>([]);
    const otherCriteriaControl = this.formBuilder.group({});

    this.form = this.formBuilder.group({
      title: ['', []],
      description: ['', []],
      guidopi: ['', []],
      guid: ['', [Validators.pattern('^[a-z0-9_, ]+')]],
      agencies: [[], { updateOn: 'blur' }],
      archiveUnitProfiles: [[], { updateOn: 'blur' }],
      beginDt: ['', []],
      endDt: ['', []],
      otherCriteriaList: otherCriteriaListControl,
      otherCriteria: otherCriteriaControl,
    });

    otherCriteriaListControl.valueChanges.subscribe((schemaElements) => {
      const currentPaths = Object.keys(otherCriteriaControl.controls);
      const expectedPaths = schemaElements.map((element) => element.Path);

      this.addMissingControls(expectedPaths, currentPaths, otherCriteriaControl);
      this.removeObsoleteControls(expectedPaths, currentPaths, otherCriteriaControl);
    });

    // Sync title type with criteria
    const titleKeys$ = archiveExchangeDataService.searchCriteria$.pipe(
      filter((searchCriteria) => !!searchCriteria),
      map((searchCriteria) => Array.from(searchCriteria.keys())),
      map((criteriaKeys) => criteriaKeys.filter((criteriaKey) => /^TITLE(\.[^.]+)?$/i.test(criteriaKey))),
    );
    combineLatest([titleKeys$, titleSearchTypes$]).subscribe(([titleKeys, titleSearchTypes]) => {
      const hasTitleSearchCriteria = !!titleKeys?.length;
      const type = hasTitleSearchCriteria ? titleKeys[0].split('.')[1] || '' : null;
      titleSearchTypes.forEach((item) => (item.disabled = hasTitleSearchCriteria && item.value !== type));

      if (hasTitleSearchCriteria) this.titleSelectedType = titleSearchTypes.find((item) => item.value === type);
    });

    // Sync archive unit types with criteria
    archiveExchangeDataService.searchCriteria$
      .pipe(
        filter((searchCriteria) => !!searchCriteria),
        map((searchCriteria) => searchCriteria.get(ALL_ARCHIVE_UNIT_TYPES)),
      )
      .subscribe((archiveUnitTypesCriteria) => {
        const selectedArchiveUnitTypes = archiveUnitTypesCriteria?.values.map((criteriaValue) => criteriaValue.value.value);
        this.archiveUnitTypesCriteria.forEach((_value, key) =>
          this.archiveUnitTypesCriteria.set(key, selectedArchiveUnitTypes?.includes(key)),
        );
      });

    Object.entries(this.form.controls)
      .filter(([key, _value]) => !COMPLEX_INPUTS.includes(key))
      .forEach(([key, control]) => {
        control.valueChanges
          .pipe(
            debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
            filter((value) => Boolean(value)),
          )
          .subscribe((value) => {
            this.addCriteriaFromParams({ [key]: value });
            control.reset(undefined, { emitEvent: false });
          });
      });
  }

  private addCriteriaFromParams(params: Params) {
    Object.entries(params).forEach(([key, value]) =>
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubjects(this.searchCriteriaService.toSearchCriteria({ [key]: value })),
    );
  }

  ngOnInit() {
    this.managementRulesSharedDataService.getCriteriaSearchListToSave().subscribe((data) => {
      this.criteriaSearchListToSave = data;
    });

    this.criteriaSearchListToSave.forEach((searchCriteria) => {
      if (searchCriteria.criteria === ALL_ARCHIVE_UNIT_TYPES) {
        searchCriteria.values.forEach((criteriaValue) => {
          this.processArchiveUnitTypeCriteriaAction('ADD', criteriaValue.id as ArchiveUnitType);
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

    this.route.queryParamMap.subscribe((params) => {
      const searchCriteria = this.archiveExchangeDataService.searchCriteria;
      searchCriteria?.forEach((sc) => {
        sc.values?.forEach((value) => this.archiveHelperService.removeCriteria(sc.key, value.value, false, [], searchCriteria, 0));
      });
      if (params.keys.length) {
        this.addCriteriaFromParams(
          Object.fromEntries(
            Object.entries<string>(this.route.snapshot.queryParams).map(([key, value]) => [
              key,
              value
                .toString()
                .split(',')
                .map((v) => decodeURIComponent(v)),
            ]),
          ),
        );
      }
    });
  }

  private searchTypes(schema: ItemNode<SchemaElement>[], path: string): SearchType[] {
    // TODO: only works for a path at the root level
    const customSearchTypes = schema.find((s) => s.item.ApiPath === path).item.CustomSearchTypes;
    return customSearchTypes?.length
      ? ['', ...customSearchTypes].map((type) => ({
          label: this.translateService.instant(`ARCHIVE_SEARCH.SEARCH_CRITERIA_FILTER.SEARCH_TYPES.${type}`),
          value: type,
        }))
      : [];
  }

  isValueTranslated(criteria: string) {
    return criteria === FINAL_ACTION_TYPE || criteria === ALL_ARCHIVE_UNIT_TYPES;
  }

  getCriteriaName(criteria: SchemaElement) {
    const path = criteria.Path.split('.').slice(0, -1);
    const parent = path.reduce((acc, p) => acc.children.find((o) => o.item.FieldName === p), {
      children: this.otherCriteriaOptions,
    } as ItemNode<SchemaElement>);
    return `${criteria.ShortName}${parent?.item ? ` (${parent.item.ShortName})` : ''}`;
  }

  toggleArchiveUnitCriteria(archiveUnitType: ArchiveUnitType, event: any) {
    const action = event.target.checked ? 'ADD' : 'REMOVE';
    this.processArchiveUnitTypeCriteriaAction(action, archiveUnitType);
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

  get beginDt() {
    return this.form.controls.beginDt;
  }

  get endDt() {
    return this.form.controls.endDt;
  }

  get otherCriteriaList(): AbstractControl<SchemaElement[]> {
    return this.form.controls.otherCriteriaList;
  }

  private addMissingControls(expectedPaths: string[], currentPaths: string[], formGroup: FormGroup): void {
    expectedPaths.forEach((path) => {
      if (!currentPaths.includes(path)) {
        formGroup.addControl(path, this.formBuilder.control(undefined));
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

  private getTranslationKey(archiveUnitType: ArchiveUnitType): string {
    const translationPrefix = 'ARCHIVE_SEARCH.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE';

    return `${translationPrefix}.${archiveUnitType}`;
  }

  private processArchiveUnitTypeCriteriaAction(action: CriteriaAction, unitType: ArchiveUnitType): void {
    const criteria = this.generateArchiveUnitTypeCriteria(unitType);
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

  private generateArchiveUnitTypeCriteria(archiveUnitType: ArchiveUnitType): SearchCriteriaAddAction {
    const translationKey = this.getTranslationKey(archiveUnitType);
    const criteriaValue: CriteriaValue = {
      value: archiveUnitType,
      id: 'archiveUnitType',
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
