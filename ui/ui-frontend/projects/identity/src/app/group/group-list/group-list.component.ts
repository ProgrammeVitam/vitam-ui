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
import {merge, Subject, Subscription} from 'rxjs';

import {
  buildCriteriaFromSearch,
  collapseAnimation, DEFAULT_PAGE_SIZE,
  Direction,
  Group,
  InfiniteScrollTable,
  PageRequest,
  rotateAnimation,
  SearchQuery,
} from 'ui-frontend-common';
import { GroupService } from '../group.service';
import {buildCriteriaFromGroupFilters} from './group-criteria-builder.util';

@Component({
  selector: 'app-group-list',
  templateUrl: './group-list.component.html',
  styleUrls: ['./group-list.component.scss'],
  animations: [
    collapseAnimation,
    rotateAnimation,
  ]
})
export class GroupListComponent extends InfiniteScrollTable<Group> implements OnDestroy, OnInit {

  @Output() groupClick = new EventEmitter<Group>();

  private updatedGroupSub: Subscription;

  @Input('search')
  set searchText(text: string) {
    this._search = text;
    this.searchChange.next(text);
  }

  private _search: string;
  private readonly searchKeys = [
    'identifier',
    'name',
    'level',
    'description'
  ];

  filterMap: { [key: string]: any[] } = {
    status: ['ENABLED'],
    level: null
  };

  orderBy = 'name';
  direction = Direction.ASCENDANT;

  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly orderChange = new Subject<string>();
  private readonly searchChange = new Subject<string>();

  levelFilterOptions: Array<{ value: string, label: string }> = [];

  constructor(public groupService: GroupService,
              @Inject(LOCALE_ID) private locale: string) {
    super(groupService);
  }

  ngOnInit() {
    this.search();
    this.refreshLevelOptions();

    this.updatedGroupSub = this.groupService.updated.subscribe((updatedGroup: Group) => {
      const profileGroupIndex = this.dataSource.findIndex((group) => updatedGroup.id === group.id);
      if (profileGroupIndex > -1) {
        this.dataSource[profileGroupIndex] = updatedGroup;
      }

    });

    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange);

    searchCriteriaChange.subscribe(() => {
      const query: SearchQuery = {
        criteria: [
          ...buildCriteriaFromGroupFilters(this.filterMap),
          ...buildCriteriaFromSearch(this._search, this.searchKeys)
          ]
      };
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));

      this.search(pageRequest);
    });

  }

  onFilterChange(key: string, values: any[]) {
    this.filterMap[key] = values;
    this.filterChange.next(this.filterMap);
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  ngOnDestroy() {
    this.updatedGroupSub.unsubscribe();
  }

  private refreshLevelOptions(query?: SearchQuery) {
    this.groupService.getNonEmptyLevels(query).subscribe((levels: string[]) => {
      this.levelFilterOptions = levels.map((level: string) => ({value: level, label: level }));
      this.levelFilterOptions.sort(sortByLabel(this.locale));
    });
  }
}


function sortByLabel(locale: string): (a: { label: string }, b: { label: string }) => number {
  return (a: { label: string }, b: { label: string }) => a.label.localeCompare(b.label, locale);
}
