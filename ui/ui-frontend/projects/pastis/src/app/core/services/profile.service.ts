/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable, OnDestroy } from '@angular/core';
import { cloneDeep } from 'lodash';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { map } from 'rxjs/operators';
import { PageRequest, PaginatedResponse } from 'ui-frontend-common';
import { environment } from '../../../environments/environment';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { FileNode } from '../../models/file-node';
import { Profile } from '../../models/profile';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileResponse } from '../../models/profile-response';
import { ProfileType } from '../../models/profile-type.enum';
import { PastisApiService } from '../api/api.pastis.service';
import { PastisConfiguration } from '../classes/pastis-configuration';
import { ArchivalProfileUnitApiService } from './archival-profile-unit-api.service';
import { ArchiveProfileApiService } from './archive-profile-api.service';

@Injectable({
  providedIn: 'root',
})
export class ProfileService implements OnDestroy {
  public page: number;
  public size: number;
  public orderBy: string;
  public direction: string;
  public criteria?: string;

  public profileMode: ProfileType;
  public profileName: string;
  public profileId: string;
  protected pageRequest: PageRequest;
  public retrievedProfiles = new BehaviorSubject<ProfileDescription[]>(null);
  protected data: ProfileDescription[];
  protected hasMore: boolean;

  subscription1$: Subscription;
  subscription2$: Subscription;
  subscription3$: Subscription;
  subscription4$: Subscription;
  subscriptions: Subscription[] = [];

  constructor(
    private apiService: PastisApiService,
    private pastisConfig: PastisConfiguration,
    private puaService: ArchivalProfileUnitApiService,
    private paService: ArchiveProfileApiService,
  ) {}

  ngOnDestroy(): void {
    this.subscriptions.forEach((subscriptions) => subscriptions.unsubscribe());
  }

  getAllProfiles(): void {
    if (environment.standalone) {
      this.getAllProfilesStandalone();
    } else {
      this.getAllProfilesVitam();
    }
  }

  refreshListProfiles(): void {
    this.getAllProfiles();
  }

  getStandaloneProfiles(): Observable<ProfileDescription[]> {
    return this.apiService.get(this.pastisConfig.getAllProfilesUrl);
  }

  getAllProfilesPA(): Observable<ProfileDescription[]> {
    let allProfilesPA: any;
    const params = new HttpParams().set('embedded', 'ALL');
    // @ts-ignore
    allProfilesPA = this.apiService.get(this.pastisConfig.getAllArchivalProfileUrl, { params });
    return allProfilesPA;
  }

  getAllProfilesPUA(): Observable<ProfileDescription[]> {
    let allProfilesPUA: any;
    const params = new HttpParams().set('embedded', 'ALL');

    allProfilesPUA = this.apiService.get(this.pastisConfig.getArchivalProfileUnitUrl, { params });
    return allProfilesPUA;
  }

  getAllProfilesStandalone(): void {
    this.getStandaloneProfiles().subscribe((profiles: ProfileDescription[]) => {
      if (profiles) {
        for (const profile of profiles) {
          if (profile.controlSchema) {
            profile.type = ProfileType.PUA;
          } else {
            profile.type = ProfileType.PA;
          }
        }
        this.retrievedProfiles.next(profiles);
      }
    });
  }

  getAllProfilesVitam(): void {
    const profiles: ProfileDescription[] = [];
    this.subscription3$ = this.getAllProfilesPUA().subscribe((profileListPUA: ProfileDescription[]) => {
      if (profileListPUA) {
        profileListPUA.forEach((p) => {
          p.type = ProfileType.PUA;
          profiles.push(p);
        });
        this.subscription4$ = this.getAllProfilesPA().subscribe((profileListPA: ProfileDescription[]) => {
          if (profileListPA) {
            profileListPA.forEach((p) => {
              p.type = ProfileType.PA;
              profiles.push(p);
            });
            this.retrievedProfiles.next(profiles);
          }
        });
      }
    });
  }

  getProfile(element: ProfileDescription): Observable<ProfileResponse> {
    return this.apiService.post<ProfileResponse>(this.pastisConfig.editProfileUrl, element, {});
  }

  // Upload a RNG or a JSON file (PA or PUA, respectively) to the server
  // Response : a JSON object
  uploadProfile(profile: FormData): Observable<ProfileResponse> {
    return this.apiService.post(this.pastisConfig.uploadProfileUrl, profile);
  }

  // Send the modified tree as post,
  // Expects a RNG or a JSON file depending on the profile type
  uploadFile(file: FileNode[], notice: ProfileDescription, profileType: string): Observable<Blob> {
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-type': 'application/json',
      }),
      responseType: 'blob',
    };
    let profile: any = cloneDeep(file[0]);

    const endPointUrl = profileType === ProfileType.PA ? this.pastisConfig.savePAasFileUrl : this.pastisConfig.savePUAasFileUrl;
    this.fixCircularReference(profile);

    if (profileType === ProfileType.PUA) {
      profile = { elementProperties: profile, notice };
    }

    return this.apiService.post(endPointUrl, profile, httpOptions);
  }

  fixCircularReference(node: FileNode) {
    node.parent = null;
    node.sedaData = null;
    node.children.forEach((child) => {
      this.fixCircularReference(child);
    });
  }

  // @ts-ignore
  getAllProfilesPAPaginated(pageRequest: PageRequest): Observable<ProfileDescription[]> {
    this.page = pageRequest.page;
    this.size = pageRequest.size;
    this.direction = pageRequest.direction;

    const params = new HttpParams().set('page', this.page.toString()).set('size', this.size.toString()).set('direction', this.direction);
    let allProfilesPA: any;
    allProfilesPA = this.apiService.get(this.pastisConfig.getProfilePaginatedUrl, { params }).pipe(
      map((paginated: PaginatedResponse<ProfileDescription>) => {
        this.data = paginated.values;
        this.page = paginated.pageNum;
        this.hasMore = paginated.hasMore;
        return this.data;
      }),
    );
    return allProfilesPA;
  }

  // @ts-ignore
  getAllProfilesPUAPaginated(pageRequest: PageRequest): Observable<ProfileDescription[]> {
    this.page = pageRequest.page;
    this.size = pageRequest.size;
    this.direction = pageRequest.direction;

    const params = new HttpParams().set('page', this.page.toString()).set('size', this.size.toString()).set('direction', this.direction);
    let allProfilesPUA: any;
    allProfilesPUA = this.apiService.get(this.pastisConfig.getArchivalProfileUnitPaginatedUrl, { params }).pipe(
      map((paginated: PaginatedResponse<ProfileDescription>) => {
        this.data = paginated.values;
        this.page = paginated.pageNum;
        this.hasMore = paginated.hasMore;

        return this.data;
      }),
    );
    return allProfilesPUA;
  }

  getAllProfilesPaginated(pageRequest: PageRequest): Observable<ProfileDescription[]> {
    const tabVide: ProfileDescription[] = [];

    this.subscription2$ = this.getAllProfilesPAPaginated(pageRequest).subscribe((data: ProfileDescription[]) => {
      if (data) {
        data.forEach((p) => (p.type = ProfileType.PA));
        data.forEach((p) => tabVide.push(p));
        this.retrievedProfiles.next(data);
      }
    });

    this.subscription1$ = this.getAllProfilesPUAPaginated(pageRequest).subscribe((data: ProfileDescription[]) => {
      // @ts-ignore
      if (data) {
        data.forEach((p) => (p.type = ProfileType.PUA));
        this.retrievedProfiles.next(data);
      }
    });

    this.subscriptions.push(this.subscription1$);
    this.subscriptions.push(this.subscription2$);

    // console.log(this.retrievedProfiles + ' tableau gell all profiles Paginated');
    return this.retrievedProfiles;
  }

  getPuaProfile(id: string, headers?: HttpHeaders): Observable<ArchivalProfileUnit> {
    return this.puaService.getOne(id, headers);
  }

  checkPuaProfile(profile: ArchivalProfileUnit, headers?: HttpHeaders): Observable<boolean> {
    return this.puaService.check(profile, headers);
  }

  getPaProfile(id: string, headers?: HttpHeaders): Observable<Profile> {
    return this.paService.getOne(id, headers);
  }

  checkPaProfile(profile: Profile, headers?: HttpHeaders): Observable<boolean> {
    return this.paService.check(profile, headers);
  }

  createProfile(path: string, type: string, sedaVersion: string): Observable<ProfileResponse> {
    const params = new HttpParams().set('type', type).set('sedaVersion', sedaVersion);
    return this.apiService.get<ProfileResponse>(path, { params });
  }

  createProfilePa(profile: Profile) {
    return this.paService.create(profile);
  }

  createArchivalUnitProfile(archivalUnitProfile: ArchivalProfileUnit) {
    return this.puaService.create(archivalUnitProfile);
  }

  updateProfilePa(profile: Profile) {
    return this.paService.updateProfilePa(profile);
  }

  updateProfilePua(archivalUnitProfile: ArchivalProfileUnit) {
    return this.puaService.updateProfilePua(archivalUnitProfile);
  }

  updateProfileFilePa(profile: Profile, file: File) {
    const formData = new FormData();
    formData.append('file', file, profile.name + '.rng');
    return this.paService.uploadProfileArchivageFile(profile.identifier, formData);
  }

  downloadProfilePaVitam(id: string) {
    return this.paService.download(id);
  }
}
