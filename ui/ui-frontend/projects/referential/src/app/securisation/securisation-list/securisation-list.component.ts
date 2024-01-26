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
import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {merge, Subject} from 'rxjs';
import {debounceTime} from 'rxjs/operators';
import {DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, PageRequest} from 'ui-frontend-common';

import {SecurisationService} from '../securisation.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

export class TraceabilityFilter {
  startDate: string;
  endDate: string;
  types: string[];
}

@Component({
  selector: 'app-securisation-list',
  templateUrl: './securisation-list.component.html',
  styleUrls: ['./securisation-list.component.scss']
})
export class SecurisationListComponent extends InfiniteScrollTable<any> implements OnDestroy, OnInit {
  // eslint-disable-next-line @angular-eslint/no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  // eslint-disable-next-line @typescript-eslint/naming-convention,no-underscore-dangle,id-blacklist,id-match
  private _searchText: string;

  @Input('filters')
  set filters(filters: TraceabilityFilter) {
    console.log('Filters: ', filters);
    this._filters = filters;
    this.filterChange.next(filters);
  }

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private _filters: TraceabilityFilter;

  @Output() securisationClick = new EventEmitter<any>();

  loaded = false;

  orderBy = 'evDateTime';
  direction = Direction.DESCENDANT;

  private readonly filterChange = new Subject<TraceabilityFilter>();
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();

  constructor(
    public securisationService: SecurisationService
  ) {
    super(securisationService);
  }

  ngOnInit() {

    this.securisationService.search(
      new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(this.buildSecurisationCriteriaFromSearch())))
      .subscribe((data: any[]) => {
        this.dataSource = data;
      });

    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildSecurisationCriteriaFromSearch();
      console.log('query: ', query);
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
  }

  buildSecurisationCriteriaFromSearch() {
    const criteria: any = {};
    criteria.evTypeProc = 'TRACEABILITY';
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

      if (this._filters.types && this._filters.types.length > 0) {
        criteria.evType = this._filters.types;
      }
    }

    return criteria;
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
  }

  searchSecurisationOrdered() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.DESCENDANT));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  securisationStatus(securisation: any): string {
    return (securisation.events !== undefined && securisation.events.length !== 0) ?
      securisation.events[securisation.events.length - 1].outcome :
      securisation.outcome;
  }

  securisationMessage(securisation: any): string {
    return (securisation.events !== undefined && securisation.events.length !== 0) ?
      securisation.events[securisation.events.length - 1].outMessage :
      securisation.outMessage;
  }
}
