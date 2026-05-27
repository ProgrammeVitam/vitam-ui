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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { Subject, Subscription, merge } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { DEFAULT_PAGE_SIZE, PageRequest } from '../../../../../vitamui-library/src/app/modules/vitamui-table/page-request.model';
import { Direction } from '../../../../../vitamui-library/src/app/modules/vitamui-table/direction.enum';
import { InfiniteScrollTable } from '../../../../../vitamui-library/src/app/modules/vitamui-table/infinite-scroll-table';
import { ManagementContract } from '../../../../../vitamui-library/src/app/modules/models/managementContract/management-contract.interface';
import { ManagementContractService } from '../management-contract.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-management-contract-list',
  templateUrl: './management-contract-list.component.html',
  styleUrls: ['./management-contract-list.component.scss'],
  standalone: false,
})
export class ManagementContractListComponent extends InfiniteScrollTable<ManagementContract> implements OnDestroy, OnInit {
  managementContractService: ManagementContractService;

  orderBy = 'Name';
  direction = Direction.ASCENDANT;
  filterMap: { [key: string]: any[] } = {
    status: ['ACTIVE', 'INACTIVE'],
  };
  _searchText: string;
  updatedManagementContractsSub: Subscription;
  firstSearchCriteriaSub: Subscription;
  searchCriteriaSub: Subscription;
  readonly filterChange = new Subject<{ [key: string]: any[] }>();
  readonly searchChange = new Subject<string>();
  readonly orderChange = new Subject<void>();

  @Input()
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  @Output()
  managementContractClick = new EventEmitter<ManagementContract>();

  constructor() {
    const managementContractService = inject(ManagementContractService);

    super(managementContractService);

    this.managementContractService = managementContractService;
  }

  ngOnInit() {
    this.pending = true;
    this.firstSearchCriteriaSub = this.managementContractService
      .search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT))
      .subscribe(
        (data: ManagementContract[]) => {
          this.dataSource = data;
        },
        () => {},
        () => (this.pending = false),
      );

    this.searchCriteriaSub = merge(this.searchChange, this.filterChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS))
      .subscribe(() => {
        const query: any = this.buildManagementContractCriteriaFromSearch();
        const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
        this.search(pageRequest);
      });

    this.subscribeOnManagementContractPatchOperation();
  }

  buildManagementContractCriteriaFromSearch() {
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

  subscribeOnManagementContractPatchOperation() {
    this.updatedManagementContractsSub = this.managementContractService.updated.subscribe((managementContract: ManagementContract) => {
      const index = this.dataSource.findIndex(
        (mngContract: ManagementContract) => mngContract.identifier === managementContract.identifier,
      );
      if (index > -1) {
        this.dataSource[index] = { ...managementContract };
      }
    });
  }

  searchManagementContractOrdered() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  onFilterChange(key: string, values: any[]) {
    this.filterMap[key] = values;
    this.filterChange.next(this.filterMap);
  }

  ngOnDestroy() {
    this.updatedData?.unsubscribe();
    this.firstSearchCriteriaSub?.unsubscribe();
    this.searchCriteriaSub?.unsubscribe();
    this.updatedManagementContractsSub?.unsubscribe();
  }
}
