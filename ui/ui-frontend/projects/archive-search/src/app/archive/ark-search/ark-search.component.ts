import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { NavigationExtras, Router } from '@angular/router';
import { ApplicationId, BreadCrumbData, TenantSelectionService, Unit } from 'ui-frontend-common';
import { PersistentIdentifierService } from '../persistent-identifier.service';
import { ArkSearchErrorResponseData, ArkStatus } from './error-response-modal/ark-search-error-response-data.interface';
import { ErrorResponseModalComponent } from './error-response-modal/error-response-modal.component';

@Component({
  selector: 'app-ark-search',
  templateUrl: './ark-search.component.html',
  styleUrls: ['./ark-search.component.scss']
})
export class ArkSearchComponent implements OnInit {
  appsHierarchy: BreadCrumbData[] = [
    { identifier: ApplicationId.PORTAL_APP },
    { identifier: ApplicationId.ARCHIVE_SEARCH_APP },
    { identifier: ApplicationId.ARK_SEARCH_APP }
  ];

  constructor(
    private dialog: MatDialog,
    private persistentIdentifierService: PersistentIdentifierService,
    private tenantSelectionService: TenantSelectionService,
    // @ts-ignore
    private router: Router,
  ) {}

  ngOnInit() {
  }

  onSearch(arkId: string) {
    this.persistentIdentifierService.findByPersistentIdentifier(arkId).subscribe(
      (units: Unit[]) => {
        console.log(JSON.stringify(units));
        const extras: NavigationExtras = {
          state: { units }
        }
        this.router.navigate(['/archive-search/tenant/', this.tenantSelectionService.getSelectedTenant().identifier], extras);
      },
      (error: any) => {
        console.log(JSON.stringify(error));
      })
  }

  openDialog(arkSearchErrorData: ArkSearchErrorResponseData) {
    arkSearchErrorData.arkId = 'ark:/22567/001a957db5eadaac';
    arkSearchErrorData.status = ArkStatus.TRANSFERRED;
    this.dialog.open(ErrorResponseModalComponent, {
      width: '800px',
      panelClass: 'vitamui-dialog',
      data: arkSearchErrorData,
    });
  }
}
