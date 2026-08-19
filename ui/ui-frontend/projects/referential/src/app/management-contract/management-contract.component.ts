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
import { Component, inject, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import {
  ApplicationService,
  GlobalEventService,
  ManagementContract,
  SidenavPage,
  TooltipDirective,
  VitamuiBannerComponent,
  VitamuiTitleBreadcrumbComponent,
} from 'vitamui-library';
import { ManagementContractCreateComponent } from './management-contract-create/management-contract-create.component';
import { ManagementContractListComponent } from './management-contract-list/management-contract-list.component';
import { shareReplay } from 'rxjs/operators';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { ManagementContractPreviewComponent } from './management-contract-preview/management-contract-preview.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-management-contract',
  templateUrl: './management-contract.component.html',
  styleUrls: ['./management-contract.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    ManagementContractPreviewComponent,
    MatSidenavContent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiBannerComponent,
    TooltipDirective,
    ManagementContractListComponent,
    TranslatePipe,
  ],
})
export class ManagementContractComponent extends SidenavPage<ManagementContract> {
  dialog = inject(MatDialog);
  private route: ActivatedRoute;
  private applicationService = inject(ApplicationService);

  @ViewChild(ManagementContractListComponent, { static: true }) managementContractListComponent: ManagementContractListComponent;

  search = '';
  tenantId: number;
  isSlaveMode: boolean;

  #isSlaveMode$ = this.applicationService.isApplicationExternalIdentifierEnabled('MANAGEMENT_CONTRACT').pipe(shareReplay(1));

  constructor() {
    const route = inject(ActivatedRoute);
    const globalEventService = inject(GlobalEventService);

    super(route, globalEventService);
    this.route = route;

    globalEventService.tenantEvent.subscribe(() => {
      this.refreshList();
    });

    this.route.params.subscribe((params) => {
      if (params['tenantIdentifier']) {
        this.tenantId = +params['tenantIdentifier'];
      }
    });
  }

  async openCreateManagementContractDialog() {
    const isSlaveMode = await firstValueFrom(this.#isSlaveMode$);
    this.dialog.closeAll(); // Prevent opening multiple dialogs
    const dialogRef = this.dialog.open<ManagementContractCreateComponent, ManagementContractCreateComponent['data']>(
      ManagementContractCreateComponent,
      {
        disableClose: true,
        data: {
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

  private refreshList() {
    if (!this.managementContractListComponent) {
      return;
    }
    this.managementContractListComponent.searchManagementContractOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  showManagementContract(item: ManagementContract) {
    this.openPanel(item);
  }
}
