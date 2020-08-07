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
import { SecurityProfile } from 'projects/vitamui-library/src/lib/models/security-profile';
import { merge, Subject } from 'rxjs';
import { debounceTime } from 'rxjs/operators';
import {
  AdminUserProfile,
  ApplicationId,
  AuthService,
  DEFAULT_PAGE_SIZE,
  Direction,
  InfiniteScrollTable,
  PageRequest,
  Role
} from 'ui-frontend-common';
import { SecurityProfileService } from '../security-profile.service';

const FILTER_DEBOUNCE_TIME_MS = 400;

@Component({
  selector: 'app-security-profile-list',
  templateUrl: './security-profile-list.component.html',
  styleUrls: ['./security-profile-list.component.scss']
})
export class SecurityProfileListComponent extends InfiniteScrollTable<SecurityProfile> implements OnDestroy, OnInit {
  // tslint:disable-next-line:no-input-rename
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  // tslint:disable-next-line:variable-name
  private _searchText: string;

  @Output() contextClick = new EventEmitter<SecurityProfile>();

  @ViewChild('filterTemplate', {static: false}) filterTemplate: TemplateRef<SecurityProfileListComponent>;
  @ViewChild('filterButton', {static: false}) filterButton: ElementRef;

  overridePendingChange: true;
  loaded = false;
  statusFilter: string[] = [];
  filterMap: { [key: string]: any[] } = {
    status: ['ENABLED', 'BLOCKED'],
    level: null,
    group: null,
  };
  groupFilterOptions: Array<{ value: string, label: string }> = [];
  levelFilterOptions: Array<{ value: string, label: string }> = [];
  orderBy = 'Name';
  direction = Direction.ASCENDANT;
  genericUserRole: Readonly<{ appId: ApplicationId, tenantIdentifier: number, roles: Role[] }>;

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
    public securityProfileService: SecurityProfileService,
    private authService: AuthService
  ) {
    super(securityProfileService);
    this.genericUserRole = {
      appId: ApplicationId.USERS_APP,
      tenantIdentifier: +this.authService.user.proofTenantIdentifier,
      roles: [Role.ROLE_GENERIC_USERS]
    };
  }

  ngOnInit() {
    this.securityProfileService.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT))
      .subscribe((data: SecurityProfile[]) => {
        this.dataSource = data;
      });

    const searchCriteriaChange = merge(this.searchChange, this.filterChange, this.orderChange)
      .pipe(debounceTime(FILTER_DEBOUNCE_TIME_MS));

    searchCriteriaChange.subscribe(() => {
      const query: any = this.buildSecurityProfileCriteriaFromSearch();
      const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
      this.search(pageRequest);
    });
  }

  buildSecurityProfileCriteriaFromSearch() {
    const criteria: any = {};
    if (this._searchText.length > 0) {
      criteria.Name = this._searchText;
      criteria.Identifier = this._searchText;
    }
    return criteria;
  }

  ngOnDestroy() {
    this.updatedData.unsubscribe();
  }

  searchSecurityProfileOrdered() {
    this.search(new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, Direction.ASCENDANT));
  }

  emitOrderChange() {
    this.orderChange.next();
  }

  deleteSecurityProfileDialog(profile: SecurityProfile) {
    this.securityProfileService.delete(profile).subscribe(
      () => {
        this.searchSecurityProfileOrdered();
      }
    );
  }

}
