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
import { Component, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { ApplicationService, DownloadUtils, GlobalEventService, IngestContract, SecurityService, SidenavPage } from 'vitamui-library';
import { DownloadSnackBarService } from './../core/service/download-snack-bar.service';
import { Observable, Subscription } from 'rxjs';
import { mergeMap } from 'rxjs/operators';
import { IngestContractCreateComponent } from './ingest-contract-create/ingest-contract-create.component';
import { IngestContractListComponent } from './ingest-contract-list/ingest-contract-list.component';
import { ImportDialogParam, ReferentialTypes } from '../shared/import-dialog/import-dialog-param.interface';
import { TranslateService } from '@ngx-translate/core';
import { FileTypes } from 'projects/vitamui-library/src/lib/models/file-types.enum';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';
import { IngestContractService } from './ingest-contract.service';
import { HttpResponse } from '@angular/common/http';

@Component({
  selector: 'app-ingest-contract',
  templateUrl: './ingest-contract.component.html',
  styleUrls: ['./ingest-contract.component.scss'],
})
export class IngestContractComponent extends SidenavPage<IngestContract> implements OnInit {
  @ViewChild(IngestContractListComponent, { static: true }) ingestContractListComponent: IngestContractListComponent;

  search = '';
  tenantId: number;
  isSlaveMode: boolean;

  tenantIdentifier: number;
  appName = 'INGEST_APP';
  hasUpdateIngestRole$: Observable<boolean>;

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router,
    globalEventService: GlobalEventService,
    private applicationService: ApplicationService,
    private securityService: SecurityService,
    private translateService: TranslateService,
    private downloadSnackBarService: DownloadSnackBarService,
    private ingestContractService: IngestContractService,
  ) {
    super(route, globalEventService);
    globalEventService.tenantEvent.subscribe(() => {
      this.refreshList();
      this.updateSlaveMode();
    });

    this.route.params.subscribe((params) => {
      if (params.tenantIdentifier) {
        this.tenantId = +params.tenantIdentifier;
      }
    });
  }

  ngOnInit() {
    this.hasUpdateIngestRole$ = this.route.params.pipe(
      mergeMap((params) => {
        this.tenantIdentifier = +params.tenantIdentifier;
        return this.securityService.hasRole(this.appName, this.tenantIdentifier, 'ROLE_UPDATE_INGEST_CONTRACTS');
      }),
    );

    this.updateSlaveMode();
  }

  openCreateIngestcontractDialog() {
    const dialogRef = this.dialog.open(IngestContractCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
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
    this.router.navigate(['..', tenantIdentifier], { relativeTo: this.route });
  }

  updateSlaveMode() {
    this.applicationService.isApplicationExternalIdentifierEnabled('INGEST_CONTRACT').subscribe((value) => {
      this.isSlaveMode = value;
    });
  }

  showIngestContract(item: IngestContract) {
    this.openPanel(item);
  }

  public openImport(): void {
    const params: ImportDialogParam = {
      title: this.translateService.instant('IMPORT_DIALOG.TITLE'),
      subtitle: this.translateService.instant('IMPORT_DIALOG.INGEST_CONTRACT_SUBTITLE'),
      fileFormatDetailInfo: this.translateService.instant('IMPORT_DIALOG.FILE_FORMAT_DETAIL_INFO'),
      allowedFiles: [FileTypes.CSV],
      referential: ReferentialTypes.INGEST_CONTRACT,
      successMessage: 'SNACKBAR.INGEST_CONTRACT_IMPORTED',
      iconMessage: 'vitamui-icon-user',
    };

    this.dialog
      .open(ImportDialogComponent, { panelClass: 'vitamui-modal', disableClose: true, data: params })
      .afterClosed()
      .subscribe((result) => {
        if (result?.successfulImport) {
          this.refreshList();
        }
      });
  }

  public downloadModel(): void {
    this.ingestContractService
      .downloadImportFileModel()
      .subscribe((response: HttpResponse<Blob>) =>
        DownloadUtils.loadFromBlob(response, response.body.type, 'Import_ingest_contract_template.csv'),
      );
  }

  public export(): void {
    this.downloadSnackBarService.openDownloadBar();
    const request: Subscription = this.ingestContractService.exportIngestContracts().subscribe(
      (response: HttpResponse<Blob>) => {
        DownloadUtils.loadFromBlob(response, response.body.type, 'Exported_ingest_contracts.csv');
        this.downloadSnackBarService.close();
      },
      () => this.downloadSnackBarService.close(),
    );

    this.downloadSnackBarService.cancelDownload.subscribe(() => request.unsubscribe());
  }
}
