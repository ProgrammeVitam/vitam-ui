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
import { Logger } from '../logger/logger';
import { Schema } from '../models';
import { EditObject } from '../object-editor/models/edit-object.model';
import { EditObjectService } from '../object-editor/services/edit-object.service';
import { TemplateService } from '../object-editor/services/template.service';
import { DisplayObject, DisplayRule, SchemaElement } from '../object-viewer/models';
import { DisplayRuleHelperService } from '../object-viewer/services/display-rule-helper.service';
import { TypeService } from '../object-viewer/services/type.service';
import { DisplayObjectType } from '../object-viewer/types';

export type SchemaElementByApiPath = { [key: string]: SchemaElement };

@Injectable({ providedIn: 'root' })
export class ArchiveUnitEditObjectService {
  constructor(
    private editObjectService: EditObjectService,
    private templateService: TemplateService,
    // For utils methods
    private logger: Logger,
    private displayRuleHelper: DisplayRuleHelperService,
    private typeService: TypeService,
  ) {}

  public computeEditObject(data: any, template: DisplayRule[], schema: Schema): EditObject {
    const projectedData = this.templateService.toProjected(data, template);

    return this.editObjectService.editObject('', projectedData, template, schema);
  }

  // Utils

  public setMissingDisplayRules(displayObject: DisplayObject): void {
    const { displayRule } = displayObject;

    if (!displayRule) {
      this.logger.info(this, `DisplayObject with path '${displayObject.path}' have no DisplayRule`);
      displayObject.displayRule = this.displayRuleHelper.toDisplayRule(displayObject.value);
    }

    displayObject.children.forEach((child) => this.setMissingDisplayRules(child));
  }

  public fillDisplayObjectLabelsWithSchemaShortNames(displayObject: DisplayObject, schemaByApiPath: SchemaElementByApiPath) {
    const schemaElement = this.getSchemaElement(displayObject, schemaByApiPath);

    if (Boolean(schemaElement)) {
      displayObject.displayRule = {
        ...displayObject.displayRule,
        ui: { ...displayObject.displayRule.ui, label: schemaElement.ShortName },
      };
    }

    displayObject.children.forEach((child) => this.fillDisplayObjectLabelsWithSchemaShortNames(child, schemaByApiPath));
  }

  public getSchemaElement(displayObject: DisplayObject, schemaByApiPath: SchemaElementByApiPath) {
    const dataPath = displayObject.displayRule?.Path;
    const schemaPath = this.displayRuleHelper.convertDataPathToSchemaPath(dataPath);

    return schemaByApiPath[schemaPath];
  }

  public displayAll(node: EditObject) {
    if (!node) return this.logger.log(this, 'Node is null');
    if (!node.displayRule) {
      return this.logger.log(this, `Node ${JSON.stringify({ ...node, control: null, children: null })} less displayRule`);
    }

    node.visible = true;
    node.displayRule = { ...node.displayRule, ui: { ...node.displayRule.ui, display: true } };
    node.children.forEach((child) => this.displayAll(child as EditObject));
  }

  public hideSchemaCategoryDisplayObjects(
    displayObject: DisplayObject,
    schemaByApiPath: SchemaElementByApiPath,
    categories: string[],
  ): void {
    const schemaElement = this.getSchemaElement(displayObject, schemaByApiPath);

    if (Boolean(schemaElement) && categories.includes(schemaElement.Category)) {
      displayObject.displayRule = { ...displayObject.displayRule, ui: { ...displayObject.displayRule.ui, display: false } };
    }

    displayObject.children.forEach((child) => this.hideSchemaCategoryDisplayObjects(child, schemaByApiPath, categories));
  }

  public hideLabellessDisplayObjects(displayObject: DisplayObject): void {
    const hasLabel = Boolean(displayObject.displayRule.ui.label);

    if (!hasLabel) {
      displayObject.displayRule = { ...displayObject.displayRule, ui: { ...displayObject.displayRule.ui, display: false } };
    }

    displayObject.children.forEach((child) => this.hideLabellessDisplayObjects(child));
  }

  public hideSpsFieldWithOneValue(displayObject: DisplayObject): void {
    const hasOneValue = Boolean(displayObject.displayRule.ui.Path === '#originating_agencies' && displayObject.value.length === 1);

    if (hasOneValue) {
      displayObject.displayRule = { ...displayObject.displayRule, ui: { ...displayObject.displayRule.ui, display: false } };
    }

    displayObject.children.forEach((child) => this.hideSpsFieldWithOneValue(child));
  }

  public displayExternals(displayObject: DisplayObject, schemaByApiPath: SchemaElementByApiPath) {
    if (displayObject.displayRule.Path) {
      const dataPath = displayObject.displayRule.Path;
      const schemaPath = this.displayRuleHelper.convertDataPathToSchemaPath(dataPath);
      const schemaElement = schemaByApiPath[schemaPath];

      if (Boolean(schemaElement) && schemaElement.Origin === 'EXTERNAL') {
        displayObject.displayRule = { ...displayObject.displayRule, ui: { ...displayObject.displayRule.ui, display: true } };
      }
    }

    displayObject.children.forEach((child) => this.displayExternals(child, schemaByApiPath));
  }

  public displayOtherMetadata(displayObject: DisplayObject, path = 'OtherMetadata') {
    if (!displayObject.path.includes(path)) return;

    if (!displayObject.displayRule) {
      return this.logger.warn(this, `Element '${displayObject.path}' has no displayRule`);
    }

    const isConsistent = this.typeService.isConsistent(displayObject.value);

    displayObject.displayRule = {
      ...displayObject.displayRule,
      ui: { ...displayObject.displayRule.ui, display: isConsistent },
    };

    if (isConsistent) {
      displayObject.children.forEach((child) => this.displayOtherMetadata(child));
    }
  }

  public hideInconsistentDisplayObjects(displayObject: DisplayObject): void {
    const isConsistent = this.typeService.isConsistent(displayObject.value);

    if (isConsistent) {
      return displayObject.children.forEach((child) => this.hideInconsistentDisplayObjects(child));
    }

    if (displayObject.displayRule) {
      displayObject.displayRule = { ...displayObject.displayRule, ui: { ...displayObject.displayRule.ui, display: false } };
    } else {
      this.logger.warn(this, `Element '${displayObject.path}' has no displayRule`);
    }
  }

  public setMissingTypes(editObject: EditObject): void {
    const { kind } = editObject;

    switch (kind) {
      case 'object':
        editObject.type = DisplayObjectType.GROUP;
        break;
      case 'object-array':
      case 'primitive-array':
        editObject.type = DisplayObjectType.LIST;
        break;
      case 'primitive':
      default:
        editObject.type = DisplayObjectType.PRIMITIVE;
    }

    editObject.children.forEach((child) => this.setMissingTypes(child as EditObject));
  }

  public collapseAll(editObject: EditObject): void {
    editObject?.children?.forEach((child) => this.collapseAll(child));

    editObject.open = false;
  }

  public expand(path: string, editObject: EditObject): void {
    editObject?.children?.forEach((child) => this.expand(path, child));

    if (editObject.path === path) editObject.open = true;
  }
}
