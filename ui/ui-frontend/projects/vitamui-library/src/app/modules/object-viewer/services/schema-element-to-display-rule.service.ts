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
import { DisplayRule } from '../models/display-rule.model';
import { ProfiledSchemaElement, SchemaElement } from '../../models/schema/schema-element.model';
import { LayoutSize } from '../types';
import { DatePatternConstants } from '../../dates.constants';

type ComponentName =
  | 'balise-n1'
  | 'balise-n2'
  | 'balise-n3'
  | 'balise-n4'
  | 'textfield-short-mono'
  | 'textfield-short-multi'
  | 'textfield-medium-mono'
  | 'textfield-medium-multi'
  | 'textfield-large-mono'
  | 'textfield-large-multi'
  | 'attribut-short-mono'
  | 'attribut-short-multi'
  | 'attribut-mono'
  | 'attribut-multi'
  | 'select-mono'
  | 'select-multi'
  | 'datepicker-date'
  | 'datepicker-datetime';

@Injectable()
export class SchemaElementToDisplayRuleService {
  private schemaElementComponentTypeToDisplayRule: Record<ComponentName, DisplayRule> = {
    'attribut-mono': {
      Path: null,
      ui: {
        Path: null,
        component: 'select+textfield',
      },
    },
    'attribut-multi': {
      Path: null,
      ui: {
        Path: null,
        component: 'select+textarea',
      },
    },
    'attribut-short-mono': {
      Path: null,
      ui: {
        Path: null,
        component: 'select+textfield',
      },
    },
    'attribut-short-multi': {
      Path: null,
      ui: {
        Path: null,
        component: 'select+textarea',
      },
    },
    'balise-n1': {
      Path: null,
      ui: {
        Path: null,
        component: 'group',
      },
    },
    'balise-n2': {
      Path: null,
      ui: {
        Path: null,
        component: 'group',
      },
    },
    'balise-n3': {
      Path: null,
      ui: {
        Path: null,
        component: 'group',
      },
    },
    'balise-n4': {
      Path: null,
      ui: {
        Path: null,
        component: 'group',
      },
    },
    'datepicker-date': {
      Path: null,
      ui: {
        Path: null,
        component: 'datepicker',
      },
    },
    'datepicker-datetime': {
      Path: null,
      ui: {
        Path: null,
        component: 'datetime',
      },
    },
    'select-mono': {
      Path: null,
      ui: {
        Path: null,
        component: 'select',
      },
    },
    'select-multi': {
      Path: null,
      ui: {
        Path: null,
        component: 'select',
      },
    },
    'textfield-large-mono': {
      Path: null,
      ui: {
        Path: null,
        component: 'textarea',
      },
    },
    'textfield-large-multi': {
      Path: null,
      ui: {
        Path: null,
        component: 'textarea',
      },
    },
    'textfield-medium-mono': {
      Path: null,
      ui: {
        Path: null,
        component: 'textfield',
      },
    },
    'textfield-medium-multi': {
      Path: null,
      ui: {
        Path: null,
        component: 'textarea',
      },
    },
    'textfield-short-mono': {
      Path: null,
      ui: {
        Path: null,
        component: 'textfield',
      },
    },
    'textfield-short-multi': {
      Path: null,
      ui: {
        Path: null,
        component: 'textarea',
      },
    },
  };

  public mapSchemaElementToComponent(schemaElement: SchemaElement): ComponentName {
    const defaultComponent: ComponentName = 'textfield-short-mono';

    if (!schemaElement) {
      return defaultComponent;
    }

    switch (schemaElement.DataType) {
      case 'OBJECT':
        const computedDepth = schemaElement.Path.split('.').length - 1;

        if (computedDepth === 0) {
          return 'balise-n1';
        }
        if (computedDepth === 1) {
          return 'balise-n2';
        }
        if (computedDepth === 2) {
          return 'balise-n3';
        }
        if (computedDepth === 3) {
          return 'balise-n4';
        }

        return defaultComponent;
      case 'STRING':
        const isUnique = schemaElement.Cardinality.includes('ONE');
        const isMultiple = !isUnique;
        const isSpecial = schemaElement.Path.includes('_.');
        const isSelect = (schemaElement as ProfiledSchemaElement).Control?.Type === 'SELECT' || false;

        const control = (schemaElement as ProfiledSchemaElement).Control;

        if (control) {
          if (
            control.Type === 'REGEX' &&
            [
              DatePatternConstants.YEAR.toString(),
              DatePatternConstants.YEAR_MONTH.toString(),
              DatePatternConstants.YEAR_MONTH_DAY.toString(),
              DatePatternConstants.FULL_DATE.toString(),
            ].includes(control.Value)
          ) {
            return 'datepicker-date';
          } else {
            return defaultComponent;
          }
        }

        if (isSelect && isUnique) return 'select-mono';
        if (isSelect && isMultiple) return 'select-multi';

        if (isSpecial) {
          if (isUnique && schemaElement.StringSize === 'SHORT') {
            return 'attribut-short-mono';
          }
          if (isMultiple && schemaElement.StringSize === 'SHORT') {
            return 'attribut-short-multi';
          }
          if (isUnique) {
            return 'attribut-mono';
          }
          if (isMultiple) {
            return 'attribut-multi';
          }

          return defaultComponent;
        }

        if (isUnique && schemaElement.StringSize === 'SHORT') {
          return 'textfield-short-mono';
        }
        if (isUnique && schemaElement.StringSize === 'MEDIUM') {
          return 'textfield-medium-mono';
        }
        if (isUnique && schemaElement.StringSize === 'LARGE') {
          return 'textfield-large-mono';
        }
        if (isMultiple && schemaElement.StringSize === 'SHORT') {
          return 'textfield-short-multi';
        }
        if (isMultiple && schemaElement.StringSize === 'MEDIUM') {
          return 'textfield-medium-multi';
        }
        if (isMultiple && schemaElement.StringSize === 'LARGE') {
          return 'textfield-large-multi';
        }

        return defaultComponent;
      case 'DATETIME':
        // if (schemaElement.DataType === 'DATETIME') {
        //   return 'datepicker-date';
        // }

        return 'datepicker-datetime';
      case 'LONG':
      case 'DOUBLE':
      case 'BOOLEAN':
      default:
        return defaultComponent;
    }
  }

  private getLayoutSize(schemaElement: SchemaElement): LayoutSize {
    if (schemaElement.DataType === 'DATETIME') {
      return 'small';
    }
    return { SHORT: 'small', MEDIUM: 'medium', LARGE: 'large' }[schemaElement.StringSize || 'MEDIUM'] as LayoutSize;
  }

  public mapSchemaElementToDisplayRule(schemaElement: SchemaElement): DisplayRule {
    const component: ComponentName = this.mapSchemaElementToComponent(schemaElement);
    const baseDisplayRule = this.schemaElementComponentTypeToDisplayRule[component];
    const layoutSize = this.getLayoutSize(schemaElement);
    return {
      ...baseDisplayRule,
      Path: schemaElement.Path,
      ui: {
        ...baseDisplayRule.ui,
        layout: {
          size: layoutSize,
          columns: layoutSize === 'small' ? 1 : 2,
        },
        Path: schemaElement.ApiPath,
        label: schemaElement.ShortName,
        display: schemaElement.Category === 'DESCRIPTION' || schemaElement.Origin === 'EXTERNAL',
      },
    };
  }

  public mapSchemaToDisplayRules(schema: SchemaElement[]): DisplayRule[] {
    return schema.map((schemaElement) => this.mapSchemaElementToDisplayRule(schemaElement));
  }

  public getSchemaElementFrontendModelPath(schemaElement: SchemaElement): string {
    const fragments = schemaElement.Path.split('.');

    if (schemaElement.ApiField) {
      fragments.pop();
      fragments.push(schemaElement.ApiField);
    }

    return fragments.join('.');
  }
}
