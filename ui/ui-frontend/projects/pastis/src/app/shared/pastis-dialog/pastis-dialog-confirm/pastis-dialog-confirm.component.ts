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
import {ComponentPortal} from '@angular/cdk/portal';
import {Component, Inject, OnInit, } from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {TranslateService} from '@ngx-translate/core';
import {PopupService} from '../../../core/services/popup.service';
import {SedaService} from '../../../core/services/seda.service';
import {PastisDialogData} from '../classes/pastis-dialog-data';
const PASTIS_DIALOG_CONFIRM_TRANSLATE_PATH = 'PASTIS_DIALOG_CONFIRM';
@Component({
  selector: 'pastis-pastis-dialog-confirm',
  templateUrl: './pastis-dialog-confirm.component.html',
  styleUrls: ['./pastis-dialog-confirm.component.scss']
})
export class PastisDialogConfirmComponent implements OnInit {

  portal: ComponentPortal<any>;

  dataBeforeClose: any;

  btnYesShouldBeDisabled: boolean ;

  popupValider: string = this.translated('.POPUP_VALIDER');
  popupAnnuler: string = this.translated('.POPUP_ANNULER');

  constructor(
    public dialogConfirmRef: MatDialogRef<PastisDialogConfirmComponent>,
    @Inject(MAT_DIALOG_DATA) public dialogReceivedData: PastisDialogData,
    public sedaService: SedaService, private popUpService: PopupService,
    private translateService: TranslateService) {
  }


  ngOnInit() {
    console.log('Data received on confirm dialog : %o', this.dialogReceivedData);
    if (this.dialogReceivedData.component) {
      this.portal = new ComponentPortal(this.dialogReceivedData.component);
      this.popUpService.setPopUpDataOnOpen(this.dialogReceivedData);
    }
    if (!this.dialogReceivedData.okLabel) { this.dialogReceivedData.okLabel = this.popupValider; }

    if (!this.dialogReceivedData.cancelLabel) { this.dialogReceivedData.cancelLabel = this.popupAnnuler; }

    this.popUpService.popUpDataBeforeClose.subscribe(data => {
        this.dataBeforeClose = data;
      });
    this.popUpService.btnYesShoudBeDisabled.subscribe(shouldDisableButton => {
          this.btnYesShouldBeDisabled = shouldDisableButton;
      });
    this.popUpService.btnYesShoudBeDisabled.next(this.dialogReceivedData.disableBtnOuiOnInit);

  }

  onNoClick(): void {
    console.log('Clicked no ');
    this.popUpService.btnYesShoudBeDisabled.next(false);
    this.dialogConfirmRef.close();
  }

  onYesClick(): void {
    console.log('Clicked ok on dialog and send data : %o', this.dataBeforeClose);
  }

  getToolTipData(data: any) {
    if (data && data.length) {
      return data.nodeName;
    }
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(PASTIS_DIALOG_CONFIRM_TRANSLATE_PATH + nameOfFieldToTranslate);
  }

  ngOnDestroy() {

  }


}
