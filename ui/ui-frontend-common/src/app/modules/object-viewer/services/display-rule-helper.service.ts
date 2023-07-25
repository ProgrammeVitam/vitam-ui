import { Injectable } from '@angular/core';
import { DisplayRule, Ui } from '../models';
import { DataStructureService } from './data-structure.service';
import { TypeService } from './type.service';

@Injectable()
export class DisplayRuleHelperService {
  constructor(private typeService: TypeService, private dataStructureService: DataStructureService) {}

  private isUndefinedOrNull(path: any): boolean {
    return path === undefined || path === null;
  }

  private arePathsEqual(path1: any, path2: any): boolean {
    return path1 === path2;
  }

  private shouldDisplay(data: any, configuration = { displayEmptyValues: true }): boolean {
    return configuration.displayEmptyValues || this.typeService.isConsistent(data);
  }

  public toDisplayRule(data: any, path = null, configuration = { displayEmptyValues: true }): DisplayRule {
    const type = this.typeService.dataType(data);

    return {
      path,
      ui: {
        path,
        component: type === 'primitive' ? 'textfield' : 'group',
        layout: {
          columns: type === 'primitive' ? 1 : 2,
          size: type === 'primitive' ? 'small' : 'medium',
        },
        open: true,
        display: this.shouldDisplay(data, configuration),
      },
    };
  }

  public getCommonDisplayRules(sourceRules: DisplayRule[], targetRules: DisplayRule[]): DisplayRule[] {
    return sourceRules.filter((sourceRule) =>
      targetRules.some((targetRule) => !this.isUndefinedOrNull(targetRule.path) && this.arePathsEqual(targetRule.path, sourceRule.path))
    );
  }

  public getUniqueDisplayRules(sourceRules: DisplayRule[], targetRules: DisplayRule[]): DisplayRule[] {
    return sourceRules.filter((sourceRule) => targetRules.every((targetRule) => !this.arePathsEqual(targetRule.path, sourceRule.path)));
  }

  public mergeDisplayRulesByPath(sourceRules: DisplayRule[], targetRules: DisplayRule[]): DisplayRule[] {
    return targetRules.map((targetRule) => {
      const sourceRule = sourceRules.find((rule) => rule.path === targetRule.path);

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
      sourceUiPathMap.set(rule.ui.path, rule);
    });

    const displayRules: DisplayRule[] = [];

    // Parcourt les règles de target et traite chaque règle
    targetRules.forEach((targetRule) => {
      const sourceRule = sourceUiPathMap.get(targetRule.ui.path);

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
    if (sourceRule.ui.path !== targetRule.ui.path) {
      throw new Error("Les règles n'ont pas le même chemin et ne peuvent pas être fusionnées.");
    }

    const mergedUI: Ui = {
      ...sourceRule.ui,
      component: targetRule.ui.component,
      layout: targetRule.ui.layout,
      favoriteKeys: targetRule.ui.favoriteKeys,
      open: targetRule.ui.open,
      display: targetRule.ui.display,
    };

    const mergedRule: DisplayRule = { ...sourceRule, ui: mergedUI };

    return mergedRule;
  }
}
