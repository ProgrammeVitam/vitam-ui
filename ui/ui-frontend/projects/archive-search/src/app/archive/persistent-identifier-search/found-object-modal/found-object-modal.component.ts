import { Component, Inject } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { NavigationExtras, Router } from '@angular/router';
import { ApiUnitObject, VersionDto, TenantSelectionService, ObjectQualifierType } from 'vitamui-library';
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
  isPhysicalMaster = false;

  constructor(
    private dialogRef: MatDialogRef<PurgedPersistentIdentifierDto>,
    private router: Router,
    private tenantSelectionService: TenantSelectionService,
    private archiveService: ArchiveService,
    @Inject(MAT_DIALOG_DATA) data: { ark: string; object: ApiUnitObject },
  ) {
    this.ark = data.ark;
    this.unitId = data.object['#unitups'][0];

    const version: VersionDto = data.object['#qualifiers']
      .flatMap((qualifier) => qualifier.versions)
      .find((version) => version.PersistentIdentifier.some((persistentId) => persistentId.PersistentIdentifierContent === data.ark));

    if (version) {
      const fragments = version.DataObjectVersion.split('_');
      this.usageVersion = version.DataObjectVersion;
      this.qualifier = fragments[0];
      this.qualifierVersion = Number.parseInt(fragments[1]);
      this.isPhysicalMaster = this.qualifier === ObjectQualifierType.PHYSICALMASTER;
    }
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

    return this.archiveService
      .downloadObjectFromUnit(this.unitId, this.qualifier, this.qualifierVersion)
      .add(() => (this.downloading = false));
  }

  closeDialog() {
    this.dialogRef.close();
  }
}
