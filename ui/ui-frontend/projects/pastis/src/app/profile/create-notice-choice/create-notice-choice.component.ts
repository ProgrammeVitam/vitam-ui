import { Component, Inject, OnInit } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { environment } from '../../../environments/environment';
import { ProfileType } from '../../models/profile-type.enum';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';

const POPUP_CREATION_CHOICE_PATH = 'PROFILE.POP_UP_CREATION.NOTICE_CHOICE';

function constantToTranslate() {
  this.firstChoice = this.translated('.FIRST_CHOICE');
  this.secondChoice = this.translated('.SECOND_CHOICE');
  this.title = this.translated('.TITLE');
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'create-notice-choice',
  templateUrl: './create-notice-choice.component.html',
  styleUrls: ['./create-notice-choice.component.scss'],
})
export class CreateNoticeChoiceComponent implements OnInit {
  firstChoice: string;
  secondChoice: string;
  title: string;
  noticePaChoice = true;
  isStandalone: boolean = environment.standalone;

  constructor(
    private dialogRef: MatDialogRef<CreateNoticeChoiceComponent>,
    private translateService: TranslateService,
    @Inject(MAT_DIALOG_DATA) public data: PastisDialogData,
  ) {}

  ngOnInit() {
    if (!this.isStandalone) {
      constantToTranslate.call(this);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.firstChoice = ProfileType.PA;
      this.secondChoice = ProfileType.PUA;
      this.title = 'Choisir le type de notice à créer :';
    }
  }

  translatedOnChange(): void {
    this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
      constantToTranslate.call(this);
      console.log(event.lang);
    });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POPUP_CREATION_CHOICE_PATH + nameOfFieldToTranslate);
  }

  onNoClick() {
    this.dialogRef.close();
  }

  onCancel() {
    this.dialogRef.close();
  }

  changeChoiceCreateProfile($event: string) {
    console.log($event);
    this.noticePaChoice = $event === this.firstChoice;
  }

  onYesClick() {
    if (this.noticePaChoice) {
      this.dialogRef.close({ success: true, action: ProfileType.PA });
    } else if (!this.noticePaChoice) {
      this.dialogRef.close({ success: true, action: ProfileType.PUA });
    }
  }
}
