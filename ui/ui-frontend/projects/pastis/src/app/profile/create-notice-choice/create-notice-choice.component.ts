import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProfileMode } from '../../models/profile-response';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';

const POPUP_CREATION_CHOICE_PATH = 'PROFILE.POP_UP_CREATION.NOTICE_CHOICE';

function constantToTranslate() {
  this.firstChoice = this.translated('.FIRST_CHOICE');
  this.secondChoice = this.translated('.SECOND_CHOICE');
  this.title = this.translated('.TITLE');
}
@Component({
  selector: 'create-notice-choice',
  templateUrl: './create-notice-choice.component.html',
  styleUrls: ['./create-notice-choice.component.scss'],
})
export class CreateNoticeChoiceComponent implements OnInit, OnDestroy {
  firstChoice: string;
  isStandalone: boolean = environment.standalone;
  noticePaChoice = true;
  secondChoice: string;
  title: string;

  private subscriptions = new Subscription();

  constructor(
    private dialogRef: MatDialogRef<CreateNoticeChoiceComponent>,
    private translateService: TranslateService,
    @Inject(MAT_DIALOG_DATA) public data: PastisDialogData
  ) {}

  ngOnInit() {
    if (!this.isStandalone) {
      constantToTranslate.call(this);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.firstChoice = ProfileMode.PA;
      this.secondChoice = ProfileMode.PUA;
      this.title = 'Choisir le type de notice à créer :';
    }
  }

  translatedOnChange(): void {
    this.subscriptions.add(
      this.translateService.onLangChange.subscribe(() => {
        constantToTranslate.call(this);
      })
    );
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
    this.noticePaChoice = $event === this.firstChoice;
  }

  onYesClick() {
    if (this.noticePaChoice) {
      this.dialogRef.close({ success: true, action: ProfileMode.PA });
    } else if (!this.noticePaChoice) {
      this.dialogRef.close({ success: true, action: ProfileMode.PUA });
    }
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
