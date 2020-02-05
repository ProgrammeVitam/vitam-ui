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
import { Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Id, PaginatedApi } from '../';
import { Direction } from './direction.enum';
import { DEFAULT_PAGE_SIZE, PageRequest } from './page-request.model';
import { PaginatedResponse } from './paginated-response.interface';

export class SearchService<T extends Id> {

  protected pageRequest: PageRequest;
  protected hasMore: boolean;
  protected data: T[];

  constructor(
    protected http: HttpClient,
    protected paginatedApi: PaginatedApi<T>,
    protected embedded?: string,
    protected headers?: HttpHeaders
  ) { }

  search(pageRequest: PageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, 'name', Direction.ASCENDANT)): Observable<T[]> {
    this.pageRequest = pageRequest;

    // TODO catch errors
    return this.paginatedApi.getAllPaginated(this.pageRequest, this.embedded, this.headers)
      .pipe(
        map((paginated: PaginatedResponse<T>) => {
          this.data = paginated.values;
          this.pageRequest.page = paginated.pageNum;
          this.hasMore = paginated.hasMore;

          return this.data;
        })
      );
  }

  loadMore(): Observable<T[]> {
    if (!this.hasMore) { return of(this.data); }
    this.pageRequest.page += 1;

    // TODO catch errors
    return this.paginatedApi.getAllPaginated(this.pageRequest, this.embedded, this.headers)
      .pipe(
        map((paginated: PaginatedResponse<T>) => {
          this.data = this.data.concat(paginated.values);
          this.pageRequest.page = paginated.pageNum;
          this.hasMore = paginated.hasMore;

          return this.data;
        })
      );
  }

  get canLoadMore() { return this.hasMore; }

}
