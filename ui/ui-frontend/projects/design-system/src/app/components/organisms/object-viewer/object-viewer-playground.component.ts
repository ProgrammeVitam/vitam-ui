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
import { Component, computed, inject, signal } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { JsonPipe } from '@angular/common';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import { of, startWith, switchMap } from 'rxjs';
import { map } from 'rxjs/operators';
import { TranslatePipe } from '@ngx-translate/core';
import {
  DisplayRule,
  EditObject,
  EditObjectService,
  Mode,
  ObjectEditorModule,
  ObjectViewerModule,
  ProfiledSchemaElement,
  TemplateService,
} from 'vitamui-library';
import { sampleData, sampleSchema, sampleTemplate } from './object-viewer.sample';

type Parsed<T> = { value?: T; error?: string };

const stringify = (value: unknown): string => JSON.stringify(value, null, 2);

const parse = <T>(text: string): Parsed<T> => {
  try {
    return { value: JSON.parse(text) };
  } catch (error) {
    return { error: (error as Error).message };
  }
};

@Component({
  selector: 'design-system-object-viewer-playground',
  imports: [ObjectViewerModule, ObjectEditorModule, JsonPipe, TranslatePipe, MatRadioGroup, MatRadioButton],
  templateUrl: './object-viewer-playground.component.html',
  styles: `
    textarea {
      width: 100%;
      font-family: monospace;
      font-size: 0.8rem;
    }
    pre {
      background: var(--vitamui-grey-100);
      padding: 1rem;
    }
  `,
})
export class ObjectViewerPlaygroundComponent {
  private readonly editObjectService = inject(EditObjectService);
  private readonly templateService = inject(TemplateService);

  protected readonly modes = Object.values(Mode);
  protected readonly mode = signal<Mode>(Mode.TEMPLATE_DRIVEN);

  protected readonly dataText = signal(stringify(sampleData));
  protected readonly templateText = signal(stringify(sampleTemplate));
  protected readonly schemaText = signal(stringify(sampleSchema));

  protected readonly data = computed(() => parse<any>(this.dataText()));
  protected readonly template = computed(() => parse<DisplayRule[]>(this.templateText()));
  protected readonly schema = computed(() => parse<ProfiledSchemaElement[]>(this.schemaText()));

  /** Rebuilt on each input change. The inputs are parsed again, as building an edit object can mutate the template. */
  protected readonly editor = computed<Parsed<EditObject>>(() => {
    if (this.data().error || this.template().error || this.schema().error) return {};

    try {
      const data = JSON.parse(this.dataText());
      const template: DisplayRule[] = JSON.parse(this.templateText());
      const schema: ProfiledSchemaElement[] = JSON.parse(this.schemaText());

      return {
        value: this.editObjectService.editObject(
          '',
          this.templateService.toProjected(data, template),
          template,
          this.editObjectService.createTemplateSchema(template, schema),
        ),
      };
    } catch (error) {
      return { error: (error as Error).message };
    }
  });

  /** Editor value, mapped back to the original structure. */
  protected readonly editedData = toSignal(
    toObservable(this.editor).pipe(
      switchMap(({ value: editObject }) =>
        editObject ? editObject.control.valueChanges.pipe(startWith(editObject.control.value)) : of(undefined),
      ),
      map((value) => (value === undefined ? undefined : this.templateService.toOriginal(value, this.template().value))),
    ),
  );

  reset(): void {
    this.dataText.set(stringify(sampleData));
    this.templateText.set(stringify(sampleTemplate));
    this.schemaText.set(stringify(sampleSchema));
    this.mode.set(Mode.TEMPLATE_DRIVEN);
  }
}
