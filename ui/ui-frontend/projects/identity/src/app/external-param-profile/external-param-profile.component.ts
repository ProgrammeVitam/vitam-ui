import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { GlobalEventService, SidenavPage, ExternalParamProfile } from 'ui-frontend-common';
import { ExternalParamProfileService } from './external-param-profile.service';
import { ExternalParamProfileCreateComponent } from './external-param-profile-create/external-param-profile-create.component';
import { ExternalParamProfileListComponent } from './external-param-profile-list/external-param-profile-list.component';

@Component({
  selector: 'app-external-param-profile',
  templateUrl: './external-param-profile.component.html',
  styleUrls: ['./external-param-profile.component.css'],
})
export class ExternalParamProfileComponent extends SidenavPage<ExternalParamProfile> implements OnInit {
  dto: ExternalParamProfile;
  tenantIdentifier: string;
  public search: string;
  @ViewChild(ExternalParamProfileListComponent, { static: true }) externalParamProfileListComponent: ExternalParamProfileListComponent;

  constructor(
    public dialog: MatDialog,
    public route: ActivatedRoute,
    public globalEventService: GlobalEventService,
    public externalParamProfileServiceService: ExternalParamProfileService
  ) {
    super(route, globalEventService);
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
  }

  onSearchSubmit(search: string) {
    this.search = search;
  }

  openExternalParamProfilCreateDialog() {
    const dialogRef = this.dialog.open(ExternalParamProfileCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: { tenantIdentifier: this.tenantIdentifier },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.externalParamProfileListComponent) {
      return;
    }
    this.externalParamProfileListComponent.search();
  }
}
