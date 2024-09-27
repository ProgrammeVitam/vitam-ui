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
import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatLegacyTabChangeEvent as MatTabChangeEvent } from '@angular/material/legacy-tabs';
import { BehaviorSubject, Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FileService } from '../../core/services/file.service';
import { ProfileService } from '../../core/services/profile.service';
import { ToggleSidenavService } from '../../core/services/toggle-sidenav.service';
import { FileNode } from '../../models/file-node';
import { ProfileType } from '../../models/profile-type.enum';
import { FileTreeComponent } from './file-tree/file-tree.component';
import { FileTreeService } from './file-tree/file-tree.service';
import { Logger } from 'vitamui-library';
import { filter } from 'rxjs/operators';
import { BreadcrumbService } from '../../core/services/breadcrumb.service';

@Component({
  // eslint-disable-next-line @angular-eslint/component-selector
  selector: 'pastis-edit-profile',
  templateUrl: './edit-profile.component.html',
  styleUrls: ['./edit-profile.component.scss'],
})
export class EditProfileComponent implements OnInit, OnDestroy, AfterViewInit {
  sedaVersionLabel = this.profileService.getSedaVersionLabel();
  isAUP = this.profileService.isMode(ProfileType.PUA);
  selectedIndex = this.profileService.isMode(ProfileType.PA) ? 0 : 2;
  dataChange = new BehaviorSubject<FileNode[]>([]);
  isStandalone: boolean = environment.standalone;

  entete = 'Entête';
  regles = 'Règles';
  unitesArchives = "Unités d'archives";
  objets = 'Objets';
  unitesArchivesPuaMode = "Unité d'archive";

  profileTabChildrenToInclude: string[] = [];
  profileTabChildrenToExclude: string[] = [];
  headerTabChildrenToInclude: string[] = [];
  headerTabChildrenToExclude: string[] = [
    'DataObjectPackage',
    'DataObjectGroup',
    'DescriptiveMetadata',
    'ManagementMetadata',
    'id',
    'BinaryDataObject',
  ];
  rulesTabChildrenToInclude: string[] = [];
  rulesTabChildrenToExclude: string[] = [];
  treeTabChildrenToInclude: string[] = [];
  treeTabChildrenToExclude: string[] = ['ManagementMetadata', 'DescriptiveMetadata'];
  objectTabChildrenToInclude: string[] = ['BinaryDataObject', 'PhysicalDataObject'];
  objectTabChildrenToExclude: string[] = ['ManagementMetadata', 'ArchiveUnit', 'DescriptiveMetadata'];

  rootNames: string[] = ['ArchiveTransfer', 'ManagementMetadata', 'DescriptiveMetadata', 'DataObjectPackage'];
  tabLabels: string[] = [];
  collectionNames = ['Entête', 'Règles', "Unités d'archives", 'Objets'].map(
    (name) => name.charAt(0).toUpperCase() + name.slice(1).toLowerCase(),
  );
  tabShowElementRules: string[][][] = [];

  collectionName: string;
  rootTabMetadataName: string;
  elementRules: string[][] = [];

  profile: FileNode[];

  @ViewChild(FileTreeComponent, { static: false }) fileTreeComponent: FileTreeComponent;

  private _fileServiceCurrentTreeSubscription: Subscription;

  constructor(
    public profileService: ProfileService,
    public fileService: FileService,
    private sideNavService: ToggleSidenavService,
    private fileTreeService: FileTreeService,
    private breadcrumbService: BreadcrumbService,
    private logger: Logger,
  ) {}

  ngOnInit(): void {
    if (!this.isStandalone) {
      this.entete = 'PROFILE.EDIT_PROFILE.ENTETE';
      this.regles = 'PROFILE.EDIT_PROFILE.REGLES';
      this.unitesArchives = 'PROFILE.EDIT_PROFILE.UNITES_ARCHIVES';
      this.objets = 'PROFILE.EDIT_PROFILE.OBJETS';
      this.unitesArchivesPuaMode = 'PROFILE.EDIT_PROFILE.UNITES_ARCHIVES_PUA_MODE';
    }
    this.tabLabels.push(this.entete, this.regles, this.unitesArchives, this.objets, this.unitesArchivesPuaMode);
    this.tabShowElementRules.push(
      [this.headerTabChildrenToInclude, this.headerTabChildrenToExclude],
      [this.profileTabChildrenToInclude, this.profileTabChildrenToExclude],
      [this.rulesTabChildrenToInclude, this.rulesTabChildrenToExclude],
      [this.treeTabChildrenToInclude, this.treeTabChildrenToExclude],
      [this.objectTabChildrenToInclude, this.objectTabChildrenToExclude],
    );
  }

  ngAfterViewInit() {
    this.changeTab({ index: this.selectedIndex } as MatTabChangeEvent);

    this._fileServiceCurrentTreeSubscription = this.fileService.currentTree
      .pipe(filter((nodes) => nodes?.length > 0 && nodes.every((node) => Boolean(node))))
      .subscribe((data) => {
        const [tree] = data;
        const nodeName = this.profileService.isMode(ProfileType.PA) ? this.rootTabMetadataName : tree.name;
        const node = this.fileService.getTree(nodeName);
        this.dispatch(node);
        this.logger.log(this, 'Init file tree node on file tree :', this.dataChange.getValue());
      });
  }

  ngOnDestroy(): void {
    this._fileServiceCurrentTreeSubscription?.unsubscribe();
  }

  changeTab(event: MatTabChangeEvent) {
    this.selectedIndex = event.index;
    this.collectionName = this.collectionNames[this.selectedIndex];
    this.rootTabMetadataName = this.rootNames[this.selectedIndex];
    this.elementRules = this.tabShowElementRules[this.selectedIndex];

    const node = this.fileService.getTree(this.rootTabMetadataName);
    this.dispatch(node);
  }

  closeSideNav() {
    this.sideNavService.hide();
  }

  canShowOnPuaMode(tabIndex: number) {
    return this.profileService.profileType === ProfileType.PUA ? tabIndex === 3 : true;
  }

  private dispatch(node: FileNode) {
    this.fileTreeService.setNestedDataSourceData([node]);
    this.fileTreeService.nestedTreeControl.expand(node);
    this.dataChange.next([node]);
    this.fileService.filteredNode.next(node);
    this.breadcrumbService.setRoot(node);
    this.fileService.nodeChange.next(node);

    if (this.fileTreeComponent) this.fileTreeComponent.updateMetadataTable(node);
  }
}
