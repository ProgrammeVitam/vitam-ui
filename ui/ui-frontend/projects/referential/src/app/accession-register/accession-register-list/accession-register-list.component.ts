/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import { Component, EventEmitter, Inject, Input, LOCALE_ID, OnDestroy, OnInit, Output } from '@angular/core';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { withLatestFrom } from 'rxjs/operators';
import {
  AccessionRegisterDetail,
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollTable,
  OjectUtils,
  PageRequest,
  InfiniteScrollDirective,
  OrderByButtonComponent,
  TableFilterComponent,
  PipesModule,
  TableFilterDirective,
  TableFilterSearchComponent,
} from 'vitamui-library';
import { AccessionRegisterSearchDto } from '../../models/accession-register-export-csv.interface';
import { AccessionRegistersService } from '../accession-register.service';
import { TruncatePipe } from '../../../../../vitamui-library/src/app/modules/pipes/truncate.pipe';
import { DateTimePipe } from '../../../../../vitamui-library/src/app/modules/pipes/datetime.pipe';
import { BytesPipe } from '../../../../../vitamui-library/src/app/modules/pipes/bytes.pipe';
import { TranslateModule } from '@ngx-translate/core';
import { MatLegacyProgressSpinnerModule } from '@angular/material/legacy-progress-spinner';
import { MatLegacyTooltipModule } from '@angular/material/legacy-tooltip';
import { NgIf, NgClass, NgFor, AsyncPipe } from '@angular/common';

@Component({
  selector: 'app-accession-register-list',
  templateUrl: './accession-register-list.component.html',
  styleUrls: ['./accession-register-list.component.scss'],
  standalone: true,
  imports: [
    InfiniteScrollDirective,
    NgIf,
    NgClass,
    OrderByButtonComponent,
    TableFilterComponent,
    NgFor,
    MatLegacyTooltipModule,
    MatLegacyProgressSpinnerModule,
    AsyncPipe,
    PipesModule,
    TranslateModule,
    BytesPipe,
    DateTimePipe,
    TruncatePipe,
    TableFilterDirective,
    TableFilterSearchComponent,
  ],
})
export class AccessionRegisterListComponent extends InfiniteScrollTable<AccessionRegisterDetail> implements OnDestroy, OnInit {
  @Output() accessionRegisterClick = new EventEmitter<AccessionRegisterDetail>();

  @Input()
  set searchText(searchText: string) {
    this.textToSearch = searchText;
    this.searchChange.next(searchText);
    this.accessionRegistersService.notifySearchChange(searchText);
  }

  // eslint-disable-next-line @angular-eslint/no-input-rename
  @Input('accessContract')
  accessContract: string;
  direction = Direction.DESCENDANT;
  orderBy = 'EndDate';

  filterMap: Map<string, string[]> = new Map<string, string[]>();
  statusFilterOptions$: Observable<Array<{ value: string; label: string }>>;

  private filterChange = new BehaviorSubject<Map<string, Array<string>>>(null);
  private searchChange = new BehaviorSubject<string>(null);
  private orderChange = new BehaviorSubject<string>(this.orderBy);
  private textToSearch: string;

  searchSub: Subscription;
  advancedSearchSub: Subscription;

  constructor(
    public accessionRegistersService: AccessionRegistersService,
    @Inject(LOCALE_ID) private locale: string,
  ) {
    super(accessionRegistersService);
  }

  ngOnInit() {
    this.statusFilterOptions$ = this.accessionRegistersService.getAccessionRegisterStatus(this.locale);
    this.advancedSearchSub = this.accessionRegistersService
      .getGlobalSearchButtonEvent()
      .pipe(
        withLatestFrom(
          this.searchChange,
          this.filterChange,
          this.orderChange,
          this.accessionRegistersService.getDateIntervalChanges(),
          this.accessionRegistersService.getAdvancedSearchData(),
        ),
      )
      .subscribe((changes) => {
        const globalSearch = changes[0];
        if (globalSearch) {
          this.searchRequest();
        }
      });
  }

  ngOnDestroy() {
    this.searchSub?.unsubscribe();
    this.advancedSearchSub?.unsubscribe();
  }

  getTotalElements() {
    return this.searchService.getTotalElements;
  }

  searchRequest() {
    const query: AccessionRegisterSearchDto = this.constructQuery();
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
    super.search(pageRequest);
  }

  exportAccessionRegisterCsv() {
    const query: AccessionRegisterSearchDto = this.constructQuery();
    query.orderBy = this.orderBy;
    query.direction = this.direction;
    this.accessionRegistersService.exportAccessionRegisterCsv(query, this.accessContract);
  }

  private constructQuery(): AccessionRegisterSearchDto {
    const query: AccessionRegisterSearchDto = {};
    this.addCriteriaFromSearch(query);
    query.filters = this.filterMap;
    this.addCriteriaFromDateFilters(query);
    this.addAdvancedCriteriaData(query);
    return query;
  }

  addAdvancedCriteriaData(query: any) {
    const avancedSearchData: any = this.accessionRegistersService.getAdvancedSearchData().getValue();
    if (avancedSearchData === null) {
      return;
    }
    if (OjectUtils.arrayNotUndefined(avancedSearchData.originatingAgencies)) {
      query.originatingAgencies = avancedSearchData.originatingAgencies;
    }
    if (OjectUtils.arrayNotUndefined(avancedSearchData.archivalAgreements)) {
      query.archivalAgreements = avancedSearchData.archivalAgreements;
    }
    if (OjectUtils.arrayNotUndefined(avancedSearchData.archivalProfiles)) {
      query.archivalProfiles = avancedSearchData.archivalProfiles;
    }
    if (OjectUtils.arrayNotUndefined(avancedSearchData.acquisitionInformations)) {
      query.acquisitionInformations = avancedSearchData.acquisitionInformations;
    }
    if (OjectUtils.valueNotUndefined(avancedSearchData.elimination)) {
      query.elimination = avancedSearchData.elimination;
    }
    if (OjectUtils.valueNotUndefined(avancedSearchData.transferReply)) {
      query.transferReply = avancedSearchData.transferReply;
    }
  }

  addCriteriaFromDateFilters(query: AccessionRegisterSearchDto) {
    const dateInterval: { endDateMin: string; endDateMax: string } = this.accessionRegistersService.getDateIntervalChanges().getValue();
    if (dateInterval !== null && (dateInterval.endDateMin !== null || dateInterval.endDateMax !== null)) {
      query.endDateInterval = dateInterval;
    }
  }

  addCriteriaFromFilters(query: AccessionRegisterSearchDto) {
    query.filters = this.filterMap;
  }

  addCriteriaFromSearch(query: AccessionRegisterSearchDto) {
    if (this.textToSearch !== undefined && this.textToSearch !== null && this.textToSearch.length > 0) {
      query.searchText = this.textToSearch;
      query.originatingAgency = this.textToSearch;
      query.opi = this.textToSearch;
    }
  }

  emitOrderChange(event: string) {
    this.orderChange.next(event);
    this.accessionRegistersService.notifyOrderChange();
  }

  onFilterChange(key: string, values: string[]) {
    this.filterMap.set(key, values);
    this.filterChange.next(this.filterMap);
    this.accessionRegistersService.notifyFilterChange(this.filterMap);
  }

  onSelectRow(accessionRegisterDetail: AccessionRegisterDetail) {
    this.accessionRegisterClick.emit(accessionRegisterDetail);
  }
}
