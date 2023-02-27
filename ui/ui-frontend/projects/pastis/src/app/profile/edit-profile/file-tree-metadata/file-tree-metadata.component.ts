/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 *
 *
 */
import { CdkTextareaAutosize } from '@angular/cdk/text-field';
import { Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatTableDataSource } from '@angular/material/table';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { environment } from 'projects/pastis/src/environments/environment';
import { Subscription } from 'rxjs';
import { StartupService } from 'ui-frontend-common';
import { FileService } from '../../../core/services/file.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ProfileService } from '../../../core/services/profile.service';
import { SedaService } from '../../../core/services/seda.service';
import { BreadcrumbDataMetadata, BreadcrumbDataTop } from '../../../models/breadcrumb';
import { AttributeData } from '../../../models/edit-attribute-models';
import {
  DateFormatType,
  FileNode,
  FileNodeInsertAttributeParams,
  FileNodeInsertParams,
  nodeNameToLabel,
  TypeConstants,
} from '../../../models/file-node';
import { CardinalityValues, MetadataHeaders } from '../../../models/models';
import { SedaData, SedaElement } from '../../../models/seda-data';
import { PastisDialogData } from '../../../shared/pastis-dialog/classes/pastis-dialog-data';
import { PastisPopupMetadataLanguageService } from '../../../shared/pastis-popup-metadata-language/pastis-popup-metadata-language.service';
import { UserActionAddPuaControlComponent } from '../../../user-actions/add-pua-control/add-pua-control.component';
import { FileTreeComponent } from '../file-tree/file-tree.component';
import { FileTreeService } from '../file-tree/file-tree.service';
import { AttributesPopupComponent } from './attributes/attributes.component';
import { FileTreeMetadataService } from './file-tree-metadata.service';

const FILE_TREE_METADATA_TRANSLATE_PATH = 'PROFILE.EDIT_PROFILE.FILE_TREE_METADATA';
const ADD_PUA_CONTROL_TRANSLATE_PATH = 'USER_ACTION.ADD_PUA_CONTROL';
const PA_MANDATORY_ENUM_FIELDS = [
  'NeedAuthorization',
  'LegalStatus',
  'DescriptionLevel',
  'KeywordType',
  'PreventInheritance',
  'FinalAction',
  'NeedReassessingAuthorization',
];

function constantToTranslate() {
  this.notificationAjoutMetadonnee = this.translated('.NOTIFICATION_AJOUT_METADONNEE');
  this.boutonAjoutMetadonnee = this.translated('.BOUTON_AJOUT_METADONNEE');
  this.boutonAjoutUA = this.translated('.BOUTON_AJOUT_UA');
  this.popupSousTitre = this.translated('.POPUP_SOUS_TITRE');
  this.popupValider = this.translated('.POPUP_VALIDER');
  this.popupAnnuler = this.translated('.POPUP_ANNULER');
  this.popupControlOkLabel = this.translated('.POPUP_CONTROL_OK_BUTTON_LABEL');
  this.popupControlSuAppComponentbTitleDialog = this.translated('.POPUP_CONTROL_SUB_TITLE_DIALOG');
  this.popupControlTitleDialog = this.translated('.POPUP_CONTROL_TITLE_DIALOG');
  this.popupControlSubTitleDialog = this.translated('.POPUP_CONTROL_SUB_TITLE_DIALOG');
}

@Component({
  selector: 'pastis-file-tree-metadata',
  templateUrl: './file-tree-metadata.component.html',
  styleUrls: ['./file-tree-metadata.component.scss'],
  // Encapsulation has to be disabled in order for the
  // component style to apply to the select panel.
  encapsulation: ViewEncapsulation.None,
})
export class FileTreeMetadataComponent implements OnInit, OnDestroy {
  public breadcrumbDataTop: BreadcrumbDataTop[];
  public breadcrumbDataMetadata: BreadcrumbDataMetadata[];

  @Output() insertItem: EventEmitter<FileNodeInsertParams> = new EventEmitter<FileNodeInsertParams>();
  @Output() addNode: EventEmitter<FileNode> = new EventEmitter<FileNode>();
  @Output() insertAttributes: EventEmitter<FileNodeInsertAttributeParams> = new EventEmitter<FileNodeInsertAttributeParams>();
  @Output() removeNode: EventEmitter<FileNode> = new EventEmitter<FileNode>();
  @Output() duplicateNode: EventEmitter<FileNode> = new EventEmitter<FileNode>();

  @ViewChild('autosize', { static: false }) autosize: CdkTextareaAutosize;

  rootAdditionalProperties: boolean;
  selected = -1;
  // Mat table
  matDataSource: MatTableDataSource<MetadataHeaders>;
  displayedColumns: string[] = ['nomDuChamp', 'valeurFixe', 'cardinalite', 'commentaire', 'menuoption'];
  clickedNode: FileNode = {} as FileNode;
  sedaData: SedaData = {} as SedaData;
  // The seda node that has been opened from the left menu
  selectedSedaNode: SedaData;
  selectedCardinalities: string[];
  cardinalityValues: CardinalityValues[] = [];
  regexPattern = '';
  hoveredElementId: number;
  buttonIsClicked: boolean;
  isStandalone: boolean = environment.standalone;
  enumerationControl: boolean;
  expressionControl: boolean;
  arrayControl: string[];
  clickedControl: FileNode;
  enumerationsSedaControl: string[];
  enumsControlSeleted: string[] = [];
  editedEnumControl: string[];
  openControls: boolean;
  radioExpressionReguliere: string;
  regex: string;
  customRegex: string;
  formatagePredefini: Array<{ label: string; value: string }> = [
    { label: 'AAAA-MM-JJ', value: '^[0-9]{4}-[0-9]{2}-[0-9]{2}$' },
    { label: 'AAAA-MM-JJTHH:MM:SS', value: '^[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}$' },
    { label: 'AAAA', value: '^[0-9]{4}$' },
    { label: 'AAAA-MM', value: '^[0-9]{4}-[0-9]{2}$' },
  ];
  availableRegex: Array<{ label: string; value: string }>;
  profileModeLabel: string;
  config: {
    locale: 'fr';
    showGoToCurrent: false;
    firstDayOfWeek: 'mo';
    format: 'YYYY-MM-DD';
  };
  notificationAjoutMetadonnee: string;
  boutonAjoutMetadonnee: string;
  boutonAjoutUA: string;
  popupSousTitre: string;
  popupValider: string;
  popupAnnuler: string;
  popupControlTitleDialog: string;
  popupControlSubTitleDialog: string;
  popupControlOkLabel: string;
  sedaLanguage: boolean;
  docPath: string;
  languagePopup: boolean;

  private rootSubscription: Subscription;

  constructor(
    private fileMetadataService: FileTreeMetadataService,
    private fileService: FileService,
    private fileTreeService: FileTreeService,
    private metadataLanguageService: PastisPopupMetadataLanguageService,
    private notificationService: NotificationService,
    private router: Router,
    private sedaService: SedaService,
    private startupService: StartupService,
    private translateService: TranslateService,
    public profileService: ProfileService
  ) {}

  ngOnInit() {
    this.rootSubscription = new Subscription();

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
      this.popupControlTitleDialog = 'Veuillez séléctionner un ou plusieurs contrôles';
      this.popupControlSubTitleDialog = 'Ajouter des contrôles supplémentaires à';
      this.popupControlOkLabel = 'AJOUTER LES CONTROLES';
    }
    this.docPath = this.isStandalone
      ? 'assets/doc/Standalone - Documentation APP - PASTIS.pdf'
      : 'assets/doc/VITAM UI - Documentation APP - PASTIS.pdf';
    this.languagePopup = false;
    this.rootSubscription.add(
      this.metadataLanguageService.sedaLanguage.subscribe((value: boolean) => {
        this.sedaLanguage = value;
      }, console.error)
    );
    this.rootSubscription.add(
      this.fileService.nodeChange.subscribe((node) => {
        if (!node) return;

        this.clickedNode = node;

        // BreadCrumb for navigation through metadatas
        this.rootSubscription.add(
          this.fileService.tabRootNode.subscribe((tabRootNode) => {
            if (!tabRootNode) return;

            const tabLabel = (nodeNameToLabel as any)[tabRootNode.name];

            this.breadcrumbDataMetadata = [{ label: tabLabel, node: tabRootNode }];

            if (tabRootNode.name === node.name) return;
            if (!node.parent) return;

            if (node.parent.name !== tabRootNode.name) {
              if (node.parent.parent) {
                if (node.parent.parent.name !== tabRootNode.name) {
                  this.breadcrumbDataMetadata = this.breadcrumbDataMetadata.concat([{ label: '...' }]);
                }
              }
              this.breadcrumbDataMetadata = this.breadcrumbDataMetadata.concat([{ label: node.parent.name, node: node.parent }]);
            }
            this.breadcrumbDataMetadata = this.breadcrumbDataMetadata.concat([{ label: node.name, node }]);
          })
        );
      })
    );
    // BreadCrump Top for navigation
    this.profileModeLabel = this.profileService.isArchiveUnitProfileMode()
      ? 'PROFILE.EDIT_PROFILE.FILE_TREE_METADATA.PUA'
      : 'PROFILE.EDIT_PROFILE.FILE_TREE_METADATA.PA';
    this.breadcrumbDataTop = [
      {
        label: 'PROFILE.EDIT_PROFILE.BREADCRUMB.PORTAIL',
        url: this.startupService.getPortalUrl(),
        external: true,
      },
      { label: 'PROFILE.EDIT_PROFILE.BREADCRUMB.CREER_ET_GERER_PROFIL', url: '/' },
      { label: this.profileModeLabel },
    ];

    this.rootSubscription.add(
      this.fileService.currentTree.subscribe((fileTree) => {
        if (!fileTree) return;

        this.clickedNode = fileTree[0];
        this.fileService.allData.next(fileTree);

        if (!this.clickedNode) return;

        // Subscription to sedaRules
        const rulesFromService = this.fileService.tabChildrenRulesChange.getValue();
        const tabChildrenToInclude = rulesFromService[0];
        const tabChildrenToExclude = rulesFromService[1];
        const filteredData = this.fileService.filteredNode.getValue();

        this.sedaService.selectedSedaNode.next(this.sedaService.sedaRules[0]);
        this.selectedSedaNode = this.sedaService.sedaRules[0];
        this.fileService.nodeChange.next(this.clickedNode);

        if (!filteredData) return;

        // Initial data for metadata table based on rules defined by tabChildrenRulesChange
        const initialData = this.fileMetadataService.fillDataTable(
          this.selectedSedaNode,
          filteredData,
          tabChildrenToInclude,
          tabChildrenToExclude
        );

        this.matDataSource = new MatTableDataSource<MetadataHeaders>(this.extendsMetadatHeadersWithFileNode(filteredData)(initialData));
      })
    );

    this.rootSubscription.add(
      this.fileMetadataService.selectedCardinalities.subscribe((cards) => {
        this.selectedCardinalities = cards;
      })
    );

    // Get Current sedaNode
    this.rootSubscription.add(
      this.sedaService.selectedSedaNode.subscribe((sedaNode) => {
        this.selectedSedaNode = sedaNode;
      })
    );

    this.rootSubscription.add(
      this.fileMetadataService.dataSource.subscribe((data) => {
        if (data)
          this.matDataSource = new MatTableDataSource<MetadataHeaders>(this.extendsMetadatHeadersWithFileNode(this.clickedNode)(data));
      })
    );

    if (this.clickedNode) {
      this.rootAdditionalProperties = this.clickedNode.additionalProperties;
    }
  }

  ngOnDestroy() {
    this.rootSubscription.unsubscribe();
  }

  navigate(d: BreadcrumbDataTop) {
    if (!d) return;

    d.external ? window.location.assign(d.url) : this.router.navigate([d.url], { skipLocationChange: false });
  }

  navigateMetadata(d: BreadcrumbDataMetadata) {
    if (!d) return;
    if (!d.node) return;

    this.fileTreeService.updateMedataTable.next(d.node);
  }

  // Permet de surcharger le filterPredicate de Material et de filtrer seulement sur la colonne selectionnée au lieu de toutes.
  setupFilter(column: string) {
    this.matDataSource.filterPredicate = (d: MetadataHeaders, filter: string) => {
      // @ts-ignore
      const textToSearch = (d[column] && d[column].toLowerCase()) || '';

      return textToSearch.indexOf(filter) !== -1;
    };
  }

  // Application du filtre sur la colonne 'nomDuChamp' correspondant aux noms métadonnées
  applyFilterTier(filterValue: string) {
    const nomDuchamp: string = this.sedaLanguage ? 'nomDuChamp' : 'nomDuChampFr';

    this.setupFilter(nomDuchamp);
    // Lors d'un reset sur le search component on renvoie un string null.
    if (filterValue === null) {
      filterValue = '';
    }
    this.matDataSource.filter = filterValue.trim().toLowerCase();
  }

  translatedOnChange(): void {
    this.rootSubscription.add(
      this.translateService.onLangChange.subscribe(() => {
        constantToTranslate.call(this);
      })
    );
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(FILE_TREE_METADATA_TRANSLATE_PATH + nameOfFieldToTranslate);
  }

  getMetadataInputPattern(type: string) {
    if (type === 'date') {
      this.regexPattern = '([0-2][0-9]|(3)[0-1])(/)(((0)[0-9])|((1)[0-2]))(/)d{4}';
      return this.regexPattern;
    }
    if (type === 'TextType' || type === null) {
      this.regexPattern = '^[a-zA-X0-9 ]*$';
      return this.regexPattern;
    }
  }

  getMetadataInputType(element: MetadataHeaders) {
    if (element) {
      if (element.type === 'date') {
        return 'date';
      } else if (PA_MANDATORY_ENUM_FIELDS.includes(element.nomDuChamp)) {
        return 'enumeration';
      } else {
        return '';
      }
    } else {
      return '';
    }
  }

  isSedaCardinalityConform(cardList: string[], card: string) {
    return cardList.includes(card);
  }

  setNodeChildrenCardinalities(metadata: MetadataHeaders, cardinality: string) {
    if (this.clickedNode.name === metadata.nomDuChamp && this.clickedNode.id === metadata.id) {
      this.clickedNode.cardinality = cardinality;
    } else if (this.clickedNode.children.length > 0) {
      const childNode = this.fileService.getFileNodeById(this.clickedNode, metadata.id);

      if (childNode) {
        childNode.cardinality = cardinality;
      }
    }
  }

  setNodeValue(metadata: MetadataHeaders, value: string) {
    if (!value) return;

    const updatedValue = value.length > 0 ? value : null;

    if (this.clickedNode.name === metadata.nomDuChamp && this.clickedNode.id === metadata.id) {
      this.clickedNode.value = updatedValue;
    } else if (this.clickedNode.children.length > 0) {
      const childNode = this.fileService.getFileNodeById(this.clickedNode, metadata.id);

      if (childNode) {
        childNode.value = updatedValue;
      }
    }
  }

  setDocumentation(metadata: MetadataHeaders, comment: string) {
    if (this.clickedNode.name === metadata.nomDuChamp && this.clickedNode.id === metadata.id) {
      this.clickedNode.documentation = comment || null;
    } else {
      const child = this.clickedNode.children.find((node) => node.name === metadata.nomDuChamp && node.id === metadata.id);

      if (child) child.documentation = comment || null;
    }
  }

  hasComplexeSedaNodeChild(node: SedaData): boolean {
    return node.Children.some((child) => child.Element === SedaElement.COMPLEXE);
  }

  isElementComplex(elementName: string) {
    const childFound = this.selectedSedaNode.Children.find((el) => el.Name === elementName);

    if (childFound) return childFound.Element === SedaElement.COMPLEXE;

    return false;
  }

  isAloneAndSimple(metadatas: MatTableDataSource<MetadataHeaders>): boolean {
    const node: SedaData = this.selectedSedaNode;
    const metadataHeaders: MetadataHeaders[] = metadatas.data;
    const hasLonelyNode = metadataHeaders.length === 1;

    if (!node) return false;
    if (!metadataHeaders) return false;
    if (!metadataHeaders[0]) return false;
    if (!hasLonelyNode) return false;
    if (this.hasComplexeSedaNodeChild(node)) return false;

    return true;
  }

  onAddNode() {
    if (this.clickedNode.name === 'DescriptiveMetadata') {
      const params: FileNodeInsertParams = {
        node: this.clickedNode,
        elementsToAdd: [
          {
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
            Collection: null,
          },
        ],
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
    if (!fileNodeId) return;

    const fileNode: FileNode = this.fileService.findChildById(fileNodeId, this.clickedNode);
    const popData: PastisDialogData = {
      cancelLabel: this.popupAnnuler,
      component: AttributesPopupComponent,
      disableBtnOuiOnInit: false,
      fileNode,
      height: null,
      okLabel: this.popupValider,
      subTitleDialog: this.popupSousTitre,
      titleDialog: fileNode.name,
      width: '1120px',
    };
    const attributeFileNodeListToAdd: FileNode[] = [];
    const attributeFileNodeListToRemove: FileNode[] = [];
    const popUpAnswer = (await this.fileService.openPopup(popData)) as AttributeData[];

    if (!popUpAnswer) return;

    // Create a list of attributes to add
    popUpAnswer
      .filter((a) => a.selected)
      .forEach((attr) => {
        const partialFileNode: any = {
          cardinality: attr.selected ? '1' : null,
          value: attr.valeurFixe ? attr.valeurFixe : null,
          documentation: attr.commentaire ? attr.commentaire : null,
          name: attr.nomDuChamp,
          type: TypeConstants.attribute,
          sedaData: this.sedaService.findSedaChildByName(attr.nomDuChamp, popData.fileNode.sedaData),
          children: [],
          id: attr.id,
        };

        attributeFileNodeListToAdd.push(partialFileNode);
      });
    // Create a list of attributes to remove
    popUpAnswer
      .filter((a) => !a.selected)
      .forEach((attr) => {
        const partialFileNode: any = {
          name: attr.nomDuChamp,
        };

        attributeFileNodeListToRemove.push(partialFileNode);
      });

    if (attributeFileNodeListToAdd) {
      const insertOrEditParams: FileNodeInsertAttributeParams = {
        node: popData.fileNode,
        elementsToAdd: attributeFileNodeListToAdd,
      };
      const attrsToAdd = attributeFileNodeListToAdd.map((e) => e.name);
      const attributeExists = popData.fileNode.children.some((child: { name: string }) => attrsToAdd.includes(child.name));

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

  async onControlClick(fileNodeId: number) {
    if (!fileNodeId) return;
    if (fileNodeId !== this.clickedNode.id) return;

    this.resetControls();

    const fileNode: FileNode = this.fileService.findChildById(fileNodeId, this.clickedNode);
    const popData: PastisDialogData = {
      cancelLabel: this.popupAnnuler,
      component: UserActionAddPuaControlComponent,
      disableBtnOuiOnInit: false,
      fileNode,
      height: null,
      okLabel: this.popupControlOkLabel,
      subTitleDialog: this.popupControlSubTitleDialog + ' "' + fileNode.name + '"',
      titleDialog: this.popupControlTitleDialog,
      width: '800px',
    };

    this.clickedControl = fileNode;

    const popUpAnswer = (await this.fileService.openPopup(popData)) as string[];

    if (!popUpAnswer) return;

    this.arrayControl = popUpAnswer;
    this.setControlsVues(this.arrayControl, popData.fileNode.name);
    this.openControls = true;
  }

  onEditControlClick(fileNodeId: number) {
    this.resetControls();
    const fileNode = this.fileService.findChildById(fileNodeId, this.clickedNode);
    this.clickedControl = fileNode;
    if (fileNode.puaData && fileNode.puaData.enum) {
      this.enumerationsSedaControl = this.selectedSedaNode.Enumeration;
      this.enumerationControl = true;
      this.editedEnumControl = [];
      this.enumsControlSeleted = [];
      this.openControls = true;
      const type: string = this.selectedSedaNode.Type;
      this.setAvailableRegex(type);
      fileNode.puaData.enum.forEach((e) => {
        this.editedEnumControl.push(e);
        this.enumsControlSeleted.push(e);
      });
    }
    if (fileNode.puaData && fileNode.puaData.pattern) {
      const actualPattern = fileNode.puaData.pattern;
      this.openControls = true;
      this.expressionControl = true;
      if (this.formatagePredefini.map((e) => e.value).includes(actualPattern)) {
        const type: string = this.selectedSedaNode.Type;
        this.setAvailableRegex(type);
        this.regex = this.availableRegex.filter((e) => e.value === actualPattern).map((e) => e.value)[0];
        this.radioExpressionReguliere = 'select';
      } else {
        this.customRegex = actualPattern;
        this.radioExpressionReguliere = 'input';
      }
    } else {
      this.customRegex = '';
    }
  }

  isAppliedControl(fileNodeId: number): boolean {
    const fileNode = this.fileService.findChildById(fileNodeId, this.clickedNode);

    if (!fileNode) return false;
    if (!fileNode.puaData) return false;

    return !!fileNode.puaData.enum || !!fileNode.puaData.pattern;
  }

  resetControls() {
    this.arrayControl = [];
    this.enumerationControl = false;
    this.expressionControl = false;
    this.enumsControlSeleted = [];
    this.editedEnumControl = [];
    this.openControls = false;
    this.regex = undefined;
    this.customRegex = undefined;
    this.enumerationsSedaControl = [];
  }

  private setAvailableRegex(type: string) {
    switch (type) {
      case DateFormatType.date:
        this.availableRegex = this.formatagePredefini.filter((e) => e.label === 'AAAA-MM-JJ');
        break;
      case DateFormatType.dateTime:
        this.availableRegex = this.formatagePredefini.filter((e) => e.label === 'AAAA-MM-JJTHH:MM:SS');
        break;
      case DateFormatType.dateType:
        this.availableRegex = this.formatagePredefini;
        break;
      default:
        this.availableRegex = this.formatagePredefini.filter((e) => e.label === 'AAAA-MM-JJ' || e.label === 'AAAA');
        break;
    }
  }

  isDataType(): boolean {
    const type: string = this.selectedSedaNode.Type;

    return type === DateFormatType.date || type === DateFormatType.dateTime || type === DateFormatType.dateType;
  }

  setControlsVues(elements: string[], sedaName: string) {
    const ENUM = 'Enumération';
    const REGEX = 'Expression régulière';

    if (elements.includes(ENUM) || elements.includes(this.translated(ADD_PUA_CONTROL_TRANSLATE_PATH + '.ENUMERATIONS_LABEL'))) {
      const type: string = this.sedaService.findSedaChildByName(sedaName, this.selectedSedaNode).Type;

      this.enumerationControl = true;
      this.enumerationsSedaControl = this.sedaService.findSedaChildByName(sedaName, this.selectedSedaNode).Enumeration;
      this.editedEnumControl = this.enumerationsSedaControl;
      this.enumsControlSeleted = this.enumerationsSedaControl;
      this.setAvailableRegex(type);
    }

    if (elements.includes(REGEX) || elements.includes(this.translated(ADD_PUA_CONTROL_TRANSLATE_PATH + '.EXPRESSION_REGULIERE_LABEL'))) {
      const type: string = this.sedaService.findSedaChildByName(sedaName, this.selectedSedaNode).Type;

      this.radioExpressionReguliere = 'select';
      this.expressionControl = true;
      this.customRegex = '';
      this.setAvailableRegex(type);
      this.regex = this.formatagePredefini[0].value;
    }
  }

  isNotRegexCustomisable(): boolean {
    const type: string = this.selectedSedaNode.Type;

    return type === DateFormatType.date || type === DateFormatType.dateTime;
  }

  onDeleteNode(nodeId: number) {
    const nodeToDelete = this.fileService.getFileNodeById(this.fileService.nodeChange.getValue(), nodeId);

    this.removeNode.emit(nodeToDelete);
  }

  onButtonClicked(elementId: number) {
    this.hoveredElementId = elementId;
  }

  isButtonClicked(elementId: number, data: MetadataHeaders): boolean {
    if (!data) return false;

    this.hoveredElementId = elementId;
    this.buttonIsClicked = true;

    return data.id === this.hoveredElementId;
  }

  isRowHovered(elementId: number): boolean {
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
    if (!this.selectedSedaNode) return false;

    const nameToSearch = elementName ? elementName : this.sedaService.selectedSedaNode.getValue().Name;
    const nodeElementType = this.sedaService.checkSedaElementType(nameToSearch, this.selectedSedaNode);

    return nodeElementType === SedaElement.COMPLEXE;
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
      return node.Children.find((c) => c.Element === SedaElement.ATTRIBUTE) !== undefined;
    }

    return false;
  }

  isDeltable(name: string): boolean {
    const node = this.fileService.getFileNodeByName(this.clickedNode, name);

    return (
      (node.parent.children.filter((child) => child.name === name).length > 1 &&
        this.sedaService.isSedaNodeObligatory(name, this.selectedSedaNode)) ||
      !this.sedaService.isSedaNodeObligatory(name, this.selectedSedaNode)
    );
  }

  getSedaDefinition(elementName: string) {
    const node = this.getSedaNode(elementName);

    if (!node) return '';

    return node.Definition;
  }

  getSedaNode(elementName: string): SedaData {
    if (this.selectedSedaNode.Name === elementName) return this.selectedSedaNode;

    return this.selectedSedaNode.Children.find((child) => child.Name === elementName);
  }

  onResolveName(elementName: string) {
    if (this.sedaLanguage) return elementName;

    const node = this.getSedaNode(elementName);

    if (!node) return elementName;
    if (node.NameFr) return node.NameFr;
    if (node.Name) return node.Name;

    return elementName;
  }

  resolveButtonLabel(node: FileNode): string {
    if (!node) return null;

    return node.name === 'DescriptiveMetadata' ? null : this.boutonAjoutMetadonnee;
  }

  resolveCurrentNodeName(): string {
    if (!this.clickedControl) return null;

    return this.clickedNode.name;
  }

  goBack() {
    this.router.navigate(['/'], { skipLocationChange: false });
  }

  onChangeSelected(element: any, value: any) {
    if (value === undefined) {
      this.setOrigineNodeValue(element);
    } else {
      this.setNodeValue(element, value);
    }
  }

  private setOrigineNodeValue(metadata: any) {
    if (this.clickedNode.name === metadata.nomDuChamp) {
      this.clickedNode.value = null;
    } else if (this.clickedNode.children.length > 0) {
      const childNode = this.fileService.getFileNodeById(this.clickedNode, metadata.id);
      if (childNode) {
        childNode.value = null;
      }
    }
  }

  toggleLanguagePopup() {
    this.languagePopup = !this.languagePopup;
  }

  isDuplicated(nomDuChamp: any) {
    return this.sedaService.isDuplicated(nomDuChamp, this.selectedSedaNode);
  }

  isElementEdit(node: MetadataHeaders): boolean {
    if (this.profileService.isArchiveUnitProfileMode()) return false;
    if (!node.nomDuChampEdit) return false;

    return true;
  }

  isEmptyEnumeration(enumerations: string[]): boolean {
    return enumerations ? enumerations.length === 0 : false;
  }

  setPatternExpressionReguliere() {
    this.clickedControl.puaData = {
      pattern: this.radioExpressionReguliere === 'select' ? this.regex : this.customRegex,
    };
  }

  onDeleteControls() {
    if (this.clickedControl) {
      this.clickedControl.puaData.enum = null;
      this.clickedControl.sedaData.Enumeration = [];
    }
    if (this.expressionControl) {
      this.clickedControl.puaData.pattern = null;
    }
    this.resetControls();
    this.updateDataSource();
  }

  onSubmitControls() {
    if (this.enumerationControl) {
      if (this.clickedControl.puaData) {
        this.clickedControl.puaData.enum = this.enumsControlSeleted;
      } else {
        this.clickedControl.puaData = {
          enum: this.enumsControlSeleted,
        };
      }
    }

    if (this.expressionControl) {
      this.setPatternExpressionReguliere();
    }

    this.resetControls();
    this.updateDataSource();
  }

  onRemoveEnumsControl(element: string) {
    let indexOfElement = this.enumsControlSeleted.indexOf(element);

    if (indexOfElement >= 0) {
      this.enumsControlSeleted.splice(indexOfElement, 1);
      this.editedEnumControl = [];
      this.enumsControlSeleted.forEach((e) => {
        this.editedEnumControl.push(e);
      });
    }

    if (this.editedEnumControl.includes(element)) {
      indexOfElement = this.editedEnumControl.indexOf(element);
      this.editedEnumControl.splice(indexOfElement, 1);
    }

    if (this.enumsControlSeleted.length === 0) {
      this.editedEnumControl = null;
    }
  }

  updateDataSource() {
    this.matDataSource.data = this.extendsMetadatHeadersWithFileNode(this.clickedControl)(this.matDataSource.data);
  }

  addEnumsControl(element: string) {
    this.enumsControlSeleted.push(element);
  }

  addEnumsControlList(elements: string[]) {
    this.enumsControlSeleted = elements;
  }

  closeControlsVue() {
    this.openControls = false;
    this.resetControls();
  }

  changeStatusAditionalProperties($event: boolean) {
    FileTreeComponent.archiveUnits.additionalProperties = $event;
    this.rootAdditionalProperties = FileTreeComponent.archiveUnits.additionalProperties;
  }

  isElementNameNotContentManagement(nomDuChamp: string) {
    return !(nomDuChamp === 'Content');
  }

  toggleAdditionalProperties($event: MatCheckboxChange, element: MetadataHeaders) {
    const additionalProperties = $event.checked;
    const { fileNode } = element;

    if (!fileNode) {
      console.warn(`No file node binded to the current element: ${element.nomDuChamp}`);

      return;
    }

    const currentElementIsClickedNode = fileNode.id === this.clickedNode.id;
    const clickedNodeHasChildren = this.clickedNode.children.length > 0;

    if (currentElementIsClickedNode) {
      this.clickedNode.puaData = { additionalProperties };
      this.clickedNode.additionalProperties = additionalProperties;
    } else if (clickedNodeHasChildren) {
      this.clickedNode.children = this.clickedNode.children.map((node) => {
        const hasSameId = node.id === element.id;
        const hasSameName = node.name === element.nomDuChamp;

        if (hasSameId && hasSameName) {
          return {
            ...node,
            puaData: {
              additionalProperties,
            },
          };
        }

        return node;
      });
    }
  }

  getNodeAdditionalProperties(element: MetadataHeaders): boolean {
    if (!element) return false;
    if (!element.fileNode) return false;
    if (!element.fileNode.puaData) return false;
    if (!element.fileNode.puaData.additionalProperties) return false;

    return element.fileNode.puaData.additionalProperties;
  }

  canEnableAdditionalPropertiesEdition(node: FileNode): boolean {
    if (!node) return false;
    if (!node.sedaData) return false;
    if (!this.isArchivalUnitProfileMode()) return false;

    return this.sedaService.isExtensible(node.sedaData);
  }

  isArchivalUnitProfileMode(): boolean {
    return this.profileService.isArchiveUnitProfileMode();
  }

  private extendMetadataHeaders(fileNode: FileNode, metadataHeaders: MetadataHeaders) {
    return {
      ...metadataHeaders,
      fileNode,
      canEnableAdditionalPropertiesEdition: this.canEnableAdditionalPropertiesEdition(fileNode),
      canEnableDeletion: this.sedaService.isDeletable(fileNode.sedaData),
      canEnableMetadataControl:
        this.isArchivalUnitProfileMode() &&
        !this.isElementComplex(metadataHeaders.nomDuChamp) &&
        metadataHeaders.id === this.clickedNode.id &&
        !this.isAppliedControl(metadataHeaders.id),
      canEnableEditionControl:
        this.isArchivalUnitProfileMode() &&
        !this.isElementComplex(metadataHeaders.nomDuChamp) &&
        metadataHeaders.id === this.clickedNode.id &&
        this.isAppliedControl(metadataHeaders.id),
    };
  }

  private extendsMetadatHeadersWithFileNode = (fileNode: FileNode) => (metadataHeadersList: MetadataHeaders[]) => {
    if (!metadataHeadersList) return [];
    if (!fileNode) return [];

    return metadataHeadersList.map((item: MetadataHeaders) => {
      const matchedChild = fileNode.children.find((child) => child.id === item.id);

      if (matchedChild) return this.extendMetadataHeaders(matchedChild, item);

      return this.extendMetadataHeaders(fileNode, item);
    });
  };
}
