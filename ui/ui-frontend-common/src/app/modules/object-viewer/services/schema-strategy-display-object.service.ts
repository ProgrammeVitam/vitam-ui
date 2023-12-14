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
import { Logger } from '../../logger/logger';
import { Collection, Schema, SchemaService } from '../../schema';
import { DisplayObject, DisplayObjectService, DisplayRule, SchemaElement } from '../models';
import { DisplayObjectHelperService } from './display-object-helper.service';
import { DisplayRuleHelperService } from './display-rule-helper.service';
import { SchemaElementToDisplayRuleService } from './schema-element-to-display-rule.service';
import { TypeService } from './type.service';

enum Mode {
  DEFAULT = 'default',
}

@Injectable()
export class SchemaStrategyDisplayObjectService implements DisplayObjectService {
  private readonly configuration = {
    displayEmptyValues: false,
  };
  private displayObject = new BehaviorSubject<DisplayObject>(null);
  private data = new BehaviorSubject<any>(null);
  private template = new BehaviorSubject<DisplayRule[]>([]);
  private mode = new BehaviorSubject<Mode>(Mode.DEFAULT);

  displayObject$: Observable<DisplayObject> = this.displayObject.asObservable();

  private readonly OriginInternal = 'internal';
  private readonly OriginExternal = 'external';
  private readonly PathOtherMetadata = 'OtherMetadata';

  constructor(
    private logger: Logger,
    private schemaService: SchemaService,
    private displayObjectHelper: DisplayObjectHelperService,
    private displayRuleHelper: DisplayRuleHelperService,
    private componentMapperService: SchemaElementToDisplayRuleService,
    private typeService: TypeService
  ) {
    combineLatest([this.schemaService.getSchema(Collection.ARCHIVE_UNIT), this.data, this.template]).subscribe(
      ([schema, data, template]) => {
        if (data === null) {
          this.displayObject.next(null);

          return;
        }

        const schemaByOrigin = this.groupSchemaByOrigin(schema);
        const internalSchema = schemaByOrigin[this.OriginInternal] || [];
        const externalSchema = schemaByOrigin[this.OriginExternal] || [];
        const defaultTemplate = this.getDefaultTemplate(data);
        const internalSchemaTemplate = this.componentMapperService.mapSchemaToDisplayRules(internalSchema);
        const externalSchemaTemplate = this.componentMapperService.mapSchemaToDisplayRules(externalSchema);
        const customTemplate = template;
        const otherMetadataTemplate = this.getOtherMetadataTemplate(externalSchema);
        const kwargs = {
          schema,
          schemaByOrigin,
          defaultTemplate,
          internalSchemaTemplate,
          externalSchemaTemplate,
          customTemplate,
          otherMetadataTemplate,
        };

        this.displayObject.next(this.computeDisplayObject(kwargs));
      },
      (error) => {
        this.logger.error(this, 'Error in observable', error);
      }
    );
  }

  private groupSchemaByOrigin(schema: SchemaElement[]): Record<string, SchemaElement[]> {
    const groupedByOrigin: Record<string, SchemaElement[]> = {};

    schema
      .filter((schemaElement) => {
        if (!schemaElement.Origin) {
          this.logger.warn(this, `Path ${schemaElement.Path} have not origin, it will be skipped...`, { schemaElement });

          return false;
        }

        return true;
      })
      .forEach((schemaElement) => {
        const originToLocaleLowerCase = schemaElement.Origin.toLocaleLowerCase();

        if (!groupedByOrigin[originToLocaleLowerCase]) {
          groupedByOrigin[originToLocaleLowerCase] = [];
        }

        groupedByOrigin[originToLocaleLowerCase].push(schemaElement);
      });

    return groupedByOrigin;
  }

  public setData(data: any): void {
    this.data.next(data);
  }

  public setTemplate(template: DisplayRule[]): void {
    if (template) {
      this.template.next(template);
    }
  }

  public setMode(mode: string): void {
    if (mode) {
      this.mode.next(mode as Mode);
    }
  }

  private computeDisplayObject({
    schema,
    defaultTemplate,
    internalSchemaTemplate,
    externalSchemaTemplate,
    customTemplate,
    otherMetadataTemplate,
  }: {
    schema: SchemaElement[];
    defaultTemplate: DisplayRule[];
    internalSchemaTemplate: DisplayRule[];
    externalSchemaTemplate: DisplayRule[];
    customTemplate: DisplayRule[];
    otherMetadataTemplate: DisplayRule[];
  }): DisplayObject {
    if (this.mode.value !== Mode.DEFAULT) {
      throw new Error(`Mode ${this.mode.value} is not supported`);
    }

    const schemaTemplate = internalSchemaTemplate.concat(externalSchemaTemplate);
    const finalSchemaTemplate = this.displayRuleHelper.mergeDisplayRulesByPath(schemaTemplate, defaultTemplate);
    const finalCustomTemplate = this.displayRuleHelper.prioritizeAndMergeDisplayRules(finalSchemaTemplate, customTemplate);
    const finalOtherMetadataTemplate = this.displayRuleHelper.prioritizeAndMergeDisplayRules(finalSchemaTemplate, otherMetadataTemplate);
    const finalNotConsumedRulesByCustomTemplate = this.displayRuleHelper.getUniqueDisplayRules(finalSchemaTemplate, finalCustomTemplate);
    const finalNotConsumedRulesByOtherMetadataTemplate = this.displayRuleHelper.getUniqueDisplayRules(
      finalSchemaTemplate,
      finalOtherMetadataTemplate
    );
    const finalNotConsumedTemplate = this.displayRuleHelper.getCommonDisplayRules(
      finalNotConsumedRulesByCustomTemplate,
      finalNotConsumedRulesByOtherMetadataTemplate
    );

    const template = finalCustomTemplate.concat(finalNotConsumedTemplate).concat(finalOtherMetadataTemplate);
    const displayObject = this.displayObjectHelper.templateDrivenDisplayObject(this.data.value, template, this.configuration);

    this.fillDisplayObjectLabelsWithSchemaShortNames(displayObject, schema);
    this.hideInconsistentDisplayObjects(displayObject);
    this.hideSchemaCategoryDisplayObjects(displayObject, schema, ['MANAGEMENT', 'OTHER']);
    this.hideLabellessDisplayObjects(displayObject);

    return displayObject;
  }

  private hideInconsistentDisplayObjects(displayObject: DisplayObject): void {
    const isConsistent = this.typeService.isConsistent(displayObject.value);

    if (isConsistent) {
      return displayObject.children.forEach((child) => this.hideInconsistentDisplayObjects(child));
    }

    displayObject.displayRule = { ...displayObject.displayRule, ui: { ...displayObject.displayRule.ui, display: false } };
  }

  private hideSchemaCategoryDisplayObjects(displayObject: DisplayObject, schema: Schema, categories: string[]): void {
    const dataPath = displayObject.displayRule.Path;
    const schemaPath = this.displayRuleHelper.convertDataPathToSchemaPath(dataPath);
    const schemaElement = schema.find((se) => se.ApiPath === schemaPath);

    if (Boolean(schemaElement) && categories.includes(schemaElement.Category)) {
      displayObject.displayRule = { ...displayObject.displayRule, ui: { ...displayObject.displayRule.ui, display: false } };
    }

    displayObject.children.forEach((child) => this.hideSchemaCategoryDisplayObjects(child, schema, categories));
  }

  private hideLabellessDisplayObjects(displayObject: DisplayObject): void {
    const hasLabel = Boolean(displayObject.displayRule.ui.label);

    if (!hasLabel) {
      displayObject.displayRule = { ...displayObject.displayRule, ui: { ...displayObject.displayRule.ui, display: false } };
    }

    displayObject.children.forEach((child) => this.hideLabellessDisplayObjects(child));
  }

  private fillDisplayObjectLabelsWithSchemaShortNames(displayObject: DisplayObject, schema: Schema) {
    const dataPath = displayObject.displayRule.Path;
    const schemaPath = this.displayRuleHelper.convertDataPathToSchemaPath(dataPath);
    const schemaElement = schema.find((se) => se.ApiPath === schemaPath);

    if (Boolean(schemaElement)) {
      displayObject.displayRule = { ...displayObject.displayRule, ui: { ...displayObject.displayRule.ui, label: schemaElement.ShortName } };
    }
    displayObject.children.forEach((child) => this.fillDisplayObjectLabelsWithSchemaShortNames(child, schema));
  }

  private getDefaultTemplate(data: any): DisplayRule[] {
    const defaultDisplayObject: DisplayObject = this.displayObjectHelper.dataDrivenDisplayObject(data, this.configuration);

    return this.displayObjectHelper.extractDisplayRules(defaultDisplayObject);
  }

  private getOtherMetadataTemplate(schema: SchemaElement[], origin = this.OriginExternal): DisplayRule[] {
    const otherMetadataDisplayRule: DisplayRule = {
      Path: null,
      ui: {
        Path: this.PathOtherMetadata,
        component: 'group',
        open: false,
        display: true,
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    };
    const externalSchemaMovedInOtherMetadataTemplate = this.componentMapperService.mapSchemaToDisplayRules(schema).map((displayRule) => ({
      ...displayRule,
      ui: {
        ...displayRule.ui,
        path: `${this.PathOtherMetadata}.${displayRule.ui.Path}`,
      },
    }));

    return [otherMetadataDisplayRule].concat(externalSchemaMovedInOtherMetadataTemplate);
  }
}
