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
import { ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { BehaviorSubject, Subscription } from 'rxjs';
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
import { Logger } from 'vitamui-library';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { NestedTreeControl } from '@angular/cdk/tree';
import { filter, map, tap } from 'rxjs/operators';

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
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-file-tree',
  templateUrl: './file-tree.component.html',
  styleUrls: ['./file-tree.component.scss'],
})
export class FileTreeComponent implements OnInit, OnDestroy {
  static archiveUnits: FileNode;
  static uaIdAndPosition = new Map<any, number>();
  private static ROOT_LEVEL = 1;
  private static ROOT_LEFT_PADDING = 28;

  @Input() rootElementName: string;
  @Input() rootElementShowName: string;
  @Input() childrenListToExclude: string[];
  @Input() childrenListToInclude: string[];
  @Input() collectionName: string;
  @Input() rootTabMetadataName: string;
  @Input() activeTabIndex: number;

  isStandalone: boolean = environment.standalone;

  dataSource = new MatTreeNestedDataSource<FileNode>();
  treeControl = new NestedTreeControl<FileNode>((node) => node.children);
  updating = false;

  data: FileNode;
  parentNodeMap = new Map<FileNode, FileNode>();
  dataChange = new BehaviorSubject<FileNode>(null);
  selectedNode: FileNode;
  sedaLanguage: boolean;
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

  private subscriptions = new Subscription();

  constructor(
    public fileTreeService: FileTreeService,
    public profileService: ProfileService,
    private fileService: FileService,
    private loggingService: NotificationService,
    private fileMetadataService: FileTreeMetadataService,
    private sedaService: SedaService,
    private sedaLanguageService: PastisPopupMetadataLanguageService,
    private translateService: TranslateService,
    private logger: Logger,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
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
    this.subscriptions.add(
      this.sedaLanguageService.sedaLanguage.subscribe({
        next: (value: boolean) => {
          this.sedaLanguage = value;
        },
        error: (error) => {
          this.logger.error(this, error);
        },
      }),
    );
    this.subscriptions.add(
      this.sedaService.sedaRules$.subscribe((value) => {
        this.sedaService.selectedSedaNode.next(value);
        this.sedaService.selectedSedaNodeParent.next(value);
      }),
    );
    this.subscriptions.add(
      this.fileTreeService.updateMetadataTable.subscribe((node) => {
        this.updateMetadataTable(node);
      }),
    );
    this.subscriptions.add(
      this.fileTreeService.data$
        .pipe(
          filter((data: FileNode[]) => Boolean(data.length)),
          tap(() => (this.updating = true)),
          tap((data: FileNode[]) => {
            this.dataSource.data = data;
            this.cdr.detectChanges();
          }),
          tap(() => (this.updating = false)),
        )
        .subscribe(),
    );
    this.subscriptions.add(
      this.fileTreeService.selectedNode$.subscribe((selectedNode) => {
        this.selectedNode = selectedNode;
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  translatedOnChange(): void {
    this.translateService.onLangChange.subscribe(() => {
      constantToTranslate.call(this);
    });
  }

  // @ts-ignore
  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(FILE_TREE_TRANSLATE_PATH + nameOfFieldToTranslate);
  }

  isAttribute(node: FileNode): boolean {
    return node ? node.type === TypeConstants.ATTRIBUTE : false;
  }

  hasChild = (_: number, node: FileNode) => (!!node.children && node.children.length > 0) || !node.dataType;

  /** Add an item (or a list of items) in the Tree */
  insertItem(parent: FileNode, elementsToAdd: string[], node?: FileNode, insertItemDuplicate?: boolean) {
    this.logger.log(this, 'After data is :', this.fileTreeService.getNestedDataSource().data);
    const sedaNodesToAdd = elementsToAdd
      .map((nodeName) => this.sedaService.findSedaNode(nodeName, parent.sedaData))
      .filter((node) => Boolean(node));

    if (parent.children && sedaNodesToAdd) {
      this.insertItemIterate(sedaNodesToAdd, insertItemDuplicate, node, parent);
      // 4. Order elements according to seda definition
      const sedaChildrenName: string[] = [];
      parent.sedaData.children.forEach((child: { name: string }) => {
        sedaChildrenName.push(child.name);
      });
      parent.children.sort((a, b) => {
        return sedaChildrenName.indexOf(a.name) - sedaChildrenName.indexOf(b.name);
      });
      // 5. Update tree
      this.sendNodeMetadata(parent);
      this.logger.log(this, 'New fileNode data is :', this.fileTreeService.getNestedDataSource().data);

      // 6. No more nodes to add
    } else {
      this.logger.log(this, 'No More Nodes can be inserted : No node was selected or node name is invalid');
    }
  }

  private insertItemIterate(elementsToAddFromSeda: SedaData[], insertItemDuplicate: boolean, node: FileNode, parent: FileNode) {
    for (const element of elementsToAddFromSeda) {
      // 1. Define a new file node, its id and seda data;
      const newNode = {} as FileNode;
      const newId = window.crypto.getRandomValues(new Uint32Array(10))[0];
      const sedaChild = element;

      // 1.2. New node type is defined according to the seda element type
      sedaChild.element === SedaElementConstants.ATTRIBUTE
        ? (newNode.type = TypeConstants.ATTRIBUTE)
        : (newNode.type = TypeConstants.ELEMENT);
      // 1.3. Fill the missing new node data
      if (insertItemDuplicate) {
        newNode.cardinality = node.cardinality;
        newNode.value = node.value;
        newNode.documentation = node.documentation;
        newNode.type = node.type;
      } else {
        newNode.cardinality = Object.values(CardinalityConstants).find((c) => c.valueOf() === sedaChild.cardinality);
      }
      newNode.name = element.name;
      newNode.id = newId;
      newNode.level = parent.level + 1;
      newNode.dataType = DataTypeConstants[sedaChild.type as keyof typeof DataTypeConstants];
      newNode.parentId = parent.id;
      newNode.parent = parent;
      newNode.children = [];
      newNode.sedaData = sedaChild;
      if (this.isElementComplex(newNode) || newNode.name === 'Management') {
        newNode.puaData = new PUA();
        newNode.puaData.additionalProperties = false;
      }
      this.logger.log(this, 'Parent node name: ' + parent.name);
      this.logger.log(this, 'New node  : ', newNode);

      // 1.4. Update parent->children relationship
      parent.children.push(newNode);
      this.parentNodeMap.set(newNode, parent);
      this.logger.log(this, 'Seda children and file children: ', parent.sedaData.children, parent.children);

      // 2. Insert all children of complex elements based on SEDA definition
      if (sedaChild.element === SedaElementConstants.COMPLEX) {
        const childrenOfComplexElement: string[] = [];
        sedaChild.children.forEach((child: { cardinality: any; name: string }) => {
          if (child.cardinality === SedaCardinalityConstants.ONE_REQUIRED || child.cardinality === SedaCardinalityConstants.MANY_REQUIRED) {
            childrenOfComplexElement.push(child.name);
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

      // 3. Insert all mandatory attributes of the added node, if there is
      if (sedaChild.children.some((child: { element: any }) => child.element === SedaElementConstants.ATTRIBUTE)) {
        const attributes: FileNode[] = [];
        sedaChild.children
          .filter((c: { element: any }) => c.element === SedaElementConstants.ATTRIBUTE)
          .forEach((child: { name: string; element: any; cardinality: any }) => {
            const isAttributeAlreadyIncluded = newNode.children.some((nodeChild) => nodeChild.name.includes(child.name));
            // If the added node contains an obligatory attribute,
            // on its seda definition and the attribute is not already part of the node,
            // we then, build an attribute node based on the seda atribute defintion
            if (
              child.element === SedaElementConstants.ATTRIBUTE &&
              child.cardinality === SedaCardinalityConstants.ONE_REQUIRED &&
              !isAttributeAlreadyIncluded
            ) {
              const childAttribute = {} as FileNode;
              childAttribute.name = child.name;
              childAttribute.cardinality = child.cardinality === SedaCardinalityConstants.ONE_REQUIRED ? '1' : null;
              childAttribute.sedaData = sedaChild;
              attributes.push(childAttribute);
            }
          });
        this.insertAttributes(newNode, attributes);
      }
    }
  }

  newAttributeNode(attribute: FileNode, parent: FileNode): FileNode {
    const { name, sedaData, cardinality, documentation, value } = attribute;

    return {
      id: window.crypto.getRandomValues(new Uint32Array(10))[0],
      name,
      level: parent.level + 1,
      type: TypeConstants.ATTRIBUTE,
      dataType: DataTypeConstants[sedaData.type as keyof typeof DataTypeConstants],
      cardinality: cardinality || '1',
      documentation: documentation || null,
      value: value || null,
      sedaData,
      parentId: parent.id,
      parent: parent,
      additionalProperties: false,
      choices: null,
      groupOrChoice: null,
      valueOrData: undefined,
      children: [],
    };
  }

  insertAttributes(parent: FileNode, attributesToAdd: FileNode[], node?: FileNode, insertItemDuplicate = false) {
    attributesToAdd
      .filter((attribute) => attribute.cardinality === SedaCardinalityConstants.ONE_REQUIRED)
      .forEach((attribute) => {
        const next = this.newAttributeNode(attribute, parent);
        parent.children.push(next);
        this.parentNodeMap.set(next, parent);
      });

    if (insertItemDuplicate && node) {
      const next = this.newAttributeNode(node, parent);
      parent.children.push(next);
      this.parentNodeMap.set(next, parent);
    }
  }

  sendNodeMetadata(node: FileNode): void {
    this.updateFileTree(node);
    this.updateMetadataTable(node);
    if (node.name === 'DataObjectGroup') {
      const dataObjectPackageId = this.fileService.getFileNodeByName(node.parent, 'DataObjectPackage').id;
      this.renderChanges(node, dataObjectPackageId);
    }
    if (node.name === 'DescriptiveMetadata') {
      FileTreeComponent.archiveUnits = node;
      this.generateArchiveUnitsNumbers(node);
      this.renderChanges(node, node.id);
      this.logger.log(this, 'Archive units : ', FileTreeComponent.archiveUnits);
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
    const [root] = this.fileService.currentTree.getValue();
    const data: FileNode = nodeIdToExpand
      ? this.fileService.getFileNodeById(root, nodeIdToExpand)
      : this.fileService.getFileNodeByName(root, this.rootTabMetadataName);
    if (data) {
      this.fileTreeService.setNestedDataSourceData([data]);
      this.fileTreeService.nestedTreeControl.expand(node);
    }
  }

  updateMetadataTable(node: FileNode) {
    if (node) this.fileTreeService.selectNode(node);

    this.fileService.nodeChange.next(node);
    this.sedaService.selectedSedaNode.next(node.sedaData);

    const shouldLoad =
      node.sedaData.element === SedaElementConstants.COMPLEX ? node.children.some((el) => el.type === TypeConstants.ELEMENT) : true;
    this.fileMetadataService.shouldLoadMetadataTable.next(shouldLoad);

    const dataTable = this.fileMetadataService.fillDataTable(node.sedaData, node, this.childrenListToInclude, this.childrenListToExclude);
    this.logger.log(this, 'Filled data on table : ', dataTable, '...should load : ', shouldLoad);
    this.fileMetadataService.dataSource.next(dataTable);
  }

  // Updates the nested tab root tree and the data tree
  updateFileTree(node: FileNode) {
    const [root] = this.fileService.currentTree.getValue();
    this.updateItem(node, root);
  }

  isElementComplexAndHasChildren(node: FileNode) {
    return node.children.some((child) => child.type === TypeConstants.ELEMENT);
  }

  isElementComplex(node: FileNode) {
    return node.sedaData.element === SedaElementConstants.COMPLEX;
  }

  onResolveName(node: FileNode) {
    if (!this.sedaLanguage && node.sedaData?.nameFr) {
      return node.sedaData.nameFr;
    }
    return node.name;
  }

  add(node: FileNode): void {
    const addItemDialogData: PastisDialogData = {
      titleDialog: this.popupAddTitleDialog,
      subTitleDialog: `${this.popupAddSubTitleDialog} ${node.name}`,
      fileNode: node,
      width: '800px',
      height: 'auto',
      okLabel: this.popupAddOkLabel,
      cancelLabel: this.popupAddCancelLabel,
      component: UserActionAddMetadataComponent,
      disableBtnOuiOnInit: true,
    };
    this.subscriptions.add(
      this.fileService
        .openDialog(addItemDialogData)
        .pipe(
          map((elementsToAdd: SedaData[]) => elementsToAdd.map((e) => e.name)),
          tap((elementToAddNames: string[]) => this.insertItem(node, elementToAddNames)),
          tap((elementToAddNames: string[]) => {
            let notification = `${this.notificationAddmetadonneeSOne} ${elementToAddNames} ${this.notificationAddmetadonneeSTwo}`;
            if (elementToAddNames.length > 1) {
              notification = `${this.notificationAddMetadonneePOne} ${elementToAddNames.join(', ')} ${this.notificationAddMetadonneePTwo}`;
            }
            this.loggingService.showSuccess(notification);
          }),
        )
        .subscribe(),
    );
  }

  remove(node: FileNode) {
    const isAttribute = node.sedaData.element === SedaElementConstants.ATTRIBUTE;
    const nodeType = isAttribute ? this.popupRemoveSedaElementAttribut : this.popupRemoveSedaElementMetadonnee;
    const titleDialog = `${this.popupRemoveTitre} ${nodeType} "${this.onResolveName(node)}" ?`;
    const subTitleDialog = isAttribute ? this.popupRemoveSousTitreAttribut : this.popupRemoveSousTitreMetadonnee;
    const removeItemDialogData: PastisDialogData = {
      component: UserActionRemoveMetadataComponent,
      titleDialog,
      subTitleDialog,
      width: '800px',
      height: 'auto',
      disableBtnOuiOnInit: false,
      okLabel: '',
      cancelLabel: '',
      fileNode: node,
    };

    this.subscriptions.add(
      this.fileService
        .openDialog(removeItemDialogData)
        .pipe(
          tap(() => this.removeItem(node, this.fileService.nodeChange.getValue())),
          tap(() => {
            const deleteTypeText =
              node.sedaData.element === SedaElementConstants.ATTRIBUTE ? this.popupRemoveDeleteTypeTextM : this.popupRemoveDeleteTypeTextF;
            const notification = `${nodeType}${node.name}${this.notificationRemoveSuccessOne}${deleteTypeText}${this.notificationRemoveSuccessTwo}`;
            this.loggingService.showSuccess(notification);
          }),
        )
        .subscribe(),
    );
  }

  duplicate(node: FileNode) {
    const isAttribute = node.sedaData.element === SedaElementConstants.ATTRIBUTE;
    const nodeType = isAttribute ? this.popupDuplicateSedaElementAttribut : this.popupDuplicateSedaElementMetadonnee;
    const titleDialog = `${this.popupDuplicateTitre} ${nodeType} "${node.name} ${this.popupDuplicateTitreTwo}"`;
    const subTitleDialog = isAttribute ? this.popupDuplicateSousTitreAttribut : this.popupDuplicateSousTitreMetadonnee;
    const duplicateItemDialogData: PastisDialogData = {
      component: DuplicateMetadataComponent,
      titleDialog,
      subTitleDialog,
      width: '800px',
      height: 'auto',
      disableBtnOuiOnInit: false,
      okLabel: '',
      cancelLabel: '',
      fileNode: node,
    };

    this.fileService
      .openDialog(duplicateItemDialogData)
      .pipe(
        tap((elementToDuplicate: string) => this.insertItem(node.parent, [elementToDuplicate], node, true)),
        tap(() => {
          const duplicateTypeText =
            node.sedaData.element === SedaElementConstants.ATTRIBUTE
              ? this.popupDuplicateDeleteTypeTextM
              : this.popupDuplicateDeleteTypeTextF;
          const notification = `${nodeType}${node.name}${this.notificationDuplicateSuccessOne}${duplicateTypeText}${this.notificationDuplicateSuccessTwo}`;

          this.loggingService.showSuccess(notification);
        }),
      )
      .subscribe();
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
    const rootNode = parentRootNode.id === childToBeRemoved.id ? this.fileTreeService.getNestedDataSource().data[0] : parentRootNode;

    const parentNode = this.findParent(childToBeRemoved.parentId, rootNode);
    if (parentNode) {
      this.logger.log(this, 'On removeItem with node : ', childToBeRemoved, 'and parent : ', parentNode);
      const index = parentNode.children.indexOf(childToBeRemoved);
      if (index !== -1) {
        parentNode.children.splice(index, 1);
        // Refacto TODO
        this.parentNodeMap.delete(childToBeRemoved);
        this.dataChange.next(this.data);
      }
      this.logger.log(this, 'Deleted node : ', childToBeRemoved, 'and his parent : ', parentNode);
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
    this.logger.log(this, 'On findParent with parent node id : ', id, ' and parent : ', parentNode);
    return this.fileService.getFileNodeById(parentNode, id);
  }

  findParentLevel(nodeToFind: FileNode): number {
    const parentNodeToSearch = this.fileTreeService.getNestedDataSource().data;
    const node = parentNodeToSearch[0];
    if (this.rootElementName === node.name && this.rootElementName === nodeToFind.name) {
      return FileTreeComponent.ROOT_LEVEL;
    }

    return nodeToFind.level - node.level;
  }

  // Checks if a node belongs to the clicked tab collection.
  // For a given node, searches the required node in the seda.json file and
  // returns true if the node's value of "Collection" is equal to the clicked tab
  isPartOfCollection(node: FileNode): boolean {
    if (!node.sedaData) {
      return false;
    }
    return this.collectionName === node.sedaData.collection.valueOf();
  }

  shouldBeOnTab(node: FileNode): boolean {
    const filteredNode = Object.assign({} as FileNode, this.fileTreeService.getNestedDataSource().data[0]);

    const includedDataObjectPackageChildren = ['DataObjectGroup', 'BinaryDataObject', 'PhysicalDataObject'];
    if (this.rootTabMetadataName === 'DataObjectPackage' && !includedDataObjectPackageChildren.includes(node.name)) {
      filteredNode.children = filteredNode.children.filter(
        (child: { name: string }) => child.name !== 'DescriptiveMetadata' && child.name !== 'ManagementMetadata',
      );
      const childFound = this.fileService.getFileNodeById(filteredNode, node.id);
      return !!childFound;
    }
    if (this.rootTabMetadataName === 'ArchiveTransfer') {
      filteredNode.children = filteredNode.children.filter((child: { name: string }) => child.name !== 'DataObjectPackage');
      const childFound = this.fileService.getFileNodeById(filteredNode, node.id);
      return !!childFound;
    }
    return true;
  }

  // Returns the positioning, in pixels, of a given node
  calculateNodePosition(node: FileNode): string {
    if (node.name === this.rootElementName) return FileTreeComponent.ROOT_LEFT_PADDING.toString();

    const basePosition = this.findParentLevel(node) * 40;
    return (node.children.length ? basePosition - 16 : basePosition - 13).toString();
  }

  addArchiveUnit(node: FileNode) {
    if (node.name === 'DescriptiveMetadata' || node.name === 'ArchiveUnit') {
      this.logger.log(this, 'Clicked seda node : ', node.sedaData);
      this.insertItem(node, ['ArchiveUnit']);
      this.loggingService.showSuccess(this.notificationAjoutMetadonneeFileTree);
    }
  }

  selectedItem(node: FileNode): boolean {
    if (!this.selectedNode) return false;

    return this.selectedNode.id === node.id;
  }

  expandChildren(node: FileNode) {
    if (this.fileTreeService.nestedTreeControl.isExpanded(node)) {
      this.viewChild = this.viewChild.filter((e) => e.id !== node.id);
      this.filterExpandedChildren(node, true);
      document.getElementById('child' + node.id).click();
      this.updateMetadataTable(node);
    } else {
      document.getElementById('child' + node.id).click();
      this.filterExpandedChildren(node, false);
      this.viewChild.push(node);
      this.updateMetadataTable(node);
    }
  }

  filterExpandedChildren(node: FileNode, isExpanded: boolean) {
    if (this.viewChild && this.viewChild.length > 0) {
      this.viewChild.forEach((e: FileNode) => {
        const abstractFunctionCondition = (isExpanded: boolean): boolean => {
          return isExpanded
            ? e.id !== node.id && e.level >= node.level
            : e.id !== node.id && (e.level === node.level || (e.level > node.level && e.parentId !== node.parentId));
        };
        if (abstractFunctionCondition(isExpanded)) {
          if (this.fileTreeService.nestedTreeControl.isExpanded(e)) {
            document.getElementById('child' + e.id).click();
          }
          this.viewChild = isExpanded ? this.viewChild.filter((e) => e.id === node.id) : this.viewChild.filter((e) => e.id !== node.id);
        }
      });
      if (isExpanded) {
        this.updateMetadataTable(node);
      }
    }
  }

  changeFileNode($event: string, node: FileNode) {
    node.nonEditFileNode = true;
    node.editName = $event;
    this.fileService.nodeChange.next(node);

    this.updateMetadataTable(node);
  }
}
