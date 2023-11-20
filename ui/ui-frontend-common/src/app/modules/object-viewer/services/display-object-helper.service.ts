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
import { Logger } from '../../logger/logger';
import { DisplayObject, DisplayRule } from '../models';
import { ComponentType, DisplayObjectType } from '../types';
import { DataStructureService } from './data-structure.service';
import { DisplayRuleHelperService } from './display-rule-helper.service';
import { TypeService } from './type.service';

@Injectable()
export class DisplayObjectHelperService {
  constructor(
    private logger: Logger,
    private typeService: TypeService,
    private dataStructureService: DataStructureService,
    private displayRuleHelperService: DisplayRuleHelperService
  ) {}

  public getComponentType(data: any, template: DisplayRule[] = [], path: string = ''): ComponentType {
    const type: DisplayObjectType = this.typeService.dataType(data);
    const componentFromTemplate = template.find((displayRule) => displayRule?.ui?.path === path)?.ui?.component;
    const componentFromType = ((displayObjectType: DisplayObjectType): ComponentType =>
      displayObjectType === DisplayObjectType.GROUP ? 'group' : 'textfield')(type);

    return componentFromTemplate || componentFromType;
  }

  public getFavoriteKeys(path: string, template: DisplayRule[] = []): string[] {
    const displayRule = template.find((rule) => rule?.ui?.path === path);

    return displayRule?.ui?.favoriteKeys || [];
  }

  public toDisplayObject(
    data: any,
    template: DisplayRule[] = [],
    path: string = '',
    configuration = { displayEmptyValues: true }
  ): DisplayObject {
    const defaultDisplayRule = this.displayRuleHelperService.toDisplayRule(data, path, configuration);
    const templateDisplayRule = template.find((rule) => rule?.ui?.path === path);
    const displayRule: DisplayRule = this.dataStructureService.deepMerge(defaultDisplayRule, templateDisplayRule);

    let children = [];

    if (data && this.typeService.isList(data)) {
      children = data.map((value: any, index: number) => this.toDisplayObject(value, template, `${path}[${index}]`, configuration));
    }

    if (data && this.typeService.isGroup(data)) {
      children = Object.entries(data).map(([key, value]) =>
        this.toDisplayObject(value, template, `${path ? path + '.' + key : key}`, configuration)
      );
    }

    return {
      children,
      component: this.getComponentType(data, template, path),
      displayRule,
      favoriteKeys: this.getFavoriteKeys(path, template),
      key: path.split('.').pop(),
      open: displayRule.ui.open,
      path,
      type: this.typeService.dataType(data),
      value: data,
    };
  }

  public dataDrivenDisplayObject(data: any, configuration = { displayEmptyValues: true }): DisplayObject {
    return this.toDisplayObject(data, [], '', configuration);
  }

  public templateDrivenDisplayObject(data: any, template: DisplayRule[], configuration = { displayEmptyValues: true }): DisplayObject {
    if (!template) {
      return null;
    }

    const mappings = template.map((displayRule) => this.mapValueToUiPath(data, displayRule));
    const consistentMappings = mappings.filter((value) => configuration.displayEmptyValues || this.typeService.hasDefined(value));
    const flatTemplatedData = consistentMappings.reduce(this.dataStructureService.deepMerge, {});
    const nestedTemplatedData = this.dataStructureService.unflatten(flatTemplatedData);
    const templatedDisplayObject = this.toDisplayObject(nestedTemplatedData, template, '', configuration);

    return templatedDisplayObject;
  }

  public mixedDrivenDisplayObject(data: any, template: DisplayRule[], configuration = { displayEmptyValues: true }): DisplayObject {
    const dataDisplayObject = this.dataDrivenDisplayObject(data, configuration);
    const templateDisplayObject = this.templateDrivenDisplayObject(data, template, configuration);
    const dataDisplayObjectValue = dataDisplayObject?.value || {};
    const templateDisplayObjectValue = templateDisplayObject?.value || {};
    const mixedValue = this.dataStructureService.deepMerge(
      this.dataStructureService.deepMerge({}, templateDisplayObjectValue),
      dataDisplayObjectValue
    );

    return this.toDisplayObject(mixedValue, template, '', configuration);
  }

  public extractDisplayRules(displayObject: DisplayObject): DisplayRule[] {
    const template: DisplayRule[] = [];
    const stack: DisplayObject[] = [displayObject];

    while (stack.length > 0) {
      const current = stack.pop();

      template.push(current.displayRule);

      if (current.children && current.children.length > 0) {
        for (let i = current.children.length - 1; i >= 0; i--) {
          stack.push(current.children[i]);
        }
      }
    }

    return template;
  }

  private mapValueToUiPath(data: any, displayRule: DisplayRule) {
    const key = displayRule.ui.path;
    const value = displayRule.path ? this.dataStructureService.deepValue(data, displayRule.path) : undefined;

    return { [key]: value };
  }
}
