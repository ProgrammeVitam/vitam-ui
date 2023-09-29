import { Injectable } from '@angular/core';
import { DisplayRule, ExtendedOntology } from '../models';

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
export class OntologyToDisplayRuleMapper {
  private ontologyComponentTypeToDisplayRule: Record<ComponentName, DisplayRule> = {
    'attribut-mono': {
      path: null,
      ui: {
        path: null,
        component: 'select+textfield',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    'attribut-multi': {
      path: null,
      ui: {
        path: null,
        component: 'select+textarea',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    'attribut-short-mono': {
      path: null,
      ui: {
        path: null,
        component: 'select+textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    'attribut-short-multi': {
      path: null,
      ui: {
        path: null,
        component: 'select+textarea',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    'balise-n1': {
      path: null,
      ui: {
        path: null,
        component: 'group',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    'balise-n2': {
      path: null,
      ui: {
        path: null,
        component: 'group',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    'balise-n3': {
      path: null,
      ui: {
        path: null,
        component: 'group',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    'balise-n4': {
      path: null,
      ui: {
        path: null,
        component: 'group',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    'datepicker-date': {
      path: null,
      ui: {
        path: null,
        component: 'datepicker',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    'datepicker-datetime': {
      path: null,
      ui: {
        path: null,
        component: 'datetime',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    'select-mono': {
      path: null,
      ui: {
        path: null,
        component: 'select',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    'select-multi': {
      path: null,
      ui: {
        path: null,
        component: 'select',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    'textfield-large-mono': {
      path: null,
      ui: {
        path: null,
        component: 'textfield',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    'textfield-large-multi': {
      path: null,
      ui: {
        path: null,
        component: 'textarea',
        layout: {
          columns: 2,
          size: 'large',
        },
      },
    },
    'textfield-medium-mono': {
      path: null,
      ui: {
        path: null,
        component: 'textfield',
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
    'textfield-medium-multi': {
      path: null,
      ui: {
        path: null,
        component: 'textarea',
        layout: {
          columns: 2,
          size: 'medium',
        },
      },
    },
    'textfield-short-mono': {
      path: null,
      ui: {
        path: null,
        component: 'textfield',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
    'textfield-short-multi': {
      path: null,
      ui: {
        path: null,
        component: 'textarea',
        layout: {
          columns: 1,
          size: 'small',
        },
      },
    },
  };

  public mapOntologyToComponent(ontology: ExtendedOntology): ComponentName {
    const defaultComponent: ComponentName = 'textfield-short-mono';

    if (!ontology) {
      return defaultComponent;
    }

    if (ontology.DataType === 'object') {
      const computedDepth = ontology.path.split('.').length - 1;

      if (computedDepth !== ontology.Depth) {
        throw new Error("Computed ontogoly depth is not equal to it's attribute Depth");
      }

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
    }

    if (ontology.DataType === 'string') {
      const isUnique = ontology.Cardinality.endsWith('1') || ontology.Cardinality === 'one';
      const isMultiple = !isUnique;
      const isSpecial = ontology.path.includes('_.');

      if (isSpecial) {
        if (isUnique && ontology.DataSize === 'short') {
          return 'attribut-short-mono';
        }
        if (isMultiple && ontology.DataSize === 'short') {
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

      if (isUnique && ontology.DataSize === 'short') {
        return 'textfield-short-mono';
      }
      if (isUnique && ontology.DataSize === 'medium') {
        return 'textfield-medium-mono';
      }
      if (isUnique && ontology.DataSize === 'large') {
        return 'textfield-large-mono';
      }
      if (isMultiple && ontology.DataSize === 'short') {
        return 'textfield-short-multi';
      }
      if (isMultiple && ontology.DataSize === 'medium') {
        return 'textfield-medium-multi';
      }
      if (isMultiple && ontology.DataSize === 'large') {
        return 'textfield-large-multi';
      }

      return defaultComponent;
    }

    if (ontology.DataType === 'enum') {
      const isUnique = ontology.Cardinality.endsWith('1') || ontology.Cardinality === 'one';
      const isMultiple = !isUnique;

      if (isUnique) {
        return 'select-mono';
      }
      if (isMultiple) {
        return 'select-multi';
      }

      return defaultComponent;
    }

    if (ontology.DataType === 'date') {
      return 'datepicker-date';
    }
    if (ontology.DataType === 'datetime') {
      return 'datepicker-datetime';
    }
    if (ontology.DataType === 'number') {
      return defaultComponent;
    }

    return defaultComponent;
  }

  private mapComponentToDisplayRule(component: ComponentName, path: string = null): DisplayRule {
    const displayRule = this.ontologyComponentTypeToDisplayRule[component];
    const { ui } = displayRule;

    return { ...displayRule, path, ui: { ...ui, path } };
  }

  public mapOntologyToDisplayRule(ontology: ExtendedOntology): DisplayRule {
    const path = this.getOntologyFrontendModelPath(ontology);
    const component: ComponentName = this.mapOntologyToComponent(ontology);
    const displayRule = this.mapComponentToDisplayRule(component, path);

    return displayRule;
  }

  public mapOntologiesToDisplayRules(ontologies: ExtendedOntology[]): DisplayRule[] {
    return ontologies.map((ontology) => this.mapOntologyToDisplayRule(ontology));
  }

  public getOntologyFrontendModelPath(ontology: ExtendedOntology): string {
    const fragments = ontology.path.split('.');

    if (ontology.ApiField) {
      fragments.pop();
      fragments.push(ontology.ApiField);
    }

    return fragments.join('.');
  }
}
