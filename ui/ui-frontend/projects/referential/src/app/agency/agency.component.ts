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
import {Component, OnInit, ViewChild} from '@angular/core';
import {VitamUIImportDialogComponent} from '../shared/vitamui-import-dialog/vitamui-import-dialog.component';

import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {GlobalEventService, SidenavPage} from 'ui-frontend-common';
import {Agency} from '../../../../vitamui-library/src/lib/models/agency';
import {Referential} from '../shared/vitamui-import-dialog/referential.enum';
import {AgencyCreateComponent} from './agency-create/agency-create.component';
import {AgencyListComponent} from './agency-list/agency-list.component';
import {AgencyService} from './agency.service';

@Component({
  selector: 'app-agency',
  templateUrl: './agency.component.html',
  styleUrls: ['./agency.component.scss']
})
export class AgencyComponent extends SidenavPage<Agency> implements OnInit {

  search = '';

  @ViewChild(AgencyListComponent, {static: true}) agencyListComponent: AgencyListComponent;

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router,
    globalEventService: GlobalEventService,
    private agencyService: AgencyService) {
    super(route, globalEventService);
  }

  openCreateAgencyDialog() {
    const dialogRef = this.dialog.open(AgencyCreateComponent, {panelClass: 'vitamui-modal', disableClose: true});
    dialogRef.afterClosed().subscribe((result) => {
      if (result.success) {
        this.refreshList();
      }
      if (result.action === 'restart') {
        this.openCreateAgencyDialog();
      }
    });
  }

  openAgencyImportDialog() {
    const dialogRef = this.dialog.open(
      VitamUIImportDialogComponent, {
        panelClass: 'vitamui-modal',
        data: Referential.AGENCY,
        disableClose: true
      });
    dialogRef.afterClosed().subscribe((result) => {
      if (result && result.success) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.agencyListComponent) {
      return;
    }
    this.agencyListComponent.searchAgencyOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  ngOnInit() {
  }

  showAgency(item: Agency) {
    this.openPanel(item);
  }

  exportAgencies() {
    this.agencyService.export();
  }

  changeTenant(tenantIdentifier: number) {
    this.router.navigate(['..', tenantIdentifier], {relativeTo: this.route});
  }

}
