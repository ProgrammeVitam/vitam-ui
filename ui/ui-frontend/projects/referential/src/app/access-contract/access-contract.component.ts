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
import { HttpResponse } from '@angular/common/http';
import { Component, inject, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import {
  AccessContract,
  AccessContractService,
  ApplicationService,
  DownloadUtils,
  FileTypes,
  GlobalEventService,
  SidenavPage,
  SnackBarService,
  VitamuiBannerComponent,
  VitamuiMenuButtonComponent,
  VitamuiTitleBreadcrumbComponent,
} from 'vitamui-library';
import { ImportDialogParam, ReferentialTypes } from '../shared/import-dialog/import-dialog-param.interface';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';

import { firstValueFrom, Subscription } from 'rxjs';
import { DownloadSnackBarService } from '../core/service/download-snack-bar.service';
import { AccessContractCreateComponent } from './access-contract-create/access-contract-create.component';
import { AccessContractListComponent } from './access-contract-list/access-contract-list.component';
import { finalize, shareReplay } from 'rxjs/operators';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { AccessContractPreviewComponent } from './access-contract-preview/access-contract-preview.component';
import { MatMenuItem } from '@angular/material/menu';

const IMPORT_FILE_MODEL_NAME = 'Import_access_contrat_template.csv';

@Component({
  selector: 'app-access',
  templateUrl: './access-contract.component.html',
  styleUrls: ['./access-contract.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    AccessContractPreviewComponent,
    MatSidenavContent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiBannerComponent,
    VitamuiMenuButtonComponent,
    MatMenuItem,
    AccessContractListComponent,
    TranslatePipe,
  ],
})
export class AccessContractComponent extends SidenavPage<AccessContract> implements OnInit, OnDestroy {
  override globalEventService: GlobalEventService;
  private dialog = inject(MatDialog);
  route: ActivatedRoute;
  private readonly accessContractService = inject(AccessContractService);
  private applicationService = inject(ApplicationService);
  private translateService = inject(TranslateService);
  private downloadSnackBarService = inject(DownloadSnackBarService);
  private snackBarService = inject(SnackBarService);

  public search = '';
  public tenantIdentifier: number;

  @ViewChild(AccessContractListComponent, { static: true }) accessContractListComponent: AccessContractListComponent;

  #isSlaveMode$ = this.applicationService.isApplicationExternalIdentifierEnabled('ACCESS_CONTRACT').pipe(shareReplay(1));

  constructor() {
    const globalEventService = inject(GlobalEventService);
    const route = inject(ActivatedRoute);

    super(route, globalEventService);

    this.globalEventService = globalEventService;
    this.route = route;
  }

  ngOnInit() {
    this.route.params.subscribe((params) => (this.tenantIdentifier = params['tenantIdentifier']));
    this.globalEventService.tenantEvent.subscribe(() => this.refreshList());
  }

  override ngOnDestroy() {
    super.ngOnDestroy();
  }

  async openCreateAccessContractDialog() {
    const isSlaveMode = await firstValueFrom(this.#isSlaveMode$);
    this.dialog.closeAll(); // Prevent opening multiple dialogs
    const dialogRef = this.dialog.open<AccessContractCreateComponent, AccessContractCreateComponent['data']>(
      AccessContractCreateComponent,
      {
        disableClose: true,
        data: {
          tenantIdentifier: this.tenantIdentifier,
          isSlaveMode: isSlaveMode,
        },
      },
    );

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
      fileFormatDetailInfo: this.translateService.instant('IMPORT_DIALOG.SCHEMA_FORMAT_CSV_SEMICOLON'),
      allowedFiles: [FileTypes.CSV, FileTypes.VND],
      referential: ReferentialTypes.ACCESS_CONTRACTS,
      successMessage: 'SNACKBAR.ACCESS_CONTRACT_IMPORTED',
      iconMessage: 'vitamui-icon-user',
    };

    this.dialog
      .open(ImportDialogComponent, { disableClose: true, data: params })
      .afterClosed()
      .subscribe((result) => {
        if (result?.successfulImport) {
          this.refreshList();
        }
      });
  }

  public export(): void {
    this.downloadSnackBarService.openDownloadBar();
    const request: Subscription = this.accessContractService
      .prepareSignedExportAccessContracts()
      .pipe(finalize(() => this.downloadSnackBarService.close()))
      .subscribe({
        next: (url) => this.snackBarService.startDownload(url),
      });

    this.downloadSnackBarService.cancelDownload.subscribe(() => request.unsubscribe());
  }

  public downloadModel(): void {
    this.accessContractService.downloadImportAccessContractFileModel().subscribe((response: HttpResponse<Blob>) => {
      DownloadUtils.loadFromBlob(response, response.body.type, IMPORT_FILE_MODEL_NAME);
      this.snackBarService.notifyDownloadStarted();
    });
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  private refreshList() {
    if (!this.accessContractListComponent) {
      return;
    }

    this.accessContractListComponent.searchAccessContractOrdered();
  }
}
