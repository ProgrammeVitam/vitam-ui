import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {merge, Subject} from 'rxjs';
import {debounceTime} from 'rxjs/operators';
import {DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, PageRequest} from 'ui-frontend-common';

import {ProbativeValueService} from '../probative-value.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

export class ProbativeValueFilters {
  startDate: string;
  endDate: string;
}

@Component({
  selector: 'app-probative-value-list',
  templateUrl: './probative-value-list.component.html',
  styleUrls: ['./probative-value-list.component.scss']
})
export class ProbativeValueListComponent extends InfiniteScrollTable<any> implements OnDestroy, OnInit {
  // tslint:disable-next-line:no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  // tslint:disable-next-line:variable-name
  private _searchText: string;

  @Input('filters')
  set filters(filters: ProbativeValueFilters) {
    this._filters = filters;
    this.filterChange.next(filters);
  }

  // tslint:disable-next-line:variable-name
  private _filters: ProbativeValueFilters;

  loaded = false;

  orderBy = '#id';
  direction = Direction.ASCENDANT;

  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();
  private readonly filterChange = new Subject<any>();

  @Output() probativeValueClick = new EventEmitter<any>();

  constructor(
    public probativeValueService: ProbativeValueService
  ) {
    super(probativeValueService);
  }

  ngOnInit() {
    this.probativeValueService.search(
      new PageRequest(
        0,
        DEFAULT_PAGE_SIZE,
        this.orderBy,
        Direction.ASCENDANT,
        JSON.stringify(this.buildProbativeValueCriteriaFromSearch())))
      .subscribe((data: any[]) => {
        this.dataSource = data;
      });

    const searchCriteriaChange = merge(this.searchChange, this.orderChange, this.filterChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildProbativeValueCriteriaFromSearch();
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
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

  ngOnDestroy() {
    this.updatedData.unsubscribe();
  }

  searchProbativeValueOrdered() {
    const query: any = this.buildProbativeValueCriteriaFromSearch();
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT, JSON.stringify(query)));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  probativeValueStatus(probativeValue: any): string {
    return (probativeValue.events !== undefined && probativeValue.events.length !== 0) ?
      probativeValue.events[probativeValue.events.length - 1].outcome :
      probativeValue.outcome;
  }

  probativeValueMessage(probativeValue: any): string {
    return (probativeValue.events !== undefined && probativeValue.events.length !== 0) ?
      probativeValue.events[probativeValue.events.length - 1].outMessage :
      probativeValue.outMessage;
  }
}
