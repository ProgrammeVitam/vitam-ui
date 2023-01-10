import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FileService } from '../../core/services/file.service';
import { PopupService } from '../../core/services/popup.service';
import { ProfileService } from '../../core/services/profile.service';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { Notice } from '../../models/notice.model';
import { Profile } from '../../models/profile';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';
import { PastisDialogDataCreate } from '../save-profile/save-profile.component';


interface Status {
  value: string;
  viewValue: string;
}

const POPUP_CREATION_CHOICE_PATH = 'PROFILE.POP_UP_CREATION_NOTICE.CHOICE';

function constantToTranslate() {
  this.profilActif = this.translated('.PROFIL_ACTIF');
  this.profilInactif = this.translated('.PROFIL_INACTIF');
}

@Component({
  selector: 'create-notice',
  templateUrl: './create-notice.component.html',
  styleUrls: [ './create-notice.component.scss' ]
})
export class CreateNoticeComponent implements OnInit, OnDestroy {
  form: FormGroup;
  stepIndex = 0;
  btnIsDisabled: boolean;
  dialogData: PastisDialogData;
  isDisabledButton = false;
  notice: Notice;
  // edit or new notice
  editNotice: boolean;
  titleDialog: string;
  subTitleDialog: string;
  okLabel: string;
  cancelLabel: string;
  arrayStatus: Status[];
  typeProfile?: string;
  modePUA: boolean;
  information: string;
  presenceNonDeclareMetadonneesPUAControl = new FormControl(false);
  createNotice: boolean;
  profilActif: string;
  profilInactif: string;
  validate: boolean;

  isStandalone: boolean = environment.standalone;

  subscriptions = new Subscription();

  constructor(public dialogRef: MatDialogRef<CreateNoticeComponent>,
              @Inject(MAT_DIALOG_DATA) public data: PastisDialogDataCreate,
              private formBuilder: FormBuilder,
              private translateService: TranslateService,
              private popUpService: PopupService,
              private fileService: FileService,
              private router: Router,
              private profileService: ProfileService) {

  }

  ngOnInit() {

    this.editNotice = this.router.url.substring(this.router.url.lastIndexOf('/') - 4, this.router.url.lastIndexOf('/')) === 'edit';
    if (this.editNotice) {
      this.validate = true;
      // Subscribe observer to notice

      this.subscriptions.add(
        this.fileService.noticeEditable.subscribe((value: Notice) => {
          this.notice = value;
        }));
    } else {
      this.notice = {
        description: '',
        name: '',
        status: 'ACTIVE',
        identifier: ''
      };
    }

    if (!this.isStandalone) {
      constantToTranslate.call(this);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.profilActif = 'Profil actif';
      this.profilInactif = 'Profil inactif';
    }
    this.arrayStatus = [
      { value: 'INACTIVE', viewValue: this.profilInactif },
      { value: 'ACTIVE', viewValue: this.profilActif }
    ];
    this.typeProfile = this.data.modeProfile;
    if (this.typeProfile === 'PUA') {
      this.modePUA = true;
    }
    this.information = 'texte d\'information';
    this.form = this.formBuilder.group({
      identifier: [ null, Validators.required ],
      intitule: [ null, Validators.required ],
      selectedStatus: [ null ],
      description: [ null ],
      autoriserPresenceMetadonnees: false
    });


    this.subscriptions.add(
      this.presenceNonDeclareMetadonneesPUAControl.valueChanges.subscribe((value) => {
        this.form.controls.autoriserPresenceMetadonnees.setValue(value);
      }));


    // Subscribe observer to button status and
    // set the inital state of the ok button to disabled

    this.subscriptions.add(
      this.popUpService.btnYesShoudBeDisabled.subscribe(status => {
        this.btnIsDisabled = status;
      }));
  }

  translatedOnChange(): void {
    this.subscriptions.add(
      this.translateService.onLangChange
        .subscribe((_: LangChangeEvent) => {
          constantToTranslate.call(this);
          // console.log(event.lang);
        }));
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_CHOICE_PATH + nameOfFieldToTranslate);
  }

  onCancel() {
    this.dialogRef.close();
  }


  upateButtonStatusAndDataToSend() {
    this.popUpService.setPopUpDataOnClose('test');
    this.popUpService.disableYesButton(true);
  }


  onNoClick(): void {
    this.dialogRef.close();
  }


  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  checkIdentifier(modePUA: boolean) {

    if (this.notice.identifier.length < 1) {
      this.validate = false;
      return;
    }
    if (modePUA) {
      const archivalProfileUnit = {} as ArchivalProfileUnit;
      archivalProfileUnit.identifier = this.notice.identifier;
      this.subscriptions.add(
        this.profileService.checkPuaProfile(archivalProfileUnit).subscribe(
          (response: boolean) => {
            if (response) {
              alert('Identifier already exists use another identifier');
              this.validate = false;
            } else {
              this.validate = true;
              this.checkIntitule();
            }
          }
        ));
    } else {
      const profile = {} as Profile;
      profile.identifier = this.notice.identifier;
      this.subscriptions.add(
        this.profileService.checkPaProfile(profile).subscribe(
          (response: boolean) => {
            if (response) {
              alert('Identifier already exists use another identifier');
              this.validate = false;
            } else {
              this.validate = true;
              this.checkIntitule();
            }
          }
        ));
    }
  }

  checkIntitule() {
    this.validate = this.notice.name.length !== 0;
  }

  onSubmit() {
    if (this.form.invalid) {
      this.isDisabledButton = true;
      return;
    }
    this.isDisabledButton = true;
    // console.log(this.form.value);
    if (this.editNotice) {
      this.fileService.noticeEditable.next(this.notice);
      this.fileService.setNotice(true);
    }
    this.dialogRef.close({ success: true, action: 'none', data: this.form.value, mode: this.typeProfile });
  }
}