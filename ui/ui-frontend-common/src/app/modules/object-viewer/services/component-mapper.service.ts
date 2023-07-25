import { DisplayRule, ExtendedOntology, Layout, Ui } from '../models';
import { ComponentType } from '../types';

type Component =
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

export class ComponentMapperService {
  public mapOntologyToComponent(ontology: ExtendedOntology): Component {
    const defaultComponent: Component = 'textfield-short-mono';

    if (!ontology) { return defaultComponent; }

    if (ontology.DataType === 'object') {
      const computedDepth = ontology.path.split('.').length - 1;

      if (computedDepth !== ontology.Depth) {
        throw new Error('Computed ontogoly depth is not equal to it\'s attribute Depth');
      }

      if (computedDepth === 0) { return 'balise-n1'; }
      if (computedDepth === 1) { return 'balise-n2'; }
      if (computedDepth === 2) { return 'balise-n3'; }
      if (computedDepth === 3) { return 'balise-n4'; }

      return defaultComponent;
    }

    if (ontology.DataType === 'string') {
      const isUnique = ontology.Cardinality.endsWith('1') || ontology.Cardinality === 'one';
      const isMultiple = !isUnique;
      const isSpecial = ontology.path.includes('_.');

      if (isSpecial) {
        if (isUnique && ontology.DataSize === 'short') { return 'attribut-short-mono'; }
        if (isMultiple && ontology.DataSize === 'short') { return 'attribut-short-multi'; }
        if (isUnique) { return 'attribut-mono'; }
        if (isMultiple) { return 'attribut-multi'; }

        return defaultComponent;
      }

      if (isUnique && ontology.DataSize === 'short') { return 'textfield-short-mono'; }
      if (isUnique && ontology.DataSize === 'medium') { return 'textfield-medium-mono'; }
      if (isUnique && ontology.DataSize === 'large') { return 'textfield-large-mono'; }
      if (isMultiple && ontology.DataSize === 'short') { return 'textfield-short-multi'; }
      if (isMultiple && ontology.DataSize === 'medium') { return 'textfield-medium-multi'; }
      if (isMultiple && ontology.DataSize === 'large') { return 'textfield-large-multi'; }

      return defaultComponent;
    }

    if (ontology.DataType === 'enum') {
      const isUnique = ontology.Cardinality.endsWith('1') || ontology.Cardinality === 'one';
      const isMultiple = !isUnique;

      if (isUnique) { return 'select-mono'; }
      if (isMultiple) { return 'select-multi'; }

      return defaultComponent;
    }

    if (ontology.DataType === 'date') { return 'datepicker-date'; }
    if (ontology.DataType === 'datetime') { return 'datepicker-datetime'; }
    if (ontology.DataType === 'number') { return defaultComponent; }

    return defaultComponent;
  }

  public mapComponentToComponentType(component: Component): ComponentType {
    switch (component) {
      case 'balise-n1':
      case 'balise-n2':
      case 'balise-n3':
      case 'balise-n4':
        return 'group';
      case 'textfield-short-multi':
      case 'textfield-medium-multi':
      case 'textfield-large-multi':
        return 'textarea';
      case 'textfield-short-mono':
      case 'textfield-medium-mono':
      case 'textfield-large-mono':
        return 'textfield';
      case 'attribut-short-mono':
      case 'attribut-mono':
        return 'select+textfield';
      case 'attribut-short-multi':
      case 'attribut-multi':
        return 'select+textarea';
      case 'select-mono':
      case 'select-multi':
        return 'select';
      case 'datepicker-date':
        return 'datepicker';
      case 'datepicker-datetime':
        return 'datetime';
      default:
        return null;
    }
  }

  public mapComponentToUi(component: Component, path: string = null): Ui {
    return {
      path,
      component: this.mapComponentToComponentType(component),
      layout: this.mapComponentToLayout(component),
    };
  }

  public mapComponentToLayout(component: Component): Layout {
    return {
      columns: this.mapComponentToColumns(component),
      size: this.mapComponentToSize(component),
    };
  }

  public mapComponentToColumns(component: Component): number {
    switch (component) {
      case 'textfield-short-multi':
      case 'textfield-short-mono':
      case 'attribut-short-mono':
      case 'attribut-short-multi':
      case 'select-mono':
      case 'select-multi':
      case 'datepicker-date':
      case 'datepicker-datetime':
        return 1;
      case 'balise-n1':
      case 'balise-n2':
      case 'balise-n3':
      case 'balise-n4':
      case 'textfield-medium-multi':
      case 'textfield-large-multi':
      case 'textfield-medium-mono':
      case 'textfield-large-mono':
      case 'attribut-mono':
      case 'attribut-multi':
      default:
        return 2;
    }
  }

  public mapComponentToSize(component: Component): 'small' | 'medium' | 'large' {
    if (component.includes('short')) { return 'small'; }
    if (component.includes('medium')) { return 'medium'; }
    if (component.includes('large')) { return 'large'; }

    return 'small';
  }

  public mapComponentToDisplayRule(component: Component, path: string = null): DisplayRule {
    return {
      path,
      ui: this.mapComponentToUi(component, path),
    };
  }

  public mapOntologyToDisplayRule(ontology: ExtendedOntology): DisplayRule {
    const path = this.getOntologyFrontendModelPath(ontology);
    const component: Component = this.mapOntologyToComponent(ontology);
    const displayRule = this.mapComponentToDisplayRule(component, path);

    return displayRule;
  }

  public mapOntologiesToDisplayRules(ontologies: ExtendedOntology[]): DisplayRule[] {
    return ontologies.map((ontology) => this.mapOntologyToDisplayRule(ontology));
  }

  public getOntologyFrontendModelPath(ontology: ExtendedOntology): string {
    const fragments = ontology.path.split('.');

    fragments.pop();
    fragments.push(ontology.ApiField);

    return fragments.join('.');
  }
}
