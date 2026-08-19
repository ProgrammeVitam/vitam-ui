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
import { Component, inject } from '@angular/core';
import { PickerType } from 'vitamui-library';
import { DatepickerComponent, SelectComponent } from 'vitamui-library';
import { FormControl, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { DatePipe, TitleCasePipe } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';

type Row = { default: FormControl; active: FormControl; disabled: FormControl; error: FormControl };

@Component({
  selector: 'design-system-datepicker',
  imports: [ReactiveFormsModule, DatepickerComponent, MatFormFieldModule, TitleCasePipe, FormsModule, SelectComponent],
  templateUrl: './design-system-datepicker.component.html',
  styleUrl: './design-system-datepicker.component.scss',
})
export class DesignSystemDatepickerComponent {
  private datePipe = inject(DatePipe);

  startDate = new FormControl();
  endDate = new FormControl();

  formats = {
    'dd/MM/yyyy': new Map<PickerType, string>([
      ['day', 'dd/MM/yyyy'],
      ['month', 'MM/yyyy'],
      ['year', 'yyyy'],
    ]),
    'dd-MM-yyyy': new Map<PickerType, string>([
      ['day', 'dd-MM-yyyy'],
      ['month', 'MM-yyyy'],
      ['year', 'yyyy'],
    ]),
    'yyyy/MM/dd': new Map<PickerType, string>([
      ['day', 'yyyy/MM/dd'],
      ['month', 'yyyy/MM'],
      ['year', 'yyyy'],
    ]),
    'yyyy-MM-dd': new Map<PickerType, string>([
      ['day', 'yyyy-MM-dd'],
      ['month', 'yyyy-MM'],
      ['year', 'yyyy'],
    ]),
  };
  availableFormats = Object.keys(this.formats);
  selectedFormat: keyof typeof this.formats = 'dd/MM/yyyy';

  configs: {
    outputType: 'String' | 'Date';
    description: string;
    items: {
      pickerType: PickerType;
      rows: {
        empty: Row;
        full: Row;
      };
    }[];
  }[];

  columns: (keyof Row)[] = ['default', 'active', 'disabled', 'error'];

  constructor() {
    this.generateConfig();
  }

  generateConfig() {
    this.configs = [
      {
        outputType: 'String',
        description: 'Those datepickers output a String representing the chosen date.',
        items: this.getItems(),
      },
      {
        outputType: 'Date',
        description: 'Those datepickers output a Date representing the chosen date.',
        items: this.getItems(),
      },
    ];
  }

  private getDate(y: number, m?: number, d?: number): string {
    return m
      ? this.datePipe.transform(d ? new Date(y, m - 1, d) : new Date(y, m - 1), this.formats[this.selectedFormat].get(d ? 'day' : 'month'))
      : String(y);
  }

  private getItems() {
    return ['day', 'month', 'year'].map((pickerType: PickerType) => {
      const value = pickerType === 'year' ? this.getDate(2022) : pickerType === 'month' ? this.getDate(2018, 5) : this.getDate(2022, 6, 16);
      return {
        pickerType: pickerType,
        rows: {
          empty: {
            default: this.createControl({ value: '', error: false, disabled: false, pickerType }),
            active: this.createControl({ value: '', error: false, disabled: false, pickerType }),
            disabled: this.createControl({ value: '', error: false, disabled: true, pickerType }),
            error: this.createControl({ value: '', error: true, disabled: false, pickerType }),
          },
          full: {
            default: this.createControl({ value, error: false, disabled: false, pickerType }),
            active: this.createControl({ value, error: false, disabled: false, pickerType }),
            disabled: this.createControl({ value, error: false, disabled: true, pickerType }),
            error: this.createControl({ value, error: true, disabled: false, pickerType }),
          },
        },
      };
    });
  }

  private createControl(
    { value, error, disabled, pickerType } = { value: '', error: false, disabled: false, pickerType: 'day' },
  ): FormControl {
    const validators = [Validators.required];
    const valueOrError = error && value ? (pickerType === 'year' ? '1' : pickerType === 'month' ? '2018-13' : '2024-02-30') : value;
    const fc = new FormControl(valueOrError, validators);
    if (error) fc.markAsTouched();
    if (disabled) fc.disable();
    return fc;
  }

  protected readonly Object = Object;
}
