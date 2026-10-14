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
import { Component, inject, input, OnChanges, OnDestroy, OnInit, output, SimpleChanges } from '@angular/core';
import { merge, Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import {
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollDirective,
  InfiniteScrollTable,
  IngestStatus,
  PageRequest,
  OrderByButtonComponent,
  PipesModule,
} from 'vitamui-library';
import type { LogbookOperation } from '../../models/logbook-event.interface';
import { ingestStatus, ingestStatusVisualColor } from '../../models/logbook-event.interface';
import { IngestService } from '../ingest.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { TranslatePipe } from '@ngx-translate/core';
import { CommonModule } from '@angular/common';

const FILTER_DEBOUNCE_TIME_MS = 400;

export class IngestFilters {
  startDate?: Date;
  endDate?: Date;
}

@Component({
  selector: 'app-ingest-list',
  templateUrl: './ingest-list.component.html',
  styleUrls: ['./ingest-list.component.scss'],
  imports: [OrderByButtonComponent, MatProgressSpinner, PipesModule, TranslatePipe, CommonModule, InfiniteScrollDirective],
})
export class IngestListComponent extends InfiniteScrollTable<any> implements OnChanges, OnDestroy, OnInit {
  ingestService: IngestService;

  IngestStatus = IngestStatus;

  readonly searchText = input<string>();

  readonly filters = input<IngestFilters>();

  readonly ingestThatHasChanged = input<LogbookOperation>();

  readonly ingestClick = output<any>();

  loaded = false;

  orderBy = 'events.evDateTime';
  direction = Direction.ASCENDANT;

  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<void>();
  private readonly filterChange = new Subject<any>();

  constructor() {
    const ingestService = inject(IngestService);

    super(ingestService);

    this.ingestService = ingestService;
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['searchText']) {
      this.searchChange.next(this.searchText());
    }
    if (changes['filters']) {
      this.filterChange.next(this.filters());
    }
    if (changes['ingestThatHasChanged']) {
      this.updateIngest(this.ingestThatHasChanged());
    }
  }

  private updateIngest(ingest: LogbookOperation) {
    if (!this.dataSource()) {
      return;
    }
    const index = this.dataSource().findIndex((o) => o.id === ingest.id);
    this.dataSource.update((ingests) => {
      const next = [...(ingests ?? [])];
      next[index] = ingest;
      return next;
    });
  }

  ngOnInit() {
    this.ingestService
      .search(
        new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.DESCENDANT, JSON.stringify(this.buildIngestCriteriaFromSearch())),
      )
      .subscribe((data: any[]) => {
        data.forEach((element: any) => {
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
        this.dataSource.set(data);
      });

    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildIngestCriteriaFromSearch();
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
    this.updatedData.subscribe(() => this.ingestService.logbookOperationsReloaded.next(this.dataSource()));
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
  }

  buildIngestCriteriaFromSearch() {
    const criteria: any = {};
    criteria.evTypeProc = 'INGEST';
    const searchText = this.searchText();
    if (searchText !== undefined && searchText.length > 0) {
      criteria['#id'] = searchText;
      criteria.obIdIn = searchText;
      criteria['events.agIdExt.TransferringAgency'] = searchText;
      criteria['events.agIdExt.originatingAgency'] = searchText;
      criteria['events.evDetData.ArchivalAgreement'] = searchText;
      criteria['events.evDetData.EvDetailReq'] = searchText;
    }

    const filters = this.filters();
    if (filters) {
      if (filters.startDate) {
        criteria.evDateTime_Start = filters.startDate;
      }
      if (filters.endDate) {
        const date = new Date(filters.endDate);
        date.setDate(date.getDate() + 1);
        criteria.evDateTime_End = date;
      }
    }
    return criteria;
  }

  searchIngestOrdered() {
    const query: any = this.buildIngestCriteriaFromSearch();
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT, JSON.stringify(query)));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  getIngestStatus(ingest: any): IngestStatus {
    return ingestStatus(ingest);
  }

  getIngestStatusColor(ingest: LogbookOperation): 'green' | 'grey' | 'orange' | 'red' | 'black' {
    const status: IngestStatus = ingestStatus(ingest);
    return ingestStatusVisualColor(status);
  }

  ingestEndDate(ingest: LogbookOperation): string {
    return ingest.events !== undefined && ingest.events.length !== 0
      ? ingest.events[ingest.events.length - 1].evDateTime
      : ingest.evDateTime;
  }
}
