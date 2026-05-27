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
import { Injectable, inject } from '@angular/core';
import { Schema } from '../../models/schema/schema.interface';
import { DisplayRule } from '../../object-viewer/models/display-rule.model';
import { Template } from '../../object-viewer/models/template.model';
import { DataStructureService } from '../../object-viewer/services/data-structure.service';
import { SchemaElementToDisplayRuleService } from '../../object-viewer/services/schema-element-to-display-rule.service';

@Injectable()
export class TemplateService {
  private dataStructureService = inject(DataStructureService);
  private schemaElementToDisplayRuleService = inject(SchemaElementToDisplayRuleService);

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
