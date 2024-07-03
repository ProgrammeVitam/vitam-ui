import { Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { ProfileService } from '../../core/services/profile.service';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileType } from '../../models/profile-type.enum';
import { PastisDialogDataCreate } from '../save-profile/save-profile.component';

const POPUP_CREATION_CHOICE_PATH = 'PROFILE.POP_UP_CREATION_NOTICE.CHOICE';

function constantToTranslate() {
  this.profilActif = this.translated('.PROFIL_ACTIF');
  this.profilInactif = this.translated('.PROFIL_INACTIF');
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'select-notice',
  templateUrl: './select-notice.component.html',
  styleUrls: ['./select-notice.component.scss'],
})
export class SelectNoticeComponent implements OnInit {
  profiles: ProfileDescription[];
  selectedProfile: ProfileDescription;
  validate: boolean;
  userValidation = false;
  showMessage: boolean;

  constructor(
    public dialogRef: MatDialogRef<SelectNoticeComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PastisDialogDataCreate,
    private translateService: TranslateService,
    private profilService: ProfileService,
  ) {}

  ngOnInit(): void {
    if (this.data.profileType === ProfileType.PUA) {
      this.profilService.getAllProfilesPUA().subscribe((profileListPUA: ProfileDescription[]) => {
        this.profiles = profileListPUA;
      });
    } else if (this.data.profileType === ProfileType.PA) {
      this.profilService.getAllProfilesPA().subscribe((profileListPUA: ProfileDescription[]) => {
        this.profiles = profileListPUA;
      });
    }
  }

  translatedOnChange(): void {
    this.translateService.onLangChange.subscribe((_: LangChangeEvent) => {
      constantToTranslate.call(this);
    });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_CHOICE_PATH + nameOfFieldToTranslate);
  }

  onSubmit() {
    this.dialogRef.close({
      success: true,
      action: 'none',
      data: this.selectedProfile,
      profileType: this.data.profileType,
      profileVersion: this.data.profileVersion,
    });
  }

  onCancel() {
    this.dialogRef.close();
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  setValidate() {
    if (this.selectedProfile) {
      this.validate = true;
    }
  }

  setUserValidation(bool: boolean) {
    this.userValidation = bool;
  }
}
