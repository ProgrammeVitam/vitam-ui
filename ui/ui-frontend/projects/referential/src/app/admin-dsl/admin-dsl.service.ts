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
import {HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {SearchUnitApiService} from 'projects/vitamui-library/src/public-api';
import {of} from 'rxjs';
import {catchError} from 'rxjs/operators';


@Injectable({
  providedIn: 'root'
})
export class AdminDslService {

  constructor(
    private unitApiService: SearchUnitApiService) {
  }

  getById(unitId: string, accessContractId: string) {
    const headers = new HttpHeaders()
      .append('X-Access-Contract-Id', accessContractId)
      .append('Content-Type', 'application/stream');
    return this.unitApiService.getById(unitId, headers);
  }

  getByDsl(unitId: string, dsl: any, accessContractId: string) {
    let headers = new HttpHeaders()
      .append('Content-Type', 'application/json')
      .append('X-Access-Contract-Id', accessContractId);

    // We don't want to display the error popup launched in the http interceptor
    // if the unit id is unknown
    if (unitId) {
      headers = headers.append('X-By-Passed-Error', '500');
    }
    return this.unitApiService.getByDsl(unitId, dsl, headers).pipe(
      catchError((httpErrorResponse: HttpErrorResponse) => {
        return of(httpErrorResponse.error);
      })
    );
  }

  getUnitObjectsByDsl(unitId: string, dsl: any, accessContractId: string) {
    const headers = new HttpHeaders()
      .append('Content-Type', 'application/json')
      .append('X-Access-Contract-Id', accessContractId)
      .append('X-By-Passed-Error', '500');

    return this.unitApiService.getUnitObjectsByDsl(unitId, dsl, headers).pipe(
      catchError((httpErrorResponse: HttpErrorResponse) => {
        return of(httpErrorResponse.error);
      })
    );
  }
}
