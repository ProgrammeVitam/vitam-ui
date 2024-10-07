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
import { finalize, Subscription } from 'rxjs';
import { FileService } from '../core/services/file.service';
import { ToggleSidenavService } from '../core/services/toggle-sidenav.service';
import { FileNode, FileNodeInsertAttributeParams, FileNodeInsertParams } from '../models/file-node';
import { ProfileDescription } from '../models/profile-description.model';
import { ProfileResponse } from '../models/profile-response';
import { EditProfileComponent } from '../profile/edit-profile/edit-profile.component';
import { ProfileService } from '../core/services/profile.service';
import { SedaService } from '../core/services/seda.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { tap } from 'rxjs/operators';

@Component({
  selector: 'app-home',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
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

  uploadedProfileSelected: ProfileDescription;

  private _routeParamsSubscription: Subscription;

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
    this.uploadedProfileResponse = this.router.getCurrentNavigation().extras.state as ProfileResponse;
    this.uploadedProfileSelected = this.router.getCurrentNavigation().extras.state as ProfileDescription;

    this.sideNavService.isOpened.subscribe((status) => {
      this.opened = status;
    });
    this.pendingSub = this.sideNavService.isPending.subscribe((status) => {
      this.pending = status;
    });
  }

  ngOnInit() {
    this.fileService.currentTreeLoaded = false;
    this.toastrService.overlayContainer = this.toastContainer;
    this._routeParamsSubscription = this.route.params.subscribe((params) => {
      const profileId = params.id;
      // If a profileId has been defined, it is retrieved from backend
      if (profileId !== undefined) {
        if (this.uploadedProfileSelected === undefined) {
          this.router.navigate(['/pastis/tenant/1'], { skipLocationChange: false });
        } else {
          this.fileService.getProfileAndUpdateTree(this.uploadedProfileSelected);
        }
      } else {
        // Otherwise we must have an user uploaded profile
        this.uploadedProfileResponse.id = null;
        this.uploadedProfileResponse.name = 'Nouveau Profil';

        this.loaderService.start();
        this.profileService
          .getMetaModel(this.uploadedProfileResponse.sedaVersion)
          .pipe(
            tap((metaModel) => {
              this.sedaService.setMetaModel(metaModel);
              this.fileService.linkFileNodeToSedaData(null, [this.uploadedProfileResponse.profile]);
              this.fileService.updateTreeWithProfile(this.uploadedProfileResponse);
            }),
            finalize(() => this.loaderService.stop()),
          )
          .subscribe();
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
    if (this.pendingSub) this.pendingSub.unsubscribe();
  }
}
