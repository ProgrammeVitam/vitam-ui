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
import { MatLegacyTableDataSource as MatTableDataSource } from '@angular/material/legacy-table';
import { Router } from '@angular/router';
import { LangChangeEvent, TranslateService } from '@ngx-translate/core';
import { environment } from 'projects/pastis/src/environments/environment';
import { mergeMap, Subscription } from 'rxjs';
import { StartupService } from 'vitamui-library';
import { FileService } from '../../../core/services/file.service';
import { NotificationService } from '../../../core/services/notification.service';
import { ProfileService } from '../../../core/services/profile.service';
import { SedaService } from '../../../core/services/seda.service';
import { BreadcrumbDataMetadata, BreadcrumbDataTop } from '../../../models/breadcrumb';
import { AttributeData } from '../../../models/edit-attribute-models';
import {
  DataTypeConstants,
  DateFormatType,
  FileNode,
  FileNodeInsertAttributeParams,
  FileNodeInsertParams,
  TypeConstants,
} from '../../../models/file-node';
import { MetadataHeaders } from '../../../models/models';
import { ProfileType } from '../../../models/profile-type.enum';
import { PuaData } from '../../../models/pua-data';
import { SedaData, SedaElementConstants } from '../../../models/seda-data';
import { PastisDialogData } from '../../../shared/pastis-dialog/classes/pastis-dialog-data';
import { PastisPopupMetadataLanguageService } from '../../../shared/pastis-popup-metadata-language/pastis-popup-metadata-language.service';
import { UserActionAddPuaControlComponent } from '../../../user-actions/add-pua-control/add-pua-control.component';
import { FileTreeComponent } from '../file-tree/file-tree.component';
import { FileTreeService } from '../file-tree/file-tree.service';
import { AttributesPopupComponent } from './attributes/attributes.component';
import { FileTreeMetadataService } from './file-tree-metadata.service';
import { filter, map, tap } from 'rxjs/operators';
import { DatePatternConstants, Logger } from 'vitamui-library';
import { BreadcrumbService } from '../../../core/services/breadcrumb.service';

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
  this.popupControlTitleDialog = this.translated('.POPUP_CONTROL_TITLE_DIALOG');
}

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-file-tree-metadata',
  templateUrl: './file-tree-metadata.component.html',
  styleUrls: ['./file-tree-metadata.component.scss'],
  // Encapsulation has to be disabled in order for the
  // component style to apply to the select panel.
  encapsulation: ViewEncapsulation.None,
})
export class FileTreeMetadataComponent implements OnInit, OnDestroy {
  @ViewChild('autosize', { static: false }) autosize: CdkTextareaAutosize;

  sedaVersionLabel: string;
  rootAdditionalProperties: boolean;
  dataType = Object.values(DataTypeConstants);
  selected = -1;
  matDataSource: MatTableDataSource<MetadataHeaders>;
  displayedColumns: string[] = ['nomDuChamp', 'valeurFixe', 'cardinalite', 'commentaire', 'menuoption'];
  clickedNode: FileNode = {} as FileNode;
  // The seda node that has been opened from the left menu
  selectedSedaNode: SedaData;
  selectedCardinalities: string[];
  hoveredElementId: number;
  buttonIsClicked: boolean;
  isStandalone: boolean = environment.standalone;
  enumerationControl: boolean;
  valueControl: boolean;
  lengthControl: boolean;
  expressionControl: boolean;
  arrayControl: string[];
  clickedControl: FileNode;
  enumerationsSedaControl: string[];
  enumsControlSelected: string[] = [];
  editedEnumControl: string[];
  openControls: boolean;
  radioExpressionReguliere: 'select' | 'input';
  regex: string;
  customRegex: string;
  private formatagePredefini: Array<{ label: string; value: string }> = [
    { label: 'AAAA-MM-JJ', value: DatePatternConstants.YEAR_MONTH_DAY },
    { label: 'AAAA-MM-JJTHH:MM:SS', value: DatePatternConstants.FULL_DATE },
    { label: 'AAAA', value: DatePatternConstants.YEAR },
    { label: 'AAAA-MM', value: DatePatternConstants.YEAR_MONTH },
  ];
  availableRegex: Array<{ label: string; value: string }>;
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
  popupControlTitleDialog: string;
  popupControlSubTitleDialog: string;
  popupControlOkLabel: string;

  sedaLanguage: boolean;
  languagePopup: boolean;
  id: number;
  nomDuChamp: string;
  type: string;
  valeurFixe: string;
  cardinalite: string[];
  commentaire: string;
  enumeration: string[];
  additionalPropertiesMetadonnee: boolean;
  buttonClickedId?: number;

  @Output() insertItem: EventEmitter<FileNodeInsertParams> = new EventEmitter<FileNodeInsertParams>();
  @Output() addNode: EventEmitter<FileNode> = new EventEmitter<FileNode>();
  @Output() insertAttributes: EventEmitter<FileNodeInsertAttributeParams> = new EventEmitter<FileNodeInsertAttributeParams>();
  @Output() removeNode: EventEmitter<FileNode> = new EventEmitter<FileNode>();
  @Output() duplicateNode: EventEmitter<FileNode> = new EventEmitter<FileNode>();

  private _profileServiceProfileModeSubscription: Subscription;
  private _fileServiceSubscription: Subscription;
  private _fileMetadataServiceSubscriptionSelectedCardinalities: Subscription;
  private _fileServiceSubscriptionNodeChange: Subscription;
  private _sedaServiceSubscriptionSelectedSedaNode: Subscription;
  private _fileMetadataServiceSubscriptionDataSource: Subscription;
  private _sedalanguageSub: Subscription;

  constructor(
    public profileService: ProfileService,
    private fileService: FileService,
    private fileMetadataService: FileTreeMetadataService,
    private sedaService: SedaService,
    private notificationService: NotificationService,
    private router: Router,
    private startupService: StartupService,
    private fileTreeService: FileTreeService,
    private metadataLanguageService: PastisPopupMetadataLanguageService,
    private translateService: TranslateService,
    private logger: Logger,
    private breadcrumbService: BreadcrumbService,
  ) {
    this.config = {
      locale: 'fr',
      showGoToCurrent: false,
      firstDayOfWeek: 'mo',
      format: 'YYYY-MM-DD',
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
      this.popupControlTitleDialog = 'Veuillez séléctionner un ou plusieurs contrôles';
      this.popupControlSubTitleDialog = 'Ajouter des contrôles supplémentaires à';
      this.popupControlOkLabel = 'AJOUTER LES CONTROLES';
    }

    this.additionalPropertiesMetadonnee = false;
    this.languagePopup = false;
    this._sedalanguageSub = this.metadataLanguageService.sedaLanguage.subscribe(
      (value: boolean) => {
        this.sedaLanguage = value;
      },
      (error) => {
        console.error(error);
      },
    );
    this._fileServiceSubscriptionNodeChange = this.fileService.nodeChange
      .pipe(
        filter((node) => Boolean(node)),
        tap((node) => (this.clickedNode = node)),
        mergeMap((node) =>
          this.breadcrumbService.root$.pipe(
            filter((root: FileNode) => Boolean(root)),
            map((root: FileNode) => ({ node, root })),
          ),
        ),
      )
      .subscribe(({ node, root }) => {
        this.breadcrumbDataMetadata = this.breadcrumbService.computeBreadcrumb({ node, root });
      });
    this.sedaVersionLabel = this.profileService.getSedaVersionLabel();
    // BreadCrump Top for navigation
    this.profileModeLabel =
      this.profileService.profileType === ProfileType.PUA
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

    this._fileServiceSubscription = this.fileService.currentTree
      .pipe(tap((_node) => (this.sedaVersionLabel = this.profileService.getSedaVersionLabel())))
      .subscribe((fileTree) => {
        if (fileTree) {
          this.clickedNode = fileTree[0];
          // Subscription to sedaRules
          if (this.clickedNode) {
            const rulesFromService = this.fileService.tabChildrenRulesChange.getValue();
            const tabChildrenToInclude = rulesFromService[0];
            const tabChildrenToExclude = rulesFromService[1];
            this.sedaService.sedaRules$.subscribe((value) => {
              this.sedaService.selectedSedaNode.next(value);
              this.selectedSedaNode = value;
            });
            this.fileService.nodeChange.next(this.clickedNode);
            const filteredData = this.fileService.filteredNode.getValue();
            // Initial data for metadata table based on rules defined by tabChildrenRulesChange
            if (filteredData) {
              const dataTable = this.fileMetadataService.fillDataTable(
                this.selectedSedaNode,
                filteredData,
                tabChildrenToInclude,
                tabChildrenToExclude,
              );
              this.matDataSource = new MatTableDataSource<MetadataHeaders>(dataTable);
            }
          }
        }
      });

    this._fileMetadataServiceSubscriptionSelectedCardinalities = this.fileMetadataService.selectedCardinalities.subscribe((cards) => {
      this.selectedCardinalities = cards;
    });

    // Get Current sedaNode
    this._sedaServiceSubscriptionSelectedSedaNode = this.sedaService.selectedSedaNode.subscribe((sedaNode) => {
      this.selectedSedaNode = sedaNode;
    });

    this._fileMetadataServiceSubscriptionDataSource = this.fileMetadataService.dataSource.subscribe((data) => {
      this.matDataSource = new MatTableDataSource<MetadataHeaders>(data);
    });

    if (this.clickedNode) {
      this.rootAdditionalProperties = this.clickedNode.additionalProperties;
    }
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
    if (this._sedaServiceSubscriptionSelectedSedaNode != null) {
      this._sedaServiceSubscriptionSelectedSedaNode.unsubscribe();
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

  navigate(d: BreadcrumbDataTop) {
    if (d.external) {
      window.location.assign(d.url);
    } else {
      this.router.navigate([d.url], { skipLocationChange: false });
    }
  }

  navigateMetadata(d: BreadcrumbDataMetadata) {
    if (d.node) {
      this.fileTreeService.updateMetadataTable.next(d.node);
    }
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
    if (filterValue == null) {
      filterValue = '';
    }
    this.matDataSource.filter = filterValue.trim().toLowerCase();
  }

  translatedOnChange(): void {
    this.translateService.onLangChange.subscribe((event: LangChangeEvent) => {
      constantToTranslate.call(this);
      this.logger.log(this, event.lang);
    });
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(FILE_TREE_METADATA_TRANSLATE_PATH + nameOfFieldToTranslate);
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
      this.clickedNode.documentation = comment || null;
    } else {
      for (const node of this.clickedNode.children) {
        if (node.name === metadata.nomDuChamp && node.id === metadata.id) {
          node.documentation = comment || null;
        }
      }
    }
  }

  isElementComplex(elementName: string) {
    const childFound = this.selectedSedaNode.children.find((el) => el.name === elementName);
    if (childFound) {
      return childFound.element === SedaElementConstants.COMPLEX;
    }
  }

  onAddNode() {
    if (this.clickedNode.name === 'DescriptiveMetadata') {
      // eslint-disable-next-line prefer-const
      let elements: SedaData[];
      elements.push({
        name: 'ArchiveUnit',
        nameFr: null,
        type: null,
        element: null,
        cardinality: null,
        definition: null,
        extensible: null,
        choice: null,
        children: null,
        enumeration: null,
        collection: null,
      });
      const params: FileNodeInsertParams = {
        node: this.clickedNode,
        elementsToAdd: elements,
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

  onEditAttributesClick(fileNodeId: number): void {
    if (!fileNodeId) return;

    const editAttributeDialogData = {} as PastisDialogData;
    const attributeFileNodeListToAdd: FileNode[] = [];
    const attributeFileNodeListToRemove: FileNode[] = [];

    editAttributeDialogData.fileNode = this.fileService.findChildById(fileNodeId, this.clickedNode);
    editAttributeDialogData.subTitleDialog = this.popupSousTitre;
    editAttributeDialogData.titleDialog = editAttributeDialogData.fileNode.name;
    editAttributeDialogData.width = '1120px';
    editAttributeDialogData.component = AttributesPopupComponent;
    editAttributeDialogData.okLabel = this.popupValider;
    editAttributeDialogData.cancelLabel = this.popupAnnuler;

    this.fileService.openDialog(editAttributeDialogData).subscribe((attributes: AttributeData[]) => {
      // Create a list of attributes to add
      attributes
        .filter((a) => a.selected)
        .forEach((attr) => {
          const fileNode = {} as FileNode;
          fileNode.cardinality = attr.selected ? '1' : null;
          fileNode.value = attr.valeurFixe ? attr.valeurFixe : null;
          fileNode.documentation = attr.commentaire ? attr.commentaire : null;
          fileNode.name = attr.nomDuChamp;
          fileNode.type = TypeConstants.ATTRIBUTE;
          fileNode.sedaData = this.sedaService.findSedaChildByName(attr.nomDuChamp, editAttributeDialogData.fileNode.sedaData);
          fileNode.children = [];
          fileNode.id = attr.id;
          attributeFileNodeListToAdd.push(fileNode);
        });
      // Create a list of attributes to remove
      attributes
        .filter((a) => !a.selected)
        .forEach((attr) => {
          const fileNode: FileNode = {} as FileNode;
          fileNode.name = attr.nomDuChamp;
          attributeFileNodeListToRemove.push(fileNode);
        });
      if (attributeFileNodeListToAdd) {
        const insertOrEditParams: FileNodeInsertAttributeParams = {
          node: editAttributeDialogData.fileNode,
          elementsToAdd: attributeFileNodeListToAdd,
        };
        const attrsToAdd = attributeFileNodeListToAdd.map((e) => e.name);
        const attributeExists = editAttributeDialogData.fileNode.children.some((child: { name: string }) =>
          attrsToAdd.includes(child.name),
        );

        // Add attribute (if it does not exist), or update them if they do
        if (attrsToAdd && !attributeExists) {
          this.insertAttributes.emit(insertOrEditParams);
        } else {
          this.fileService.updateNodeChildren(editAttributeDialogData.fileNode, attributeFileNodeListToAdd);
        }
      }
      if (attributeFileNodeListToRemove.length) {
        this.fileService.removeItem(attributeFileNodeListToRemove, editAttributeDialogData.fileNode);
      }
    });
  }

  async onControlClick(fileNodeId: number) {
    const controlDialogData = {} as PastisDialogData;
    if (fileNodeId && fileNodeId === this.clickedNode.id) {
      this.resetControls();
      controlDialogData.fileNode = this.fileService.findChildById(fileNodeId, this.clickedNode);
      controlDialogData.titleDialog = this.popupControlTitleDialog;
      controlDialogData.subTitleDialog = this.popupControlSubTitleDialog + ' "' + controlDialogData.fileNode.name + '"';
      this.clickedControl = controlDialogData.fileNode;
      controlDialogData.width = '800px';
      controlDialogData.component = UserActionAddPuaControlComponent;
      controlDialogData.okLabel = this.popupControlOkLabel;
      controlDialogData.cancelLabel = this.popupAnnuler;

      this.fileService.openDialog(controlDialogData).subscribe((arrayControl: string[]) => {
        this.logger.log(this, 'The answer for arrays control was ', arrayControl);
        this.arrayControl = arrayControl;
        this.setControlsVues(this.arrayControl, controlDialogData.fileNode);
        this.openControls = true;
      });
    }
  }

  onEditControlClick(fileNodeId: number) {
    this.resetControls();
    const fileNode = this.fileService.findChildById(fileNodeId, this.clickedNode);
    this.clickedControl = fileNode;
    if (fileNode.puaData && fileNode.puaData.enum) {
      this.enumerationsSedaControl = this.selectedSedaNode.enumeration;
      this.enumerationControl = true;
      this.editedEnumControl = [];
      this.enumsControlSelected = [];
      this.openControls = true;
      fileNode.puaData.enum.forEach((e) => {
        this.editedEnumControl.push(e);
        this.enumsControlSelected.push(e);
      });
    }
    if (fileNode.puaData && fileNode.puaData.pattern) {
      const actualPattern = fileNode.puaData.pattern;
      this.openControls = true;
      this.expressionControl = true;
      this.commentaire = fileNode.documentation;
      this.setAvailableRegex(this.selectedSedaNode.type);
      if (this.availableRegex.map((e) => e.value).includes(actualPattern)) {
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
    if (fileNode.puaData && fileNode.puaData.enum) {
      return true;
    }
    return !!(fileNode.puaData && fileNode.puaData.pattern);
  }

  hasCustomRegex(element: MetadataHeaders): boolean {
    const fileNode = this.fileService.getFileNodeById(this.fileService.nodeChange.getValue(), element.id);
    const actualPattern = fileNode?.puaData?.pattern;
    return (
      actualPattern &&
      !this.getAvailableRegex(fileNode?.sedaData?.Type)
        .map((e) => e.value)
        .includes(actualPattern)
    );
  }

  resetControls() {
    this.arrayControl = [];
    this.enumerationControl = false;
    this.expressionControl = false;
    this.lengthControl = false;
    this.valueControl = false;
    this.enumsControlSelected = [];
    this.editedEnumControl = [];
    this.openControls = false;
    this.regex = undefined;
    this.customRegex = undefined;
    this.enumerationsSedaControl = [];
  }

  private setAvailableRegex(type: string) {
    this.availableRegex = this.getAvailableRegex(type);
  }

  private getAvailableRegex(type: string): Array<{ label: string; value: string }> {
    switch (type) {
      case DateFormatType.date:
        return this.formatagePredefini.filter((e) => e.label === 'AAAA-MM-JJ');
      case DateFormatType.dateTime:
        return this.formatagePredefini.filter((e) => e.label === 'AAAA-MM-JJTHH:MM:SS');
      case DateFormatType.dateType:
        return this.formatagePredefini;
      default:
        return this.formatagePredefini.filter((e) => e.label === 'AAAA-MM-JJ' || e.label === 'AAAA');
    }
  }

  isDataType(): boolean {
    const type: string = this.selectedSedaNode.type;
    return type === DateFormatType.date || type === DateFormatType.dateTime || type === DateFormatType.dateType;
  }

  private setControlsVues(elements: string[], fileNode: FileNode) {
    const sedaName = fileNode.name;
    if (elements.includes('Enumération') || elements.includes(this.translated(ADD_PUA_CONTROL_TRANSLATE_PATH + '.ENUMERATIONS_LABEL'))) {
      this.enumerationControl = true;

      this.enumerationsSedaControl = this.sedaService.findSedaChildByName(sedaName, this.selectedSedaNode).enumeration;
      this.editedEnumControl = this.enumerationsSedaControl;
      this.enumsControlSelected = this.enumerationsSedaControl;
      const type: string = this.sedaService.findSedaChildByName(sedaName, this.selectedSedaNode).type;
      this.setAvailableRegex(type);
    }
    if (
      elements.includes('Expression régulière') ||
      elements.includes(this.translated(ADD_PUA_CONTROL_TRANSLATE_PATH + '.EXPRESSION_REGULIERE_LABEL'))
    ) {
      this.radioExpressionReguliere = 'select';
      this.expressionControl = true;
      this.customRegex = '';
      this.commentaire = fileNode.documentation;
      const type: string = this.sedaService.findSedaChildByName(sedaName, this.selectedSedaNode).type;
      this.setAvailableRegex(type);
      this.regex = this.formatagePredefini[0].value;
    }
    if (
      (this.isStandalone && elements.includes('Longueur Min/Max')) ||
      elements.includes(this.translated(ADD_PUA_CONTROL_TRANSLATE_PATH + '.LENGTH_MIN_MAX_LABEL'))
    ) {
      this.lengthControl = true;
    }
    if (
      (this.isStandalone && elements.includes('Valeur Min/Max')) ||
      elements.includes(this.translated(ADD_PUA_CONTROL_TRANSLATE_PATH + '.VALUE_MIN_MAX_LABEL'))
    ) {
      this.valueControl = true;
    }
  }

  isNotRegexCustomisable(): boolean {
    const type: string = this.selectedSedaNode.type;
    return type === DateFormatType.date || type === DateFormatType.dateTime;
  }

  onDeleteNode(nodeId: number) {
    const nodeToDelete = this.fileService.getFileNodeById(this.fileService.nodeChange.getValue(), nodeId);
    this.removeNode.emit(nodeToDelete);
  }

  onButtonClicked(elementId: number) {
    this.hoveredElementId = elementId;
    this.buttonClickedId = elementId;
  }

  isButtonClicked(elementId: number) {
    return this.buttonClickedId === elementId;
  }

  isRowHovered(elementId: number) {
    return this.hoveredElementId === elementId || this.buttonClickedId === elementId;
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
      const nameToSearch = elementName ? elementName : this.sedaService.selectedSedaNode.getValue().name;
      const nodeElementType = this.sedaService.checkSedaElementType(nameToSearch, this.selectedSedaNode);
      return nodeElementType === SedaElementConstants.COMPLEX;
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

    if (node && node.children.length > 0) {
      return node.children.find((c) => c.element === SedaElementConstants.ATTRIBUTE) !== undefined;
    }
    return false;
  }

  isDeletable(name: string): boolean {
    return !this.sedaService.isMandatory(name);
  }

  getSedaDefinition(elementName: string) {
    const node = this.getSedaNode(elementName);
    if (node != null) {
      return node.definition;
    }
    return '';
  }

  getSedaNode(elementName: string): SedaData {
    if (this.selectedSedaNode.name === elementName) {
      return this.selectedSedaNode;
    } else {
      for (const node of this.selectedSedaNode.children) {
        if (node.name === elementName) {
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
      if (node.nameFr) {
        return node.nameFr;
      }
      return node.name;
    }
    return elementName;
  }

  resolveButtonLabel(node: FileNode) {
    if (node) {
      return node.name === 'DescriptiveMetadata' ? null : this.boutonAjoutMetadonnee;
    }
  }

  onChangeSelected(element: any, value: any) {
    if (value === undefined) {
      this.setOrigineNodeValue(element, value);
    } else {
      this.logger.log(this, value + ' Valeur On Change Selected');
      this.setNodeValue(element, value);
    }
  }

  private setOrigineNodeValue(metadata: any, newValue: any) {
    this.logger.log(this, metadata.cardinalite + 'new Value ' + newValue);
    if (this.clickedNode.name === metadata.nomDuChamp) {
      this.clickedNode.value = null;
    } else if (this.clickedNode.children.length > 0) {
      const childNode = this.fileService.getFileNodeById(this.clickedNode, metadata.id);
      if (childNode) {
        childNode.value = null;
      }
    }
  }

  changeSedaLanguage() {
    this.metadataLanguageService.sedaLanguage.subscribe(
      (value: boolean) => {
        this.sedaLanguage = value;
      },
      (error) => {
        console.error(error);
      },
    );
  }

  openChoicePopup() {
    this.languagePopup = !this.languagePopup;
  }

  isDuplicated(nomDuChamp: any) {
    return this.sedaService.isDuplicated(nomDuChamp, this.selectedSedaNode);
  }

  isElementEdit(node: MetadataHeaders): boolean {
    if (this.profileService.profileType === ProfileType.PUA) {
      return false;
    }
    return !!node.nomDuChampEdit;
  }

  isEmptyEnumeration(enumerations: string[]): boolean {
    return enumerations ? enumerations.length === 0 : false;
  }

  private setPatternRegex() {
    if (!this.clickedControl.puaData) {
      this.clickedControl.puaData = {} as PuaData;
    }

    this.clickedControl.puaData.pattern = this.radioExpressionReguliere === 'select' ? this.regex : this.customRegex;
    if (this.radioExpressionReguliere === 'input') {
      this.clickedControl.documentation = this.commentaire;
      const item = this.matDataSource.data.find((d) => d.id === this.clickedControl.id);
      if (item) item.commentaire = this.commentaire; // Force la mise à jour du commentaire dans le matTable
    }
  }

  onDeleteControls() {
    if (this.clickedControl) {
      this.clickedControl.puaData.enum = null;
      this.clickedControl.sedaData.enumeration = [];
    }
    if (this.expressionControl) {
      this.clickedControl.puaData.pattern = null;
    }
    this.resetControls();
  }

  onSubmitControls() {
    if (this.enumerationControl) {
      if (this.clickedControl.puaData) {
        this.clickedControl.puaData.enum = this.enumsControlSelected;
      } else {
        this.clickedControl.puaData = {
          enum: this.enumsControlSelected,
        };
      }
    }
    if (this.expressionControl) {
      this.setPatternRegex();
    }
    this.resetControls();
  }

  onRemoveEnumsControl(element: string) {
    let indexOfElement = this.enumsControlSelected.indexOf(element);
    if (indexOfElement >= 0) {
      this.enumsControlSelected.splice(indexOfElement, 1);
      this.editedEnumControl = [];
      this.enumsControlSelected.forEach((e) => {
        this.editedEnumControl.push(e);
      });
    }

    if (this.editedEnumControl.includes(element)) {
      indexOfElement = this.editedEnumControl.indexOf(element);
      this.editedEnumControl.splice(indexOfElement, 1)[0];
    }
    if (this.enumsControlSelected.length === 0) {
      this.editedEnumControl = null;
    }
  }

  addEnumsControl(element: string) {
    this.enumsControlSelected.push(element);
  }

  closeControlsVue() {
    this.openControls = false;
    this.resetControls();
  }

  changeStatusAdditionalProperties($event: boolean) {
    FileTreeComponent.archiveUnits.additionalProperties = $event;
    this.rootAdditionalProperties = FileTreeComponent.archiveUnits.additionalProperties;
  }

  isElementNameNotContentManagement(nomDuChamp: string) {
    return !(nomDuChamp === 'Content');
  }

  toggleAutorisation(element: any) {
    this.additionalPropertiesMetadonnee = !this.getNodeAdditionalProperties(element);
    this.setNodeAdditionalPropertiesChange(this.additionalPropertiesMetadonnee, element);
  }

  private setNodeAdditionalPropertiesChange(additionalProperties: boolean, element: MetadataHeaders) {
    this.clickedNode.children = this.clickedNode.children.map((node) => {
      const hasSameId = node.id === element.id;
      const hasSameName = node.name === element.nomDuChamp;

      if (hasSameId && hasSameName) {
        return {
          ...node,
          puaData: {
            additionalProperties,
          },
          additionalProperties,
        };
      }

      return node;
    });
  }

  getNodeAdditionalProperties(element: MetadataHeaders): boolean {
    for (const node of this.clickedNode.children) {
      if (node.name === element.nomDuChamp && node.id === element.id && node.puaData) {
        return node.puaData.additionalProperties;
      }
    }
    return false;
  }
}
