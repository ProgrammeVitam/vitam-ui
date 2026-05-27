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

import { Injectable, LOCALE_ID, inject } from '@angular/core';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Observable, of, switchMap } from 'rxjs';
import { catchError, map, shareReplay, tap } from 'rxjs/operators';
import { AccessContractService } from '../../../app/modules/services/access-contract.service';
import { FileType } from '../../models/file-type.enum';
import { Unit } from '../../../app/modules/models/units/unit.interface';
import { UnitType } from '../../../app/modules/models/units/unit-type.enum';
import { VitamuiHttpHeaders } from '../../../app/modules/vitamui-http-headers.enum';
import { SearchUnitApiService } from '../../api/search-unit-api.service';
import { DescriptionLevel } from '../../models/description-level.enum';
import { Node } from '../../models/node.interface';

import { getKeywordValue } from '../../utils/keyword.util';
import { HttpHeaders } from '@angular/common/http';

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
  private searchUnitApi = inject(SearchUnitApiService);
  private accessContractService = inject(AccessContractService);
  private locale = inject(LOCALE_ID);

  private _pending = 0;

  get pending(): boolean {
    return this._pending > 0;
  }

  public loadTreeFromDataSource(units: Unit[], idPrefix: string): Node[] {
    return this.getNestedChildren(units, idPrefix);
  }

  public loadFilingPlan(): Observable<Unit[]> {
    return this.accessContractService.currentAccessContractId$.pipe(
      map((accessContractId) => new HttpHeaders().set(VitamuiHttpHeaders.X_ACCESS_CONTRACT_ID, accessContractId)),
      switchMap((headers) => this.searchUnitApi.getFilingPlan(headers)),
      catchError(() => of({ $hits: null, $results: [] })),
      map((response) => response.$results),
      shareReplay(1),
    );
  }

  public loadTree(idPrefix: string): Observable<Node[]> {
    this._pending++;
    return this.loadFilingPlan().pipe(
      map((results) => this.getNestedChildren(results, idPrefix)),
      tap(() => this._pending--),
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
          label: fetchTitle(unit.Title, unit.Title_),
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

export function fetchTitle(title: string, titleInLanguages: any) {
  return title ? title : titleInLanguages ? (titleInLanguages.fr ? titleInLanguages.fr : titleInLanguages.en) : titleInLanguages.en;
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
