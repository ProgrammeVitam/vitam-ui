/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import { Injectable } from '@angular/core';
import { BehaviorSubject, combineLatest, Observable } from 'rxjs';
import { Logger } from '../../../logger/logger';
import { Collection, Schema } from '../../../models';
import { EditObjectService } from '../../../object-editor/services/edit-object.service';
import { DisplayObject, DisplayObjectService, DisplayRule } from '../../../object-viewer/models';
import { SchemaService } from '../../../schema';
import { ArchiveUnitEditObjectService, SchemaElementByApiPath } from '../../archive-unit-edit-object.service';
import { ArchiveUnitTemplateService } from '../../archive-unit-template.service';

enum Mode {
  DEFAULT = 'default',
}

@Injectable()
export class ArchiveUnitViewerService implements DisplayObjectService {
  private displayObject = new BehaviorSubject<DisplayObject>(null);
  private data = new BehaviorSubject<any>(null);
  private customTemplate = new BehaviorSubject<DisplayRule[]>([]);
  private mode = new BehaviorSubject<Mode>(Mode.DEFAULT);

  private template = new BehaviorSubject<DisplayRule[]>([]);
  private schema = new BehaviorSubject<Schema>([]);

  displayObject$: Observable<DisplayObject> = this.displayObject.asObservable();

  constructor(
    private logger: Logger,
    private schemaService: SchemaService,
    private archiveUnitTemplateService: ArchiveUnitTemplateService,
    private archiveUnitEditObjectService: ArchiveUnitEditObjectService,
    private editObjectService: EditObjectService,
  ) {
    combineLatest([this.schemaService.getSchema(Collection.ARCHIVE_UNIT), this.data, this.customTemplate]).subscribe(
      ([schema, data, template]) => {
        if (data === null) return this.displayObject.next(null);
        if (this.mode.value !== Mode.DEFAULT) throw new Error(`Mode ${this.mode.value} is not supported`);

        this.displayObject.next(this.computeDisplayObject(data, template, schema));
      },
      (error) => {
        this.logger.error(this, 'Error in observable', error);
      },
    );
  }

  private computeDisplayObject(data: any, customTemplate: DisplayRule[], schema: Schema): DisplayObject {
    this.template.next(this.archiveUnitTemplateService.computeTemplate(data, customTemplate, schema));
    this.schema.next(this.editObjectService.createTemplateSchema(this.template.value, schema));

    const displayObject = this.archiveUnitEditObjectService.computeEditObject(data, this.template.value, this.schema.value);
    const schemaByApiPath = schema.reduce((acc, s) => {
      acc[s.ApiPath] = s;
      return acc;
    }, {} as SchemaElementByApiPath);

    this.archiveUnitEditObjectService.setMissingDisplayRules(displayObject);
    this.archiveUnitEditObjectService.fillDisplayObjectLabelsWithSchemaShortNames(displayObject, schemaByApiPath);
    this.archiveUnitEditObjectService.displayAll(displayObject);
    this.archiveUnitEditObjectService.hideSchemaCategoryDisplayObjects(displayObject, schemaByApiPath, ['MANAGEMENT', 'OTHER']);
    this.archiveUnitEditObjectService.hideLabellessDisplayObjects(displayObject);
    this.archiveUnitEditObjectService.displayExternals(displayObject, schemaByApiPath);
    this.archiveUnitEditObjectService.hideInconsistentDisplayObjects(displayObject);
    // Attention display Other MetaData doit être après hideInconsistentDisplayObjects
    this.archiveUnitEditObjectService.displayOtherMetadata(displayObject);
    this.archiveUnitEditObjectService.hideSpsFieldWithOneValue(displayObject);

    return displayObject;
  }

  public setData(data: any): void {
    this.data.next(data);
  }

  public setTemplate(template: DisplayRule[]): void {
    if (template) this.customTemplate.next(template);
  }

  public setMode(mode: string): void {
    if (mode) this.mode.next(mode as Mode);
  }
}
