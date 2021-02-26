import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute} from '@angular/router';

import {SecurityProfile} from 'projects/vitamui-library/src/lib/models/security-profile';
import {ApplicationService, GlobalEventService, SidenavPage} from 'ui-frontend-common';
import {SecurityProfileCreateComponent} from './security-profile-create/security-profile-create.component';
import {SecurityProfileListComponent} from './security-profile-list/security-profile-list.component';

@Component({
  selector: 'app-security-profile',
  templateUrl: './security-profile.component.html',
  styleUrls: ['./security-profile.component.scss']
})
export class SecurityProfileComponent extends SidenavPage<SecurityProfile> implements OnInit {

  search = '';
  isSlaveMode: boolean;

  @ViewChild(SecurityProfileListComponent, {static: true}) contextListComponent: SecurityProfileListComponent;

  constructor(public dialog: MatDialog, route: ActivatedRoute, globalEventService: GlobalEventService,
              private applicationService: ApplicationService) {
    super(route, globalEventService);
  }

  openCreateSecurityProfileDialog() {
    const dialogRef = this.dialog.open(SecurityProfileCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true
    });
    dialogRef.componentInstance.isSlaveMode = this.isSlaveMode;
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

  updateSlaveMode() {
    this.applicationService.isApplicationExternalIdentifierEnabled('SECURITY_PROFILE').subscribe((value) => {
      this.isSlaveMode = value;
    });
  }

  ngOnInit() {
    this.updateSlaveMode();
  }

  showSecurityProfile(item: SecurityProfile) {
    this.openPanel(item);
  }

}
