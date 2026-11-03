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
import { Component, computed, inject } from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { JsonPipe } from '@angular/common';
import { of, startWith, switchMap } from 'rxjs';
import { TranslatePipe } from '@ngx-translate/core';
import { DisplayRule, EditObject, EditObjectService, ObjectEditorModule, ProfiledSchemaElement, TemplateService } from 'vitamui-library';
import { sampleData, sampleSchema, sampleTemplate } from '../object-viewer.sample';
import { jsonInput, Parsed } from './json-input';
import { JsonInputsComponent } from './json-inputs.component';

@Component({
  selector: 'design-system-object-editor-playground',
  imports: [ObjectEditorModule, JsonPipe, TranslatePipe, JsonInputsComponent],
  template: `
    <design-system-json-inputs [inputs]="[data, template, schema]" />

    <h3 class="mt-3">{{ 'OBJECT_VIEWER.EDITOR' | translate }}</h3>
    <code>&lt;vitamui-common-object-editor [editObject]="editObject"&gt;</code>
    @if (editor().error; as error) {
      <p class="text-danger">{{ error }}</p>
    }
    @if (editor().value; as editObject) {
      <div class="mt-3">
        <vitamui-common-object-editor [editObject]="editObject"></vitamui-common-object-editor>
      </div>

      <h3>{{ 'OBJECT_VIEWER.PROJECTED_DATA' | translate }}</h3>
      <pre>{{ projectedData() | json }}</pre>

      <h3>{{ 'OBJECT_VIEWER.RECONSTITUTED_DATA' | translate }}</h3>
      <pre>{{ reconstitutedData() | json }}</pre>
    }
  `,
  styles: `
    pre {
      background: var(--vitamui-grey-100);
      padding: 1rem;
    }
  `,
})
export class ObjectEditorPlaygroundComponent {
  private readonly editObjectService = inject(EditObjectService);
  private readonly templateService = inject(TemplateService);

  protected readonly data = jsonInput<unknown>('OBJECT_VIEWER.DATA', sampleData);
  protected readonly template = jsonInput<DisplayRule[]>('OBJECT_VIEWER.TEMPLATE', sampleTemplate);
  protected readonly schema = jsonInput<ProfiledSchemaElement[]>('OBJECT_VIEWER.SCHEMA', sampleSchema);

  /** Rebuilt on each input change. The inputs are parsed again, as building an edit object can mutate the template. */
  protected readonly editor = computed<Parsed<EditObject>>(() => {
    if (this.data.parsed().error || this.template.parsed().error || this.schema.parsed().error) return {};

    try {
      const data = JSON.parse(this.data.text());
      const template: DisplayRule[] = JSON.parse(this.template.text());
      const schema: ProfiledSchemaElement[] = JSON.parse(this.schema.text());

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

  /** Form value, in the projected structure defined by the template. */
  protected readonly projectedData = toSignal(
    toObservable(this.editor).pipe(
      switchMap(({ value: editObject }) =>
        editObject ? editObject.control.valueChanges.pipe(startWith(editObject.control.value)) : of(undefined),
      ),
    ),
  );

  /**
   * Form value, mapped back to the original structure: this is what would be sent on save.
   * Merged into the original data to keep the fields not mapped by the template.
   */
  protected readonly reconstitutedData = computed(() => {
    const projectedData = this.projectedData();
    if (projectedData === undefined) return undefined;

    return { ...(this.data.parsed().value as object), ...this.templateService.toOriginal(projectedData, this.template.parsed().value) };
  });
}
