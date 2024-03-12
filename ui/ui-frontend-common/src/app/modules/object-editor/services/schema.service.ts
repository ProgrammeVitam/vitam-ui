import { Injectable } from '@angular/core';
import { Collection, Schema } from '../../models';
import { SchemaElement } from '../../object-viewer/models';
import { SedaVersion } from '../../object-viewer/types';
import { PathService } from './path.service';

export interface SchemaOptions {
  collection?: Collection;
  versions?: SedaVersion[];
  pathKey: string;
}

@Injectable({ providedIn: 'root' })
export class SchemaService {
  constructor(private pathService: PathService) {}

  public subschema(schema: Schema, options: SchemaOptions = { collection: null, versions: null, pathKey: 'ApiPath' }): Schema {
    let subschema = schema.slice();

    if (options.collection) subschema = subschema.filter((schemaElement) => schemaElement.Collection === options.collection);
    if (options.versions)
      subschema = subschema.filter((schemaElement) => schemaElement.SedaVersions.some((version) => options.versions.includes(version)));

    return subschema;
  }

  public data(path: string, schema: Schema, options: SchemaOptions = { collection: null, versions: null, pathKey: 'ApiPath' }): any {
    const subschema = this.subschema(schema, options);
    const paths = subschema.map((element) => element[options.pathKey]);
    const children = this.pathService.children(path, paths);

    if (children.length) {
      const entries = children.map((child) => {
        const element = subschema.find((element) => element[options.pathKey] === child);
        const key = child.split('.').pop();

        if (this.isArray(element)) return { key, value: [] };
        if (this.isObject(element)) return { key, value: {} };

        return { key, value: null };
      });

      return entries.reduce((acc, cur) => ({ ...acc, [cur.key]: cur.value }), {});
    }

    const element = subschema.find((element) => element[options.pathKey] === this.normalize(path));

    if (element) {
      const isPrimitiveArray = !this.isObject(element) && this.isArray(element);

      if (isPrimitiveArray) return [];
    }

    return null;
  }

  /**
   * Removes array indexes from path to match schema formalism.
   *
   * @param path a path.
   * @returns a without array indexes.
   */
  public normalize(path: string): string {
    return path ? path.replace(/\[\d+\]/g, '') : path;
  }

  public kind(
    path: string,
    schema: Schema,
    options: SchemaOptions = { pathKey: 'ApiPath' },
  ): 'object' | 'object-array' | 'primitive-array' | 'primitive' | 'unknown' {
    const element = schema.find((element) => element[options.pathKey] === path);

    if (element) {
      const isArray = this.isArray(element);
      const isObject = this.isObject(element);

      if (isArray && isObject) return 'object-array';
      if (isArray) return 'primitive-array';
      if (isObject) return 'object';
      return 'primitive';
    }

    return 'unknown';
  }

  public validate(schema: Schema, messages = []) {
    const paths = schema.map((element) => element.Path);

    if (paths.length > new Set(paths).size) {
      messages.push(`Schema seems have duplicates elements`);
    }

    schema.forEach((element) => this.collectErrors(element, schema, paths, messages));

    if (messages.length) {
      throw new Error(JSON.stringify(messages, null, 2));
    }
  }

  private collectErrors(element: SchemaElement, schema: Schema, paths: string[], messages: string[]): void {
    const childrenPaths = this.pathService.children(element.Path, paths);

    if (childrenPaths.length > new Set(childrenPaths).size) {
      messages.push(`Element '${element.Path}' seems have duplicates elements in its children '${childrenPaths}'`);
    }

    const children = schema.filter((e) => childrenPaths.includes(e.Path));

    if (element.Type !== 'OBJECT' && children.length) {
      messages.push(`Element '${element.Path}' is a leaf and seems have children '${childrenPaths}'`);
    }

    if (element.Type === 'OBJECT' && children.length === 0) {
      messages.push(`Element '${element.Path}' is a group and seems not have children`);
    }

    children.forEach((child) => this.collectErrors(child, schema, paths, messages));
  }

  private isArray(element: SchemaElement): boolean {
    return element.Cardinality.includes('MANY');
  }

  private isObject(element: SchemaElement): boolean {
    return element.DataType === 'OBJECT';
  }
}
