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
import { Component, OnDestroy, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { filter, of, Subscription, switchMap } from 'rxjs';
import { Direction, GlobalEventService, SidenavPage, StartupService } from 'vitamui-library';
import { environment } from '../../../environments/environment';
import { PastisConfiguration } from '../../core/classes/pastis-configuration';
import { ProfileService } from '../../core/services/profile.service';
import { ToggleSidenavService } from '../../core/services/toggle-sidenav.service';
import { BreadcrumbDataTop } from '../../models/breadcrumb';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileResponse } from '../../models/profile-response';
import { DataGeneriquePopupService } from '../../shared/data-generique-popup.service';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';
import { CreateProfileComponent, CreateProfileFormResult } from '../create-profile/create-profile.component';
import { ProfileInformationTabComponent } from '../profile-preview/profile-information-tab/profile-information-tab/profile-information-tab.component';
import { ProfileType } from '../../models/profile-type.enum';
import { LoadProfileComponent, LoadProfileConfig } from './load-profile/load-profile.component';
import { Profile } from '../../models/profile';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { NoticeService } from '../../core/services/notice.service';
import { MatDialogConfig } from '@angular/material/dialog';

const POPUP_CREATION_PATH = 'PROFILE.POP_UP_CREATION';
const POPUP_UPLOAD_PATH = 'PROFILE.POP_UP_UPLOAD_FILE';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-list-profile',
  templateUrl: './list-profile.component.html',
  styleUrls: ['./list-profile.component.scss'],
})
export class ListProfileComponent extends SidenavPage<ProfileDescription> implements OnInit, OnDestroy {
  @ViewChild(ProfileInformationTabComponent, { static: true }) profileInformationTabComponent: ProfileInformationTabComponent;

  @ViewChild('confirmReplacement') confirmReplacement: TemplateRef<any>;

  retrievedProfiles: ProfileDescription[] = [];

  matDataSource: MatTableDataSource<ProfileDescription>;

  numPA: number;

  numPUA: number;

  totalProfileNum: number;

  search: string;

  numProfilesFiltered: ProfileDescription[];

  filterType: string;

  isStandalone: boolean = environment.standalone;

  direction = Direction.ASCENDANT;

  orderBy = 'identifier';

  sedaUrl: string =
    this.pastisConfig.pastisPathPrefix + (this.isStandalone ? '' : this.startupService.getTenantIdentifier()) + this.pastisConfig.sedaUrl;

  newProfileUrl: string = this.pastisConfig.pastisNewProfile;

  subscription1$: Subscription;
  _uploadProfileSub: Subscription;
  subscriptions: Subscription[] = [];

  donnees: string[];

  promise: Promise<any>;

  expanded: number;

  pending: boolean;

  pendingSub: Subscription;

  public breadcrumbDataTop: Array<BreadcrumbDataTop>;

  protected translations = {
    popupCreationCancelLabel: 'ANNULER',
    popupCreationTitleDialog: 'Choix du type de profil',
    popupCreationSubTitleDialog: "Création d'un profil",
    popupCreationOkLabel: 'VALIDER',
    popupUploadTitle: 'Charger un profil',
    popupUploadSubTitle: 'Téléchargement du profil',
    popupUploadCancelLabel: 'ANNULER',
    popupUploadOkLabel: 'CONFIRMER',
  };

  profilesChargees = false;

  constructor(
    private profileService: ProfileService,
    private noticeService: NoticeService,
    private router: Router,
    private dialog: MatDialog,
    private startupService: StartupService,
    private pastisConfig: PastisConfiguration,
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private dataGeneriquePopupService: DataGeneriquePopupService,
    private translateService: TranslateService,
    private toggleService: ToggleSidenavService,
  ) {
    super(route, globalEventService);
    this.pendingSub = this.toggleService.isPending.subscribe((status) => {
      this.pending = status;
    });
  }

  ngOnInit() {
    if (!this.isStandalone) {
      this.loadTranslations();
    }
    this.dataGeneriquePopupService.currentDonnee.subscribe((donnees) => (this.donnees = donnees));
    this.breadcrumbDataTop = [
      {
        label: 'PROFILE.EDIT_PROFILE.BREADCRUMB.PORTAIL',
        url: this.startupService.getPortalUrl(),
        external: true,
      },
      { label: 'PROFILE.EDIT_PROFILE.BREADCRUMB.CREER_ET_GERER_PROFIL', url: '/' },
    ];

    this.subscription1$ = this.refreshListProfiles();
    this.subscriptions.push(this.subscription1$);
  }

  private loadTranslations() {
    this.loadTranslation('popupCreationCancelLabel', `${POPUP_CREATION_PATH}.POPUP_CREATION_CANCEL_LABEL`);
    this.loadTranslation('popupCreationTitleDialog', `${POPUP_CREATION_PATH}.POPUP_CREATION_TITLE_DIALOG`);
    this.loadTranslation('popupCreationSubTitleDialog', `${POPUP_CREATION_PATH}.POPUP_CREATION_SUBTITLE_DIALOG`);
    this.loadTranslation('popupCreationOkLabel', `${POPUP_CREATION_PATH}.POPUP_CREATION_OK_LABEL`);
    this.loadTranslation('popupUploadTitle', `${POPUP_UPLOAD_PATH}.POPUP_UPLOAD_TITLE_LABEL`);
    this.loadTranslation('popupUploadSubTitle', `${POPUP_UPLOAD_PATH}.POPUP_UPLOAD_SUBTITLE_LABEL`);
    this.loadTranslation('popupUploadCancelLabel', `${POPUP_UPLOAD_PATH}.POPUP_UPLOAD_CANCEL_LABEL`);
    this.loadTranslation('popupUploadOkLabel', `${POPUP_UPLOAD_PATH}.POPUP_UPLOAD_OK_LABEL`);
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
      this.numPA = this.retrievePAorPUA(ProfileType.PA, false);
      this.numPUA = this.retrievePAorPUA(ProfileType.PUA, false);
      this.totalProfileNum = this.retrievedProfiles ? this.retrievedProfiles.length : 0;
    });
  }

  private loadTranslation(key: keyof typeof this.translations, nameOfFieldToTranslate: string) {
    this.translateService.get(nameOfFieldToTranslate).subscribe((t) => (this.translations[key] = t));
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
      this._uploadProfileSub = this.profileService.uploadProfile(formData).subscribe((response: any) => {
        if (response) {
          this.router.navigate([this.pastisConfig.pastisNewProfile], { state: response, relativeTo: this.route });
        }
      });
      this.subscriptions.push(this._uploadProfileSub);
    }
  }

  createProfile() {
    const createProfileDialogConfig: MatDialogConfig<PastisDialogData> = {
      width: '800px',
      panelClass: 'pastis-popup-modal-box',
      data: {
        titleDialog: this.translations.popupCreationTitleDialog,
        subTitleDialog: this.translations.popupCreationSubTitleDialog,
        width: '800px',
        height: '800px',
        okLabel: this.translations.popupCreationOkLabel,
        cancelLabel: this.translations.popupCreationCancelLabel,
      },
    };
    const dialogRef = this.dialog.open(CreateProfileComponent, createProfileDialogConfig);
    const subscription = dialogRef
      .afterClosed()
      .pipe(
        filter<CreateProfileFormResult>((result) => Boolean(result)),
        switchMap((result) =>
          this.profileService.createProfile(this.pastisConfig.createProfileByTypeUrl, result.profileType, result.profileVersion),
        ),
        filter<ProfileResponse>((profileResponse) => Boolean(profileResponse)),
      )
      .subscribe((profileResponse) =>
        this.router.navigate([this.pastisConfig.pastisNewProfile], { state: profileResponse, relativeTo: this.route }),
      );
    this.subscriptions.push(subscription);
  }

  public onSearchSubmit(search: string): void {
    if (!search) {
      search = '';
    }
    this.search = search;
    const profileDescriptions = this.retrievedProfiles.filter(
      (profile) =>
        profile.identifier.toLowerCase().indexOf(search.toLowerCase()) >= 0 ||
        profile.name.toLowerCase().indexOf(search.toLowerCase()) >= 0,
    );
    // console.log(this.retrievedProfiles)
    this.totalProfileNum = profileDescriptions.length;
    this.numPA = profileDescriptions.filter((profile: ProfileDescription) => profile.type === ProfileType.PA).length;
    this.numPUA = profileDescriptions.filter((profile: ProfileDescription) => profile.type === ProfileType.PUA).length;
  }

  changeType(type: string) {
    if (type !== undefined) {
      this.filterType = type;
    }
  }

  ngOnDestroy() {
    this.profileService.retrievedProfiles.next([]);
    this.subscriptions.forEach((subscriptions) => subscriptions.unsubscribe());
    if (this.pendingSub) this.pendingSub.unsubscribe();
  }

  showProfile(element: ProfileDescription) {
    // console.log('showProfile')
    if (!this.isStandalone) {
      this.openPanel(element);
    }
  }

  private isProfilAttached(inputProfile: ProfileDescription): boolean {
    return !!((inputProfile.controlSchema && inputProfile.controlSchema.length !== 2) || inputProfile.path);
  }

  async updateProfileNotice(profileDescription: ProfileDescription) {
    const extensionsByType: Map<ProfileType, string[]> = new Map([
      [ProfileType.PA, ['.rng']],
      [ProfileType.PUA, ['.json']],
    ]);

    (this.isProfilAttached(profileDescription)
      ? this.dialog.open(this.confirmReplacement, { panelClass: 'vitamui-confirm-dialog' }).afterClosed()
      : of(true)
    )
      .pipe(
        filter((confirmed) => confirmed),
        switchMap(() =>
          this.dialog
            .open<LoadProfileComponent, LoadProfileConfig>(LoadProfileComponent, {
              panelClass: 'vitamui-modal',
              data: {
                title: this.translations.popupUploadTitle,
                subTitle: this.translations.popupUploadSubTitle,
                okLabel: this.translations.popupUploadOkLabel,
                cancelLabel: this.translations.popupUploadCancelLabel,
                extensions: extensionsByType.get(profileDescription.type as ProfileType) || [],
                multipleFiles: false,
              },
            })
            .afterClosed(),
        ),
      )
      .subscribe((files) => {
        if (files) {
          const fileToUpload: File = files[0];
          if (profileDescription.type === ProfileType.PA) {
            const profile: Profile = this.noticeService.profileDescriptionToPaProfile(profileDescription);
            this.profileService.updateProfileFilePa(profile, fileToUpload).subscribe(() => this.refreshListProfiles());
          }
          if (profileDescription.type === ProfileType.PUA && fileToUpload) {
            const fileReader = new FileReader();
            fileReader.readAsText(fileToUpload, 'UTF-8');
            fileReader.onload = () => {
              const jsonObj: ProfileDescription = JSON.parse(fileReader.result.toString());
              profileDescription.controlSchema = jsonObj.controlSchema;
              const archivalProfileUnit: ArchivalProfileUnit = this.noticeService.profileDescriptionToPuaProfile(profileDescription);
              this.profileService.updateProfilePua(archivalProfileUnit).subscribe(() => this.refreshListProfiles());
            };
            fileReader.onerror = (error) => console.error(error);
          }
        }
      });
  }
}
