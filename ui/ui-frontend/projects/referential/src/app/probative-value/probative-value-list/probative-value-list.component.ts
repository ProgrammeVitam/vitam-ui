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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { merge, Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, PageRequest } from 'vitamui-library';

import { ProbativeValueService } from '../probative-value.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

export class ProbativeValueFilters {
  startDate: string;
  endDate: string;
}

@Component({
  selector: 'app-probative-value-list',
  templateUrl: './probative-value-list.component.html',
  styleUrls: ['./probative-value-list.component.scss'],
})
export class ProbativeValueListComponent extends InfiniteScrollTable<any> implements OnDestroy, OnInit {
  // eslint-disable-next-line @angular-eslint/no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  private _searchText: string;

  @Input()
  set filters(filters: ProbativeValueFilters) {
    this._filters = filters;
    this.filterChange.next(filters);
  }

  private _filters: ProbativeValueFilters;

  loaded = false;

  orderBy = 'evDateTime';
  direction = Direction.ASCENDANT;

  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<void>();
  private readonly filterChange = new Subject<any>();

  @Output() probativeValueClick = new EventEmitter<any>();

  constructor(public probativeValueService: ProbativeValueService) {
    super(probativeValueService);
  }

  ngOnInit() {
    this.probativeValueService
      .search(
        new PageRequest(
          0,
          DEFAULT_PAGE_SIZE,
          this.orderBy,
          Direction.ASCENDANT,
          JSON.stringify(this.buildProbativeValueCriteriaFromSearch()),
        ),
      )
      .subscribe((data: any[]) => (this.dataSource = data));

    const searchCriteriaChange = merge(this.searchChange, this.orderChange, this.filterChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildProbativeValueCriteriaFromSearch();
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
  }

  buildProbativeValueCriteriaFromSearch() {
    const criteria: any = {};
    criteria.evTypeProc = 'AUDIT';
    criteria.evType = 'EXPORT_PROBATIVE_VALUE';
    if (this._searchText !== undefined && this._searchText.length > 0) {
      criteria['#id'] = this._searchText;
    }

    if (this._filters) {
      if (this._filters.startDate) {
        criteria.evDateTime_Start = this._filters.startDate;
      }
      if (this._filters.endDate) {
        criteria.evDateTime_End = this._filters.endDate;
      }
    }

    return criteria;
  }

  searchProbativeValueOrdered() {
    const query: any = this.buildProbativeValueCriteriaFromSearch();
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT, JSON.stringify(query)));
  }

  emitOrderChange() {
    this.orderChange.next();
  }
}
