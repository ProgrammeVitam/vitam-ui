/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */

import { HttpHeaders } from '@angular/common/http';
import { Inject, Injectable, LOCALE_ID } from '@angular/core';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { catchError, map, shareReplay, tap } from 'rxjs/operators';
import { FileType, UnitType } from '../../../app/modules';
import { Unit } from '../../../app/modules/models/units/unit.interface';
import { SearchUnitApiService } from '../../api/search-unit-api.service';
import { DescriptionLevel } from '../../models/description-level.enum';
import { Node } from '../../models/node.interface';

import { getKeywordValue } from '../../utils/keyword.util';

export enum ExpandLevel {
  NONE,
  ROOT_ONLY,
  ALL,
}

export enum FilingPlanMode {
  SOLO,
  INCLUDE_ONLY,
  BOTH,
}

@Injectable({
  providedIn: 'root',
})
export class FilingPlanService {
  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private _pending = 0;

  constructor(
    private searchUnitApi: SearchUnitApiService,
    @Inject(LOCALE_ID) private locale: string,
  ) {}

  get pending(): boolean {
    return this._pending > 0;
  }

  public loadTree(tenantIdentifier: number, accessContractId: string, idPrefix: string): Observable<Node[]> {
    this._pending++;
    const headers = new HttpHeaders({
      'X-Tenant-Id': tenantIdentifier.toString(),
      'X-Access-Contract-Id': accessContractId,
    });
    const units$ = this.searchUnitApi.getFilingPlan(headers).pipe(
      catchError(() => {
        return of({ $hits: null, $results: [] });
      }),
      map((response) => response.$results),
      tap(() => this._pending--),
      shareReplay(1),
    );
    return units$.pipe(
      map((results) => {
        return this.getNestedChildren(results, idPrefix);
      }),
    );
  }

  private getFileTypeFromUnit(unit: Unit): FileType {
    return this.getFileType(unit['#unitType'], unit.DescriptionLevel);
  }

  private getFileType(unitType: UnitType, descriptionLevel: DescriptionLevel) {
    // TODO file type for documents
    if (descriptionLevel === DescriptionLevel.FILE) {
      if (unitType === UnitType.HOLDING_UNIT) {
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
          checked: false,
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
    const nodes: { included: string[]; excluded: string[] } = control.value;
    if (!nodes) {
      return { missingNodes: { value: 'nodes required' } };
    }
    if (!nodes.included || nodes.included.length < 1) {
      return { missingIncludedNodes: { value: 'included nodes required' } };
    }
    return null;
  };
}
