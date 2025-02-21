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
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { CountryOption, CountryService, Option, VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';
import { EditableFieldModule } from '../../../../../../../identity/src/app/shared/editable-field';
import { NgForOf, NgIf } from '@angular/common';
import { TranslateModule } from '@ngx-translate/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { extend } from 'underscore';
import { MAT_MOMENT_DATE_ADAPTER_OPTIONS } from '@angular/material-moment-adapter';
import { MatNativeDateModule } from '@angular/material/core';

@Component({
  selector: 'design-system-old-inputs',
  standalone: true,
  imports: [
    EditableFieldModule,
    MatButtonToggleModule,
    MatDatepickerModule,
    MatFormFieldModule,
    MatInputModule,
    MatNativeDateModule,
    MatSelectModule,
    NgForOf,
    NgIf,
    ReactiveFormsModule,
    TranslateModule,
    VitamUICommonModule,
    VitamUILibraryModule,
  ],
  templateUrl: './design-system-old-inputs.component.html',
  styleUrl: './design-system-old-inputs.component.scss',
  providers: [{ provide: MAT_MOMENT_DATE_ADAPTER_OPTIONS, useValue: { useUtc: true } }],
})
export class DesignSystemOldInputsComponent implements OnInit {
  patternOptions = [
    { value: 'pattern 1', disabled: false },
    { value: 'pattern 2', disabled: false },
  ];
  patternControl = new FormControl();

  datePickerControl = new FormControl();

  control = new FormControl();
  autoCompleteSelect = new FormControl();
  autoCompleteSelectDisabled = new FormControl();

  streetEmpty = new FormControl('', [Validators.maxLength(3)]);
  streetInvalid = new FormControl('azerty', [Validators.maxLength(3)]);
  streetDisable = new FormControl('azerty', [Validators.maxLength(6)]);

  editablePatterns = new FormControl();
  editablePatternsOptions = [
    { value: 'value 1', disabled: false },
    { value: 'value 2', disabled: false },
  ];

  emailFirstPart = new FormControl('azerty', [Validators.maxLength(25)]);
  email = new FormControl('azerty@test.fr', [Validators.maxLength(25)]);
  domain = new FormControl('test.fr', [Validators.maxLength(10)]);
  emails = new FormControl(['azerty@test.fr', 'azerty@test2.com'], [Validators.maxLength(30)]);

  list = new FormControl(['azerty1', 'azerty2'], [Validators.maxLength(30)]);
  country = new FormControl('FR', [Validators.maxLength(10)]);
  textarea = new FormControl('name\naddress\ncity', [Validators.maxLength(25)]);
  level = new FormControl('LEVEL', [Validators.maxLength(10)]);
  toggle = new FormControl('Value 3');

  duration = new FormControl({ days: 5, hours: 10, minutes: 5 });
  file = new FormControl(new File(['test'], 'test', { type: 'text/plain' }));

  countries: Option[] = [];

  constructor(private countryService: CountryService) {}

  ngOnInit() {
    this.initMultiselectOptions();
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
    });
    this.autoCompleteSelectDisabled.disable({ emitEvent: false });
  }
}
