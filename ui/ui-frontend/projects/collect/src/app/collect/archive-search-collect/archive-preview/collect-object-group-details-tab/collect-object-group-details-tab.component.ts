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
import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {
  ApiUnitObject,
  DescriptionLevel,
  FileInfoDto,
  FormatIdentificationDto,
  qualifiersToVersionsWithQualifier,
  Unit,
  VersionWithQualifierDto
} from 'ui-frontend-common';
import {ArchiveCollectService} from '../../archive-collect.service';

@Component({
  selector: 'app-collect-object-group-details-tab',
  templateUrl: './collect-object-group-details-tab.component.html',
  styleUrls: ['./collect-object-group-details-tab.component.scss'],
  animations: [
    trigger('collapse', [
      state('false', style({height: AUTO_STYLE, visibility: AUTO_STYLE})),
      state('true', style({height: '0', visibility: 'hidden'})),
      transition('false => true', animate(300 + 'ms ease-in')),
      transition('true => false', animate(300 + 'ms ease-out')),
    ]),
  ],
})
export class CollectObjectGroupDetailsTabComponent implements OnInit, OnChanges {
  @Input() archiveUnit: Unit;
  unitObject: ApiUnitObject;
  versionsWithQualifiersOrdered: Array<VersionWithQualifierDto>;

  constructor(private archiveCollectService: ArchiveCollectService, private clipboard: Clipboard) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.archiveUnit) {
      this.unitObject = null;
      this.versionsWithQualifiersOrdered = null;
      if (this.unitHasObject()) {
        this.getObjectGroupDetailsById(this.archiveUnit);
      }
    }
  }

  unitHasObject(): boolean {
    return this.archiveUnit.DescriptionLevel === DescriptionLevel.ITEM && !!this.archiveUnit['#object'];
  }

  onClickDownloadObject(event: Event, versionWithQualifier: VersionWithQualifierDto) {
    event.stopPropagation();
    return this.archiveCollectService.launchDownloadObjectFromUnit(
      this.archiveUnit['#id'],
      this.archiveUnit['#object'],
      this.archiveUnit['#tenant'],
      versionWithQualifier.qualifier,
      versionWithQualifier.version
    );
  }

  copyToClipboard(text: string) {
    this.clipboard.copy(text);
  }

  getObjectGroupDetailsById(archiveUnit: Unit) {
    this.archiveCollectService.getObjectGroupDetailsById(archiveUnit['#object']).subscribe((unitObject) => {
      this.unitObject = unitObject;
      this.versionsWithQualifiersOrdered = qualifiersToVersionsWithQualifier(this.unitObject['#qualifiers']);
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

  getFormatLitteral(formatIdentificationDto: FormatIdentificationDto): string {
    return formatIdentificationDto ? formatIdentificationDto.FormatLitteral : null;
  }

  getMimeType(formatIdentificationDto: FormatIdentificationDto): string {
    return formatIdentificationDto ? formatIdentificationDto.MimeType : null;
  }

  getFormatId(formatIdentificationDto: FormatIdentificationDto): string {
    return formatIdentificationDto ? formatIdentificationDto.FormatId : null;
  }

  getEncoding(formatIdentificationDto: FormatIdentificationDto): string {
    return formatIdentificationDto ? formatIdentificationDto.Encoding : null;
  }

  getFileName(fileInfoDto: FileInfoDto): string {
    return fileInfoDto ? fileInfoDto.Filename : null;
  }

  getLastModified(fileInfoDto: FileInfoDto): string {
    return fileInfoDto ? fileInfoDto.LastModified : null;
  }

  getDateCreatedByApplication(fileInfoDto: FileInfoDto): string {
    return fileInfoDto ? fileInfoDto.DateCreatedByApplication : null;
  }

  getCreatingOsVersion(fileInfoDto: FileInfoDto): string {
    return fileInfoDto ? fileInfoDto.CreatingOsVersion : null;
  }

  getCreatingOs(fileInfoDto: FileInfoDto): string {
    return fileInfoDto ? fileInfoDto.CreatingOs : null;
  }

  getCreatingApplicationVersion(fileInfoDto: FileInfoDto): string {
    return fileInfoDto ? fileInfoDto.CreatingApplicationVersion : null;
  }

  getCreatingApplicationName(fileInfoDto: FileInfoDto): string {
    return fileInfoDto ? fileInfoDto.CreatingApplicationName : null;
  }
}
