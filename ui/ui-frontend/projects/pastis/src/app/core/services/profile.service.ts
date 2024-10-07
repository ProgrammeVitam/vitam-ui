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
import { cloneDeep } from 'lodash-es';
import { BehaviorSubject, combineLatest, filter, from, mergeMap, Observable, pipe, Subscription, toArray } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { FileNode } from '../../models/file-node';
import { Profile } from '../../models/profile';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileResponse } from '../../models/profile-response';
import { ProfileType } from '../../models/profile-type.enum';
import { ProfileVersion, ProfileVersionOptions } from '../../models/profile-version.enum';
import { SedaData } from '../../models/seda-data';
import { PastisApiService } from '../api/api.pastis.service';
import { PastisConfiguration } from '../classes/pastis-configuration';
import { ArchivalProfileUnitApiService } from './archival-profile-unit-api.service';
import { ArchiveProfileApiService } from './archive-profile-api.service';

@Injectable({
  providedIn: 'root',
})
export class ProfileService implements OnDestroy {
  public profileType: ProfileType;
  public profileVersion: ProfileVersion;
  public profileName: string;
  public profileId: string;
  public retrievedProfiles = new BehaviorSubject<ProfileDescription[]>(null);
  protected data: ProfileDescription[];

  private subscriptions = new Subscription();
  private setProfileTypeOperator = pipe(
    filter((profiles: ProfileDescription[]) => Boolean(profiles)),
    mergeMap((profiles) => from(profiles)),
    map((profile) => ({ ...profile, type: profile.controlSchema ? ProfileType.PUA : ProfileType.PA })),
    toArray(),
  );

  constructor(
    private apiService: PastisApiService,
    private pastisConfig: PastisConfiguration,
    private puaService: ArchivalProfileUnitApiService,
    private paService: ArchiveProfileApiService,
  ) {}

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  getAllProfiles(): void {
    if (environment.standalone) return this.getAllProfilesStandalone();

    return this.getAllProfilesVitam();
  }

  refreshListProfiles(): void {
    this.getAllProfiles();
  }

  getStandaloneProfiles(): Observable<ProfileDescription[]> {
    return this.apiService.get<ProfileDescription[]>(this.pastisConfig.getAllProfilesUrl).pipe(this.setProfileTypeOperator);
  }

  getAllProfilesPA(profileVersion?: ProfileVersion): Observable<ProfileDescription[]> {
    const options = {
      params: new HttpParams().set('embedded', 'ALL'),
    };

    if (profileVersion) {
      options.params = options.params.set('criteria', JSON.stringify({ SedaVersion: profileVersion }));
    }

    return this.apiService.get<ProfileDescription[]>(this.pastisConfig.getAllArchivalProfileUrl, options).pipe(this.setProfileTypeOperator);
  }

  getAllProfilesPUA(profileVersion?: ProfileVersion): Observable<ProfileDescription[]> {
    const options = {
      params: new HttpParams().set('embedded', 'ALL'),
    };

    if (profileVersion) {
      options.params = options.params.set('criteria', JSON.stringify({ SedaVersion: profileVersion }));
    }

    return this.apiService
      .get<ProfileDescription[]>(this.pastisConfig.getArchivalProfileUnitUrl, options)
      .pipe(this.setProfileTypeOperator);
  }

  getAllProfilesStandalone(): void {
    this.getStandaloneProfiles()
      .pipe(this.setProfileTypeOperator)
      .subscribe((profiles: ProfileDescription[]) => this.retrievedProfiles.next(profiles));
  }

  getAllProfilesVitam(): void {
    this.subscriptions.add(
      combineLatest([this.getAllProfilesPUA(), this.getAllProfilesPA()])
        .pipe(map(([archiveUnitProfiles, archivalProfiles]: ProfileDescription[][]) => [...archiveUnitProfiles, ...archivalProfiles]))
        .subscribe((profiles) => this.retrievedProfiles.next(profiles)),
    );
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
  uploadFile(file: FileNode[], notice: ProfileDescription, profileType: ProfileType, profileVersion: ProfileVersion): Observable<Blob> {
    const httpOptions: any = {
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
    } else {
      httpOptions.params = new HttpParams().set('version', profileVersion);
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

  checkPuaProfile(profile: ArchivalProfileUnit, headers?: HttpHeaders): Observable<boolean> {
    return this.puaService.check(profile, headers);
  }

  checkPaProfile(profile: Profile, headers?: HttpHeaders): Observable<boolean> {
    return this.paService.check(profile, headers);
  }

  createProfile(path: string, type: ProfileType, version: ProfileVersion): Observable<ProfileResponse> {
    const params = new HttpParams().set('type', type).set('version', version);
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

  getMetaModel(version: ProfileVersion): Observable<SedaData> {
    const params = new HttpParams().set('version', version);
    return this.apiService.get<SedaData>(this.pastisConfig.metaModelUrl, { params });
  }

  getSedaVersionLabel(): string | undefined {
    return ProfileVersionOptions.filter((profileVersionOption) => profileVersionOption.version === this.profileVersion)
      .map((profileVersionOption) => profileVersionOption.label)
      .shift();
  }

  isMode(mode: ProfileType): boolean {
    return this.profileType === mode;
  }
}
