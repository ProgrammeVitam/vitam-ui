/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
import { Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogRef } from '@angular/material/dialog';
import { NavigationExtras, Router } from '@angular/router';
import {
  AccessContract,
  AccessContractService,
  ApiUnitObject,
  DialogHeaderComponent,
  ObjectQualifierType,
  qualifiersToVersionsWithQualifier,
  TenantSelectionService,
  VersionWithQualifierDto,
} from 'vitamui-library';
import { PurgedPersistentIdentifierDto } from '../../../core/api/persistent-identifier-response-dto.interface';
import { ArchiveService } from '../../archive.service';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-found-object-modal',
  templateUrl: './found-object-modal.component.html',
  styleUrls: ['./found-object-modal.component.scss'],
  imports: [DialogHeaderComponent, MatDialogActions, TranslatePipe],
})
export class FoundObjectModalComponent {
  private dialogRef = inject<MatDialogRef<PurgedPersistentIdentifierDto>>(MatDialogRef);
  private router = inject(Router);
  private tenantSelectionService = inject(TenantSelectionService);
  private archiveService = inject(ArchiveService);
  private accessContractService = inject(AccessContractService);

  ark: string;
  usageVersion: string;
  private readonly qualifier: string;
  private readonly qualifierVersion: number;
  private readonly unitId: string;
  downloading = false;
  isPhysicalMaster = false;
  versionWithQualifier: VersionWithQualifierDto;

  constructor() {
    const data = inject<{
      ark: string;
      object: ApiUnitObject;
    }>(MAT_DIALOG_DATA);

    this.ark = data.ark;
    this.unitId = data.object['#unitups'][0];
    this.versionWithQualifier = qualifiersToVersionsWithQualifier(data.object['#qualifiers']).find((version) =>
      version.PersistentIdentifier?.some((persistentId) => persistentId.PersistentIdentifierContent === data.ark),
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
