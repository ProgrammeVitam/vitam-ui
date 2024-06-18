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
import { Subject, Subscription } from 'rxjs';
import { debounceTime, startWith } from 'rxjs/operators';
import {
  buildCriteriaFromSearch,
  CriteriaSearchQuery,
  DEFAULT_PAGE_SIZE,
  Direction,
  ExternalParamProfile,
  InfiniteScrollTable,
  PageRequest,
  Profile,
} from 'vitamui-library';
import { ProfileService } from '../../profile/profile.service';
import { ExternalParamProfileService } from '../external-param-profile.service';
import { SharedService } from '../shared.service';

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
  @Input()
  set searchText(searchText: string) {
    this._searchText = searchText;
    this.searchChange.next(searchText);
  }

  constructor(
    public externalParamProfileServiceService: ExternalParamProfileService,
    private profileService: ProfileService,
    private sharedService: SharedService,
  ) {
    super(externalParamProfileServiceService);
  }

  ngOnInit() {
    this.searchCriteriaSub = this.searchChange
      .pipe(startWith(null), debounceTime(this.filterDebounceTimeMs))
      .subscribe(() => this.search());
    this.subscribeOnExteralParamProfilePatchOperation();
  }

  ngOnDestroy() {
    this.updatedProfileSub.unsubscribe();
    this.searchCriteriaSub.unsubscribe();
  }

  private subscribeOnExteralParamProfilePatchOperation() {
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
          bulkOperationsThreshold: externalParamProfile.bulkOperationsThreshold,
          usePlatformThreshold: externalParamProfile.usePlatformThreshold,
        };
      }
    });
  }

  emitExternalProfile(externalParamProfile: ExternalParamProfile) {
    this.profileService.get('' + externalParamProfile.idProfile).subscribe((profile: Profile) => {
      this.sharedService.emitSourceProfile(profile);
      this.sharedService.emitReadOnly(profile.readonly);
      this.externalParamProfileClick.emit(externalParamProfile);
    });
  }

  search() {
    const query: CriteriaSearchQuery = {
      criteria: [...buildCriteriaFromSearch(this._searchText, this.searchKeys)],
    };
    const pageRequest = new PageRequest(0, DEFAULT_PAGE_SIZE, this.orderBy, this.direction, JSON.stringify(query));
    super.search(pageRequest);
  }
}
