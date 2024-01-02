import { Injectable } from '@angular/core';
import { DisplayRule, Ui } from '../models';
import { DisplayObjectType } from '../types';
import { DataStructureService } from './data-structure.service';
import { TypeService } from './type.service';

@Injectable()
export class DisplayRuleHelperService {
  constructor(private typeService: TypeService, private dataStructureService: DataStructureService) {}

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

  public toDisplayRule(data: any, path = null, configuration = { displayEmptyValues: true }): DisplayRule {
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
      targetRules.some((targetRule) => !this.isUndefinedOrNull(targetRule.Path) && this.arePathsEqual(targetRule.Path, sourceRule.Path))
    );
  }

  public getUniqueDisplayRules(sourceRules: DisplayRule[], targetRules: DisplayRule[]): DisplayRule[] {
    return sourceRules.filter((sourceRule) => targetRules.every((targetRule) => !this.arePathsEqual(targetRule.Path, sourceRule.Path)));
  }

  public mergeDisplayRulesByPath(sourceRules: DisplayRule[], targetRules: DisplayRule[]): DisplayRule[] {
    return targetRules.map((targetRule) => {
      const sourceRule = sourceRules.find((rule) => rule.Path === targetRule.Path);

      if (sourceRule) {
        this.dataStructureService.deepMerge(sourceRule, targetRule);
      }

      return targetRule;
    });
  }

  public prioritizeAndMergeDisplayRules(sourceRules: DisplayRule[], targetRules: DisplayRule[]): DisplayRule[] {
    const sourceUiPathMap = new Map<string, DisplayRule>();

    // Indexe les règles d'affichage de source par chemin
    sourceRules.forEach((rule) => {
      sourceUiPathMap.set(rule.ui.Path, rule);
    });

    const displayRules: DisplayRule[] = [];

    // Parcourt les règles de target et traite chaque règle
    targetRules.forEach((targetRule) => {
      const sourceRule = sourceUiPathMap.get(targetRule.ui.Path);

      // Si une règle correspondante existe dans source, fusionne les valeurs
      if (sourceRule) {
        // Fusionne les valeurs de sourceRule et targetRule (utilisez votre propre logique de fusion)
        const mergedRule = this.mergeDisplayRules(sourceRule, targetRule);
        displayRules.push(mergedRule);
      } else {
        // Si aucune règle correspondante n'existe dans source, ajoute simplement la règle de target
        displayRules.push(targetRule);
      }
    });

    return displayRules;
  }

  private mergeDisplayRules(sourceRule: DisplayRule, targetRule: DisplayRule): DisplayRule {
    if (sourceRule.ui.Path !== targetRule.ui.Path) {
      throw new Error("Les règles n'ont pas le même chemin et ne peuvent pas être fusionnées.");
    }

    const mergedUI: Ui = {
      ...sourceRule.ui,
      component: targetRule.ui.component,
      layout: targetRule.ui.layout,
      favoriteKeys: targetRule.ui.favoriteKeys,
      open: targetRule.ui.open,
      display: targetRule.ui.display,
      label: targetRule.ui.label,
    };

    const mergedRule: DisplayRule = { ...sourceRule, ui: mergedUI };

    return mergedRule;
  }

  public convertDataPathToSchemaPath(dataPath: string): string {
    return dataPath ? dataPath.replace(/\[\d+\]/g, '') : dataPath;
  }
}
