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

@Injectable()
export class DataStructureService {
  /**
   * Transforms a nested object into a flat object.
   *
   * @param data Nested object.
   * @returns Flat object.
   */
  public flatten(data: any) {
    const result = {};
    function recurse(cur: any, prop: any) {
      if (Object(cur) !== cur) {
        result[prop] = cur;
      } else if (Array.isArray(cur)) {
        for (let i = 0, l = cur.length; i < l; i++) {
          recurse(cur[i], prop + '[' + i + ']');
        }
        if (cur.length === 0) {
          result[prop] = [];
        }
      } else {
        let isEmpty = true;
        for (const p in cur) {
          isEmpty = false;
          recurse(cur[p], prop ? prop + '.' + p : p);
        }
        if (isEmpty && prop) {
          result[prop] = {};
        }
      }
    }
    recurse(data, '');
    return result;
  }

  /**
   * Transforms flat a object into nested object.
   *
   * @param data Flat object.
   * @returns Nested object.
   */
  public unflatten(data: any) {
    if (Object(data) !== data || Array.isArray(data)) {
      return data;
    }

    const regex = /\.?([^.\[\]]+)|\[(\d+)\]/g;
    const resultholder = {};

    for (const p in data) {
      let cur = resultholder;
      let prop = '';
      let m: RegExpExecArray;

      while ((m = regex.exec(p))) {
        cur = cur[prop] || (cur[prop] = m[2] ? [] : {});
        prop = m[2] || m[1];
      }
      cur[prop] = data[p];
    }

    return resultholder[''] || resultholder;
  }

  /**
   * Finds a value inside an object by providing its path.
   *
   * @param obj Root object for the research.
   * @param path Path of value inside the root object.
   * @returns Found value.
   */
  public deepValue(obj: any, path: any): any {
    path = path.replace(/\[(\w+)\]/g, '.$1'); // Convertit tous les [index] en .index
    path = path.replace(/^\./, ''); // Supprime le point au début s'il y en a un

    const keys = path.split('.');

    for (let i = 0, len = keys.length; i < len; i++) {
      if (obj && typeof obj === 'object') {
        obj = obj[keys[i]];
      } else {
        return undefined;
      }
    }

    return obj;
  }

  /**
   * Folds an array into sereval rows.
   *
   * @param array Initial array.
   * @param columnCount Max columns by row.
   * @returns A n+1 dimentional array.
   */
  public fold(array: any[], columnCount: number): any[][] {
    const rowCount = Math.ceil(array.length / columnCount);
    const rows = [];

    for (let row = 0; row < rowCount; row++) {
      const columns: any[] = [];

      for (let column = 0; column < columnCount; column++) {
        const index = row * columnCount + column;

        if (index < array.length) {
          columns.push(array[index]);
        }
      }

      rows.push(columns);
    }

    return rows;
  }

  /**
   * Deep merge two objects.
   *
   * @param target Target object.
   * @param source Source object.
   * @returns A merged object.
   */
  public deepMerge(target: any, source: any) {
    // Iterate through all properties in the source object
    for (const key in source) {
      if (source.hasOwnProperty(key)) {
        // Check if the current property is an object and needs merging
        if (source[key] instanceof Object && key in target && target[key] instanceof Object) {
          // Recursively merge the nested objects
          this.deepMerge(target[key], source[key]);
        } else {
          // Perform a regular assignment if the property doesn't need merging
          target[key] = source[key];
        }
      }
    }

    return target;
  }
}
