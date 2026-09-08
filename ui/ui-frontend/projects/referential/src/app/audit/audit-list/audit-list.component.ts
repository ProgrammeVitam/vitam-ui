/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { Component, EventEmitter, inject, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { merge, Subject, Subscription, timer } from 'rxjs';
import { debounceTime, switchMap } from 'rxjs/operators';
import { DEFAULT_PAGE_SIZE, Direction, Event, InfiniteScrollTable, PageRequest } from 'vitamui-library';
import { AUDIT_CATEGORY_FILTER_EV_TYPE, AuditCategoryFilter, AuditChainType, AuditOperation } from '../../models/audit.interface';
import { AuditService } from '../audit.service';

const FILTER_DEBOUNCE_TIME_MS = 400;
const POLLING_INTERVAL_MS = 5000;

// The operation's own evDetData (audit.parsedData.type) carries the actual chain type, as stored
// by the chainAudit workflow.
const CHAIN_AUDIT_TYPE_BY_CATEGORY: Partial<Record<AuditCategoryFilter, AuditChainType>> = {
  [AuditCategoryFilter.TRACEABILITY_CHAIN_AUDIT_UNIT]: AuditChainType.UNIT,
  [AuditCategoryFilter.TRACEABILITY_CHAIN_AUDIT_OBJECTGROUP]: AuditChainType.OBJECT_GROUP,
  [AuditCategoryFilter.TRACEABILITY_CHAIN_AUDIT_LOGBOOK_OPERATION]: AuditChainType.LOGBOOK_OPERATION,
};

export class AuditFilters {
  startDate: string;
  endDate: string;
  types: AuditCategoryFilter[];
}

@Component({
  selector: 'app-audit-list',
  templateUrl: './audit-list.component.html',
  styleUrls: ['./audit-list.component.scss'],
  standalone: false,
})
export class AuditListComponent extends InfiniteScrollTable<any> implements OnDestroy, OnInit {
  auditService: AuditService;

  // eslint-disable-next-line @angular-eslint/no-input-rename
  @Input('search') set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  private _searchText: string;

  @Input() set filters(filters: AuditFilters) {
    this._filters = filters;
    this.filterChange.next(filters);
  }

  private _filters: AuditFilters;

  @Output() auditClick = new EventEmitter<any>();

  public loaded = false;
  public orderBy = 'evDateTime';
  public direction = Direction.DESCENDANT;
  public filterMap: { [key: string]: any[] } = { type: null };

  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<void>();
  private readonly filterChange = new Subject<any>();
  private pollingSubscription: Subscription;

  constructor() {
    const auditService = inject(AuditService);

    super(auditService);

    this.auditService = auditService;
  }

  ngOnInit() {
    this.auditService
      .search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(this.buildCriteriaFromSearch())))
      .subscribe((data: any[]) => {
        this.dataSource = data;
        this.startPolling();
      });

    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildCriteriaFromSearch();
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
      this.restartPolling();
    });
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
    this.stopPolling();
  }

  searchAuditOrdered() {
    const query: any = this.buildCriteriaFromSearch();
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query)));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  auditStatus(audit: any): string {
    return audit.events !== undefined && audit.events.length !== 0 ? audit.events[audit.events.length - 1].outcome : audit.outcome;
  }

  auditMessage(audit: any): string {
    return audit.events !== undefined && audit.events.length !== 0 ? audit.events[audit.events.length - 1].outMessage : audit.outMessage;
  }

  public getOperationCategories(): string[] {
    return Object.keys(AuditCategoryFilter);
  }

  public onFilterCategoryChange(values: AuditCategoryFilter[]): void {
    this._filters.types = values;
    this.filterChange.next(this.filterMap);
  }

  /**
   * The 3 chain audit filter entries share the same evType server-side, but the operation's
   * evDetData (audit.parsedData.type) does carry the actual chain type when populated, so this
   * narrows the already evType-filtered rows down further. Rows without that data (e.g. audits
   * launched before this data was tracked) are kept rather than silently hidden.
   */
  public get filteredDataSource(): any[] {
    if (!this.dataSource) {
      return this.dataSource;
    }

    const selectedChainCategories = (this._filters?.types || []).filter((category) => CHAIN_AUDIT_TYPE_BY_CATEGORY[category] !== undefined);

    if (selectedChainCategories.length === 0 || selectedChainCategories.length === 3) {
      return this.dataSource;
    }

    const wantedChainTypes = new Set(selectedChainCategories.map((category) => CHAIN_AUDIT_TYPE_BY_CATEGORY[category]));

    return this.dataSource.filter((item) => {
      if (item.type !== AuditOperation.TRACEABILITY_CHAIN_AUDIT) {
        return true;
      }
      const chainType = item.parsedData?.type;
      return !chainType || wantedChainTypes.has(chainType);
    });
  }

  private buildCriteriaFromSearch() {
    const criteria: any = {};
    criteria.evTypeProc = 'AUDIT';
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
        criteria.evType = this.toEvTypes(this._filters.types);
      }
    }

    // Default type filter used to exclude the other types
    if (!criteria.evType) {
      criteria.evType = Object.values(AuditOperation);
    }

    return criteria;
  }

  private toEvTypes(categories: AuditCategoryFilter[]): string[] {
    return Array.from(new Set(categories.map((category) => AUDIT_CATEGORY_FILTER_EV_TYPE[category])));
  }

  private restartPolling(): void {
    this.stopPolling();
    this.startPolling();
  }

  private stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
    }
  }

  private startPolling(): void {
    this.pollingSubscription = timer(POLLING_INTERVAL_MS)
      .pipe(
        switchMap(() => {
          const query: any = this.buildCriteriaFromSearch();
          const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
          return this.auditService.search(pageRequest);
        }),
      )
      .subscribe((data: Event[]) => {
        this.updateDataSource(data);
      });
  }

  private updateDataSource(newData: any[]): void {
    if (!this.dataSource || this.dataSource.length === 0) {
      this.dataSource = newData;
      return;
    }
    newData.forEach((newItem: Event) => {
      const existingItemIndex = this.dataSource.findIndex((item) => item.id === newItem.id);
      if (existingItemIndex !== -1) {
        const existingItem = this.dataSource[existingItemIndex];
        const newStatus = this.auditMessage(newItem);
        const oldStatus = this.auditMessage(existingItem);

        if (newStatus !== oldStatus) {
          this.dataSource[existingItemIndex] = { ...existingItem, ...newItem };
          this.auditClick.next(newItem);
        }
      } else {
        this.dataSource.unshift(newItem);
      }
    });
  }
}
