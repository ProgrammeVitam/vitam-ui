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
import { Component, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import {
  Event,
  GlobalEventService,
  SearchBarComponent,
  SidenavPage,
  VitamuiTitleBreadcrumbComponent,
  VitamuiBannerComponent,
  DatepickerComponent,
} from 'vitamui-library';
import { AuditChainCreateComponent } from './audit-chain-create/audit-chain-create.component';
import { AuditCreateComponent } from './audit-create/audit-create.component';
import { AuditListComponent } from './audit-list/audit-list.component';
import { DateTime } from 'luxon';
import { MatSidenavContainer, MatSidenav, MatSidenavContent } from '@angular/material/sidenav';
import { AuditPreviewComponent } from './audit-preview/audit-preview.component';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-audit',
  templateUrl: './audit.component.html',
  styleUrls: ['./audit.component.scss'],
  imports: [
    MatSidenavContainer,
    MatSidenav,
    AuditPreviewComponent,
    MatSidenavContent,
    VitamuiTitleBreadcrumbComponent,
    VitamuiBannerComponent,
    ReactiveFormsModule,
    DatepickerComponent,
    AuditListComponent,
    TranslatePipe,
  ],
})
export class AuditComponent extends SidenavPage<Event> {
  dialog = inject(MatDialog);
  route: ActivatedRoute;
  override globalEventService: GlobalEventService;
  private formBuilder = inject(FormBuilder);

  public dateRangeFilterForm: FormGroup;
  public filters: any = {};
  public search: string;
  public tenantIdentifier: string;

  @ViewChild(SearchBarComponent, { static: true }) searchBar: SearchBarComponent;
  @ViewChild(AuditListComponent, { static: true }) auditListComponent: AuditListComponent;

  constructor() {
    const route = inject(ActivatedRoute);
    const globalEventService = inject(GlobalEventService);

    super(route, globalEventService);
    this.route = route;
    this.globalEventService = globalEventService;

    route.params.subscribe((params) => {
      this.tenantIdentifier = params['tenantIdentifier'];
    });

    this.dateRangeFilterForm = this.formBuilder.group({
      startDate: null,
      endDate: null,
    });

    this.dateRangeFilterForm.controls['startDate'].valueChanges.subscribe((value: Date) => {
      this.filters = { ...this.filters, startDate: value ? DateTime.fromJSDate(value).startOf('day').toISO() : null };
    });

    this.dateRangeFilterForm.controls['endDate'].valueChanges.subscribe((value: Date) => {
      this.filters = { ...this.filters, endDate: value ? DateTime.fromJSDate(value).endOf('day').toISO() : null };
    });
  }

  openCreateAuditDialog() {
    const dialogRef = this.dialog.open(AuditCreateComponent, { disableClose: true });
    dialogRef.componentInstance.tenantIdentifier = +this.tenantIdentifier;
    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined && result.success) {
        this.refreshList();
      }
    });
  }

  openCreateChainAuditDialog() {
    const dialogRef = this.dialog.open(AuditChainCreateComponent, { disableClose: true });
    dialogRef.afterClosed().subscribe((result) => {
      if (result !== undefined && result.success) {
        this.refreshList();
      }
    });
  }

  private refreshList() {
    if (!this.auditListComponent) {
      return;
    }

    this.auditListComponent.searchAuditOrdered();
  }

  onSearchSubmit(search: string) {
    this.search = search || '';
  }

  showAudit(item: Event) {
    this.openPanel(item);
  }
}
