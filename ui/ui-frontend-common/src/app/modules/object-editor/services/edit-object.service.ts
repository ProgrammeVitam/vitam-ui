import { Injectable } from '@angular/core';

import { AbstractControl, FormArray, FormBuilder } from '@angular/forms';
import { orderedFields } from '../../archive/archive-unit-fields';
import { Logger } from '../../logger/logger';
import { Schema } from '../../models';
import { DisplayRule } from '../../object-viewer/models';
import { DataStructureService } from '../../object-viewer/services/data-structure.service';
import { TypeService } from '../../object-viewer/services/type.service';
import { ComponentType, DisplayObjectType } from '../../object-viewer/types';
import { Action, EditObject } from '../models/edit-object.model';
import { PathService } from './path.service';
import { SchemaOptions, SchemaService } from './schema.service';

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
    private logger: Logger,
  ) {}

  public editObject(path: string, data: any, template: DisplayRule[], schema: Schema): EditObject {
    const schemaPath = this.schemaService.normalize(path);
    const defaultValue = this.schemaService.data(schemaPath, schema);
    const baseEditObject = this.baseEditObject(path, schemaPath, data, defaultValue, template, schema);
    let children = [];
    let control: AbstractControl = this.formBuilder.control(data);
    let actions: { [key: string]: Action } = {};

    if (this.typeService.isList(data)) {
      if (baseEditObject.kind === 'object-array') {
        children = data.map((value: any, index: number) => this.editObject(`${path}[${index}]`, value, template, schema));
        control = this.formBuilder.array(children.map((child) => child.control));

        // Add action to current editObject
        actions.add = {
          label: ADD_ACTION_LABEL,
          handler: (data = null) => {
            const fullData = defaultValue ? this.dataService.deepMerge(defaultValue, data) : data;
            const editObject = this.editObject(`${path}[${children.length}]`, fullData, template, schema);

            (control as FormArray).push(editObject.control);
            children.push(editObject);
            control.markAsDirty();

            editObject.actions.add = actions.add;
            editObject.actions.remove = {
              label: REMOVE_ACTION_LABEL,
              handler: () => this.remove({ ...baseEditObject, control, children })(editObject),
            };
          },
        };

        // Add actions to child of current object
        children.forEach((child, index) => {
          child.actions.add = actions.add;
          child.actions.remove = {
            label: REMOVE_ACTION_LABEL,
            handler: () => this.remove({ ...baseEditObject, control, children })(child),
          };
        });
      }
    }

    if (this.typeService.isGroup(data)) {
      const fullData = defaultValue ? this.dataService.deepMerge(defaultValue, data) : data;

      if (fullData) {
        children = Object.entries(fullData).map(([key, value]) =>
          this.editObject(`${path ? path + '.' + key : key}`, value, template, schema),
        );
        control = this.formBuilder.group(children.reduce((acc, child) => ({ ...acc, [child.key]: child.control }), {}));
      }
    }

    if (baseEditObject.displayRule?.ui?.disabled) control.disable({ onlySelf: true, emitEvent: false });

    const editObject = { ...baseEditObject, control, children, actions } as EditObject;
    this.sort(editObject, orderedFields);

    return editObject;
  }

  public kind(data: any): 'object' | 'object-array' | 'primitive-array' | 'primitive' | 'unknown' {
    if (this.typeService.isPrimitive(data)) return 'primitive';
    if (this.typeService.isPrimitiveList(data)) return 'primitive-array';
    if (this.typeService.isList(data)) return 'object-array';
    if (this.typeService.isGroup(data)) return 'object';

    return 'unknown';
  }

  public createaTemplateSchema(template: DisplayRule[], schema: Schema, options: SchemaOptions = { pathKey: 'ApiPath' }): Schema {
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
          Indexed: false,
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
        .filter((element) => (element[options.pathKey] as string).startsWith(rule.Path))
        .forEach((element) => {
          element[options.pathKey] = element[options.pathKey].replace(rule.Path, rule.ui.Path);
        });
    });

    return next;
  }

  public valueToType(data: any): DisplayObjectType {
    switch (this.kind(data)) {
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

  public sort(editObject: EditObject, ordenedFields: string[]): void {
    editObject?.children?.forEach((child) => this.sort(child, ordenedFields));

    if (editObject.kind !== 'object') return;

    const ordenedChildPaths = this.pathService.children(this.schemaService.normalize(editObject.path), ordenedFields);
    const ordenedChildren = ordenedChildPaths.reduce((acc, childPath) => {
      const child = editObject.children.find((child) => this.schemaService.normalize(child.path) === childPath);

      if (child) return acc.concat([child]);

      this.logger.log(this, `Child path '${childPath}' not found in editObject children`, editObject);

      return acc;
    }, []);
    const unmatchedChildren = editObject.children.filter(
      (child) =>
        !ordenedChildren.some(
          (ordenedChild) => this.schemaService.normalize(ordenedChild.path) === this.schemaService.normalize(child.path),
        ),
    );
    const sortedChildren = ordenedChildren.concat(unmatchedChildren);

    // Ici, on met à jour la référence car ça nous évite de recalculer les actions pour chaque editObject.
    editObject.children.splice(0, editObject.children.length);
    sortedChildren.forEach((item) => editObject.children.push(item));
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
    const isRootPath = !Boolean(path);
    const kind = isRootPath || this.isArrayElement(key) ? this.kind(value) : this.schemaService.kind(schemaPath, schema);
    const type = this.kindToType(kind);
    const displayRule = template.find((rule) => rule.ui.Path === schemaPath);
    const component: ComponentType =
      displayRule?.ui?.component || (['object', 'object-array', 'primitive-array'].includes(kind) ? 'group' : 'textfield');

    return {
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
    };
  }

  private isArrayElement(path: string): boolean {
    return /\[\d+\]/gm.test(path);
  }

  private remove =
    (parent: Partial<EditObject>) =>
    (child: EditObject): void => {
      const index = parent.children.findIndex((item) => item === child);

      if (index < 0) throw new Error('Cannot removeAt negative index');
      if (parent.kind !== 'object-array') throw new Error('Cannot removeAt on non object array');

      (parent.control as FormArray).removeAt(index);
      parent.control.markAsDirty();
      parent.children.splice(index, 1);
    };
}
