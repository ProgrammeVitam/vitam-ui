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
import { AfterViewInit, Component, EventEmitter, HostListener, Input, OnChanges, Output, SimpleChanges, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTab, MatTabGroup, MatTabHeader } from '@angular/material/tabs';
import { ConfirmActionComponent } from 'projects/vitamui-library/src/public-api';
import { Observable } from 'rxjs';
import { IngestContract } from 'ui-frontend-common';
import { IngestContractService } from '../ingest-contract.service';
import { IngestContractFormatTabComponent } from './ingest-contract-format-tab/ingest-contract-format-tab.component';
import { IngestContractHeritageTabComponent } from './ingest-contract-heritage-tab/ingest-contract-heritage-tab.component';
import { IngestContractInformationTabComponent } from './ingest-contract-information-tab/ingest-contract-information-tab.component';
import { IngestContractObjectTabComponent } from './ingest-contract-object-tab/ingest-contract-object-tab.component';

@Component({
  selector: 'app-ingest-contract-preview',
  templateUrl: './ingest-contract-preview.component.html',
  styleUrls: ['./ingest-contract-preview.component.scss'],
})
export class IngestContractPreviewComponent implements OnChanges, AfterViewInit {
  @Output() previewClose: EventEmitter<any> = new EventEmitter();
  @Input() ingestContract: IngestContract;
  @Input() tenantIdentifier: number;
  @Input() readOnly: boolean;

  isPopup: boolean;

  tabUpdated: boolean[] = [false, false, false, false, false, false];
  @ViewChild('tabs', { static: false }) tabs: MatTabGroup;

  tabLinks: Array<
    | IngestContractInformationTabComponent
    | IngestContractFormatTabComponent
    | IngestContractObjectTabComponent
    | IngestContractHeritageTabComponent
  > = [];
  @ViewChild('infoTab', { static: false }) infoTab: IngestContractInformationTabComponent;
  @ViewChild('formatsTab', { static: false }) formatsTab: IngestContractFormatTabComponent;
  @ViewChild('objectsTab', { static: false }) objectsTab: IngestContractObjectTabComponent;
  @ViewChild('heritageTab', { static: false }) heritageTab: IngestContractHeritageTabComponent;

  @HostListener('window:beforeunload', ['$event'])
  async beforeunloadHandler(event: any) {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      event.preventDefault();
      await this.checkBeforeExit();
      return '';
    }
  }

  constructor(
    private matDialog: MatDialog,
    private ingestContractService: IngestContractService,
  ) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.hasOwnProperty('ingestContract')) {
      this.ingestContract = changes.ingestContract.currentValue;
    }
  }

  ngAfterViewInit() {
    this.tabs._handleClick = this.interceptTabChange.bind(this);
    this.tabLinks[0] = this.infoTab;
    this.tabLinks[1] = this.formatsTab;
    this.tabLinks[2] = this.objectsTab;
    this.tabLinks[3] = this.heritageTab;
  }

  updatedChange(updated: boolean, index: number) {
    this.tabUpdated[index] = updated;
  }

  async checkBeforeExit() {
    if (await this.confirmAction()) {
      const submitAccessContractUpdate: Observable<IngestContract> = this.tabLinks[this.tabs.selectedIndex].prepareSubmit();

      submitAccessContractUpdate.subscribe(() => {
        this.ingestContractService.get(this.ingestContract.identifier).subscribe((response) => {
          this.ingestContract = response;
        });
      });
    } else {
      this.tabLinks[this.tabs.selectedIndex].resetForm(this.ingestContract);
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

  filterEvents(event: any): boolean {
    return (
      event.outDetail &&
      (event.outDetail.includes('EXT_VITAMUI_UPDATE_INGEST_CONTRACT') || event.outDetail.includes('EXT_VITAMUI_CREATE_INGEST_CONTRACT'))
    );
  }

  async emitClose() {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      await this.checkBeforeExit();
    }
    this.previewClose.emit();
  }
}
