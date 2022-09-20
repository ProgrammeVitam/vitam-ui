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
import { merge, Subject, Subscription } from 'rxjs';
import { debounceTime, startWith } from 'rxjs/operators';
import {
  ApplicationId,
  buildCriteriaFromSearch,
  Criterion,
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollTable,
  Operators,
  PageRequest,
  Profile,
  SearchQuery
} from 'ui-frontend-common';
import { ProfileService } from '../profile.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-profile-list',
  templateUrl: './profile-list.component.html',
  styleUrls: ['./profile-list.component.scss']
})
export class ProfileListComponent extends InfiniteScrollTable<Profile> implements OnDestroy, OnInit {

  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }
  private _searchText: string;

  orderBy = 'name';
  direction = Direction.ASCENDANT;

  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();
  private readonly searchKeys = ['name', 'description', 'identifier'];

  @Output() profileClick = new EventEmitter<Profile>();

  private updatedProfileSub: Subscription;

  constructor(public rngProfileService: ProfileService) {
    super(rngProfileService);
    this.updatedProfileSub = this.rngProfileService.updated.subscribe((updatedProfile: Profile) => {
      const profileIndex = this.dataSource.findIndex((profile) => updatedProfile.id === profile.id);
      if (profileIndex > -1) {
        this.dataSource[profileIndex] = {
          id: this.dataSource[profileIndex].id,
          enabled: updatedProfile.enabled,
          name: updatedProfile.name,
          level: updatedProfile.level,
          customerId: updatedProfile.customerId,
          groupsCount: updatedProfile.groupsCount,
          description: updatedProfile.description,
          usersCount: this.dataSource[profileIndex].usersCount,
          tenantIdentifier: this.dataSource[profileIndex].tenantIdentifier,
          tenantName: this.dataSource[profileIndex].tenantName,
          applicationName: this.dataSource[profileIndex].applicationName,
          roles: this.dataSource[profileIndex].roles,
          readonly: this.dataSource[profileIndex].readonly,
          externalParamId: this.dataSource[profileIndex].externalParamId,
        };
      }
    });
  }

  ngOnInit() {
    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange)
      .pipe(
        startWith(null),
        debounceTime(FILTER_DEBOUNCE_TIME_MS)
      );

    searchCriteriaChange.subscribe(() => this.search());
  }

  ngOnDestroy() {
    this.updatedProfileSub.unsubscribe();
  }

  search() {
    const defaultCriterion: Criterion = { key: 'applicationName', value: ApplicationId.USERS_APP, operator: Operators.equals };
    const query: SearchQuery = {
      criteria: [
        defaultCriterion,
        ...buildCriteriaFromSearch(this._searchText, this.searchKeys),
      ]
    };
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));

    super.search(pageRequest);
  }

}
