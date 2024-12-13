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
import { TranslateModule } from '@ngx-translate/core';
import {
  CountryOption,
  CountryService,
  Option,
  VitamUIAutocompleteMultiSelectModule,
  VitamuiAutocompleteMultiselectOptions,
} from 'vitamui-library';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { AsyncPipe, NgIf } from '@angular/common';
import { extend } from 'underscore';
import { Observable } from 'rxjs';
import { map, tap } from 'rxjs/operators';

@Component({
  standalone: true,
  imports: [TranslateModule, VitamUIAutocompleteMultiSelectModule, FormsModule, ReactiveFormsModule, NgIf, AsyncPipe],
  templateUrl: './design-system-autocomplete-multi-select.component.html',
  styleUrl: './design-system-autocomplete-multi-select.component.scss',
})
export class DesignSystemAutocompleteMultiSelectComponent implements OnInit {
  control = new FormControl();
  activeControl = new FormControl();
  disabledControl = (() => {
    const fc = new FormControl('');
    fc.disable();
    return fc;
  })();
  errorControl = (() => {
    const fc = new FormControl(null, [Validators.required]);
    fc.markAsTouched();
    return fc;
  })();

  multiSelectOptions$: Observable<VitamuiAutocompleteMultiselectOptions>;

  constructor(private countryService: CountryService) {}

  ngOnInit() {
    this.initMultiselectOptions();
  }

  private initMultiselectOptions(): void {
    this.multiSelectOptions$ = this.countryService.getAvailableCountries().pipe(
      map((values: CountryOption[]) => {
        const countries = values.map((value) =>
          extend({
            key: value.code,
            label: value.name,
          }),
        );
        return { options: countries, customSorting: this.sortAlphabetically };
      }),
      tap((options) => this.activeControl.setValue([options.options[0].key])),
    );
  }

  private sortAlphabetically = (a: Option, b: Option): number => {
    return a.label.toLocaleLowerCase() > b.label.toLocaleLowerCase() ? 1 : -1;
  };
}
