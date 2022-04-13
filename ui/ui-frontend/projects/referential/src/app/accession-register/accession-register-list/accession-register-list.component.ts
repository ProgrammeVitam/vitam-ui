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
import {Component, EventEmitter, Inject, Input, LOCALE_ID, OnDestroy, OnInit, Output} from '@angular/core';
import {BehaviorSubject, Observable, Subscription} from 'rxjs';
import {withLatestFrom} from 'rxjs/operators';
import {
  AccessionRegisterDetail,
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollTable,
  OjectUtils,
  PageRequest
} from 'ui-frontend-common';
import {AccessionRegistersService} from '../accession-register.service';

@Component({
  selector: 'app-accession-register-list',
  templateUrl: './accession-register-list.component.html',
  styleUrls: ['./accession-register-list.component.scss'],
})
export class AccessionRegisterListComponent extends InfiniteScrollTable<AccessionRegisterDetail> implements OnDestroy, OnInit {
  @Output() accessionRegisterClick = new EventEmitter<AccessionRegisterDetail>();

  @Input('search')
  set searchText(searchText: string) {
    this.entryToSearch = searchText;
    this.searchChange.next(searchText);
    this.accessionRegistersService.notifySearchChange(searchText);
  }

  @Input('accessContract')
  accessContract: string;
  filterDebounceTimeMs = 400;
  direction = Direction.DESCENDANT;
  orderBy = 'EndDate';

  private filterChange = new BehaviorSubject<{ [key: string]: any[] }>({});
  private searchChange = new BehaviorSubject<string>(null);
  private orderChange = new BehaviorSubject<string>(this.orderBy);
  private searchKeys = ['OriginatingAgency', 'Opi'];
  private entryToSearch: string;

  statusFilterOptions$: Observable<Array<{ value: string; label: string }>>;
  filterMap: { [key: string]: any[] } = {
    Status: [],
  };

  searchSub: Subscription;
  advancedSearchSub: Subscription;

  constructor(public accessionRegistersService: AccessionRegistersService, @Inject(LOCALE_ID) private locale: string) {
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
          this.accessionRegistersService.getAdvancedSearchData()
        )
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

  searchRequest() {
    const dateInterval: { endDateMin: string; endDateMax: string } = this.accessionRegistersService.getDateIntervalChanges().getValue();
    const avancedSearchData: any = this.accessionRegistersService.getAdvancedSearchData().getValue();
    const query: any = {};
    this.addCriteriaFromSearch(query);
    this.addCriteriaFromFilters(query);
    this.addCriteriaFromDateFilters(query, dateInterval);
    this.addAdvancedCriteriaData(query, avancedSearchData);
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
    super.search(pageRequest);
  }

  exportAccessionRegisterCsv() {
    const avancedSearchData: any = this.accessionRegistersService.getAdvancedSearchData().getValue();
    const query: any = {};
    query.searchText = this.entryToSearch;
    if (this.filterMap.Status.length !== 0) {
      query.statusFilter = this.filterMap.Status;
    }
    const dateInterval: { endDateMin: string; endDateMax: string } = this.accessionRegistersService.getDateIntervalChanges().getValue();
    if (dateInterval !== null && (dateInterval.endDateMin !== null || dateInterval.endDateMax !== null)) {
      query.dateInterval = dateInterval;
    }
    query.advancedSearch = {}
    this.addAdvancedCriteriaData(query.advancedSearch, avancedSearchData);
    this.accessionRegistersService.exportAccessionRegisterCsv(query, this.accessContract);
  }

  addAdvancedCriteriaData(query: any, avancedSearchData: any) {
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

    if (OjectUtils.valueNotUndefined(avancedSearchData.transfer_reply)) {
      query.transfer_reply = avancedSearchData.transfer_reply;
    }
  }

  addCriteriaFromDateFilters(query: any, dateInterval: { endDateMin: string; endDateMax: string }) {
    if (dateInterval !== null && (dateInterval.endDateMin !== null || dateInterval.endDateMax !== null)) {
      query.EndDate = dateInterval;
    }
  }

  addCriteriaFromFilters(query: any) {
    if (this.filterMap.Status.length !== 0) {
      query.Status = this.filterMap.Status;
    }
  }

  addCriteriaFromSearch(query: any) {
    if (this.entryToSearch !== undefined && this.entryToSearch !== null && this.entryToSearch.length > 0) {
      this.searchKeys.forEach((key) => {
        query[key] = this.entryToSearch;
      });
    }
  }

  emitOrderChange(event: string) {
    this.orderChange.next(event);
    this.accessionRegistersService.notifyOrderChange();
  }

  onFilterChange(key: string, values: any[]) {
    this.filterMap[key] = values;
    this.filterChange.next(this.filterMap);
    this.accessionRegistersService.notifyFilterChange(this.filterMap);
  }

  onSelectRow(accessionRegisterDetail: AccessionRegisterDetail) {
    this.accessionRegisterClick.emit(accessionRegisterDetail);
  }

}
