import { Injectable } from '@angular/core';
import { DisplayRule } from '../models';
import { LayoutSize } from '../types';
import { SchemaElement } from '../../models';

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

        // Rules for enums not represented in schemas
        // if (schemaElement.Type === 'TEXT') {
        //   const isUnique = schemaElement.Cardinality.includes('ONE');
        //   const isMultiple = !isUnique;

        //   if (isUnique) {
        //     return 'select-mono';
        //   }
        //   if (isMultiple) {
        //     return 'select-multi';
        //   }

        //   return defaultComponent;
        // }

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
