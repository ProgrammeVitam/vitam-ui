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
  // tslint:disable-next-line:no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  // tslint:disable-next-line:variable-name
  private _searchText: string;

  @Input('filters')
  set filters(filters: TraceabilityFilter) {
    console.log('Filters: ', filters);
    this._filters = filters;
    this.filterChange.next(filters);
  }

  // tslint:disable-next-line:variable-name
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
