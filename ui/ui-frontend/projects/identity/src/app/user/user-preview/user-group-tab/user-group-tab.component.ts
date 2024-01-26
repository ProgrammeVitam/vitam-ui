/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { Component, Input, OnChanges, OnDestroy, OnInit } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { Subject } from 'rxjs';

import { AdminUserProfile, AuthService, Group, isRootLevel, Profile, User } from 'ui-frontend-common';
import { GroupService } from '../../../group/group.service';
import { GroupAttributionComponent } from '../../group-attribution/group-attribution.component';
import { GroupSelection } from '../../group-selection.interface';
import { UserService } from '../../user.service';

@Component({
  selector: 'app-user-group-tab',
  templateUrl: './user-group-tab.component.html',
  styleUrls: ['./user-group-tab.component.scss'],
})
export class UserGroupTabComponent implements OnInit, OnChanges, OnDestroy {

  @Input()
  set user(user: User) {
      this._user = user;
  }
  get user(): User { return this._user; }
  private _user: User;

  @Input()
  set readOnly(readOnly: boolean) {
    if (readOnly) {
      this.showUpdateButton = false;
    } else {
      this.showUpdateButton = true;
    }
  }
  get readOnly(): boolean { return this._readOnly; }
  private _readOnly: boolean;

  @Input()
  set userInfo(userInfo: AdminUserProfile) {
    this._userInfo = userInfo;
  }
  get userInfo() { return this._userInfo; }
  private _userInfo: AdminUserProfile;

  @Input()
  get groups(): Group[] { return this._groups; }
  set groups(groupList: Group[]) {
    this._groups = groupList;
  }

  // eslint-disable-next-line @typescript-eslint/naming-convention, no-underscore-dangle, id-blacklist, id-match
  private _groups: Group[];

  form: FormGroup;
  activeGroups: GroupSelection[];
  userGroup: Group;
  showUpdateButton: boolean;
  public groupProfiles: Profile [] = [];

  private destroy = new Subject();

  constructor(
    public groupAttrDialog: MatDialog,
    public userService: UserService,
    public groupService: GroupService,
    public authService: AuthService,
  ) {}

  ngOnDestroy(): void {
    this.destroy.next();
  }

  ngOnInit() {}

  ngOnChanges() {
    this.getUserProfileDetail();
    this.getGroupProfiles();
  }

  getGroupProfiles() {
    if(this.userGroup) {
      this.groupService.get(this.userGroup.id).subscribe((groupRetrieved) => {
        this.groupProfiles = groupRetrieved.profiles;
      });
    }
  }

  getUserProfileDetail() {
      this.activeGroups = this.groups.map((group) => Object({ id: group.id, name: group.name,
                                                       description: group.description,
                                                       level: group.level,
                                                       selected: false, profiles: group.profiles }));
      if (!isRootLevel(this.authService.user)) {
      this.activeGroups = this.activeGroups.filter((g) => g.id !== this.authService.user.groupId);
      }
      if (this.user.groupId) {
        this.userGroup = this.groups.find((group) => group.id === this.user.groupId);
      }
  }

  getAttributableGroups(): GroupSelection [] {
    if (this.userInfo.type === 'LIST') {
      this.activeGroups = [];
      this.userInfo.profilGroup.forEach((displayGroup) => {
        const profilGroup = this.groups.find((group) => group.id === displayGroup.id);
        const simplifiedGroup = Object({ id: displayGroup.id, name: displayGroup.name,
                                         description: displayGroup.description, selected: false, profiles: profilGroup?.profiles});
        this.activeGroups.push(simplifiedGroup);
      });
    }

    return this.activeGroups;
  }

  openAttributionGroupe() {
    this.activeGroups = this.getAttributableGroups();
    const dialogRef = this.groupAttrDialog.open(GroupAttributionComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: [this.user, this.userGroup, this.activeGroups]
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) { this.refreshTab(); }
    });
  }

  refreshTab() {
    this.userService.get(this.user.id).subscribe(
      (response) => this.user = response,
      (error) => {
        console.error(error);
      });
  }

}
