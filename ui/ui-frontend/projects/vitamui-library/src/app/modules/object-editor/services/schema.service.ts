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
import { Injectable, inject } from '@angular/core';
import { Logger } from '../../logger/logger';
import { Collection, ProfiledSchemaElement, Schema, SchemaElement } from '../../models';
import { internationalizedKeys } from '../../object-viewer/services/display-object-helper.service';
import { SedaVersion } from '../../object-viewer/types';
import { PathService } from './path.service';

export interface SchemaOptions {
  collection?: Collection;
  versions?: SedaVersion[];
  pathKey: keyof SchemaElement;
}

interface SchemaError {
  readonly element: SchemaElement;
  readonly messages: string[];
}

@Injectable()
export class SchemaService {
  private pathService = inject(PathService);
  private logger = inject(Logger);

  public subschema(schema: Schema, options: SchemaOptions = { collection: null, versions: null, pathKey: 'ApiPath' }): Schema {
    let subschema = schema.slice();

    if (options.collection) subschema = subschema.filter((schemaElement) => schemaElement.Collection === options.collection);
    if (options.versions)
      subschema = subschema.filter((schemaElement) => schemaElement.SedaVersions?.some((version) => options.versions.includes(version)));

    return subschema;
  }

  public data(path: string, schema: Schema, options: SchemaOptions = { collection: null, versions: null, pathKey: 'ApiPath' }): any {
    const subschema = this.subschema(schema, options);
    const paths = subschema.map((element) => element[options.pathKey]) as string[];
    const children = this.pathService.children(path, paths);

    if (children.length) {
      const entries = children.map((child) => {
        const element = subschema.find((element) => element[options.pathKey] === child);
        const key = child.split('.').pop();

        if (this.isArray(element)) return { key, value: [] as unknown[] };
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

  public find(path: string, schema: Schema, options: SchemaOptions = { pathKey: 'ApiPath' }): SchemaElement {
    return schema.find((element) => element[options.pathKey] === path);
  }

  public validate(schema: Schema, options = { passive: false }) {
    const errorMessages: string[] = [];
    const paths = schema.map((element) => element.Path);

    if (paths.length > new Set(paths).size) {
      errorMessages.push(`Schema seems have duplicates elements`);
    }

    const schemaErrors = schema.map((element) => this.collectSchemaElementErrors(element, schema, paths));

    schemaErrors.forEach((schemaError) => schemaError.messages.forEach((message) => errorMessages.push(message)));

    if (errorMessages.length) {
      const content = errorMessages.reduce((acc, cur, i) => `${acc}${i + 1}. ${cur}\n`, '');
      const errorReport = `Validation found ${errorMessages.length} errors in provided schema.\n\n${content}`;

      if (options.passive) {
        this.logger.warn(this, errorReport);
      } else {
        throw new Error(errorReport);
      }
    }
  }

  public collectSchemaElementErrors(element: SchemaElement, schema: Schema, paths: string[]): SchemaError {
    const messages: string[] = [];
    const childrenPaths = this.pathService.children(element.Path, paths);
    const children = schema.filter((e) => childrenPaths.includes(e.Path));

    if (internationalizedKeys.includes(element.ApiField)) {
      this.logger.warn(this, `Element ${element.ApiPath} excluded from validation`);

      return { element, messages };
    }

    if (childrenPaths.length > new Set(childrenPaths).size) {
      messages.push(`Element '${element.Path}' seems have duplicates elements in its children '${childrenPaths}'`);
    }

    if (element.Type !== 'OBJECT' && children.length) {
      messages.push(`Element '${element.Path}' is a leaf and seems have children '${childrenPaths}'`);
    }

    if (element.Type === 'OBJECT' && children.length === 0) {
      messages.push(`Element '${element.Path}' is a group and seems not have children`);
    }

    return { element, messages };
  }

  public isRequired(element: ProfiledSchemaElement): boolean {
    return (element?.EffectiveCardinality || element?.Cardinality)?.includes('REQUIRED') || false;
  }

  public isVirtual(element: SchemaElement) {
    return Boolean(element?.Origin === 'VIRTUAL');
  }

  private isArray(element: SchemaElement): boolean {
    return element.Cardinality.includes('MANY');
  }

  private isObject(element: SchemaElement): boolean {
    return element.DataType === 'OBJECT';
  }
}
