import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { NavigationExtras, Router } from '@angular/router';
import { ApplicationId, BreadCrumbData, TenantSelectionService } from 'ui-frontend-common';
import {
  PersistentIdentifierResponseDto,
  PurgedPersistentIdentifierDto,
} from '../../core/api/persistent-identifier-response-dto.interface';
import { PersistentIdentifierService } from '../persistent-identifier.service';
import { PurgedPersistentIdentifierModalComponent } from './purged-persistent-identifier-modal/purged-persistent-identifier-modal.component';

@Component({
  selector: 'app-persistent-identifier-search',
  templateUrl: './persistent-identifier-search.component.html',
  styleUrls: ['./persistent-identifier-search.component.scss'],
})
export class PersistentIdentifierSearchComponent {
  appsHierarchy: BreadCrumbData[] = [
    { identifier: ApplicationId.PORTAL_APP },
    { identifier: ApplicationId.ARCHIVE_SEARCH_APP },
    { identifier: ApplicationId.PERSISTENT_IDENTIFIER_SEARCH_APP },
  ];

  constructor(
    private dialog: MatDialog,
    private persistentIdentifierService: PersistentIdentifierService,
    private tenantSelectionService: TenantSelectionService,
    private router: Router,
  ) {}

  onSearch(id: string) {
    this.persistentIdentifierService.findUnitsByPersistentIdentifier(id).subscribe(
      (persistentIdentifierResponse: PersistentIdentifierResponseDto) => {
        if (persistentIdentifierResponse.$history?.length > 0) {
          const purgedPersistentIdentifier: PurgedPersistentIdentifierDto = persistentIdentifierResponse.$history.slice(-1)[0];
          this.openDialog(id, purgedPersistentIdentifier);
        } else {
          const extras: NavigationExtras = {
            queryParams: {
              ark: id,
            },
          };
          this.router.navigate(['/archive-search/tenant/', this.tenantSelectionService.getSelectedTenant().identifier], extras);
        }
      },
      (error: any) => {
        console.log(JSON.stringify(error));
      },
    );
  }

  openDialog(ark: string, purgedPersistentIdentifier: PurgedPersistentIdentifierDto) {
    this.dialog.open(PurgedPersistentIdentifierModalComponent, {
      width: '800px',
      panelClass: 'vitamui-dialog',
      data: { ark, purgedPersistentIdentifier },
    });
  }
}
