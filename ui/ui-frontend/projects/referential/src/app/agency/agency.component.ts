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
import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslatePipe, TranslateService } from '@ngx-translate/core';
import {
  Agency,
  AgencyService,
  ApplicationId,
  FileTypes,
  GlobalEventService,
  QueryParamsService,
  Role,
  SecurityService,
  SidenavPage,
  VitamUICommonModule,
} from 'vitamui-library';
import { zip } from 'rxjs';
import { ImportDialogParam, ReferentialTypes } from '../shared/import-dialog/import-dialog-param.interface';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';
import { AgencyCreateComponent } from './agency-create/agency-create.component';
import { AgencyListComponent } from './agency-list/agency-list.component';
import { AgencyCreateModule } from './agency-create';
import { AgencyPreviewComponent } from './agency-preview/agency-preview.component';

import { ImportDialogModule } from '../shared/import-dialog/import-dialog.module';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatMenuItem } from '@angular/material/menu';
import { map } from 'rxjs/operators';

@Component({
  selector: 'app-agency',
  templateUrl: './agency.component.html',
  styleUrls: ['./agency.component.scss'],
  imports: [
    AgencyCreateModule,
    AgencyListComponent,
    AgencyPreviewComponent,
    ImportDialogModule,
    MatMenuItem,
    MatProgressSpinnerModule,
    MatSidenavModule,
    TranslatePipe,
    VitamUICommonModule,
  ],
})
export class AgencyComponent extends SidenavPage<Agency> implements OnInit {
  dialog = inject(MatDialog);
  globalEventService: GlobalEventService;
  private route: ActivatedRoute;
  private securityService = inject(SecurityService);
  private agencyService = inject(AgencyService);
  private translateService = inject(TranslateService);
  private router = inject(Router);
  private queryParamsService = inject(QueryParamsService);

  @ViewChild(AgencyListComponent, { static: true }) agencyListComponent: AgencyListComponent;

  search = '';
  tenantIdentifier: number;
  hasCreateRole = false;
  hasImportRole = false;
  hasExportRole = false;
  hasUpdateRole = false;

  constructor() {
    const globalEventService = inject(GlobalEventService);
    const route = inject(ActivatedRoute);

    super(route, globalEventService);

    this.globalEventService = globalEventService;
    this.route = route;
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = +params.tenantIdentifier;
    });
    this.queryParamsService
      .getQueryParams()
      .pipe(map((queryParam) => queryParam.s || ''))
      .subscribe((s) => (this.search = s));

    zip(
      this.securityService.hasRole$(ApplicationId.AGENCIES_APP, Role.ROLE_CREATE_AGENCIES, this.tenantIdentifier),
      this.securityService.hasRole$(ApplicationId.AGENCIES_APP, Role.ROLE_IMPORT_AGENCIES, this.tenantIdentifier),
      this.securityService.hasRole$(ApplicationId.AGENCIES_APP, Role.ROLE_EXPORT_AGENCIES, this.tenantIdentifier),
      this.securityService.hasRole$(ApplicationId.AGENCIES_APP, Role.ROLE_UPDATE_AGENCIES, this.tenantIdentifier),
    ).subscribe((values: [boolean, boolean, boolean, boolean]) => {
      this.hasCreateRole = values[0];
      this.hasImportRole = values[1];
      this.hasExportRole = values[2];
      this.hasUpdateRole = values[3];
    });
  }

  public openCreateAgencyDialog(): void {
    const dialogRef = this.dialog.open(AgencyCreateComponent, { disableClose: true });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.refreshList();
      }
      if (result?.action === 'edit') {
        this.router.navigateByUrl(`/agency/tenant/${this.tenantIdentifier}/agencies/${result?.agency?.identifier}/edit`);
      }
    });
  }

  public openAgencyImportDialog(): void {
    const params: ImportDialogParam = {
      title: this.translateService.instant('IMPORT_DIALOG.TITLE'),
      subtitle: this.translateService.instant('IMPORT_DIALOG.AGENCY_SUBTITLE'),
      allowedFiles: [FileTypes.CSV, FileTypes.VND],
      referential: ReferentialTypes.AGENCY,
      successMessage: 'SNACKBAR.AGENCY_CONTRACT_IMPORTED',
      errorMessage: 'SNACKBAR.AGENCY_CONTRACT_IMPORT_FAIL',
      iconMessage: 'vitamui-icon-agent',
    };

    this.dialog
      .open(ImportDialogComponent, {
        disableClose: true,
        data: params,
      })
      .afterClosed()
      .subscribe((result) => {
        if (result?.successfulImport) {
          this.refreshList();
        }
      });
  }

  public onSearchSubmit(search: string): void {
    this.search = search || '';
    this.queryParamsService.setQueryParams({ s: this.search || undefined });
  }

  public showAgency(item: Agency): void {
    this.openPanel(item);
  }

  public exportAgencies(): void {
    this.agencyService.export();
  }

  private refreshList() {
    if (!this.agencyListComponent) {
      return;
    }
    this.agencyListComponent.searchAgencyOrdered();
  }
}
