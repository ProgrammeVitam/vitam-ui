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
import { DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, PageRequest } from 'ui-frontend-common';

import { IngestService } from '../ingest.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

export class IngestFilters {
  startDate: string;
  endDate: string;
}

@Component({
  selector: 'app-ingest-list',
  templateUrl: './ingest-list.component.html',
  styleUrls: ['./ingest-list.component.scss']
})
export class IngestListComponent extends InfiniteScrollTable<any> implements OnDestroy, OnInit {
  // tslint:disable-next-line:no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }
  // tslint:disable-next-line:variable-name
  private _searchText: string;

  @Input('filters')
  set filters(filters: IngestFilters) {
    this._filters = filters;
    this.filterChange.next(filters);
  }
  // tslint:disable-next-line:variable-name
  private _filters: IngestFilters;

  @Output() ingestClick = new EventEmitter<any>();

  loaded = false;

  orderBy = 'events.evDateTime';
  direction = Direction.ASCENDANT;

  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();
  private readonly filterChange = new Subject<any>();

  constructor(public ingestService: IngestService) {
    super(ingestService);
  }

  ngOnInit() {
    this.ingestService.search(
      new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.DESCENDANT,
        JSON.stringify(this.buildIngestCriteriaFromSearch()))
    ).subscribe((data: any[]) => {
      data.map((element: any) => {
        if (element.evDetData && element.evDetData.length >= 2) {
          element.evDetData = JSON.parse(element.evDetData);
        }
        if (element.agIdExt && element.agIdExt.length >= 2) {
          element.agIdExt = JSON.parse(element.agIdExt);
        }
        if (element.rightsStatementIdentifier && element.rightsStatementIdentifier.length >= 2) {
          element.rightsStatementIdentifier = JSON.parse(element.rightsStatementIdentifier);
        }
      });
      this.dataSource = data;
    });

    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildIngestCriteriaFromSearch();
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
  }

  buildIngestCriteriaFromSearch() {
    const criteria: any = {};
    criteria.evTypeProc = 'INGEST';
    if (this._searchText !== undefined && this._searchText.length > 0) {
      criteria['#id'] = this._searchText;
      criteria.obIdIn = this._searchText;
      criteria['agIdExt.TransferringAgency'] = this._searchText;
      criteria['agIdExt.originatingAgency'] = this._searchText;
      criteria['agIdExt.ArchivalAgreement'] = this._searchText;
      criteria['evDetData.EvDetailReq'] = this._searchText;
    }

    if (this._filters) {
      if (this._filters.startDate) {
        criteria.evDateTime_Start = this._filters.startDate;
      }
      if (this._filters.endDate) {
        const date = new Date(this._filters.endDate);
        date.setDate(date.getDate() + 1);
        criteria.evDateTime_End = date;
      }
    }

    return criteria;
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
  }

  searchIngestOrdered() {
    const query: any = this.buildIngestCriteriaFromSearch();
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT, JSON.stringify(query)));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  getOperationStatus(ingest: any): string {
    const eventsLength = ingest.events.length;
    if (eventsLength > 0) {
      if (ingest.evType === ingest.events[eventsLength - 1].evType) {
        return ingest.events[eventsLength - 1].outcome;
      } else {
        return 'En cours';
      }
    }
  }

  ingestStatus(ingest: any): string {
    if (this.getOperationStatus(ingest) === 'En cours') {
      return 'En cours';
    } else {
      return (ingest.events !== undefined && ingest.events.length !== 0) ?
        ingest.events[ingest.events.length - 1].outcome : ingest.outcome;
    }
  }

  ingestEndDate(ingest: any): string {
    return (ingest.events !== undefined && ingest.events.length !== 0) ? ingest.events[ingest.events.length - 1].evDateTime : ingest.evDateTime;
  }
}
