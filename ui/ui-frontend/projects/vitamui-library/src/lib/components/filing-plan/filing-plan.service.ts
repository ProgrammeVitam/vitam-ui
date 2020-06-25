import {Observable, of} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';

import {HttpHeaders} from '@angular/common/http';
import {Inject, Injectable, LOCALE_ID} from '@angular/core';

import {SearchUnitApiService} from '../../api/search-unit-api.service';

import {DescriptionLevel} from '../../models/description-level.enum';
import {FileType} from '../../models/file-type.enum';
import {Node} from '../../models/node.interface';
import {Unit} from '../../models/unit.interface';

import {getKeywordValue} from '../../utils/keyword.util';

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

  public loadTree(tenantIdentifier: number, accessContractId: string, idPrefix: string): Observable<Node[]> {
    this._pending++;
    const headers = new HttpHeaders({
      'X-Tenant-Id': tenantIdentifier.toString(),
      'X-Access-Contract-Id': accessContractId
    });

    return this.searchUnitApi.getFilingPlan(headers).pipe(
      catchError(() => {
        return of({$hits: null, $results: []});
      }),
      map((response) => response.$results),
      tap(() => {
        this._pending--;
      }),
      map(results => this.getNestedChildren(results, idPrefix))
    );
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
        (parentNode && parentNode.vitamId && unit['#unitups'] && unit['#unitups'][0] === parentNode.vitamId) ||
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

        outNode.children = this.getNestedChildren(arr, idPrefix, outNode).sort(byTitle(this.locale));

        out.push(outNode);
      }
    });

    return out;
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
