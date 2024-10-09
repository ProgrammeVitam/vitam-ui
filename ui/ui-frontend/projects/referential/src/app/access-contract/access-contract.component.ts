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
import { HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { FileTypes } from 'projects/vitamui-library/src/lib/models/file-types.enum';
import { AccessContract, ApplicationService, DownloadUtils, GlobalEventService, SidenavPage, AccessContractService } from 'vitamui-library';
import { ImportDialogParam, ReferentialTypes } from '../shared/import-dialog/import-dialog-param.interface';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';

import { Subject, Subscription } from 'rxjs';
import { DownloadSnackBarService } from '../core/service/download-snack-bar.service';
import { AccessContractCreateComponent } from './access-contract-create/access-contract-create.component';
import { AccessContractListComponent } from './access-contract-list/access-contract-list.component';

@Component({
  selector: 'app-access',
  templateUrl: './access-contract.component.html',
  styleUrls: ['./access-contract.component.scss'],
})
export class AccessContractComponent extends SidenavPage<AccessContract> implements OnInit, OnDestroy {
  public search = '';
  public tenantIdentifier: number;
  public isSlaveMode = false;

  @ViewChild(AccessContractListComponent, { static: true }) accessContractListComponent: AccessContractListComponent;

  private readonly destroyer$ = new Subject<void>();

  constructor(
    public globalEventService: GlobalEventService,
    private dialog: MatDialog,
    private route: ActivatedRoute,
    private readonly accessContractService: AccessContractService,
    private applicationService: ApplicationService,
    private translateService: TranslateService,
    private downloadSnackBarService: DownloadSnackBarService,
  ) {
    super(route, globalEventService);
  }

  ngOnInit() {
    this.route.params.subscribe((params) => (this.tenantIdentifier = params.tenantIdentifier));
    this.globalEventService.tenantEvent.subscribe(() => {
      this.refreshList();
      this.updateSlaveMode();
    });

    this.updateSlaveMode();
  }

  ngOnDestroy() {
    super.ngOnDestroy();
    this.destroyer$.next();
    this.destroyer$.complete();
  }

  public openCreateAccesscontractDialog() {
    const dialogRef = this.dialog.open(AccessContractCreateComponent, { panelClass: 'vitamui-modal', disableClose: true });
    dialogRef.componentInstance.tenantIdentifier = this.tenantIdentifier;
    dialogRef.componentInstance.isSlaveMode = this.isSlaveMode;

    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        this.refreshList();
      }
    });
  }

  public openImport(): void {
    const params: ImportDialogParam = {
      title: this.translateService.instant('IMPORT_DIALOG.TITLE'),
      subtitle: this.translateService.instant('IMPORT_DIALOG.ACCESS_CONTRACT_SUBTITLE'),
      fileFormatDetailInfo: this.translateService.instant('IMPORT_DIALOG.FILE_FORMAT_DETAIL_INFO'),
      allowedFiles: [FileTypes.CSV, FileTypes.VND],
      referential: ReferentialTypes.ACCESS_CONTRACT,
      successMessage: 'SNACKBAR.ACCESS_CONTRACT_IMPORTED',
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

  public export(): void {
    this.downloadSnackBarService.openDownloadBar();
    const request: Subscription = this.accessContractService.exportAccessContracts().subscribe(
      (response: HttpResponse<Blob>) => {
        DownloadUtils.loadFromBlob(response, response.body.type, 'Exported_access_contracts.csv');
        this.downloadSnackBarService.close();
      },
      () => this.downloadSnackBarService.close(),
    );

    this.downloadSnackBarService.cancelDownload.subscribe(() => request.unsubscribe());
  }

  public downloadModel(): void {
    this.accessContractService
      .downloadImportAccessContractFileModel()
      .subscribe((response: HttpResponse<Blob>) =>
        DownloadUtils.loadFromBlob(response, response.body.type, 'Import_access_contrat_template.csv'),
      );
  }

  public onSearchSubmit(search: string) {
    this.search = search || '';
  }

  private refreshList() {
    if (!this.accessContractListComponent) {
      return;
    }

    this.accessContractListComponent.searchAccessContractOrdered();
  }

  private updateSlaveMode() {
    this.applicationService.isApplicationExternalIdentifierEnabled('ACCESS_CONTRACT').subscribe((value) => {
      this.isSlaveMode = value;
    });
  }
}
