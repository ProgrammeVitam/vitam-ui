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
import { AfterViewInit, Component, ElementRef, OnInit, QueryList, ViewChildren } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import {
  CountryOption,
  CountryService,
  FormFieldValueWrapperComponent,
  Option,
  SelectComponent,
  SlideToggleModule,
  VitamuiSelectOptions,
} from 'vitamui-library';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AsyncPipe, JsonPipe } from '@angular/common';
import { extend } from 'underscore';
import { Observable } from 'rxjs';
import { delay, map } from 'rxjs/operators';

@Component({
  imports: [
    AsyncPipe,
    FormFieldValueWrapperComponent,
    FormsModule,
    JsonPipe,
    ReactiveFormsModule,
    SelectComponent,
    SlideToggleModule,
    TranslatePipe,
  ],
  templateUrl: './design-system-select.component.html',
  styleUrl: './design-system-select.component.scss',
})
export class DesignSystemSelectComponent implements OnInit, AfterViewInit {
  configs: {
    name: string;
    multiple?: boolean;
    entries: { type: string; states: { id: string; control: FormControl }[] }[];
  }[];

  enableSearch = false;
  enableSelectAll = false;
  enableDisplaySelected = false;

  multiSelectOptions$: Observable<VitamuiSelectOptions>;

  wrapperControl = new FormControl('FR');

  @ViewChildren(SelectComponent, { read: ElementRef }) components: QueryList<ElementRef>;

  constructor(private countryService: CountryService) {}

  ngOnInit() {
    this.initMultiselectOptions();
  }

  ngAfterViewInit() {
    this.multiSelectOptions$.subscribe((options) => {
      const selectedOptionValue = options.options[0].key;
      this.configs = [
        {
          name: 'Simple select',
          multiple: false,
          entries: this.getEntries(selectedOptionValue),
        },
        {
          name: 'Multiple select',
          multiple: true,
          entries: this.getEntries([selectedOptionValue]),
        },
      ];
    });

    this.multiSelectOptions$.pipe(delay(1)).subscribe(() => {
      this.components.forEach((component) => {
        const nativeElement = component.nativeElement as HTMLElement;
        const isActive = nativeElement.getAttribute('data-active') === 'true';
        if (isActive) nativeElement.querySelector('mat-select').dispatchEvent(new Event('focus'));
      });
    });
  }

  private getEntries(selectedOptionValue: any) {
    return [
      {
        type: 'empty',
        states: [
          { id: 'Default', control: this.createControl() },
          { id: 'Active', control: this.createControl() },
          { id: 'Disabled', control: this.createControl({ disabled: true }) },
          { id: 'Error', control: this.createControl({ error: true }) },
        ],
      },
      {
        type: 'full',
        states: [
          { id: 'Default', control: this.createControl({ value: selectedOptionValue }) },
          { id: 'Active', control: this.createControl({ value: selectedOptionValue }) },
          { id: 'Disabled', control: this.createControl({ disabled: true, value: selectedOptionValue }) },
          { id: 'Error', control: this.createControl({ error: true, value: selectedOptionValue }) },
        ],
      },
    ];
  }

  private createControl(config?: { disabled?: boolean; error?: boolean; value?: any }): FormControl {
    const validators = config?.error || config?.disabled ? [Validators.required, Validators.pattern('.*GB.*')] : [];

    const fc = new FormControl(null, validators);
    if (config?.disabled) fc.disable();
    if (config?.error) fc.markAsTouched();
    if (config?.value) fc.setValue(config.value);

    return fc;
  }

  private initMultiselectOptions(): void {
    this.multiSelectOptions$ = this.countryService.getAvailableCountries().pipe(
      map((values: CountryOption[]) => {
        const countries = values.map((value) =>
          extend({
            key: value.code,
            label: value.name,
            disabled: value.code === 'DK',
          }),
        );
        return { options: countries, customSorting: this.sortAlphabetically };
      }),
    );
  }

  private sortAlphabetically = (a: Option, b: Option): number => {
    return a.label.toLocaleLowerCase() > b.label.toLocaleLowerCase() ? 1 : -1;
  };
  protected readonly Object = Object;
}
