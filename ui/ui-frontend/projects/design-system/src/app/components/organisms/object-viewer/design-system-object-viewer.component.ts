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
import { toSignal } from '@angular/core/rxjs-interop';
import { JsonPipe } from '@angular/common';
import { MatRadioButton, MatRadioGroup } from '@angular/material/radio';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { TranslatePipe } from '@ngx-translate/core';
import { EditObject, EditObjectService, Mode, ObjectEditorModule, ObjectViewerModule, TemplateService } from 'vitamui-library';
import { sampleData, sampleSchema, sampleTemplate } from './object-viewer.sample';

@Component({
  imports: [ObjectViewerModule, ObjectEditorModule, JsonPipe, TranslatePipe, MatRadioGroup, MatRadioButton, MatTabGroup, MatTab],
  templateUrl: './design-system-object-viewer.component.html',
  styles: `
    pre {
      background: var(--vitamui-grey-100);
      padding: 1rem;
    }
  `,
})
export class DesignSystemObjectViewerComponent {
  private readonly editObjectService = inject(EditObjectService);
  private readonly templateService = inject(TemplateService);

  protected readonly originalData = sampleData;
  protected readonly template = sampleTemplate;
  protected readonly schema = sampleSchema;

  protected readonly modes = Object.values(Mode);
  protected readonly mode = signal<Mode>(Mode.TEMPLATE_DRIVEN);

  protected readonly editObject: EditObject = this.editObjectService.editObject(
    '',
    this.templateService.toProjected(sampleData, sampleTemplate),
    sampleTemplate,
    this.editObjectService.createTemplateSchema(sampleTemplate, sampleSchema),
  );

  /** Form value, in the projected structure defined by the template. */
  protected readonly projectedData = toSignal(this.editObject.control.valueChanges, { initialValue: this.editObject.control.value });

  /**
   * Edited data, mapped back to the original structure, so that the viewer reflects the editor changes.
   * Merged into the original data to keep the fields not mapped by the template.
   */
  protected readonly data = computed(() => ({ ...sampleData, ...this.templateService.toOriginal(this.projectedData(), sampleTemplate) }));
}
