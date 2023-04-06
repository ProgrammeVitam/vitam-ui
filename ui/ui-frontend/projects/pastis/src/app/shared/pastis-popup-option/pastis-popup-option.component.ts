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
  // tslint:disable-next-line:component-selector
  selector: 'pastis-popup-option',
  templateUrl: './pastis-popup-option.component.html',
  styleUrls: [ './pastis-popup-option.component.scss' ]
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

  subscription1$: Subscription;
  subscription2$: Subscription;
  uploadProfileSub: Subscription;
  subscriptions: Subscription[] = [];
  isStandalone: boolean = environment.standalone;
  popupCreationCancelLabel: string;
  popupCreationTitleDialog: string;
  popupCreationSubTitleDialog: string;
  popupCreationOkLabel: string;

  donnees: string[];

  data: FileNode[] = [];

  archivalProfileUnit: ArchivalProfileUnit;
  profile: Profile;

  profileDescription: ProfileDescription;

  @Input()
  sedaUrl: string;
  @Input()
  newProfileUrl: string;
  @Input()
  uploader: FileUploader = new FileUploader({ url: '' });

  expanded = false;


  constructor(private router: Router, private profileService: ProfileService,
              public dialog: MatDialog, private noticeService: NoticeService,
              private translateService: TranslateService, private loaderService: NgxUiLoaderService,
              private notificationService: NotificationService, private route: ActivatedRoute) {
  }

  ngOnInit(): void {
    constantToTranslate.call(this, this.editProfile);
    this.translatedOnChange();
  }

  translatedOnChange(): void {
    this.translateService.onLangChange
      .subscribe((_: LangChangeEvent) => {
        constantToTranslate.call(this);
        // console.log(event.lang);
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
      this.uploadProfileSub = this.profileService.uploadProfile(formData).subscribe((response: any) => {
        if (response) {
          // console.log('File submited! Reponse is : ', response);
          this.router.navigate([ this.newProfileUrl ], { state: response, relativeTo: this.route });
        }
      });
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
        data: dataToSendToPopUp
      }
    );
    this.subscription2$ = dialogRef.afterClosed().subscribe((result) => {
      if (result.success) {
        // console.log(result.action + ' PA ou PUA ?');
        if (result.action === 'PA' || result.action === 'PUA') {
          // tslint:disable-next-line:no-shadowed-variable
          const dataToSendToPopUp = {} as PastisDialogDataCreate;
          dataToSendToPopUp.titleDialog = this.popupSaveCreateNoticeTitleDialog;
          dataToSendToPopUp.subTitleDialog = this.popupSaveCreateNoticeSubTitleDialog;
          dataToSendToPopUp.okLabel = this.popupSaveCreateNoticeOkLabel;
          dataToSendToPopUp.cancelLabel = this.popupSaveCreateNoticeCancelLabel;
          dataToSendToPopUp.modeProfile = result.action;
          // tslint:disable-next-line:no-shadowed-variable
          const dialogRef = this.dialog.open(CreateNoticeComponent, {
              width: '800px',
              panelClass: 'pastis-popup-modal-box',
              data: dataToSendToPopUp
            }
          );
          // tslint:disable-next-line:no-shadowed-variable
          dialogRef.afterClosed().subscribe((result) => {
            let retour;
            if (result.success) {
              retour = result.data;
              // console.log(retour.identifier + "identifier")
              if (result.mode === 'PUA') {
                // console.log('je suis sur un enregistrement d\'un PUA');
                const profileDescription = this.noticeService.puaNotice(retour);
                this.profileService.createArchivalUnitProfile(profileDescription).subscribe(() => {
                  this.changeExpand();
                  this.notificationService.showSuccess('La création de notice a bien été effectué');
                  this.profileService.refreshListProfiles();
                  // console.log('ok create');
                });
              } else if (result.mode === 'PA') {
                // console.log(retour.identifier);
                const profile: Profile = this.noticeService.paNotice(retour, true);
                // STEP 1 : Create Notice
                this.profileService.createProfilePa(profile).subscribe(() => {
                  // console.log("ok create" + createdProfile)
                  this.changeExpand();
                  this.notificationService.showSuccess('La création de notice a bien été effectué');
                  this.profileService.refreshListProfiles();
                });

              }
            }
          });
        }
      }
    });
    this.loaderService.stop();
    this.subscriptions.push(this.subscription2$);
  }

  ngOnDestroy(): void {
    if (this.uploadProfileSub) {
      this.uploadProfileSub.unsubscribe();
    }
  }

}
