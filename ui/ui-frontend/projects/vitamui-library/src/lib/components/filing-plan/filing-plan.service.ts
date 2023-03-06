import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { HttpHeaders } from '@angular/common/http';
import { Inject, Injectable, LOCALE_ID } from '@angular/core';

import { SearchUnitApiService } from '../../api/search-unit-api.service';

import { DescriptionLevel } from '../../models/description-level.enum';
import { FileType } from '../../models/file-type.enum';
import { Node } from '../../models/node.interface';
import { Unit } from '../../models/unit.interface';

import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { getKeywordValue } from '../../utils/keyword.util';

export enum ExpandLevel {
  NONE,
  ROOT_ONLY,
  ALL
}

export enum FilingPlanMode {
  SOLO,
  INCLUDE_ONLY,
  BOTH
}

@Injectable({
  providedIn: 'root'
})
export class FilingPlanService {
  // tslint:disable-next-line:variable-name
  private _pending = 0;

  constructor(
    private searchUnitApi: SearchUnitApiService,
    @Inject(LOCALE_ID) private locale: string
  ) {
  }

  get pending(): boolean {
    return this._pending > 0;
  }

  private cache: {
    [id: string]: {
      value: Observable<Unit[]>
    }
  } = {};

  getCachedValue(accessContractId: string): Observable<Unit[]> {
    const item = this.cache[accessContractId];
    if (!item) {
      return null;
    }
    return item.value;
  }

  setCachedValue(value: Observable<Unit[]>, accessContractId: string) {
    this.cache[accessContractId] = {value};
  }

  public loadTree(tenantIdentifier: number, accessContractId: string, idPrefix: string): Observable<Node[]> {
    let units$ = this.getCachedValue(accessContractId);
    if (!units$) {
      this._pending++;
      const headers = new HttpHeaders({
        'X-Tenant-Id': tenantIdentifier.toString(),
        'X-Access-Contract-Id': accessContractId
      });
      units$ = this.searchUnitApi.getFilingPlan(headers).pipe(
        catchError(() => {
          return of({$hits: null, $results: []});
        }),
        map(response => response.$results),
        tap(() => this._pending--)
      );
      this.setCachedValue(units$, accessContractId);
    }
    return units$.pipe(map(results => {
      return this.getNestedChildren(results, idPrefix)
    }));
  }

  private getFileTypeFromUnit(unit: Unit): FileType {
    return this.getFileType(unit['#unitType'], unit.DescriptionLevel);
  }

  private getFileType(unitType: string, descriptionLevel: string) {
    // TODO file type for documents
    if (descriptionLevel === DescriptionLevel.FILE) {
      if (unitType === 'HOLDING_UNIT') {
        return FileType.FOLDER_HOLDING;
      }

      return FileType.FOLDER_INGEST;
    }

    return null;
  }

  private getNestedChildren(arr: Unit[], idPrefix: string, parentNode?: Node): Node[] {
    const out: Node[] = [];
    arr.forEach((unit) => {
      if (
        (parentNode && parentNode.vitamId && unit['#unitups'] && unit['#unitups'][0] === parentNode.vitamId)
        ||
        (!parentNode && (!unit['#unitups'] || !unit['#unitups'].length || !idExists(arr, unit['#unitups'][0])))
      ) {
        const outNode: Node = {
          id: idPrefix + '-' + unit['#id'],
          label: unit.Title,
          type: this.getFileTypeFromUnit(unit),
          children: [],
          ingestContractIdentifier: getKeywordValue(unit, 'ingest_contract'),
          vitamId: unit['#id'],
          parents: parentNode ? [parentNode] : [],
          checked: false
          // OriginatingAgencyArchiveUnitIdentifier: [unit.OriginatingAgencyArchiveUnitIdentifier]
        };
        outNode.children = this.getNestedChildren(arr, idPrefix, outNode);
        out.push(outNode);
      }
    });
    return out.sort(byTitle(this.locale));
  }

}

function idExists(units: Unit[], id: string): boolean {
  return !!units.find((unit) => unit['#id'] === id);
}

function byTitle(locale: string): (a: Node, b: Node) => number {
  return (a, b) => {
    if (!a || !b || !a.label || !b.label) {
      return 0;
    }
    return a.label.localeCompare(b.label, locale);
  };
}

/** Required at least one node in included */
export function oneIncludedNodeRequired(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const nodes: { included: string[], excluded: string[] } = control.value;
    if (!nodes) {
      return {missingNodes: {value: 'nodes required'}}
    }
    if (!nodes.included || nodes.included.length < 1) {
      return {missingIncludedNodes: {value: 'included nodes required'}}
    }
    return null;
  };
}
