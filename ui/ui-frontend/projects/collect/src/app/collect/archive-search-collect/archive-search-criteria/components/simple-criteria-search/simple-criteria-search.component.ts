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

import { ChangeDetectorRef, Component, effect, inject, Injector, OnInit, signal, untracked } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { Validators } from '@angular/forms';
import { FieldTree, form, FormField, pattern } from '@angular/forms/signals';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';
import { debounceTime, filter, map, share, skip } from 'rxjs/operators';
import {
  AgencyService,
  ArchiveUnitProfilesService,
  ConfigService,
  CriteriaAction,
  CriteriaDataType,
  CriteriaOperator,
  CriteriaValue,
  DatepickerComponent,
  EditableInputComponent,
  ItemNode,
  Option,
  SchemaElement,
  SchemaService,
  SearchCriteriaAddAction,
  SearchCriteriaEltDto,
  SearchCriteriaService,
  SearchCriteriaTypeEnum,
  SearchProvider,
  SelectComponent,
  SelectWithTreeComponent,
  TooltipDirective,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { ArchiveSearchConstsEnum } from '../../models/archive-search-consts-enum';
import { ArchiveSharedDataService } from '../../../../core/archive-shared-data.service';
import { ManagementRulesSharedDataService } from '../../services/management-rules-shared-data.service';
import { ActivatedRoute, Params } from '@angular/router';
import { ArchiveSearchHelperService } from '../../services/archive-search-helper.service';
import { MatCheckbox, MatCheckboxChange } from '@angular/material/checkbox';
import { AsyncPipe, CommonModule, NgTemplateOutlet } from '@angular/common';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { OverlayModule } from '@angular/cdk/overlay';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';

const FINAL_ACTION_TYPE = 'FINAL_ACTION_TYPE';
const ARCHIVE_UNIT_WITH_OBJECTS = 'ARCHIVE_UNIT_WITH_OBJECTS';
const ARCHIVE_UNIT_WITHOUT_OBJECTS = 'ARCHIVE_UNIT_WITHOUT_OBJECTS';
const ALL_ARCHIVE_UNIT_TYPES = 'ALL_ARCHIVE_UNIT_TYPES';
const ERRORS = 'ERRORS';
const ARCHIVE_UNIT_WITH_ERRORS = 'ARCHIVE_UNIT_WITH_ERRORS';

type ArchiveUnitType =
  | 'ARCHIVE_UNIT_FILING_UNIT'
  | 'ARCHIVE_UNIT_HOLDING_UNIT'
  | 'ARCHIVE_UNIT_WITH_OBJECTS'
  | 'ARCHIVE_UNIT_WITHOUT_OBJECTS'
  | 'ARCHIVE_UNIT_WITH_ERRORS';

const GUID_PATTERN = '^[a-z0-9_, ]+';

interface SimpleCriteriaSearchForm {
  title: string;
  description: string;
  guid: string;
  beginDt: Date | null;
  endDt: Date | null;
  // Keys (multi-select) when the referential service is available, free text otherwise
  agencies: string[] | string | null;
  archiveUnitProfiles: string[] | string | null;
  otherCriteriaList: SchemaElement[];
  // Value of each selected "other criteria", indexed by its path
  otherCriteria: Record<string, unknown>;
}

type SimpleCriteriaKey = Exclude<keyof SimpleCriteriaSearchForm, 'otherCriteriaList'>;

const SIMPLE_CRITERIA_KEYS: SimpleCriteriaKey[] = [
  'title',
  'description',
  'guid',
  'beginDt',
  'endDt',
  'otherCriteria',
  'agencies',
  'archiveUnitProfiles',
];

const keysList = [ALL_ARCHIVE_UNIT_TYPES, ERRORS];

@Component({
  selector: 'app-simple-criteria-search',
  templateUrl: './simple-criteria-search.component.html',
  styleUrls: ['./simple-criteria-search.component.css'],
  imports: [
    FormField,
    TooltipDirective,
    SelectComponent,
    DatepickerComponent,
    NgTemplateOutlet,
    MatCheckbox,
    SelectWithTreeComponent,
    AsyncPipe,
    TranslatePipe,
    CommonModule,
    EditableInputComponent,
    MatButtonToggleModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    OverlayModule,
  ],
})
export class SimpleCriteriaSearchComponent implements OnInit {
  dialog = inject(MatDialog);
  private readonly archiveExchangeDataService = inject(ArchiveSharedDataService);
  private readonly managementRulesSharedDataService = inject(ManagementRulesSharedDataService);
  private readonly translateService = inject(TranslateService);
  private readonly route = inject(ActivatedRoute);
  private readonly searchCriteriaService = inject(SearchCriteriaService);
  private readonly archiveHelperService = inject(ArchiveSearchHelperService);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly injector = inject(Injector);

  private readonly model = signal<SimpleCriteriaSearchForm>({
    title: '',
    description: '',
    guid: '',
    beginDt: null,
    endDt: null,
    agencies: [],
    archiveUnitProfiles: [],
    otherCriteriaList: [],
    otherCriteria: {},
  });
  readonly form = form(this.model, (path) => {
    pattern(path.guid, new RegExp(`${GUID_PATTERN}$`));
  });
  // Also given to the editable input, which validates the value before confirming it
  readonly guidValidator = Validators.pattern(GUID_PATTERN);
  criteriaSearchListToSave: SearchCriteriaEltDto[] = [];

  archiveUnitTypesCriteria: Map<any, boolean> = new Map<any, boolean>([
    [ARCHIVE_UNIT_WITH_OBJECTS, false],
    [ARCHIVE_UNIT_WITHOUT_OBJECTS, false],
    [ARCHIVE_UNIT_WITH_ERRORS, false],
  ]);

  otherCriteriaOptions = signal<ItemNode<SchemaElement>[]>(undefined);
  getOtherCriteriaDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

  selectOptions = signal({
    agency: { options: [] as Option[] },
    archiveUnitProfile: { options: [] as Option[] },
  } satisfies { [key: string]: VitamuiSelectOptions });
  private readonly offlineServices$: Observable<SearchProvider[]>;

  constructor() {
    const schemaService = inject(SchemaService);
    const agencyService = inject(AgencyService);
    const archiveUnitProfilesService = inject(ArchiveUnitProfilesService);
    const configService = inject(ConfigService);

    this.offlineServices$ = configService.config$
      .pipe(map((configuration) => configuration.COLLECT))
      .pipe(map((config) => config?.OFFLINE_SERVICES));

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
      .subscribe((options) => {
        this.selectOptions.update((selectOptions) => ({ ...selectOptions, agency: options }));
      });

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
      .subscribe((options) => {
        this.selectOptions.update((selectOptions) => ({ ...selectOptions, archiveUnitProfile: options }));
      });

    const descriptiveSchemaTree$ = schemaService.getDescriptiveSchemaTree().pipe(share());
    descriptiveSchemaTree$.subscribe((schema) => {
      this.otherCriteriaOptions.set(schema);
    });

    // Keep one "other criteria" field for each selected schema element
    effect(() => {
      const expectedPaths = this.form
        .otherCriteriaList()
        .value()
        .map((element) => element.Path);
      untracked(() => this.syncOtherCriteriaFields(expectedPaths));
    });

    // Sync archive unit types with criteria
    this.archiveExchangeDataService.searchCriteria$.pipe(filter((searchCriteria) => !!searchCriteria)).subscribe((searchCriteria) => {
      const searchCriteriasValues = [...searchCriteria.entries()]
        .filter(([key]) => keysList.includes(key))
        .flatMap(([_, value]) => value.values)
        .map((test) => test.value.value);
      this.archiveUnitTypesCriteria.forEach((_, key) => {
        this.archiveUnitTypesCriteria.set(key, searchCriteriasValues.includes(key));
      });
      // The map is mutated in place: request a refresh explicitly (zoneless).
      this.cdr.markForCheck();
    });

    this.offlineServices$.subscribe((offlineServices) => {
      const isAgenciesOffline = offlineServices?.includes('agencies');
      const isPUAOffline = offlineServices?.includes('archive-unit-profiles');

      // The multi-selects (online referentials) only commit their value when they are closed
      const updateOnBlurKeys: SimpleCriteriaKey[] = [
        ...(isAgenciesOffline ? [] : ['agencies' as const]),
        ...(isPUAOffline ? [] : ['archiveUnitProfiles' as const]),
      ];

      SIMPLE_CRITERIA_KEYS.forEach((key) => {
        const field = this.simpleCriteriaField(key);
        this.committedValues$(key, updateOnBlurKeys.includes(key))
          .pipe(
            filter(() => field().valid()),
            debounceTime(ArchiveSearchConstsEnum.UPDATE_DEBOUNCE_TIME),
            filter((value) => Boolean(value)),
          )
          .subscribe((value) => {
            this.addCriteriaFromParams({ [key]: value });
            this.resetField(key);
          });
      });
    });
  }

  /**
   * Values committed by the user in the field: every value change or, for fields updated on blur,
   * the value when the field is blurred after being changed.
   */
  private committedValues$(key: SimpleCriteriaKey, updateOnBlur: boolean): Observable<unknown> {
    const field = this.simpleCriteriaField(key);
    if (!updateOnBlur) {
      return toObservable(field().value, { injector: this.injector }).pipe(skip(1));
    }
    return toObservable(field().touched, { injector: this.injector }).pipe(
      filter(Boolean),
      map(() => {
        const dirty = field().dirty();
        // Back to untouched/pristine, to detect the next blur
        field().reset();
        return dirty ? field().value() : null;
      }),
    );
  }

  private simpleCriteriaField(key: SimpleCriteriaKey): FieldTree<unknown> {
    return this.form[key] as FieldTree<unknown>;
  }

  private resetField(key: SimpleCriteriaKey) {
    if (key === 'otherCriteria') {
      const otherCriteria = this.form.otherCriteria().value();
      this.form.otherCriteria().reset(Object.fromEntries(Object.keys(otherCriteria).map((path): [string, unknown] => [path, null])));
    } else {
      this.simpleCriteriaField(key)().reset(null);
    }
  }

  private syncOtherCriteriaFields(expectedPaths: string[]) {
    const otherCriteria = this.form.otherCriteria().value();
    const currentPaths = Object.keys(otherCriteria);
    if (currentPaths.length === expectedPaths.length && expectedPaths.every((path) => currentPaths.includes(path))) {
      return;
    }
    this.form
      .otherCriteria()
      .value.set(Object.fromEntries(expectedPaths.map((path) => [path, path in otherCriteria ? otherCriteria[path] : null])));
  }

  private addCriteriaFromParams(params: Params) {
    Object.entries(params).forEach(async ([key, value]) =>
      this.archiveExchangeDataService.addSimpleSearchCriteriaSubjects(await this.searchCriteriaService.toSearchCriteria({ [key]: value })),
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

  isValueTranslated(criteria: string) {
    return criteria === FINAL_ACTION_TYPE || keysList.includes(criteria);
  }

  getCriteriaName(criteria: SchemaElement) {
    const path = criteria.Path.split('.').slice(0, -1);
    const parent = path.reduce((acc, p) => acc.children.find((o) => o.item.FieldName === p), {
      children: this.otherCriteriaOptions(),
    } as ItemNode<SchemaElement>);
    return `${criteria.ShortName}${parent?.item ? ` (${parent.item.ShortName})` : ''}`;
  }

  toggleArchiveUnitCriteria(archiveUnitType: ArchiveUnitType, event: MatCheckboxChange) {
    this.archiveUnitTypesCriteria.set(archiveUnitType, event.checked);
    const action = event.checked ? 'ADD' : 'REMOVE';
    this.processArchiveUnitTypeCriteriaAction(action, archiveUnitType);
  }

  addCriteria(criteria: SearchCriteriaAddAction) {
    const { keyElt, valueElt } = criteria;

    if (!keyElt || !valueElt) return;

    this.archiveExchangeDataService.addSimpleSearchCriteriaSubject(criteria);
  }

  isProvided(service: SearchProvider): Observable<boolean> {
    return this.offlineServices$.pipe(map((offlineServices) => !offlineServices?.includes(service)));
  }

  private getTranslationKey(archiveUnitType: ArchiveUnitType): string {
    const translationPrefix = 'COLLECT.SEARCH_CRITERIA_FILTER.FIELDS.UNIT_TYPE';

    return `${translationPrefix}.${archiveUnitType}`;
  }

  private processArchiveUnitTypeCriteriaAction(action: CriteriaAction, unitType: ArchiveUnitType): void {
    const criteria = this.generateArchiveUnitTypeCriteria(unitType);
    const criteriaValue = criteria.valueElt;

    if (action === 'ADD') this.addCriteria(criteria);
    if (action === 'REMOVE') this.removeCriteria(criteria.keyElt, criteriaValue);
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
      keyElt: archiveUnitType === (ARCHIVE_UNIT_WITH_ERRORS as ArchiveUnitType) ? ERRORS : ALL_ARCHIVE_UNIT_TYPES,
      valueElt: criteriaValue,
      labelElt: this.translateService.instant(translationKey),
      keyTranslated: true,
      operator: archiveUnitType === (ARCHIVE_UNIT_WITH_ERRORS as ArchiveUnitType) ? CriteriaOperator.EXISTS : CriteriaOperator.EQ,
      valueTranslated: false,
      dataType: CriteriaDataType.STRING,
      category: SearchCriteriaTypeEnum.FIELDS,
    };
  }
}
