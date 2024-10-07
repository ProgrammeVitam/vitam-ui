import { Component, Inject } from '@angular/core';
import { MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef } from '@angular/material/legacy-dialog';
import { NavigationExtras, Router } from '@angular/router';
import {
  AccessContract,
  AccessContractService,
  ApiUnitObject,
  ObjectQualifierType,
  qualifiersToVersionsWithQualifier,
  TenantSelectionService,
  VersionWithQualifierDto,
} from 'vitamui-library';
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
  versionWithQualifier: VersionWithQualifierDto;

  constructor(
    private dialogRef: MatDialogRef<PurgedPersistentIdentifierDto>,
    private router: Router,
    private tenantSelectionService: TenantSelectionService,
    private archiveService: ArchiveService,
    private accessContractService: AccessContractService,
    @Inject(MAT_DIALOG_DATA) data: { ark: string; object: ApiUnitObject },
  ) {
    this.ark = data.ark;
    this.unitId = data.object['#unitups'][0];
    this.versionWithQualifier = qualifiersToVersionsWithQualifier(data.object['#qualifiers']).find((version) =>
      version.PersistentIdentifier.some((persistentId) => persistentId.PersistentIdentifierContent === data.ark),
    );

    if (this.versionWithQualifier) {
      const fragments = this.versionWithQualifier.DataObjectVersion.split('_');
      this.usageVersion = this.versionWithQualifier.DataObjectVersion;
      this.qualifier = fragments[0];
      this.qualifierVersion = Number.parseInt(fragments[1]);
      this.isPhysicalMaster = this.qualifier === ObjectQualifierType.PHYSICALMASTER;
      this.accessContractService.currentAccessContract$.subscribe(
        (accessContract: AccessContract) => (this.versionWithQualifier.downloadAllowed = this.accessContractAllowDownload(accessContract)),
      );
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

  accessContractAllowDownload(accessContract: AccessContract): boolean {
    if (accessContract.everyDataObjectVersion) {
      return true;
    }
    if (!accessContract.dataObjectVersion) {
      return false;
    }
    return accessContract.dataObjectVersion.includes(this.versionWithQualifier.qualifier);
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
