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
import { Component, EventEmitter, Inject, Input, LOCALE_ID, OnDestroy, OnInit, Output } from '@angular/core';
import { merge, Observable, Subject, Subscription } from 'rxjs';
import { debounceTime, startWith } from 'rxjs/operators';
import { DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, PageRequest } from 'ui-frontend-common';
import { AccessionRegisterDetail } from '../../../../../vitamui-library/src/lib/models/accession-registers-detail';
import { AccessionRegistersService } from '../accession-register.service';
import { AccessionRegisterBusiness } from '../accession-register.business';

@Component({
  selector: 'app-accession-register-list',
  templateUrl: './accession-register-list.component.html',
  styleUrls: ['./accession-register-list.component.scss'],
})
export class AccessionRegisterListComponent extends InfiniteScrollTable<AccessionRegisterDetail> implements OnDestroy, OnInit {
  @Output() accessionRegisterClick = new EventEmitter<AccessionRegisterDetail>();
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();
  private readonly searchKeys = ['OriginatingAgency', 'Opi'];
  private _searchText: string;

  filterDebounceTimeMs = 400;
  direction = Direction.DESCENDANT;
  orderBy = 'StartDate';

  statusFilterOptions$: Observable<Array<{ value: string; label: string }>>;
  filterMap: { [key: string]: any[] } = {
    Status: [],
  };

  searchSub: Subscription;

  constructor(
    public accessionRegistersService: AccessionRegistersService,
    public accessionRegisterBusiness: AccessionRegisterBusiness,
    @Inject(LOCALE_ID) private locale: string
  ) {
    super(accessionRegistersService);
  }

  ngOnInit() {
    this.searchSub = merge(this.searchChange, this.filterChange, this.orderChange)
      .pipe(startWith(null), debounceTime(this.filterDebounceTimeMs))
      .subscribe(() => this.search());
    this.statusFilterOptions$ = this.accessionRegisterBusiness.getAccessionRegisterStatus(this.locale);
  }

  ngOnDestroy() {
    this.searchSub.unsubscribe();
  }

  //Gestion de la recherche
  search() {
    const query: any = {};
    this.addCriteriaFromSearch(query);
    this.addCriteriaFromFilters(query);
    console.log(query);
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
    super.search(pageRequest);
  }

  addCriteriaFromFilters(query: any) {
    if (this.filterMap['Status'].length !== 0) {
      query['Status'] = this.filterMap['Status'];
    }
  }

  addCriteriaFromSearch(query: any) {
    if (this._searchText != undefined && this._searchText.length > 0) {
      this.searchKeys.forEach((key) => {
        query[key] = this._searchText;
      });
    }
  }

  //Gestion du tri
  emitOrderChange() {
    this.orderChange.next();
  }

  //Gestion des filtres
  onFilterChange(key: string, values: any[]) {
    this.filterMap[key] = values;
    this.filterChange.next(this.filterMap);
  }
}
