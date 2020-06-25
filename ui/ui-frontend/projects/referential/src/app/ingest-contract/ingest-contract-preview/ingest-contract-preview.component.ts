import {Component, EventEmitter, HostListener, Input, OnInit, Output, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {MatTab, MatTabGroup, MatTabHeader} from '@angular/material/tabs';
import {ConfirmActionComponent, IngestContract} from 'projects/vitamui-library/src/public-api';
import {Observable} from 'rxjs';
import {IngestContractService} from '../ingest-contract.service';
import {IngestContractFormatTabComponent} from './ingest-contract-format-tab/ingest-contract-format-tab.component';
import {IngestContractInformationTabComponent} from './ingest-contract-information-tab/ingest-contract-information-tab.component';
import {IngestContractObjectTabComponent} from './ingest-contract-object-tab/ingest-contract-object-tab.component';

@Component({
  selector: 'app-ingest-contract-preview',
  templateUrl: './ingest-contract-preview.component.html',
  styleUrls: ['./ingest-contract-preview.component.scss']
})
export class IngestContractPreviewComponent implements OnInit {

  @Output() previewClose: EventEmitter<any> = new EventEmitter();
  @Input() ingestContract: IngestContract;
  @Input() tenantIdentifier: number;

  isPopup: boolean;

  tabUpdated: boolean[] = [false, false, false, false, false];
  @ViewChild('tabs', {static: false}) tabs: MatTabGroup;

  tabLinks: Array<IngestContractInformationTabComponent | IngestContractFormatTabComponent | IngestContractObjectTabComponent> = [];
  @ViewChild('infoTab', {static: false}) infoTab: IngestContractInformationTabComponent;
  @ViewChild('formatsTab', {static: false}) formatsTab: IngestContractFormatTabComponent;
  @ViewChild('objectsTab', {static: false}) objectsTab: IngestContractObjectTabComponent;

  @HostListener('window:beforeunload', ['$event'])
  beforeunloadHandler(event: any) {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      event.preventDefault();
      this.checkBeforeExit();
      return '';
    }
  }

  constructor(private matDialog: MatDialog, private ingestContractService: IngestContractService) {
  }

  ngOnInit() {
  }

  ngAfterViewInit = () => {
    this.tabs._handleClick = this.interceptTabChange.bind(this);
    this.tabLinks[0] = this.infoTab;
    this.tabLinks[1] = this.formatsTab;
    this.tabLinks[2] = this.objectsTab;
  }

  updatedChange(updated: boolean, index: number) {
    this.tabUpdated[index] = updated;
  }

  async checkBeforeExit() {
    if (await this.confirmAction()) {
      const submitAccessContractUpdate: Observable<IngestContract> = this.tabLinks[this.tabs.selectedIndex].prepareSubmit();

      submitAccessContractUpdate.subscribe(() => {
        this.ingestContractService.get(this.ingestContract.identifier).subscribe(
          response => {
            this.ingestContract = response;
          }
        );
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
    const dialog = this.matDialog.open(ConfirmActionComponent, {panelClass: 'vitamui-confirm-dialog'});
    dialog.componentInstance.dialogType = 'changeTab';
    return await dialog.afterClosed().toPromise();
  }

  filterEvents(event: any): boolean {
    return event.outDetail && (
      event.outDetail.includes('EXT_VITAMUI_UPDATE_INGEST_CONTRACT') ||
      event.outDetail.includes('EXT_VITAMUI_CREATE_INGEST_CONTRACT')
    );
  }

  async emitClose() {
    if (this.tabUpdated[this.tabs.selectedIndex]) {
      await this.checkBeforeExit();
    }
    this.previewClose.emit();
  }

}
