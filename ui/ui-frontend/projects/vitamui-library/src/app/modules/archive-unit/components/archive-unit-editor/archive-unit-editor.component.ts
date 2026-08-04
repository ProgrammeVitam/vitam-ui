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
import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges, inject } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';

import { EditObject } from '../../../object-editor/models/edit-object.model';
import { DisplayRule } from '../../../object-viewer/models';
import { customTemplate } from '../../archive-unit-template';
import type { ArchiveUnit } from '../../models/archive-unit';
import { JsonPatchDto } from '../../models/json-patch';
import { ArchiveUnitEditorService } from './archive-unit-editor.service';

@Component({
  selector: 'vitamui-common-archive-unit-editor',
  templateUrl: './archive-unit-editor.component.html',
  styleUrls: ['./archive-unit-editor.component.scss'],
  providers: [ArchiveUnitEditorService],
  standalone: false,
})
export class ArchiveUnitEditorComponent implements OnInit, OnChanges, OnDestroy {
  private archiveUnitEditorService = inject(ArchiveUnitEditorService);

  @Input() data!: ArchiveUnit;
  @Input() template: DisplayRule[] = customTemplate;

  editObject$ = new BehaviorSubject<EditObject>(null);

  private subscriptions = new Subscription();

  ngOnInit(): void {
    this.archiveUnitEditorService.setTemplate(this.template);
    this.archiveUnitEditorService.setData(this.data);

    this.subscriptions.add(
      this.archiveUnitEditorService.editObject$.subscribe((data) => {
        // FIXME en attendant que les balises avec attribut ( 'Title_', 'Description_')  seront géré avec l’US story #12147, on les affiche pas en mode edition
        // De même, le champ 'Title' est rendu temporairement obligatoire.
        data?.children.map((node) => {
          if ('Generalities' === node.key) {
            node?.children.map((child) => {
              if (['Title'].includes(child.key)) {
                child.required = true;
              }
              if (['Title_', 'Description_'].includes(child.key))
                child.displayRule = { ...child.displayRule, ui: { ...child.displayRule.ui, display: false } };
            });
          }
        });
        this.editObject$.next(data);
      }),
    );
  }

  ngOnChanges(changes: SimpleChanges): void {
    const { data, template } = changes;

    if (data) this.archiveUnitEditorService.setData(data.currentValue);
    if (template) this.archiveUnitEditorService.setTemplate(template.currentValue);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  getJsonPatch(): JsonPatchDto {
    const jsonPatchDto = this.archiveUnitEditorService.toJsonPatchDto();
    if (jsonPatchDto.jsonPatch.length === 0) throw new Error('No changes to generate in Json Patch');
    return jsonPatchDto;
  }
}
