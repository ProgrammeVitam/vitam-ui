/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/).


This software is governed by the CeCILL-C  license under French law and
abiding by the rules of distribution of free software.  You can  use,
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info".

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability.

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or
data to be ensured and,  more generally, to use and operate it in the
same conditions as regards security.

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
*/
import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { environment } from '../../../environments/environment';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';

const POPUP_CREATION_CHOICE_PATH = 'PROFILE.POP_UP_CREATION.CHOICE';

function constantToTranslate() {
  this.firstChoice = this.translated('.FIRST_CHOICE');
  this.secondChoice = this.translated('.SECOND_CHOICE');
  this.title = this.translated('.TITLE');
}

@Component({
  selector: 'pastis-create-profile',
  templateUrl: './create-profile.component.html',
  styleUrls: [ './create-profile.component.scss' ]
})
export class CreateProfileComponent implements OnInit {
  firstChoice: string;
  secondChoice: string;
  title: string;
  profilPaChoice = true;
  isStandalone: boolean = environment.standalone;

  constructor(private dialogRef: MatDialogRef<CreateProfileComponent>, private translateService: TranslateService,
              @Inject(MAT_DIALOG_DATA) public data: PastisDialogData) {
  }

  ngOnInit() {
    if (!this.isStandalone) {
      constantToTranslate.call(this);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.firstChoice = 'PA';
      this.secondChoice = 'PUA';
      this.title = 'Sélectionner un profil d\'archivage :';
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

  onNoClick() {
    this.dialogRef.close();
  }

  onCancel() {
    this.dialogRef.close();
  }

  changeChoiceCreateProfile($event: string) {
    console.log($event);
    if ($event === this.firstChoice) {
      this.profilPaChoice = true;
    } else {
      this.profilPaChoice = false;
    }
  }

  onYesClick() {
    if (this.profilPaChoice) {
      this.dialogRef.close({ success: true, action: 'PA' });
    } else if (!this.profilPaChoice) {
      this.dialogRef.close({ success: true, action: 'PUA' });
    }
  }

}
