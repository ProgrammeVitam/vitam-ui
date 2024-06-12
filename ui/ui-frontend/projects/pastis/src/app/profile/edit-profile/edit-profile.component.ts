import { AfterViewInit, Component, OnDestroy, ViewChild } from '@angular/core';
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
import { NestedTreeControl } from '@angular/cdk/tree';
import { MatLegacyTabChangeEvent as MatTabChangeEvent } from '@angular/material/legacy-tabs';
import { MatTreeNestedDataSource } from '@angular/material/tree';
import { TranslateService } from '@ngx-translate/core';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { BehaviorSubject, Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FileService } from '../../core/services/file.service';
import { ProfileService } from '../../core/services/profile.service';
import { SedaService } from '../../core/services/seda.service';
import { ToggleSidenavService } from '../../core/services/toggle-sidenav.service';
import { FileNode } from '../../models/file-node';
import { ProfileType } from '../../models/profile-type.enum';
import { SedaData } from '../../models/seda-data';
import { FileTreeComponent } from './file-tree/file-tree.component';
import { FileTreeService } from './file-tree/file-tree.service';

const EDIT_PROFILE_TRANSLATE_PATH = 'PROFILE.EDIT_PROFILE';

export interface UploadedProfileResponse {
  profile: FileNode[];
  id: number;
}

@Component({
  // tslint:disable-next-line:component-selector
  selector: 'pastis-edit-profile',
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.scss'],
})
export class EditProfileComponent implements OnDestroy, AfterViewInit {
  nodeToSend: FileNode;
  sedaParentNode: SedaData;
  selectedIndex: number;
  activeTabIndex: number;
  dataChange = new BehaviorSubject<FileNode[]>([]);
  isStandalone: boolean = environment.standalone;
  puaMode: boolean;

  entete = 'Entête';
  regles = 'Règles';
  unitesArchives = "Unités d'archives";
  objets = 'Objets';
  unitesArchivesPuaMode = "Unité d'archive";

  profileTabChildrenToInclude: string[] = [];
  profileTabChildrenToExclude: string[] = [];
  headerTabChildrenToInclude: string[] = [];
  headerTabChildrenToExclude: string[] = [];
  rulesTabChildrenToInclude: string[] = [];
  rulesTabChildrenToExclude: string[] = [];
  treeTabChildrenToInclude: string[] = [];
  treeTabChildrenToExclude: string[] = [];
  objectTabChildrenToInclude: string[] = [];
  objectTabChildrenToExclude: string[] = [];

  rootNames: string[] = [];
  tabLabels: string[] = [];
  collectionNames: string[] = [];
  tabShowElementRules: string[][][] = [];

  collectionName: string;
  rootTabMetadataName: string;
  elementRules: string[][] = [];

  profile: FileNode[];

  @ViewChild(FileTreeComponent, { static: false }) fileTreeComponent: FileTreeComponent;

  private _fileServiceCurrentTreeSubscription: Subscription;

  constructor(
    private sedaService: SedaService,
    private fileService: FileService,
    private sideNavService: ToggleSidenavService,
    public profileService: ProfileService,
    private loaderService: NgxUiLoaderService,
    private fileTreeService: FileTreeService,
    private translateService: TranslateService,
  ) {
    this.selectedIndex = 0;
  }

  initAll() {
    this.puaMode = this.profileService.profileMode !== ProfileType.PA;
    if (!this.isStandalone) {
      this.entete = 'PROFILE.EDIT_PROFILE.ENTETE';
      this.regles = 'PROFILE.EDIT_PROFILE.REGLES';
      this.unitesArchives = 'PROFILE.EDIT_PROFILE.UNITES_ARCHIVES';
      this.objets = 'PROFILE.EDIT_PROFILE.OBJETS';
      this.unitesArchivesPuaMode = 'PROFILE.EDIT_PROFILE.UNITES_ARCHIVES_PUA_MODE';
    }
    this.tabLabels.push(this.entete, this.regles, this.unitesArchives, this.objets, this.unitesArchivesPuaMode);

    const collectionSeda: string[] = [];
    collectionSeda.push('Entête', 'Règles', "Unités d'archives", 'Objets');
    this.fileTreeService.nestedTreeControl = new NestedTreeControl<FileNode>(this.getChildren);
    this.collectionNames = collectionSeda.map((name) => name.charAt(0).toUpperCase() + name.slice(1).toLowerCase());

    this.rootNames.push('ArchiveTransfer', 'ManagementMetadata', 'DescriptiveMetadata', 'DataObjectPackage');

    // Children to include or exclude
    this.profileTabChildrenToInclude.push();
    this.profileTabChildrenToExclude.push();
    this.headerTabChildrenToInclude.push();
    this.headerTabChildrenToExclude.push(
      'DataObjectPackage',
      'DataObjectGroup',
      'DescriptiveMetadata',
      'ManagementMetadata',
      'id',
      'BinaryDataObject',
    );
    this.rulesTabChildrenToInclude.push();
    this.rulesTabChildrenToExclude.push();
    this.treeTabChildrenToInclude.push();
    this.treeTabChildrenToExclude.push('ManagementMetadata', 'ArchiveUnit', 'DescriptiveMetadata');
    this.objectTabChildrenToInclude.push('BinaryDataObject', 'PhysicalDataObject');
    this.objectTabChildrenToExclude.push('ManagementMetadata', 'ArchiveUnit', 'DescriptiveMetadata');
    this.tabShowElementRules.push(
      [this.headerTabChildrenToInclude, this.headerTabChildrenToExclude],
      [this.profileTabChildrenToInclude, this.profileTabChildrenToExclude],
      [this.rulesTabChildrenToInclude, this.rulesTabChildrenToExclude],
      [this.treeTabChildrenToInclude, this.treeTabChildrenToExclude],
      [this.objectTabChildrenToInclude, this.objectTabChildrenToExclude],
    );
    this.initActiveTabAndProfileMode();
    this.setTabsAndMetadataRules(this.activeTabIndex);

    // Set initial rules
    this.fileService.setCollectionName(this.collectionName);
    this.fileService.setTabRootMetadataName(this.rootTabMetadataName);
    this.fileService.setNewChildrenRules(this.elementRules);
  }

  ngAfterViewInit() {
    this._fileServiceCurrentTreeSubscription = this.fileService.currentTree.subscribe((response) => {
      this.initAll();
      if (response) {
        this.nodeToSend = response[0];
        if (this.nodeToSend) {
          this.fileService.allData.next(response);
          const filteredData = this.getFilteredData(this.rootTabMetadataName);

          this.fileTreeService.nestedDataSource = new MatTreeNestedDataSource();
          this.fileTreeService.nestedDataSource.data = filteredData;
          this.fileTreeService.nestedTreeControl.dataNodes = filteredData;
          this.fileTreeService.nestedTreeControl.expand(filteredData[0]);
          this.dataChange.next(filteredData);
          this.fileService.filteredNode.next(filteredData[0]);
        }
      }
      this.loadProfileData(this.activeTabIndex);
      console.log('Init file tree node on file tree : %o', this.dataChange.getValue());
    });

    this.sedaParentNode = this.sedaService.sedaRules[0];
  }

  translated(nameOfFieldToTranslate: string): string {
    return this.translateService.instant(EDIT_PROFILE_TRANSLATE_PATH + nameOfFieldToTranslate);
  }

  initActiveTabAndProfileMode() {
    this.profileService.profileMode === ProfileType.PA ? (this.activeTabIndex = 0) : (this.activeTabIndex = 2);
  }

  loadProfile(event: MatTabChangeEvent) {
    this.selectedIndex = event.index;
    this.loadProfileData(event.index);
  }

  setTabsAndMetadataRules(tabIndex: number) {
    this.collectionName = this.profileService.profileMode === ProfileType.PA ? this.collectionNames[tabIndex] : this.collectionNames[2];
    this.rootTabMetadataName = this.profileService.profileMode === ProfileType.PA ? this.rootNames[tabIndex] : this.rootNames[2];
    this.elementRules =
      this.profileService.profileMode === ProfileType.PA ? this.tabShowElementRules[tabIndex] : this.tabShowElementRules[2];
  }

  loadProfileData(tabindex: number) {
    this.setTabsAndMetadataRules(tabindex);
    this.fileService.collectionName.next(this.collectionName);
    this.fileService.rootTabMetadataName.next(this.rootTabMetadataName);
    this.fileService.tabChildrenRulesChange.next(this.elementRules);
    const fiteredData = this.getFilteredData(this.rootTabMetadataName);
    if (fiteredData) {
      this.fileService.tabRootNode.next(fiteredData[0]);
      this.loaderService.start();
      this.fileService.nodeChange.next(fiteredData[0]);
      this.fileTreeService.nestedDataSource.data = fiteredData;
      this.fileTreeService.nestedTreeControl.dataNodes = fiteredData;
      this.fileTreeService.nestedTreeControl.expand(fiteredData[0]);
      this.fileTreeComponent.sendNodeMetadata(fiteredData[0]);
    }

    this.loaderService.stop();
  }

  getFilteredData(rootTreeMetadataName: string): FileNode[] {
    if (this.nodeToSend) {
      const nodeNameToFilter = this.profileService.profileMode === ProfileType.PA ? rootTreeMetadataName : this.nodeToSend.name;
      const currentNode = this.fileService.getFileNodeByName(this.fileService.allData.getValue()[0], nodeNameToFilter);
      const filteredData = [];
      filteredData.push(currentNode);
      console.log('Filtered data : ', filteredData);
      return filteredData;
    }
  }

  getChildren = (node: FileNode) => node.children;

  closeSideNav() {
    this.sideNavService.hide();
  }

  canShowOnPuaMode(tabIndex: number) {
    return this.profileService.profileMode === ProfileType.PUA ? tabIndex === 3 : true;
  }

  ngOnDestroy() {
    if (this._fileServiceCurrentTreeSubscription != null) {
      this._fileServiceCurrentTreeSubscription.unsubscribe();
    }
  }
}
