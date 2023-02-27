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
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { FileNode } from '../../models/file-node';
import { Profile } from '../../models/profile';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileMode } from '../../models/profile-response';
import { CreateNoticeChoiceComponent } from '../../profile/create-notice-choice/create-notice-choice.component';
import { CreateNoticeComponent } from '../../user-actions/create-notice/create-notice.component';
import { PastisDialogDataCreate } from '../../user-actions/save-profile/save-profile.component';
import { PastisDialogData } from '../pastis-dialog/classes/pastis-dialog-data';

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
  selector: 'pastis-popup-option',
  templateUrl: './pastis-popup-option.component.html',
  styleUrls: ['./pastis-popup-option.component.scss'],
})
export class PastisPopupOptionComponent implements OnInit, OnDestroy {
  @Input()
  sedaUrl: string;
  @Input()
  newProfileUrl: string;
  @Input()
  uploader: FileUploader = new FileUploader({ url: '' });

  subscriptions: Subscription = new Subscription();

  archivalProfileUnit: ArchivalProfileUnit;
  data: FileNode[] = [];
  donnees: string[];
  editProfile: boolean;
  expanded = false;
  isStandalone: boolean = environment.standalone;
  popupCreationCancelLabel: string;
  popupCreationOkLabel: string;
  popupCreationSubTitleDialog: string;
  popupCreationTitleDialog: string;
  popupSaveCancelLabel: string;
  popupSaveCreateNoticeCancelLabel: string;
  popupSaveCreateNoticeOkLabel: string;
  popupSaveCreateNoticeSubTitleDialog: string;
  popupSaveCreateNoticeTitleDialog: string;
  popupSaveOkLabel: string;
  popupSaveSubTitleDialog: string;
  popupSaveTitleDialog: string;
  profile: Profile;
  profileDescription: ProfileDescription;

  constructor(
    private router: Router,
    public dialog: MatDialog,
    private profileService: ProfileService,
    private noticeService: NoticeService,
    private translateService: TranslateService,
    private loaderService: NgxUiLoaderService,
    private notificationService: NotificationService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    constantToTranslate.call(this, this.editProfile);
    this.subscriptions.add(
      this.translateService.onLangChange.subscribe((_: LangChangeEvent) => {
        constantToTranslate.call(this);
      })
    );
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

      this.subscriptions.add(
        this.profileService.uploadProfile(formData).subscribe((response: any) => {
          if (response) {
            this.router.navigate([this.newProfileUrl], { state: response, relativeTo: this.route });
          }
        })
      );
    }
  }

  changeExpand() {
    this.expanded = !this.expanded;
  }

  async createNotice() {
    this.loaderService.start();
    const dataToSendToPopUp = {} as PastisDialogData;
    dataToSendToPopUp.titleDialog = this.popupCreationTitleDialog;
    dataToSendToPopUp.subTitleDialog = this.popupCreationSubTitleDialog;
    dataToSendToPopUp.width = '800px';
    dataToSendToPopUp.height = '800px';
    dataToSendToPopUp.okLabel = this.popupCreationOkLabel;
    dataToSendToPopUp.cancelLabel = this.popupCreationCancelLabel;
    const dialogRef = this.dialog.open(CreateNoticeChoiceComponent, {
      width: '800px',
      panelClass: 'pastis-popup-modal-box',
      data: dataToSendToPopUp,
    });
    this.subscriptions.add(
      dialogRef.afterClosed().subscribe((result) => {
        if (result.success) {
          if (result.action === ProfileMode.PA || result.action === ProfileMode.PUA) {
            const dataToSendToPopUp2 = {} as PastisDialogDataCreate;
            dataToSendToPopUp2.titleDialog = this.popupSaveCreateNoticeTitleDialog;
            dataToSendToPopUp2.subTitleDialog = this.popupSaveCreateNoticeSubTitleDialog;
            dataToSendToPopUp2.okLabel = this.popupSaveCreateNoticeOkLabel;
            dataToSendToPopUp2.cancelLabel = this.popupSaveCreateNoticeCancelLabel;
            dataToSendToPopUp2.modeProfile = result.action;
            const dialogRef2 = this.dialog.open(CreateNoticeComponent, {
              width: '800px',
              panelClass: 'pastis-popup-modal-box',
              data: dataToSendToPopUp2,
            });
            this.subscriptions.add(
              dialogRef2.afterClosed().subscribe((result1) => {
                const { success, mode, data } = result1 || {};

                if (success) {
                  if (mode === ProfileMode.PUA) {
                    const profileDescription = this.noticeService.puaNotice(data);

                    this.subscriptions.add(
                      this.profileService.createArchivalUnitProfile(profileDescription).subscribe(() => {
                        this.changeExpand();
                        this.notificationService.showSuccess('La création de notice a bien été effectué');
                        this.profileService.refreshListProfiles();
                      })
                    );
                  } else if (mode === ProfileMode.PA) {
                    const profile: Profile = this.noticeService.paNotice(data, true);
                    // STEP 1 : Create Notice
                    this.subscriptions.add(
                      this.profileService.createProfilePa(profile).subscribe(() => {
                        this.changeExpand();
                        this.notificationService.showSuccess('La création de notice a bien été effectué');
                        this.profileService.refreshListProfiles();
                      })
                    );
                  }
                }
              })
            );
          }
        }
      })
    );
    this.loaderService.stop();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
