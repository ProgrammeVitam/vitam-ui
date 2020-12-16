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
import {animate, state, style, transition, trigger} from '@angular/animations';
import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnDestroy,
  OnInit,
  Output,
  TemplateRef,
  ViewChild
} from '@angular/core';
import {Context} from 'projects/vitamui-library/src/lib/models/context';
import { merge, Subject } from 'rxjs';
import {debounceTime} from 'rxjs/operators';
import {
  AdminUserProfile,
  ApplicationId,
  AuthService,
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollTable,
  PageRequest,
  Role,
  User
} from 'ui-frontend-common';
import {ContextService} from '../context.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-context-list',
  templateUrl: './context-list.component.html',
  styleUrls: ['./context-list.component.scss'],
  animations: [
    trigger('expansion', [
      state('collapsed', style({height: '0px', visibility: 'hidden'})),
      state('expanded', style({height: '*', visibility: 'visible'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),

    trigger('arrow', [
      state('collapsed', style({transform: 'rotate(180deg)'})),
      state('expanded', style({transform: 'none'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4,0.0,0.2,1)')),
    ]),
  ]
})
export class ContextListComponent extends InfiniteScrollTable<Context> implements OnDestroy, OnInit {
  // tslint:disable-next-line:no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    console.log('searchTect: ', searchText);
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  // tslint:disable-next-line:variable-name
  private _searchText: string;

  @Output() contextClick = new EventEmitter<Context>();

  @ViewChild('filterTemplate', {static: false}) filterTemplate: TemplateRef<ContextListComponent>;
  @ViewChild('filterButton', {static: false}) filterButton: ElementRef;

  overridePendingChange: true;
  loaded = false;

  filterMap: { [key: string]: any[] } = {
    status: ['ACTIVE', 'INACTIVE']
  };

  orderBy = 'Name';
  direction = Direction.ASCENDANT;
  genericUserRole: Readonly<{ appId: ApplicationId, tenantIdentifier: number, roles: Role[] }>;

  private groups: Array<{ id: string, group: any }> = [];
  private readonly filterChange = new Subject<{ [key: string]: any[] }>();
  private readonly searchChange = new Subject<string>();
  private readonly orderChange = new Subject<string>();

  @Input()
  get connectedUserInfo(): AdminUserProfile {
    return this._connectedUserInfo;
  }

  set connectedUserInfo(userInfo: AdminUserProfile) {
    this._connectedUserInfo = userInfo;
  }

  // tslint:disable-next-line:variable-name
  private _connectedUserInfo: AdminUserProfile;

  constructor(
    public contextService: ContextService,
    /*   private groupApiService: GroupApiService,
       @Inject(LOCALE_ID) private locale: string,*/
    private authService: AuthService
  ) {
    super(contextService);
    this.genericUserRole = {
      appId: ApplicationId.USERS_APP,
      tenantIdentifier: +this.authService.user.proofTenantIdentifier,
      roles: [Role.ROLE_GENERIC_USERS]
    };
  }

  ngOnInit() {
    this.contextService.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT))
      .subscribe((data: Context[]) => {
        this.dataSource = data;
      });

    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildContextCriteriaFromSearch();
      console.log('query: ', query);
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
  }

  buildContextCriteriaFromSearch() {
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

  ngOnDestroy() {
    this.updatedData.unsubscribe();
  }

  getGroup(user: User) {
    const userGroup = this.groups.find((group) => group.id === user.groupId);

    return userGroup ? userGroup.group : undefined;
  }

  searchContextOrdered() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  emitOrderChange() {
    console.log('Order change !');
    this.orderChange.next();
  }

  onFilterChange(key: string, values: any[]) {
    this.filterMap[key] = values;
    this.filterChange.next(this.filterMap);
  }


}
