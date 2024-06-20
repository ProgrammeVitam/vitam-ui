import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { Router } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { ApplicationService } from 'vitamui-library';
import { environment } from '../../../environments/environment';
import { FileService } from '../../core/services/file.service';
import { PopupService } from '../../core/services/popup.service';
import { ProfileService } from '../../core/services/profile.service';
import { ArchivalProfileUnit } from '../../models/archival-profile-unit';
import { Notice } from '../../models/notice.model';
import { Profile } from '../../models/profile';
import { ProfileType } from '../../models/profile-type.enum';
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
  // tslint:disable-next-line:component-selector
  selector: 'create-notice',
  templateUrl: './create-notice.component.html',
  styleUrls: ['./create-notice.component.scss'],
})
export class CreateNoticeComponent implements OnInit, OnDestroy {
  form: FormGroup;
  stepIndex = 0;
  btnIsDisabled: boolean;
  dialogData: PastisDialogData;
  notice: Notice;
  // edit or new notice
  editNotice: boolean;
  titleDialog: string;
  subTitleDialog: string;
  okLabel: string;
  cancelLabel: string;
  arrayStatus: Status[];
  typeProfile?: ProfileType;
  modePUA: boolean;
  information: string;
  presenceNonDeclareMetadonneesPUAControl = new FormControl(false);
  createNotice: boolean;
  profilActif: string;
  profilInactif: string;
  validate: boolean;

  isStandalone: boolean = environment.standalone;

  subscriptions = new Subscription();
  externalIdentifierEnabled: boolean;

  constructor(
    public dialogRef: MatDialogRef<CreateNoticeComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PastisDialogDataCreate,
    private formBuilder: FormBuilder,
    private translateService: TranslateService,
    private popupService: PopupService,
    private fileService: FileService,
    private router: Router,
    private profileService: ProfileService,
    private applicationService: ApplicationService,
  ) {}

  ngOnInit() {
    this.typeProfile = this.data.profileMode;
    if (this.typeProfile === ProfileType.PUA) {
      this.modePUA = true;
    }
    this.applicationService
      .isApplicationExternalIdentifierEnabled(this.typeProfile === ProfileType.PUA ? 'ARCHIVE_UNIT_PROFILE' : 'PROFILE')
      .subscribe((value) => {
        this.externalIdentifierEnabled = value;
      });
    this.editNotice = this.router.url.substring(this.router.url.lastIndexOf('/') - 4, this.router.url.lastIndexOf('/')) === 'edit';
    if (this.editNotice) {
      this.validate = true;
      // Subscribe observer to notice
      this.subscriptions.add(
        this.fileService.noticeEditable.subscribe((value: Notice) => {
          this.notice = value;
        }),
      );
    } else {
      this.notice = {
        description: '',
        name: '',
        status: 'ACTIVE',
        identifier: '',
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
      { value: 'ACTIVE', viewValue: this.profilActif },
    ];
    this.information = "texte d'information";
    this.form = this.formBuilder.group({
      identifier: [null, Validators.required],
      intitule: [null, Validators.required],
      selectedStatus: [null],
      description: [null],
      autoriserPresenceMetadonnees: false,
    });

    this.subscriptions.add(
      this.presenceNonDeclareMetadonneesPUAControl.valueChanges.subscribe((value) => {
        this.form.controls.autoriserPresenceMetadonnees.setValue(value);
      }),
    );

    // Subscribe observer to button status and
    // set the inital state of the ok button to disabled

    this.subscriptions.add(
      this.popupService.btnYesShoudBeDisabled.subscribe((status) => {
        this.btnIsDisabled = status;
      }),
    );
  }

  translatedOnChange(): void {
    this.subscriptions.add(
      this.translateService.onLangChange.subscribe((_: LangChangeEvent) => {
        constantToTranslate.call(this);
      }),
    );
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_CHOICE_PATH + nameOfFieldToTranslate);
  }

  onCancel() {
    this.dialogRef.close();
  }

  upateButtonStatusAndDataToSend() {
    this.popupService.setPopUpDataOnClose('test');
    this.popupService.disableYesButton(true);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  // tslint:disable-next-line:use-lifecycle-interface
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
        this.profileService.checkPuaProfile(archivalProfileUnit).subscribe((response: boolean) => {
          if (response) {
            alert('Identifier already exists use another identifier');
            this.validate = false;
          } else {
            this.validate = true;
            this.checkIntitule();
          }
        }),
      );
    } else {
      const profile = {} as Profile;
      profile.identifier = this.notice.identifier;
      this.subscriptions.add(
        this.profileService.checkPaProfile(profile).subscribe((response: boolean) => {
          if (response) {
            alert('Identifier already exists use another identifier');
            this.validate = false;
          } else {
            this.validate = true;
            this.checkIntitule();
          }
        }),
      );
    }
  }

  checkIntitule() {
    this.validate = this.notice.name.length !== 0;
  }

  onSubmit() {
    if (!this.externalIdentifierEnabled) {
      this.form.controls.identifier.setValue(this.form.controls.intitule.value);
    }
    if (this.form.invalid) {
      return;
    }
    if (this.editNotice) {
      this.fileService.noticeEditable.next(this.notice);
      this.fileService.setNotice(true);
    }
    this.dialogRef.close({ success: true, action: 'none', data: this.form.value, mode: this.typeProfile });
  }
}
