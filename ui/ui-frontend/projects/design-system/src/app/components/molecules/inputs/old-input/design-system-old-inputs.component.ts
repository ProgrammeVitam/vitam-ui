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
import { Component, inject, OnInit, signal } from '@angular/core';
import { disabled, form, FormField, maxLength, SchemaPathTree } from '@angular/forms/signals';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import {
  CountryOption,
  CountryService,
  EditablePatternsComponent,
  Option,
  VitamUICommonModule,
  VitamUILibraryModule,
} from 'vitamui-library';

import { TranslatePipe } from '@ngx-translate/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { extend } from 'underscore';

interface OldInputsForm {
  pattern: string[] | null;
  toggleGroup: string | null;
  streetEmpty: string;
  streetDisabled: string;
  editablePatterns: string[] | null;
  emailFirstPart: string;
  email: string;
  domain: string;
  country: string;
  textarea: string;
  level: string;
  toggle: string;
  file: File;
}

function oldInputsSchema(path: SchemaPathTree<OldInputsForm>) {
  maxLength(path.streetEmpty, 3);
  maxLength(path.streetDisabled, 6);
  maxLength(path.emailFirstPart, 25);
  maxLength(path.email, 25);
  maxLength(path.domain, 10);
  maxLength(path.country, 10);
  maxLength(path.textarea, 25);
  maxLength(path.level, 10);
}

@Component({
  selector: 'design-system-old-inputs',
  imports: [
    EditablePatternsComponent,
    MatButtonToggleModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    FormField,
    TranslatePipe,
    VitamUICommonModule,
    VitamUILibraryModule,
  ],
  templateUrl: './design-system-old-inputs.component.html',
  styleUrl: './design-system-old-inputs.component.scss',
})
export class DesignSystemOldInputsComponent implements OnInit {
  private readonly countryService = inject(CountryService);

  patternOptions = [
    { value: 'pattern 1', disabled: false },
    { value: 'pattern 2', disabled: false },
  ];

  editablePatternsOptions = [
    { value: 'value 1', disabled: false },
    { value: 'value 2', disabled: false },
  ];

  // The same model is shared by the enabled and the disabled versions of the components
  readonly model = signal<OldInputsForm>({
    pattern: null,
    toggleGroup: null,
    streetEmpty: '',
    streetDisabled: 'azerty',
    editablePatterns: null,
    emailFirstPart: 'azerty',
    email: 'azerty@test.fr',
    domain: 'test.fr',
    country: 'FR',
    textarea: 'name\naddress\ncity',
    level: 'LEVEL',
    toggle: 'Value 3',
    file: new File(['test'], 'test', { type: 'text/plain' }),
  });
  readonly fields = form(this.model, oldInputsSchema);
  readonly disabledFields = form(this.model, (path) => {
    oldInputsSchema(path);
    disabled(path);
  });

  countries: Option[] = [];

  ngOnInit() {
    this.countryService.getAvailableCountries().subscribe((values: CountryOption[]) => {
      this.countries = values.map((value) =>
        extend({
          key: value.code,
          label: value.name,
        }),
      );
    });
  }
}
