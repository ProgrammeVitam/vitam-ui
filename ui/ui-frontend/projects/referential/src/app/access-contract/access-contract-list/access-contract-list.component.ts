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
import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {merge, Subject} from 'rxjs';
import {debounceTime, takeUntil} from 'rxjs/operators';
import {
  AccessContract,
  collapseAnimation,
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollTable,
  PageRequest,
  rotateAnimation,
} from 'ui-frontend-common';

import {AccessContractService} from '../access-contract.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-access-contract-list',
  templateUrl: './access-contract-list.component.html',
  styleUrls: ['./access-contract-list.component.scss'],
  animations: [collapseAnimation, rotateAnimation],
})
export class AccessContractListComponent extends InfiniteScrollTable<AccessContract> implements OnDestroy, OnInit {
  // tslint:disable-next-line:no-input-rename
  @Input('search') set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  @Output() accessContractClick = new EventEmitter<AccessContract>();

  public orderBy = 'Name';
  public direction = Direction.ASCENDANT;
  public filterMap: { [key: string]: any[] } = { status: ['ACTIVE', 'INACTIVE'] };
  public readonly orderChange = new Subject<string>();

  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();

  // tslint:disable-next-line:variable-name
  private _searchText: string;

  private readonly destroyer$ = new Subject();

  constructor(public accessContractService: AccessContractService) {
    super(accessContractService);
  }

  ngOnInit() {
    this.pending = true;
    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange).pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));
    this.accessContractService.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT)).subscribe(
      (data: AccessContract[]) => {
        this.dataSource = data;
      },
      () => {},
      () => (this.pending = false)
    );

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildAccessContractCriteriaFromSearch();
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });

    this.replaceUpdatedAccessContract();
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public searchAccessContractOrdered(): void {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  public onFilterChange(key: string, values: any[]) {
    this.filterMap[key] = values;
    this.filterChange.next(this.filterMap);
  }

  private replaceUpdatedAccessContract(): void {
    this.accessContractService.updated.pipe(takeUntil(this.destroyer$)).subscribe(
      (updatedAccessContract: AccessContract) => {
        const index = this.dataSource.findIndex((item: AccessContract) => item.id === updatedAccessContract.id);
        if (index !== -1) {
          this.dataSource[index] = updatedAccessContract;
        }
      }
    );
  }

  private buildAccessContractCriteriaFromSearch() {
    const criteria: any = {};
    if (this._searchText.length > 0) {
      criteria.Name = this._searchText;
      criteria.Identifier = this._searchText;
    }

    if (this.filterMap.status.length > 0) {
      criteria.Status = this.filterMap.status;
    }

    return criteria;
  }

  emitOrderChange() {
    this.orderChange.next();
  }


}
