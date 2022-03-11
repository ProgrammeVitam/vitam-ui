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
import {CdkTextareaAutosize} from '@angular/cdk/text-field';
import {Component, EventEmitter, Output, ViewChild, ViewEncapsulation} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MatCheckboxChange} from '@angular/material/checkbox';
import {MatTableDataSource} from '@angular/material/table';
import {Router} from '@angular/router';
import {LangChangeEvent, TranslateService} from '@ngx-translate/core';
import {environment} from 'projects/pastis/src/environments/environment';
import {Subscription} from 'rxjs';
import {StartupService} from 'ui-frontend-common';
import {FileService} from '../../../core/services/file.service';
import {NotificationService} from '../../../core/services/notification.service';
import {ProfileService} from '../../../core/services/profile.service';
import {SedaService} from '../../../core/services/seda.service';
import {BreadcrumbDataMetadata, BreadcrumbDataTop} from '../../../models/breadcrumb';
import {AttributeData} from '../../../models/edit-attribute-models';
import {
  CardinalityConstants,
  DataTypeConstants,
  FileNode,
  FileNodeInsertAttributeParams,
  FileNodeInsertParams,
  nodeNameToLabel,
  TypeConstants,
  ValueOrDataConstants
} from '../../../models/file-node';
import {CardinalityValues, MetadataHeaders} from '../../../models/models';
import {SedaData, SedaElementConstants} from '../../../models/seda-data';
import {PastisDialogData} from '../../../shared/pastis-dialog/classes/pastis-dialog-data';
import {PastisPopupMetadataLanguageService} from '../../../shared/pastis-popup-metadata-language/pastis-popup-metadata-language.service';
import {FileTreeService} from '../file-tree/file-tree.service';
import {AttributesPopupComponent} from './attributes/attributes.component';
import {FileTreeMetadataService} from './file-tree-metadata.service';


const FILE_TREE_METADATA_TRANSLATE_PATH = 'PROFILE.EDIT_PROFILE.FILE_TREE_METADATA';

function constantToTranslate() {
  this.notificationAjoutMetadonnee = this.translated('.NOTIFICATION_AJOUT_METADONNEE');
  this.boutonAjoutMetadonnee = this.translated('.BOUTON_AJOUT_METADONNEE');
  this.boutonAjoutUA = this.translated('.BOUTON_AJOUT_UA');
  this.popupSousTitre = this.translated('.POPUP_SOUS_TITRE');
  this.popupValider = this.translated('.POPUP_VALIDER');
  this.popupAnnuler = this.translated('.POPUP_ANNULER');
}

@Component({
  selector: 'pastis-file-tree-metadata',
  templateUrl: './file-tree-metadata.component.html',
  styleUrls: ['./file-tree-metadata.component.scss'],
  // Encapsulation has to be disabled in order for the
  // component style to apply to the select panel.
  encapsulation: ViewEncapsulation.None,
})

export class FileTreeMetadataComponent {

  valueOrData = Object.values(ValueOrDataConstants);
  dataType = Object.values(DataTypeConstants);
  cardinalityList: string[];
  cardinalityLabels = Object.values(CardinalityConstants);
  selected = -1;

  // Mat table
  matDataSource: MatTableDataSource<MetadataHeaders>;

  @ViewChild('autosize', {static: false}) autosize: CdkTextareaAutosize;

  displayedColumns: string[] = ['nomDuChamp', 'valeurFixe', 'cardinalite', 'commentaire', 'menuoption'];

  clickedNode: FileNode = {} as FileNode;

  sedaData: SedaData = {} as SedaData;

  // The seda node that has been opened from the left menu
  selectedSedaNode: SedaData;

  selectedCardinalities: string[];

  allowedSedaCardinalityList: string[][];

  cardinalityValues: CardinalityValues[] = [];

  regexPattern = '';

  patternType: string;

  rowIndex: number;

  hoveredElementId: number;

  buttonIsClicked: boolean;

  isStandalone: boolean = environment.standalone;

  public breadcrumbDataTop: Array<BreadcrumbDataTop>;
  public breadcrumbDataMetadata: Array<BreadcrumbDataMetadata>;

  profileModeLabel: string;

  config: {};


  notificationAjoutMetadonnee: string;
  boutonAjoutMetadonnee: string;
  boutonAjoutUA: string;
  popupSousTitre: string;
  popupValider: string;
  popupAnnuler: string;

  @Output()
  public insertItem: EventEmitter<FileNodeInsertParams> = new EventEmitter<FileNodeInsertParams>();

  @Output()
  public addNode: EventEmitter<FileNode> = new EventEmitter<FileNode>();

  @Output()
  public insertAttributes: EventEmitter<FileNodeInsertAttributeParams> = new EventEmitter<FileNodeInsertAttributeParams>();

  @Output()
  public removeNode: EventEmitter<FileNode> = new EventEmitter<FileNode>();

  private _profileServiceProfileModeSubscription: Subscription;

  @Output()
  public duplicateNode: EventEmitter<FileNode> = new EventEmitter<FileNode>();

  private _fileServiceSubscription: Subscription;
  private _fileMetadataServiceSubscriptionSelectedCardinalities: Subscription;
  private _fileServiceSubscriptionNodeChange: Subscription;
  private _sedaServiceSubscritptionSelectedSedaNode: Subscription;
  private _fileMetadataServiceSubscriptionDataSource: Subscription;
  private _sedalanguageSub: Subscription;

  sedaLanguage: boolean;

  docPath: string;

  languagePopup: boolean;

  metadatadaValueFormControl = new FormControl('', [Validators.required, Validators.pattern(this.regexPattern)]);

  valueForm = this.fb.group({
    valeurFixe: ['', [Validators.pattern(this.regexPattern)]],
  });
  public searchForm: FormGroup;
  id: number;
  nomDuChamp: string;
  type: string;
  valeurFixe: string;
  cardinalite: string[];
  commentaire: string;
  enumeration: string[];

  constructor(private fileService: FileService, private fileMetadataService: FileTreeMetadataService,
              private sedaService: SedaService, private fb: FormBuilder, private notificationService: NotificationService,
              private router: Router, private startupService: StartupService, public profileService: ProfileService,
              private fileTreeService: FileTreeService, private metadataLanguageService: PastisPopupMetadataLanguageService,
              private translateService: TranslateService) {

    this.config = {
      locale: 'fr',
      showGoToCurrent: false,
      firstDayOfWeek: 'mo',
      format: 'YYYY-MM-DD'
    };
  }

  ngOnInit() {

    if (!this.isStandalone) {
      constantToTranslate.call(this);
      this.translatedOnChange();
    } else if (this.isStandalone) {
      this.notificationAjoutMetadonnee = 'La métadonnée ArchiveUnit a été ajoutée';
      this.boutonAjoutMetadonnee = 'Ajouter une métadonnée';
      this.boutonAjoutUA = 'Ajouter une UA';
      this.popupSousTitre = 'Edition des attributs de';
      this.popupValider = 'Valider';
      this.popupAnnuler = 'Annuler';
    }


    this.docPath = this.isStandalone ? 'assets/doc/Standalone - Documentation APP - PASTIS.pdf' : 'assets/doc/VITAM UI - Documentation APP - PASTIS.pdf';
    this.languagePopup = false;
    this._sedalanguageSub = this.metadataLanguageService.sedaLanguage.subscribe(
      (value: boolean) => {
        this.sedaLanguage = value;
      },
      (error) => {
        console.log(error);
      }
    );
    this._fileServiceSubscriptionNodeChange = this.fileService.nodeChange.subscribe(node => {
      this.clickedNode = node;
      // BreadCrumb for navigation through metadatas
      if (node && node !== undefined) {
        const breadCrumbNodeLabel: string =  node.name;
        this.fileService.tabRootNode.subscribe(tabRootNode => {
          if (tabRootNode) {
            const tabLabel = (nodeNameToLabel as any)[tabRootNode.name];
            this.breadcrumbDataMetadata = [{ label: tabLabel, node: tabRootNode}];
            if (tabRootNode.name !== breadCrumbNodeLabel) {
              if (node.parent) {
                if (node.parent.name !== tabRootNode.name) {
                  if (node.parent.parent) {
                    if (node.parent.parent.name !== tabRootNode.name) {
                      this.breadcrumbDataMetadata = this.breadcrumbDataMetadata.concat([ { label: '...' } ]);
                    }
                  }
                  this.breadcrumbDataMetadata = this.breadcrumbDataMetadata.concat([ { label: node.parent.name, node: node.parent } ]);
                }
                this.breadcrumbDataMetadata = this.breadcrumbDataMetadata.concat([ { label: breadCrumbNodeLabel, node } ]);
              }
            }
          }
        });
      }
    });
    // BreadCrump Top for navigation
    this.profileModeLabel = this.profileService.profileMode === 'PUA' ? 'PROFILE.EDIT_PROFILE.FILE_TREE_METADATA.PUA' : 'PROFILE.EDIT_PROFILE.FILE_TREE_METADATA.PA';
    this.breadcrumbDataTop = [{ label: 'PROFILE.EDIT_PROFILE.BREADCRUMB.PORTAIL', url: this.startupService.getPortalUrl(), external: true}, { label: 'PROFILE.EDIT_PROFILE.BREADCRUMB.CREER_ET_GERER_PROFIL', url: '/'}, { label: this.profileModeLabel }];

    this._fileServiceSubscription = this.fileService.currentTree.subscribe(fileTree => {
      if (fileTree) {
        this.clickedNode = fileTree[0];
        this.fileService.allData.next(fileTree);
        // Subscription to sedaRules
        if (this.clickedNode) {
          const rulesFromService = this.fileService.tabChildrenRulesChange.getValue();
          const tabChildrenToInclude = rulesFromService[0];
          const tabChildrenToExclude = rulesFromService[1];
          this.sedaService.selectedSedaNode.next(this.sedaService.sedaRules[0]);
          this.selectedSedaNode = this.sedaService.sedaRules[0];
          this.fileService.nodeChange.next(this.clickedNode);
          const filteredData = this.fileService.filteredNode.getValue();
          // Initial data for metadata table based on rules defined by tabChildrenRulesChange
          if (filteredData) {
            const dataTable = this.fileMetadataService.fillDataTable(this.selectedSedaNode, filteredData, tabChildrenToInclude, tabChildrenToExclude);
            this.matDataSource = new MatTableDataSource<MetadataHeaders>(dataTable);
          }
        }
      }
    });

    this._fileMetadataServiceSubscriptionSelectedCardinalities = this.fileMetadataService.selectedCardinalities.subscribe(cards => {
      this.selectedCardinalities = cards;
    });

    // Get Current sedaNode
    this._sedaServiceSubscritptionSelectedSedaNode = this.sedaService.selectedSedaNode.subscribe(sedaNode => {
      this.selectedSedaNode = sedaNode;
    });

    this._fileMetadataServiceSubscriptionDataSource = this.fileMetadataService.dataSource.subscribe(data => {
      this.matDataSource = new MatTableDataSource<MetadataHeaders>(data);
    });
  }

  navigate(d: BreadcrumbDataTop) {
      if (d.external) {
        window.location.assign(d.url);
      } else {
        this.router.navigate([d.url], {skipLocationChange: false});
      }
  }

  navigateMetadata(d: BreadcrumbDataMetadata) {
    if (d.node && d.node !== undefined) {
      this.fileTreeService.updateMedataTable.next(d.node);
    }
  }

  // Permet de surcharger le filterPredicate de Material et de filtrer seulement sur la colonne selectionnée au lieu de toutes.
  setupFilter(column: string) {
    this.matDataSource.filterPredicate = (d: MetadataHeaders, filter: string) => {
      // @ts-ignore
      const textToSearch = d[column] && d[column].toLowerCase() || '';
      return textToSearch.indexOf(filter) !== -1;
    };
  }

  // Application du filtre sur la colonne 'nomDuChamp' correspondant aux noms métadonnées
  applyFilterTier(filterValue: string) {
    const nomDuchamp: string = this.sedaLanguage ? 'nomDuChamp' : 'nomDuChampFr';
    this.setupFilter(nomDuchamp);
    // Lors d'un reset sur le search component on renvoie un string null.
    if (filterValue == null) {
      filterValue = '';
    }
    this.matDataSource.filter = filterValue.trim().toLowerCase();
  }

  translatedOnChange(): void {
    this.translateService.onLangChange
      .subscribe((event: LangChangeEvent) => {
        constantToTranslate.call(this);
        console.log(event.lang);
      });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(FILE_TREE_METADATA_TRANSLATE_PATH + nameOfFieldToTranslate);
  }

  getMetadataInputPattern(type: string) {
    if (type === 'date') {
      this.regexPattern = '([0-2][0-9]|(3)[0-1])(\/)(((0)[0-9])|((1)[0-2]))(\/)\d{4}';
      return this.regexPattern;
    }
    if (type === 'TextType' || type === null) {
      this.regexPattern = '^[a-zA-X0-9 ]*$';
      return this.regexPattern;
    }
  }

  getMetadataInputType(element: MetadataHeaders) {
    if (element.type === 'date') {
      return 'date';
    }
    if (element.enumeration.length > 0) {
      return 'enumeration';
    }
  }

  findCardinality(event: any) {

    if (!event) {
      return CardinalityConstants.Obligatoire;
    } else {
      return event;
    }

  }

  isSedaCardinalityConform(cardList: string[], card: string) {
    return cardList.includes(card);
  }

  findCardinalityName(clickedNode: FileNode) {
    if (!clickedNode.cardinality) {
      return '1';
    } else {
      return this.cardinalityValues.find(c => c.value == clickedNode.cardinality).value;
    }
  }

  setNodeChildrenCardinalities(metadata: MetadataHeaders, newCard: string) {
    if (this.clickedNode.name === metadata.nomDuChamp && this.clickedNode.id === metadata.id) {
      this.clickedNode.cardinality = newCard;
    } else if (this.clickedNode.children.length > 0) {
      const childNode = this.fileService.getFileNodeById(this.clickedNode, metadata.id);
      if (childNode) {
        childNode.cardinality = newCard;
      }
    }

  }

  setNodeValue(metadata: MetadataHeaders, newValue: string) {
    console.log(metadata.cardinalite + 'new Value ' + newValue);
    if (newValue != null) {
      const updatedValue = newValue.length > 0 ? newValue : null;
      if (this.clickedNode.name === metadata.nomDuChamp) {
        this.clickedNode.value = updatedValue;
      } else if (this.clickedNode.children.length > 0) {
        const childNode = this.fileService.getFileNodeById(this.clickedNode, metadata.id);
        if (childNode) {
          childNode.value = updatedValue;
        }
      }
    }
  }

  setDocumentation(metadata: MetadataHeaders, comment: string) {
    if (this.clickedNode.name === metadata.nomDuChamp && this.clickedNode.id === metadata.id) {
      comment ? this.clickedNode.documentation = comment : this.clickedNode.documentation = null;
    } else {
      for (const node of this.clickedNode.children) {
        if (node.name === metadata.nomDuChamp && node.id === metadata.id) {
          comment ? node.documentation = comment : node.documentation = null;
        }
      }
    }
  }

  isElementComplex(elementName: string) {
    const childFound = this.selectedSedaNode.Children.find(el => el.Name === elementName);
    if (childFound) {
      return childFound.Element === SedaElementConstants.complex;
    }
  }

  onAddNode() {
    if (this.clickedNode.name === 'DescriptiveMetadata') {
      console.log('Yes');
      let elements: SedaData[];
      elements.push({
        Name: 'ArchiveUnit',
        NameFr: null,
        Type: null,
        Element: null,
        Cardinality: null,
        Definition: null,
        Extensible: null,
        Choice: null,
        Children: null,
        Enumeration: null,
        Collection: null
      });
      const params: FileNodeInsertParams = {
        node: this.clickedNode,
        elementsToAdd: elements
      };
      this.insertItem.emit(params);
      this.notificationService.showSuccess(this.notificationAjoutMetadonnee);

    } else {
      this.addNode.emit(this.clickedNode);
    }
  }

  onDuplicateNode(id: number) {
    const nodeToDuplicate = this.fileService.getFileNodeById(this.fileService.nodeChange.getValue(), id);
    this.duplicateNode.emit(nodeToDuplicate);
  }

  async onEditAttributesClick(fileNodeId: number) {
    const popData = {} as PastisDialogData;
    const attributeFileNodeListToAdd: FileNode[] = [];
    const attributeFileNodeListToRemove: FileNode[] = [];

    if (fileNodeId) {
      popData.fileNode = this.fileService.findChildById(fileNodeId, this.clickedNode);
      popData.subTitleDialog = this.popupSousTitre;
      popData.titleDialog = popData.fileNode.name;
      popData.width = '1120px';
      popData.component = AttributesPopupComponent;
      popData.okLabel = this.popupValider;
      popData.cancelLabel = this.popupAnnuler;

      const popUpAnswer = await this.fileService.openPopup(popData) as AttributeData[];
      console.log('The answer for edit attributte was ', popUpAnswer);

      if (popUpAnswer) {

        // Create a list of attributes to add
        popUpAnswer.filter(a => a.selected).forEach(attr => {
          const fileNode = {} as FileNode;
          fileNode.cardinality = attr.selected ? '1' : null;
          fileNode.value = attr.valeurFixe ? attr.valeurFixe : null;
          fileNode.documentation = attr.commentaire ? attr.commentaire : null;
          fileNode.name = attr.nomDuChamp;
          fileNode.type = TypeConstants.attribute;
          fileNode.sedaData = this.sedaService.findSedaChildByName(attr.nomDuChamp, popData.fileNode.sedaData);
          fileNode.children = [];
          fileNode.id = attr.id;
          attributeFileNodeListToAdd.push(fileNode);
        });
        // Create a list of attributes to remove
        popUpAnswer.filter(a => !a.selected).forEach(attr => {
          const fileNode: FileNode = {} as FileNode;
          fileNode.name = attr.nomDuChamp;
          attributeFileNodeListToRemove.push(fileNode);
        });
        if (attributeFileNodeListToAdd) {
          const insertOrEditParams: FileNodeInsertAttributeParams = {
            node: popData.fileNode,
            elementsToAdd: attributeFileNodeListToAdd
          };
          const attrsToAdd = attributeFileNodeListToAdd.map(e => e.name);
          const attributeExists = popData.fileNode.children.some((child: { name: string; }) => attrsToAdd.includes(child.name));

          // Add attribute (if it does not exist), or update them if they do
          if (attrsToAdd && !attributeExists) {
            this.insertAttributes.emit(insertOrEditParams);
          } else {
            this.fileService.updateNodeChildren(popData.fileNode, attributeFileNodeListToAdd);
          }
        }
        if (attributeFileNodeListToRemove.length) {
          this.fileService.removeItem(attributeFileNodeListToRemove, popData.fileNode);
        }
      }
    }
  }

  onDeleteNode(nodeId: number) {
    const nodeToDelete = this.fileService.getFileNodeById(this.fileService.nodeChange.getValue(), nodeId);
    this.removeNode.emit(nodeToDelete);
  }

  onButtonClicked(elementId: number) {
    this.hoveredElementId = elementId;
  }

  isButtonClicked(elementId: number, data: MetadataHeaders) {
    if (data) {
      this.hoveredElementId = elementId;
      this.buttonIsClicked = true;
      return data.id === this.hoveredElementId;
    }
  }

  isRowHovered(elementId: number) {
    return this.hoveredElementId === elementId;
  }

  onMouseOver(row: MetadataHeaders) {
    this.buttonIsClicked = false;
    this.hoveredElementId = row.id;
  }

  onMouseLeave() {
    if (!this.buttonIsClicked) {
      this.hoveredElementId = 0;
    }
  }

  checkElementType(elementName?: string) {
    if (this.selectedSedaNode) {
      const nameToSearch = elementName ? elementName : this.sedaService.selectedSedaNode.getValue().Name;
      const nodeElementType = this.sedaService.checkSedaElementType(nameToSearch, this.selectedSedaNode);
      return nodeElementType === SedaElementConstants.complex;
    }
  }

  shouldLoadMetadataTable() {
    return this.fileMetadataService.shouldLoadMetadataTable.getValue();
  }

  /**
   * Returns a boolean if a given node has one or more attributes
   * regarding its seda specification
   * @param nodeName The node's name to be tested
   */
  hasAttributes(nodeName: string): boolean {

    const node = this.sedaService.findSedaChildByName(nodeName, this.selectedSedaNode);

    if (node && node.Children.length > 0) {
      return (node.Children.find(c => c.Element == SedaElementConstants.attribute) !== undefined);
    }
    return false;
  }


  isSedaObligatory(name: string): boolean {
    return this.sedaService.isSedaNodeObligatory(name, this.selectedSedaNode);
  }

  getSedaDefinition(elementName: string) {
    const node = this.getSedaNode(elementName);
    if (node != null) {
      return node.Definition;
    }
    return '';
  }

  getSedaNode(elementName: string): SedaData {
    if (this.selectedSedaNode.Name === elementName) {
      return this.selectedSedaNode;
    } else {
      for (const node of this.selectedSedaNode.Children) {
        if (node.Name === elementName) {
          return node;
        }
      }
    }
    return null;
  }

  onResolveName(elementName: string) {
    if (this.sedaLanguage) {
      return elementName;
    }
    const node = this.getSedaNode(elementName);
    if (node != null) {
      if (node.NameFr) {
        return node.NameFr;
      }
      return node.Name;
    }
    return elementName;
  }


  resolveButtonLabel(node: FileNode) {
    if (node) {
      return node.name === 'DescriptiveMetadata' ? null : this.boutonAjoutMetadonnee;
    }
  }

  resolveCurrentNodeName() {
    if (this.clickedNode) {
      return this.clickedNode.name;
    }
  }

  goBack() {
    this.router.navigate(['/'], {skipLocationChange: false});
  }

  ngOnDestroy() {
    if (this._fileServiceSubscription != null) {
      this._fileServiceSubscription.unsubscribe();
    }
    if (this._fileMetadataServiceSubscriptionSelectedCardinalities != null) {
      this._fileMetadataServiceSubscriptionSelectedCardinalities.unsubscribe();
    }
    if (this._fileServiceSubscriptionNodeChange != null) {
      this._fileServiceSubscriptionNodeChange.unsubscribe();
    }
    if (this._sedaServiceSubscritptionSelectedSedaNode != null) {
      this._sedaServiceSubscritptionSelectedSedaNode.unsubscribe();
    }
    if (this._fileMetadataServiceSubscriptionDataSource != null) {
      this._fileMetadataServiceSubscriptionDataSource.unsubscribe();
    }
    if (this._profileServiceProfileModeSubscription != null) {
      this._profileServiceProfileModeSubscription.unsubscribe();
    }
    if (this._sedalanguageSub != null) {
      this._sedalanguageSub.unsubscribe();
    }
  }

  onChange(val: any, $event: MatCheckboxChange) {

    console.log('onChange file tree metadata go dans methode setNodeValue' + val + ' et event ' + $event);
    // @ts-ignore
    this.setNodeValue(val, $event);


  }

  changeSedaLanguage() {
    this.metadataLanguageService.sedaLanguage.subscribe(
      (value: boolean) => {
        this.sedaLanguage = value;
      },
      (error) => {
        console.log(error);
      }
    );
  }

  openChoicePopup() {
    this.languagePopup = !this.languagePopup;
  }

  isDuplicated(nomDuChamp: any) {
    return this.sedaService.isDuplicated(nomDuChamp, this.selectedSedaNode);
  }

}
