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

import { DEFAULT_PAGE_SIZE, Direction, IEvent, InfiniteScrollTable, PageRequest } from 'vitamui-library';

import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  Output,
  SimpleChanges,
  TemplateRef,
  ViewChild,
} from '@angular/core';

import { merge, Subject, Subscription } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { EventFilter } from '../event-filter.interface';
import { LogbookDownloadService } from '../logbook-download.service';
import { LogbookOperation } from '../logbook-operation.enum';
import { LogbookSearchService } from '../logbook-search.service';

const FILTER_DEBOUNCE_TIME_MS = 400;
const ARCHIVE_TRANSFER = 'ARCHIVE_TRANSFER';
const ARCHIVE_TRANSFER_LABEL = 'ARCHIVE_TRANSFER_LABEL';

@Component({
  selector: 'app-logbook-operation-list',
  templateUrl: './logbook-operation-list.component.html',
  styleUrls: ['./logbook-operation-list.component.scss'],
})
export class LogbookOperationListComponent extends InfiniteScrollTable<IEvent> implements OnInit, OnChanges, OnDestroy {
  @Input() tenantIdentifier: number;

  @Input('search') set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  @Input('filters') set searchFilters(searchFilters: Readonly<EventFilter>) {
    this._searchFilters = searchFilters;
    this.searchFiltersChange.next(searchFilters);
  }

  @Output() eventClick = new EventEmitter<IEvent>();

  @ViewChild('filterTemplate') filterTemplate: TemplateRef<LogbookOperationListComponent>;
  @ViewChild('filterButton') filterButton: ElementRef;

  public orderByDate = false;
  public filterMap: { [key: string]: any[] } = { operationCategories: null };
  public orderDirection = Direction.ASCENDANT;
  public readonly orderChange = new Subject<void>();
  public readonly LOGBOOK_OPERATION = LogbookOperation;
  public readonly DIRECTION = Direction;

  private readonly searchFiltersChange = new Subject<Readonly<EventFilter>>();
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();
  private _searchText: string;
  private _searchFilters: Readonly<EventFilter>;
  private logbookOperationsSubscription: Subscription;

  constructor(
    public logbookSearchService: LogbookSearchService,
    private logbookDownloadService: LogbookDownloadService,
  ) {
    super(logbookSearchService);
  }

  ngOnInit() {
    this.updatedData.subscribe(() => this.onDataSourceReloaded());
    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange, this.searchFiltersChange).pipe(
      debounceTime(FILTER_DEBOUNCE_TIME_MS),
    );

    searchCriteriaChange.subscribe(() => this.refreshList());
    this.logbookOperationsSubscription = this.logbookDownloadService.logbookOperationsReloaded.subscribe((logbookOperationsReloaded) =>
      this.updateLogbookOperations(logbookOperationsReloaded),
    );
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.tenantIdentifier || changes.filters) {
      this.refreshList();
    }
  }

  ngOnDestroy(): void {
    this.logbookOperationsSubscription?.unsubscribe();
  }

  public changeOrderByDate(): void {
    this.orderByDate = !this.orderByDate;
    this.orderChange.next();
  }

  public getOperationCategories(): string[] {
    return Object.keys(LogbookOperation);
  }

  public manageOperationLabel(type: string) {
    return type === ARCHIVE_TRANSFER ? ARCHIVE_TRANSFER_LABEL : type;
  }

  public handleClick(event: IEvent) {
    this.eventClick.emit(event);
  }

  public onFilterChange(key: string, values: any[]): void {
    this.filterMap[key] = values;
    this.filterChange.next(this.filterMap);
  }

  public resetFilters(): void {
    this.filterMap.operationCategories = null;
    this.filterChange.next(this.filterMap);
  }

  public refreshList(): void {
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, null, null);
    const query = JSON.stringify(
      LogbookSearchService.buildVitamQuery(pageRequest, this.buildCriteriaFromSearch(), this.orderByDate ? 1 : -1),
    );
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, null, null, query));
  }

  private onDataSourceReloaded() {
    if (this.pending) {
      return;
    }

    this.logbookDownloadService.logbookOperationsReloaded.next(this.dataSource);
  }

  private updateLogbookOperations(logbookOperationsReloaded: IEvent[]) {
    logbookOperationsReloaded.forEach((logbookOperation) => {
      const index = this.dataSource.findIndex((o) => o.id === logbookOperation.id);
      this.dataSource[index] = logbookOperation;
    });
  }

  private buildCriteriaFromSearch() {
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
}
