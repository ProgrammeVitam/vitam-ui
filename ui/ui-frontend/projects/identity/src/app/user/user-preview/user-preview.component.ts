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
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AdminUserProfile, AuthService, Customer, Group, isLevelAllowed, StartupService, User } from 'ui-frontend-common';
import { UserInfo } from 'ui-frontend-common/app/modules/models/user/user-info.interface';
import { UserInfoService } from './../user-info.service';

import { UserApiService } from '../../core/api/user-api.service';
import { GroupService } from '../../group/group.service';
import { GroupSelection } from '../group-selection.interface';
import { UserService } from '../user.service';

@Component({
  selector: 'app-user-preview',
  templateUrl: './user-preview.component.html',
  styleUrls: ['./user-preview.component.scss'],
})
export class UserPreviewComponent implements OnDestroy, OnInit {
  @Input() isPopup: boolean;

  // tslint:disable-next-line:variable-name
  _user: User;

  get user(): User {
    return this._user;
  }
  @Input()
  set user(user: User) {
    this._user = user;
    this.collectionsMap = new Map();
    const map = new Map();
    const ids: string[] = [];
    if (this._user) {
      map.set(this._user.id, 'users');
      ids.push(this._user.identifier);

      if (this._user.userInfoId) {
        this.userInfoService.get(this._user.userInfoId).subscribe((response) => {
          this.userInfo = response;
          ids.push(this.userInfo.identifier);
          map.set(this.userInfo.id, 'userinfos');
          this.collectionsMap = map;
          this.identifiers = ids;
        });
      } else {
        this.collectionsMap = map;
        this.identifiers = ids;
      }
    }
  }
  @Input() customer: Customer;

  @Output() previewClose = new EventEmitter();

  @ViewChild('confirmDisabledUserDialog', { static: true }) confirmDisabledUserDialog: TemplateRef<UserPreviewComponent>;
  @ViewChild('confirmEnabledUserDialog', { static: true }) confirmEnabledUserDialog: TemplateRef<UserPreviewComponent>;
  @ViewChild('confirmdeleteUserDialog', { static: true }) confirmdeleteUserDialog: TemplateRef<UserPreviewComponent>;

  public connectedUserInfo: AdminUserProfile;
  public userUpdatedSub: Subscription;
  public attribaGroups: GroupSelection[];
  public userInfo: UserInfo;
  public collectionsMap: Map<string, string>;
  public identifiers: string[];

  @Input()
  get groups(): Group[] {
    return this._groups;
  }
  set groups(groupList: Group[]) {
    this._groups = groupList;
  }

  get isVitamEnabled(): boolean {
    return this.startupService.isVitamEnabled();
  }

  // tslint:disable-next-line:variable-name
  private _groups: Group[];


  constructor(
    private matDialog: MatDialog,
    private userService: UserService,
    private authService: AuthService,
    public userApi: UserApiService,
    private startupService: StartupService,
    public groupService: GroupService,
    private userInfoService: UserInfoService,
  ) {}

  ngOnInit() {
    this.connectedUserInfo = this.userService.getUserProfileInfo(this.authService.user);
    this.userUpdatedSub = this.userService.userUpdated.subscribe((updatedUser: User) => {
      this.user = updatedUser;
    });
  }

  ngOnDestroy() {
    this.userUpdatedSub.unsubscribe();
  }

  openPopup() {
    window.open(
      this.startupService.getConfigStringValue('UI_URL') + '/user/' + this.user.id,
      'detailPopup',
      'width=584, height=713, resizable=no, location=no',
    );
    this.emitClose();
  }

  updateStatus(status: string) {
    let dialogToOpen;
    if (status === 'ENABLED') {
      dialogToOpen = this.confirmEnabledUserDialog;
    } else if (status === 'DISABLED') {
      dialogToOpen = this.confirmDisabledUserDialog;
    }
    const dialogRef = this.matDialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
    dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.userService.patch({ id: this.user.id, status }).subscribe((user) => {
          this.user = user;
        });
      });
  }

  levelNotAllowed(): boolean {
    if (this.user) {
      return !isLevelAllowed(this.authService.user, this.user.level);
    }
  }

  emitClose() {
    this.previewClose.emit();
  }

  filterAuthenticationEvents(event: any): boolean {
    return (
      event.outDetail &&
      (event.outDetail.includes('EXT_VITAMUI_CREATE_USER') ||
        event.outDetail.includes('EXT_VITAMUI_UPDATE_USER') ||
        event.outDetail.includes('EXT_VITAMUI_BLOCK_USER') ||
        event.outDetail.includes('EXT_VITAMUI_PASSWORD_REVOCATION') ||
        event.outDetail.includes('EXT_VITAMUI_PASSWORD_INIT') ||
        event.outDetail.includes('EXT_VITAMUI_PASSWORD_CHANGE') ||
        event.outDetail.includes('EXT_VITAMUI_CREATE_USER_INFO') ||
        event.outDetail.includes('EXT_VITAMUI_UPDATE_USER_INFO'))
    );
  }

  deleteUser(user: User, status: string) {
    const emailadress = user.email.split('@');
    const email = 'anonyme-' + user.identifier + '@' + emailadress[1];
    const firstname = '';
    const lastname = '';
    const siteCode = '';
    const internalCode = '';
    const groupId = '';
    const address = {
      street: '',
      zipCode: '',
      city: '',
      country: '',
    };

    let dialogToOpen;
    dialogToOpen = this.confirmdeleteUserDialog;
    const dialogRef = this.matDialog.open(dialogToOpen, { panelClass: 'vitamui-dialog' });
    dialogRef
      .afterClosed()
      .pipe(filter((result) => !!result))
      .subscribe(() => {
        this.userService
          .deleteUser({
            id: this.user.id,
            lastname,
            email,
            address,
            mobile: null,
            phone: null,
            status,
            groupId,
            firstname,
            siteCode,
            internalCode,
          })
          // tslint:disable-next-line:no-shadowed-variable
          .subscribe((user) => {
            this.user = user;
            this.emitClose();
          });
      });
  }
}
