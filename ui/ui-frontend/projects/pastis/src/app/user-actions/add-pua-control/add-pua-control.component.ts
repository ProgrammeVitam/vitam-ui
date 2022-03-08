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
import { Component, OnInit } from '@angular/core';
import { SedaData } from '../../models/seda-data';
import { MatDialogRef } from '@angular/material/dialog';
import { PastisDialogConfirmComponent } from '../../shared/pastis-dialog/pastis-dialog-confirm/pastis-dialog-confirm.component';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';
import { PopupService } from '../../core/services/popup.service';

@Component({
  selector: 'pastis-user-action-add-metadata',
  templateUrl: './add-pua-control.component.html',
  styleUrls: ['./add-pua-control.component.scss']
})
export class UserActionAddPuaControlComponent implements OnInit {

  btnIsDisabled: boolean;

  sedaData: SedaData;
  enumerationsLabel: string = "Enumération";
  expressionReguliereLabel: string = "Expression régulière";
  lengthMinMaxLabel: string = "Longueur Min/Max";
  valueMinMaxLabel: string = "Valeur Min/Max";
  enumerationsDefinition: string = "Signaler les valeurs autorisées";
  expressionReguliereDefinition: string = "Définir une expression régulière pour la valeur de la métadonnée";
  allowedChildren: string[];
  addedItems: string[] = [];
  dialogData: PastisDialogData;

  atLeastOneIsSelected: boolean;


  constructor(public dialogRef: MatDialogRef<PastisDialogConfirmComponent>,
    private popUpService: PopupService) {
      this.refreshAllowedChildren();
    }

  ngOnInit() {
    // Subscribe observer to button status and
    // set the inital state of the ok button to disabled
    this.popUpService.btnYesShoudBeDisabled.subscribe(status => {
      this.btnIsDisabled = status;
    })
  }

  onRemoveSelectedElement(element: string) {
    if(this.isExclusive(element)){
      this.refreshAllowedChildren();
    }else{
      let indexOfElement = this.addedItems.indexOf(element)
      if (indexOfElement >= 0) {
        this.allowedChildren.push(this.addedItems.splice(indexOfElement, 1)[0])
      }
    }
    this.addedItems.length > 0 ? this.atLeastOneIsSelected = true : this.atLeastOneIsSelected = false
    this.upateButtonStatusAndDataToSend();
  }

  onAddSelectedElement(element: string) {
    this.addedItems.push(element);
    if(this.isExclusive(element)){
      this.refreshAllowedChildren(element);
    }else{
      this.allowedChildren = this.allowedChildren.filter(e => e != element);
    }
    this.addedItems.length > 0 ? this.atLeastOneIsSelected = true : this.atLeastOneIsSelected = false
    this.upateButtonStatusAndDataToSend();
  }

  upateButtonStatusAndDataToSend() {
    this.popUpService.setPopUpDataOnClose(this.addedItems);
    this.popUpService.disableYesButton(!this.atLeastOneIsSelected)
  }

  getDefinition(element: string): string {
    if(element === this.enumerationsLabel){
      return this.enumerationsDefinition
    }
    if(element === this.expressionReguliereLabel){
      return this.expressionReguliereDefinition
    }
    return '';
  }

  ngOnDestroy(): void {
  }

  isExclusive(element: string): boolean{
    return element === this.valueMinMaxLabel || element === this.enumerationsLabel;
  }

  refreshAllowedChildren(element?: string){

    if(element){
      this.addedItems = [element];
      this.allowedChildren = [];
    }else{
      this.allowedChildren = [
        this.enumerationsLabel,
        this.valueMinMaxLabel,
        this.lengthMinMaxLabel,
        this.expressionReguliereLabel
      ];
      this.addedItems = [];
    }
  }

}
