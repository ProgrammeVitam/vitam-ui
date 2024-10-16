/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { Profile } from '../../models/profile';
import { ProfileService } from '../../core/services/profile.service';
import { ToggleSidenavService } from '../../core/services/toggle-sidenav.service';
import { finalize, mergeMap, Observable, of, pipe, switchMap, throwError, UnaryFunction } from 'rxjs';
import { ProfileDescription } from '../../models/profile-description.model';
import { FileNode } from '../../models/file-node';
import { ProfileType } from '../../models/profile-type.enum';
import { Injectable } from '@angular/core';

export interface FileUploadPayload {
  profile: Profile;
  profileDescription: ProfileDescription;
  data: FileNode[];
}

@Injectable({
  providedIn: 'root',
})
export class ArchiveProfileSaverService {
  private attachmentOperator: UnaryFunction<Observable<FileUploadPayload>, Observable<Profile>> = pipe(
    mergeMap((payload: FileUploadPayload) =>
      payload.profile ? of(payload) : throwError(() => new Error('No profile after action attempt')),
    ),
    switchMap((payload: FileUploadPayload) =>
      this.profileService
        .uploadFile(payload.data, payload.profileDescription, ProfileType.PA, payload.profile.sedaVersion)
        .pipe(mergeMap((data) => of({ file: new File([data], 'file'), profile: payload.profile }))),
    ),
    switchMap(({ file, profile }) => this.profileService.updateProfileFilePa(profile, file)),
  );

  constructor(
    private profileService: ProfileService,
    private toggleService: ToggleSidenavService,
  ) {}

  create(profileToCreate: Profile, profileDescription: ProfileDescription, data: FileNode[]): Observable<Profile> {
    this.toggleService.showPending();
    return this.profileService.createProfilePa(profileToCreate).pipe(
      mergeMap((profile: Profile): Observable<FileUploadPayload> => of({ profile, profileDescription, data })),
      this.attachmentOperator,
      finalize(() => this.toggleService.hidePending()),
    );
  }

  update(profileToUpdate: Profile, profileDescription: ProfileDescription, data: FileNode[]): Observable<Profile> {
    this.toggleService.showPending();
    return this.profileService.updateProfilePa(profileToUpdate).pipe(
      mergeMap((profile: Profile): Observable<FileUploadPayload> => of({ profile, profileDescription, data })),
      this.attachmentOperator,
      finalize(() => this.toggleService.hidePending()),
    );
  }

  attach(profileToAttach: Profile, profileDescription: ProfileDescription, data: FileNode[]): Observable<Profile> {
    this.toggleService.showPending();
    return of({ profile: profileToAttach, profileDescription, data }).pipe(
      this.attachmentOperator,
      finalize(() => this.toggleService.hidePending()),
    );
  }
}
