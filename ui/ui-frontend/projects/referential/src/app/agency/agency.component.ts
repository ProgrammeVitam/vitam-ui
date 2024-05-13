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
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { FileTypes } from 'projects/vitamui-library/src/public-api';
import { zip } from 'rxjs';
import { Agency, ApplicationId, GlobalEventService, Role, SecurityService, SidenavPage } from 'vitamui-library';
import { ImportDialogParam, ReferentialTypes } from '../shared/import-dialog/import-dialog-param.interface';
import { ImportDialogComponent } from '../shared/import-dialog/import-dialog.component';
import { AgencyCreateComponent } from './agency-create/agency-create.component';
import { AgencyListComponent } from './agency-list/agency-list.component';
import { AgencyService } from './agency.service';

@Component({
  selector: 'app-agency',
  templateUrl: './agency.component.html',
  styleUrls: ['./agency.component.scss'],
})
export class AgencyComponent extends SidenavPage<Agency> implements OnInit {
  @ViewChild(AgencyListComponent, { static: true }) agencyListComponent: AgencyListComponent;

  search = '';
  tenantIdentifier: number;
  hasCreateRole = false;
  hasImportRole = false;
  hasExportRole = false;

  constructor(
    public dialog: MatDialog,
    public globalEventService: GlobalEventService,
    private route: ActivatedRoute,
    private securityService: SecurityService,
    private agencyService: AgencyService,
    private translateService: TranslateService,
  ) {
    super(route, globalEventService);
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = +params.tenantIdentifier;
    });

    zip(
      this.securityService.hasRole(ApplicationId.AGENCIES_APP, this.tenantIdentifier, Role.ROLE_CREATE_AGENCIES),
      this.securityService.hasRole(ApplicationId.AGENCIES_APP, this.tenantIdentifier, Role.ROLE_IMPORT_AGENCIES),
      this.securityService.hasRole(ApplicationId.AGENCIES_APP, this.tenantIdentifier, Role.ROLE_EXPORT_AGENCIES),
    ).subscribe((values: [boolean, boolean, boolean]) => {
      this.hasCreateRole = values[0];
      this.hasImportRole = values[1];
      this.hasExportRole = values[2];
    });
  }

  public openCreateAgencyDialog(): void {
    const dialogRef = this.dialog.open(AgencyCreateComponent, { panelClass: 'vitamui-modal', disableClose: true });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.refreshList();
      }
      if (result?.action === 'restart') {
        this.openCreateAgencyDialog();
      }
    });
  }

  public openAgencyImportDialog(): void {
    const params: ImportDialogParam = {
      title: this.translateService.instant('IMPORT_DIALOG.TITLE'),
      subtitle: this.translateService.instant('IMPORT_DIALOG.AGENCY_SUBTITLE'),
      allowedFiles: [FileTypes.CSV],
      referential: ReferentialTypes.AGENCY,
      successMessage: 'SNACKBAR.AGENCY_CONTRACT_IMPORTED',
      errorMessage: 'SNACKBAR.AGENCY_CONTRACT_IMPORT_FAIL',
      iconMessage: 'vitamui-icon-agent',
    };

    this.dialog
      .open(ImportDialogComponent, {
        panelClass: 'vitamui-modal',
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
