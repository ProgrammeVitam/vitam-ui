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
import { Component, input, output } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { JsonInput } from './json-input';

/** JSON textareas of a playground, side by side, with a reset button. */
@Component({
  selector: 'design-system-json-inputs',
  imports: [TranslatePipe],
  template: `
    <div class="d-flex justify-content-between align-items-center">
      <p class="m-0">{{ 'OBJECT_VIEWER.PLAYGROUND.INTRO' | translate }}</p>
      <button type="button" class="btn secondary" (click)="reset()">{{ 'OBJECT_VIEWER.PLAYGROUND.RESET' | translate }}</button>
    </div>

    <div class="inputs mt-3">
      @for (jsonInput of inputs(); track jsonInput.label) {
        <div>
          <h3>{{ jsonInput.label | translate }}</h3>
          <textarea
            rows="20"
            spellcheck="false"
            [value]="jsonInput.text()"
            (input)="jsonInput.text.set($any($event.target).value)"
          ></textarea>
          @if (jsonInput.parsed().error; as error) {
            <p class="text-danger">{{ 'OBJECT_VIEWER.PLAYGROUND.INVALID_JSON' | translate }} : {{ error }}</p>
          }
        </div>
      }
    </div>
  `,
  styles: `
    .inputs {
      display: grid;
      grid-auto-columns: minmax(0, 1fr);
      grid-auto-flow: column;
      gap: 1rem;
    }
    textarea {
      width: 100%;
      font-family: monospace;
      font-size: 0.8rem;
    }
  `,
})
export class JsonInputsComponent {
  readonly inputs = input.required<JsonInput<unknown>[]>();
  readonly inputsReset = output<void>();

  reset(): void {
    this.inputs().forEach((jsonInput) => jsonInput.reset());
    this.inputsReset.emit();
  }
}
