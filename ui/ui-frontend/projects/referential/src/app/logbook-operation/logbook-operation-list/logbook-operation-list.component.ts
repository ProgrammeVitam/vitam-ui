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
import {animate, state, style, transition, trigger} from '@angular/animations';
import {DEFAULT_PAGE_SIZE, Direction, Event, InfiniteScrollTable, PageRequest} from 'ui-frontend-common';

import {
  Component, ElementRef, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, TemplateRef, ViewChild
} from '@angular/core';

import {merge, Subject} from 'rxjs';
import {debounceTime} from 'rxjs/operators';
import {EventFilter} from '../event-filter.interface';
import {LOGBOOK_OPERATION_CATEGORIES} from '../logbook-operation-constants';
import {LogbookSearchService} from '../logbook-search.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-logbook-operation-list',
  templateUrl: './logbook-operation-list.component.html',
  styleUrls: ['./logbook-operation-list.component.scss'],
  animations: [
    trigger('expansion', [
      state('collapsed', style({height: '0px', visibility: 'hidden'})),
      state('expanded', style({height: '*', visibility: 'visible'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),

    trigger('arrow', [
      state('collapsed', style({transform: 'rotate(180deg)'})),
      state('expanded', style({transform: 'none'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),
  ]
})
export class LogbookOperationListComponent extends InfiniteScrollTable<Event> implements OnInit, OnChanges {

  @Input() tenantIdentifier: number;

  // tslint:disable-next-line:no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  // tslint:disable-next-line:no-input-rename
  @Input('filters')
  set searchFilters(searchFilters: Readonly<EventFilter>) {
    this._searchFilters = searchFilters;
    this.searchFiltersChange.next(searchFilters);
  }

  // tslint:disable-next-line:variable-name
  private _searchText: string;
  // tslint:disable-next-line:variable-name
  private _searchFilters: Readonly<EventFilter>;

  @ViewChild('filterTemplate', {static: false}) filterTemplate: TemplateRef<LogbookOperationListComponent>;
  @ViewChild('filterButton', {static: false}) filterButton: ElementRef;

  @Output() eventClick = new EventEmitter<Event>();

  orderBy = 'name';
  direction = Direction.ASCENDANT;

  private readonly searchFiltersChange = new Subject<Readonly<EventFilter>>();
  private readonly filterChange = new Subject<{[key: string]: any[]}>();
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();

  filterMap: {[key: string]: any[]} = {
    operationCategories: null,
  };
  operationCategoriesFilterOptions: Array<{value: string, label: string}> = [];

  constructor(public logbookSearchService: LogbookSearchService) {
    super(logbookSearchService);
  }

  ngOnInit() {

    this.pending = true;
    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange, this.searchFiltersChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      this.refreshList();
    });

    this.refreshOperationCategoriesOptions();
    this.refreshList();

  }

  buildCriteriaFromSearch() {
    const criteria: any = {};
    if (this._searchText !== undefined && this._searchText.length > 0) {
      criteria.evId = this._searchText;
    }

    if (this._searchFilters && this._searchFilters.dateRange) {
      if (this._searchFilters.dateRange.startDate) {
        criteria.evDateTime_Start = this._searchFilters.dateRange.startDate;
      }
      if (this._searchFilters.dateRange.endDate) {
        criteria.evDateTime_End = this._searchFilters.dateRange.endDate;
      }
    }

    if (this.filterMap && this.filterMap.operationCategories) {
      criteria.types = this.filterMap.operationCategories;
    }

    return criteria;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.tenantIdentifier || changes.filters) {
      this.refreshList();
    }
  }

  refreshOperationCategoriesOptions() {
    this.operationCategoriesFilterOptions = LOGBOOK_OPERATION_CATEGORIES.
      map((operationCategory) => ({value: operationCategory.key, label: operationCategory.label}));
  }

  refreshList() {
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction);

    const query = JSON.stringify(LogbookSearchService.buildVitamQuery(pageRequest, this.buildCriteriaFromSearch()));

    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, query));

  }

  onFilterChange(key: string, values: any[]) {
    this.filterMap[key] = values;
    this.filterChange.next(this.filterMap);
  }

  resetFilters() {
    this.filterMap.operationCategories = null;
    this.filterChange.next(this.filterMap);
  }

  selectEvent(event: Event) {
    this.eventClick.emit(event);
  }
}
