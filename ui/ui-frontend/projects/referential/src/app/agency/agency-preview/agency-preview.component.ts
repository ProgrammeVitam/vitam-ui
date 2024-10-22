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

import { AfterViewInit, Component, EventEmitter, HostListener, Input, Output, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import {
  MatLegacyTab as MatTab,
  MatLegacyTabGroup as MatTabGroup,
  MatLegacyTabHeader as MatTabHeader,
} from '@angular/material/legacy-tabs';
import { Observable } from 'rxjs';
import { Agency } from 'vitamui-library';
import { ConfirmActionComponent } from 'vitamui-library';
import { AgencyService } from '../agency.service';
import { AgencyInformationTabComponent } from './agency-information-tab/agency-information-tab.component';

@Component({
  selector: 'app-agency-preview',
  templateUrl: './agency-preview.component.html',
  styleUrls: ['./agency-preview.component.scss'],
})
export class AgencyPreviewComponent implements AfterViewInit {
  @Input() agency: Agency;
  @Input() tenantIdentifier: number;
  @Input() readOnly: boolean;

  @Output() previewClose = new EventEmitter<void>();

  public selectedIndex = 0;

  // tab indexes: info = 0; history = 1;
  tabUpdated: boolean[] = [false, false];
  tabLinks: Array<AgencyInformationTabComponent> = [];
  @ViewChild('tabs', { static: false }) tabs: MatTabGroup;
  @ViewChild('infoTab', { static: false }) infoTab: AgencyInformationTabComponent;

  constructor(
    private matDialog: MatDialog,
    private agencyService: AgencyService,
  ) {}

  ngAfterViewInit() {
    this.tabs._handleClick = this.interceptTabChange.bind(this);
    this.tabLinks[0] = this.infoTab;
  }

  @HostListener('window:beforeunload', ['$event'])
  beforeunloadHandler(event: any) {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      event.preventDefault();
      this.checkBeforeExit();
      return '';
    }
  }

  updatedChange(updated: boolean, index: number) {
    this.tabUpdated[index] = updated;
  }

  async checkBeforeExit() {
    if (await this.confirmAction()) {
      const submitAgencyUpdate: Observable<Agency> = this.tabLinks[this.tabs.selectedIndex].prepareSubmit();

      submitAgencyUpdate.subscribe(() => {
        this.agencyService.get(this.agency.identifier).subscribe((response) => {
          this.agency = response;
        });
      });
    } else {
      this.tabLinks[this.tabs.selectedIndex].resetForm(this.agency);
    }
  }

  async interceptTabChange(tab: MatTab, tabHeader: MatTabHeader, idx: number) {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      await this.checkBeforeExit();
    }

    const args = [tab, tabHeader, idx];
    return MatTabGroup.prototype._handleClick.apply(this.tabs, args);
  }

  async confirmAction(): Promise<boolean> {
    const dialog = this.matDialog.open(ConfirmActionComponent, { panelClass: 'vitamui-confirm-dialog' });
    dialog.componentInstance.dialogType = 'changeTab';
    return await dialog.afterClosed().toPromise();
  }

  public filterEvents(event: any): boolean {
    return (
      event.outDetail &&
      (event.outDetail.includes('EXT_VITAMUI_UPDATE_ACCESS_CONTRACT') || event.outDetail.includes('EXT_VITAMUI_CREATE_ACCESS_CONTRACT'))
    );
  }

  public async emitClose() {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      await this.checkBeforeExit();
    }
    this.previewClose.emit();
  }
}
