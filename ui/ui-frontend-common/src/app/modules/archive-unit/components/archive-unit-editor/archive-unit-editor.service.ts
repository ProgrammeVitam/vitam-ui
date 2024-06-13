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
import { FormGroup } from '@angular/forms';
import { BehaviorSubject, combineLatest } from 'rxjs';
import { Logger } from '../../../logger/logger';
import { Collection, Schema } from '../../../models';
import { EditObject } from '../../../object-editor/models/edit-object.model';
import { EditObjectService } from '../../../object-editor/services/edit-object.service';
import { SchemaService as SchemaUtils } from '../../../object-editor/services/schema.service';
import { TemplateService } from '../../../object-editor/services/template.service';
import { DisplayRule, SchemaElement } from '../../../object-viewer/models';
import { internationalizedKeys } from '../../../object-viewer/services/display-object-helper.service';
import { SedaVersion } from '../../../object-viewer/types';
import { SchemaService } from '../../../schema';
import { diff } from '../../../utils';
import { ArchiveUnitEditObjectService } from '../../archive-unit-edit-object.service';
import { ArchiveUnitTemplateService } from '../../archive-unit-template.service';
import { ArchiveUnit } from '../../models/archive-unit';
import { JsonPatch, JsonPatchDto } from '../../models/json-patch';

@Injectable()
export class ArchiveUnitEditorService {
  private collection = new BehaviorSubject<Collection>(Collection.ARCHIVE_UNIT);
  private sedaVersions = new BehaviorSubject<SedaVersion[]>(['INTERNE', '2.3']);
  private category = new BehaviorSubject<SchemaElement['Category']>('DESCRIPTION');
  private data = new BehaviorSubject<any>(null);
  private editObject = new BehaviorSubject<EditObject>(null);
  private customTemplate = new BehaviorSubject<DisplayRule[]>([]);

  private template = new BehaviorSubject<DisplayRule[]>([]);
  private schema = new BehaviorSubject<Schema>([]);

  editObject$ = this.editObject.asObservable();

  constructor(
    private logger: Logger,
    private schemaService: SchemaService,
    private templateService: TemplateService,
    private schemaUtils: SchemaUtils,
    private archiveUnitTemplateService: ArchiveUnitTemplateService,
    private archiveUnitEditObject: ArchiveUnitEditObjectService,
    private editObjectService: EditObjectService,
  ) {
    combineLatest([this.data, this.customTemplate, this.schemaService.getSchema(this.collection.value)]).subscribe(
      ([data, customTemplate, schema]) => {
        if (data === null) return this.editObject.next(null);

        this.schemaUtils.validate(schema, { passive: true });

        const paths = schema.map((element) => element.Path);
        const schemaErrors = schema.map((element) => this.schemaUtils.collectSchemaElementErrors(element, schema, paths));
        const subschema: Schema = schema
          .filter(
            (element) =>
              element.Category === this.category.value || element.Origin === 'EXTERNAL' || internationalizedKeys.includes(element.Path),
          ) // External elements are categorized as OTHER by default by schema API
          .filter((element) => {
            if (element?.SedaVersions?.length) return element.SedaVersions.some((version) => this.sedaVersions.value.includes(version));

            // No seda version for external elements, we should allow these elements
            return true;
          })
          .filter(
            (element) => !schemaErrors.some((schemaError) => schemaError.element.Path === element.Path && schemaError.messages.length > 0),
          );

        const editObject: EditObject = this.computeEditObject(data, customTemplate, subschema);

        this.editObject.next(editObject);
      },
      (error) => {
        this.logger.error(this, 'Error in observable', error);
      },
    );
  }

  public setData(data: any): void {
    this.data.next(data);
  }

  public setTemplate(template: DisplayRule[]): void {
    if (template) this.customTemplate.next(template);
  }

  private computeEditObject(data: any, customTemplate: DisplayRule[], schema: Schema): EditObject {
    this.template.next(this.archiveUnitTemplateService.computeTemplate(data, customTemplate, schema));
    this.schema.next(this.editObjectService.createTemplateSchema(this.template.value, schema));

    const editObject = this.archiveUnitEditObject.computeEditObject(data, this.template.value, this.schema.value);

    this.archiveUnitEditObject.setMissingTypes(editObject); // TODO: Idealement utiliser soit type ou kind pour les ngIf des composants
    this.archiveUnitEditObject.setMissingDisplayRules(editObject);
    this.archiveUnitEditObject.displayAll(editObject);
    this.archiveUnitEditObject.collapseAll(editObject);
    this.archiveUnitEditObject.expand('', editObject);
    this.archiveUnitEditObject.expand('Generalities', editObject);
    this.archiveUnitEditObject.hideSpsFieldWithOneValue(editObject);

    return editObject;
  }

  private setLabels(editObject: EditObject, schema: Schema): void {
    const { displayRule } = editObject;
    const path = displayRule?.ui?.Path;

    if (displayRule?.ui?.Path) {
      const element = schema.find((e) => e.ApiPath === path);

      if (element) editObject.displayRule.ui.label = element.ShortName;
    }

    editObject.children.forEach((c) => this.setLabels(c, schema));
  }

  public getValue(): ArchiveUnit {
    const projectedValue = (this.editObject.value.control as FormGroup).getRawValue();
    const updatedValue = this.templateService.toOriginal(projectedValue, this.template.value);

    return updatedValue;
  }

  public getOriginalValue(): ArchiveUnit {
    const projectedValue = this.templateService.toProjected(this.data.value, this.template.value);
    const originalValue = this.templateService.toOriginal(projectedValue, this.template.value);

    return originalValue;
  }

  public toJsonPatch(): JsonPatch {
    const originalValue = this.getOriginalValue();
    const updatedValue = this.getValue();
    const criterias = [undefined, null, [], {}, ''];
    const consistentOriginalValue = this.filterByCriteria(originalValue, criterias);
    const consistentUpdatedValue = this.filterByCriteria(updatedValue, criterias);

    const changes = diff(consistentUpdatedValue, consistentOriginalValue);
    const replaceEntries = Object.entries(changes);
    const addEntries = Object.entries(consistentUpdatedValue).filter(([key]) => !Object.keys(consistentOriginalValue).includes(key));
    const removeEntries = Object.entries(consistentOriginalValue).filter(([key]) => !Object.keys(consistentUpdatedValue).includes(key));
    const jsonPatch: JsonPatch = [];

    removeEntries.forEach(([key, value]) => {
      jsonPatch.push({ op: 'remove', path: key, value });
    });
    addEntries.forEach(([key, value]) => {
      jsonPatch.push({ op: 'add', path: key, value });
    });
    replaceEntries.forEach(([key]) => {
      jsonPatch.push({ op: 'replace', path: key, value: consistentUpdatedValue[key] });
    });

    return jsonPatch;
  }

  public toJsonPatchDto(): JsonPatchDto {
    return {
      id: this.data.value['#id'],
      jsonPatch: this.toJsonPatch(),
    };
  }

  private filterByCriteria(input: any, criterias: any[]): any {
    if (Array.isArray(input)) {
      // Si c'est un tableau
      return input
        .map((item) => this.filterByCriteria(item, criterias)) // Appel récursif pour chaque élément
        .filter((item) => !criterias.some((criteria) => JSON.stringify(criteria) === JSON.stringify(item))); // Filtrer les éléments selon les critères fournis
    } else if (typeof input === 'object' && input !== null) {
      // Si c'est un objet
      const filteredObj = {};
      Object.entries(input).forEach(([key, value]) => {
        const filteredValue = this.filterByCriteria(value, criterias);
        if (!criterias.some((criteria) => JSON.stringify(criteria) === JSON.stringify(filteredValue))) {
          filteredObj[key] = filteredValue;
        }
      });
      return filteredObj;
    }
    // Pour tout autre type de valeur, renvoyer directement la valeur
    return input;
  }
}
