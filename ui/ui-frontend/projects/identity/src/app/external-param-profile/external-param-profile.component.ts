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
import { ExternalParamProfile, GlobalEventService, SidenavPage } from 'ui-frontend-common';
import { ExternalParamProfileCreateComponent } from './external-param-profile-create/external-param-profile-create.component';
import { ExternalParamProfileListComponent } from './external-param-profile-list/external-param-profile-list.component';
import { ExternalParamProfileService } from './external-param-profile.service';

@Component({
  selector: 'app-external-param-profile',
  templateUrl: './external-param-profile.component.html',
  styleUrls: ['./external-param-profile.component.css'],
})
export class ExternalParamProfileComponent extends SidenavPage<ExternalParamProfile> implements OnInit {
  dto: ExternalParamProfile;
  tenantIdentifier: string;
  public search: string;
  @ViewChild(ExternalParamProfileListComponent, { static: true }) externalParamProfileListComponent: ExternalParamProfileListComponent;

  constructor(
    public dialog: MatDialog,
    public route: ActivatedRoute,
    public globalEventService: GlobalEventService,
    public externalParamProfileServiceService: ExternalParamProfileService,
  ) {
    super(route, globalEventService);
  }

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      this.tenantIdentifier = params.tenantIdentifier;
    });
  }

  onSearchSubmit(search: string) {
    this.search = search;
  }

  openExternalParamProfilCreateDialog() {
    const dialogRef = this.dialog.open(ExternalParamProfileCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
      data: { tenantIdentifier: this.tenantIdentifier },
    });
    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.externalParamProfileListComponent) {
      return;
    }
    this.externalParamProfileListComponent.search();
  }
}
