import { Injectable } from '@angular/core';
import { DataStructureService } from '../../object-viewer/services/data-structure.service';

@Injectable({ providedIn: 'root' })
export class PathService {
  constructor(private dataStructureService: DataStructureService) {}

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
