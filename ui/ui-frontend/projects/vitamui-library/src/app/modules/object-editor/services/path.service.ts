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
import { DataStructureService } from '../../object-viewer/services/data-structure.service';

@Injectable()
export class PathService {
  private dataStructureService = inject(DataStructureService);

  public dot(path: string): string {
    return path.replace(/\[/g, '.').replace(/\]/g, '');
  }

  public children(path: string, paths: string[], separator = '.'): string[] {
    if (path === null || path === undefined) return [];
    if (path === '') return paths.filter((item) => item.split(separator).length === 1);

    return paths
      .filter((item) => item.startsWith(path + separator) || item.startsWith(path + '['))
      .filter((item) => this.dot(item).split(separator).length === this.dot(path).split(separator).length + 1);
  }

  public paths(data: any, options = { arrayNotation: true }): string[] {
    return Array.from(
      Object.keys(this.dataStructureService.flatten(data, options.arrayNotation)).reduce((acc, cur) => {
        const fragments = cur.split('.');

        while (fragments.length) {
          acc.add(fragments.join('.'));
          fragments.pop();
        }

        return acc;
      }, new Set<string>()),
    ).sort();
  }

  public entries(data: any, options = { arrayNotation: true }): { key: string; value: any }[] {
    return Array.from(
      Object.entries(this.dataStructureService.flatten(data, options.arrayNotation)).reduce((acc, cur) => {
        const fragments = cur[0].split('.');

        while (fragments.length) {
          const path = fragments.join('.');
          const value = this.dataStructureService.deepValue(data, path);
          acc.add({ key: path, value });
          fragments.pop();
        }

        return acc;
      }, new Set<{ key: string; value: any }>()),
    ).sort();
  }

  public value(data: any, path: string): any {
    return this.dataStructureService.deepValue(data, path);
  }
}
