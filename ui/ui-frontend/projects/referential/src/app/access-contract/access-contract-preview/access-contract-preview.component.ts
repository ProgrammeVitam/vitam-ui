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
import { AfterViewInit, Component, EventEmitter, HostListener, Input, Output, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTab, MatTabGroup, MatTabHeader } from '@angular/material/tabs';
import { Observable } from 'rxjs';
import { AccessContract, ConfirmActionComponent, AccessContractService } from 'vitamui-library';
import { AccessContractInformationTabComponent } from './access-contract-information-tab/access-contract-information-tab.component';
import { AccessContractAuthorizationsTabComponent } from './access-contract-authorizations-tab/access-contract-authorizations-tab.component';
import { AccessContractWriteAccessTabComponent } from './access-contract-write-access-tab/access-contract-write-access-tab.component';

@Component({
  selector: 'app-access-contract-preview',
  templateUrl: './access-contract-preview.component.html',
  styleUrls: ['./access-contract-preview.component.scss'],
  standalone: false,
})
export class AccessContractPreviewComponent implements AfterViewInit {
  @Input() accessContract: AccessContract;
  @Input() tenantIdentifier: number;

  @Output() previewClose: EventEmitter<any> = new EventEmitter();

  @ViewChild('tabs', { static: false }) tabs: MatTabGroup;
  updatableTabs: {
    id: string;
    component?: AccessContractInformationTabComponent | AccessContractWriteAccessTabComponent;
  }[] = [];

  @ViewChild('infoTab', { static: false }) infoTab: AccessContractInformationTabComponent;
  @ViewChild('authorizationsTab', { static: false }) authorizationsTab: AccessContractAuthorizationsTabComponent;
  @ViewChild('writeTab', { static: false }) writeTab: AccessContractWriteAccessTabComponent;

  constructor(
    private matDialog: MatDialog,
    private accessContractService: AccessContractService,
  ) {}

  ngAfterViewInit() {
    this.tabs._handleClick = this.interceptTabChange.bind(this);
    this.updatableTabs = [
      { id: 'infoTab', component: this.infoTab },
      { id: 'authorizationsTab' },
      { id: 'writeTab', component: this.writeTab },
      { id: 'positionTab' },
      { id: 'historyTab' },
    ];
  }

  @HostListener('window:beforeunload', ['$event'])
  async beforeunloadHandler(event: any) {
    const activeTab = this.getActiveTab();
    if (activeTab?.component?.unChanged === false) {
      event.preventDefault();
      await this.checkBeforeExit();
      return '';
    }
  }

  public async emitClose() {
    await this.checkBeforeExit();

    this.previewClose.emit();
  }

  public filterEvents(event: any): boolean {
    return (
      event.outDetail &&
      (event.outDetail.includes('STP_UPDATE_ACCESS_CONTRACT') ||
        event.outDetail.includes('STP_IMPORT_ACCESS_CONTRACT') ||
        event.outDetail.includes('STP_BACKUP_ACCESS_CONTRACT'))
    );
  }

  private async checkBeforeExit() {
    const activeTabComponent = this.getActiveTab().component;
    // If we didn't define the tab component we don't need to check it.
    if (!activeTabComponent || activeTabComponent.form.invalid || activeTabComponent.unChanged) {
      return;
    }
    if (await this.confirmAction()) {
      const submitAccessContractUpdate: Observable<AccessContract> = activeTabComponent.prepareSubmit();

      submitAccessContractUpdate.subscribe(() => {
        this.accessContractService.get(this.accessContract.identifier).subscribe((response) => {
          this.accessContract = response;
        });
      });
    } else {
      activeTabComponent.resetForm(this.accessContract);
    }
  }

  private async interceptTabChange(tab: MatTab, tabHeader: MatTabHeader, idx: number) {
    await this.checkBeforeExit();

    const args = [tab, tabHeader, idx];
    return MatTabGroup.prototype._handleClick.apply(this.tabs, args);
  }

  private async confirmAction(): Promise<boolean> {
    const dialog = this.matDialog.open(ConfirmActionComponent, { panelClass: 'small' });
    dialog.componentInstance.dialogType = 'changeTab';
    return await dialog.afterClosed().toPromise();
  }

  updatedAccessContract(accessContract: AccessContract) {
    this.accessContract = accessContract;
  }

  private getActiveTab() {
    const activeIndex = this.tabs.selectedIndex;
    return this.updatableTabs[activeIndex];
  }
}
