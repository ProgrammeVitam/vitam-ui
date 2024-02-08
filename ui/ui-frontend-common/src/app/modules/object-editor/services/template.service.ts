import { Injectable } from '@angular/core';
import { DisplayRule } from '../../object-viewer/models';
import { DataStructureService } from '../../object-viewer/services/data-structure.service';
import { SchemaElementToDisplayRuleService } from '../../object-viewer/services/schema-element-to-display-rule.service';
import { Schema } from '../../models';

@Injectable({ providedIn: 'root' })
export class TemplateService {
  constructor(
    private dataStructureService: DataStructureService,
    private schemaElementToDisplayRuleService: SchemaElementToDisplayRuleService,
  ) {}

  public toProjected(originalData: any, template: DisplayRule[]): any {
    if (!template) return originalData;

    return this.dataStructureService.unflatten(
      template
        .map((displayRule) => ({
          [displayRule.ui.Path]: displayRule.Path ? this.dataStructureService.deepValue(originalData, displayRule.Path) : undefined,
        }))
        .filter((item) => item[Object.keys(item)[0]] !== undefined)
        .reduce((acc, cur) => this.dataStructureService.deepMerge(acc, cur), {}),
    );
  }

  public toOriginal(projectedData: any, template: DisplayRule[]): any {
    if (!template) return projectedData;

    return this.dataStructureService.unflatten(
      template
        .filter((displayRule) => displayRule.Path)
        .map((displayRule) => ({ [displayRule.Path]: this.dataStructureService.deepValue(projectedData, displayRule.ui.Path) }))
        .reduce((acc, cur) => this.dataStructureService.deepMerge(acc, cur), {}),
    );
  }

  public template(schema: Schema): DisplayRule[] {
    return this.schemaElementToDisplayRuleService.mapSchemaToDisplayRules(schema);
  }
}
