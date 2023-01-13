/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApplicationService, GlobalEventService, ManagementContract, SidenavPage } from 'ui-frontend-common';
import { ManagementContractCreateComponent } from './management-contract-create/management-contract-create.component';
import { ManagementContractListComponent } from './management-contract-list/management-contract-list.component';

@Component({
  selector: 'app-management-contract',
  templateUrl: './management-contract.component.html',
  styleUrls: ['./management-contract.component.scss'],
})
export class ManagementContractComponent extends SidenavPage<ManagementContract> implements OnInit, OnDestroy {
  @ViewChild(ManagementContractListComponent, { static: true }) managementContractListComponent: ManagementContractListComponent;

  search = '';
  tenantId: number;
  isSlaveMode: boolean;

  subscriptions: Subscription = new Subscription();

  constructor(
    public dialog: MatDialog,
    private route: ActivatedRoute,
    globalEventService: GlobalEventService,
    private applicationService: ApplicationService
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
    this.updateSlaveMode();
  }

  openCreateManagementcontractDialog() {
    const dialogRef = this.dialog.open(ManagementContractCreateComponent, {
      panelClass: 'vitamui-modal',
      disableClose: true,
    });
    dialogRef.componentInstance.isSlaveMode = this.isSlaveMode;
    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.managementContractListComponent) {
      return;
    }
    this.managementContractListComponent.searchManagementContractOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  updateSlaveMode() {
    this.subscriptions.add(
      this.applicationService.isApplicationExternalIdentifierEnabled('MANAGEMENT_CONTRACT').subscribe((value) => {
        this.isSlaveMode = value;
      })
    );
  }

  showManagementContract(item: ManagementContract) {
    this.openPanel(item);
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
