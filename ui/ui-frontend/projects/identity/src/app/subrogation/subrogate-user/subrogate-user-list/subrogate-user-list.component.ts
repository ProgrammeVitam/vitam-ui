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
import { forkJoin, Observable, Subject } from 'rxjs';
import {
  ApplicationId, AuthService, AuthUser, buildCriteriaFromSearch, Criterion, DEFAULT_PAGE_SIZE, Direction,
  Group, InfiniteScrollTable, Operators, PageRequest, Profile, SearchQuery, SubrogationModalService, SubrogationUser
} from 'ui-frontend-common';

import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';

import { SubrogationService } from '../../subrogation.service';

const MINIMUM_CRITICALITY = 0;
const AVERAGE_CRITICALITY = 1;
const MAXIMUM_CRITICALITY = 2;

@Component({
  selector: 'app-subrogate-user-list',
  templateUrl: './subrogate-user-list.component.html',
  styleUrls: ['./subrogate-user-list.component.scss']
})
export class SubrogateUserListComponent extends InfiniteScrollTable<SubrogationUser> implements OnDestroy, OnInit {

  @Input() emailDomains: string[];

  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  private _searchText: string;

  private groups: Array<{id: string, group: any}> = [];
  overridePendingChange: true;
  loaded = false;
  customerId: string;
  currenteUser: AuthUser;

  private readonly searchChange = new Subject<string>();
  private readonly searchKeys = [
    'firstname',
    'lastname',
    'email',
    'mobile',
    'phone',
    'identifier'
  ];


  constructor(
    public subrogationService: SubrogationService,
    public dialog: MatDialog,
    private activatedRoute: ActivatedRoute,
    private subrogationModalService: SubrogationModalService,
    private authService: AuthService,
  ) {
    super(subrogationService);
  }

  ngOnInit() {

    this.currenteUser = this.authService.user;
    this.refreshDataList();
    this.activatedRoute.params.subscribe(() => this.refreshDataList());

    this.updatedData.subscribe(() => {

      const groupIds = new Set (this.dataSource.map((subrogationUser: SubrogationUser) => subrogationUser.groupId));

      const observables = new Array<Observable<Group>>();
      groupIds.forEach((groupId) => {
        const existingGroup = this.groups.find((group) => group.id === groupId);
        if (!existingGroup) {
          observables.push(this.subrogationService.getGroupById(groupId));
        }
      });

      if (observables && observables.length > 0) {
        forkJoin([forkJoin(observables), this.subrogationService.getAllByCustomerId(this._getCustomerId())]).subscribe((results) => {
          results[0].forEach((group) => {
            this.groups.push({ id: group.id, group });
          });

          const subrogations = results[1];
          this.dataSource.filter((subrogationUser: SubrogationUser) => !subrogationUser.criticality)
          .forEach(((subrogationUser: SubrogationUser) => {
            const subrogateUserGroup = this.getGroup(subrogationUser);
            const subroUser = subrogations.find((s) => s.surrogate === subrogationUser.email);
            if (subroUser && subroUser.superUser) {
              subrogationUser.superUserEmail = subroUser.superUser;
            }
            if (subrogateUserGroup) {
              subrogationUser.criticality = this.computeCriticality(subrogateUserGroup.profiles);
            }
          }));

          this.loaded = true;
          this.pending = false;
        });
      } else {
        this.loaded = true;
        this.pending = false;
      }
    });

    this._onSearchChange();
  }

  openUserSubrogationDialog(subrogateUser: SubrogationUser): void {
    this.subrogationModalService.open(this.emailDomains, subrogateUser);
  }

  getGroup(subrogateUser: SubrogationUser): any {
    const subrogateUserGroup = this.groups.find((group) => group.id === subrogateUser.groupId);

    return subrogateUserGroup ? subrogateUserGroup.group : undefined;
  }

  refreshDataList(): void {
    this._search({ criteria: this._getCriterionArrayToCustomer() });
  }

  private _getCriterionArrayToCustomer(): Array<Criterion> {
    const criterionArray = [];
    criterionArray.push({
      key: 'customerId',
      value: this._getCustomerId(),
      operator: Operators.equals
    });
    return criterionArray;
  }

  private _onSearchChange(): void {
    this.searchChange.subscribe(() => {
      const query: SearchQuery = {
        criteria: [
          ...this._getCriterionArrayToCustomer(),
          ...buildCriteriaFromSearch(this._searchText, this.searchKeys)
        ]
      };

      this._search(query);
    });
  }

  private _search(query: SearchQuery): void {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, 'lastname', Direction.ASCENDANT, JSON.stringify(query)));
  }

  private _getCustomerId(): string {
    return this.activatedRoute.snapshot.paramMap.get('customerId');
  }

  private computeCriticality(profiles: Profile[]): number {
    let criticality = MINIMUM_CRITICALITY;
    for (const profile of profiles) {
      if (profile.applicationName === ApplicationId.CUSTOMERS_APP) {
        criticality = MAXIMUM_CRITICALITY;
        break;
      } else if (criticality < MAXIMUM_CRITICALITY && (
        profile.applicationName === ApplicationId.USERS_APP
        || profile.applicationName === ApplicationId.PROFILES_APP
        || profile.applicationName === ApplicationId.SUBROGATIONS_APP
        || profile.applicationName === ApplicationId.GROUPS_APP
      )) {
        criticality = AVERAGE_CRITICALITY;
      }
    }

    return criticality;
  }

  ngOnDestroy(): void {
    this.updatedData.unsubscribe();
  }

}
