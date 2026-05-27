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
import { Component, EventEmitter, OnDestroy, OnInit, Output, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { EMPTY, filter, mergeMap, Observable, of, Subscription } from 'rxjs';
import { ApplicationId } from '../../../../../vitamui-library/src/app/modules/application-id.enum';
import { SnackBarService } from '../../../../../vitamui-library/src/app/modules/components/snack-bar/snack-bar.service';
import { StartupService } from '../../../../../vitamui-library/src/app/modules/startup.service';
import { environment } from '../../../environments/environment';
import { FileService } from '../../core/services/file.service';
import { NoticeService } from '../../core/services/notice.service';
import { PopupService } from '../../core/services/popup.service';
import { ProfileService } from '../../core/services/profile.service';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { FileNode } from '../../models/file-node';
import { Profile } from '../../models/profile';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileType } from '../../models/profile-type.enum';
import { VitamUIExceptionResponse } from '../../models/vitam-ui-exception-response.model';
import { DataGeneriquePopupService } from '../../shared/data-generique-popup.service';
import { ProfileVersion } from '../../models/profile-version.enum';
import { map, switchMap, tap } from 'rxjs/operators';
import { ArchiveProfileSaverService } from './archive-profile-saver.service';
import { ArchiveUnitProfileSaverService } from './archive-unit-profile-saver.service';
import { SaveProfilePopupComponent } from '../save-profile-popup/save-profile-popup.component';

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
  standalone: false,
})
export class UserActionSaveProfileComponent implements OnInit, OnDestroy {
  private profileService = inject(ProfileService);
  private popupService = inject(PopupService);
  private fileService = inject(FileService);
  private startupService = inject(StartupService);
  private dataGeneriquePopupService = inject(DataGeneriquePopupService);
  private noticeService = inject(NoticeService);
  private translateService = inject(TranslateService);
  dialog = inject(MatDialog);
  private router = inject(Router);
  private archiveProfileSaverService = inject(ArchiveProfileSaverService);
  private archiveUnitProfileSaverService = inject(ArchiveUnitProfileSaverService);
  private snackBarService = inject(SnackBarService);

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

  profile: Profile;

  profileDescription: ProfileDescription;
  isSlaveMode: boolean;

  // eslint-disable-next-line @angular-eslint/no-output-native
  @Output() close = new EventEmitter();

  constructor() {
    this.editProfile = this.router.url.substring(this.router.url.lastIndexOf('/') - 4, this.router.url.lastIndexOf('/')) === 'edit';
  }

  ngOnInit() {
    constantToTranslate.call(this, this.editProfile);
    this.translatedOnChange();
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

  saveProfile() {
    this.data = this.fileService.currentTree.getValue();
    if (this.isStandalone) return this.subscriptions.add(this.downloadProfile().subscribe());

    const donnees = ['Local', 'SAE', "Où souhaitez-vous l'enregistrer ?"];
    this.dataGeneriquePopupService.changeDonnees(donnees);
    const saveProfileDialogRef = this.dialog.open(SaveProfilePopupComponent, {
      disableClose: true,
    });

    const subscription = saveProfileDialogRef
      .afterClosed()
      .pipe(
        filter((result) => result && result.action),
        switchMap((result) => {
          switch (result.action) {
            case 'local':
              return this.downloadProfile();
            case 'rattachement':
              return this.attach(result.data);
            case 'creation':
              this.create(result.data);
              return EMPTY;
          }
        }),
      )
      .subscribe();
    this.subscriptions.add(subscription);
  }

  private create(data: any) {
    if (this.profileService.isMode(ProfileType.PUA)) {
      const profileDescription = this.editProfile
        ? this.fileService.notice.getValue()
        : this.noticeService.profileFromNotice(data, this.profileService.profileVersion, this.editProfile, true);
      this.subscriptions.add(this.saveArchiveUnitProfile(profileDescription, this.data).subscribe());
    }
    if (this.profileService.isMode(ProfileType.PA)) {
      const profile: Profile = this.noticeService.paNotice(data, this.profileService.profileVersion, true);
      if (!this.editProfile) {
        this.profile = { ...this.profile, ...profile };
        this.profileDescription = {
          ...this.noticeService.profileFromNotice(data, this.profileService.profileVersion, this.editProfile, false),
          ...this.profileDescription,
        };
      } else {
        this.subscriptions.add(
          this.fileService.notice.subscribe((value: ProfileDescription) => {
            this.profile = { ...value, ...profile };
            this.profileDescription = value;
          }),
        );
      }
      this.subscriptions.add(this.saveArchiveProfile().subscribe());
    }
  }

  private attach(profileDescription: ProfileDescription): Observable<any> {
    const data: FileNode[] = this.data;
    let action: Observable<any>;

    if (this.profileService.isMode(ProfileType.PUA)) {
      action = this.archiveUnitProfileSaverService.update(profileDescription, data);
    }

    if (this.profileService.isMode(ProfileType.PA)) {
      const profile = this.noticeService.paNotice(profileDescription, this.profileService.profileVersion, false);
      action = this.archiveProfileSaverService.attach(profile, profileDescription, data);
    }

    return action.pipe(
      tap({
        next: () => this.success('PROFILE.LIST_PROFILE.PROFILE_PREVIEW.MODIFICATION_SUCCESS'),
        error: ({ error }: { error: VitamUIExceptionResponse }) => this.displayLogbookOperationSnackBar(error),
      }),
    );
  }

  displayLogbookOperationSnackBar(error: VitamUIExceptionResponse, message: string = null): void {
    const operationId = error?.args?.at(0) || null;
    const hasOperationId = Boolean(operationId);
    const content = message || error?.message || 'raison inconnue';

    if (!hasOperationId) {
      const { status, exception } = error;
      const errorMessage = `Erreur ${status || '???'} (${exception || '???'}): ${content}`;
      this.snackBarService.open({ message: errorMessage });
      return;
    }

    const tenantId = this.startupService.getTenantIdentifier();
    this.snackBarService.open({
      message: 'SNACKBAR.ERROR_WITH_LOGBOOK_OPERATION_ID',
      translateParams: { content },
      buttons: [
        {
          appId: ApplicationId.LOGBOOK_OPERATION_APP,
          path: `/tenant/${tenantId}?guid=${operationId}`,
          label: 'SNACKBAR.ERROR_WITH_LOGBOOK_OPERATION_ID_BUTTON',
        },
      ],
    });
  }

  saveArchiveProfile(): Observable<Profile> {
    if (this.editProfile) {
      return this.archiveProfileSaverService.update(this.profile, this.profileDescription, this.data).pipe(
        tap({
          next: () => this.success('PROFILE.LIST_PROFILE.PROFILE_PREVIEW.MODIFICATION_SUCCESS'),
          error: ({ error }: { error: VitamUIExceptionResponse }) => {
            const message = 'La modification du profil a échoué';
            this.displayLogbookOperationSnackBar(error, message);
          },
        }),
      );
    }

    return this.archiveProfileSaverService.create(this.profile, this.profileDescription, this.data).pipe(
      tap({
        next: () => this.success('PROFILE.LIST_PROFILE.PROFILE_PREVIEW.CREATION_SUCCESS'),
        error: ({ error }: { error: VitamUIExceptionResponse }) => {
          const message = 'La création du profil a échoué';
          this.displayLogbookOperationSnackBar(error, message);
        },
      }),
    );
  }

  saveArchiveUnitProfile(profileDescription: ProfileDescription, data: FileNode[]): Observable<ArchivalProfileUnit> {
    if (this.editProfile) {
      return this.archiveUnitProfileSaverService.update(profileDescription, data).pipe(
        tap({
          next: () => this.success('PROFILE.LIST_PROFILE.PROFILE_PREVIEW.MODIFICATION_SUCCESS'),
          error: ({ error }: { error: VitamUIExceptionResponse }) => this.displayLogbookOperationSnackBar(error),
        }),
      );
    }

    return this.archiveUnitProfileSaverService.create(profileDescription, data).pipe(
      tap({
        next: () => this.success('PROFILE.LIST_PROFILE.PROFILE_PREVIEW.CREATION_SUCCESS'),
        error: ({ error }: { error: VitamUIExceptionResponse }) => this.displayLogbookOperationSnackBar(error),
      }),
    );
  }

  success(msg: string) {
    this.snackBarService.open({
      message: msg,
    });
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
        const normalizedNoticeName = notice?.name?.toLowerCase().replace(/ /gm, '_') || 'pastis_profile';
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
