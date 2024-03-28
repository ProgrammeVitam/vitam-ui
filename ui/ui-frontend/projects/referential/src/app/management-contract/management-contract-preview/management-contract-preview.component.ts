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
import { Subscription } from 'rxjs';
import { tap } from 'rxjs/operators';
import { ManagementContract } from 'ui-frontend-common';
import { ConfirmActionComponent } from 'vitamui-library';
import { ManagementContractIdentificationTabComponent } from './management-contract-identification-tab/management-contract-identification-tab.component';
import { ManagementContractInformationTabComponent } from './management-contract-information-tab/management-contract-information-tab.component';
import { ManagementContractStorageTabComponent } from './management-contract-storage-tab/management-contract-storage-tab.component';
@Component({
  selector: 'app-management-contract-preview',
  templateUrl: './management-contract-preview.component.html',
  styleUrls: ['./management-contract-preview.component.scss'],
})
export class ManagementContractPreviewComponent implements OnChanges, AfterViewInit {
  @Output() previewClose: EventEmitter<any> = new EventEmitter();
  @Input() inputManagementContract: ManagementContract;
  @ViewChild('tabs', { static: false }) tabs: MatTabGroup;
  @ViewChild('informationTab', { static: false }) informationTab: ManagementContractInformationTabComponent;
  @ViewChild('storageTab', { static: false }) storageTab: ManagementContractStorageTabComponent;
  @ViewChild('identificationTab', { static: false }) identificationTab: ManagementContractIdentificationTabComponent;

  tabUpdated: boolean[] = [false, false, false, false];
  private tabLinks: Array<
    ManagementContractInformationTabComponent | ManagementContractStorageTabComponent | ManagementContractIdentificationTabComponent
  > = [];

  @HostListener('window:beforeunload', ['$event'])
  async beforeunloadHandler(event: any) {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      event.preventDefault();
      await this.checkBeforeExit();
      return '';
    }
  }

  constructor(private matDialog: MatDialog) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.inputManagementContract && this.tabUpdated.some((update) => update)) {
      this.inputManagementContract = changes.inputManagementContract.previousValue;
      this.checkBeforeExit().then(() => {
        this.inputManagementContract = changes.inputManagementContract.currentValue;
      });
    }
  }

  ngAfterViewInit(): void {
    this.tabs._handleClick = this.interceptTabChange.bind(this);
    this.tabLinks.push(this.informationTab);
    this.tabLinks.push(this.storageTab);
    this.tabLinks.push(this.identificationTab);
  }

  updatedChange(updated: boolean, index: number) {
    this.tabUpdated[index] = updated;
  }

  filterEvents(event: any): boolean {
    return (
      event.outDetail &&
      (event.outDetail.toString().includes('STP_UPDATE_MANAGEMENT_CONTRACT') ||
        event.outDetail.toString().includes('STP_IMPORT_MANAGEMENT_CONTRACT'))
    );
  }

  async emitClose() {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      await this.checkBeforeExit();
    }
    this.previewClose.emit();
  }

  async checkBeforeExit() {
    if (await this.confirmAction()) {
      const subscription: Subscription = this.tabLinks[this.tabs.selectedIndex]
        .prepareSubmit()
        .pipe(
          tap((managementContract) => {
            this.inputManagementContract = managementContract;
          }),
        )
        .subscribe(() => subscription.unsubscribe());
    } else {
      this.tabLinks[this.tabs.selectedIndex].resetForm(this.inputManagementContract);
    }
  }

  async confirmAction(): Promise<boolean> {
    const dialog = this.matDialog.open(ConfirmActionComponent, { panelClass: 'vitamui-confirm-dialog' });

    dialog.componentInstance.dialogType = 'changeTab';

    return await dialog.afterClosed().toPromise();
  }

  async interceptTabChange(tab: MatTab, tabHeader: MatTabHeader, idx: number) {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      await this.checkBeforeExit();
    }

    return MatTabGroup.prototype._handleClick.apply(this.tabs, [tab, tabHeader, idx]);
  }
}
