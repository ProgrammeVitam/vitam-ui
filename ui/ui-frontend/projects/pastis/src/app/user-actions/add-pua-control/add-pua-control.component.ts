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
import { Component, OnInit, TemplateRef } from '@angular/core';
import { SedaData, SedaElementConstants } from '../../models/seda-data';
import { FileNode } from '../../models/file-node';
import { FileService } from '../../core/services/file.service';
import { SedaService } from '../../core/services/seda.service';
import { MatDialogRef } from '@angular/material/dialog';
import { PastisDialogConfirmComponent } from '../../shared/pastis-dialog/pastis-dialog-confirm/pastis-dialog-confirm.component';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';
import { PopupService } from '../../core/services/popup.service';
import { Subscription } from 'rxjs';
import { PastisPopupMetadataLanguageService } from '../../shared/pastis-popup-metadata-language/pastis-popup-metadata-language.service';

@Component({
  selector: 'pastis-user-action-add-metadata',
  templateUrl: './add-pua-control.component.html',
  styleUrls: ['./add-pua-control.component.scss']
})
export class UserActionAddPuaControlComponent implements OnInit {

  btnIsDisabled: boolean;

  sedaData: SedaData;
  allowedChildren: string[] = [
    "Enumération",
    "Expression régulière",
    "Longueur Min/Max",
    "Valeur Min/Max"
  ]
  namesFiltered: any = [];
  sedaNodeFound: SedaData;
  selectedSedaNode: SedaData;
  addedItems: string[] = [];
  dialogData: PastisDialogData;

  atLeastOneIsSelected: boolean;
  customTemplate: TemplateRef<any>
  fileNode: FileNode;
  sedaLanguage: boolean;
  sedaLanguageSub: Subscription;


  constructor(public dialogRef: MatDialogRef<PastisDialogConfirmComponent>,
    private fileService: FileService, private sedaService: SedaService,
    private popUpService: PopupService, private sedaLanguageService: PastisPopupMetadataLanguageService) { }

  ngOnInit() {
    this.sedaLanguageSub = this.sedaLanguageService.sedaLanguage.subscribe(
      (value: boolean) => {
        this.sedaLanguage = value;
      },
      (error) => {
        console.log(error)
      }
    );
    this.fileService.nodeChange.subscribe(fileNode => { this.fileNode = fileNode })
    this.sedaData = this.sedaService.sedaRules[0];

    this.sedaNodeFound = this.fileNode.sedaData;

    // Subscribe observer to button status and
    // set the inital state of the ok button to disabled
    this.popUpService.btnYesShoudBeDisabled.subscribe(status => {
      this.btnIsDisabled = status;
    })
  }


  isElementSelected(element: string) {
    if (this.addedItems) {
      return this.addedItems.includes(element);
    }
  }

  onRemoveSelectedElement(element: string) {
    let indexOfElement = this.addedItems.indexOf(element)
    console.log(indexOfElement)
    if (indexOfElement >= 0) {
      this.allowedChildren.push(this.addedItems.splice(indexOfElement, 1)[0])
    }
    console.error(this.allowedChildren)
    this.addedItems.length > 0 ? this.atLeastOneIsSelected = true : this.atLeastOneIsSelected = false
    this.upateButtonStatusAndDataToSend();
  }

  onAddSelectedElement(element: string) {
    this.addedItems.push(element);

    this.allowedChildren = this.allowedChildren.filter(e => e != element);
    this.addedItems.length > 0 ? this.atLeastOneIsSelected = true : this.atLeastOneIsSelected = false
    this.upateButtonStatusAndDataToSend();
  }

  upateButtonStatusAndDataToSend() {
    this.popUpService.setPopUpDataOnClose(this.addedItems);
    this.popUpService.disableYesButton(!this.atLeastOneIsSelected)
  }

  onAllItemsAdded() {
    return this.allowedChildren.length === this.addedItems.length;
  }

  isElementComplex(element: SedaData) {
    if (element) {
      return element.Element === SedaElementConstants.complex;
    }
  }

  getDefinition(element: SedaData): string {
    return element ? element.Definition : '';
  }

  onYesClick(): void {
    console.log("Clicked ok on dialog : %o", this.selectedSedaNode);

  }
  onNoClick(): void {
    this.dialogRef.close();
  }

  onResolveName(element: SedaData): string {
    if (this.sedaLanguage) {
      return element.Name;
    }
    else {
      if (element.NameFr) {
        return element.NameFr;
      }
    }
    return element.Name;
  }
  ngOnDestroy(): void {
    if (this.sedaLanguageSub != null) {
      this.sedaLanguageSub.unsubscribe();
    }
  }

}
