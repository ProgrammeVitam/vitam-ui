import { Injectable } from '@angular/core';
import { DisplayRule, Ui } from '../models';
import { DisplayObjectType } from '../types';
import { TypeService } from './type.service';

@Injectable()
export class DisplayRuleHelperService {
  constructor(private typeService: TypeService) {}

  private isUndefinedOrNull(path: any): boolean {
    return path === undefined || path === null;
  }

  private arePathsEqual(path1: any, path2: any): boolean {
    // converts data paths with indexes into schema paths without indexes
    const p1 = this.convertDataPathToSchemaPath(path1);
    const p2 = this.convertDataPathToSchemaPath(path2);

    return p1 === p2;
  }

  private shouldDisplay(data: any, configuration = { displayEmptyValues: true }): boolean {
    return configuration.displayEmptyValues || this.typeService.isConsistent(data);
  }

  public toDisplayRule(data: any, path: string = null, configuration = { displayEmptyValues: true }): DisplayRule {
    const type = this.typeService.dataType(data);
    const isPrimitive = type === DisplayObjectType.PRIMITIVE;

    return {
      Path: path,
      ui: {
        Path: path,
        component: isPrimitive ? 'textfield' : 'group',
        layout: {
          columns: isPrimitive ? 1 : 2,
          size: isPrimitive ? 'small' : 'medium',
        },
        open: true,
        display: this.shouldDisplay(data, configuration),
      },
    };
  }

  public getCommonDisplayRules(sourceRules: DisplayRule[], targetRules: DisplayRule[]): DisplayRule[] {
    return sourceRules.filter((sourceRule) =>
      targetRules.some((targetRule) => !this.isUndefinedOrNull(targetRule.Path) && this.arePathsEqual(targetRule.Path, sourceRule.Path)),
    );
  }

  public getUniqueDisplayRules(sourceRules: DisplayRule[], targetRules: DisplayRule[]): DisplayRule[] {
    return sourceRules.filter((sourceRule) => targetRules.every((targetRule) => !this.arePathsEqual(targetRule.Path, sourceRule.Path)));
  }

  public prioritizeAndMergeDisplayRules(sourceRuleMap: { [key: string]: DisplayRule }, targetRules: DisplayRule[]): DisplayRule[] {
    return targetRules.map((targetRule) =>
      sourceRuleMap[targetRule.Path] ? this.mergeDisplayRules(sourceRuleMap[targetRule.Path], targetRule) : targetRule,
    );
  }

  private mergeDisplayRules(sourceRule: DisplayRule, targetRule: DisplayRule): DisplayRule {
    if (sourceRule.Path !== targetRule.Path) throw new Error('Rules with different paths cannot by merged');

    const ui: Ui = { ...sourceRule.ui, ...targetRule.ui };

    return { ...sourceRule, ui };
  }

  public convertDataPathToSchemaPath(dataPath: string): string {
    return dataPath ? dataPath.replace(/\[\d+\]/g, '') : dataPath;
  }
}
