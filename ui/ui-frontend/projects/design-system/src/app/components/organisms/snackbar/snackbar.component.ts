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
import { Component, effect, inject, signal, untracked } from '@angular/core';
import { applyEach, debounce, form, FormField } from '@angular/forms/signals';
import { SnackBarService, VitamUICommonModule, VitamUILibraryModule } from 'vitamui-library';

const INFINITE_DURATION = 0;

interface SnackbarButtonForm {
  label: string;
  url: string;
  outline: boolean;
}

interface SnackbarForm {
  icon: string | null;
  message: string;
  buttons: SnackbarButtonForm[];
}

@Component({
  templateUrl: './snackbar.component.html',
  styleUrls: ['./snackbar.component.scss'],
  imports: [VitamUICommonModule, VitamUILibraryModule, FormField],
})
export class SnackbarComponent {
  private readonly snackBarService = inject(SnackBarService);

  iconOptions: string[] = ['vitamui-icon-user', 'vitamui-icon-agent', 'vitamui-icon-contrat', 'vitamui-icon-link'];

  readonly form = form(signal<SnackbarForm>({ icon: null, message: 'Some message', buttons: [] }), (path) => {
    debounce(path.message, 'blur');
    applyEach(path.buttons, (button) => {
      debounce(button.label, 'blur');
      debounce(button.url, 'blur');
    });
  });

  constructor() {
    // (Re)opens the snackbar with the current configuration, initially and on each change
    effect(() => {
      const value = this.form().value();
      untracked(() => this.snackBarService.open({ ...value, duration: INFINITE_DURATION }));
    });
  }

  addButton() {
    this.form.buttons().value.update((buttons) => [...buttons, { label: 'Click me!', url: '', outline: false }]);
  }

  removeButton(i: number) {
    this.form.buttons().value.update((buttons) => buttons.filter((_, index) => index !== i));
  }
}
