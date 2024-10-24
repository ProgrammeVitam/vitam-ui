/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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

import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { Observable } from 'rxjs';
import { TenantSelectionService, Unit } from 'vitamui-library';
import { ArchiveCollectService } from '../../archive-collect.service';

@Component({
  selector: 'app-archive-unit-information-tab',
  templateUrl: './archive-unit-information-tab.component.html',
  styleUrls: ['./archive-unit-information-tab.component.css'],
})
export class ArchiveUnitInformationTabComponent implements OnChanges {
  @Input() archiveUnit: Unit;

  @Output() showNormalPanel = new EventEmitter<any>();

  uaPath$: Observable<{ fullPath: string; resumePath: string }>;
  fullPath = false;
  hasDownloadDocumentRole = false;

  constructor(
    private archiveService: ArchiveCollectService,
    private tenantSelectionService: TenantSelectionService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    this.checkDownloadPermissions();

    if (changes.archiveUnit?.currentValue['#id']) {
      // TODO : Créer Web service de création du chemin d'archive
      // this.uaPath$ = this.archiveService.buildArchiveUnitPath(this.archiveUnit, this.accessContract);
    }
    this.fullPath = false;
  }

  onDownloadObjectFromUnit(archiveUnit: Unit) {
    return this.archiveService.downloadObjectFromUnit(archiveUnit['#id'], this.archiveUnit['#object']);
  }

  showArchiveUniteFullPath() {
    this.fullPath = true;
  }

  private checkDownloadPermissions() {
    this.archiveService
      .hasCollectRole('ROLE_COLLECT_GET_ARCHIVE_BINARY', this.tenantSelectionService.getSelectedTenant().identifier)
      .subscribe((result) => {
        this.hasDownloadDocumentRole = result;
      });
  }
}
