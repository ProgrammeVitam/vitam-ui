import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import { merge, Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, PageRequest } from 'ui-frontend-common';

import { ProbativeValueService } from '../probative-value.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

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

  loaded = false;

  orderBy = '#id';
  direction = Direction.ASCENDANT;

  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();

  @Output() probativeValueClick = new EventEmitter<any>();

  constructor(
    public probativeValueService: ProbativeValueService
  ) {
    super(probativeValueService);
  }

  ngOnInit() {
    this.probativeValueService.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT, JSON.stringify(this.buildProbativeVlaueCriteriaFromSearch())))
      .subscribe((data: any[]) => {
        this.dataSource = data;
      });

    const searchCriteriaChange = merge(this.searchChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildProbativeVlaueCriteriaFromSearch();
      console.log('query: ', query);
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
  }

  buildProbativeVlaueCriteriaFromSearch() {
    const criteria: any = {};
    criteria.evTypeProc = 'AUDIT';
    criteria.evType = 'EXPORT_PROBATIVE_VALUE';
    if (this._searchText != undefined && this._searchText.length > 0) {
      criteria['#id'] = this._searchText;
    }
    return criteria;
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
  }

  searchProbativeVlaueOrdered() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  probativeVlaueStatus(probativeVlaue: any): string {
    return (probativeVlaue.events != undefined && probativeVlaue.events.length != 0) ? probativeVlaue.events[probativeVlaue.events.length - 1].outcome : probativeVlaue.outcome;
  }

  probativeVlaueMessage(probativeVlaue: any): string {
    return (probativeVlaue.events != undefined && probativeVlaue.events.length != 0) ? probativeVlaue.events[probativeVlaue.events.length - 1].outMessage : probativeVlaue.outMessage;
  }
}
