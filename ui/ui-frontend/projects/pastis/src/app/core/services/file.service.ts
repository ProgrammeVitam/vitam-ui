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
import { Injectable, OnDestroy } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { BehaviorSubject, mergeMap, ReplaySubject, Subscription } from 'rxjs';
import { FileNode, TypeConstants } from '../../models/file-node';
import { Notice } from '../../models/notice.model';
import { ProfileDescription } from '../../models/profile-description.model';
import { ProfileResponse } from '../../models/profile-response';
import { SedaData, SedaElementConstants } from '../../models/seda-data';
import { FileTreeMetadataService } from '../../profile/edit-profile/file-tree-metadata/file-tree-metadata.service';
import { PastisDialogData } from '../../shared/pastis-dialog/classes/pastis-dialog-data';
import { PastisDialogConfirmComponent } from '../../shared/pastis-dialog/pastis-dialog-confirm/pastis-dialog-confirm.component';
import { ProfileService } from './profile.service';
import { SedaService } from './seda.service';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class FileService implements OnDestroy {
  currentTree = new ReplaySubject<FileNode[]>();
  notice = new BehaviorSubject<ProfileDescription>(null);
  noticeEditable = new BehaviorSubject<Notice>(null);
  nodeChange = new BehaviorSubject<FileNode>(null);
  allData = new BehaviorSubject<FileNode[]>([]);

  currentTreeLoaded = false;

  collectionName = new BehaviorSubject<string>(null);
  rootTabMetadataName = new BehaviorSubject<string>(null);
  tabRootNode = new BehaviorSubject<FileNode>(null);

  filteredNode = new BehaviorSubject<FileNode>(null);
  tabChildrenRulesChange = new BehaviorSubject<string[][]>([]);

  parentNodeMap = new Map<FileNode, FileNode>();
  sedaDataArchiveUnit: SedaData;

  private _profileServiceGetProfileSubscription: Subscription;

  constructor(
    private profileService: ProfileService,
    private fileMetadataService: FileTreeMetadataService,
    private dialog: MatDialog,
    private sedaService: SedaService,
  ) {}

  /**
   * Update the tree with the profile provided
   * @param profileResponse profileResponse sent from backend
   */
  updateTreeWithProfile(profileResponse: ProfileResponse) {
    this.profileService.profileType = profileResponse.type;
    this.profileService.profileVersion = profileResponse.sedaVersion;
    this.profileService.profileName = profileResponse.name;
    this.profileService.profileId = profileResponse.id;

    this.currentTree.next([profileResponse.profile]);
    this.currentTreeLoaded = true;
    if (profileResponse.notice) {
      this.notice.next(profileResponse.notice);
      this.setNotice(false);
    }
  }

  /**
   * Get profile from backend with id
   */
  getProfileAndUpdateTree(element: ProfileDescription) {
    this._profileServiceGetProfileSubscription = this.profileService
      .getProfile(element)
      .pipe(
        mergeMap((profile) =>
          this.profileService.getMetaModel(profile.sedaVersion).pipe(
            map((metaModel) => ({
              profile,
              metaModel,
            })),
          ),
        ),
      )
      .subscribe(({ profile, metaModel }) => {
        this.sedaService.setMetaModel(metaModel);
        this.linkFileNodeToSedaData(null, [profile.profile], [metaModel]);
        this.updateTreeWithProfile(profile);
      });
  }

  /**
   * Relie chaque FileNode a sa définition Seda
   *
   * Les nodes correspondant aux ArchivesUnit
   * se réfèrent à la définition SEDA de l'ArchiveUnit mère (ils sont récursifs...)
   */
  linkFileNodeToSedaData(parent: FileNode, nodes: FileNode[], sedaData: SedaData[]) {
    if (!sedaData) {
      sedaData = [];
    }
    Array.prototype.forEach.call(nodes, (node: FileNode) => {
      if (parent) {
        node.parent = parent;
      }
      const nodeName: string = node.name === 'xml:id' ? 'id' : node.name;
      if (nodeName) {
        let sedaDataMatch: SedaData;

        // Si le node en cours est une ArchiveUnit, lui même enfant d'une ArchiveUnit...
        if (nodeName === 'ArchiveUnit' && this.sedaDataArchiveUnit !== undefined) {
          // Alors on utilise la définition SEDA de l'ArchiveUnit mère..
          sedaDataMatch = this.sedaDataArchiveUnit;
        } else {
          // Sinon on recherche la définition SEDA dans l'arbre
          sedaDataMatch = sedaData.find((seda) => seda.name === nodeName);
          // sedaDataMatch = this.sedaService.getSedaNodeRecursively(sedaData[0],nodeName)
        }
        if (!sedaDataMatch) {
          // Sometimes,the sedaData has no property children, but many siblings (e.g. elements on the same level of the tree)
          // In this case, the recursivity must be used on each symbling since the service getSedaNodeRecursively
          // expects a tree root element with Children key
          for (const element of sedaData) {
            const resultRecursity = this.sedaService.getSedaNodeRecursively(element, nodeName);
            if (resultRecursity) {
              sedaDataMatch = resultRecursity;
              break;
            }
          }
        } else {
          // Si le node en cours est l'achive ArchiveUnit mère, on la sauvegarde pour l'utiliser pour les ArchivesUnit enfants
          if (sedaDataMatch.name === 'ArchiveUnit' && this.sedaDataArchiveUnit === undefined) {
            this.sedaDataArchiveUnit = sedaDataMatch;
            // On introduit la récursivité sur les ArchivesUnit
            this.sedaDataArchiveUnit.children.find((c: { name: string }) => c.name === 'ArchiveUnit').children =
              this.sedaDataArchiveUnit.children;
          }
        }
        node.sedaData = sedaDataMatch;
        if (node.children.length > 0) {
          this.linkFileNodeToSedaData(node, node.children, node.sedaData?.children);
        }
      }
    });
  }

  /**
   * Update the children of a node, based on given list of nodes
   */
  updateNodeChildren(parentNode: FileNode, newChildrenNodes: FileNode[]) {
    // eslint-disable-next-line guard-for-in
    for (const idx in parentNode.children) {
      const childFromNewChildren = newChildrenNodes.find((newChild) => newChild.id === parentNode.children[idx].id);
      if (childFromNewChildren) {
        parentNode.children[idx] = childFromNewChildren;
      }
    }
  }

  openPopup(popData: PastisDialogData) {
    const dialogConfirmRef = this.dialog.open(PastisDialogConfirmComponent, {
      width: popData.width,
      height: popData.height,
      data: popData,
      panelClass: 'pastis-popup-modal-box',
    });
    return new Promise((resolve, reject) => {
      dialogConfirmRef.afterClosed().subscribe((data) => {
        resolve(data);
        // console.log('The confirm dialog was closed with data : ', data);
      }, reject);
    });
  }

  findChildById(nodeId: number, node: FileNode): FileNode {
    if (nodeId === node.id) {
      return node;
    }
    for (const child of node.children) {
      if (child.id === nodeId) {
        return child;
      }
    }
  }

  setCollectionName(collectionName: string) {
    this.collectionName.next(collectionName);
  }

  setTabRootMetadataName(rootTabMetadataName: string) {
    this.rootTabMetadataName.next(rootTabMetadataName);
  }

  setNewChildrenRules(rules: string[][]) {
    this.tabChildrenRulesChange.next(rules);
  }

  /**
   * Delete all the attributes of the concerned FileNode
   * @param fileNode The concerned FileNode
   */
  deleteAllAttributes(fileNode: FileNode): void {
    fileNode.children = fileNode.children.filter((c) => c.type !== TypeConstants.attribute);
  }

  removeItem(nodesToBeDeleted: FileNode[], root: FileNode) {
    if (nodesToBeDeleted.length) {
      for (const node of nodesToBeDeleted) {
        const nodeToBeDeleted = this.getFileNodeByName(root, node.name);
        // Check if node exists in the file tree
        if (nodeToBeDeleted) {
          const parentNode = nodeToBeDeleted.parent;
          // console.log('On removeItem with node : ', nodeToBeDeleted, 'and parent : ', parentNode);
          const index = parentNode.children.indexOf(nodeToBeDeleted);
          if (index !== -1) {
            parentNode.children.splice(index, 1);
            this.parentNodeMap.delete(nodeToBeDeleted);
          }
          // console.log('Deleted node : ', nodeToBeDeleted, 'and his parent : ', parentNode);
        }
      }
    }
    // console.log('No nodes will be deleted');
  }

  getFileNodeByName(fileTree: FileNode, nodeNameToFind: string): FileNode {
    if (fileTree) {
      if (fileTree.name === nodeNameToFind) {
        return fileTree;
      }
      for (const child of fileTree.children) {
        const res = this.getFileNodeByName(child, nodeNameToFind);
        if (res) {
          return res;
        }
      }
    }
  }

  getFileNodeById(fileTree: FileNode, nodeIdToFind: number): any {
    if (fileTree) {
      if (fileTree.id === nodeIdToFind) {
        return fileTree;
      }
      for (const child of fileTree.children) {
        const res = this.getFileNodeById(child, nodeIdToFind);
        if (res) {
          return res;
        }
      }
    }
  }

  updateMedataTable(node: FileNode) {
    // let isNodeComplex = this.sedaService.checkSedaElementType(node.name,this.sedaService.selectedSedaNodeParent.getValue())
    const rulesFromService = this.tabChildrenRulesChange.getValue();
    const tabChildrenToInclude = rulesFromService[0];
    const tabChildrenToExclude = rulesFromService[1];
    this.sedaService.selectedSedaNode.next(node.sedaData);
    const dataTable = this.fileMetadataService.fillDataTable(node.sedaData, node, tabChildrenToInclude, tabChildrenToExclude);
    const hasAtLeastOneComplexChild = node.children.some((el) => el.type === TypeConstants.element);

    if (node.sedaData.element === SedaElementConstants.complex) {
      this.fileMetadataService.shouldLoadMetadataTable.next(hasAtLeastOneComplexChild);
      // console.log('The current tab root node is : ', node);
      this.sedaService.selectedSedaNode.next(node.sedaData);
      // console.log('Filled data on table : ', dataTable, '...should load : '
      //    , this.fileMetadataService.shouldLoadMetadataTable.getValue());
      this.fileMetadataService.dataSource.next(dataTable);
    } else {
      this.fileMetadataService.shouldLoadMetadataTable.next(true);
      this.fileMetadataService.dataSource.next(dataTable);
    }
  }

  setNotice(inverse: boolean) {
    let noticeProfile: ProfileDescription;
    noticeProfile = this.notice.getValue();
    let notice: Notice;
    notice = this.noticeEditable.getValue();
    // console.error("notice profile :" + JSON.stringify(noticeProfile))
    // set notice editable from notice profile
    if (!inverse) {
      const notice: Notice = {
        identifier: noticeProfile.identifier,
        status: noticeProfile.status,
        name: noticeProfile.name,
        description: noticeProfile.description,
      };
      this.noticeEditable.next(notice);
    } else {
      if (notice.identifier) {
        noticeProfile.identifier = notice.identifier;
      }
      noticeProfile.name = notice.name;
      noticeProfile.status = notice.status;
      noticeProfile.description = notice.description;
      this.notice.next(noticeProfile);
    }
  }

  ngOnDestroy() {
    // console.log('fileService.currentTreeLoaded2 ' + this.currentTreeLoaded);
    if (this._profileServiceGetProfileSubscription != null) {
      this._profileServiceGetProfileSubscription.unsubscribe();
    }
  }
}
