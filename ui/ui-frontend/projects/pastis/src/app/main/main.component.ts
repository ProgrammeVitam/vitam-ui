/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
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
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastContainerDirective, ToastrService } from 'ngx-toastr';
import { finalize, map, Subscription, switchMap } from 'rxjs';
import { FileService } from '../core/services/file.service';
import { ToggleSidenavService } from '../core/services/toggle-sidenav.service';
import { FileNode, FileNodeInsertAttributeParams, FileNodeInsertParams } from '../models/file-node';
import { ProfileResponse } from '../models/profile-response';
import { EditProfileComponent } from '../profile/edit-profile/edit-profile.component';
import { ProfileService } from '../core/services/profile.service';
import { SedaService } from '../core/services/seda.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { tap } from 'rxjs/operators';
import { ProfileType } from '../models/profile-type.enum';
import { ProfileVersion } from '../models/profile-version.enum';

@Component({
  selector: 'app-home',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
  standalone: false,
})
export class MainComponent implements OnInit, OnDestroy {
  @ViewChild('treeSelector', { static: true }) tree: any;
  @ViewChild('autosize', { static: false }) autosize: CdkTextareaAutosize;
  @ViewChild(ToastContainerDirective, { static: true })
  toastContainer: ToastContainerDirective;
  @ViewChild(EditProfileComponent)
  editProfileComponent: EditProfileComponent;

  opened: boolean;
  pending: boolean;
  pendingSub: Subscription;
  events: string[] = [];

  uploadedProfileResponse: ProfileResponse;
  uploadedProfileByFile: ProfileResponse;

  private _routeParamsSubscription: Subscription;
  private _profileLoadingSubscription: Subscription;

  constructor(
    public fileService: FileService,
    private route: ActivatedRoute,
    private sideNavService: ToggleSidenavService,
    private toastrService: ToastrService,
    private profileService: ProfileService,
    private sedaService: SedaService,
    private loaderService: NgxUiLoaderService,
    private router: Router,
  ) {
    this.sideNavService.isOpened.subscribe((status) => {
      this.opened = status;
    });
    this.pendingSub = this.sideNavService.isPending.subscribe((status) => {
      this.pending = status;
    });
    const navigation = this.router.getCurrentNavigation();
    this.uploadedProfileByFile = navigation?.extras.state?.payload;
  }

  ngOnInit() {
    this.fileService.currentTreeLoaded = false;
    this.toastrService.overlayContainer = this.toastContainer;
    this._routeParamsSubscription = this.route.params.subscribe((params) => {
      const profileId = params.id;

      // If a profileId has been defined, it is retrieved from backend
      if (profileId !== undefined) {
        this.loadProfileById(profileId);
      } else {
        // Check for query params to create a new profile
        this.route.queryParams.subscribe((queryParams) => {
          if (this.uploadedProfileByFile !== undefined) {
            this.uploadNewProfile();
          } else if (queryParams['type'] && queryParams['version']) {
            const type: ProfileType = queryParams?.type;
            const version: ProfileVersion = queryParams?.version;
            this.createNewProfile(type, version);
          } else {
            // No valid params, redirect to list
            this.router.navigate(['/'], { skipLocationChange: false });
          }
        });
      }
    });
    this.opened = true;
  }

  openSideNav() {
    this.opened = true;
    this.sideNavService.show();
  }

  insertionItem($event: FileNodeInsertParams) {
    const names: string[] = $event.elementsToAdd.map((e) => e.name);
    this.editProfileComponent.fileTreeComponent.insertItem($event.node, names);
    console.log('Params : ', $event);
  }

  addNode($event: FileNode) {
    this.editProfileComponent.fileTreeComponent.add($event);
  }

  insertAttribute($event: FileNodeInsertAttributeParams) {
    console.log('Params in attributes : ', $event);
    this.editProfileComponent.fileTreeComponent.insertAttributes($event.node, $event.elementsToAdd);
  }

  removeNode($event: FileNode) {
    this.editProfileComponent.fileTreeComponent.remove($event);
  }

  duplicateNode($event: FileNode) {
    this.editProfileComponent.fileTreeComponent.duplicate($event);
  }

  ngOnDestroy(): void {
    if (this._routeParamsSubscription != null) {
      this._routeParamsSubscription.unsubscribe();
    }
    if (this._profileLoadingSubscription != null) {
      this._profileLoadingSubscription.unsubscribe();
    }
    if (this.pendingSub) this.pendingSub.unsubscribe();
  }

  private loadProfileById(profileId: string) {
    // Unsubscribe from previous profile loading to avoid multiple concurrent requests
    if (this._profileLoadingSubscription != null) {
      this._profileLoadingSubscription.unsubscribe();
    }

    // Subscribe to profiles list
    this._profileLoadingSubscription = this.profileService.retrievedProfiles.subscribe((profiles) => {
      if (!profiles || profiles.length === 0) {
        // Profiles not loaded yet, refresh the list
        this.profileService.refreshListProfiles();
        return;
      }

      // Find the profile in the list
      const profileDescription = profiles.find((p) => p.id === profileId);
      if (profileDescription) {
        this.fileService.getProfileAndUpdateTree(profileDescription);
      } else {
        this.router.navigate(['/'], { skipLocationChange: false });
      }
    });
  }

  private createNewProfile(profileType: ProfileType, profileVersion: ProfileVersion) {
    this.loaderService.start();
    this.profileService
      .createProfile(profileType, profileVersion)
      .pipe(
        tap((profileResponse) => {
          this.uploadedProfileResponse = profileResponse;
          this.uploadedProfileResponse.id = null;
        }),
        switchMap((profileResponse) =>
          this.profileService.getMetaModel(profileResponse.sedaVersion).pipe(map((metaModel) => ({ profileResponse, metaModel }))),
        ),
        tap(({ profileResponse, metaModel }) => {
          this.sedaService.setMetaModel(metaModel);
          this.fileService.linkFileNodeToSedaData(null, [profileResponse.profile]);
          this.fileService.updateTreeWithProfile(profileResponse);
        }),
        finalize(() => this.loaderService.stop()),
      )
      .subscribe();
  }

  private uploadNewProfile() {
    this.loaderService.start();
    this.profileService
      .getMetaModel(this.uploadedProfileByFile.sedaVersion)
      .pipe(
        map((metaModel) => {
          this.sedaService.setMetaModel(metaModel);
          this.fileService.linkFileNodeToSedaData(null, [this.uploadedProfileByFile.profile]);
          this.fileService.updateTreeWithProfile(this.uploadedProfileByFile);
        }),
        finalize(() => this.loaderService.stop()),
      )
      .subscribe();
  }
}
