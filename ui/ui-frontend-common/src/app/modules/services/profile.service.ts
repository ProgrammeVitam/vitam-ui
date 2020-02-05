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

import { Observable } from 'rxjs';

import { HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { map } from 'rxjs/operators';
import { ProfileApiService } from '../api/profile-api.service';
import { Criterion, Profile, SearchQuery } from '../models';
import { Operators } from '../vitamui-table';

@Injectable({
  providedIn: 'root'
})
export class ProfileService {

  constructor(private profileApi: ProfileApiService) { }

  list(level?: string, tenantIdentifier?: number, applicationNameExclude?: string []): Observable<Profile[]> {

    let httpHeaders = new HttpHeaders();
    const criterionArray: Criterion[] = [];
    const enabledCriterion: Criterion = { key: 'enabled', value: true, operator: Operators.equals };
    criterionArray.push(enabledCriterion);

    if (level || level === '') {
      const levelCriterion: Criterion = { key: 'level', value: level, operator: Operators.equals };
      criterionArray.push(levelCriterion);
    }

    if (tenantIdentifier) {
      httpHeaders = httpHeaders.set('X-Tenant-Id', tenantIdentifier.toString());
      const criterionTenantIdentifier: Criterion = { key: 'tenantIdentifier', value: tenantIdentifier, operator: Operators.equals };
      criterionArray.push(criterionTenantIdentifier);
    }

    if (applicationNameExclude && applicationNameExclude.length) {
      const criterion = { key: 'applicationName', value: applicationNameExclude, operator: Operators.notin };
      criterionArray.push(criterion);
    }
    const query: SearchQuery = { criteria: criterionArray };
    const params = new HttpParams().set('criteria', JSON.stringify(query)).set('embedded', 'ALL');

    return this.profileApi.getAllByParams(params, httpHeaders);
  }

  getLevelsNoEmpty(query?: SearchQuery): Observable<string[]> {
    return this.profileApi.getLevels(query)
      .pipe(
        map((levels) => levels.filter((l) => !!l))
      );
  }
}
