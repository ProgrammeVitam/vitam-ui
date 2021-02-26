import {Component, OnInit, ViewChild} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {IngestContract} from 'projects/vitamui-library/src/lib/models/ingest-contract';
import {ApplicationService, GlobalEventService, SidenavPage} from 'ui-frontend-common';

import {IngestContractCreateComponent} from './ingest-contract-create/ingest-contract-create.component';
import {IngestContractListComponent} from './ingest-contract-list/ingest-contract-list.component';

@Component({
  selector: 'app-ingest-contract',
  templateUrl: './ingest-contract.component.html',
  styleUrls: ['./ingest-contract.component.scss']
})
export class IngestContractComponent extends SidenavPage<IngestContract> implements OnInit {


  @ViewChild(IngestContractListComponent, {static: true}) ingestContractListComponent: IngestContractListComponent;

  search = '';
  tenantId: number;
  isSlaveMode: boolean;

  constructor(public dialog: MatDialog, private route: ActivatedRoute, private router: Router, globalEventService: GlobalEventService,
              private applicationService: ApplicationService) {
    super(route, globalEventService);
    globalEventService.tenantEvent.subscribe(() => {
      this.refreshList();
      this.updateSlaveMode();
    });

    this.route.params.subscribe(params => {
      console.log('params: ', params);
      if (params.tenantIdentifier) {
        this.tenantId = +params.tenantIdentifier;
      }
    });
  }

  openCreateIngestcontractDialog() {
    const dialogRef = this.dialog.open(IngestContractCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true
    });
    dialogRef.componentInstance.tenantIdentifier = this.tenantId;
    dialogRef.componentInstance.isSlaveMode = this.isSlaveMode;
    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.ingestContractListComponent) {
      return;
    }
    this.ingestContractListComponent.searchIngestContractOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], {relativeTo: this.route});
  }

  updateSlaveMode() {
    this.applicationService.isApplicationExternalIdentifierEnabled('INGEST_CONTRACT').subscribe((value) => {
      this.isSlaveMode = value;
    });
  }

  ngOnInit() {
    this.updateSlaveMode();
  }

  showIngestContract(item: IngestContract) {
    this.openPanel(item);
  }

}
