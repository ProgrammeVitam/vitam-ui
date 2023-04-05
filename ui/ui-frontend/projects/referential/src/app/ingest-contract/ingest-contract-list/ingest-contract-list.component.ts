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
import { Subject, Subscription, merge } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import { DEFAULT_PAGE_SIZE, Direction, InfiniteScrollTable, IngestContract, PageRequest } from 'ui-frontend-common';

import { IngestContractService } from '../ingest-contract.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-ingest-contract-list',
  templateUrl: './ingest-contract-list.component.html',
  styleUrls: ['./ingest-contract-list.component.scss'],
})
export class IngestContractListComponent extends InfiniteScrollTable<IngestContract> implements OnDestroy, OnInit {
  orderBy = 'Name';
  direction = Direction.ASCENDANT;
  filterMap: { [key: string]: any[] } = {
    status: ['ACTIVE', 'INACTIVE'],
  };
  private _searchText: string;
  private updatedIngestContractsSub: Subscription;
  private firstSearchCriteriaSub: Subscription;
  private searchCriteriaSub: Subscription;
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();

  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  @Output()
  ingestContractClick = new EventEmitter<IngestContract>();

  constructor(public ingestContractService: IngestContractService) {
    super(ingestContractService);
  }

  ngOnInit() {
    this.pending = true;
    this.firstSearchCriteriaSub = this.ingestContractService
      .search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT))
      .subscribe(
        (data: IngestContract[]) => {
          this.dataSource = data;
        },
        () => {},
        () => (this.pending = false)
      );

    this.searchCriteriaSub = merge(this.searchChange, this.filterChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS))
      .subscribe(() => {
        const query: any = this.buildIngestContractCriteriaFromSearch();
        const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
        this.search(pageRequest);
      });

    this.subscribeOnIngestContractPatchOperation();
  }

  buildIngestContractCriteriaFromSearch() {
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

  subscribeOnIngestContractPatchOperation() {
    this.updatedIngestContractsSub = this.ingestContractService.updated.subscribe((ingestContract: IngestContract) => {
      const index = this.dataSource.findIndex((ingContract: IngestContract) => ingContract.identifier === ingestContract.identifier);
      if (index > -1) {
        this.dataSource[index] = {
          id: ingestContract.id,
          tenant: ingestContract.tenant,
          version: ingestContract.version,
          name: ingestContract.name,
          identifier: ingestContract.identifier,
          description: ingestContract.description,
          status: ingestContract.status,
          creationDate: ingestContract.creationDate,
          lastUpdate: ingestContract.lastUpdate,
          activationDate: ingestContract.activationDate,
          deactivationDate: ingestContract.deactivationDate,
          checkParentLink: ingestContract.checkParentLink,
          linkParentId: ingestContract.linkParentId,
          checkParentId: ingestContract.checkParentId,
          masterMandatory: ingestContract.masterMandatory,
          everyDataObjectVersion: ingestContract.everyDataObjectVersion,
          dataObjectVersion: ingestContract.dataObjectVersion,
          formatUnidentifiedAuthorized: ingestContract.formatUnidentifiedAuthorized,
          everyFormatType: ingestContract.everyFormatType,
          formatType: ingestContract.formatType,
          archiveProfiles: ingestContract.archiveProfiles,
          managementContractId: ingestContract.managementContractId,
          computeInheritedRulesAtIngest: ingestContract.computeInheritedRulesAtIngest,
        };
      }
    });
  }

  searchIngestContractOrdered() {
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
    this.updatedData.unsubscribe();
    this.firstSearchCriteriaSub.unsubscribe();
    this.searchCriteriaSub.unsubscribe();
    this.updatedIngestContractsSub.unsubscribe();
  }
}
