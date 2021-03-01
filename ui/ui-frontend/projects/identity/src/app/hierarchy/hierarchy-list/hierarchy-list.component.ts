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
import { merge, Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter, map, tap } from 'rxjs/operators';
import {
  Application,
  ApplicationId, ApplicationService, buildCriteriaFromSearch, Criterion, Direction,
  InfiniteScrollTable, Operators, PageRequest, Profile, SearchQuery
} from 'ui-frontend-common';

import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { DEFAULT_PAGE_SIZE } from '../../core/customer.service';
import { HierarchyService } from '../hierarchy.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-hierarchy-list',
  templateUrl: './hierarchy-list.component.html',
  styleUrls: ['./hierarchy-list.component.scss']
})
export class HierarchyListComponent extends InfiniteScrollTable<Profile> implements OnDestroy, OnInit {

  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }
  private _searchText: string;

  @Output() profileClick = new EventEmitter<Profile>();

  private tenantIdentifier: number;
  private updatedProfileSub: Subscription;
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();
  private readonly searchKeys = ['name', 'description', 'identifier'];

  constructor(public hierarchyService: HierarchyService, public applicationService: ApplicationService, private route: ActivatedRoute) {
    super(hierarchyService);
    this.updatedProfileSub = this.hierarchyService.updated.subscribe((updatedProfile: Profile) => {
      const profileIndex = this.dataSource.findIndex((profile) => updatedProfile.id === profile.id);
      if (profileIndex > -1) {
        this.dataSource[profileIndex] = {
          id: this.dataSource[profileIndex].id,
          enabled: updatedProfile.enabled,
          name: updatedProfile.name,
          level: updatedProfile.level,
          customerId: this.dataSource[profileIndex].customerId,
          groupsCount: this.dataSource[profileIndex].groupsCount,
          description: updatedProfile.description,
          usersCount: this.dataSource[profileIndex].usersCount,
          tenantName: this.dataSource[profileIndex].tenantName,
          tenantIdentifier: this.dataSource[profileIndex].tenantIdentifier,
          applicationName: this.dataSource[profileIndex].applicationName,
          roles: this.dataSource[profileIndex].roles,
          readonly: this.dataSource[profileIndex].readonly,
          externalParamId: this.dataSource[profileIndex].externalParamId,
        };
      }
    });
  }

  ngOnInit() {
    const tenantChange = this.route.paramMap.pipe(
      filter((paramMap) => !!paramMap.get('tenantIdentifier')),
      map((paramMap) => +paramMap.get('tenantIdentifier')),
      distinctUntilChanged(),
      tap((tenantIdentifier) => {
        this.tenantIdentifier = tenantIdentifier;
        this.hierarchyService.setTenantId(tenantIdentifier);
      }),
    );

    const searchCriteriaChange = merge(tenantChange, this.searchChange, this.filterChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => this.search());
  }

  ngOnDestroy() {
    this.updatedProfileSub.unsubscribe();
  }

  search() {
    const defaultCriteria: Criterion[] = [
      {
        key: 'applicationName',
        value: [ApplicationId.USERS_APP],
        operator: Operators.notin
      },
      { key: 'externalParamId', value: null, operator: Operators.equals },
      { key: 'tenantIdentifier', value: this.tenantIdentifier, operator: Operators.equals }
    ];

    const query: SearchQuery = {
      criteria: [
        ...defaultCriteria,
        ...buildCriteriaFromSearch(this._searchText, this.searchKeys),
     ]
    };

    super.search(new PageRequest(0, DEFAULT_PAGE_SIZE, 'name', Direction.ASCENDANT, JSON.stringify(query)));
  }

  getApplicationName(appId: string): string {
    if (appId) {
      const matchApplication: Application = this.applicationService.applications.find((application) => application.identifier === appId);
      return matchApplication ? matchApplication.name : appId;
    }

    return '';
  }

}
