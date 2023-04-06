import { OnDestroy } from '@angular/core';
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
import { Component, OnInit, Pipe, PipeTransform, TemplateRef } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { FileService } from '../../core/services/file.service';
import { PopupService } from '../../core/services/popup.service';
import { ProfileService } from '../../core/services/profile.service';
import { SedaService } from '../../core/services/seda.service';
import { FileNode } from '../../models/file-node';
import { SedaCardinalityConstants, SedaData, SedaElementConstants } from '../../models/seda-data';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';
import { PastisDialogConfirmComponent } from '../../shared/pastis-dialog/pastis-dialog-confirm/pastis-dialog-confirm.component';
import { PastisPopupMetadataLanguageService } from '../../shared/pastis-popup-metadata-language/pastis-popup-metadata-language.service';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'pastis-user-action-add-metadata',
  templateUrl: './add-metadata.component.html',
  styleUrls: [ './add-metadata.component.scss' ]
})
export class UserActionAddMetadataComponent implements OnInit, OnDestroy {

  btnIsDisabled: boolean;

  sedaData: SedaData;
  allowedChildren: SedaData[];
  filterName: string;
  namesFiltered: any = [];
  sedaNodeFound: SedaData;
  selectedSedaNode: SedaData;
  addedItems: SedaData[] = [];
  dialogData: PastisDialogData;

  atLeastOneIsSelected: boolean;
  customTemplate: TemplateRef<any>;
  fileNode: FileNode;
  sedaLanguage: boolean;
  sedaLanguageSub: Subscription;


  constructor(public dialogRef: MatDialogRef<PastisDialogConfirmComponent>,
              private fileService: FileService, private sedaService: SedaService,
              private popUpService: PopupService, private sedaLanguageService: PastisPopupMetadataLanguageService,
              private profileService: ProfileService) {
  }

  ngOnInit() {
    this.sedaLanguageSub = this.sedaLanguageService.sedaLanguage.subscribe(
      (value: boolean) => {
        this.sedaLanguage = value;
      },
      (error) => {
        console.log(error);
      }
    );
    this.fileService.nodeChange.subscribe(fileNode => {
      this.fileNode = fileNode;
    });
    this.sedaData = this.sedaService.sedaRules[0];

    this.sedaNodeFound = this.fileNode.sedaData;

    if (this.profileService.profileMode === 'PA') {
      this.allowedChildren = this.sedaService.findSelectableElementList(this.sedaNodeFound, this.fileNode)
        .filter(e => e.Element !== SedaElementConstants.attribute);
    } else if (this.profileService.profileMode === 'PUA') {
      if (this.fileNode.name === 'ArchiveUnit') {
        if (this.fileNode.children.map((nodeChildren: FileNode) => nodeChildren.name).includes('ArchiveUnitProfile')) {
          this.allowedChildren = this.sedaService.findSelectableElementList(this.sedaNodeFound, this.fileNode)
            .filter(e => e.Element !== SedaElementConstants.attribute)
            .filter(e => e.Name === 'Management');
        } else {
          this.allowedChildren = this.sedaService.findSelectableElementList(this.sedaNodeFound, this.fileNode)
            .filter(e => e.Element !== SedaElementConstants.attribute)
            .filter(e => e.Name === 'Management' || e.Name === 'ArchiveUnitProfile');
        }
      } else {
        this.allowedChildren = this.sedaNodeFound.Children.filter((e: SedaData) => e.Name !== 'id');
        if (this.fileNode.sedaData.Children.filter((e: SedaData) => e.Name.endsWith('Rule')).length > 0) {
          if (this.fileNode.children.filter((e: FileNode) => e.name === 'PreventInheritance').length > 0) {
            this.allowedChildren = this.allowedChildren.filter((e: SedaData) => e.Name !== 'RefNonRuleId');
          }
          if (this.fileNode.children.filter((e: FileNode) => e.name === 'RefNonRuleId').length > 0) {
            this.allowedChildren = this.allowedChildren.filter((e: SedaData) => e.Name !== 'PreventInheritance');
          }
        }

      }
      this.fileNode.children.forEach((child: FileNode) => {
        if (child.cardinality.endsWith('1')) {
          this.allowedChildren = this.allowedChildren.filter((e: SedaData) => e.Name !== child.name);
        }
      })
    }
    // Subscribe observer to button status and
    // set the inital state of the ok button to disabled
    this.popUpService.btnYesShoudBeDisabled.subscribe(status => {
      this.btnIsDisabled = status;
    });
  }

  selectSedaElement(selectedElements: string[]) {
    if (selectedElements.length) {
      this.selectedSedaNode = this.sedaService.getSedaNode(this.sedaData, selectedElements[0]);
    }
  }

  isElementSelected(element: SedaData) {
    if (this.addedItems) {
      return this.addedItems.includes(element);
    }
  }

  onRemoveSelectedElement(element: SedaData) {
    const indexOfElement = this.addedItems.indexOf(element);
    if (indexOfElement >= 0) {
      this.addedItems.splice(indexOfElement, 1);
    }
    if (element.Cardinality !== (SedaCardinalityConstants.zeroOrMore || SedaCardinalityConstants.oreOrMore)) {
      this.allowedChildren.push(element);
      this.allowedChildren = this.allowedChildren.slice(0, this.allowedChildren.length);
    }
    const orderedNames = Object.values(this.allowedChildren);
    this.allowedChildren.sort((a, b) => {
      return orderedNames.indexOf(a) - orderedNames.indexOf(b);
    });
    this.addedItems.length > 0 ? this.atLeastOneIsSelected = true : this.atLeastOneIsSelected = false;
    this.upateButtonStatusAndDataToSend();
  }

  onAddSelectedElement(element: SedaData) {
    this.addedItems.push(element);

    if (element.Cardinality.endsWith('1')) {
      this.allowedChildren = this.allowedChildren.filter(e => e !== element);
    }

    if (this.fileNode.sedaData.Children.filter((e: SedaData) => e.Name.endsWith('Rule')).length > 0) {
      if (element.Name === 'PreventInheritance') {
        this.allowedChildren = this.allowedChildren.filter((e: SedaData) => e.Name !== 'RefNonRuleId');
      }
      if (element.Name === 'RefNonRuleId') {
        this.allowedChildren = this.allowedChildren.filter((e: SedaData) => e.Name !== 'PreventInheritance');
      }
    }

    this.addedItems.length > 0 ? this.atLeastOneIsSelected = true : this.atLeastOneIsSelected = false;
    this.upateButtonStatusAndDataToSend();
  }

  upateButtonStatusAndDataToSend() {
    this.popUpService.setPopUpDataOnClose(this.addedItems);
    this.popUpService.disableYesButton(!this.atLeastOneIsSelected);
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

  onNoClick(): void {
    this.dialogRef.close();
  }

  public onSearchSubmit(search: string): void {
    this.filterName = search;
  }

  onResolveName(element: SedaData): string {
    if (this.sedaLanguage) {
      return element.Name;
    } else {
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

@Pipe({ name: 'filterByName' })
export class FilterByNamePipe implements PipeTransform {
  transform(listOfElements: SedaData[], nameToFilter: string, sedaLanguage: boolean): SedaData[] {
    if (!listOfElements) {
      return null;
    }
    if (!nameToFilter) {
      return listOfElements;
    }
    if (sedaLanguage) {
      return listOfElements.filter(element => element.Name !== undefined)
        .filter(element => element.Name.toLowerCase().indexOf(nameToFilter.toLowerCase()) >= 0);
    } else {
      return listOfElements
        .filter(element => element.NameFr !== undefined)
        .filter(element => element.NameFr.toLowerCase().indexOf(nameToFilter.toLowerCase()) >= 0);
    }

  }
}
