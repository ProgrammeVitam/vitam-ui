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
import { HttpClient, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BASE_URL, BaseHttpClient, PageRequest, PaginatedResponse } from 'vitamui-library';
import { Profile } from '../../models/profile';
import { PastisConfiguration } from '../classes/pastis-configuration';
import { SKIP_ERROR_NOTIFICATION } from 'vitamui-library';

const HTTP_STATUS_OK = 200;

@Injectable({
  providedIn: 'root',
})
export class ArchiveProfileApiService extends BaseHttpClient<Profile> {
  // @ts-ignore
  constructor(
    http: HttpClient,
    @Inject(BASE_URL) baseUrl: string,
    private pastisConfig: PastisConfiguration,
  ) {
    // console.log('passage dans service archive API');
    super(http, baseUrl);
  }

  getAllByParams(params: HttpParams, headers?: HttpHeaders) {
    return super.getAllByParams(params, headers);
  }

  getAllPaginated(pageRequest: PageRequest, embedded?: string, headers?: HttpHeaders): Observable<PaginatedResponse<Profile>> {
    return super.getAllPaginated(pageRequest, embedded, headers);
  }

  getOne(id: string, headers?: HttpHeaders): Observable<Profile> {
    return super.getOne(id, headers);
  }

  download(id: string, headers?: HttpHeaders): Observable<Blob> {
    return super.getHttp().get(super.getApiUrl() + this.pastisConfig.downloadProfile + '/' + id, { responseType: 'blob', headers });
  }

  uploadProfileArchivageFile(id: string, profile: FormData, headers: HttpHeaders = new HttpHeaders()): Observable<any> {
    const allHeaders = headers.set(SKIP_ERROR_NOTIFICATION, '1');
    return super.getHttp().put(this.apiUrl + this.pastisConfig.importProfileInExistingNotice + '/' + id, profile, {
      responseType: 'json',
      headers: allHeaders,
    });
  }

  updateProfilePa(profile: Profile, headers?: HttpHeaders): Observable<Profile> {
    profile.path = null;
    return this.http.put<Profile>(this.apiUrl + this.pastisConfig.archiveProfileApiPath + '/' + profile.id, profile, { headers });
  }

  patch(partialAgency: { id: string; [key: string]: any }, headers?: HttpHeaders) {
    return super.patch(partialAgency, headers);
  }

  create(profile: Profile, headers?: HttpHeaders): Observable<Profile> {
    return this.http.post<Profile>(this.apiUrl + this.pastisConfig.archiveProfileApiPath + '/', profile, { headers });
  }

  check(profile: Profile, headers?: HttpHeaders): Observable<boolean> {
    return super
      .getHttp()
      .post<any>(super.getApiUrl() + this.pastisConfig.archiveProfileApiPath + '/check', profile, {
        observe: 'response',
        headers,
      })
      .pipe(map((response: HttpResponse<void>) => response.status === HTTP_STATUS_OK));
  }

  delete(id: string, headers?: HttpHeaders) {
    return super.getHttp().delete(super.getApiUrl() + '/' + id, { headers });
  }
}
