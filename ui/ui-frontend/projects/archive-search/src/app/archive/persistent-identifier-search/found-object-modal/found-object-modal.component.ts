import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NavigationExtras, Router } from '@angular/router';
import { ApiUnitObject, TenantSelectionService } from 'ui-frontend-common';
import { PurgedPersistentIdentifierDto } from '../../../core/api/persistent-identifier-response-dto.interface';
import { ArchiveService } from '../../archive.service';

@Component({
  selector: 'app-found-object-modal',
  templateUrl: './found-object-modal.component.html',
  styleUrls: ['./found-object-modal.component.scss'],
})
export class FoundObjectModalComponent {
  ark: string;
  usageVersion: string;
  private readonly qualifier: string;
  private readonly qualifierVersion: number;
  private readonly unitId: string;
  downloading = false;

  constructor(
    private dialogRef: MatDialogRef<PurgedPersistentIdentifierDto>,
    private router: Router,
    private tenantSelectionService: TenantSelectionService,
    private archiveService: ArchiveService,
    @Inject(MAT_DIALOG_DATA) data: { ark: string; object: ApiUnitObject },
  ) {
    this.ark = data.ark;
    this.qualifier = data.object['#qualifiers'][0].qualifier;
    this.qualifierVersion = Number.parseInt(data.object['#qualifiers'][0]['#nbc']);
    this.usageVersion = `${this.qualifier}_${this.qualifierVersion}`;
    this.unitId = data.object['#unitups'][0];
  }

  lookupUnit() {
    const extras: NavigationExtras = {
      queryParams: {
        GUID: this.unitId,
      },
    };
    this.closeDialog();
    this.router.navigate(['/archive-search/tenant/', this.tenantSelectionService.getSelectedTenant().identifier], extras);
  }

  async downloadObject() {
    this.downloading = true;
    await this.archiveService.launchDownloadObjectFromUnit(
      this.unitId,
      this.tenantSelectionService.getSelectedTenant().identifier,
      this.qualifier,
      this.qualifierVersion,
    );
    this.downloading = false;
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
