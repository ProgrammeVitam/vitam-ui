import {Component, Inject, OnInit} from '@angular/core';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {Router} from '@angular/router';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {environment} from '../../../environments/environment';
import {PastisDialogData} from '../../shared/pastis-dialog/classes/pastis-dialog-data';

const POP_UP_SAVE_CHOICE_PATH = 'PROFILE.POP_UP_SAVE.CHOICE';

function constantToTranslate() {
  this.firstChoiceEnregistrement = this.translated('.FIRST_CHOICE_ENREGISTREMENT');
  this.secondChoiceEnregistrement = this.translated('.SECOND_CHOICE_ENREGISTREMENT');
  this.titleEnregistrement = this.translated('.TITLE_ENREGISTREMENT');
  this.firstChoiceGestionNotice = this.translated('.FIRST_CHOICE_GESTION_NOTICE');
  this.secondChoiceGestionNotice = this.translated('.SECOND_CHOICE_GESTION_NOTICE');
  this.titleGestionNotice = this.translated('.TITLE_GESTION_NOTICE');

  this.okLabelNext = this.translated('.ENREGISTREMENT_OK_LABEL_NEXT');
  this.okLabelTerminate = this.translated('.ENREGISTREMENT_OK_LABEL_TERMINATE');
}

@Component({
  selector: 'save-profile-options',
  templateUrl: './save-profile-options.component.html',
  styleUrls: ['./save-profile-options.component.scss']
})
export class SaveProfileOptionsComponent implements OnInit {
  firstChoiceEnregistrement: string;
  secondChoiceEnregistrement: string;
  titleEnregistrement: string;

  firstChoiceGestionNotice: string;
  secondChoiceGestionNotice: string;
  titleGestionNotice: string;

  okLabelTerminate: string;
  okLabelNext: string;

  valueSelected: boolean;
  gestionNotice: boolean;
  isStandalone: boolean = environment.standalone;
  editProfile: boolean;


  constructor(public dialogRef: MatDialogRef<SaveProfileOptionsComponent>, private translateService: TranslateService,
              @Inject(MAT_DIALOG_DATA) public data: PastisDialogData, private router: Router) {
    this.editProfile = this.router.url.substring(this.router.url.lastIndexOf('/') - 4, this.router.url.lastIndexOf('/')) === 'edit';
  }

  ngOnInit(): void {
    if (!this.isStandalone) {
      constantToTranslate.call(this);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.firstChoiceEnregistrement = 'Local';
      this.secondChoiceEnregistrement = 'SAE';
      this.titleEnregistrement = 'Où souhaitez-vous l’enregistrer ?';

      this.firstChoiceGestionNotice = 'Création d\'une nouvelle notice';
      this.secondChoiceGestionNotice = 'Rattachement à une notice existante';
      this.titleGestionNotice = 'Gestion de la notice du profil';

      this.okLabelTerminate = 'TERMINER';
      this.okLabelNext = 'SUIVANT';
    }
    this.valueSelected = false;
    this.gestionNotice = true;
  }

  translatedOnChange(): void {
    this.translateService.onLangChange
      .subscribe((_: LangChangeEvent) => {
        constantToTranslate.call(this);
      });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(POP_UP_SAVE_CHOICE_PATH + nameOfFieldToTranslate);
  }

  changeStatus($event: string) {
    if ($event === this.secondChoiceEnregistrement) {
      this.data.okLabel = this.okLabelNext;
      this.valueSelected = true;
    } else {
      this.data.okLabel = this.okLabelTerminate;
      this.valueSelected = false;
    }
  }

  changeStatusGestionNoticeProfil($event: string) {
    if ($event === this.firstChoiceGestionNotice) {
      this.gestionNotice = true;
    } else {
      this.gestionNotice = false;
    }
  }

  onNoClick() {
    this.dialogRef.close();
  }

  onCancel() {
    this.dialogRef.close();
  }

  onYesClick() {
    if (this.valueSelected && this.gestionNotice) {
      this.dialogRef.close({success: true, action: 'creation'});
    } else if (!this.valueSelected) {
      this.dialogRef.close({success: true, action: 'local'});
    } else if (this.valueSelected && !this.gestionNotice) {
      this.dialogRef.close({success: true, action: 'rattachement'});
    }
  }
}
