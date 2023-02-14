/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
import {animate, AUTO_STYLE, state, style, transition, trigger} from '@angular/animations';
import {Clipboard} from '@angular/cdk/clipboard';
import {HttpHeaders} from '@angular/common/http';
import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ApiUnitObject, qualifiersToVersionsWithQualifier, Unit,VersionWithQualifierDto} from 'ui-frontend-common';
import {DescriptionLevel} from 'vitamui-library';
import {ArchiveService} from '../../archive.service';

@Component({
  selector: 'app-archive-unit-objects-details-tab',
  templateUrl: './archive-unit-objects-details-tab.component.html',
  styleUrls: ['./archive-unit-objects-details-tab.component.scss'],
  animations: [
    trigger('collapse', [
      state('false', style({height: AUTO_STYLE, visibility: AUTO_STYLE})),
      state('true', style({height: '0', visibility: 'hidden'})),
      transition('false => true', animate(300 + 'ms ease-in')),
      transition('true => false', animate(300 + 'ms ease-out')),
    ]),
  ],
})
export class ArchiveUnitObjectsDetailsTabComponent implements OnChanges {
  @Input() archiveUnit: Unit;
  @Input() accessContract: string;
  @Input() tenantIdentifier: number;
  unitObject: ApiUnitObject;
  versionsWithQualifiersOrdered: Array<VersionWithQualifierDto>;

  constructor(private archiveService: ArchiveService,
              private clipboard: Clipboard) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.archiveUnit) {
      this.unitObject = null;
      this.versionsWithQualifiersOrdered = null;
      if (this.uniHasObject()) {
        this.sendCalls(this.archiveUnit);
      }
    }
  }

  private uniHasObject(): boolean {
    return this.archiveUnit.DescriptionLevel === DescriptionLevel.ITEM;
  }

  onClickDownloadObject(event: Event, versionWithQualifier: VersionWithQualifierDto) {
    event.stopPropagation();
    return this.archiveService.launchDownloadObjectFromUnit(this.archiveUnit['#id'],
      this.tenantIdentifier,
      this.accessContract,
      versionWithQualifier.qualifier,
      versionWithQualifier.version);
  }

  copyToClipboard(text: string) {
    this.clipboard.copy(text);
  }

  sendCalls(archiveUnit: Unit) {
    const headers = new HttpHeaders()
      .append('Content-Type', 'application/json')
      .append('X-Access-Contract-Id', this.accessContract);
    this.archiveService
      .getObjectById(archiveUnit['#id'], headers)
      .subscribe((unitObject) => {
        this.unitObject = unitObject
        this.versionsWithQualifiersOrdered = qualifiersToVersionsWithQualifier(this.unitObject['#qualifiers'])
        this.setFirstVersionWithQualifierOpen();
      });
  }

  setFirstVersionWithQualifierOpen() {
    if (this.versionsWithQualifiersOrdered && this.versionsWithQualifiersOrdered.length > 0) {
      this.versionsWithQualifiersOrdered[0].opened = true;
    }
  }

  openClose(versionWithQualifier: VersionWithQualifierDto) {
    versionWithQualifier.opened = !versionWithQualifier.opened;
  }
}
