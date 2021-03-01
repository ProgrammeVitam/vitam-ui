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
import { debounceTime } from 'rxjs/operators';
import {
  AdminUserProfile, ApplicationId, AuthService, buildCriteriaFromSearch, collapseAnimation, Criterion, DEFAULT_PAGE_SIZE,
  Direction, InfiniteScrollTable, Operators, PageRequest, Role, rotateAnimation, SearchQuery, User, VitamUISnackBar
} from 'ui-frontend-common';

import { HttpParams } from '@angular/common/http';
import {
  Component, ElementRef, EventEmitter, Inject, Input, LOCALE_ID, OnDestroy, OnInit, Output,
  TemplateRef, ViewChild
} from '@angular/core';

import { GroupApiService } from '../../core/api/group-api.service';
import { UserService } from '../user.service';
import { buildCriteriaFromUserFilters } from './user-criteria-builder.util';
import { VitamUISnackBarComponent } from '../../shared/vitamui-snack-bar';
import { CustomerService } from '../../core/customer.service';


const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss'],
  animations: [
    collapseAnimation,
    rotateAnimation,
  ]
})

export class UserListComponent extends InfiniteScrollTable<User> implements OnDestroy, OnInit {



  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }
  private _searchText: string;

  @Output() userClick = new EventEmitter<User>();

  @ViewChild('filterTemplate', { static: false }) filterTemplate: TemplateRef<UserListComponent>;
  @ViewChild('filterButton', { static: false }) filterButton: ElementRef;

  overridePendingChange: true;
  loaded = false;
  statusFilter: string[] = [];
  filterMap: { [key: string]: any[] } = {
    status: [],
    level: null,
    group: null,
  };
  groupFilterOptions: Array<{ value: string, label: string }> = [];
  levelFilterOptions: Array<{ value: string, label: string }> = [];
  orderBy = 'lastname';
  direction = Direction.ASCENDANT;
  genericUserRole: Readonly<{ appId: ApplicationId, tenantIdentifier: number, roles: Role[] }>;
  totalMonth: number;
  isInactifUsers = false;

  private groups: Array<{ id: string, group: any }> = [];
  private updatedUserSub: Subscription;
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();
  private readonly searchKeys = [
    'firstname',
    'lastname',
    'email',
    'mobile',
    'phone',
    'identifier',
  ];

  @Input()
  get connectedUserInfo(): AdminUserProfile { return this._connectedUserInfo; }
  set connectedUserInfo(userInfo: AdminUserProfile) {
    this._connectedUserInfo = userInfo;
  }
  private _connectedUserInfo: AdminUserProfile;

  constructor(
    private customerService: CustomerService,
    private snackBar: VitamUISnackBar,
    public userService: UserService,
    private groupApiService: GroupApiService,
    @Inject(LOCALE_ID) private locale: string,
    private authService: AuthService
  ) {
    super(userService);
    this.genericUserRole = {
      appId: ApplicationId.USERS_APP,
      tenantIdentifier: +this.authService.user.proofTenantIdentifier,
      roles: [Role.ROLE_GENERIC_USERS]
    };
  }

  ngOnInit() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
    this.refreshLevelOptions();

    this.updatedUserSub = this.userService.userUpdated.subscribe((updatedUser: User) => {
      const userIndex = this.dataSource.findIndex((user) => updatedUser.id === user.id);
      if (userIndex > -1) {
        this.userService.get(updatedUser.id).subscribe((user: User) => {
          this.dataSource[userIndex] = user;
        });
      }
    });

    this.updatedData.subscribe(() => {
      const groupIds = new Set(this.dataSource.map((user: User) => user.groupId));

      const idsToFetch = new Array<string>();
      groupIds.forEach((groupId) => {
        const existingGroup = this.groups.find((group) => group.id === groupId);
        if (!existingGroup) {
          idsToFetch.push(groupId);
        }
      });

      if (idsToFetch && idsToFetch.length > 0) {
        const criterionArray: Criterion[] = [];
        criterionArray.push({ key: 'id', value: idsToFetch, operator: Operators.in });

        const params = new HttpParams().set('embedded', 'ALL').set('criteria', JSON.stringify({ criteria: criterionArray }));

        this.groupApiService.getAllByParams(params).subscribe((results) => {
          results.forEach((group) => {
            this.groups.push({ id: group.id, group });
          });
          this.loaded = true;
          this.pending = false;
        });

        this.checkInactifUsers();

      } else {
        this.loaded = true;
        this.pending = false;
      }
    });

    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: SearchQuery = {
        criteria: [
          ...buildCriteriaFromUserFilters(this.filterMap),
          ...buildCriteriaFromSearch(this._searchText, this.searchKeys),
        ]
      };
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));

      this.search(pageRequest);
    });

    this.groupApiService.getAllByParams(new HttpParams()).subscribe((groups) => {
      this.groupFilterOptions = groups.map((group) => ({ value: group.id, label: group.name }));
      this.groupFilterOptions.sort(sortByLabel(this.locale));
    });
  }

  refreshLevelOptions(query?: SearchQuery) {
    this.userService.getLevelsNoEmpty(query).subscribe((levels) => {
      this.levelFilterOptions = levels.map((level) => ({ value: level, label: level }));
      this.levelFilterOptions.sort(sortByLabel(this.locale));
    });
  }

  ngOnDestroy() {
    this.updatedUserSub.unsubscribe();
    this.updatedData.unsubscribe();
  }

  getGroup(user: User) {
    const userGroup = this.groups.find((group) => group.id === user.groupId);

    return userGroup ? userGroup.group : undefined;
  }

  onFilterChange(key: string, values: any[]) {
    this.filterMap[key] = values;
    this.filterChange.next(this.filterMap);
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  showUser(user: User) {
    if (user.status === "REMOVED") {
      this.snackBar.openFromComponent(VitamUISnackBarComponent, {
        panelClass: 'vitamui-snack-bar',
        duration: 10000,
        data: { type: 'useralreadyDeleted' },
      });
    }
    else {

      this.userClick.emit(user);
    }
  }


  checkInactifUsers() {

    this.customerService.getMyCustomer().subscribe((customer) => {
      if (customer.gdprAlert) {

        this.dataSource.filter((user: User) => user.status === "DISABLED" && user.disablingDate !== null).forEach((u: User) => {

          this.totalMonth = ((new Date().getFullYear()) - (new Date(u.disablingDate)).getFullYear()) * 12 - (new Date(u.disablingDate)).getMonth() + (new Date().getMonth());

          if (this.totalMonth > customer.gdprAlertDelay && u.email.split('@')[1] === customer.defaultEmailDomain) {

            this.isInactifUsers = true;
          }
        })

        if (this.isInactifUsers) {

          this.snackBar.openFromComponent(VitamUISnackBarComponent, {
            panelClass: 'vitamui-snack-bar',
            duration: 60000,
            data: { type: 'usersToDelete' },
          });
        }
      }
    });



  }

}

function sortByLabel(locale: string): (a: { label: string }, b: { label: string }) => number {
  return (a: { label: string }, b: { label: string }) => a.label.localeCompare(b.label, locale);
}
