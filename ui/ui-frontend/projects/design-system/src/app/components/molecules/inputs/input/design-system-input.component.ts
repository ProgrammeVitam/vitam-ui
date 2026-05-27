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
import { Component } from '@angular/core';
import { InputComponent } from '../../../../../../../vitamui-library/src/lib/components/input/input.component';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'design-system-input',
  imports: [ReactiveFormsModule, InputComponent],
  templateUrl: './design-system-input.component.html',
  styleUrl: './design-system-input.component.scss',
})
export class DesignSystemInputComponent {
  configs: {
    name: string;
    multiple?: boolean;
    textarea?: boolean;
    entries: { type: string; states: { id: string; control: FormControl }[] }[];
  }[];

  constructor() {
    this.configs = [
      {
        name: 'Simple input',
        multiple: false,
        textarea: false,
        entries: this.getEntries('Test'),
      },
      {
        name: 'Multiple input',
        multiple: true,
        textarea: false,
        entries: this.getEntries(['Test 1', 'Test 2']),
      },
      {
        name: 'Simple textarea',
        multiple: false,
        textarea: true,
        entries: this.getEntries(
          'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.',
        ),
      },
      {
        name: 'Multiple textarea',
        multiple: true,
        textarea: true,
        entries: this.getEntries([
          'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.',
          'Consectetur adipiscing elit, sed do eiusmod ut labore et dolore magna. Ut enim ad minim veniam, quis laboris nisi ut aliquip ex ea commodo consequat. ',
        ]),
      },
    ];
  }

  private getEntries(value: string | string[]) {
    return [
      {
        type: 'empty',
        states: [
          { id: 'Default', control: this.createControl() },
          { id: 'Active', control: this.createControl() },
          { id: 'Disabled', control: this.createControl({ disabled: true, error: true }) },
          { id: 'Error', control: this.createControl({ error: true }) },
        ],
      },
      {
        type: 'full',
        states: [
          { id: 'Default', control: this.createControl({ value: value }) },
          { id: 'Active', control: this.createControl({ value: value }) },
          { id: 'Disabled', control: this.createControl({ disabled: true, error: true, value: value }) },
          { id: 'Error', control: this.createControl({ error: true, value: value }) },
        ],
      },
    ];
  }

  private createControl(config?: { disabled?: boolean; error?: boolean; value?: any }): FormControl {
    const validators = config?.error ? [Validators.required, Validators.pattern('.*TextContent.*')] : [];

    const fc = new FormControl(null, validators);
    if (config?.disabled) fc.disable();
    if (config?.error) fc.markAsTouched();
    if (config?.value) fc.setValue(config.value);

    return fc;
  }
}
