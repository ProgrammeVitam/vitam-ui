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

import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiUnitObject, DescriptionLevel, Unit, VersionWithQualifierDto, qualifiersToVersionsWithQualifier } from 'ui-frontend-common';
import { GetorixUnitFullPath } from '../../../core/model/getorix-unit-full-path.interface';
import { GetorixDepositService } from '../../../getorix-deposit.service';

@Component({
  selector: 'archive-unit-informations-tab',
  templateUrl: './archive-unit-informations-tab.component.html',
  styleUrls: ['./archive-unit-informations-tab.component.scss'],
})
export class ArchiveUnitInformationsTabComponent implements OnInit, OnDestroy, OnChanges {
  @Input()
  archiveUnit: Unit;
  @Input()
  archiveUnitId: string;

  unitFullPath: GetorixUnitFullPath[] = [];
  unitObject: ApiUnitObject;
  versionsWithQualifiersOrdered: Array<VersionWithQualifierDto> = [];
  loadingFullPath = true;
  loadingObjectSize = true;

  subscriptions: Subscription = new Subscription();

  constructor(private getorixDepositService: GetorixDepositService) {}

  ngOnInit() {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.archiveUnitId) {
      this.getUnitFullPath();
    }

    if (changes.archiveUnit) {
      this.unitObject = null;
      this.versionsWithQualifiersOrdered = [];
      if (this.unitHasObject()) {
        this.getObjectGroupDetailsById(this.archiveUnit);
      }
    }
  }

  ngOnDestroy() {
    this.subscriptions?.unsubscribe();
  }

  getUnitFullPath() {
    if (this.archiveUnitId) {
      this.loadingFullPath = true;
      this.unitFullPath = [];
      this.subscriptions.add(
        this.getorixDepositService.getUnitFullPath(this.archiveUnitId).subscribe((response) => {
          this.unitFullPath = response;
          this.loadingFullPath = false;
        }),
      );
    }
  }

  getObjectGroupDetailsById(archiveUnit: Unit) {
    this.loadingObjectSize = true;
    this.subscriptions.add(
      this.getorixDepositService.getObjectGroupDetailsById(archiveUnit['#object']).subscribe((unitObject) => {
        this.unitObject = unitObject;

        this.versionsWithQualifiersOrdered = qualifiersToVersionsWithQualifier(this.unitObject['#qualifiers']);
        this.loadingObjectSize = false;
      }),
    );
  }

  unitHasObject(): boolean {
    if (this.archiveUnit) {
      return this.archiveUnit.DescriptionLevel === DescriptionLevel.ITEM && !!this.archiveUnit['#object'];
    }
  }

  getObjectType() {
    return this.versionsWithQualifiersOrdered[0]?.FileInfo?.Filename?.split('.')[1]?.toUpperCase();
  }
}
