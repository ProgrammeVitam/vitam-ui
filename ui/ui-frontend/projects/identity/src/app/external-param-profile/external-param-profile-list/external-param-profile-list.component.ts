import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import {
  buildCriteriaFromSearch,
  DEFAULT_PAGE_SIZE,
  Direction,
  ExternalParamProfile,
  InfiniteScrollTable,
  PageRequest,
  Profile,
  SearchQuery,
} from 'ui-frontend-common';
import { ProfileService } from '../../profile/profile.service';
import { ExternalParamProfileService } from '../external-param-profile.service';
import { SharedService } from '../shared.service';
import { debounceTime, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-external-param-profile-list',
  templateUrl: './external-param-profile-list.component.html',
  styleUrls: ['./external-param-profile-list.component.css'],
})
export class ExternalParamProfileListComponent extends InfiniteScrollTable<ExternalParamProfile> implements OnDestroy, OnInit {
  orderBy = 'name';
  direction = Direction.ASCENDANT;
  private updatedProfileSub: Subscription;
  private searchCriteriaSub: Subscription;
  private _searchText: string;
  private readonly searchChange = new Subject<string>();
  private readonly searchKeys = ['name'];
  private readonly filterDebounceTimeMs = 400;

  @Output() externalParamProfileClick = new EventEmitter<ExternalParamProfile>();
  @Input('search')
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  constructor(
    public externalParamProfileServiceService: ExternalParamProfileService,
    private profileService: ProfileService,
    private sharedService: SharedService
  ) {
    super(externalParamProfileServiceService);
  }

  ngOnInit() {
    this.searchCriteriaSub = this.searchChange
      .pipe(startWith(null), debounceTime(this.filterDebounceTimeMs))
      .subscribe(() => this.search());
    this.updatedProfileSub = this.externalParamProfileServiceService.updated.subscribe((externalParamProfile: ExternalParamProfile) => {
      const extParamProfileIndex = this.dataSource.findIndex((extParamProfile) => extParamProfile.id === externalParamProfile.id);
      if (extParamProfileIndex > -1) {
        this.dataSource[extParamProfileIndex] = {
          id: externalParamProfile.id,
          enabled: externalParamProfile.enabled,
          name: externalParamProfile.name,
          description: externalParamProfile.description,
          accessContract: externalParamProfile.accessContract,
          externalParamIdentifier: externalParamProfile.externalParamIdentifier,
          profileIdentifier: externalParamProfile.profileIdentifier,
          idExternalParam: externalParamProfile.idExternalParam,
          idProfile: externalParamProfile.idProfile,
        };
      }
    });
  }

  ngOnDestroy() {
    this.updatedProfileSub.unsubscribe();
    this.searchCriteriaSub.unsubscribe();
  }

  emitExternalProfile(externalParamProfile: ExternalParamProfile) {
    this.profileService.get('' + externalParamProfile.idProfile).subscribe((profile: Profile) => {
      this.sharedService.emitSourceProfile(profile);
      this.sharedService.emitReadOnly(profile.readonly);
      this.externalParamProfileClick.emit(externalParamProfile);
    });
  }

  search() {
    const query: SearchQuery = {
      criteria: [...buildCriteriaFromSearch(this._searchText, this.searchKeys)],
    };
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
    super.search(pageRequest);
  }
}
