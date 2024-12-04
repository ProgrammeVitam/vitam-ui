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
import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import {
  CountryOption,
  CountryService,
  ItemNode,
  Option,
  SchemaElement,
  SchemaService,
  VitamuiAutocompleteMultiselectOptions,
  VitamUIAutocompleteMultiSelectTreeModule,
} from 'vitamui-library';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { takeUntil } from 'rxjs/operators';
import { extend } from 'underscore';
import { Subject } from 'rxjs';
import { NgIf } from '@angular/common';
import { MockSchemaService } from '../../../../../../../vitamui-library/src/app/modules/schema/mock-schema.service';

@Component({
  selector: 'design-system-autocomplete-multi-select-tree',
  standalone: true,
  imports: [VitamUIAutocompleteMultiSelectTreeModule, ReactiveFormsModule, TranslateModule, NgIf],
  templateUrl: './design-system-autocomplete-multi-select-tree.component.html',
  styleUrl: './design-system-autocomplete-multi-select-tree.component.scss',
  providers: [{ provide: SchemaService, useClass: MockSchemaService }],
})
export class DesignSystemAutocompleteMultiSelectTreeComponent implements OnInit, OnDestroy {
  public autoCompleteSelect = new FormControl();
  public autoCompleteSelectDisabled = new FormControl();
  public autoCompleteMultiSelectTree = new FormControl();
  public autoCompleteMultiSelectTree2 = new FormControl();

  public multiSelectOptions: VitamuiAutocompleteMultiselectOptions;
  public schemaOptions: ItemNode<SchemaElement>[] = [];
  public countries: Option[] = [];

  private readonly destroyer$ = new Subject<void>();

  constructor(
    private countryService: CountryService,
    private translateService: TranslateService,
    private schemaService: SchemaService,
  ) {}

  ngOnInit() {
    this.initMultiselectOptions();
    this.initSchemaOptions();
    this.translateService.onLangChange.pipe(takeUntil(this.destroyer$)).subscribe(() => {
      this.updateCountryTranslation();
    });
  }

  public getSchemaElementDisplayValue = (element: SchemaElement) =>
    `${element.Origin === 'EXTERNAL' ? 'EXT-' : ''}${element.ShortName} - ${element.FieldName}`;

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

  private updateCountryTranslation(): void {
    this.countries.forEach((country) => {
      country.label = this.countryService.getTranslatedCountryNameByCode(country.key);
    });
  }

  private sortAlphabetically = (a: Option, b: Option): number => {
    return a.label.toLocaleLowerCase() > b.label.toLocaleLowerCase() ? 1 : -1;
  };

  ngOnDestroy() {
    this.destroyer$.next();
    this.destroyer$.complete();
  }
}
