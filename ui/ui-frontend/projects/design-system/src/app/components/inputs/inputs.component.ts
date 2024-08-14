import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { MockSchemaService } from 'projects/vitamui-library/src/app/modules/schema/mock-schema.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { extend } from 'underscore';
import {
  CountryOption,
  CountryService,
  ItemNode,
  Option,
  SchemaElement,
  SchemaService,
  VitamuiAutocompleteMultiselectOptions,
} from 'vitamui-library';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'design-system-inputs',
  templateUrl: './inputs.component.html',
  styleUrls: ['./inputs.component.scss'],
  providers: [CountryService, { provide: SchemaService, useClass: MockSchemaService }],
})
export class InputsComponent implements OnInit, OnDestroy {
  public control = new FormControl();

  public repeatableEmpty = new FormControl(['']);
  public repeatableOneValue = new FormControl(['Lorem Ipsum']);
  public repeatableThreeValues = new FormControl(['Lorem Ipsum', 'Index géographique des archives départementales de la Vendée', 'Affred']);
  public repeatableDisabled = (() => {
    const fc = new FormControl(['Lorem Ipsum', 'Index géographique des archives départementales de la Vendée', 'Affred']);
    fc.disable();
    return fc;
  })();
  public repeatableTextareaEmpty = new FormControl(['']);
  public repeatableTextareaOneValue = new FormControl([
    'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.',
  ]);
  public repeatableTextareaTwoValues = new FormControl([
    'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.',
    'Consectetur adipiscing elit, sed do eiusmod ut labore et dolore magna. Ut enim ad minim veniam, quis laboris nisi ut aliquip ex ea commodo consequat. ',
  ]);
  public repeatableTextareaDisabled = (() => {
    const fc = new FormControl([
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.',
      'Consectetur adipiscing elit, sed do eiusmod ut labore et dolore magna. Ut enim ad minim veniam, quis laboris nisi ut aliquip ex ea commodo consequat. ',
    ]);
    fc.disable();
    return fc;
  })();

  public streetEmpty = new FormControl('', [Validators.maxLength(3)]);
  public streetInvalid = new FormControl('azerty', [Validators.maxLength(3)]);
  public streetDisable = new FormControl('azerty', [Validators.maxLength(6)]);
  public emailFirstPart = new FormControl('azerty', [Validators.maxLength(25)]);
  public email = new FormControl('azerty@test.fr', [Validators.maxLength(25)]);
  public domain = new FormControl('test.fr', [Validators.maxLength(10)]);
  public emails = new FormControl(['azerty@test.fr', 'azerty@test2.com'], [Validators.maxLength(30)]);
  public list = new FormControl(['azerty1', 'azerty2'], [Validators.maxLength(30)]);
  public country = new FormControl('FR', [Validators.maxLength(10)]);
  public textarea = new FormControl('name\naddress\ncity', [Validators.maxLength(25)]);
  public level = new FormControl('LEVEL', [Validators.maxLength(10)]);
  public toggle = new FormControl('Value 3');
  public datePickerControl = new FormControl();
  public duration = new FormControl({ days: 5, hours: 10, minutes: 5 });
  public file = new FormControl(new File(['test'], 'test', { type: 'text/plain' }));

  public countries: Option[] = [];
  public multiSelectOptions: VitamuiAutocompleteMultiselectOptions;
  public schemaOptions: ItemNode<SchemaElement>[] = [];
  public getSchemaElementDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

  public autoCompleteSelect = new FormControl();
  public autoCompleteSelectDisabled = new FormControl();
  public autoCompleteMultiSelect = new FormControl();
  public autoCompleteMultiSelectTree = new FormControl();
  public autoCompleteMultiSelectTree2 = new FormControl();

  public datepickerYearEmpty = new FormControl();
  public datepickerMonthEmpty = new FormControl();
  public datepickerDayEmpty = new FormControl();

  public datepickerYear = new FormControl('2022');
  public datepickerMonth = new FormControl('2018-05');
  public datepickerDay = new FormControl('2022-06-16');

  public datepickerDisabledEmpty = (() => {
    const fc = new FormControl('');
    fc.disable();
    return fc;
  })();
  public datepickerDisabledYear = (() => {
    const fc = new FormControl('2022');
    fc.disable();
    return fc;
  })();
  public datepickerDisabledMonth = (() => {
    const fc = new FormControl('2019-02');
    fc.disable();
    return fc;
  })();
  public datepickerDisabledDay = (() => {
    const fc = new FormControl('2024-01-01');
    fc.disable();
    return fc;
  })();

  public datepickerEmptyError = (() => {
    const fc = new FormControl(null, Validators.required);
    fc.markAsDirty();
    return fc;
  })();
  public datepickerErrorYear = (() => {
    const fc = new FormControl('202255');
    fc.markAsDirty();
    return fc;
  })();
  public datepickerErrorMonth = (() => {
    const fc = new FormControl('2018-13');
    fc.markAsDirty();
    return fc;
  })();
  public datepickerErrorDay = (() => {
    const fc = new FormControl('2024-02-30');
    fc.markAsDirty();
    return fc;
  })();

  public editablePatterns = new FormControl();
  public editablePatternsOptions = [
    { value: 'value 1', disabled: false },
    { value: 'value 2', disabled: false },
  ];

  private readonly destroyer$ = new Subject<void>();

  constructor(
    private countryService: CountryService,
    private translateService: TranslateService,
    private schemaService: SchemaService,
  ) {}

  onChange = (_: any) => {};
  onTouched = () => {};

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  ngOnInit() {
    this.initMultiselectOptions();
    this.initSchemaOptions();
    this.translateService.onLangChange.pipe(takeUntil(this.destroyer$)).subscribe(() => {
      this.updateCountryTranslation();
    });
  }

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  private initMultiselectOptions(): void {
    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((value) =>
        extend({
          key: value.code,
          label: value.name,
        }),
      );
      this.autoCompleteSelect.setValue('DE');
      this.multiSelectOptions = { options: this.countries, customSorting: this.sortAlphabetically };
    });
    this.autoCompleteSelectDisabled.disable({ emitEvent: false });
  }

  private sortAlphabetically = (a: Option, b: Option): number => {
    return a.label.toLocaleLowerCase() > b.label.toLocaleLowerCase() ? 1 : -1;
  };

  private updateCountryTranslation(): void {
    this.countries.forEach((country) => {
      country.label = this.countryService.getTranslatedCountryNameByCode(country.key);
    });
  }

  private initSchemaOptions(): void {
    this.schemaService.getDescriptiveSchemaTree().subscribe((schemaOptions) => {
      this.schemaOptions = schemaOptions;

      this.autoCompleteMultiSelectTree2.setValue([
        schemaOptions.find((o) => o.item.FieldName === 'TextContent').item,
        schemaOptions.find((o) => o.item.FieldName === 'RegisteredDate').item,
        schemaOptions.find((o) => o.item.FieldName === 'Agent').children.find((o) => o.item.FieldName === 'Activity').item,
        schemaOptions.find((o) => o.item.FieldName === 'Agent').children.find((o) => o.item.FieldName === 'DeathDate').item,
      ]);
    });
  }
}
