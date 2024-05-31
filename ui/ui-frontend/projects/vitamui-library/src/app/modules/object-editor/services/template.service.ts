import { Injectable } from '@angular/core';
import { Schema } from '../../models';
import { DisplayRule } from '../../object-viewer/models';
import { Template } from '../../object-viewer/models/template.model';
import { DataStructureService } from '../../object-viewer/services/data-structure.service';
import { SchemaElementToDisplayRuleService } from '../../object-viewer/services/schema-element-to-display-rule.service';

@Injectable({ providedIn: 'root' })
export class TemplateService {
  constructor(
    private dataStructureService: DataStructureService,
    private schemaElementToDisplayRuleService: SchemaElementToDisplayRuleService,
  ) {}

  public toProjected(originalData: any, template: DisplayRule[]): any {
    if (!template) return originalData;
    this.detectCircularReferences(template);

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
    this.detectCircularReferences(template);

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

  public detectCircularReferences(template: Template): void {
    const paths = template.filter((rule) => rule.Path).map((rule) => rule.Path);
    const pathSet = new Set(paths);
    if (paths.length === pathSet.size) return;

    const dupePaths = paths.slice();
    pathSet.forEach((path) => {
      const index = dupePaths.indexOf(path);

      if (index > -1) dupePaths.splice(index, 1);
    });

    const report: { path: string; targets: Set<string>; message: string }[] = [];
    dupePaths.forEach((path) => {
      const dupeRules = template.filter((rule) => rule.Path === path);
      const targetPaths = dupeRules.map((rule) => rule.ui.Path);
      const targetPathSet = new Set(targetPaths);
      for (let targetPath1 of targetPathSet) {
        for (let targetPath2 of targetPathSet) {
          if (targetPath1.includes(targetPath2) && targetPath1 !== targetPath2) {
            report.push({
              path,
              targets: targetPathSet,
              message: `Rule '${path}' contains circular references [${Array.from(targetPathSet).map((path) => `'${path}'`)}]`,
            });
          }
        }
      }
    });

    if (report.length) throw new Error(report.map((item) => item.message).join('\n'));
  }
}
