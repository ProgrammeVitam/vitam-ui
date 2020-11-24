import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute} from '@angular/router';

import {SecurityProfile} from 'projects/vitamui-library/src/lib/models/security-profile';
import {GlobalEventService, SidenavPage} from 'ui-frontend-common';
import {SecurityProfileCreateComponent} from './security-profile-create/security-profile-create.component';
import {SecurityProfileListComponent} from './security-profile-list/security-profile-list.component';

@Component({
  selector: 'app-security-profile',
  templateUrl: './security-profile.component.html',
  styleUrls: ['./security-profile.component.scss']
})
export class SecurityProfileComponent extends SidenavPage<SecurityProfile> implements OnInit {

  search = '';

  @ViewChild(SecurityProfileListComponent, {static: true}) contextListComponent: SecurityProfileListComponent;

  constructor(public dialog: MatDialog, route: ActivatedRoute, globalEventService: GlobalEventService) {
    super(route, globalEventService);
  }

  openCreateSecurityProfileDialog() {
    const dialogRef = this.dialog.open(SecurityProfileCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result.success) {
        this.refreshList();
      }
      if (result.action === 'restart') {
        this.openCreateSecurityProfileDialog();
      }
    });
  }

  private refreshList() {
    if (!this.contextListComponent) {
      return;
    }
    this.contextListComponent.searchSecurityProfileOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  ngOnInit() {
  }

  showSecurityProfile(item: SecurityProfile) {
    this.openPanel(item);
  }

}
