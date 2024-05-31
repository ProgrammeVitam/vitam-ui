import { Component } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { NavigationExtras, Router } from '@angular/router';
import { combineLatest } from 'rxjs';
import { ApiUnitObject, ApplicationId, BreadCrumbData, TenantSelectionService } from 'vitamui-library';
import { PurgedPersistentIdentifierDto } from '../../core/api/persistent-identifier-response-dto.interface';
import { PERMANENT_IDENTIFIER } from '../archive-search/archive-search.component';
import { PersistentIdentifierService } from '../persistent-identifier.service';
import { FoundObjectModalComponent } from './found-object-modal/found-object-modal.component';
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
    combineLatest([
      this.persistentIdentifierService.findUnitsByPersistentIdentifier(id),
      this.persistentIdentifierService.findObjectsByPersistentIdentifier(id),
    ]).subscribe(
      ([units, objects]) => {
        if (units.$history?.length > 0) {
          const purgedPersistentIdentifier: PurgedPersistentIdentifierDto = units.$history.slice(-1)[0];
          this.openPurgedDialog(id, purgedPersistentIdentifier);
        } else if (units.$results?.length > 0) {
          this.searchByArk(id);
        } else if (objects.$history?.length > 0) {
          const purgedPersistentIdentifier: PurgedPersistentIdentifierDto = objects.$history.slice(-1)[0];
          this.openPurgedDialog(id, purgedPersistentIdentifier);
        } else if (objects.$results?.length > 0) {
          this.openFoundObjectDialog(id, objects.$results.slice(-1)[0]);
        } else {
          this.searchByArk(id);
        }
      },
      (error: any) => {
        console.log(JSON.stringify(error));
      },
    );
  }

  private searchByArk(id: string) {
    const extras: NavigationExtras = {
      queryParams: {
        [PERMANENT_IDENTIFIER]: id,
      },
    };
    this.router.navigate(['/archive-search/tenant/', this.tenantSelectionService.getSelectedTenant().identifier], extras);
  }

  openPurgedDialog(ark: string, purgedPersistentIdentifier: PurgedPersistentIdentifierDto) {
    this.dialog.open(PurgedPersistentIdentifierModalComponent, {
      width: '800px',
      panelClass: 'vitamui-dialog',
      data: { ark, purgedPersistentIdentifier },
    });
  }

  openFoundObjectDialog(ark: string, object: ApiUnitObject) {
    this.dialog.open(FoundObjectModalComponent, {
      width: '800px',
      panelClass: 'vitamui-dialog',
      data: { ark, object },
    });
  }
}
