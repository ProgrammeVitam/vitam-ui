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
import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';

import {
  ApplicationService,
  Context,
  GlobalEventService,
  SidenavPage,
  VitamuiBannerComponent,
  VitamuiTitleBreadcrumbComponent,
} from 'vitamui-library';

import { ContextCreateComponent } from './context-create/context-create.component';
import { ContextListComponent } from './context-list/context-list.component';
import { shareReplay } from 'rxjs/operators';
import { firstValueFrom } from 'rxjs';
import { MatSidenav, MatSidenavContainer, MatSidenavContent } from '@angular/material/sidenav';
import { ContextPreviewComponent } from './context-preview/context-preview.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-context',
  templateUrl: './context.component.html',
  styleUrls: ['./context.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    ContextPreviewComponent,
    MatSidenavContent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiBannerComponent,
    ContextListComponent,
    TranslatePipe,
  ],
})
export class ContextComponent extends SidenavPage<Context> implements OnInit {
  dialog = inject(MatDialog);
  route: ActivatedRoute;
  private applicationService = inject(ApplicationService);

  search = '';
  tenantIdentifier: string;

  @ViewChild(ContextListComponent, { static: true }) contextListComponent: ContextListComponent;

  #isSlaveMode$ = this.applicationService.isApplicationExternalIdentifierEnabled('CONTEXT').pipe(shareReplay(1));

  constructor() {
    const route = inject(ActivatedRoute);
    const globalEventService = inject(GlobalEventService);

    super(route, globalEventService);

    this.route = route;
  }

  async openCreateContextDialog() {
    const isSlaveMode = await firstValueFrom(this.#isSlaveMode$);
    this.dialog.closeAll(); // Prevent opening multiple dialogs
    const dialogRef = this.dialog.open<ContextCreateComponent, ContextCreateComponent['data']>(ContextCreateComponent, {
      disableClose: true,
      data: {
        isSlaveMode: isSlaveMode,
      },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.contextListComponent) {
      return;
    }
    this.contextListComponent.searchContextOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  ngOnInit() {
    this.route.params.subscribe((params) => (this.tenantIdentifier = params['tenantIdentifier']));
  }

  showContext(item: Context) {
    this.openPanel(item);
  }
}
