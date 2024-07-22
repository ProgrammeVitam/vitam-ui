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
import { Schema, SchemaElement } from '../models';
import { DisplayObject, DisplayRule } from '../object-viewer/models';
import { DisplayObjectHelperService } from '../object-viewer/services/display-object-helper.service';
import { DisplayRuleHelperService } from '../object-viewer/services/display-rule-helper.service';
import { SchemaElementToDisplayRuleService } from '../object-viewer/services/schema-element-to-display-rule.service';

@Injectable({ providedIn: 'root' })
export class ArchiveUnitTemplateService {
  constructor(
    private componentMapperService: SchemaElementToDisplayRuleService,
    private displayRuleHelper: DisplayRuleHelperService,
    private displayObjectHelper: DisplayObjectHelperService,
    private logger: Logger,
  ) {}

  public computeTemplate(_originalData: any, customTemplate: DisplayRule[], originalSchema: Schema): DisplayRule[] {
    const schemaByOrigin = this.groupSchemaByOrigin(originalSchema);
    const internalSchema = schemaByOrigin.INTERNAL || [];
    const externalSchema = schemaByOrigin.EXTERNAL || [];
    // const defaultTemplate = this.getDefaultTemplate(originalData);
    const internalSchemaTemplate = this.componentMapperService.mapSchemaToDisplayRules(internalSchema);
    const externalSchemaTemplate = this.componentMapperService.mapSchemaToDisplayRules(externalSchema);
    const otherMetadataTemplate = this.computeOtherMetadataTemplate(externalSchema);
    // Attention, le template de schéma se base sur un mapping interne vers externe pour les chemins, or la donnée reçue côté frontend est déjà au format externe.
    const backToFrontSchemaTemplate = internalSchemaTemplate.concat(externalSchemaTemplate);
    const frontToFrontSchemaTemplate = backToFrontSchemaTemplate.map((rule) => ({ ...rule, Path: rule.ui.Path }));
    // const finalSchemaTemplate = this.displayRuleHelper.mergeDisplayRulesByPath(defaultTemplate, frontToFrontSchemaTemplate);

    const finalSchemaTemplate = frontToFrontSchemaTemplate;
    const finalSchemaTemplateMap = finalSchemaTemplate.reduce(
      (acc, rule) => {
        acc[rule.Path] = rule;

        return acc;
      },
      {} as { [key: string]: DisplayRule },
    );
    const finalCustomTemplate = this.displayRuleHelper.prioritizeAndMergeDisplayRules(finalSchemaTemplateMap, customTemplate);
    const finalOtherMetadataTemplate = this.displayRuleHelper.prioritizeAndMergeDisplayRules(finalSchemaTemplateMap, otherMetadataTemplate);
    const finalNotConsumedRulesByCustomTemplate = this.displayRuleHelper.getUniqueDisplayRules(finalSchemaTemplate, finalCustomTemplate);
    const finalNotConsumedRulesByOtherMetadataTemplate = this.displayRuleHelper.getUniqueDisplayRules(
      finalSchemaTemplate,
      finalOtherMetadataTemplate,
    );
    const finalNotConsumedTemplate = this.displayRuleHelper.getCommonDisplayRules(
      finalNotConsumedRulesByCustomTemplate,
      finalNotConsumedRulesByOtherMetadataTemplate,
    );

    return finalCustomTemplate.concat(finalNotConsumedTemplate).concat(finalOtherMetadataTemplate);
  }

  private computeOtherMetadataTemplate(schema: SchemaElement[], options = { path: 'OtherMetadata' }): DisplayRule[] {
    const { path } = options;
    const otherMetadataDisplayRule: DisplayRule = {
      Path: null,
      ui: {
        Path: path,
        component: 'group',
        open: false,
        display: true,
        layout: {
          columns: 2,
          size: 'medium',
        },
        label: 'DISPLAY_RULE.OTHER_METADATA.LABEL',
      },
    };
    const externalSchemaMovedInOtherMetadataTemplate = this.componentMapperService
      .mapSchemaToDisplayRules(schema.filter((element) => element.Origin === 'EXTERNAL'))
      .map((displayRule) => ({ ...displayRule, ui: { ...displayRule.ui, Path: `${path}.${displayRule.ui.Path}` } }));

    return [otherMetadataDisplayRule].concat(externalSchemaMovedInOtherMetadataTemplate);
  }

  private groupSchemaByOrigin(schema: SchemaElement[]): Record<SchemaElement['Origin'], SchemaElement[]> {
    return schema
      .filter((schemaElement) => {
        if (schemaElement.Origin) return true;

        this.logger.warn(this, `Path ${schemaElement.Path} have not origin, it will be skipped...`, { schemaElement });

        return false;
      })
      .reduce(
        (acc, cur) => {
          acc[cur.Origin].push(cur);

          return acc;
        },
        { INTERNAL: [], EXTERNAL: [], VIRTUAL: [] },
      );
  }

  // @ts-ignore
  private getDefaultTemplate(data: any): DisplayRule[] {
    const defaultDisplayObject: DisplayObject = this.displayObjectHelper.dataDrivenDisplayObject(data, { displayEmptyValues: false });

    return this.displayObjectHelper.extractDisplayRules(defaultDisplayObject);
  }
}
