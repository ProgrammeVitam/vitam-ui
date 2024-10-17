import { Injectable } from '@angular/core';

import { AbstractControl, FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject } from 'rxjs';
import { orderedFields } from '../../archive-unit/archive-unit-fields';
import { Logger } from '../../logger/logger';
import { Schema } from '../../models';
import { DisplayRule, ProfiledSchemaElement, SchemaElement } from '../../object-viewer/models';
import { Template } from '../../object-viewer/models/template.model';
import { DataStructureService } from '../../object-viewer/services/data-structure.service';
import { TypeService } from '../../object-viewer/services/type.service';
import { Cardinality, ComponentType, DisplayObjectType } from '../../object-viewer/types';
import { Action, EditObject } from '../models/edit-object.model';
import { PathService } from './path.service';
import { SchemaOptions, SchemaService } from './schema.service';
import { CustomValidators } from '../pattern.validator';

const ADD_ACTION_LABEL = 'ARCHIVE_UNIT.ACTIONS.ADD';
const REMOVE_ACTION_LABEL = 'ARCHIVE_UNIT.ACTIONS.REMOVE';

@Injectable({
  providedIn: 'root',
})
export class EditObjectService {
  constructor(
    private schemaService: SchemaService,
    private typeService: TypeService,
    private dataService: DataStructureService,
    private pathService: PathService,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private logger: Logger,
  ) {}

  public editObject(path: string, data: any, template: DisplayRule[], schema: Schema): EditObject {
    const schemaPath = this.schemaService.normalize(path);
    const defaultValue = this.schemaService.data(schemaPath, schema);
    const baseEditObject = this.baseEditObject(path, schemaPath, data, defaultValue, template, schema);
    let children: EditObject[] = [];
    let control: AbstractControl = this.formBuilder.control(data);
    let actions: { [key: string]: Action } = {};

    if (baseEditObject.kind === 'object-array') {
      children = data?.map((value: any, index: number) => this.editObject(`${path}[${index}]`, value, template, schema)) || [];
      control = this.formBuilder.array(children.map((child) => child.control));
    }

    if (baseEditObject.kind === 'object') {
      const fullData = defaultValue ? this.dataService.deepMerge(defaultValue, data) : data;

      if (fullData) {
        children = Object.entries(fullData).map(([key, value]) =>
          this.editObject(`${path ? path + '.' + key : key}`, value, template, schema),
        );
        control = this.formBuilder.group(children.reduce((acc, child) => ({ ...acc, [child.key]: child.control }), {}));
      }
    }

    const editObject = { ...baseEditObject, control, children, actions } as EditObject;

    editObject.childrenChange.next(children);

    this.computeAddActions(
      template,
      schema,
    )(editObject).forEach((action) => {
      editObject.actions[action.name] = action;
    });
    this.computeChildrenRemoveActions(editObject).forEach((action, i) => {
      const child = editObject.children[i];

      if (!child.required && !child.virtual) child.actions.remove = action;
    });
    this.sort(editObject, orderedFields);

    if (editObject.displayRule?.ui?.disabled) control.disable({ onlySelf: true, emitEvent: false });
    if (editObject.required) control.addValidators(Validators.required);
    if (editObject.pattern) control.addValidators(CustomValidators.pattern(editObject.pattern, editObject.hint));
    if (['primitive-array', 'object-array'].includes(editObject.kind) && editObject.cardinality === 'ONE_REQUIRED')
      control.addValidators(Validators.maxLength(1));
    if (editObject.kind === 'object-array' && editObject.children.length === 0) editObject.actions.add.handler();

    this.removeCardinalityZero(editObject);

    return editObject;
  }

  public kind(data: any): 'object' | 'object-array' | 'primitive-array' | 'primitive' | 'unknown' {
    if (this.typeService.isPrimitive(data)) return 'primitive';
    if (this.typeService.isPrimitiveList(data)) return 'primitive-array';
    if (this.typeService.isList(data)) return 'object-array';
    if (this.typeService.isGroup(data)) return 'object';

    return 'unknown';
  }

  public createTemplateSchema(template: DisplayRule[], schema: Schema, options: SchemaOptions = { pathKey: 'ApiPath' }): Schema {
    const next: Schema = JSON.parse(JSON.stringify(schema));

    template.forEach((rule) => {
      if (rule.Path === null) {
        const field = rule.ui.Path.split('.').pop();

        next.push({
          Path: rule.ui.Path,
          FieldName: field,
          ApiField: field,
          Type: 'OBJECT',
          DataType: 'OBJECT',
          Origin: 'VIRTUAL',
          StringSize: 'MEDIUM',
          Cardinality: 'ONE',
          SedaVersions: options.versions || [],
          Collection: options.collection,
          ApiPath: rule.ui.Path,
          Category: 'DESCRIPTION',
          ShortName: rule.ui.label,
        });

        return;
      }

      next
        .filter(
          (element) =>
            (element[options.pathKey as keyof SchemaElement] as string).startsWith(rule.Path + '.') ||
            (element[options.pathKey as keyof SchemaElement] as string).startsWith(rule.Path + '[') ||
            (element[options.pathKey as keyof SchemaElement] as string) === rule.Path,
        )
        .forEach((element) => {
          // @ts-ignore
          element[options.pathKey] = (element[options.pathKey] as string).replace(rule.Path, rule.ui.Path);
        });
    });

    return next;
  }

  public kindToType(kind: EditObject['kind']): DisplayObjectType {
    switch (kind) {
      case 'object':
        return DisplayObjectType.GROUP;
      case 'object-array':
      case 'primitive-array':
        return DisplayObjectType.LIST;
      case 'primitive':
      default:
        return DisplayObjectType.PRIMITIVE;
    }
  }

  public sort(editObject: EditObject, orderedFields: string[]): void {
    editObject?.children?.forEach((child) => this.sort(child, orderedFields));

    if (editObject.kind !== 'object') return;

    const orderedChildPaths = this.pathService.children(this.schemaService.normalize(editObject.path), orderedFields);
    const orderedChildren = orderedChildPaths.reduce((acc, childPath) => {
      const child = editObject.children.find((child) => this.schemaService.normalize(child.path) === childPath);

      if (child) return acc.concat([child]);

      this.logger.log(this, `Child path '${childPath}' not found in editObject children`, editObject);

      return acc;
    }, []);
    const unmatchedChildren = editObject.children.filter(
      (child) =>
        !orderedChildren.some(
          (orderedChild) => this.schemaService.normalize(orderedChild.path) === this.schemaService.normalize(child.path),
        ),
    );
    const sortedChildren = orderedChildren.concat(unmatchedChildren);

    // Ici, on met à jour la référence car ça nous évite de recalculer les actions pour chaque editObject.
    editObject.children.splice(0, editObject.children.length);
    sortedChildren.forEach((item) => editObject.children.push(item));
  }

  private removeCardinalityZero(editObject: EditObject) {
    editObject?.children?.forEach((child) => this.removeCardinalityZero(child));

    if (editObject.cardinality !== 'ZERO') {
      if (editObject.virtual) {
        if (!editObject.children.every((ele) => ele.cardinality === 'ZERO')) {
          return;
        }
        editObject.cardinality = 'ZERO';
      }

      if (['object-array', 'object'].includes(editObject.kind)) editObject?.actions?.remove?.handler();
      else {
        editObject.displayRule = { ...editObject.displayRule, ui: { ...editObject.displayRule.ui, display: false } };
        editObject.control.setValue(editObject.kind === 'primitive-array' ? [] : null);
      }
    }
  }

  private baseEditObject(
    path: string,
    schemaPath: string,
    value: any,
    defaultValue: any,
    template: DisplayRule[],
    schema: Schema,
  ): Partial<EditObject> {
    const key = path.split('.').pop();
    const isRoot = path === '';
    const kind = this.computeKind(value, path, schemaPath, schema);
    const type = this.kindToType(kind);
    const displayRule = template.find((rule) => rule.ui.Path === schemaPath);
    const component: ComponentType =
      displayRule?.ui?.component || (['object', 'object-array', 'primitive-array'].includes(kind) ? 'group' : 'textfield');
    const partialEditObject: Partial<EditObject> = {
      key,
      path,
      kind,
      type,
      component,
      value,
      default: defaultValue,
      displayRule,
      open: true,
      favoriteKeys: [],
      required: true,
      virtual: false,
      childrenChange: new BehaviorSubject<EditObject[]>([]),
      cardinality: this.computeCardinality(kind, schemaPath, schema),
    };

    if (isRoot) return partialEditObject;

    const schemaElement = this.schemaService.find(schemaPath, schema);

    if (!schemaElement) return partialEditObject;

    const profiledSchemaElement: ProfiledSchemaElement = schemaElement;
    const { Control, EffectiveCardinality } = profiledSchemaElement;

    // Have currently 2 types of controls REGEX or SELECT.
    // Value or Values field is sufficient to determine the kind of control.
    const pattern = Control?.Value;
    const options = Control?.Values;
    const hint = Control?.Comment;
    const cardinality = EffectiveCardinality || schemaElement.Cardinality;

    return {
      ...partialEditObject,
      required: this.schemaService.isRequired(schemaElement as ProfiledSchemaElement),
      virtual: this.schemaService.isVirtual(schemaElement),
      pattern,
      options,
      hint,
      cardinality,
    };
  }

  private computeCardinality(kind: EditObject['kind'], schemaPath: string, schema: Schema): Cardinality {
    const element = this.schemaService.find(schemaPath, schema);

    if (element) return element.Cardinality;
    if (['object-array', 'primitive-array'].includes(kind)) return 'MANY';

    return 'ONE';
  }

  private computeKind(value: any, path: string, schemaPath: string, schema: Schema): EditObject['kind'] {
    const isRoot = path === '';
    const isObject = /\[\d+\]$/gm.test(path);
    const element = this.schemaService.find(schemaPath, schema);
    const kind = this.schemaService.kind(schemaPath, schema);

    if (isRoot || isObject || kind === 'unknown' || element.Origin === 'EXTERNAL') return this.kind(value);

    return kind;
  }

  private computeChildrenRemoveActions = (editObject: Partial<EditObject>): Action[] => {
    if (editObject.kind === 'object-array') {
      return editObject.children.map((child) => ({
        name: 'remove',
        label: REMOVE_ACTION_LABEL,
        handler: () => {
          const index = editObject.children.findIndex((item) => item === child);
          const canRemove =
            Boolean(editObject.required && editObject.children.length > 1) ||
            Boolean(!editObject.required && editObject.children.length > 0);

          if (index !== -1 && canRemove) {
            (editObject.control as FormArray).removeAt(index);
            (editObject.control as FormArray).markAsDirty();
            editObject.children.splice(index, 1);
            editObject.childrenChange.next(editObject.children);
          }
        },
      }));
    }

    if (editObject.kind === 'object') {
      return editObject.children.map((child) => ({
        name: 'remove',
        label: REMOVE_ACTION_LABEL,
        handler: () => {
          const index = editObject.children.findIndex((item) => item === child);

          if (index !== -1) {
            (editObject.control as FormGroup).removeControl(child.key);
            (editObject.control as FormArray).markAsDirty();
            editObject.children.splice(index, 1);
            editObject.childrenChange.next(editObject.children);
          }
        },
      }));
    }

    return [];
  };

  private computeAddActions =
    (template: Template, schema: Schema) =>
    (editObject: Partial<EditObject>): Action[] => {
      if (editObject.kind === 'object-array') {
        const add: Action = {
          name: 'add',
          label: ADD_ACTION_LABEL,
          handler: (data: any = null) => {
            const defaultValue = this.schemaService.data(this.schemaService.normalize(editObject.path), schema);
            const fullData = defaultValue ? this.dataService.deepMerge(defaultValue, data) : data;
            const eo = this.editObject(`${editObject.path}[${editObject.children.length}]`, fullData, template, schema);
            const canAdd =
              Boolean(editObject.cardinality !== 'ZERO') &&
              (Boolean(['ONE', 'ONE_REQUIRED'].includes(editObject.cardinality) && editObject.children.length < 1) ||
                Boolean(['MANY', 'MANY_REQUIRED'].includes(editObject.cardinality)));

            if (canAdd) {
              (editObject.control as FormArray).push(eo.control);
              (editObject.control as FormArray).markAsDirty();
              editObject.children.push(eo);
              this.sort(editObject as EditObject, orderedFields);
              editObject.childrenChange.next(editObject.children);

              this.computeChildrenRemoveActions(editObject).forEach((action, i) => {
                editObject.children[i].actions.remove = action;
              });

              eo.actions.add = add;
            }
          },
        };

        editObject.children.forEach((child) => {
          child.actions.add = add;
        });

        return [add];
      }

      if (editObject.kind === 'object') {
        return editObject.children
          .filter((child) => child.kind === 'object')
          .filter((child) => !child.required)
          .filter((child) => !child.virtual)
          .map((child) => {
            return {
              name: `add-${child.key.toLowerCase()}`,
              label: `${this.translateService.instant(ADD_ACTION_LABEL)}: ${child.key}`,
              handler: (data: any = null) => {
                if (editObject.children.some((item) => item.path === child.path)) return;

                const defaultValue = this.schemaService.data(this.schemaService.normalize(child.path), schema);
                const fullData = defaultValue ? this.dataService.deepMerge(defaultValue, data) : data;
                const eo = this.editObject(child.path, fullData, template, schema);

                (editObject.control as FormGroup).addControl(eo.key, eo.control);
                (editObject.control as FormGroup).markAsDirty();
                editObject.children.push(eo);
                this.sort(editObject as EditObject, orderedFields);
                editObject.childrenChange.next(editObject.children);

                this.computeChildrenRemoveActions(editObject).forEach((action, i) => {
                  editObject.children[i].actions.remove = action;
                });
              },
            };
          });
      }

      return [];
    };
}
