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
