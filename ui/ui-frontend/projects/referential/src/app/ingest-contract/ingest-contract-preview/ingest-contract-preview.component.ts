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
import {
  AfterViewInit,
  Component,
  EventEmitter,
  HostListener,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild,
  inject,
} from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTab, MatTabGroup, MatTabHeader } from '@angular/material/tabs';
import { Observable } from 'rxjs';
import { IngestContract } from '../../../../../vitamui-library/src/app/modules/models/ingest-contract/ingest-contract';
import { ConfirmActionComponent } from '../../../../../vitamui-library/src/lib/components/confirm-action/confirm-action.component';
import { IngestContractService } from '../ingest-contract.service';
import { IngestContractFormatTabComponent } from './ingest-contract-format-tab/ingest-contract-format-tab.component';
import { IngestContractHeritageTabComponent } from './ingest-contract-heritage-tab/ingest-contract-heritage-tab.component';
import { IngestContractInformationTabComponent } from './ingest-contract-information-tab/ingest-contract-information-tab.component';
import { IngestContractObjectTabComponent } from './ingest-contract-object-tab/ingest-contract-object-tab.component';
import { IngestContractSignatureTabComponent } from './ingest-contract-signature-tab/ingest-contract-signature-tab.component';

@Component({
  selector: 'app-ingest-contract-preview',
  templateUrl: './ingest-contract-preview.component.html',
  styleUrls: ['./ingest-contract-preview.component.scss'],
  standalone: false,
})
export class IngestContractPreviewComponent implements OnChanges, AfterViewInit {
  private matDialog = inject(MatDialog);
  private ingestContractService = inject(IngestContractService);

  @Output() previewClose: EventEmitter<any> = new EventEmitter();
  @Input() ingestContract: IngestContract;
  @Input() tenantIdentifier: number;
  @Input() readOnly: boolean;

  isPopup: boolean;

  @ViewChild('tabs', { static: false }) tabs: MatTabGroup;
  updatableTabs: {
    id: string;
    component?:
      | IngestContractInformationTabComponent
      | IngestContractFormatTabComponent
      | IngestContractObjectTabComponent
      | IngestContractHeritageTabComponent
      | IngestContractSignatureTabComponent;
    updated: boolean;
    valid: boolean;
  }[] = [];

  @ViewChild('infoTab', { static: false }) infoTab: IngestContractInformationTabComponent;
  @ViewChild('formatsTab', { static: false }) formatsTab: IngestContractFormatTabComponent;
  @ViewChild('objectsTab', { static: false }) objectsTab: IngestContractObjectTabComponent;
  @ViewChild('heritageTab', { static: false }) heritageTab: IngestContractHeritageTabComponent;
  @ViewChild('signatureTab', { static: false }) signatureTab: IngestContractSignatureTabComponent;

  @HostListener('window:beforeunload', ['$event'])
  async beforeunloadHandler(event: any) {
    const activeTab = this.getActiveTab();
    if (activeTab.updated === true) {
      event.preventDefault();
      await this.checkBeforeExit();
      return '';
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.hasOwnProperty('ingestContract')) {
      this.ingestContract = changes.ingestContract.currentValue;
    }
  }

  ngAfterViewInit() {
    this.tabs._handleClick = this.interceptTabChange.bind(this);
    this.updatableTabs = [
      { id: 'infoTab', component: this.infoTab, updated: false, valid: false },
      { id: 'formatsTab', component: this.formatsTab, updated: false, valid: false },
      { id: 'objectsTab', component: this.objectsTab, updated: false, valid: false },
      { id: 'heritageTab', component: this.heritageTab, updated: false, valid: true },
      { id: 'attachmentTab', updated: false, valid: true },
      { id: 'signatureTab', component: this.signatureTab, updated: false, valid: true },
      { id: 'historyTab', updated: false, valid: true },
    ];
  }

  tabUpdated(updated: boolean, index: string) {
    if (this.updatableTabs.length === 0) return;
    const updatableTab = this.updatableTabs.find((tab) => tab.id === index);
    updatableTab.updated = updated;
  }

  tabValidityChanged(isFormTabValid: boolean, index: string) {
    if (this.updatableTabs.length === 0) return;
    const updatableTab = this.updatableTabs.find((tab) => tab.id === index);
    updatableTab.valid = isFormTabValid;
  }

  async checkBeforeExit() {
    const activeTabComponent = this.getActiveTab().component;
    // if we didn't define the tab component we don't need to check it
    if (!activeTabComponent) {
      return;
    }
    if (await this.confirmAction()) {
      const submitAccessContractUpdate: Observable<IngestContract> = activeTabComponent.prepareSubmit();

      submitAccessContractUpdate.subscribe(() => {
        this.ingestContractService.get(this.ingestContract.identifier).subscribe((response) => {
          this.ingestContract = response;
        });
      });
    } else {
      activeTabComponent.resetForm(this.ingestContract);
    }
  }

  async interceptTabChange(tab: MatTab, tabHeader: MatTabHeader, idx: number) {
    const activeTab = this.getActiveTab();
    if (activeTab.valid && activeTab.updated) {
      await this.checkBeforeExit();
    }
    const args = [tab, tabHeader, idx];
    return MatTabGroup.prototype._handleClick.apply(this.tabs, args);
  }

  async confirmAction(): Promise<boolean> {
    const dialog = this.matDialog.open(ConfirmActionComponent, { panelClass: 'small' });
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
    const activeTab = this.getActiveTab();
    if (activeTab.valid && activeTab.updated) {
      await this.checkBeforeExit();
    }
    this.previewClose.emit();
  }

  updatedIngestContract(ingestContract: IngestContract) {
    this.ingestContract = ingestContract;
  }

  private getActiveTab() {
    const activeIndex = this.tabs.selectedIndex;
    return this.updatableTabs[activeIndex];
  }
}
