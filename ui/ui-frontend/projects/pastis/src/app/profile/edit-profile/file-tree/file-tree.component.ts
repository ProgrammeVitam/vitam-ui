/*
Copyright © CINES - Centre Informatique National pour l'Enseignement Supérieur (2020)

[dad@cines.fr]

This software is a computer program whose purpose is to provide
a web application to create, edit, import and export archive
profiles based on the french SEDA standard
(https://redirect.francearchives.fr/seda/)


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
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { Component, Input, OnDestroy, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, Subscription, throwError } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { FileService } from '../../../core/services/file.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ProfileService } from '../../../core/services/profile.service';
import { SedaService } from '../../../core/services/seda.service';
import { CardinalityConstants, DataTypeConstants, FileNode, TypeConstants } from '../../../models/file-node';
import { PUA } from '../../../models/pua.model';
import { SedaCardinalityConstants, SedaData, SedaElementConstants } from '../../../models/seda-data';
import { PastisDialogData } from '../../../shared/pastis-dialog/classes/pastis-dialog-data';
import { PastisPopupMetadataLanguageService } from '../../../shared/pastis-popup-metadata-language/pastis-popup-metadata-language.service';
import { UserActionAddMetadataComponent } from '../../../user-actions/add-metadata/add-metadata.component';
import { DuplicateMetadataComponent } from '../../../user-actions/duplicate-metadata/duplicate-metadata.component';
import { UserActionRemoveMetadataComponent } from '../../../user-actions/remove-metadata/remove-metadata.component';
import { FileTreeMetadataService } from '../file-tree-metadata/file-tree-metadata.service';
import { FileTreeService } from './file-tree.service';

const FILE_TREE_TRANSLATE_PATH = 'PROFILE.EDIT_PROFILE.FILE_TREE';

function constantToTranslate() {
  this.notificationRemoveSuccessOne = this.translated('.NOTIFICATION_REMOVE_SUCCESS_ONE');
  this.notificationRemoveSuccessTwo = this.translated('.NOTIFICATION_REMOVE_SUCCESS_TWO');
  this.notificationDuplicateSuccessOne = this.translated('.NOTIFICATION_DUPLICATE_SUCCESS_ONE');
  this.notificationDuplicateSuccessTwo = this.translated('.NOTIFICATION_DUPLICATE_SUCCESS_TWO');
  this.notificationAddMetadonneePOne = this.translated('.NOTIFICATION_ADD_MEDATADONNEE_PONE');
  this.notificationAddMetadonneePTwo = this.translated('.NOTIFICATION_ADD_METADONNEE_PTWO');
  this.notificationAddmetadonneeSOne = this.translated('.NOTIFICATION_ADD_METADONNEE_SONE');
  this.notificationAddmetadonneeSTwo = this.translated('.NOTIFICATION_ADD_METADONNEE_STWO');
  this.notificationAjoutMetadonneeFileTree = this.translated('.NOTIFICATION_ADD_METADONNEE_FILE_TREE');
  this.popupRemoveSedaElementAttribut = this.translated('.POPUP_REMOVE_SEDA_ELEMENT_ATTRIBUT');
  this.popupRemoveSedaElementMetadonnee = this.translated('.POPUP_REMOVE_SEDA_ELEMENT_METADONNEE');
  this.popupRemoveTitre = this.translated('.POPUP_REMOVE_TITRE');
  this.popupRemoveSousTitreAttribut = this.translated('.POPUP_REMOVE_SOUS_TITRE_ATTRIBUT');
  this.popupRemoveSousTitreMetadonnee = this.translated('.POPUP_SOUS_TITRE_METADONNEE');
  this.popupRemoveDeleteTypeTextM = this.translated('.POPUP_REMOVE_DELETE_TYPE_TEXT_M');
  this.popupRemoveDeleteTypeTextF = this.translated('.POPUP_REMOVE_DELETE_TYPE_TEXT_F');
  this.popupAddCancelLabel = this.translated('.POPUP_ADD_CANCEL_LABEL');
  this.popupAddTitleDialog = this.translated('.POPUP_ADD_TITLE_DIALOG');
  this.popupAddSubTitleDialog = this.translated('.POPUP_ADD_SUBTITLE_DIALOG');
  this.popupAddOkLabel = this.translated('.POPUP_ADD_OK_LABEL');
  this.popupDuplicateSedaElementAttribut = this.translated('.POPUP_DUPLICATE_SEDA_ELEMENT_ATTRIBUT');
  this.popupDuplicateSedaElementMetadonnee = this.translated('.POPUP_DUPLICATE_SEDA_ELEMENT_METADONNEE');
  this.popupDuplicateTitre = this.translated('.POPUP_DUPLICATE_TITRE');
  this.popupDuplicateTitreTwo = this.translated('.POPUP_DUPLICATE_TITRE_TWO');
  this.popupDuplicateSousTitreAttribut = this.translated('.POPUP_DUPLICATE_SOUS_TITRE_ATTRIBUT');
  this.popupDuplicateSousTitreMetadonnee = this.translated('.POPUP_DUPLICATE_SOUS_TITRE_METADONNEE');
  this.popupDuplicateDeleteTypeTextM = this.translated('.POPUP_DUPLICATE_DELETE_TYPE_TEXT_M');
  this.popupDuplicateDeleteTypeTextF = this.translated('.POPUP_DUPLICATE_DELETE_TYPE_TEXT_F');
}

@Component({
  selector: 'pastis-file-tree',
  templateUrl: './file-tree.component.html',
  styleUrls: ['./file-tree.component.scss'],
})
export class FileTreeComponent implements OnDestroy {
  constructor(
    private fileService: FileService,
    private loggingService: NotificationService,
    private fileMetadataService: FileTreeMetadataService,
    private sedaService: SedaService,
    private sedaLanguageService: PastisPopupMetadataLanguageService,
    public fileTreeService: FileTreeService,
    private translateService: TranslateService,
    public profileService: ProfileService
  ) {}

  static archiveUnits: FileNode;
  static archiveUnitsNumber: number;
  static uaIdAndPosition = new Map<any, number>();

  @ViewChild('treeSelector', { static: true }) tree: any;
  @ViewChild('autosize', { static: false }) autosize: CdkTextareaAutosize;

  @Input()
  rootElementName: string;
  @Input()
  rootElementShowName: string;
  @Input()
  childrenListToExclude: string[];
  @Input()
  childrenListToInclude: string[];
  @Input()
  collectionName: string;
  @Input()
  activeTabIndex: number;

  isStandalone: boolean = environment.standalone;

  data: FileNode;
  newNodeName: string;
  sedaData: SedaData;
  treeData: FileNode[];
  curentRootTabName: string;
  parentNodeMap = new Map<FileNode, FileNode>();
  dataChange = new BehaviorSubject<FileNode>(null);
  rulesChange: string[][] = [];
  rootMetadataName: string;
  selectedItemList: FileNode;
  sedaLanguage: boolean;
  sedaLanguageSub: Subscription;
  viewChild: FileNode[] = [];

  notificationRemoveSuccessOne: string;
  notificationRemoveSuccessTwo: string;
  notificationAddMetadonneePOne: string;
  notificationAddMetadonneePTwo: string;
  notificationAddmetadonneeSOne: string;
  notificationAddmetadonneeSTwo: string;
  notificationAjoutMetadonneeFileTree: string;
  notificationDuplicateSuccessOne: string;
  notificationDuplicateSuccessTwo: string;
  popupRemoveSedaElementAttribut: string;
  popupRemoveSedaElementMetadonnee: string;
  popupRemoveTitre: string;
  popupRemoveSousTitreAttribut: string;
  popupRemoveSousTitreMetadonnee: string;
  popupRemoveDeleteTypeTextM: string;
  popupRemoveDeleteTypeTextF: string;
  popupAddCancelLabel: string;
  popupAddTitleDialog: string;
  popupAddSubTitleDialog: string;
  popupAddOkLabel: string;
  popupDuplicateSedaElementAttribut: string;
  popupDuplicateSedaElementMetadonnee: string;
  popupDuplicateTitre: string;
  popupDuplicateTitreTwo: string;
  popupDuplicateSousTitreAttribut: string;
  popupDuplicateSousTitreMetadonnee: string;
  popupDuplicateDeleteTypeTextM: string;
  popupDuplicateDeleteTypeTextF: string;
  text: string;

  nonEditFileNode: boolean = false;

  private _fileServiceTabChildrenRulesChange: Subscription;
  private _fileServiceCollectionName: Subscription;
  private _fileServiceRootTabMetadataName: Subscription;
  private _fileTreeServiceUpdateMedataTable: Subscription;

  ngOnInit() {
    if (!this.isStandalone) {
      constantToTranslate.call(this);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.notificationRemoveSuccessOne = ' a été ';
      this.notificationRemoveSuccessTwo = 'avec succès';
      this.notificationDuplicateSuccessOne = ' a été ';
      this.notificationDuplicateSuccessTwo = 'avec succès';
      this.notificationAddMetadonneePOne = 'Les métadonnées';
      this.notificationAddMetadonneePTwo = ' ont été ajoutées';
      this.notificationAddmetadonneeSOne = 'La métadonnée';
      this.notificationAddmetadonneeSTwo = ' a été ajoutée';
      this.notificationAjoutMetadonneeFileTree = 'La métadonnée ArchiveUnit a été ajoutée';
      this.popupRemoveSedaElementAttribut = "L'attribut";
      this.popupRemoveSedaElementMetadonnee = 'La métadonnée ';
      this.popupRemoveTitre = 'Voulez-vous supprimer';
      this.popupRemoveSousTitreAttribut = "Suppression d'un attribut";
      this.popupRemoveSousTitreMetadonnee = "Suppression d'une métadonnée";
      this.popupRemoveDeleteTypeTextM = 'supprimé ';
      this.popupRemoveDeleteTypeTextF = 'supprimée ';
      this.popupAddCancelLabel = 'Annuler';
      this.popupAddTitleDialog = 'Veuillez sélectionner une ou plusieurs métadonnées';
      this.popupAddSubTitleDialog = 'Ajouter des métadonnées à';
      this.popupAddOkLabel = 'Ajouter les métadonnées';
      this.popupDuplicateSedaElementAttribut = "L'attribut";
      this.popupDuplicateSedaElementMetadonnee = ' la métadonnée ';
      this.popupDuplicateTitre = 'Voulez-vous dupliquer';
      this.popupDuplicateSousTitreAttribut = "Duplication d'un attribut";
      this.popupDuplicateSousTitreMetadonnee = "Duplication d'une métadonnée";
      this.popupDuplicateDeleteTypeTextM = 'dupliqué ';
      this.popupDuplicateDeleteTypeTextF = 'dupliquée ';
      this.popupDuplicateTitreTwo = 'son contenu et son paramétrage (cardinalités et commentaire)';
    }
    this.sedaLanguageSub = this.sedaLanguageService.sedaLanguage.subscribe(
      (value: boolean) => {
        this.sedaLanguage = value;
      },
      (error) => {
        console.log(error);
      }
    );
    this.sedaData = this.sedaService.sedaRules[0];
    this.sedaService.selectedSedaNode.next(this.sedaService.sedaRules[0]);
    this.sedaService.selectedSedaNodeParent.next(this.sedaData);

    this._fileServiceTabChildrenRulesChange = this.fileService.tabChildrenRulesChange.subscribe((rules) => {
      this.rulesChange = rules;
    });
    this._fileServiceCollectionName = this.fileService.collectionName.subscribe((collection) => {
      this.collectionName = collection;
    });
    this._fileServiceRootTabMetadataName = this.fileService.rootTabMetadataName.subscribe((metadataName) => {
      this.rootMetadataName = metadataName;
    });
    this._fileTreeServiceUpdateMedataTable = this.fileTreeService.updateMedataTable.subscribe((node) => {
      this.updateMedataTable(node);
    });
  }
  translatedOnChange(): void {
    this.translateService.onLangChange.subscribe(() => {
      constantToTranslate.call(this);
    });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(FILE_TREE_TRANSLATE_PATH + nameOfFieldToTranslate);
  }

  isAttribute(node: FileNode): boolean {
    return node ? node.type === TypeConstants[TypeConstants.attribute] : false;
  }

  getChildren = (node: FileNode) => node.children;

  hasNestedChild(nodeData: FileNode): boolean {
    return !nodeData.type;
  }

  /** Select the category so we can insert the new item. */
  async addNewItem(node: FileNode) {
    const dataToSendToPopUp = {} as PastisDialogData;
    dataToSendToPopUp.titleDialog = this.popupAddTitleDialog;
    (dataToSendToPopUp.subTitleDialog = `${this.popupAddSubTitleDialog} ${node.name}`), node.name;
    dataToSendToPopUp.fileNode = node;
    dataToSendToPopUp.width = '800px';
    dataToSendToPopUp.okLabel = this.popupAddOkLabel;
    dataToSendToPopUp.cancelLabel = this.popupAddCancelLabel;
    dataToSendToPopUp.component = UserActionAddMetadataComponent;
    dataToSendToPopUp.disableBtnOuiOnInit = true;
    const elementsToAdd = (await this.fileService.openPopup(dataToSendToPopUp)) as SedaData[];
    const names: string[] = elementsToAdd.map((e) => e.Name);
    if (elementsToAdd) {
      // this.sedaService.selectedSedaNode.next(sedaNode);
      this.insertItem(node, names);
      elementsToAdd.length > 1
        ? this.loggingService.showSuccess(this.notificationAddMetadonneePOne + ' ' + names.join(', ') + this.notificationAddMetadonneePTwo)
        : this.loggingService.showSuccess(this.notificationAddmetadonneeSOne + ' ' + names + ' ' + this.notificationAddmetadonneeSTwo);
    }
  }

  /** Add an item (or a list of items) in the Tree */
  insertItem(parent: FileNode, elementsToAdd: string[], node?: FileNode, insertItemDuplicate?: boolean) {
    const elementsToAddFromSeda: SedaData[] = [];
    for (const element of elementsToAdd) {
      parent.sedaData.Children.forEach((child) => {
        if (child.Name === element) {
          elementsToAddFromSeda.push(child);
        }
      });
    }

    if (parent.children && elementsToAddFromSeda) {
      this.insertItemIterate(elementsToAddFromSeda, insertItemDuplicate, node, parent);
      // 4. Order elements according to seda definition
      const sedaChildrenName: string[] = [];
      parent.sedaData.Children.forEach((child: { Name: string }) => {
        sedaChildrenName.push(child.Name);
      });
      parent.children.sort((a, b) => {
        return sedaChildrenName.indexOf(a.name) - sedaChildrenName.indexOf(b.name);
      });
      // 5. Update tree
      this.sendNodeMetadata(parent);

      // 6. No more nodes to add
    } else {
    }
  }

  private insertItemIterate(elementsToAddFromSeda: SedaData[], insertItemDuplicate: boolean, node: FileNode, parent: FileNode) {
    for (const element of elementsToAddFromSeda) {
      // 1. Define a new file node, its id and seda data;
      const newNode = {} as FileNode;
      const newId = window.crypto.getRandomValues(new Uint32Array(10))[0];
      const sedaChild = element;

      // 1.2. New node type is defined acording to the seda element type
      sedaChild.Element === SedaElementConstants.attribute
        ? (newNode.type = TypeConstants.attribute)
        : (newNode.type = TypeConstants.element);
      // 1.3. Fill the missing new node data
      if (insertItemDuplicate) {
        newNode.cardinality = node.cardinality;
        newNode.value = node.value;
        newNode.documentation = node.documentation;
        newNode.type = node.type;
      } else {
        newNode.cardinality = Object.values(CardinalityConstants).find((c) => c.valueOf() === sedaChild.Cardinality);
      }
      newNode.name = element.Name;
      newNode.id = newId;
      newNode.level = parent.level + 1;
      newNode.dataType = DataTypeConstants[sedaChild.Type as keyof typeof DataTypeConstants];
      newNode.parentId = parent.id;
      newNode.parent = parent;
      newNode.children = [];
      newNode.sedaData = sedaChild;
      if (this.isElementComplex(newNode) || newNode.name === 'Management') {
        newNode.puaData = new PUA();
        newNode.puaData.additionalProperties = false;
      }

      // 1.4. Update parent->children relashionship
      parent.children.push(newNode);
      this.parentNodeMap.set(newNode, parent);

      // 2. Insert all children of complex elements based on SEDA definition
      if (sedaChild.Element === SedaElementConstants.complex) {
        const childrenOfComplexElement: string[] = [];
        sedaChild.Children.forEach((child: { Cardinality: any; Name: string }) => {
          if (child.Cardinality === SedaCardinalityConstants.one || child.Cardinality === SedaCardinalityConstants.oreOrMore) {
            childrenOfComplexElement.push(child.Name);
          }
        });
        if (insertItemDuplicate) {
          node.children.forEach((child: FileNode) => {
            this.insertItem(newNode, [child.name], child, insertItemDuplicate);
          });
        } else {
          this.insertItem(newNode, childrenOfComplexElement);
        }
      }

      // 3. Insert all olbigatory attributes of the added node, if there is
      if (sedaChild.Children.some((child: { Element: any }) => child.Element === SedaElementConstants.attribute)) {
        const attributes: FileNode[] = [];
        sedaChild.Children.filter((c: { Element: any }) => c.Element === SedaElementConstants.attribute).forEach(
          (child: { Name: string; Element: any; Cardinality: any }) => {
            const isAttributeAlreadyIncluded = newNode.children.some((nodeChild) => nodeChild.name.includes(child.Name));
            // If the added node contains an obligatory attribute,
            // on its seda definition and the attribute is not already part of the node,
            // we then, build an attribute node based on the seda atribute defintion
            if (
              child.Element === SedaElementConstants.attribute &&
              child.Cardinality === SedaCardinalityConstants.one &&
              !isAttributeAlreadyIncluded
            ) {
              const childAttribute = {} as FileNode;
              childAttribute.name = child.Name;
              childAttribute.cardinality = child.Cardinality === SedaCardinalityConstants.one ? '1' : null;
              childAttribute.sedaData = sedaChild;
              attributes.push(childAttribute);
            }
          }
        );
        this.insertAttributes(newNode, attributes);
      }
    }
  }

  newAttributNode(attribute: FileNode, parent: FileNode) {
    const newAttributeNode = {} as FileNode;
    const newId = window.crypto.getRandomValues(new Uint32Array(10))[0];
    newAttributeNode.name = attribute.name;
    newAttributeNode.id = newId;
    newAttributeNode.level = parent.level + 1;
    newAttributeNode.type = TypeConstants.attribute;
    newAttributeNode.dataType = DataTypeConstants[attribute.sedaData.Type as keyof typeof DataTypeConstants];
    newAttributeNode.parentId = parent.id;
    newAttributeNode.children = [];
    newAttributeNode.cardinality = !attribute.cardinality ? '1' : attribute.cardinality;
    newAttributeNode.documentation = attribute.documentation ? attribute.documentation : null;
    newAttributeNode.value = attribute.value ? attribute.value : null;
    newAttributeNode.sedaData = attribute.sedaData;
    newAttributeNode.parent = parent;
    parent.children.push(newAttributeNode);
    this.parentNodeMap.set(newAttributeNode, parent);
  }

  // @ts-ignore
  insertAttributes(parent: FileNode, attributesToAdd: FileNode[], node?: FileNode, insertItemDuplicate?: boolean) {
    if (attributesToAdd) {
      for (const attribute of attributesToAdd) {
        // Only attributes with cardinality one will be included
        if (attribute.cardinality === SedaCardinalityConstants.one) {
          this.newAttributNode(attribute, parent);
        }
      }
    }
    /* //TODO : à revoir pour duplication des attributs
   if(insertItemDuplicate){
      this.newAttributNode(node, parent);
    }*/
  }

  sendNodeMetadata(node: FileNode): void {
    this.updateFileTree(node);
    this.updateMedataTable(node);
    if (node.name === 'DataObjectGroup') {
      const dataObjectPackageId = this.fileService.getFileNodeByName(node.parent, 'DataObjectPackage').id;
      this.renderChanges(node, dataObjectPackageId);
    }
    if (node.name === 'DescriptiveMetadata') {
      FileTreeComponent.archiveUnits = node;
      this.generateArchiveUnitsNumbers(node);
      this.renderChanges(node, node.id);
    } else {
      this.renderChanges(node);
    }
  }

  generateArchiveUnitsNumbers(archiveUnit: FileNode): void {
    if (archiveUnit.name === 'DescriptiveMetadata') {
      const archiveUnitLevel = archiveUnit.level - 1;
      FileTreeComponent.uaIdAndPosition.set(archiveUnitLevel, archiveUnit.id);
    }
    let counter = 0;
    archiveUnit.children.forEach((child) => {
      if (child.name === 'ArchiveUnit' || child.nonEditFileNode) {
        counter++;
        const archiveUnitLevel = archiveUnit.level - 1 + '.' + counter;
        FileTreeComponent.uaIdAndPosition.set(archiveUnitLevel, child.id);
      }
    });
  }

  // Refresh Tree by opening an given node (option)
  // If the a node name is not prodived, the function will open the root tab element
  renderChanges(node: FileNode, nodeIdToExpand?: number) {
    let data: FileNode;
    if (nodeIdToExpand) {
      data = this.fileService.getFileNodeById(this.fileService.allData.getValue()[0], nodeIdToExpand);
    } else {
      const rootTabName = this.fileService.rootTabMetadataName.getValue();
      data = this.fileService.getFileNodeByName(this.fileService.allData.getValue()[0], rootTabName);
    }
    if (data) {
      const dataArray = [];
      dataArray.push(data);
      this.fileTreeService.nestedDataSource.data = null;
      this.fileTreeService.nestedDataSource.data = dataArray;
      this.fileTreeService.nestedTreeControl.expand(node);
    }
  }

  updateMedataTable(node: FileNode) {
    if (node) {
      this.selectedItemList = node;
    }
    const rulesFromService = this.fileService.tabChildrenRulesChange.getValue();
    const tabChildrenToInclude = rulesFromService[0];
    const tabChildrenToExclude = rulesFromService[1];
    this.fileService.nodeChange.next(node);
    this.sedaService.selectedSedaNode.next(node.sedaData);
    const dataTable = this.fileMetadataService.fillDataTable(node.sedaData, node, tabChildrenToInclude, tabChildrenToExclude);
    const hasAtLeastOneComplexChild = node.children.some((el) => el.type === TypeConstants.element);

    if (node.sedaData.Element === SedaElementConstants.complex) {
      this.fileMetadataService.shouldLoadMetadataTable.next(hasAtLeastOneComplexChild);
      this.fileMetadataService.dataSource.next(dataTable);
    } else {
      this.fileMetadataService.shouldLoadMetadataTable.next(true);
      this.fileMetadataService.dataSource.next(dataTable);
    }
  }

  // Updates the nested tab root tree and the data tree
  updateFileTree(node: FileNode) {
    this.fileTreeService.nestedDataSource.data[0] = node;
    const allData = this.fileService.allData.getValue()[0];
    this.updateItem(node, allData);
  }

  sendNodeMetadataIfChildren(node: FileNode) {
    if (node.children.length) {
      this.sendNodeMetadata(node);
    }
  }

  isElementComplexAndHasChildren(node: FileNode) {
    return node.children.some((child) => child.type === TypeConstants.element);
  }

  isElementComplex(node: FileNode) {
    return node.sedaData.Element === SedaElementConstants.complex;
  }

  onResolveName(node: FileNode) {
    if (this.sedaLanguage) {
      return node.name;
    } else {
      if (node.sedaData && node.sedaData.NameFr) {
        return node.sedaData.NameFr;
      }
    }
    return node.name;
  }

  async remove(node: FileNode) {
    const dataToSendToPopUp = {} as PastisDialogData;
    const nodeType =
      node.sedaData.Element == SedaElementConstants.attribute ? this.popupRemoveSedaElementAttribut : this.popupRemoveSedaElementMetadonnee;
    dataToSendToPopUp.titleDialog = this.popupRemoveTitre + ' ' + nodeType + ' "' + this.onResolveName(node) + '" ?';
    dataToSendToPopUp.subTitleDialog =
      node.sedaData.Element === SedaElementConstants.attribute ? this.popupRemoveSousTitreAttribut : this.popupRemoveSousTitreMetadonnee;
    dataToSendToPopUp.fileNode = node;
    dataToSendToPopUp.component = UserActionRemoveMetadataComponent;

    const popUpAnswer = (await this.fileService.openPopup(dataToSendToPopUp)) as FileNode;
    if (popUpAnswer) {
      const deleteTypeText =
        node.sedaData.Element === SedaElementConstants.attribute ? this.popupRemoveDeleteTypeTextM : this.popupRemoveDeleteTypeTextF;
      this.removeItem(node, this.fileService.nodeChange.getValue());
      this.loggingService.showSuccess(
        nodeType + node.name + this.notificationRemoveSuccessOne + deleteTypeText + this.notificationRemoveSuccessTwo
      );
    }
  }

  /**
   * Duplicate an item tree
   * @param node
   */
  async duplicate(node: FileNode) {
    const dataToSendToPopUp = {} as PastisDialogData;
    const nodeType =
      node.sedaData.Element == SedaElementConstants.attribute
        ? this.popupDuplicateSedaElementAttribut
        : this.popupDuplicateSedaElementMetadonnee;
    dataToSendToPopUp.titleDialog = this.popupDuplicateTitre + ' ' + nodeType + ' "' + node.name + ' ' + this.popupDuplicateTitreTwo;
    dataToSendToPopUp.subTitleDialog =
      node.sedaData.Element == SedaElementConstants.attribute
        ? this.popupDuplicateSousTitreAttribut
        : this.popupDuplicateSousTitreMetadonnee;
    dataToSendToPopUp.fileNode = node;
    dataToSendToPopUp.component = DuplicateMetadataComponent;

    const elementToDuplicate = (await this.fileService.openPopup(dataToSendToPopUp)) as string;
    if (elementToDuplicate) {
      const duplicateTypeText =
        node.sedaData.Element == SedaElementConstants.attribute ? this.popupDuplicateDeleteTypeTextM : this.popupDuplicateDeleteTypeTextF;
      const addedItems: string[] = [];
      addedItems.push(elementToDuplicate);
      this.insertItem(node.parent, addedItems, node, true);
      this.loggingService.showSuccess(
        nodeType + node.name + this.notificationDuplicateSuccessOne + duplicateTypeText + this.notificationDuplicateSuccessTwo
      );
    }
  }

  isSedaNodeObligatory(nodeName: string): boolean {
    if (this.sedaData) {
      for (const child of this.sedaData.Children) {
        if (child.Name === nodeName) {
          return child.Cardinality !== '1';
        }
      }
    }
  }

  buildFileTree(obj: object, level: number): FileNode[] {
    // This should recive Root node of Tree of Type FileNode
    // so we dont have to create a new node and use it as it is
    return Object.keys(obj).reduce<FileNode[]>((accumulator: FileNode[], key: keyof object) => {
      const value = obj[key];
      const node = {} as FileNode;
      node.id = level;
      node.level = level;
      node.name = key;
      node.parentId = null;
      if (value != null) {
        if (typeof value === 'object') {
          node.children = this.buildFileTree(value, level + 1);
        } else {
          node.type = value;
        }
      }
      return accumulator.concat(node);
    }, []);
  }

  /** Remove an item Tree node given a parent node and the child to be removed */
  removeItem(childToBeRemoved: FileNode, parentRootNode: FileNode) {
    // If the parentRoot is a reference to the child to be removed, we search for its parent from the root tab node
    const rootNode = parentRootNode.id === childToBeRemoved.id ? this.fileTreeService.nestedDataSource.data[0] : parentRootNode;

    const parentNode = this.findParent(childToBeRemoved.parentId, rootNode);
    if (parentNode) {
      const index = parentNode.children.indexOf(childToBeRemoved);
      if (index !== -1) {
        parentNode.children.splice(index, 1);
        // Refacto TODO
        this.parentNodeMap.delete(childToBeRemoved);
        this.dataChange.next(this.data);
      }

      this.sendNodeMetadata(parentNode);
    }
  }

  /** Update an item Tree node */
  updateItem(newRootNode: FileNode, allData: FileNode) {
    for (const idx in allData.children) {
      if (allData.children[idx].id === newRootNode.id) {
        allData.children[idx] = newRootNode;
      } else {
        this.updateItem(newRootNode, allData.children[idx]);
      }
    }
  }

  /** Find a parent tree node */
  findParent(id: number, parentNode: FileNode): FileNode {
    return this.fileService.getFileNodeById(parentNode, id);
  }

  findParentLevel(nodeToFind: FileNode): number {
    const parentNodeToSearch = this.fileTreeService.nestedDataSource.data;
    for (const node of parentNodeToSearch) {
      // For nested elements
      if (
        this.rootElementName === node.name &&
        this.rootElementName === nodeToFind.name &&
        parentNodeToSearch[0].name === node.name &&
        parentNodeToSearch[0].id !== nodeToFind.id
      ) {
        return 1;
      }
      return nodeToFind.level - node.level;
    }
  }

  // Checks if a node belongs to the clicked tab collection.
  // For a given node, searches the required node in the seda.json file and
  // returns true if the node's value of "Collection" is equal to the clicked tab
  isPartOfCollection(node: FileNode): boolean {
    if (!node.sedaData) {
      return false;
    }
    return this.collectionName === node.sedaData.Collection.valueOf();
  }

  shouldBeOnTab(node: FileNode): boolean {
    const rootNodeName = this.fileService.rootTabMetadataName.getValue();
    const filteredNode = Object.assign({} as FileNode, this.fileTreeService.nestedDataSource.data[0]);

    const includedDataObjectPackageChildren = ['DataObjectGroup', 'BinaryDataObject', 'PhysicalDataObject'];
    if (rootNodeName === 'DataObjectPackage' && !includedDataObjectPackageChildren.includes(node.name)) {
      filteredNode.children = filteredNode.children.filter(
        (child: { name: string }) => child.name !== 'DescriptiveMetadata' && child.name !== 'ManagementMetadata'
      );
      const childFound = this.fileService.getFileNodeById(filteredNode, node.id);
      return !!childFound;
    }
    if (rootNodeName === 'ArchiveTransfer') {
      filteredNode.children = filteredNode.children.filter((child: { name: string }) => child.name !== 'DataObjectPackage');
      const childFound = this.fileService.getFileNodeById(filteredNode, node.id);
      return !!childFound;
    }
    return true;
  }

  // Returns the positioning, in pixels, of a given node
  calculateNodePosition(node: FileNode): string {
    // Root node name
    if (node.name === this.rootElementName) {
      return new Number(28).toString();
    }
    // Root children with children
    if (node.children.length && node.name !== this.rootElementName) {
      return new Number(this.findParentLevel(node) * 40 - 16).toString();
    }
    // Root children without children-
    if (!node.children.length && node.name !== this.rootElementName) {
      return new Number(this.findParentLevel(node) * 40 - 13).toString();
    }
  }

  /** Error handler */
  handleError(error: any) {
    let errorMessage = '';
    if (error.error instanceof ErrorEvent) {
      // Get client-side error
      errorMessage = error.error.message;
    } else {
      // Get server-side error
      errorMessage = `Error Code: ${error.status} Message: ${error.message}`;
    }
    window.alert(errorMessage);
    return throwError(errorMessage);
  }

  showAllowedChidren(node: FileNode) {
    if (this.childrenListToExclude) {
      return !this.childrenListToExclude.includes(node.name);
    }
  }

  addArchiveUnit(node: FileNode) {
    if (node.name == 'DescriptiveMetadata' || node.name == 'ArchiveUnit') {
      this.insertItem(node, ['ArchiveUnit']);
      // Refresh the metadata tree and the metadatatable
      this.renderChanges(node);
      this.loggingService.showSuccess(this.notificationAjoutMetadonneeFileTree);
    }
  }

  selectedItem(node: FileNode): boolean {
    if (this.selectedItemList && node) {
      if (node.name === 'ManagementMetadata') {
        console.log(this);
      }
      if (this.selectedItemList.id == node.id) {
        return true;
      }
    }
    return false;
  }
  expendChildren(node: FileNode) {
    if (this.fileTreeService.nestedTreeControl.isExpanded(node)) {
      this.viewChild = this.viewChild.filter((e) => e.id != node.id);
      this.filterExpandedChildren(node, true);
      document.getElementById('child' + node.id).click();
      this.updateMedataTable(node);
    } else {
      document.getElementById('child' + node.id).click();
      this.filterExpandedChildren(node, false);
      this.viewChild.push(node);
      this.updateMedataTable(node);
    }
  }
  filterExpandedChildren(node: FileNode, isExpanded: boolean) {
    if (this.viewChild && this.viewChild.length > 0) {
      this.viewChild.forEach((e: FileNode) => {
        const abstractFunctionCondition: Function = (isExpanded: boolean): boolean => {
          return isExpanded
            ? e.id != node.id && e.level >= node.level
            : e.id != node.id && (e.level === node.level || (e.level > node.level && e.parentId != node.parentId));
        };
        if (abstractFunctionCondition(isExpanded)) {
          if (this.fileTreeService.nestedTreeControl.isExpanded(e)) {
            document.getElementById('child' + e.id).click();
          }
          this.viewChild = isExpanded ? this.viewChild.filter((e) => e.id === node.id) : this.viewChild.filter((e) => e.id !== node.id);
        }
      });
      if (isExpanded) {
        this.updateMedataTable(node);
      }
    }
  }

  ngOnDestroy() {
    if (this.sedaLanguageSub != null) {
      this.sedaLanguageSub.unsubscribe();
    }
    if (this._fileServiceTabChildrenRulesChange != null) {
      this._fileServiceTabChildrenRulesChange.unsubscribe();
    }
    if (this._fileServiceCollectionName != null) {
      this._fileServiceCollectionName.unsubscribe();
    }
    if (this._fileServiceRootTabMetadataName != null) {
      this._fileServiceRootTabMetadataName.unsubscribe();
    }
    if (this._fileTreeServiceUpdateMedataTable != null) {
      this._fileTreeServiceUpdateMedataTable.unsubscribe();
    }
  }

  changeFileNode($event: string, node: FileNode) {
    node.nonEditFileNode = true;
    node.editName = $event;
    this.fileService.nodeChange.next(node);

    this.updateMedataTable(node);
  }
}
