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
import { Component, Input, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { FileUploader } from 'ng2-file-upload';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { environment } from 'projects/pastis/src/environments/environment';
import { Subscription } from 'rxjs';
import { NoticeService } from '../../core/services/notice.service';
import { NotificationService } from '../../core/services/notification.service';
import { ProfileService } from '../../core/services/profile.service';
import { FileNode } from '../../models/file-node';
import { Profile } from '../../models/profile';
import { ProfileType } from '../../models/profile-type.enum';
import { CreateNoticeComponent } from '../../user-actions/create-notice/create-notice.component';
import { PastisDialogDataCreate } from '../../user-actions/save-profile/save-profile.component';
import { PastisDialogData } from '../pastis-dialog/classes/pastis-dialog-data';
import { CreateProfileComponent, CreateProfileFormResult } from '../../profile/create-profile/create-profile.component';
import { MatDialogConfig } from '@angular/material/dialog';

function constantToTranslate(edit: boolean) {
  if (edit) {
    this.popupSaveCreateNoticeTitleDialog = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_CREATE_NOTICE_TITLE_DIALOG_EDIT');
    this.popupSaveCreateNoticeSubTitleDialog = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_CREATE_NOTICE_SUBTITLE_DIALOG_EDIT');
  } else {
    this.popupSaveCreateNoticeTitleDialog = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_CREATE_NOTICE_TITLE_DIALOG');
    this.popupSaveCreateNoticeSubTitleDialog = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_CREATE_NOTICE_SUBTITLE_DIALOG');
  }
  this.popupSaveCancelLabel = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_SAVE_CANCEL_LABEL');
  this.popupSaveTitleDialog = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_SAVE_TITLE_DIALOG');
  this.popupSaveSubTitleDialog = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_SAVE_SUBTITLE_DIALOG');
  this.popupSaveOkLabel = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_SAVE_OK_LABEL');
  this.popupSaveCreateNoticeCancelLabel = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_CREATE_NOTICE_CANCEL_LABEL');
  this.popupSaveCreateNoticeOkLabel = this.translated('PROFILE.POP_UP_SAVE.SAVE_PROFILE.POPUP_CREATE_NOTICE_OK_LABEL');
  this.popupCreationCancelLabel = this.translated('PROFILE.POP_UP_CREATION.POPUP_CREATION_CANCEL_LABEL');
  this.popupCreationTitleDialog = this.translated('PROFILE.POP_UP_CREATION.POPUP_CREATION_TITLE_DIALOG');
  this.popupCreationSubTitleDialog = this.translated('PROFILE.POP_UP_CREATION.POPUP_CREATION_SUBTITLE_DIALOG');
  this.popupCreationOkLabel = this.translated('PROFILE.POP_UP_CREATION.POPUP_CREATION_OK_LABEL');
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-popup-option',
  templateUrl: './pastis-popup-option.component.html',
  styleUrls: ['./pastis-popup-option.component.scss'],
  standalone: false,
})
export class PastisPopupOptionComponent implements OnInit, OnDestroy {
  popupSaveCancelLabel: string;
  popupSaveTitleDialog: string;
  popupSaveSubTitleDialog: string;
  popupSaveOkLabel: string;

  popupSaveCreateNoticeCancelLabel: string;
  popupSaveCreateNoticeTitleDialog: string;
  popupSaveCreateNoticeSubTitleDialog: string;
  popupSaveCreateNoticeOkLabel: string;
  editProfile: boolean;

  subscriptions: Subscription = new Subscription();

  isStandalone: boolean = environment.standalone;
  popupCreationCancelLabel: string;
  popupCreationTitleDialog: string;
  popupCreationSubTitleDialog: string;
  popupCreationOkLabel: string;

  data: FileNode[] = [];
  profile: Profile;

  @Input()
  sedaUrl: string;
  @Input()
  newProfileUrl: string;
  @Input()
  uploader: FileUploader = new FileUploader({ url: '' });

  expanded = false;

  constructor(
    private router: Router,
    public dialog: MatDialog,
    private profileService: ProfileService,
    private noticeService: NoticeService,
    private translateService: TranslateService,
    private loaderService: NgxUiLoaderService,
    private notificationService: NotificationService,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    constantToTranslate.call(this, this.editProfile);
    this.translatedOnChange();
  }

  translatedOnChange(): void {
    this.translateService.onLangChange.subscribe((_: LangChangeEvent) => {
      constantToTranslate.call(this);
    });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(nameOfFieldToTranslate);
  }

  goToSedaView() {
    const url = document.URL + '/sedaview';
    window.open(url, '_blank');
  }

  uploadProfile(files: File[]): void {
    const fileToUpload: File = files[0];

    if (fileToUpload) {
      const formData = new FormData();
      formData.append('file', fileToUpload, fileToUpload.name);
      this.profileService.uploadProfile(formData).subscribe((response: any) => {
        if (response) {
          this.router.navigate([this.newProfileUrl], { state: response, relativeTo: this.route });
        }
      });
    }
  }

  changeExpand() {
    this.expanded = !this.expanded;
  }

  async createNotice() {
    this.loaderService.start();
    const createNoticeDialogConfig: MatDialogConfig<PastisDialogData> = {
      width: '800px',
      panelClass: 'pastis-popup-modal-box',
      data: {
        titleDialog: this.popupCreationTitleDialog,
        subTitleDialog: this.popupCreationSubTitleDialog,
        width: '800px',
        height: '800px',
        okLabel: this.popupCreationOkLabel,
        cancelLabel: this.popupCreationCancelLabel,
      },
    };
    const dialogRef = this.dialog.open(CreateProfileComponent, createNoticeDialogConfig);
    const subscription1 = dialogRef.afterClosed().subscribe((result: CreateProfileFormResult) => {
      if (result) {
        const createNoticeData = {} as PastisDialogDataCreate;
        createNoticeData.titleDialog = this.popupSaveCreateNoticeTitleDialog;
        createNoticeData.subTitleDialog = this.popupSaveCreateNoticeSubTitleDialog;
        createNoticeData.okLabel = this.popupSaveCreateNoticeOkLabel;
        createNoticeData.cancelLabel = this.popupSaveCreateNoticeCancelLabel;
        createNoticeData.profileType = result?.profileType;
        createNoticeData.profileVersion = result?.profileVersion;
        const createNoticeDialogRef = this.dialog.open(CreateNoticeComponent, {
          width: '800px',
          panelClass: 'pastis-popup-modal-box',
          data: createNoticeData,
        });
        const subscription2 = createNoticeDialogRef.afterClosed().subscribe((result) => {
          let retour;
          if (result.success) {
            retour = result.data;
            if (result.profileType === ProfileType.PUA) {
              const profileDescription = this.noticeService.puaNotice(retour, result.profileVersion);
              this.profileService.createArchivalUnitProfile(profileDescription).subscribe(() => {
                this.changeExpand();
                this.notificationService.showSuccess(this.translateService.instant('PASTIS_POPUP_OPTION.CREATION_SUCCESS'));
                this.profileService.refreshListProfiles();
              });
            } else if (result.profileType === ProfileType.PA) {
              const profile: Profile = this.noticeService.paNotice(retour, result.profileVersion, true);
              // STEP 1 : Create Notice
              this.profileService.createProfilePa(profile).subscribe(() => {
                this.changeExpand();
                this.notificationService.showSuccess(this.translateService.instant('PASTIS_POPUP_OPTION.CREATION_SUCCESS'));
                this.profileService.refreshListProfiles();
              });
            }
          }
        });
        this.subscriptions.add(subscription2);
      }
    });
    this.subscriptions.add(subscription1);
    this.loaderService.stop();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
