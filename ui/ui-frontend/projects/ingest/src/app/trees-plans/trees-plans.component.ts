import { Component, OnInit } from '@angular/core';
import { MatDialogConfig, MatDialog } from '@angular/material';
import {  ActivatedRoute, Router } from '@angular/router';
import { GlobalEventService, SidenavPage } from 'ui-frontend-common';
import { UploadComponent } from '../core/common/upload.component';

@Component({
  selector: 'app-trees-plans',
  templateUrl: './trees-plans.component.html',
  styleUrls: ['./trees-plans.component.scss']
})
export class TreesPlansComponent extends SidenavPage<any> implements OnInit {

  search: string;
  tenantIdentifier: string;

  constructor(private router: Router, private route: ActivatedRoute, globalEventService: GlobalEventService, public dialog: MatDialog) {
    super(route, globalEventService);

    route.params.subscribe(params => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
  }

  ngOnInit() {
  }

  openImportTreePlanPopup(type: string) {
    const dialogConfig = new MatDialogConfig();

    dialogConfig.panelClass = 'vitamui-modal';
    dialogConfig.disableClose = false;

    dialogConfig.data = {
      tenantIdentifier: this.tenantIdentifier,
      givenContextId: type
    };

    const dialogRef = this.dialog.open(UploadComponent, dialogConfig);

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.refresh();
      }
    });
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }

  refresh() {
  }
}
