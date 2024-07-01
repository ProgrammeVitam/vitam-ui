import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, Validators, ReactiveFormsModule } from '@angular/forms';
import { TranslateService, TranslateModule } from '@ngx-translate/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { extend } from 'underscore';
import {
  CountryOption,
  CountryService,
  Option,
  VitamuiAutocompleteMultiselectOptions,
  VitamUICommonInputComponent,
  EditableFieldComponent,
  VitamUIListInputComponent,
  VitamuiRepeatableInputComponent,
  VitamUIAutocompleteComponent,
  VitamUIAutocompleteMultiSelectModule,
  LevelInputComponent,
  SlideToggleComponent,
  DatepickerComponent,
  VitamUIDurationInputComponent,
  EditableInputComponent,
  EditableEmailInputComponent,
  MultipleEmailInputComponent,
  EditableSelectComponent,
  EditableOptionComponent,
  VitamUIInputErrorComponent,
  EditableTextareaComponent,
  VitamUITextareaComponent,
  EditableLevelInputComponent,
  EditableToggleGroupComponent,
  EditableButtonToggleComponent,
  EditableDurationInputComponent,
  EditableFileComponent,
} from 'vitamui-library';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatLegacyInputModule } from '@angular/material/legacy-input';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { NgFor, NgIf } from '@angular/common';
import { EditablePatternsComponent } from '../../../../../identity/src/app/shared/editable-field/editable-patterns/editable-patterns.component';
import { MatLegacyOptionModule } from '@angular/material/legacy-core';
import { MatLegacySelectModule } from '@angular/material/legacy-select';
import { MatLegacyFormFieldModule } from '@angular/material/legacy-form-field';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'design-system-inputs',
  templateUrl: './inputs.component.html',
  styleUrls: ['./inputs.component.scss'],
  providers: [CountryService],
  standalone: true,
  imports: [
    VitamUICommonInputComponent,
    ReactiveFormsModule,
    EditableFieldComponent,
    MatLegacyFormFieldModule,
    MatLegacySelectModule,
    MatLegacyOptionModule,
    EditablePatternsComponent,
    VitamUIListInputComponent,
    VitamuiRepeatableInputComponent,
    NgFor,
    VitamUIAutocompleteComponent,
    NgIf,
    VitamUIAutocompleteMultiSelectModule,
    LevelInputComponent,
    SlideToggleComponent,
    MatButtonToggleModule,
    DatepickerComponent,
    MatLegacyInputModule,
    MatDatepickerModule,
    VitamUIDurationInputComponent,
    TranslateModule,
    EditableInputComponent,
    EditableEmailInputComponent,
    MultipleEmailInputComponent,
    EditableSelectComponent,
    EditableOptionComponent,
    VitamUIInputErrorComponent,
    EditableTextareaComponent,
    VitamUITextareaComponent,
    EditableLevelInputComponent,
    EditableToggleGroupComponent,
    EditableButtonToggleComponent,
    EditableDurationInputComponent,
    EditableFileComponent,
  ],
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

  public countries: Option[];
  public multiSelectOptions: VitamuiAutocompleteMultiselectOptions;

  public autoCompleteSelect = new FormControl();
  public autoCompleteSelectDisabled = new FormControl();
  public autoCompleteMultiSelect = new FormControl();

  public editablePatterns = new FormControl();
  public editablePatternsOptions = [
    { value: 'value 1', disabled: false },
    { value: 'value 2', disabled: false },
  ];

  private readonly destroyer$ = new Subject<void>();

  constructor(
    private countryService: CountryService,
    private translateService: TranslateService,
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
}
