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
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { FileService } from 'projects/pastis/src/app/core/services/file.service';
import { PopupService } from 'projects/pastis/src/app/core/services/popup.service';
import { SedaService } from 'projects/pastis/src/app/core/services/seda.service';
import { PastisDialogData } from 'projects/pastis/src/app/shared/pastis-dialog/classes/pastis-dialog-data';
import { PastisPopupMetadataLanguageService } from 'projects/pastis/src/app/shared/pastis-popup-metadata-language/pastis-popup-metadata-language.service';
import { Subscription } from 'rxjs';
import { AttributeData } from '../../../../models/edit-attribute-models';
import { CardinalityConstants, DataTypeConstants, FileNode, TypeConstants, ValueOrDataConstants } from '../../../../models/file-node';
import { SedaData } from '../../../../models/seda-data';
import { FileTreeMetadataService } from '../file-tree-metadata.service';

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'pastis-edit-attributes',
  templateUrl: './attributes.component.html',
  styleUrls: ['./attributes.component.scss'],
})
export class AttributesPopupComponent implements OnInit, OnDestroy {
  displayedColumns: string[] = ['selected', 'nomDuChamp', 'valeurFixe', 'commentaire'];

  attributeCardinalities: string[];

  elementSedaCardinality: string;

  selectedValue: string[];

  parentFileNode: FileNode;

  selected = -1;

  // The datasource used by the DataTable in the popup
  // It's data contains the list of Attributes to display
  matDataSource: MatTableDataSource<AttributeData>;

  sedaLanguage: boolean;
  sedaLanguageSub: Subscription;

  constructor(
    public dialogRef: MatDialogRef<AttributesPopupComponent>,
    @Inject(MAT_DIALOG_DATA) public dialogReceivedData: PastisDialogData,
    private sedaService: SedaService,
    private fileService: FileService,
    private fileTreeMetadataService: FileTreeMetadataService,
    private popUpService: PopupService,
    private sedaLanguageService: PastisPopupMetadataLanguageService,
  ) {}

  ngOnInit() {
    this.sedaLanguageSub = this.sedaLanguageService.sedaLanguage.subscribe(
      (value: boolean) => {
        this.sedaLanguage = value;
      },
      (error) => {
        console.log(error);
      },
    );

    this.fileService.currentTree.subscribe((fileTree) => {
      if (fileTree) {
        this.parentFileNode = fileTree[0];
      }
    });
    this.matDataSource = this.getDataSource(this.dialogReceivedData.fileNode.sedaData, this.dialogReceivedData.fileNode);
    this.initAttributeCardinality();
    // Subscribe any datasource change to setPopUpDataOnClose
    setTimeout(() => {
      this.popUpService.setPopUpDataOnClose(this.matDataSource.data);
    }, 50);
  }

  // Checks if a file node has an atttribute child
  initAttributeCardinality() {
    // tslint:disable-next-line:forin
    for (const index in this.matDataSource.data) {
      const fileNode = this.dialogReceivedData.fileNode;
      const att = this.matDataSource.data[index];
      const attSedaData = fileNode.sedaData.Children.find((child: { Name: string }) => child.Name === att.nomDuChamp);
      if (attSedaData.Cardinality === CardinalityConstants.Obligatoire) {
        this.matDataSource.data[index].selected = true;
      } else {
        this.matDataSource.data[index].selected = att.selected;
      }
    }
  }

  setElementComment(elementName: string, newComment: string) {
    for (const idx in this.matDataSource.data) {
      if (this.matDataSource.data[idx].nomDuChamp === elementName) {
        this.matDataSource.data[idx].commentaire = newComment;
      }
    }
    // console.log('ParentFileNode : ', this.parentFileNode);
    for (const node of this.parentFileNode.children) {
      if (node.name === elementName) {
        node.documentation = newComment;
      }
    }
  }

  setElementValue(elementName: string, newValue: string) {
    for (const idx in this.matDataSource.data) {
      if (this.matDataSource.data[idx].nomDuChamp === elementName) {
        this.matDataSource.data[idx].valeurFixe = newValue;
      }
    }
    for (const node of this.parentFileNode.children) {
      if (node.name === elementName) {
        node.value = newValue;
      }
    }
  }

  /**
   * Function that computes the "checked" state of the "select all" checkbox
   * If all checkboxs are checked, then the "select all" checkbox is checked
   */
  isChecked(): boolean {
    return this.matDataSource.data.filter((a) => !a.selected).length === 0;
  }

  isSedaObligatory(attribute: AttributeData): boolean {
    if (attribute) {
      const popUpData = this.popUpService.getPopUpDataOnOpen() as PastisDialogData;
      if (popUpData) {
        const popSendSedaNodeFilted = popUpData.fileNode.sedaData.Children.find(
          (child: { Name: string }) => child.Name === attribute.nomDuChamp,
        );
        return popSendSedaNodeFilted.Cardinality.startsWith('1');
      }
    }
    return;
  }

  toggleAllAttributes(toggleAllCheckChange: MatCheckboxChange): void {
    const istoggleAllChecked = toggleAllCheckChange.checked;
    this.matDataSource.data.forEach((a) => {
      this.isSedaObligatory(a) ? (a.selected = true) : (a.selected = istoggleAllChecked);
      a.selectedCardinality = '1';
    });
  }

  toggleAttribute(change: MatCheckboxChange, elementName: string): void {
    const element = this.matDataSource.data.find((a) => a.nomDuChamp === elementName);
    element.selected = change.checked;
  }

  /**
   * Returns the modified FileNode from the popup
   *
   * It parses the datasource of the DataTable to collect the attributes
   * and add them to the modified FileNode
   */
  getFileNodeFromPopup(): FileNode {
    // We get the original FileNode that was passed to the popup
    const fileNode: FileNode = this.dialogReceivedData.fileNode;

    this.fileService.deleteAllAttributes(fileNode);

    // Map all selected AttributeData to FileNode and add them as children of the fileNode
    this.matDataSource.data
      .filter((attributeData) => attributeData.selected)
      .forEach((attributeData) => {
        const attributeFileNode: FileNode = {} as FileNode;
        attributeFileNode.id = window.crypto.getRandomValues(new Uint32Array(10))[0];
        attributeFileNode.cardinality = attributeData.selectedCardinality;
        attributeFileNode.children = [];
        attributeFileNode.dataType =
          DataTypeConstants[
            fileNode.sedaData.Children.find(
              (child) => child.Name === attributeData.nomDuChamp,
            ).Type.toString() as keyof typeof DataTypeConstants
          ];
        attributeFileNode.documentation = attributeData.commentaire ? attributeData.commentaire : null;
        attributeFileNode.level = fileNode.level + 1;
        attributeFileNode.name = attributeData.nomDuChamp;
        attributeFileNode.parentId = fileNode.id;
        attributeFileNode.type = TypeConstants.attribute;
        attributeFileNode.value = attributeData.valeurFixe ? attributeData.valeurFixe : null;
        attributeFileNode.valueOrData = ValueOrDataConstants.value;
        // Add the attribute to the filenode
      });

    return fileNode;
  }

  /**
   * Get the datasource required to feed the datatable in the popup
   *
   * This datasource consists of a list of AttributeData
   *
   * @param sedaNode The seda definition of the node we want to edit
   * @param fileNode The node which we want to edit attributes
   */
  getDataSource(sedaNode: SedaData, fileNode: FileNode): MatTableDataSource<AttributeData> {
    const attributeDataList: AttributeData[] = [];
    // Loop on all the attributes available for the node in the seda definition
    // Maps all the attributes node to AttributesData object
    this.sedaService.getAttributes(sedaNode, sedaNode.Collection).forEach((sedaAttribute) => {
      const attributeData: AttributeData = {} as AttributeData;

      attributeData.nomDuChamp = sedaAttribute.Name;
      attributeData.type = sedaAttribute.Element;

      // Check if the attribute is already added to the current node
      const fileAttribute = fileNode.children.find((child) => child.name === attributeData.nomDuChamp) as FileNode;
      // let mattAttFound = this.matDataSource.data.find(att=> att.nomDuChamp === fileAttribute.name);
      if (fileAttribute) {
        // If the attribute is present in the FileNode
        // We fill in the fields with the corresponding values
        attributeData.valeurFixe = fileAttribute.value;
        attributeData.selected = true;
        attributeData.id = fileAttribute.id;
        attributeData.commentaire = fileAttribute.documentation;
        attributeData.cardinalities = this.fileTreeMetadataService.allowedCardinality.get(fileAttribute.cardinality);
        attributeData.selectedCardinality = fileAttribute.cardinality;
        attributeData.enumeration = sedaAttribute.Enumeration;
        attributeData.valeurFixe = fileAttribute.value;
      } else {
        // If the attribute is not present, we fill in defaults values
        attributeData.valeurFixe = null;
        attributeData.selected = false;
        attributeData.commentaire = null;
        attributeData.id = window.crypto.getRandomValues(new Uint32Array(10))[0];
        attributeData.cardinalities = this.fileTreeMetadataService.allowedCardinality.get(sedaAttribute.Cardinality);
        attributeData.selectedCardinality = null;
        attributeData.enumeration = sedaAttribute.Enumeration;
      }
      attributeDataList.push(attributeData);
    });
    // Create and return the datasource with the attribute's data
    const result = new MatTableDataSource<AttributeData>(attributeDataList);
    return result;
  }

  getAttributeInputType(element: AttributeData) {
    if (element.enumeration.length > 0) {
      return 'enumeration';
    }
  }

  getSedaDefinition(elementName: string) {
    if (this.dialogReceivedData.fileNode.sedaData) {
      for (const node of this.dialogReceivedData.fileNode.sedaData.Children) {
        if (node.Name === elementName) {
          return node.Definition;
        }
      }
    }
    return '';
  }

  onResolveName(elementName: string): string {
    if (this.dialogReceivedData.fileNode.sedaData) {
      for (const node of this.dialogReceivedData.fileNode.sedaData.Children) {
        if (node.Name === elementName) {
          if (this.sedaLanguage) {
            return node.Name;
          } else {
            if (node.NameFr) {
              return node.NameFr;
            }
            return node.Name;
          }
        }
      }
    }
    return '';
  }
  ngOnDestroy(): void {
    if (this.sedaLanguageSub != null) {
      this.sedaLanguageSub.unsubscribe();
    }
  }
}
