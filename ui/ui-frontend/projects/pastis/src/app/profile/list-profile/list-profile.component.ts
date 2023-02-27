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
import { Component, Input, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { FileUploader } from 'ng2-file-upload';
import { Subscription } from 'rxjs';
import { Direction, GlobalEventService, SidenavPage, StartupService } from 'ui-frontend-common';
import { environment } from '../../../environments/environment';
import { PastisConfiguration } from '../../core/classes/pastis-configuration';
import { NoticeService } from '../../core/services/notice.service';
import { ProfileService } from '../../core/services/profile.service';
import { ToggleSidenavService } from '../../core/services/toggle-sidenav.service';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { BreadcrumbDataTop } from '../../models/breadcrumb';
import { MetadataHeaders } from '../../models/models';
import { Profile } from '../../models/profile';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileMode, ProfileResponse } from '../../models/profile-response';
import { DataGeneriquePopupService } from '../../shared/data-generique-popup.service';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';
import { CreateProfileComponent } from '../create-profile/create-profile.component';
import { ProfileInformationTabComponent } from '../profile-preview/profile-information-tab/profile-information-tab/profile-information-tab.component';

const POPUP_CREATION_PATH = 'PROFILE.POP_UP_CREATION';

function constantToTranslate() {
  this.popupCreationCancelLabel = this.translated('.POPUP_CREATION_CANCEL_LABEL');
  this.popupCreationTitleDialog = this.translated('.POPUP_CREATION_TITLE_DIALOG');
  this.popupCreationSubTitleDialog = this.translated('.POPUP_CREATION_SUBTITLE_DIALOG');
  this.popupCreationOkLabel = this.translated('.POPUP_CREATION_OK_LABEL');
}

@Component({
  selector: 'pastis-list-profile',
  templateUrl: './list-profile.component.html',
  styleUrls: ['./list-profile.component.scss'],
})
export class ListProfileComponent extends SidenavPage<ProfileDescription> implements OnInit, OnDestroy {
  @ViewChild(ProfileInformationTabComponent, { static: true }) profileInformationTabComponent: ProfileInformationTabComponent;
  @Input() uploader: FileUploader = new FileUploader({ url: '' });

  retrievedProfiles: ProfileDescription[] = [];
  matDataSource: MatTableDataSource<ProfileDescription>;
  numPA: number;
  numPUA: number;
  totalProfileNum: number;
  hoveredElementId: number;
  buttonIsClicked: boolean;
  search: string;
  numProfilesFiltered: ProfileDescription[];
  profilModel: ProfileDescription;
  filterType: string;
  isStandalone: boolean = environment.standalone;
  direction = Direction.ASCENDANT;
  orderBy = 'identifier';
  sedaUrl: string =
    this.pastisConfig.pastisPathPrefix + (this.isStandalone ? '' : this.startupService.getTenantIdentifier()) + this.pastisConfig.sedaUrl;
  newProfileUrl: string = this.pastisConfig.pastisNewProfile;
  docPath = this.isStandalone
    ? 'assets/doc/Standalone - Documentation APP - PASTIS.pdf'
    : 'assets/doc/VITAM UI - Documentation APP - PASTIS.pdf';
  donnees: string[];
  promise: Promise<any>;
  expanded: boolean;
  pending: boolean;
  public breadcrumbDataTop: Array<BreadcrumbDataTop>;
  popupCreationCancelLabel: string;
  popupCreationTitleDialog: string;
  popupCreationSubTitleDialog: string;
  popupCreationOkLabel: string;
  profilesChargees = false;

  private subscriptions = new Subscription();

  constructor(
    private profileService: ProfileService,
    private noticeService: NoticeService,
    private router: Router,
    private dialog: MatDialog,
    private startupService: StartupService,
    private pastisConfig: PastisConfiguration,
    private route: ActivatedRoute,
    private dataGeneriquePopupService: DataGeneriquePopupService,
    private translateService: TranslateService,
    private toggleService: ToggleSidenavService,
    public globalEventService: GlobalEventService
  ) {
    super(route, globalEventService);
    this.expanded = false;
    this.subscriptions.add(
      this.toggleService.isPending.subscribe((status) => {
        this.pending = status;
      })
    );
  }

  ngOnInit() {
    if (!this.isStandalone) {
      constantToTranslate.call(this);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.popupCreationCancelLabel = 'Annuler';
      this.popupCreationTitleDialog = 'Choix du type de profil';
      this.popupCreationSubTitleDialog = "Création d'un profil";
      this.popupCreationOkLabel = 'VALIDER';
    }
    this.subscriptions.add(this.dataGeneriquePopupService.currentDonnee.subscribe((donnees) => (this.donnees = donnees)));
    this.breadcrumbDataTop = [
      {
        label: 'PROFILE.EDIT_PROFILE.BREADCRUMB.PORTAIL',
        url: this.startupService.getPortalUrl(),
        external: true,
      },
      { label: 'PROFILE.EDIT_PROFILE.BREADCRUMB.CREER_ET_GERER_PROFIL', url: '/' },
    ];

    this.subscriptions.add(this.refreshListProfiles());
  }

  private refreshListProfiles() {
    this.toggleService.showPending();
    this.profileService.refreshListProfiles();

    return this.profileService.retrievedProfiles.subscribe((profileList: ProfileDescription[]) => {
      if (profileList) {
        this.retrievedProfiles = profileList;
        this.profilesChargees = true;
        this.toggleService.hidePending();
      }
      this.matDataSource = new MatTableDataSource<ProfileDescription>(this.retrievedProfiles);
      this.numPA = this.retrievePAorPUA(ProfileMode.PA, false);
      this.numPUA = this.retrievePAorPUA(ProfileMode.PUA, false);
      this.totalProfileNum = this.retrievedProfiles ? this.retrievedProfiles.length : 0;
    });
  }

  translatedOnChange(): void {
    this.subscriptions.add(
      this.translateService.onLangChange.subscribe(() => {
        constantToTranslate.call(this);
      })
    );
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_PATH + nameOfFieldToTranslate);
  }

  retrievePAorPUA(term: string, filter: boolean): number {
    const profiles: ProfileDescription[] = filter === false ? this.retrievedProfiles : this.numProfilesFiltered;
    const profileNum = profiles.filter((p) => p.type === term).length;
    return profileNum ? profileNum : 0;
  }

  navigate(d: BreadcrumbDataTop) {
    if (d.external) {
      window.location.assign(d.url);
    } else {
      this.router.navigate([d.url], { skipLocationChange: false });
    }
  }

  editProfile(element: ProfileDescription) {
    this.router.navigate([this.pastisConfig.pastisEditPage, element.id], {
      state: element,
      relativeTo: this.route,
      skipLocationChange: false,
    });
  }

  uploadProfile(files: File[]): void {
    const fileToUpload: File = files[0];

    if (fileToUpload) {
      const formData = new FormData();
      formData.append('file', fileToUpload, fileToUpload.name);
      this.subscriptions.add(
        this.profileService.uploadProfile(formData).subscribe((response: any) => {
          if (response) {
            this.router.navigate([this.pastisConfig.pastisNewProfile], { state: response, relativeTo: this.route });
          }
        })
      );
    }
  }

  async createProfile() {
    const dataToSendToPopUp = {} as PastisDialogData;
    dataToSendToPopUp.titleDialog = this.popupCreationTitleDialog;
    dataToSendToPopUp.subTitleDialog = this.popupCreationSubTitleDialog;
    dataToSendToPopUp.width = '800px';
    dataToSendToPopUp.height = '800px';
    dataToSendToPopUp.okLabel = this.popupCreationOkLabel;
    dataToSendToPopUp.cancelLabel = this.popupCreationCancelLabel;
    const dialogRef = this.dialog.open(CreateProfileComponent, {
      width: '800px',
      panelClass: 'pastis-popup-modal-box',
      data: dataToSendToPopUp,
    });
    this.subscriptions.add(
      dialogRef.afterClosed().subscribe((result) => {
        const { success, action } = result || {};

        if (!success) return;

        if (action === ProfileMode.PA || action === ProfileMode.PUA) {
          this.subscriptions.add(
            this.profileService.createProfile(this.pastisConfig.createProfileByTypeUrl, action).subscribe((response: ProfileResponse) => {
              if (response) {
                this.router.navigate([this.pastisConfig.pastisNewProfile], { state: response, relativeTo: this.route });
              }
            })
          );
        }
      })
    );
  }

  public onSearchSubmit(search: string): void {
    if (!search) {
      search = '';
    }
    this.search = search;
    const profileDescriptions = this.retrievedProfiles.filter(
      (profile) =>
        profile.identifier.toLowerCase().indexOf(search.toLowerCase()) >= 0 || profile.name.toLowerCase().indexOf(search.toLowerCase()) >= 0
    );
    this.totalProfileNum = profileDescriptions.length;
    this.numPA = profileDescriptions.filter((profile: ProfileDescription) => profile.type === ProfileMode.PA).length;
    this.numPUA = profileDescriptions.filter((profile: ProfileDescription) => profile.type === ProfileMode.PUA).length;
  }

  isRowHovered(elementId: number) {
    return this.hoveredElementId === elementId;
  }

  onMouseOver(row: MetadataHeaders) {
    this.buttonIsClicked = false;
    this.hoveredElementId = row.id;
  }

  onMouseLeave() {
    if (!this.buttonIsClicked) {
      this.hoveredElementId = 0;
    }
  }

  changeType(type: string) {
    if (type !== undefined) {
      this.filterType = type;
    }
  }

  ngOnDestroy() {
    this.profileService.retrievedProfiles.next([]);
    this.subscriptions.unsubscribe();
  }

  showProfile(element: ProfileDescription) {
    if (!this.isStandalone) {
      this.openPanel(element);
    }
  }

  updateProfileNotice(profileDescription: ProfileDescription, files: File[]) {
    const fileToUpload: File = files[0];
    let profile: Profile;
    let archivalProfileUnit: ArchivalProfileUnit;
    if (profileDescription.type === ProfileMode.PA) {
      profile = this.noticeService.profileDescriptionToPaProfile(profileDescription);
      this.profileService.updateProfileFilePa(profile, fileToUpload).subscribe(() => {
        this.expanded = !this.expanded;
        this.refreshListProfiles();
      });
    }
    if (profileDescription.type === ProfileMode.PUA) {
      if (fileToUpload) {
        let jsonObj: ProfileDescription;
        const fileReader = new FileReader();
        fileReader.readAsText(fileToUpload, 'UTF-8');
        fileReader.onload = () => {
          jsonObj = JSON.parse(fileReader.result.toString());
          profileDescription.controlSchema = jsonObj.controlSchema;
          archivalProfileUnit = this.noticeService.profileDescriptionToPuaProfile(profileDescription);
          this.profileService.updateProfilePua(archivalProfileUnit).subscribe(() => {
            this.expanded = !this.expanded;
            this.refreshListProfiles();
          });
        };
        fileReader.onerror = (error) => {
          console.error(error);
        };
      }
    }
  }

  changeExpand(element: ProfileDescription) {
    if (element.type === ProfileMode.PA && !element.path && element.status === 'ACTIVE') {
      this.expanded = !this.expanded;
    }
    if (element.type === ProfileMode.PUA && element.status === 'ACTIVE' && (element.controlSchema === '{}' || !element.controlSchema)) {
      this.expanded = !this.expanded;
    }
  }
}
