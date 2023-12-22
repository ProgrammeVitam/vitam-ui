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
import {HttpClient, HttpHeaders, HttpParams, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {tap} from 'rxjs/operators';
import {Agency,DownloadUtils,SearchService, VitamUISnackBarService} from 'ui-frontend-common';


import {AgencyApiService} from '../core/api/agency-api.service';

@Injectable({
  providedIn: 'root',
})
export class AgencyService extends SearchService<Agency> {
  updated = new Subject<Agency>();

  constructor(private agencyApiService: AgencyApiService, private snackBarService: VitamUISnackBarService, http: HttpClient) {
    super(http, agencyApiService, 'ALL');
  }

  get(id: string): Observable<Agency> {
    return this.agencyApiService.getOne(encodeURI(id));
  }

  getAll(): Observable<Agency[]> {
    const params = new HttpParams().set('embedded', 'ALL');
    return this.agencyApiService.getAllByParams(params);
  }

  existsProperties(properties: { name?: string; identifier?: string }): Observable<any> {
    const existAgency: any = {};
    if (properties.name) {
      existAgency.name = properties.name;
    }
    if (properties.identifier) {
      existAgency.identifier = properties.identifier;
    }

    const agency = existAgency as Agency;
    return this.agencyApiService.check(agency, this.headers);
  }

  create(agency: Agency) {
    return this.agencyApiService.create(agency, this.headers).pipe(
      tap(
        () => {
          this.snackBarService.open({
            message: 'SNACKBAR.AGENCY_CONTRACT_CREATED',
            icon: 'vitamui-icon-agent',
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        }
      )
    );
  }

  patch(data: { id: string; [key: string]: any }): Observable<Agency> {
    return this.agencyApiService.patch(data).pipe(
      tap((response) => this.updated.next(response)),
      tap(
        () => {
          this.snackBarService.open({
            message: 'SNACKBAR.AGENCY_CONTRACT_UPDATED',
            icon: 'vitamui-icon-agent',
          });
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        }
      )
    );
  }

  delete(agency: Agency): Observable<any> {
    return this.agencyApiService.delete(agency.id).pipe(
      tap(
        (response) => {
          if (response === false) {
            this.snackBarService.open({
              message: 'SNACKBAR.AGENCY_CONTRACT_DELETE_ERROR',
              icon: 'vitamui-icon-agent',
            });
          } else {
            this.snackBarService.open({
              message: 'SNACKBAR.AGENCY_CONTRACT_DELETED',
              icon: 'vitamui-icon-agent',
            });
          }
        },
        (error) => {
          this.snackBarService.open({ message: error.error.message, translate: false });
        }
      )
    );
  }

  export() {
    this.snackBarService.open({
      message: 'SNACKBAR.AGENCY_CONTRACT_EXPORT_ALL',
      icon: 'vitamui-icon-agent',
    });

    this.agencyApiService.export().subscribe(
      (response: HttpResponse<Blob>) => {
        DownloadUtils.loadFromBlob(response, response.body.type, 'agencies.csv');
      },
      (error) => {
        this.snackBarService.open({ message: error.error.message, translate: false });
      }
    );
  }

  setTenantId(tenantIdentifier: number) {
    this.headers = new HttpHeaders({ 'X-Tenant-Id': tenantIdentifier.toString() });
  }
}
