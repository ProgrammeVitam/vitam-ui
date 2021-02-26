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
import {MatDialog} from '@angular/material/dialog';
import {ActivatedRoute, Router} from '@angular/router';
import {AccessContract} from 'projects/vitamui-library/src/public-api';
import {ApplicationService, GlobalEventService, SidenavPage} from 'ui-frontend-common';

import {AccessContractCreateComponent} from './access-contract-create/access-contract-create.component';
import {AccessContractListComponent} from './access-contract-list/access-contract-list.component';


@Component({
  selector: 'app-access',
  templateUrl: './access-contract.component.html',
  styleUrls: ['./access-contract.component.scss']
})
export class AccessContractComponent extends SidenavPage<AccessContract> implements OnInit {

  search = '';
  tenantId: number;
  isSlaveMode: boolean;

  @ViewChild(AccessContractListComponent, {static: true}) accessContractListComponent: AccessContractListComponent;

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    private router: Router,
    globalEventService: GlobalEventService,
    private applicationService: ApplicationService) {

    super(route, globalEventService);
    globalEventService.tenantEvent.subscribe(() => {
      this.refreshList();
      this.updateSlaveMode();
    });

    this.route.params.subscribe(params => {
      if (params.tenantIdentifier) {
        this.tenantId = params.tenantIdentifier;
      }
    });
  }

  openCreateAccesscontractDialog() {
    const dialogRef = this.dialog.open(AccessContractCreateComponent, {
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
    if (!this.accessContractListComponent) {
      return;
    }
    this.accessContractListComponent.searchAccessContractOrdered();
  }

  changeTenant(tenantIdentifier: number) {
    this.tenantId = tenantIdentifier;
    this.router.navigate(['..', tenantIdentifier], {relativeTo: this.route});
  }

  updateSlaveMode() {
    this.applicationService.isApplicationExternalIdentifierEnabled('ACCESS_CONTRACT').subscribe((value) => {
      this.isSlaveMode = value;
    });
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  ngOnInit() {
    this.updateSlaveMode();
  }

  showAccessContract(item: AccessContract) {
    this.openPanel(item);
  }

}
