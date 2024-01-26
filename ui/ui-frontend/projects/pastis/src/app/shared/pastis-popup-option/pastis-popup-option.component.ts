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
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-popup-option',
  templateUrl: './pastis-popup-option.component.html',
  styleUrls: ['./pastis-popup-option.component.scss']
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

  constructor(private router: Router,
              public dialog: MatDialog,
              private profileService: ProfileService,
              private noticeService: NoticeService,
              private translateService: TranslateService,
              private loaderService: NgxUiLoaderService,
              private notificationService: NotificationService,
              private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    constantToTranslate.call(this, this.editProfile);
    this.translatedOnChange();
  }

  translatedOnChange(): void {
    this.translateService.onLangChange
      .subscribe((_: LangChangeEvent) => {
        constantToTranslate.call(this);
      });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(nameOfFieldToTranslate);
  }

  goToSedaView() {
    const url = document.URL + '/sedaview'
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
    const createNoticeChoiceData = {} as PastisDialogData;
    createNoticeChoiceData.titleDialog = this.popupCreationTitleDialog;
    createNoticeChoiceData.subTitleDialog = this.popupCreationSubTitleDialog;
    createNoticeChoiceData.width = '800px';
    createNoticeChoiceData.height = '800px';
    createNoticeChoiceData.okLabel = this.popupCreationOkLabel;
    createNoticeChoiceData.cancelLabel = this.popupCreationCancelLabel;
    const createNoticeChoiceDialogRef = this.dialog.open(CreateNoticeChoiceComponent, {
        width: '800px',
        panelClass: 'pastis-popup-modal-box',
        data: createNoticeChoiceData
      }
    );
    const subscription1 = createNoticeChoiceDialogRef.afterClosed().subscribe((result) => {
      if (result.success) {
        if (result.action === ProfileType.PA || result.action === ProfileType.PUA) {
          // eslint-disable-next-line no-shadow
          const createNoticeData = {} as PastisDialogDataCreate;
          createNoticeData.titleDialog = this.popupSaveCreateNoticeTitleDialog;
          createNoticeData.subTitleDialog = this.popupSaveCreateNoticeSubTitleDialog;
          createNoticeData.okLabel = this.popupSaveCreateNoticeOkLabel;
          createNoticeData.cancelLabel = this.popupSaveCreateNoticeCancelLabel;
          createNoticeData.profileMode = result.action;
          // eslint-disable-next-line no-shadow
          const createNoticeDialogRef = this.dialog.open(CreateNoticeComponent, {
              width: '800px',
              panelClass: 'pastis-popup-modal-box',
              data: createNoticeData
            }
          );
          const subscription2 = createNoticeDialogRef.afterClosed().subscribe((result) => {
            let retour;
            if (result.success) {
              retour = result.data;
              if (result.mode === ProfileType.PUA) {
                const profileDescription = this.noticeService.puaNotice(retour);
                this.profileService.createArchivalUnitProfile(profileDescription).subscribe(() => {
                  this.changeExpand();
                  this.notificationService.showSuccess('La création de notice a bien été effectué');
                  this.profileService.refreshListProfiles();
                });
              } else if (result.mode === ProfileType.PA) {
                const profile: Profile = this.noticeService.paNotice(retour, true);
                // STEP 1 : Create Notice
                this.profileService.createProfilePa(profile).subscribe(() => {
                  this.changeExpand();
                  this.notificationService.showSuccess('La création de notice a bien été effectué');
                  this.profileService.refreshListProfiles();
                });

              }
            }
          });

          this.subscriptions.add(subscription2);
        }
      }
    });
    this.subscriptions.add(subscription1);
    this.loaderService.stop();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

}
