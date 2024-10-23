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
import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { MatLegacySnackBar as MatSnackBar } from '@angular/material/legacy-snack-bar';
import { Router } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { filter, mergeMap, Observable, of, Subscription, switchMap } from 'rxjs';
import { StartupService, VitamUISnackBarComponent } from 'vitamui-library';
import { environment } from '../../../environments/environment';
import { FileService } from '../../core/services/file.service';
import { NoticeService } from '../../core/services/notice.service';
import { NotificationService } from '../../core/services/notification.service';
import { PopupService } from '../../core/services/popup.service';
import { ProfileService } from '../../core/services/profile.service';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { FileNode } from '../../models/file-node';
import { Profile } from '../../models/profile';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileType } from '../../models/profile-type.enum';
import { VitamUIExceptionResponse } from '../../models/vitam-ui-exception-response.model';
import { DataGeneriquePopupService } from '../../shared/data-generique-popup.service';
import { CreateNoticeComponent } from '../create-notice/create-notice.component';
import { SaveProfileOptionsComponent } from '../save-profile-options/save-profile-options.component';
import { SelectNoticeComponent } from '../select-notice/select-notice.component';
import { ProfileVersion } from '../../models/profile-version.enum';
import { map, tap } from 'rxjs/operators';
import { ArchiveProfileSaverService } from './archive-profile-saver.service';
import { ArchiveUnitProfileSaverService } from './archive-unit-profile-saver.service';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';

export interface PastisDialogDataCreate {
  height: string;
  titleDialog: string;
  subTitleDialog: string;
  okLabel: string;
  cancelLabel: string;
  profileType?: ProfileType;
  profileVersion?: ProfileVersion;
  isSlaveMode?: boolean;
}

const POPUP_SAVE_PATH = 'PROFILE.POP_UP_SAVE';

function constantToTranslate(edit: boolean) {
  if (edit) {
    this.popupSaveCreateNoticeTitleDialog = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_TITLE_DIALOG_EDIT');
    this.popupSaveCreateNoticeSubTitleDialog = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_SUBTITLE_DIALOG_EDIT');
  } else {
    this.popupSaveCreateNoticeTitleDialog = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_TITLE_DIALOG');
    this.popupSaveCreateNoticeSubTitleDialog = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_SUBTITLE_DIALOG');
  }
  this.popupSaveCancelLabel = this.translated('.SAVE_PROFILE.POPUP_SAVE_CANCEL_LABEL');
  this.popupSaveTitleDialog = this.translated('.SAVE_PROFILE.POPUP_SAVE_TITLE_DIALOG');
  this.popupSaveSubTitleDialog = this.translated('.SAVE_PROFILE.POPUP_SAVE_SUBTITLE_DIALOG');
  this.popupSaveOkLabel = this.translated('.SAVE_PROFILE.POPUP_SAVE_OK_LABEL');
  this.popupSaveCreateNoticeCancelLabel = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_CANCEL_LABEL');
  this.popupSaveCreateNoticeOkLabel = this.translated('.SAVE_PROFILE.POPUP_CREATE_NOTICE_OK_LABEL');
  this.popupSaveSelectNoticeTitleDialog = this.translated('.SAVE_PROFILE.POPUP_SELECT_NOTICE_TITLE_DIALOG');
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-user-action-save-profile',
  templateUrl: './save-profile.component.html',
  styleUrls: ['./save-profile.component.scss'],
})
export class UserActionSaveProfileComponent implements OnInit, OnDestroy {
  popupSaveCancelLabel: string;
  popupSaveTitleDialog: string;
  popupSaveSubTitleDialog: string;
  popupSaveOkLabel: string;

  popupSaveCreateNoticeCancelLabel: string;
  popupSaveCreateNoticeTitleDialog: string;
  popupSaveCreateNoticeSubTitleDialog: string;
  popupSaveCreateNoticeOkLabel: string;

  popupSaveSelectNoticeTitleDialog: string;

  isStandalone: boolean = environment.standalone;
  editProfile: boolean;

  data: FileNode[] = [];
  donnees: string[];

  subscriptions: Subscription = new Subscription();

  archivalProfileUnit: ArchivalProfileUnit;
  profile: Profile;

  profileDescription: ProfileDescription;
  isSlaveMode: boolean;

  // eslint-disable-next-line @angular-eslint/no-output-native
  @Output() close = new EventEmitter();

  constructor(
    private profileService: ProfileService,
    private popupService: PopupService,
    private fileService: FileService,
    private startupService: StartupService,
    private snackBar: MatSnackBar,
    private dataGeneriquePopupService: DataGeneriquePopupService,
    private noticeService: NoticeService,
    private translateService: TranslateService,
    public dialog: MatDialog,
    private router: Router,
    private notificationService: NotificationService,
    private archiveProfileSaverService: ArchiveProfileSaverService,
    private archiveUnitProfileSaverService: ArchiveUnitProfileSaverService,
  ) {
    this.editProfile = this.router.url.substring(this.router.url.lastIndexOf('/') - 4, this.router.url.lastIndexOf('/')) === 'edit';
  }

  ngOnInit() {
    if (!this.isStandalone) {
      constantToTranslate.call(this, this.editProfile);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.popupSaveCancelLabel = 'ANNULER';
      this.popupSaveTitleDialog = 'Sélectionner les options de votre enregistrement';
      this.popupSaveSubTitleDialog = 'Enregistrement';
      this.popupSaveOkLabel = 'VALIDER';
      this.popupSaveCreateNoticeCancelLabel = 'PRECEDENT';
      this.popupSaveCreateNoticeTitleDialog = 'Rédiger la notice de profil';
      this.popupSaveCreateNoticeSubTitleDialog = 'Enregistrement';
      this.popupSaveCreateNoticeOkLabel = 'TERMINER';
    }
    this.isSlaveMode = this.popupService.externalIdentifierEnabled;
    this.subscriptions.add(this.dataGeneriquePopupService.currentDonnee.subscribe((donnees) => (this.donnees = donnees)));
  }

  translatedOnChange(): void {
    this.subscriptions.add(
      this.translateService.onLangChange.subscribe((_: LangChangeEvent) => {
        constantToTranslate.call(this);
      }),
    );
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_SAVE_PATH + nameOfFieldToTranslate);
  }

  save() {
    // Retrieve the current file tree data as a JSON
    this.data = this.fileService.currentTree.getValue();
    if (this.isStandalone) return this.subscriptions.add(this.downloadProfile().subscribe());

    const donnees = ['Local', 'SAE', "Où souhaitez-vous l'enregistrer ?"];
    this.dataGeneriquePopupService.changeDonnees(donnees);

    this.subscriptions.add(this.selectSaveOption());
  }

  private selectSaveOption(): Subscription {
    return this.dialog
      .open<SaveProfileOptionsComponent, PastisDialogData>(SaveProfileOptionsComponent, {
        width: '800px',
        panelClass: 'pastis-popup-modal-box',
        data: {
          titleDialog: this.popupSaveTitleDialog,
          subTitleDialog: this.popupSaveSubTitleDialog,
          width: '800px',
          height: '800px',
          okLabel: this.popupSaveOkLabel,
          cancelLabel: this.popupSaveCancelLabel,
        },
      })
      .afterClosed()
      .pipe(filter(({ success }) => success))
      .subscribe((result) => {
        if (result.action === 'local') {
          this.subscriptions.add(this.downloadProfile().subscribe());
        } else if (result.action === 'creation') {
          this.subscriptions.add(this.create());
        } else if (result.action === 'rattachement') {
          this.subscriptions.add(this.attach().subscribe());
        }
      });
  }

  private create(): Subscription {
    return this.dialog
      .open<CreateNoticeComponent, PastisDialogDataCreate>(CreateNoticeComponent, {
        width: '800px',
        panelClass: 'pastis-popup-modal-box',
        data: {
          height: 'auto',
          titleDialog: this.popupSaveCreateNoticeTitleDialog,
          subTitleDialog: this.popupSaveCreateNoticeSubTitleDialog,
          okLabel: this.popupSaveCreateNoticeOkLabel,
          cancelLabel: this.popupSaveCreateNoticeCancelLabel,
          profileType: this.profileService.profileType,
          profileVersion: this.profileService.profileVersion,
          isSlaveMode: this.isSlaveMode,
        },
      })
      .afterClosed()
      .pipe(filter(({ success }) => success))
      .subscribe(({ profileVersion, data: createNoticeDialogParams }) => {
        if (this.profileService.isMode(ProfileType.PUA)) {
          const profileDescription = this.editProfile
            ? this.fileService.notice.getValue()
            : this.noticeService.profileFromNotice(createNoticeDialogParams, this.editProfile, true);
          this.subscriptions.add(this.saveArchiveUnitProfile(profileDescription, this.data).subscribe());
        }
        if (this.profileService.isMode(ProfileType.PA)) {
          const profile: Profile = this.noticeService.paNotice(createNoticeDialogParams, profileVersion, true);
          if (!this.editProfile) {
            // CREER NOTICE PUIS ASSIGNER LE PROFIL A LA NOTICE
            this.profile = { ...this.profile, ...profile };
            this.profileDescription = {
              ...this.noticeService.profileFromNotice(createNoticeDialogParams, this.editProfile, false),
              ...this.profileDescription,
            };
          } else {
            this.subscriptions.add(
              this.fileService.notice.subscribe((value: ProfileDescription) => {
                this.profile = Object.assign(profile, value);
                this.profileDescription = value;
              }),
            );
          }
          // STEP 1 : Create or update Notice
          this.subscriptions.add(this.saveArchiveProfile().subscribe());
        }
      });
  }

  private attach(): Observable<any> {
    return this.dialog
      .open<SelectNoticeComponent, PastisDialogDataCreate>(SelectNoticeComponent, {
        width: '800px',
        panelClass: 'pastis-popup-modal-box',
        data: {
          height: 'auto',
          isSlaveMode: this.isSlaveMode,
          titleDialog: this.popupSaveSelectNoticeTitleDialog,
          subTitleDialog: this.popupSaveCreateNoticeSubTitleDialog,
          okLabel: this.popupSaveCreateNoticeOkLabel,
          cancelLabel: this.popupSaveCreateNoticeCancelLabel,
          profileType: this.profileService.profileType,
          profileVersion: this.profileService.profileVersion,
        },
      })
      .afterClosed()
      .pipe(
        filter(
          ({ success }: { success: boolean; data: ProfileDescription; profileType: ProfileType; profileVersion: ProfileVersion }) =>
            success,
        ),
        switchMap((targetNoticeEvent) => {
          const profileDescription = targetNoticeEvent.data;
          const data: FileNode[] = this.data;
          let action: Observable<any>;

          if (this.profileService.isMode(ProfileType.PUA)) {
            action = this.archiveUnitProfileSaverService.update(profileDescription, data);
          }

          if (this.profileService.isMode(ProfileType.PA)) {
            const profile = this.noticeService.paNotice(profileDescription, targetNoticeEvent.profileVersion, false);

            action = this.archiveProfileSaverService.attach(profile, profileDescription, data);
          }

          return action;
        }),
        tap({
          next: () => this.success('La modification du profil a bien été effectué'),
          error: (error) => this.displaySnackBar(error),
        }),
      );
  }

  displaySnackBar(vitamUIException: VitamUIExceptionResponse) {
    if (!vitamUIException || !vitamUIException.args || vitamUIException.args.length < 1) {
      return;
    }
    this.snackBar.openFromComponent(VitamUISnackBarComponent, {
      panelClass: 'vitamui-snack-bar',
      data: {
        type: 'otherType',
        messageKey: 'SNACKBAR.ERROR_WITH_LOGBOOK_OPERATION_ID',
        buttonAction: () => this.goToOperation(vitamUIException.args[0]),
        buttonMessageKey: 'SNACKBAR.ERROR_WITH_LOGBOOK_OPERATION_ID_BUTTON',
      },
    });
  }

  goToOperation(operationId: string) {
    const baseUrl = this.startupService.getReferentialUrl();
    const tenant = this.startupService.getTenantIdentifier();

    window.location.href = `${baseUrl}/logbook-operation/tenant/${tenant}?guid=${operationId}`;
  }

  saveArchiveProfile(): Observable<Profile> {
    if (this.editProfile) {
      return this.archiveProfileSaverService.update(this.profile, this.profileDescription, this.data).pipe(
        tap({
          next: () => this.success('La modification du profil a bien été effectué'),
          error: (error) => {
            const message = error?.error?.message || error?.message || 'raison inconnue';
            this.notificationService.showError(`La modification du profil a échoué (${message})`);
          },
        }),
      );
    }

    return this.archiveProfileSaverService.create(this.profile, this.profileDescription, this.data).pipe(
      tap({
        next: () => this.success('La création du profil a bien été effectué'),
        error: (error) => {
          const message = error?.error?.message || error?.message || 'raison inconnue';
          this.notificationService.showError(`La création du profil a échoué (${message})`);
        },
      }),
    );
  }

  saveArchiveUnitProfile(profileDescription: ProfileDescription, data: FileNode[]): Observable<ArchivalProfileUnit> {
    if (this.editProfile) {
      return this.archiveUnitProfileSaverService.update(profileDescription, data).pipe(
        tap({
          next: () => this.success('La modification du profil a bien été effectué'),
          error: (error) => this.displaySnackBar(error),
        }),
      );
    }

    return this.archiveUnitProfileSaverService.create(profileDescription, data).pipe(
      tap({
        next: () => this.success('La création du profil a bien été effectué'),
        error: (error) => this.displaySnackBar(error),
      }),
    );
  }

  success(msg: string) {
    this.notificationService.showSuccess(msg);
    // sleep 3 sec before return pastishome
    setTimeout(() => {
      this.router.navigate(['pastis']);
    }, 3000);
  }

  downloadProfile(): Observable<any> {
    return of(this.data).pipe(
      filter((data: FileNode[]) => Boolean(data)),
      mergeMap((data) => of({ data, notice: this.fileService.notice.value })),
      mergeMap(({ data, notice }) =>
        this.profileService
          .uploadFile(data, notice, this.profileService.profileType, this.profileService.profileVersion)
          .pipe(map((data: Blob) => ({ data, notice }))),
      ),
      tap((payload) => {
        const { data, notice } = payload;
        const type = this.profileService.isMode(ProfileType.PA) ? 'application/xml' : 'application/json';
        const extension = this.profileService.isMode(ProfileType.PA) ? 'rng' : 'json';
        const normalizedNoticeName = notice.name.toLowerCase().replace(/ /gm, '_');
        const filename = `${normalizedNoticeName}.${extension}`;
        const href = window.URL.createObjectURL(new Blob([data], { type }));
        const link = document.createElement('a');
        link.href = href;
        link.download = filename;
        // this is necessary as link.click() does not work on the latest firefox
        link.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));
        setTimeout(() => {
          // For Firefox it is necessary to delay revoking the ObjectURL
          window.URL.revokeObjectURL(href);
          link.remove();
        }, 100);
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
