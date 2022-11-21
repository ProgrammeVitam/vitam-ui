import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {PastisDialogDataCreate} from "../save-profile/save-profile.component";
import {LangChangeEvent, TranslateService} from "@ngx-translate/core";
import {ProfileService} from "../../core/services/profile.service";
import {ProfileDescription} from "../../models/profile-description.model";


const POPUP_CREATION_CHOICE_PATH = 'PROFILE.POP_UP_CREATION_NOTICE.CHOICE';

function constantToTranslate() {
  this.profilActif = this.translated('.PROFIL_ACTIF');
  this.profilInactif = this.translated('.PROFIL_INACTIF');
}

@Component({
  selector: 'select-notice',
  templateUrl: './select-notice.component.html',
  styleUrls: ['./select-notice.component.scss']
})
export class SelectNoticeComponent implements OnInit {

  profiles: ProfileDescription[];
  selectedProfile: ProfileDescription;
  validate: boolean;
  userValidation: boolean = false;
  showMessage: boolean;


  constructor(
    public dialogRef: MatDialogRef<SelectNoticeComponent>,
    @Inject(MAT_DIALOG_DATA) public data: PastisDialogDataCreate,
    private translateService: TranslateService,
    private profilService: ProfileService
  ) {
  }

  ngOnInit(): void {
    if (this.data.modeProfile === 'PUA') {
      this.profilService.getAllProfilesPUA().subscribe((profileListPUA: ProfileDescription[]) => {
        this.profiles = profileListPUA;
      })
    } else if (this.data.modeProfile === 'PA') {
      this.profilService.getAllProfilesPA().subscribe((profileListPUA: ProfileDescription[]) => {
        this.profiles = profileListPUA;
      })
    }

  }

  translatedOnChange(): void {
    this.translateService.onLangChange
      .subscribe((event: LangChangeEvent) => {
        constantToTranslate.call(this);
        console.log(event.lang);
      });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_CHOICE_PATH + nameOfFieldToTranslate);
  }

  onSubmit() {
    this.dialogRef.close({success: true, action: 'none', data: this.selectedProfile, mode: this.data.modeProfile});
  }

  onCancel() {
    this.dialogRef.close();
  }

  onNoClick(): void {
    this.dialogRef.close();
  }


  setValidate() {
    if (this.selectedProfile) {
      this.validate = true
    }
  }

  setUserValidation(bool: boolean) {
    this.userValidation = bool;
  }


}
